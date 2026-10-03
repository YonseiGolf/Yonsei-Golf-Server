# Yonsei Golf Server

NestJS 12 + TypeScript + TypeORM + MySQL API 서버입니다. pnpm으로 의존성을 관리하고 Biome으로 lint/format을 수행합니다. DB 스키마는 Flyway만 변경합니다.

## 로컬 실행

Node.js 24 LTS와 Docker가 필요합니다. `packageManager`에 고정된 pnpm 버전을 사용합니다.

```sh
corepack enable
pnpm install --frozen-lockfile
cp .env.example .env
pnpm db:up
pnpm db:migrate
pnpm start:dev
```

서버는 `http://localhost:8080`, MySQL은 `127.0.0.1:3307`, Mailpit 웹 UI는 `http://localhost:8025`, 로컬 MinIO는 `http://localhost:9000`에서 실행됩니다. 공식 MinIO 이미지(`quay.io/minio/minio`, `minio/minio`)는 공개 배포가 중단되어 로컬 개발과 통합 테스트에서는 커뮤니티 빌드 `pgsty/minio`를 사용합니다. `minio-init`이 로컬 버킷 `yg-local`과 읽기 정책을 생성합니다. 개발용 MinIO는 로컬 테스트용으로만 제공하며 운영 스토리지는 기존 S3/MinIO endpoint를 사용합니다.

실제 카카오 로그인에는 `.env`의 카카오 앱 설정이 필요합니다. `KAKAO_TOKEN_URL`은 토큰 교환 endpoint이고, `KAKAO_CALLBACK_URL`은 인증 코드를 발급받을 때 사용한 redirect URI입니다. 이전 설정의 `KAKAO_REDIRECT_URI`는 토큰 endpoint로 취급하므로 두 값을 혼동하지 마세요. 통합 테스트는 외부 카카오 계정 없이 실행됩니다.

## 검증

```sh
pnpm check
pnpm docker:build
pnpm test:container
```

`pnpm check`는 Biome 검사, TypeScript 타입 검사, Nest 빌드, 아키텍처·도메인 단위 테스트, 통합 테스트를 실행합니다. 단위 테스트(`pnpm test:unit`)는 Docker 없이 헥사고날 계층 규칙과 도메인 규칙을 검사합니다. 통합 테스트는 매번 임시 MySQL 8.4 컨테이너에 `db/migration`의 Flyway SQL 전체를 적용하고, Nest HTTP 요청과 실제 DB 행을 검증합니다. Repository·SQL·트랜잭션을 모킹하거나 SQLite로 대체하지 않습니다. 카카오는 로컬 HTTP 서버, 메일은 로컬 SMTP 서버, 이미지 업로드는 실제 MinIO 컨테이너를 사용합니다. 테스트 설정은 운영 `.env`를 읽지 않습니다.

`pnpm test:container`는 빌드한 Flyway 이미지와 Node.js 24 운영 이미지를 기동해 HTTP 요청이 MySQL에 저장되는지 검증합니다. 모든 테스트 컨테이너는 종료 시 정리됩니다. 첫 실행에는 이미지 다운로드 시간이 필요합니다.

개별 명령: `pnpm lint`, `pnpm lint:fix`, `pnpm typecheck`, `pnpm build`, `pnpm test:unit`, `pnpm test:integration`.

CI는 `TZ=UTC`와 `TZ=Asia/Seoul`에서 통합 테스트를 각각 실행합니다. 로컬에서도 `TZ=UTC pnpm test:integration`으로 시간대에 따른 날짜 회귀를 확인할 수 있습니다.

## 구조

```text
src/
  domain/         엔티티와 도메인 규칙 (user, recruitment, apply, mail)
  application/    기능 슬라이스별 유스케이스와 포트 (provided·required)
    user/           카카오 로그인, 토큰, 가입, 권한, 회원
    recruitment/    모집 기간, 면접 시간, 모집 시작 알림 메일
    apply/          지원서, 접수·결과 메일, 사진 업로드 URL
    mail/           관리자가 고치는 메일 양식(제목·본문)과 기본 문구
    shared/         페이지, ID, 날짜, 메일 발송 계약
  adapter/        바깥 세계와 닿는 구현
    webapi/         컨트롤러, 인증 가드, 예외 필터, 요청 로그
    security/       JWT
    integration/    카카오, SMTP, S3/MinIO
    config/         환경 변수, TypeORM 설정, Nest 모듈 조립
  support/        오류 체계와 역할 데코레이터
db/migration/     Flyway SQL (V1~V9는 Spring 시절 원본, V10부터 추가)
test/             아키텍처·도메인 단위 테스트, 실제 MySQL 통합 테스트, 운영 이미지 테스트
legacy/spring/    이관 전 Java 소스·테스트·빌드·운영 자료
```

의존은 `adapter → application → domain` 방향으로만 흐르는 헥사고날 구조이며, 규칙과 결정은 [아키텍처 문서](docs/ARCHITECTURE.md)에 있습니다. 이관 범위, 동작 변경 및 검증 내용은 [이관 기록](docs/MIGRATION.md)에 정리되어 있습니다. `legacy/spring`은 참고용이며 현재 빌드나 배포에 포함되지 않습니다. 기존 비공개 `src/main/resources/application.properties`는 이동하거나 커밋하지 않으며, AWS 환경 파일 생성 스크립트의 입력으로 사용할 수 있습니다.

## Flyway와 데이터

`db/migration`의 기존 SQL은 내용과 체크섬을 보존했습니다. 새 스키마 변경은 `V11__description.sql`부터 추가합니다. `V10`은 쓰지 않는 게시판·댓글·게시판 템플릿·이미지·쿠폰 테이블(`board`, `board_template`, `reply`, `image`, `coupon`, `user_coupon`)을 삭제합니다. 적용된 SQL이나 `flyway_schema_history`를 수정하지 마세요. TypeORM의 `synchronize`와 `migrationsRun`은 항상 `false`입니다.

```sh
pnpm db:validate
pnpm db:migrate
```

Flyway는 SQL 이력을 검증하고 적용합니다. ORM 매핑은 실제 MySQL 통합 테스트로 검증합니다. 운영에서는 자동 baseline과 `clean`을 비활성화합니다. 기존 DB에 Flyway 이력이 없다면 실제 스키마와 V1~V9 적용 상태를 먼저 대조해야 합니다.

## AWS 배포

`Dockerfile`은 `runtime`과 `migrations` 두 이미지를 빌드합니다. Java/Flyway는 일회성 migration 이미지에만 있고, API 이미지는 Node.js 24와 운영 의존성만 포함합니다. API는 root가 아닌 `node` 계정으로 실행합니다.

`dev` 브랜치 CI가 모든 검증을 통과한 뒤 AMD64/ARM64 이미지를 Docker Hub에 게시합니다. GitHub Secrets의 `DOCKER_USERNAME`, `DOCKER_PASSWORD`가 필요합니다. 이 workflow는 서버를 직접 배포하지 않습니다.

같은 commit의 이미지 태그 쌍을 사용하는 것을 권장합니다.

```sh
export AWS_PROFILE=yg-server
export YG_APP_IMAGE=birdiehyun/yg-server:<commit-sha>
export YG_FLYWAY_IMAGE=birdiehyun/yg-server:flyway-<commit-sha>
./scripts/generate-aws-env.sh
```

`<commit-sha>`는 실제 commit SHA로 바꿉니다. 생성된 `.env.aws`와 `compose.aws.yml`을 EC2에 전달한 뒤 실행합니다. 비밀 환경 파일은 저장소와 이미지에서 제외되며, AWS 접근키 없이 instance role을 사용합니다. `STORAGE_PROVIDER=s3`(`APP_PROFILE=aws`의 기본값)에서는 객체를 private으로 업로드하고 CloudFront 기반 URL을 반환합니다.

```sh
docker compose --env-file .env.aws -f compose.aws.yml pull
docker compose --env-file .env.aws -f compose.aws.yml up -d
docker compose --env-file .env.aws -f compose.aws.yml ps
docker compose --env-file .env.aws -f compose.aws.yml logs --tail=100 app
```

앱은 Flyway migration 작업이 성공해야 시작됩니다. 기존 Spring Compose의 `spring-server`와 새 `app`은 동일한 컨테이너 이름을 쓰므로, 최초 전환 시 기존 서버만 중지·제거한 뒤 새 Compose를 시작해야 합니다. DB와 스토리지 볼륨을 삭제하지 마세요. 전환 전 DB 백업과 `flyway info/validate` 확인을 권장합니다.

현재 이관은 새 DDL 없이 기존 스키마를 사용하므로 기존 Spring 이미지로 앱을 되돌릴 수 있습니다. 이후 스키마를 변경했다면 앱 이미지 복귀만으로 DB 변경이 되돌아가지는 않습니다. 기존 Redis는 현재 기능에서 사용하지 않아 새 Compose에서 제외했으며, 운영 중인 Redis와 데이터는 이 PR에서 삭제하지 않습니다.

### 이미지 스토리지로 MinIO 사용

이미지 스토리지는 `APP_PROFILE`과 별도로 `STORAGE_PROVIDER=minio|s3`로 선택합니다. 생략하면 `home`은 `minio`, `aws`는 `s3`입니다. AWS 배포에서 MinIO를 쓰려면 아래 키를 담은 파일을 준비해 스크립트에 전달합니다.

```sh
# minio.env
S3_ENDPOINT=https://minio.example.com
AWS_S3_BUCKET=yg-img-storage
AWS_S3_PUBLIC_URL=https://minio.example.com
AWS_ACCESS_KEY_ID=<MinIO access key>
AWS_SECRET_ACCESS_KEY=<MinIO secret key>
```

```sh
export YG_STORAGE_PROVIDER=minio
export YG_MINIO_ENV_FILE=/path/to/minio.env
./scripts/generate-aws-env.sh
```

브라우저가 presigned URL로 MinIO에 직접 업로드하므로 `S3_ENDPOINT`는 브라우저에서 접근할 수 있는 공개 주소여야 합니다. 이미지 URL은 `${AWS_S3_PUBLIC_URL}/${AWS_S3_BUCKET}/<key>` 형식이며, 버킷에는 익명 `s3:GetObject`만 허용하는 정책이 필요합니다. 접근키는 해당 버킷만 쓸 수 있는 전용 계정으로 발급하세요.

## 맥미니(home) 배포

`compose.home.yml`은 API, 전용 MySQL 8.4, Flyway를 한 호스트에서 실행합니다. 이미지는 `STORAGE_PROVIDER=minio`(home 기본값)로 외부 MinIO에 저장합니다. API는 `127.0.0.1:8080`에만 열리므로 Cloudflare Tunnel 같은 리버스 프록시로 외부에 공개합니다.

`.env.home`은 compose 변수와 API 환경변수를 함께 담으며 저장소에 커밋하지 않습니다.

```sh
APP_IMAGE=birdiehyun/yg-server:<commit-sha>
FLYWAY_IMAGE=birdiehyun/yg-server:flyway-<commit-sha>
MYSQL_DATABASE=ygserver
MYSQL_ROOT_PASSWORD=<random>
DATABASE_USERNAME=yg_server
DATABASE_PASSWORD=<random>
JWT_SECRET_KEY=<Base64, 디코딩 후 32바이트 이상>
KAKAO_CLIENT_ID=<카카오 앱 키>
KAKAO_CLIENT_SECRET=<카카오 client secret>
SMTP_HOST=<SMTP host>
SMTP_PORT=587
SMTP_USERNAME=<SMTP 계정>
SMTP_PASSWORD=<SMTP 비밀번호>
SMTP_REQUIRE_TLS=true
STORAGE_PROVIDER=minio
S3_ENDPOINT=https://minio.example.com
AWS_S3_BUCKET=yg-img-storage
AWS_S3_PUBLIC_URL=https://minio.example.com
AWS_ACCESS_KEY_ID=<MinIO access key>
AWS_SECRET_ACCESS_KEY=<MinIO secret key>
```

```sh
docker compose --env-file .env.home -f compose.home.yml pull
docker compose --env-file .env.home -f compose.home.yml up -d
docker compose --env-file .env.home -f compose.home.yml ps
docker compose --env-file .env.home -f compose.home.yml logs --tail=100 app
```

### 자동 배포

`dev`에 머지되면 `be-deploy.yml`이 테스트 → 이미지 게시 → `deploy.yml` 순서로 실행합니다. `deploy.yml`의 러너는 Tailscale에 `tag:ci`로 붙어 맥미니 SSH에 `yg-server <커밋 SHA>`만 요청합니다. 맥미니의 SSH 키는 `~/deploy/deploy.sh`만 실행하도록 제한돼 있고(Tailscale 주소에서만, 셸·포워딩 불가), 스크립트가 `.env.home`의 이미지 태그를 바꿔 `pull`·`up -d`(Flyway 포함)를 실행합니다. 헬스체크가 실패하면 이전 태그로 되돌리고, 결과를 Slack으로 알립니다.

특정 커밋으로 다시 배포하거나 롤백하려면 Actions → **Deploy to Mac mini** → Run workflow에 커밋 SHA(40자)를 넣습니다. 해당 커밋의 이미지가 Docker Hub에 있어야 합니다. Flyway는 되돌리지 않으므로, 스키마를 바꾼 커밋 이전으로 롤백할 때는 이전 코드가 새 스키마에서 동작하는지 확인합니다.

필요한 저장소 secrets: `TS_OAUTH_CLIENT_ID`, `TS_OAUTH_SECRET`(Tailscale OAuth client, Auth Keys write, `tag:ci`), `MACMINI_SSH_KEY`, `MACMINI_KNOWN_HOSTS`.

기존 DB를 옮길 때는 `mysql`만 먼저 시작해 덤프를 넣은 뒤 전체를 시작합니다. 덤프에 `flyway_schema_history`가 있어야 Flyway가 기존 이력을 검증하고 새 migration만 적용합니다.

```sh
docker compose --env-file .env.home -f compose.home.yml up -d mysql
docker compose --env-file .env.home -f compose.home.yml exec -T mysql \
  sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' < dump.sql
docker compose --env-file .env.home -f compose.home.yml up -d
```

DB 데이터는 `mysql-data` 볼륨에 있으므로 `down -v`를 사용하지 마세요. 백업은 같은 방식으로 `mysqldump --single-transaction`을 실행해 받습니다.

## 인증·환경 호환성

JWT는 기존 Base64 비밀키를 디코딩해 HS256으로 검증하고 `userProfile` claim을 유지합니다. 키는 디코딩 후 32바이트 이상이어야 합니다. 키를 교체하면 기존 토큰은 다시 로그인해야 합니다. 기존 `SPRING_PROFILES_ACTIVE`, `SPRING_MAIL_*`, JDBC 형식의 `DATABASE_URL`, `AWS_S3_ENDPOINT`, `AWS_ACCESS_KEY`도 입력 별칭으로 지원합니다.

운영 refresh cookie는 `HttpOnly`, `Secure`, `SameSite=None`입니다. 로컬 HTTP 개발에서는 `.env.example`처럼 `COOKIE_SECURE=false`를 사용하면 `SameSite=Lax`로 발급합니다. 메일과 카카오 호출에는 타임아웃을 설정합니다. 개인 지원서 데이터나 비밀키가 오류 응답에 포함되지 않도록 공통 예외 처리를 사용합니다.

## 로그

요청마다 응답이 끝날 때 한 줄을 남깁니다(`context: HTTP`). 정상 응답은 `log`, 4xx와 도중에 끊긴 요청은 `warn`, 5xx는 `error` 레벨입니다. 성공한 `/healthcheck`는 Docker와 외부 확인이 자주 호출하므로 남기지 않습니다.

| 필드 | 내용 |
|---|---|
| `requestId` | 요청 ID. 들어온 `X-Request-Id` 또는 Cloudflare `cf-ray`를 쓰고, 없으면 새로 만듭니다. 응답 헤더 `X-Request-Id`로도 돌려줍니다 |
| `method`, `path`, `route`, `query` | 요청 경로. `route`는 `/admin/forms/:id` 같은 라우트 템플릿이고, `query`에서 이름에 token·code·secret·password·key가 들어간 값은 가립니다 |
| `status`, `durationMs`, `aborted` | 응답 코드, 처리 시간, 응답 전에 연결이 끊겼는지 |
| `userId` / `kakaoId` | 인증된 호출자 (회원 토큰 / 가입 전 카카오 토큰) |
| `ip`, `userAgent` | `cf-connecting-ip`(없으면 소켓 주소), 브라우저 정보 |
| `error`, `cause` | 실패 이유. `cause`는 응답에 넣지 않는 내부 원인입니다 (예: `TokenExpiredError: jwt expired`, `Kakao token 400: invalid_grant KOE320`, SMTP 오류) |
| `stack` | 예상하지 못한 5xx의 스택 트레이스 |

요청 본문, 헤더(Authorization 포함), 쿠키는 남기지 않습니다.

`NODE_ENV=production`이면 한 줄에 JSON 객체 하나로 출력하고, 그 밖에는 사람이 읽는 텍스트로 출력합니다. `LOG_FORMAT=json|text`로 바꿀 수 있습니다. Grafana(Loki)에서는 이렇게 찾습니다.

```logql
{container="yg-server"} | json | status >= 500
{container="yg-server"} | json | level="warn" | route="/oauth/kakao"
{container="yg-server"} | json | requestId="<응답 헤더의 X-Request-Id>"
```
