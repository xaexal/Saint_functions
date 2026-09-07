# Saint 프로젝트 — OCI Functions 이전 분석 문서

- 분석 대상 경로: `C:/Users/xaexal/git/Saint_functions`
- 분석 기준 시점: 2026-09-05 (이후 코드가 변경되면 이 문서는 다시 검증 필요)
- 이 문서는 소스코드를 직접 읽어 확인한 사실만 기록했다. 확인하지 못하고 판단만 한 내용은 반드시 "(추정)"으로 표시했다.
- **본 분석 시점까지 소스코드는 전혀 수정하지 않았다.**

---

## 1. 현재 프로젝트의 전체 구조

```
Saint_functions/
├── build.gradle
├── settings.gradle
├── gradlew, gradlew.bat
├── keystore.p12                # 로컬 HTTPS용 PKCS12 키스토어
├── secrets.properties           # git 미추적(.gitignore), 민감 설정값
├── restart.bat, run-tunnel.bat  # 로컬 실행 스크립트
├── src/
│   ├── main/
│   │   ├── java/com/xaexal/app/
│   │   │   ├── SaintApplication.java      # @SpringBootApplication 진입점
│   │   │   ├── SecurityConfig.java        # Spring Security 설정
│   │   │   ├── ServletInitializer.java    # SpringBootServletInitializer (war 배포 흔적)
│   │   │   ├── WebConfig.java             # CORS 설정
│   │   │   ├── Controller/   (41개 클래스)
│   │   │   ├── Service/      (5개 클래스)
│   │   │   ├── Entity/       (47개 클래스, JPA @Entity)
│   │   │   ├── Repository/   (40개 인터페이스, Spring Data JPA)
│   │   │   ├── DTO/          (44개 클래스)
│   │   │   └── Common/       (16개 클래스, AOP/보안/DB/S3/예외처리 등 공통 모듈)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── keystore.p12, keystore.p12.selfsigned.bak
│   │       ├── logbak-spring.xml
│   │       └── USB/           # saint.sql, templates.xml 등 (용도 미확인, 아래 16번 참고)
│   └── test/
└── OCI_FUNCTIONS_ANALYSIS.md  (본 문서)
```

- Java 소스 파일 총 198개 (`find src -name "*.java" | wc -l` 기준).
- `DAO/`(MyBatis) 패키지는 이 저장소에는 **존재하지 않는다.** (7번 항목 참고)
- 이 프로젝트는 `com.xaexal.app` 단일 패키지 안에 계층별 하위 패키지를 두는 구조이며, 도메인별 하위 패키지 분리는 되어 있지 않다.

---

## 2. Spring Boot 버전

`build.gradle` 기준:

```
id 'org.springframework.boot' version '3.4.2'
id 'io.spring.dependency-management' version '1.1.7'
```

- **Spring Boot 3.4.2**

---

## 3. Java 버전

`build.gradle` 기준:

```
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

- **Java 17** (Gradle toolchain으로 고정)

---

## 4. 빌드 도구 및 버전

- **Gradle Wrapper 8.12.1** (`gradle/wrapper/gradle-wrapper.properties`의 `distributionUrl=...gradle-8.12.1-bin.zip`)
- `build.gradle`에 `id 'java'`, `org.springframework.boot`, `io.spring.dependency-management` 플러그인만 적용. `war` 플러그인은 없음 → **jar 단일 빌드** 구조.
- 단, `ServletInitializer.java`(`SpringBootServletInitializer` 상속)가 소스에 남아 있어 과거 war 배포 방식의 흔적이 코드 레벨에는 아직 존재한다.

---

## 5. Controller 목록과 각 Controller의 역할

경로: `src/main/java/com/xaexal/app/Controller/`

모두 `@RestController`이며, `@RequestMapping` 경로를 기준으로 역할을 정리했다. 역할 설명 중 `@RequestMapping` 경로 자체는 소스 확인 사실이고, 세부 업무 의미(예: "교인 관리")는 컨트롤러/엔드포인트 이름과 CLAUDE.md 프로젝트 설명을 근거로 한 요약이다(대부분 자명하나, 완전한 업무 의미는 "(추정)"으로 표기).

| 클래스 | `@RequestMapping` | 역할 (추정 포함) |
|---|---|---|
| `Applicant_` | `/applicant` | 신청자 관리 (추정: 교육/신청 프로세스 관련) |
| `Attached_` | `/attached` | 첨부파일 관리 |
| `Baptism_` | `/baptism` | 세례 정보 관리 |
| `Board_` | `/board` | 게시판(공지사항/제안하기) CRUD |
| `BoardType_` | `/boardtype` | 게시판 유형 관리 |
| `BudgetAccount_` | `/budget` | 예산 계정(과목) 트리 관리 |
| `Bulletin_` | `/bulletin` | 주보 관리 |
| `Church_` | `/church` | 교회 정보 관리 |
| `ChurchMove_` | `/church_move` | 교인 소속 교회 이동 처리 |
| `CommunityReport_` | `/community_report` | 공동체(구역 등) 보고서 관리 (추정) |
| `EquipBook_` | `/equip_book` | 장비 예약 관리 |
| `Equipment_` | `/equipment` | 장비(시설 부속) 관리 |
| `Expense_` | `/expense` | 지출 관리 |
| `Family_` | `/family` | 가족 관계 관리 |
| `Home` | `/`, `/user/info` | 헬스체크/로그인 사용자 정보 조회 |
| `ImageController` | `/api/image` | S3 이미지 프록시 조회 (`/api/image/{key}`) |
| `Income_` | `/income` | 수입(헌금 포함) 관리 |
| `Lov_` | (`@RequestMapping` 미탐지, 서비스는 `_Lov_`로 확인) | 공통 코드(List of Values) 트리 관리 |
| `Member_` | `/member` | 회원(교인) 정보 관리, 이미지 업로드(S3) 포함 |
| `Message_` | `/message` | 쪽지/메시지 발신·수신함 관리 |
| `Navi_` | `/navi` | 좌측 메뉴(네비게이션) 관리 |
| `Offering_` | `/offering` | 헌금-수입 연결(income 연결 테이블) 관리 |
| `Payment_` | `/payment` | 결제(Toss 등) 처리 |
| `Polity_` | `/polity` | 교회 정치(직분/조직) 관련 관리 (추정) |
| `Position_` | `/position` | 직분/직위 관리 |
| `Preferences_` | `/preferences` | 사용자/전역 환경설정 관리 |
| `Priority_` | `/priority` | 우선순위 관련 관리 (추정) |
| `Private_` | 없음 | **빈 클래스** — `public class Private_ { }` 외 내용 없음. 사실상 죽은 코드 |
| `Reply_` | `/reply` | 댓글 관리 |
| `RolePermissions_` | `/rolepermissions` | 역할별 권한 관리 |
| `Roles_` | `/roles` | 역할(권한 그룹) 트리 관리 |
| `Saint_` | `/saint` | 교적(교인-교회 소속) 핵심 도메인 관리 |
| `Schedule_` | `/schedule` | 일정 관리 |
| `School_` | `/school` | 교육 과정(수강신청 포함) 관리 |
| `Space_` | `/space` | 시설(공간) 관리 |
| `SpaceBook_` | `/space_book` | 공간 예약(신청) 관리 |
| `Staff_` | `/staff` | 직원/사역자 관리 |
| `Student_` | `/student` | 수강생 관리 |
| `TestSshTennel` | `/test-ssh` (GET) | SSH 터널 연결 테스트용 디버그 엔드포인트 |
| `UserRoles_` | `/userroles` | 사용자-역할 매핑 관리 |
| `WorshipStat_` | `/worship_stat` | 예배 통계 관리 |

비고:
- `Lov_.java`는 grep으로 클래스 내 `@RequestMapping` 라인이 직접 잡히지 않았다(파일 구조상 상속 또는 다른 위치에 선언되었을 가능성 — 미확인, "추정" 표시). 별도 확인 필요.
- `@LoginCheck` 어노테이션이 37개 컨트롤러 파일, 총 170곳에 사용되어 있음(문자열 매칭 기준). 즉 대부분의 엔드포인트가 세션 로그인 여부를 전제로 동작한다.

---

## 6. Service 목록과 각 Service의 역할

경로: `src/main/java/com/xaexal/app/Service/` — **5개 클래스만 존재.**

| 클래스 | 확인된 역할 |
|---|---|
| `_Church_` | `@Scheduled(cron = "0 0 0 * * *")`로 등록된 **매일 자정 배치 작업 2건**을 포함: ① `cleanupInactiveChurches()` — 등록 7일 경과·교인 0명 교회 삭제 및 `Midnight` 테이블에 로그 저장, ② `updateExpense()` — 교회별 등록교인수×capita 기준 비용 계산 후 `church.expense` 갱신 |
| `_Lov_` | 공통 코드(LOV) 등록/수정 로직. `seqno` 자동 채번(`findMaxSeqnoByParId` 기반) 포함 |
| `_Message_` | 쪽지 발신 처리(`send(senderId, title, content, receiverIds)`), 수신자별 `MessageRecipient` 생성 |
| `InitChurch` | 교회 신규 생성 시 기본 LOV(세례구분/헌금/교육과정) 초기 데이터 생성 |
| `S3Service` | AWS S3 클라이언트 초기화(`@PostConstruct`), 객체 목록 조회/삭제, presigned URL 생성(`generatePresignedUrl`), 공개 URL 생성 |

비고:
- 대부분의 비즈니스 로직은 위 5개 Service가 아니라 **Controller 클래스 내부에 Repository를 직접 주입**하여 구현되어 있다(코드 확인 사실). 즉 Service 계층이 실제로 담당하는 범위는 좁고, Controller가 사실상 Service 역할까지 겸하고 있다.

---

## 7. MyBatis/JPA 사용 현황

- **JPA만 사용한다. MyBatis는 사용하지 않는다.**
  - `build.gradle`에 mybatis 관련 의존성이 없음(확인 사실).
  - 소스 전체에서 `mybatis`, `SqlSession`, `@Mapper` 문자열 검색 결과 **0건**.
  - `application.properties`에 `spring.jpa.*`, `spring.datasource.*` 설정만 존재하고 mybatis-config 관련 설정 없음.
- `spring.jpa.hibernate.ddl-auto=update` — 애플리케이션 기동 시 Hibernate가 스키마를 자동 변경한다(확인 사실). 운영 환경/FaaS 전환 시 위험 요소가 될 수 있다.
- **CLAUDE.md(프로젝트 최상위 안내 문서)에는 `DAO/` 패키지(MyBatis 매퍼)가 있다고 기술되어 있으나, 이 저장소(`Saint_functions`)에는 해당 패키지가 존재하지 않는다.** 즉 CLAUDE.md는 원본 `Saint` 저장소 기준 설명이며, `Saint_functions`는 구조가 다른 별도 브랜치/분기 저장소로 보인다(추정 — 두 저장소 간 정확한 관계는 미확인, 16번 항목 참고).

---

## 8. Entity/DTO/Mapper 구조

MyBatis Mapper는 없으므로(7번 참고) Entity/DTO/Repository(Spring Data JPA) 구조로 정리한다.

- **Entity** (`src/main/java/com/xaexal/app/Entity/`, 47개): `Applicant`, `Attached`, `AttendenaceRegister`, `Baptism`, `BaseEntity`, `Billing`, `Board`, `BoardAux`, `BoardType`, `BudgetAccount`, `Bulletin`, `Candidate`, `Church`, `ChurchMove`, `CommunityReport`, `EquipBook`, `Equipment`, `Expense`, `Family`, `GlobalPreference`, `Income`, `Lov`, `Manager`, `Member`, `Message`, `MessageRecipient`, `Midnight`, `Navi`, `NaviRole`, `Offering`, `One2one`, `PaymentMethod`, `Position`, `Priority`, `Reply`, `RolePermissions`, `Roles`, `Saint`, `Schedule`, `School`, `Space`, `SpaceBook`, `Staff`, `Student`, `UserRoles`, `UserRolesId`, `WorshipStat`
- **Repository** (`src/main/java/com/xaexal/app/Repository/`, 40개, 전부 Spring Data JPA `interface`): `ApplicantRep`, `AttachedRep`, `BaptismRep`, `BillingRep`, `BoardAuxRep`, `BoardRep`, `BoardTypeRep`, `BudgetAccountRep`, `BulletinRep`, `ChurchMoveRep`, `ChurchRep`, `CommunityReportRep`, `EquipBookRep`, `EquipmentRep`, `ExpenseRep`, `FamilyRep`, `GlobalPreferenceRep`, `IncomeRep`, `LovRep`, `MemberRep`, `MessageRecipientRep`, `MessageRep`, `MidnightRep`, `NaviRep`, `OfferingRep`, `PaymentMethodRep`, `PositionRep`, `PriorityRep`, `ReplyRep`, `RolePermissionsRep`, `RolesRep`, `SaintRep`, `ScheduleRep`, `SchoolRep`, `SpaceBookRep`, `SpaceRep`, `StaffRep`, `StudentRep`, `UserRolesRep`, `WorshipStatRep`
- **DTO** (`src/main/java/com/xaexal/app/DTO/`, 44개): `BoardList`, `BoardResult`, `Cell`, `CellHistory`, `Church`, `CommunityReportDetail`, `CommunityReportList`, `DailyStatResult`, `FamilyMember`, `iBudgetOverview`, `Ids`, `iExpense`, `iIncome`, `iLeafBudget`, `ImageResponse`, `iNaviSub`, `iOffer`, `iOrgChart`, `iPayday4Church`, `iPaymentMethod`, `iPosition`, `iRolePermissions`, `iUserRoles`, `MemberBaptismSaint`, `MessageDetail`, `MessageInboxItem`, `MessageOutboxItem`, `MessageReceiverInfo`, `MonthStatResult`, `Newclass`, `OfferResult`, `Person`, `PolityMember`, `PositioniHistory`, `RecipientStatus`, `Reply`, `Reply01`, `SaintAndChurch`, `SaintMember`, `SchoolResult`, `SchoolStudentResult`, `StaffMember`, `StatisticResult`, `StudentMember`
- **Mapper(MyBatis)**: 해당 없음 (7번 항목 참고)

---

## 9. MySQL 연결 방식

- 드라이버: `com.mysql.cj.jdbc.Driver` (`com.mysql:mysql-connector-j`, `build.gradle`)
- 연결 모드 분기(`Common/DataSourceConfig.java`): `app.datasource.mode` 값이 `tunnel`이면 `localhost:{tunnel-local-port}`(기본 3307)로, `direct`(기본값)면 `app.datasource.direct-host:direct-port`(기본 127.0.0.1:3306)로 접속.
- `dataSource()` 빈은 `@DependsOn("sshTunnelManager")`로 선언되어, **SSH 터널이 먼저 초기화된 뒤에만** DataSource가 생성되도록 강제되어 있음(확인 사실).
- `Common/SshTunnelManager.java`:
  - `@PostConstruct`에서 `mode=tunnel`일 때만 JSch 기반 SSH 터널을 오픈(`193.123.234.59`, 사용자 `ubuntu`, 원격 DB `127.0.0.1:3306` → 로컬 `3307`).
  - `@Scheduled(fixedDelay = 30_000)`로 30초마다 터널 연결 상태를 확인하고 끊겨 있으면 재연결을 시도하는 **상주 워치독**.
  - `@PreDestroy`에서 터널 종료.
- JDBC URL(`DataSourceConfig`): `jdbc:mysql://{host}:{port}/{db-name}?serverTimezone=UTC&sslMode=DISABLED&connectTimeout=5000&socketTimeout=15000`
- HikariCP 설정(`application.properties`): `maximum-pool-size=10`, `minimum-idle=5`, `connection-timeout=10000`, `validation-timeout=5000`, `keepalive-time=30000`, `max-lifetime=1700000`

---

## 10. application.properties/application.yml의 주요 설정

파일: `src/main/resources/application.properties` (yml 파일은 존재하지 않음, 확인 사실)

- `spring.config.import=file:secrets.properties` — 민감 값을 별도 파일로 분리해서 병합.
- DB: `spring.datasource.driver-class-name`, `username=xaexal`, `password`(평문 하드코딩 확인됨), `app.datasource.*` (9번 항목 참고)
- 메일: `spring.mail.host=smtp.naver.com`, `port=587`, `username=cavenagh@naver.com`, `password=${MAIL_PASSWORD}`(환경변수), STARTTLS 사용
- 세션: `server.servlet.session.timeout=120m`
- 파일 업로드: `spring.servlet.multipart.max-file-size=50MB`, `max-request-size=50MB`
- 결제: `toss.client-key=${TOSS_CLIENT_KEY}`, `toss.secret-key=${TOSS_SECRET_KEY}` (환경변수)
- AWS: `aws.access-key-id=<REDACTED>`(평문 하드코딩 확인됨), `aws.secret-access-key=${AWS_SECRET_KEY}`, `aws.s3.region=ap-northeast-2`, `aws.s3.bucket-name=xaexal`
- JPA/Hibernate: `spring.jpa.hibernate.ddl-auto=update`, `spring.jpa.show-sql=true`, `database-platform=org.hibernate.dialect.MySQL8Dialect`, 각종 `logging.level.*` (SQL 바인딩 TRACE 등 상세 디버그 로깅 다수 활성화됨)
- Jackson: `spring.jackson.deserialization.fail-on-unknown-properties=false`
- OAuth2: Google/Kakao/Naver 3개 provider 등록. Kakao·Naver는 client-id/secret이 **평문으로 하드코딩**되어 있음(확인 사실). Google은 `${GOOGLE_CLIENT_ID}` / `${GOOGLE_CLIENT_SECRET}` 환경변수 사용. redirect-uri는 모두 `https://localhost:8443/login/oauth2/code/{provider}`로 로컬 고정.
- 서버: `server.port=8443`, `server.ssl.enabled=true`, `key-store=classpath:keystore.p12`(PKCS12), `key-store-password` 평문 하드코딩, `key-alias=springboot`

---

## 11. 외부 라이브러리와 용도

`build.gradle` 기준:

| 라이브러리 | 용도 |
|---|---|
| `spring-boot-starter-web` | REST API(내장 톰캣 포함) |
| `spring-boot-starter-tomcat` | 내장 서블릿 컨테이너 |
| `org.projectlombok:lombok` | 보일러플레이트 코드 축소(compileOnly + annotationProcessor) |
| `com.mysql:mysql-connector-j` | MySQL JDBC 드라이버 (runtimeOnly) |
| `com.googlecode.json-simple:json-simple:1.1.1` | 단순 JSON 파싱 |
| `spring-boot-starter-mail` | SMTP 메일 발송 |
| `com.jcraft:jsch:0.1.55` | SSH 터널링(`SshTunnel`, `SshTunnelManager`에서 사용) |
| `jakarta.annotation:jakarta.annotation-api:2.1.1` | `@PostConstruct`/`@PreDestroy` 등 표준 애노테이션 |
| `spring-boot-starter-aop` | `@LoginCheck`/`@LogRequest` AOP 구현 |
| `software.amazon.awssdk:s3:2.25.66` | AWS S3 클라이언트(파일 업로드/이미지 프록시/presigned URL) |
| `com.fasterxml.jackson.core:jackson-databind` | JSON 직렬화/역직렬화 |
| `spring-boot-starter-data-jpa` | JPA(Hibernate) ORM |
| `spring-boot-starter-security` | Spring Security(인증/인가 프레임워크, 실제 인가는 permitAll) |
| `spring-boot-starter-oauth2-client` | Google/Kakao/Naver OAuth2 로그인 |
| `spring-boot-devtools` | 로컬 개발용 라이브 리로드(developmentOnly) |
| `spring-boot-starter-test`, `junit-platform-launcher` | 테스트 |

---

## 12. 세션, 쿠키, 파일 업로드 등 서버 상태에 의존하는 기능

- **HttpSession 기반 인증**: 로그인 성공 시(`Common/OAuth2SuccessHandler.java`) `HttpSession`에 `member_id`, `mobile`, `name`, `level`, `church_id`, `church_name`, `role_id`, `title` 등을 저장. `Common/LoginAspect.java`의 `@Around("@annotation(LoginCheck)")`가 `session.getAttribute("member_id") == null`이면 401을 반환. 37개 컨트롤러, 170곳의 메서드가 이 방식에 의존(확인 사실).
- **JSESSIONID 쿠키**: 코드에서 `Cookie`를 직접 다루는 부분은 없으나(문자열 검색 결과 0건), 세션 기반 인증이므로 브라우저-서버 간 서블릿 컨테이너가 자동 발급하는 JSESSIONID 쿠키에 암묵적으로 의존한다.
- **`RememberMeFilter`**(`Common/RememberMeFilter.java`): `OncePerRequestFilter`를 상속하지만 현재 구현부는 `chain.doFilter(request, response);` 한 줄뿐인 **빈 필터**(확인 사실). Spring Security 필터체인보다 먼저 실행되도록 order=-200으로 등록만 되어 있음.
- **파일 업로드**: `Member_`, `Board_`, `Bulletin_` 3개 컨트롤러에서 `MultipartFile` 파라미터 사용(확인 사실). 로컬 디스크에 쓰는 코드(`new File`, `FileOutputStream`, `Paths.get` 등)는 소스 전체에서 검색되지 않았고, 업로드 파일은 `S3Service`를 통해 AWS S3로 전달되는 구조로 보인다(대부분 확인, 세부 위임 경로는 각 컨트롤러 메서드 본문 미전수 검토).
- **SSH 터널 상주 프로세스**: `SshTunnelManager`가 애플리케이션 기동 시부터 종료 시까지 살아있는 것을 전제로 SSH 세션과 30초 주기 워치독 스레드를 유지한다(9번 항목 참고).
- **매일 자정 스케줄러**: `_Church_`의 `@Scheduled(cron = "0 0 0 * * *")` 2건(6번 항목 참고) — 애플리케이션이 자정에 켜져 있어야 동작.
- **인메모리 상태 없음(확인)**: static 캐시나 전역 인메모리 상태를 별도로 두는 코드는 이번 조사 범위에서는 발견하지 못했다(전수 조사는 아님 — 16번 항목 참고).

---

## 13. OCI Functions로 이전할 때 수정이 필요한 파일과 이유

| 파일/영역 | 문제 | 이유 |
|---|---|---|
| `Common/LoginAspect.java`, `Common/LoginCheck.java`, 37개 컨트롤러의 `@LoginCheck` 170곳, `Common/OAuth2SuccessHandler.java` | `HttpSession` 의존 | OCI Functions는 요청마다 컨테이너/인스턴스가 재사용되지 않을 수 있어 인메모리 세션을 신뢰할 수 없음. JWT 등 무상태(stateless) 토큰 기반이나 외부 세션 스토어(Redis 등)로 전환 필요 |
| `Common/SshTunnelManager.java`, `Common/SshTunnel.java`, `Common/DataSourceConfig.java` | SSH 터널 상시 연결 + 30초 워치독 스레드 전제 | Functions는 상시 백그라운드 스레드를 유지할 것이라는 보장이 없고, 콜드스타트마다 SSH 핸드셰이크 비용 발생. OCI 내부 네트워크(VCN Private Endpoint, Bastion, 또는 DB 자체를 OCI로 이전)로 대체 필요 |
| `Service/_Church_.java`의 `@Scheduled` 2건 | Functions 런타임 자체에는 크론 스케줄러가 없음 | OCI Functions + OCI Events/Resource Scheduler(또는 별도 트리거) 조합으로 분리 필요 |
| `SaintApplication.java`, `ServletInitializer.java`, 내장 톰캣 구동 방식 전체 | Spring Boot 내장 서블릿 컨테이너 기동 모델은 FaaS 핸들러 모델과 다름 | Spring Cloud Function 어댑터 도입 또는 각 Controller를 개별 함수 핸들러로 재작성하는 구조적 작업 필요 |
| `application.properties`의 `server.ssl.*`, `keystore.p12` | 애플리케이션 레벨 TLS 종단 처리 | OCI Functions 앞단은 보통 API Gateway가 TLS를 종단 처리하므로 애플리케이션 레벨 SSL 설정은 제거 대상 |
| `WebConfig.java`의 `allowedOrigins("https://localhost:3000")` | CORS 허용 origin이 로컬 개발 주소로 하드코딩 | 배포 도메인이 달라지므로 API Gateway 또는 환경변수 기반 설정으로 전환 필요 |
| `application.properties`의 `spring.jpa.hibernate.ddl-auto=update` | 매 기동 시 스키마 자동 변경 시도 | 콜드스타트가 잦아지면 스키마 검사/변경 비용과 위험이 커짐. `validate` 또는 `none`으로 전환 권장 |
| `application.properties` 내 평문 비밀정보(DB 비밀번호, AWS access key, Kakao/Naver client-secret, SSL keystore 비밀번호 등) | 소스에 평문 노출 | OCI Vault 등 시크릿 매니저로 이관 필요 |
| `Service/S3Service.java`의 `@PostConstruct` S3Client 초기화 | 매 콜드스타트마다 재실행되며 초기화 지연 유발 가능 | 초기화 비용 검토 및 필요 시 지연 초기화로 조정 |
| `Common/DataSourceConfig.java`의 HikariCP 풀 설정 | `maximum-pool-size=10` 등은 상시 서버 전제 값 | Functions 동시 실행 인스턴스 수를 고려해 풀 크기 축소 필요 |

---

## 14. 수정하지 않고 그대로 사용할 수 있는 파일

- **Entity 47개**(`Entity/*.java`) — 순수 JPA 매핑, 프레임워크 실행 모델과 무관하게 재사용 가능
- **Repository 40개**(`Repository/*.java`) — Spring Data JPA 인터페이스, DB 스키마가 그대로면 재사용 가능
- **DTO 44개**(`DTO/*.java`) — 순수 데이터 객체, 그대로 재사용 가능
- **`Service/S3Service.java`** — 로컬 디스크 의존 없이 AWS SDK v2로만 동작, 그대로 이식 가능
- **`Common/Result.java`** — API 응답 래퍼, 순수 로직
- **`Common/GlobalExceptionHandler.java`** — 예외 매핑 로직 자체는 재사용 가능(단, `@RestControllerAdvice` 방식이 FaaS 어댑터에서도 동일하게 동작하는지는 채택할 어댑터에 따라 재검증 필요 — 추정)
- **`Common/ExpenseCalculator.java`** — 순수 계산 로직(정적 유틸리티로 보임, 세부 미검토)
- **각 Controller 메서드 본문 중 세션 체크(`@LoginCheck`)를 제외한 비즈니스 로직 부분** — 로직 자체는 Repository 호출 위주라 인증 방식만 분리하면 재사용 가능

---

## 15. OCI Functions 이전 시 예상되는 문제점

1. **인증 방식 전면 교체 필요**: 170곳의 `@LoginCheck`가 세션에 의존하므로, 무상태 인증으로 바꾸는 작업의 범위가 전체 API 표면에 걸쳐 있다.
2. **DB 접속 구조 재설계 필요**: SSH 터널 + 워치독 스레드는 FaaS 실행 모델과 근본적으로 맞지 않는다. 네트워크 경로 자체를 다시 설계해야 한다.
3. **엔드포인트-함수 매핑 설계 필요**: 41개 Controller, 다수의 세부 엔드포인트를 OCI Functions의 함수 단위로 어떻게 분할할지(도메인별 묶음 vs 엔드포인트별 개별 함수) 결정이 선행되어야 하며, 이에 따라 API Gateway 라우팅 설계도 함께 필요하다.
4. **배치 스케줄러 분리 필요**: `_Church_`의 자정 배치 2건을 별도 트리거 체계로 이전해야 한다.
5. **콜드스타트 비용**: SSL 컨텍스트 로딩(keystore), S3Client/Presigner 초기화, Hibernate `ddl-auto=update` 스키마 검사 등 `@PostConstruct`성 초기화가 여러 곳에 있어 콜드스타트 지연이 누적될 수 있다.
6. **시크릿 관리 이전**: 현재 평문/속성파일 기반 비밀정보를 OCI Vault 등으로 옮기는 작업이 선행되어야 하며, 이 과정에서 배포 파이프라인도 함께 손봐야 한다.
7. **OAuth2 redirect-uri 재설정**: 현재 `https://localhost:8443/login/oauth2/code/{provider}`로 고정되어 있어, 배포 도메인이 바뀌면 Google/Kakao/Naver 콘솔의 redirect-uri 등록도 함께 바꿔야 한다(애플리케이션 밖의 외부 설정 변경 필요).
8. **Hibernate `ddl-auto=update`의 위험성**: 다중 함수 인스턴스가 동시에 뜨는 환경에서 스키마 자동 변경이 동시에 시도될 경우의 안전성이 검증되지 않았다(추정 — 실제 동시성 문제 발생 여부는 미확인).

---

## 16. 아직 확인하지 못한 사항

- `Lov_.java`의 정확한 `@RequestMapping` 선언 위치와 전체 엔드포인트 목록(1차 grep에서 클래스 내 직접 매칭되지 않음, 재확인 필요).
- `Controller/Private_.java`가 실제로 죽은 코드인지, 향후 사용 예정 스텁인지 — 코드만으로는 판단 불가(현재는 빈 클래스임은 확인).
- `src/main/resources/USB/` 디렉터리(`saint.sql`, `templates.xml`, `_deleletemap.xml`, `_getmap.xml`, `_param.xml`, `_parse.xml`, `_postmap.xml`, `_rok.xml`)의 정확한 용도 — 파일명으로 미루어 과거 다른 연동/백업 흔적으로 추정되나 내용은 미검토.
- 각 Controller 메서드 전수(41개 클래스, 다수 엔드포인트)에 대한 라인 단위 검토는 하지 않았다. 특히 파일 업로드가 실제로 S3로만 가는지, 예외적으로 로컬 파일을 다루는 지점이 있는지는 `Member_`, `Board_`, `Bulletin_` 3개 컨트롤러의 해당 메서드 본문을 직접 열어 재확인 필요.
- `Common/` 패키지 중 아직 본문을 열어보지 않은 파일: `Common/Errata.java`, `Common/LogRequest.java`, `Common/Message.java`, `Common/S3Config.java`, `Common/SshTunnel.java`(구현 세부), `Common/ChurchInitService.java`.
- `CLAUDE.md`에 기술된 원본 `Saint` 저장소(`c:/Users/xaexal/git/Saint`, MyBatis `DAO/` 포함)와 이번에 분석한 `Saint_functions` 저장소의 정확한 관계(포크 시점, 동기화 여부, 어느 쪽이 최신인지)는 확인하지 않았다.
- 테스트 코드(`src/test/`)의 존재 여부와 내용은 이번 분석에서 열어보지 않았다.
- OCI Functions의 구체적인 런타임(Java FDK 버전, Fn Project 기반 여부 등) 제약사항은 이번 분석 범위(로컬 소스코드)에는 포함되어 있지 않으므로 별도 확인이 필요하다.
