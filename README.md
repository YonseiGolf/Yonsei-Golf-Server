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

서버는 `http://localhost:8080`, MySQL은 `127.0.0.1:3307`, Mailpit 웹 UI는 `http://localhost:8025`, 로컬 MinIO는 `http://localhost:9000`에서 실행됩니다. `minio-init`이 로컬 버킷 `yg-local`과 읽기 정책을 생성합니다. 개발용 MinIO는 로컬 테스트용으로만 제공하며 운영 스토리지는 기존 S3/MinIO endpoint를 사용합니다.

실제 카카오 로그인에는 `.env`의 카카오 앱 설정이 필요합니다. `KAKAO_TOKEN_URL`은 토큰 교환 endpoint이고, `KAKAO_CALLBACK_URL`은 인증 코드를 발급받을 때 사용한 redirect URI입니다. 이전 설정의 `KAKAO_REDIRECT_URI`는 토큰 endpoint로 취급하므로 두 값을 혼동하지 마세요. 통합 테스트는 외부 카카오 계정 없이 실행됩니다.

## 검증

```sh
pnpm check
pnpm docker:build
pnpm test:container
```

`pnpm check`는 Biome 검사, TypeScript 타입 검사, Nest 빌드, 통합 테스트를 실행합니다. 통합 테스트는 매번 임시 MySQL 8.4 컨테이너에 실제 Flyway V1~V9를 적용하고, Nest HTTP 요청과 실제 DB 행을 검증합니다. Repository·SQL·트랜잭션을 모킹하거나 SQLite로 대체하지 않습니다. 카카오는 로컬 HTTP 서버, 메일은 로컬 SMTP 서버, 이미지 업로드는 실제 MinIO 컨테이너를 사용합니다. 테스트 설정은 운영 `.env`를 읽지 않습니다.

`pnpm test:container`는 빌드한 Flyway 이미지와 Node.js 24 운영 이미지를 기동해 HTTP 요청이 MySQL에 저장되는지 검증합니다. 모든 테스트 컨테이너는 종료 시 정리됩니다. 첫 실행에는 이미지 다운로드 시간이 필요합니다.

개별 명령: `pnpm lint`, `pnpm lint:fix`, `pnpm typecheck`, `pnpm build`, `pnpm test:integration`.

## 구조

```text
src/
  users/          카카오 로그인, JWT, 권한, 회원
  boards/         게시판, 댓글, 템플릿
  applications/   모집, 면접 시간, 지원서
  email/          SMTP, 발송 이력과 재시도 처리
  storage/        S3/MinIO presigned PUT URL
  database/       TypeORM 설정과 공통 매핑
  common/         응답, 검증, 날짜, 예외 처리
db/migration/     기존 Flyway SQL (V1~V9)
test/             실제 MySQL 통합 테스트와 운영 이미지 테스트
legacy/spring/    이관 전 Java 소스·테스트·빌드·운영 자료
```

이관 범위, 동작 변경 및 검증 내용은 [이관 기록](docs/MIGRATION.md)에 정리되어 있습니다. `legacy/spring`은 참고용이며 현재 빌드나 배포에 포함되지 않습니다. 기존 비공개 `src/main/resources/application.properties`는 이동하거나 커밋하지 않으며, AWS 환경 파일 생성 스크립트의 입력으로 사용할 수 있습니다.

## Flyway와 데이터

`db/migration`의 기존 SQL은 내용과 체크섬을 보존했습니다. 새 스키마 변경은 `V10__description.sql`부터 추가합니다. 적용된 SQL이나 `flyway_schema_history`를 수정하지 마세요. TypeORM의 `synchronize`와 `migrationsRun`은 항상 `false`입니다.

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

`<commit-sha>`는 실제 commit SHA로 바꿉니다. 생성된 `.env.aws`와 `compose.aws.yml`을 EC2에 전달한 뒤 실행합니다. 비밀 환경 파일은 저장소와 이미지에서 제외되며, AWS 접근키 없이 instance role을 사용합니다. `APP_PROFILE=aws`에서는 객체를 private으로 업로드하고 CloudFront 기반 URL을 반환합니다.

```sh
docker compose --env-file .env.aws -f compose.aws.yml pull
docker compose --env-file .env.aws -f compose.aws.yml up -d
docker compose --env-file .env.aws -f compose.aws.yml ps
docker compose --env-file .env.aws -f compose.aws.yml logs --tail=100 app
```

앱은 Flyway migration 작업이 성공해야 시작됩니다. 기존 Spring Compose의 `spring-server`와 새 `app`은 동일한 컨테이너 이름을 쓰므로, 최초 전환 시 기존 서버만 중지·제거한 뒤 새 Compose를 시작해야 합니다. DB와 스토리지 볼륨을 삭제하지 마세요. 전환 전 DB 백업과 `flyway info/validate` 확인을 권장합니다.

현재 이관은 새 DDL 없이 기존 스키마를 사용하므로 기존 Spring 이미지로 앱을 되돌릴 수 있습니다. 이후 스키마를 변경했다면 앱 이미지 복귀만으로 DB 변경이 되돌아가지는 않습니다. 기존 Redis는 현재 기능에서 사용하지 않아 새 Compose에서 제외했으며, 운영 중인 Redis와 데이터는 이 PR에서 삭제하지 않습니다.

## 인증·환경 호환성

JWT는 기존 Base64 비밀키를 디코딩해 HS256으로 검증하고 `userProfile` claim을 유지합니다. 키는 디코딩 후 32바이트 이상이어야 합니다. 키를 교체하면 기존 토큰은 다시 로그인해야 합니다. 기존 `SPRING_PROFILES_ACTIVE`, `SPRING_MAIL_*`, JDBC 형식의 `DATABASE_URL`, `AWS_S3_ENDPOINT`, `AWS_ACCESS_KEY`도 입력 별칭으로 지원합니다.

운영 refresh cookie는 `HttpOnly`, `Secure`, `SameSite=None`입니다. 로컬 HTTP 개발에서는 `.env.example`처럼 `COOKIE_SECURE=false`를 사용하면 `SameSite=Lax`로 발급합니다. 메일과 카카오 호출에는 타임아웃을 설정합니다. 개인 지원서 데이터나 비밀키가 오류 응답에 포함되지 않도록 공통 예외 처리를 사용합니다.
