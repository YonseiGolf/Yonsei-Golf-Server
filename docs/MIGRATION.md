# NestJS 이관 진행 기록

## 목표와 원칙

- NestJS + TypeScript + TypeORM + MySQL, 패키지 매니저 pnpm, lint/format Biome.
- 스키마 변경은 Flyway만 수행한다. TypeORM synchronize와 자체 migration 실행은 끈다.
- 통합 테스트는 격리된 Testcontainers MySQL에서 실제 Flyway를 실행한 뒤 HTTP 요청과 DB 저장 결과를 검증한다.
- Repository, DataSource, 트랜잭션, SQL을 모킹하지 않는다. 외부 카카오·SMTP·스토리지는 로컬 테스트 서버를 사용할 수 있다.
- 기존 API 경로와 주요 응답 형식을 유지한다. 개선한 동작은 이 문서에 기록한다.
- 운영 DB나 외부 수신자에게 테스트 요청을 보내지 않는다.

## 단계

- [x] 1. pnpm / Biome / Nest / MySQL / Flyway / 통합 테스트 기반
- [x] 2. 회원 / 카카오 인증 / 권한 / 게시판 / 댓글 / 템플릿
- [x] 3. 모집 / 면접 / 지원서 / 메일 / 이미지
- [x] 4. 전체 통합 테스트 / CI / Docker / 배포 문서 / Spring 코드 보관

## 검증 명령

`pnpm check`로 Biome, 타입 검사, 빌드, 실제 MySQL 통합 테스트를 실행한다.

## 이관 시 확인 사항

- 적용된 V1~V9 SQL 내용과 Flyway checksum 보존.
- 기존 BIGINT ID는 내부 문자열로 보존하고, 응답은 안전한 정수 범위에서 기존 숫자 형식을 유지.
- MySQL BIT(1) 불리언 및 한국 시간의 DATE/DATETIME 매핑.
- JWT HS256과 기존 Base64 인코딩된 비밀키 처리.
- 지원서와 활동·면접 관계 저장의 원자성, 메일 실패 시 저장 결과.
- MySQL에는 ORM 외의 기존 테이블도 존재하므로 스키마 자동 동기화를 금지.

## 변경한 동작 / 검증 결과

### 보존한 계약

- 기존 HTTP method와 경로 45개를 대조해 모두 보존했다.
- `status/code/message/data`, Spring Page의 페이지 메타데이터, 기존 한국어 날짜 형식을 유지한다. 일부 성공 메시지 문구는 정리했다.
- V1~V9 SQL을 `db/migration`으로 이동했으며 원본과 바이트 단위로 동일함을 확인했다. DDL을 추가하지 않았다.
- 기존 스키마의 미사용 coupon, user_coupon, image, refresh_token 테이블과 데이터는 그대로 둔다.
- Spring JWT의 `userProfile`, HS256, Base64 key 해석을 유지하며 `kind` 없는 기존 토큰도 받는다.
- 이미지의 `photo_key` 우선 조회와 기존 전체 URL fallback, home의 bucket 포함 URL, AWS의 CloudFront URL 정책을 유지한다.
- 지원서 제출 가능 기간 검사와 결과메일의 기수 범위는 기존 동작을 유지한다. 제출 API는 기간을 강제하지 않으며, 결과메일 API는 전달된 합격 조건에 맞는 기수 전체를 대상으로 한다.

### 명시적으로 개선한 동작

- 인증이 필요한 모든 경로에서 JWT 서명·만료·토큰 용도를 검증하고, 관리자 권한과 BLACK_LIST 상태는 DB에서 확인한다. 최신 DB 권한이 토큰의 과거 claim보다 우선한다.
- 이미 다른 카카오 계정에 연결된 회원의 재연결은 409로 거부한다. 기존 `kakao_id=0` 회원의 이름·학번 연결 방식과 회원 등급은 보존한다.
- 요청 본문·쿼리의 형식, 길이, 숫자 범위를 검증한다. 정의하지 않은 필드는 400으로 거부한다. 페이지 크기는 100으로 제한하며 서버의 기존 고정 정렬을 사용한다.
- 삭제된 게시글은 상세 조회와 댓글 작성에서도 404로 처리한다. 소유권 위반은 403, 없는 데이터는 404, FK로 삭제할 수 없는 데이터는 409를 반환한다.
- 지원서·동아리 활동·면접 선택을 단일 트랜잭션으로 저장한다. 선택한 면접이 없거나 다른 기수라면 앞선 INSERT도 롤백된다.
- 지원서 접수메일은 저장 커밋 후 발송한다. 메일 실패가 이미 접수된 지원서를 롤백하지 않는다.
- 발송 성공 후 이력 또는 sent_at을 저장하므로 실패한 결과/모집 알림은 endpoint를 다시 호출해 재시도할 수 있다. DB 행 잠금으로 동시 중복 발송을 직렬화한다.
- SMTP 수락 후 DB 커밋 전 프로세스 장애까지 포함한 exactly-once 전송은 보장하지 않는다. 접수메일의 자동 재시도 워커는 없으며, 실패 후 재발송에는 `ApplicationNotifier.notify(id, null)` 호출이 필요하다(헥사고날 리팩토링 전에는 `EmailService.applicationNotification`).
- 서류·최종 합격의 null/false/true를 구분한다. 조회 쿼리에서 조건 생략 또는 `null`은 기존대로 IS NULL이며, 문자열 `false`를 true로 변환하지 않는다.
- refresh cookie에 SameSite를 명시하고 카카오 refresh token 회전 시 쿠키도 갱신한다.
- SQL 오류의 바인딩 값 로깅을 끄고 응답에 내부 DB 오류를 노출하지 않는다. Nest 전이 의존성 multer는 패치된 2.3.0으로 고정한다.
- 이미지 스토리지 선택을 `APP_PROFILE`에서 분리해 `STORAGE_PROVIDER=minio|s3`로 지정한다. 생략하면 home은 minio, aws는 s3로 기존 동작과 같으며, AWS 배포에서도 MinIO를 사용할 수 있다.

### 검증

- `pnpm check`: Biome lint/format, strict TypeScript 검사, Nest 빌드, 통합 테스트 23개.
- `pnpm docker:build`: Node.js 24 운영 이미지와 Flyway 이미지 빌드.
- `pnpm test:container`: 실제 운영 이미지의 HTTP 요청으로 MySQL 저장 확인, 비root 실행 확인.
- `pnpm audit --prod`: 알려진 취약점 없음.
- 원본 45개 API 경로 대조 및 9개 Flyway SQL의 원본 바이트 비교.
- 실제 MySQL의 저장/수정/삭제, FK 제약, 트랜잭션 롤백, BIT(1), BIGINT, 날짜를 검증.
- 로컬 HTTP 카카오 서버, 실제 SMTP 서버, 실제 MinIO PUT/GET으로 외부 연동을 검증.
- 공식 MinIO 이미지(quay.io/minio/minio, minio/minio)의 공개 배포가 중단되어 CI 이미지 pull이 실패했다. 로컬 개발과 통합 테스트는 커뮤니티 빌드 `pgsty/minio`로 바꿨다.
- GitHub Actions는 PR 검증과 dev 이미지 게시를 분리하며 게시 전에도 같은 검증을 실행한다.

### CI에서 발견한 날짜 회귀와 수정

- UTC CI에서 모집일과 생년월일이 하루 앞당겨졌다. MySQL `DATE`가 +09:00 자정의 JavaScript `Date`로 변환된 뒤 TypeORM이 호스트 시간대로 날짜를 다시 추출한 것이 원인이었다.
- [TypeORM의 `dateStrings` 설정](https://typeorm.io/docs/drivers/mysql/)을 `['DATE']`로 지정해 날짜만 문자열로 유지한다. 면접 시각 등 `DATETIME`은 기존처럼 +09:00 기준 `Date`로 처리한다. 프로세스 시간대와 무관하게 달력 날짜를 보존한다.
- 모집의 모든 날짜, 생년월일, 동아리 활동 시작·종료일의 실제 저장·조회 결과를 검증하고, CI에서 동일한 통합 테스트를 UTC와 Asia/Seoul 각각 실행한다.

### 운영 전환

- 이 작업은 운영 배포나 운영 DB 마이그레이션을 실행하지 않았다.
- 기존 Spring 자료는 `legacy/spring`에 보관하며 Node 빌드와 Docker context에서 제외한다.
- 기존 비공개 application.properties와 .env.aws는 변경하지 않는다. 새 환경 파일은 스크립트를 직접 실행할 때 생성된다.
- 기존 Spring 컨테이너와 새 Nest 컨테이너의 이름이 같아 최초 전환 시 기존 앱 컨테이너만 중지·제거해야 한다. 기존 DB/Redis/스토리지 데이터는 삭제하지 않는다.
- 실행 및 배포 절차는 루트 README를 따른다. API와 Flyway 이미지는 같은 commit 태그로 선택한다.
- 맥미니 운영은 `compose.home.yml`로 API, 전용 MySQL 8.4, Flyway를 실행하고 이미지는 MinIO에 저장한다. 기존 RDS 데이터는 `mysqldump`로 옮기며 `flyway_schema_history`를 함께 옮겨 V1~V9 이력을 보존한다.
