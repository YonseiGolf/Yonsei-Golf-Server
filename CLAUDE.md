# Yonsei Golf Server

연세대학교 골프동아리 API 서버. NestJS + TypeScript + TypeORM + MySQL, pnpm, Biome.
이 문서는 이 저장소에서 코드를 쓰는 사람(과 AI)이 지킬 규칙이다. 구조를 바꾸는 PR은 이 문서와
[아키텍처 문서](docs/ARCHITECTURE.md)도 함께 고친다.

## 자주 쓰는 명령

```bash
pnpm check              # lint + typecheck + build + 단위 테스트 + 통합 테스트 (CI와 같다)
pnpm test:unit          # 아키텍처 규칙 + 도메인 규칙. Docker 불필요
pnpm test:integration   # 실제 MySQL·Flyway·MinIO 컨테이너. Docker 필요
pnpm lint:fix           # Biome 포맷·import 정렬
```

## 아키텍처 — 헥사고날 3계층

```text
adapter ──▶ application ──▶ domain      (support 는 계층 밖 공용)
```

- `domain/<애그리거트>`: 엔티티·도메인 규칙. `typeorm`과 `support/errors`만 쓴다. Nest 금지.
- `application/<슬라이스>`: `provided`(인바운드 포트 + 요청·응답), `required`(저장소·외부 포트), 슬라이스 루트(서비스).
  다른 슬라이스는 `provided`로만 참조한다. 공통 계약은 `application/shared`.
- `adapter/webapi`: 컨트롤러는 provided 포트만 주입. `adapter/integration`·`adapter/security`: required 포트 구현.
  `adapter/config`: 환경 변수 바인딩과 슬라이스별 Nest 모듈(포트 ↔ 구현 연결).
- 포트는 추상 클래스(타입 겸 DI 토큰). 저장소 포트는 `Repository<T>`를 확장한 추상 클래스다.
- 안쪽 계층은 `support/errors`의 오류를 던지고 HTTP 상태는 `ApiExceptionFilter`만 정한다.
- `test/architecture.test.ts`가 이 규칙을 강제한다. 깨지면 의존 방향과 책임 배치를 먼저 확인한다.

규칙의 전체 목록, NestJS에 맞춘 결정, 새 기능 체크리스트는 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)에 있다.

## 지켜야 할 것

- **API 계약**: 기존 HTTP 경로·응답 형식(`status/code/message/data`, Spring Page JSON)·오류 메시지를 유지한다.
  BIGINT ID는 내부에서 문자열로 다루고 응답은 `apiId`로 변환한다.
- **DB**: 스키마는 Flyway(`db/migration`)만 바꾼다. 적용된 SQL은 고치지 않고 `V10__...`부터 추가한다.
  TypeORM `synchronize`는 항상 꺼 둔다.
- **테스트**: 통합 테스트가 기본이다. 실제 MySQL·Flyway에서 HTTP 요청과 DB 행을 검증하고, 저장소·SQL·트랜잭션을
  모킹하지 않는다. 카카오·SMTP는 로컬 테스트 서버, 스토리지는 MinIO 컨테이너를 쓴다. 도메인 규칙은 단위 테스트로도 잡는다.
- **시간**: 달력 날짜는 `YYYY-MM-DD` 문자열, 시각은 한국 시간 기준 `Date`. `TZ=UTC`와 `TZ=Asia/Seoul` 모두에서 통과해야 한다.
- **로그**: 요청 본문·헤더·쿠키·토큰을 남기지 않는다. 실패 원인은 오류의 `cause`로 넘긴다.

## Git / PR

- 브랜치: `feat/<주제>`, `fix/<주제>`, `refactor/<주제>`. PR은 `dev`로 보낸다. `dev` 머지 후 CI가 이미지를 게시한다.
- 커밋 제목: `feat:`/`fix:`/`refactor:`/`test:`/`docs:`/`chore:` + 영어 명령문 한 줄.
