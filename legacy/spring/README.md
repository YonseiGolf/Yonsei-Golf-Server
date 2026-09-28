# Yonsei-Golf-Server

## Profiles

- `home`: MinIO endpoint, static MinIO credentials, public-read object ACL, and bucket-prefixed public URLs.
- `aws`: EC2 instance-role credentials, private Amazon S3 objects, and CloudFront public URLs.

The default profile is `home`. Set `SPRING_PROFILES_ACTIVE=aws` on the AWS EC2 instance. The local `application.properties` file is used only as a source for generating runtime secrets; `.dockerignore` prevents it from being copied into an image.

## Build and push

Build a multi-architecture image so the same tag works on the ARM64 AWS instance and an AMD64 home server:

```bash
docker login
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  --tag birdiehyun/yg-server:latest \
  --push \
  .
```

To build only for the current AWS `t4g.micro` instance:

```
docker buildx build \
  --platform linux/arm64 \
  --tag birdiehyun/yg-server:latest \
  --push \
  .
```

## AWS runtime configuration

Generate `.env.aws` from the `yg-server` Terragrunt outputs, the RDS secret in AWS Secrets Manager, and the untracked local `application.properties`:

```bash
export AWS_PROFILE=yg-server
./scripts/generate-aws-env.sh
```

The generated file is mode `0600` and ignored by Git and Docker. It intentionally does not contain an AWS access key; the `aws` profile uses the EC2 instance role.

Copy the Compose file and generated environment to the instance, then start Spring and a local Redis container:

```bash
INFRA_DIR=../yg-server/infra/live/dev
EC2_IP="$(cd "${INFRA_DIR}" && terragrunt output -raw ec2_public_ip)"
YG_SSH_PRIVATE_KEY="$HOME/.ssh/id_ed25519_personal" # YG_SSH_PUBLIC_KEY와 짝인 개인 키

ssh -i "${YG_SSH_PRIVATE_KEY}" "ubuntu@${EC2_IP}" 'mkdir -p ~/yg-server'
scp -i "${YG_SSH_PRIVATE_KEY}" \
  compose.aws.yml .env.aws \
  "ubuntu@${EC2_IP}:yg-server/"
ssh -i "${YG_SSH_PRIVATE_KEY}" "ubuntu@${EC2_IP}" \
  'cd ~/yg-server && docker compose --env-file .env.aws -f compose.aws.yml up -d'
```

Check startup and application logs:

```bash
ssh -i "${YG_SSH_PRIVATE_KEY}" "ubuntu@${EC2_IP}" \
  'cd ~/yg-server && docker compose -f compose.aws.yml ps && docker compose -f compose.aws.yml logs --tail=200 spring-server'
```
