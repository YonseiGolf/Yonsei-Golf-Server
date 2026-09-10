#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
PROJECT_DIR=$(cd -- "${SCRIPT_DIR}/.." && pwd)
INFRA_DIR=${YG_INFRA_DIR:-"${PROJECT_DIR}/../yg-server/infra/live/dev"}
PROPERTIES_FILE=${YG_PROPERTIES_FILE:-"${PROJECT_DIR}/src/main/resources/application.properties"}
OUTPUT_FILE=${YG_AWS_ENV_FILE:-"${PROJECT_DIR}/.env.aws"}
AWS_PROFILE_NAME=${AWS_PROFILE:-yg-server}
AWS_REGION_NAME=${AWS_REGION:-ap-northeast-2}
APP_IMAGE_NAME=${YG_APP_IMAGE:-birdiehyun/yg-server:latest}

for command_name in aws jq openssl terragrunt; do
    if ! command -v "${command_name}" >/dev/null 2>&1; then
        echo "required command not found: ${command_name}" >&2
        exit 1
    fi
done

if [[ ! -d "${INFRA_DIR}" ]]; then
    echo "Terragrunt directory not found: ${INFRA_DIR}" >&2
    exit 1
fi

if [[ ! -f "${PROPERTIES_FILE}" ]]; then
    echo "Local application.properties not found: ${PROPERTIES_FILE}" >&2
    exit 1
fi

property_value() {
    local key=$1
    local value
    value=$(awk -v property_key="${key}" '
        index($0, property_key "=") == 1 {
            print substr($0, length(property_key) + 2)
            exit
        }
    ' "${PROPERTIES_FILE}")

    if [[ -z "${value}" ]]; then
        echo "required property is missing or empty: ${key}" >&2
        exit 1
    fi

    printf '%s' "${value}"
}

terragrunt_output() {
    local output_name=$1
    (cd -- "${INFRA_DIR}" && terragrunt output -raw "${output_name}")
}

quote_env_value() {
    local value=$1
    value=${value//\\/\\\\}
    value=${value//\"/\\\"}
    value=${value//$'\n'/\\n}
    printf '"%s"' "${value}"
}

write_env() {
    local key=$1
    local value=$2
    printf '%s=' "${key}"
    quote_env_value "${value}"
    printf '\n'
}

rds_secret_arn=$(terragrunt_output rds_master_secret_arn)
rds_secret_json=$(aws secretsmanager get-secret-value \
    --profile "${AWS_PROFILE_NAME}" \
    --region "${AWS_REGION_NAME}" \
    --secret-id "${rds_secret_arn}" \
    --query SecretString \
    --output text)

database_host=$(jq -er '.host' <<<"${rds_secret_json}")
database_port=$(jq -er '.port' <<<"${rds_secret_json}")
database_name=$(jq -er '.dbname' <<<"${rds_secret_json}")
database_username=$(jq -er '.username' <<<"${rds_secret_json}")
database_password=$(jq -er '.password' <<<"${rds_secret_json}")
s3_bucket=$(terragrunt_output s3_bucket_name)
image_base_url=$(terragrunt_output image_url)
redis_password=${YG_REDIS_PASSWORD:-$(openssl rand -hex 24)}

umask 077
temporary_file=$(mktemp "${OUTPUT_FILE}.tmp.XXXXXX")
trap 'rm -f "${temporary_file}"' EXIT

{
    write_env SPRING_PROFILES_ACTIVE aws
    write_env APP_IMAGE "${APP_IMAGE_NAME}"
    write_env AWS_REGION "${AWS_REGION_NAME}"
    write_env S3_BUCKET "${s3_bucket}"
    write_env IMAGE_BASE_URL "${image_base_url}"
    write_env AWS_S3_BUCKET "${s3_bucket}"
    write_env AWS_S3_PUBLIC_URL "${image_base_url}"
    write_env DATABASE_URL "jdbc:mysql://${database_host}:${database_port}/${database_name}"
    write_env DATABASE_USERNAME "${database_username}"
    write_env DATABASE_PASSWORD "${database_password}"
    write_env REDIS_HOST redis
    write_env REDIS_PORT 6379
    write_env REDIS_PASSWORD "${redis_password}"
    write_env KAKAO_CLIENT_ID "$(property_value KAKAO_CLIENT_ID)"
    write_env KAKAO_CLIENT_SECRET "$(property_value KAKAO_CLIENT_SECRET)"
    write_env KAKAO_REDIRECT_URI "$(property_value KAKAO_REDIRECT_URI)"
    write_env KAKAO_LOGIN_URI "$(property_value KAKAO_LOGIN_URI)"
    write_env JWT_SECRET_KEY "$(property_value JWT_SECRET_KEY)"
    write_env ALGORITHM "$(property_value ALGORITHM)"
    write_env SECRET_KEY "$(property_value SECRET_KEY)"
    write_env SPRING_MAIL_HOST "$(property_value spring.mail.host)"
    write_env SPRING_MAIL_PORT "$(property_value spring.mail.port)"
    write_env SPRING_MAIL_USERNAME "$(property_value spring.mail.username)"
    write_env SPRING_MAIL_PASSWORD "$(property_value spring.mail.password)"
    write_env SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH \
        "$(property_value spring.mail.properties.mail.smtp.auth)"
    write_env SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE \
        "$(property_value spring.mail.properties.mail.smtp.starttls.enable)"
} >"${temporary_file}"

mv "${temporary_file}" "${OUTPUT_FILE}"
trap - EXIT
chmod 0600 "${OUTPUT_FILE}"

echo "AWS runtime environment created: ${OUTPUT_FILE}"
echo "No AWS access key was copied; the aws profile uses the EC2 instance role."
