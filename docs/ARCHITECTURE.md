# 헥사고날 아키텍처

`~/everygolf/server`(Spring Boot·Kotlin)의 헥사고날 3계층 구조를 NestJS·TypeORM에 맞게 옮긴 구조다.
의존은 바깥에서 안으로만 흐르고, `test/architecture.test.ts`가 아래 규칙을 자동으로 검사한다.
구조를 바꾸는 PR은 코드·아키텍처 테스트·이 문서를 함께 고친다.

## 구조

```text
adapter ──▶ application ──▶ domain        (support 는 모든 계층이 쓰는 계층 밖 공용)

src/
├── main.ts · bootstrap.ts · app.module.ts   진입점과 조립 루트. 어느 계층도 이 파일을 import 하지 않는다
├── domain/<애그리거트>                      TypeORM 엔티티·값·도메인 규칙. Nest 금지
│   ├── common                               BaseEntity, BIT(1) 변환
│   ├── user · board · recruitment · apply
├── application/<슬라이스>                   한 기능이 한 슬라이스. 공개 표면은 provided·required 둘뿐
│   ├── provided/                            인바운드 포트(추상 클래스) + 요청 DTO·응답 타입
│   ├── required/                            아웃바운드 포트: TypeORM 저장소, 카카오·JWT·S3 계약
│   ├── (슬라이스 루트)                      포트를 구현하는 서비스·QueryService·메일 문구
│   └── shared/                              여러 슬라이스가 쓰는 계약: 페이지, ID, 날짜, 이메일 DTO, MailSender
├── adapter/
│   ├── webapi/<슬라이스>                    컨트롤러(@WebApiAdapter) → provided 포트
│   ├── webapi/security                      AccessGuard, @Access, @PrincipalId
│   ├── webapi/*.ts                          예외 필터(HTTP 상태 결정), 요청 로그, IdPipe, 응답 봉투
│   ├── security/jwt                         TokenIssuer 구현
│   ├── integration/kakao · mail · storage   KakaoOAuthClient · MailSender · ImageStorage 구현
│   └── config/                              환경 변수 바인딩(Settings), DB 옵션, 로거, 슬라이스별 Nest 모듈
└── support/                                 오류 체계(errors), 로그 원인 요약, 역할 데코레이터(stereotype)
```

| 슬라이스 | 책임 | 주요 포트 |
| --- | --- | --- |
| `user` | 카카오 로그인, 토큰, 가입·연결, 권한, 회원 등급 | provided: `LoginManager` `AccessVerifier` `UserRegister` `UserFinder` / required: `UserRepository` `KakaoOAuthClient` `TokenIssuer` |
| `board` | 게시글, 댓글, 게시글 템플릿 | provided: `BoardRegister` `BoardFinder` `BoardTemplateRegister` `BoardTemplateFinder` |
| `recruitment` | 모집 기간, 면접 시간, 모집 시작 알림 메일 | provided: `RecruitmentRegister` `RecruitmentFinder` `InterviewTimeRegister` `InterviewTimeFinder` `RecruitmentAlertManager` |
| `apply` | 지원서 제출·조회·합격 여부, 접수·결과 메일, 사진 업로드 URL | provided: `ApplicationRegister` `ApplicationFinder` `ApplicationNotifier` `ApplicationPhotoUploader` / required: `ImageStorage` |
| `health` | DB 상태 확인 | provided: `HealthChecker` |

슬라이스 사이 의존은 `apply → recruitment.provided.InterviewTimeFinder` 하나다(지원서의 면접 선택 검증·표시).

## 계층 규칙

- **domain**: `typeorm`과 `support/errors`만 import 한다. Nest·HTTP를 모른다. JPA 매핑을 허용한 참고 구조처럼
  TypeORM 데코레이터는 매핑 메타데이터로 허용하므로 완전한 영속성 독립 모델은 아니다.
  엔티티는 정적 팩토리(`User.register`, `Board.write`, `Recruitment.open`, `Application.submit`)로 만들고,
  상태 변경과 규칙은 의미 있는 메서드(`linkKakao`, `edit`, `delete`, `reschedule`, `markSent`)에 둔다.
- **application**: `typeorm`, `class-validator`, `class-transformer`, `node:crypto`와 `@nestjs/common`의 `Logger`만
  쓴다. HTTP 예외·가드·컨트롤러·`fetch`·`process.env`는 어댑터의 일이다. 서비스는 `@ApplicationService()`를 붙인다.
- **slice 경계**: 다른 슬라이스는 그 슬라이스의 `provided`로만 참조한다. `application/shared`는 모두가 쓸 수 있고
  어느 슬라이스에도 의존하지 않는다. 슬라이스·애그리거트 사이 순환은 금지한다.
- **adapter**: 컨트롤러는 `provided` 포트만 생성자로 주입받는다(서비스·저장소·아웃바운드 어댑터 직접 주입 금지).
  아웃바운드 어댑터(`integration`, `security`)는 `required` 포트를 구현하고 `provided`나 서비스에 의존하지 않는다.
  쿠키 설정처럼 공개 설정이 필요하면 `adapter/config`의 `Settings`를 주입할 수 있다.
- **설정은 adapter에서 끝낸다**: 환경 변수 해석과 검증은 `adapter/config/settings.ts`가 맡는다. application은 설정을
  읽지 않는다(지금은 순수 설정 값이 필요한 유스케이스가 없다).

## 포트 규약

- 포트는 **추상 클래스**다. TypeScript 인터페이스는 런타임에 사라져 Nest DI 토큰이 될 수 없으므로, 추상 멤버만 둔
  추상 클래스를 타입 겸 토큰으로 쓴다. 구현은 `implements`로 선언하고 `adapter/config/*.module.ts`에서
  `{ provide: 포트, useExisting: 서비스 }`(provided) 또는 `{ provide: 포트, useClass: 어댑터 }`(required)로 묶는다.
  `@Inject(TOKEN)`이 필요 없다.
- **provided** 이름은 능력 기준이다: 쓰기 `<대상>Register`, 읽기 `<대상>Finder`, 흐름 관리 `<대상>Manager`,
  그 밖의 동작은 역할 이름(`AccessVerifier`, `ApplicationNotifier`, `ApplicationPhotoUploader`).
  요청 DTO(class-validator 포함)와 응답 타입도 provided에 둔다. provided는 domain·다른 provided·shared만 참조한다.
  입력 검증은 전역 `ValidationPipe`(HTTP)에서 한다. 참고 구조처럼 포트 호출마다 검증하지는 않으므로
  HTTP 밖에서 포트를 부를 때는 호출자가 올바른 DTO를 넘긴다.
- **required**: Spring Data 저장소가 그대로 required 포트인 참고 구조처럼 **TypeORM `Repository<T>`를 확장한 추상
  클래스가 저장소 포트**다(`UserRepository extends Repository<User>`). 별도 저장소 어댑터 클래스를 만들지 않고
  `adapter/config/repository.ts`의 `repositoryProvider`가 `DataSource.getRepository(엔티티)`에 연결한다.
  저장소는 소유 슬라이스만 쓴다. 카카오·JWT·S3처럼 바깥 시스템은 계약만 두고 adapter가 구현한다.
  여러 슬라이스가 쓰는 메일 발송 계약 `MailSender`만 `application/shared`에 있다.
- 트랜잭션은 TypeORM에 주변(ambient) 트랜잭션 전파가 없어서 한 서비스 메서드 안에서 `DataSource.transaction`
  또는 `repository.manager.transaction`으로 연다. 트랜잭션이 슬라이스 경계를 넘지 않게 유스케이스를 나눈다.

## 역할과 데코레이터

| 역할 | 데코레이터 (`support/stereotype`) | 위치 |
| --- | --- | --- |
| provided 구현·조회 서비스 | `@ApplicationService()` | `application/<슬라이스>/` 루트 |
| 컨트롤러 | `@WebApiAdapter()` | `adapter/webapi/` |
| 아웃바운드 구현 | `@Adapter()` | `adapter/integration/`, `adapter/security/` |
| Nest 모듈 | `@Module()` | `adapter/config/`, `app.module.ts` |
| 저장소 포트 | `extends Repository<T>` | `application/<슬라이스>/required/` |

데코레이터는 `@Injectable()`/`@Controller()`와 같게 동작하고, 아키텍처 테스트가 위치를 검사할 수 있게 역할을 이름으로 드러낸다.
`*QueryService`는 provided Finder를 구현하면서 슬라이스 안에서 쓰는 NotFound 조회(`getById`)를 함께 맡는다.

## 오류와 응답

- 안쪽 계층은 `support/errors`의 `ExpectedError` 하위 오류를 던진다:
  `InvalidInputError`(400), `UnauthenticatedError`(401), `ForbiddenError`(403), `NotFoundError`(404),
  `ConflictError`(409), `ExternalServiceError`(502). HTTP 상태는 `adapter/webapi/api-exception.filter.ts`만 안다.
- 메시지는 그대로 응답에 나가고, `cause`는 요청 로그(`cause` 필드)에만 남는다. 예: JWT 검증 실패 원인, 카카오 오류 본문, SMTP 오류.
- 그 밖의 오류는 기존처럼 500과 일반 문구로 응답한다. FK·중복 키 오류는 409다.

## 자동 검사 (`pnpm test:unit`)

`test/architecture.test.ts`는 `src`의 import 그래프를 TypeScript 컴파일러 API로 읽어 다음을 검사한다.
각 규칙은 위반 픽스처를 실제로 거절하는지도 함께 검증한다(참고 구조의 `ArchitectureRulesTest`).

| 규칙 | 내용 |
| --- | --- |
| `layerDependencies` | adapter → application → domain 방향, domain의 support 의존은 `errors`만, 루트 파일 import 금지 |
| `coreDependencies` | domain·application의 외부 패키지 허용 목록, application의 `@nestjs/common`은 `Logger`만, `fetch`·`process.env` 금지 |
| `sliceBoundaries` | 다른 슬라이스는 provided로만, shared는 슬라이스에 의존하지 않음 |
| `portContracts` | provided·required는 구현(서비스)·Nest를 참조하지 않음 |
| `noCycles` | 애플리케이션 슬라이스·도메인 애그리거트 순환 금지 |
| `rolePlacement` | 데코레이터·Nest 모듈·저장소 포트의 위치 |
| `adapterDependencies` | 컨트롤러는 provided만, 아웃바운드 어댑터는 required만 참조 |
| `portImplementations` | provided 구현은 자기 슬라이스 루트, required 구현은 adapter |

외부 패키지 허용 목록은 명시적이다. 새 라이브러리를 코어에서 쓰려면 목록을 바꾸는 이유를 PR에 남긴다.
검사는 import와 소스 텍스트 기준이라 트랜잭션 범위, 잠금 순서, 다른 애그리거트 상태 변경의 적절성은 리뷰로 확인한다.

`test/domain.test.ts`는 DB 없이 도메인 규칙(가입·연결 충돌, 작성자 확인, 모집 기간 검증, 결과 유형)을 검사한다.
HTTP·DB 동작은 기존처럼 실제 MySQL·Flyway 통합 테스트가 검증한다.

## 새 기능을 붙일 때

1. `domain/<애그리거트>`에 엔티티와 팩토리·규칙 메서드.
2. `application/<슬라이스>/required`에 저장소·외부 포트, `provided`에 포트와 요청·응답 타입.
3. 슬라이스 루트에 포트를 구현하는 `@ApplicationService()` 서비스. 다른 슬라이스가 필요하면 그쪽 provided Finder를 쓴다.
4. `adapter/webapi/<슬라이스>`에 컨트롤러(provided만 주입), 외부 시스템은 `adapter/integration/<시스템>`.
5. `adapter/config/<슬라이스>.module.ts`에 포트와 구현을 묶고 `app.module.ts`에 모듈을 추가한다.
6. 통합 테스트와 필요하면 도메인 단위 테스트를 추가하고 `pnpm test:unit`의 아키텍처 검사를 통과시킨다.

## 이 리팩토링에서 달라진 점

HTTP 경로 45개, 요청·응답 형식, 상태 코드, 오류 메시지, DB 스키마, 설정 키는 바꾸지 않았다.

- 지원서 제출은 동아리 활동 기간과 면접 선택을 **첫 INSERT 전에** 검증한다. 전에는 지원서를 INSERT한 뒤
  같은 트랜잭션에서 검증하고 롤백했다. 결과(400, 저장된 행 없음)와 오류 순서(활동 → 면접)는 같다.
- 인증은 토큰 검증(`authenticate`)과 현재 회원 상태 확인(`authorize`)으로 나뉜다. 순서와 결과는 같고,
  권한 거절(403)에도 요청 로그에 호출자가 남는다.
- 코드에서 직접 부르던 이름이 바뀌었다: 접수 메일 재발송은 `EmailService.applicationNotification(id, null)` 대신
  `ApplicationNotifier.notify(id, null)`, 토큰 발급은 `AuthService` 대신 `TokenIssuer` 포트다.
