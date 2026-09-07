# Saint-functions → OCI Functions 전환 계획

- 대상 저장소: `C:/Users/xaexal/git/Saint_functions` (원본 `C:/Users/xaexal/git/Saint`는 이 작업에서 절대 수정하지 않음)
- 계획 작성 시점: 2026-09-05
- 선행 문서: `OCI_FUNCTIONS_ANALYSIS.md`(현재 코드베이스 구조 분석) — 본 계획은 그 분석 결과를 전제로 한다. 본 문서 작성을 위해 실제 소스코드를 재확인했으며, 재확인 과정에서 이전 분석에 없던 사실도 추가로 발견해 반영했다(아래 각주 참고).
- **본 계획 수립 시점까지 소스코드는 전혀 수정하지 않았다.** 아래 계획은 승인 후 실제 변경 작업의 기준 문서로 사용한다.
- 표기 규칙: 실제 코드/설정을 확인해 얻은 사실은 그대로 서술하고, 확정되지 않은 설계 판단·외부 인프라 정보(예: OCI VCN/서브넷/사설IP 등 실제 값)는 "(추정)" 또는 "(확인 필요)"로 명시했다.

---

## 0. 이번 재확인 과정에서 새로 확인한 사실 (이전 분석 문서 대비 추가분)

1. **`Common/S3Config.java`에 AWS Access Key/Secret Key가 평문으로 하드코딩되어 있다.** (`application.properties`의 `aws.access-key-id`/`aws.secret-access-key`를 사용하는 `S3Service`와는 별개로, `S3Config`는 자체적으로 하드코딩된 자격증명으로 `S3Client` 빈을 생성한다.) `ImageController`가 생성자 주입받는 `S3Client`는 이 `S3Config` 빈이다. **이는 이번 마이그레이션에서 가장 먼저 제거해야 할 하드코딩 시크릿이다.**
2. **파일 업로드는 이미 로컬 디스크를 전혀 거치지 않는다.** `Member_`, `Board_`, `Bulletin_` 3개 컨트롤러 모두 `MultipartFile.getBytes()`를 바로 `RequestBody.fromBytes(...)`로 S3에 업로드한다(`putObject`). 즉 파일 업로드 기능은 서버리스 환경에 이미 적합한 구조다.
3. **`secrets.properties`에 `SSH_PRIVATE_KEY_BASE64`, `SSH_PRIVATE_KEY_PASSPHRASE` 키가 존재하지만, 현재 소스코드 어디에서도 참조되지 않는다.** (`grep` 결과 0건) 미사용/예비 값으로 보인다(추정).
4. **`.github/workflows/test-ssh.yaml`**: `20260420` 브랜치에 push될 때 `appleboy/ssh-action`으로 GitHub Secrets(`SERVER_HOST`/`SERVER_USER`/`SERVER_PASSWORD`)를 이용해 SSH 접속만 테스트하는 워크플로우. 실제 배포 파이프라인은 아직 없다.
5. **`.vscode/git-commit-sync.ps1`**: 변경사항을 자동으로 `git add -A` → 커밋(타임스탬프 메시지) → `pull --rebase` → `push`하는 스크립트가 존재한다. 이 세션에서 커밋/푸시는 사용자 명시적 요청이 있을 때만 수행하며, 이 자동화 스크립트를 임의로 실행하지 않는다.
6. **`WebConfig.java`의 CORS 설정에 `allowCredentials(true)`가 명시되어 있다.** 즉 프론트엔드(`https://localhost:3000`)와 세션 쿠키(JSESSIONID)를 주고받는 것을 전제로 이미 설계되어 있다 — 세션 유지 방식을 결정할 때 이 점이 중요한 근거가 된다(7번 항목에서 활용).
7. **`SaintApplication.java`에 `@EnableScheduling`이 선언되어 있고, `EntityManagerFactory` 빈도 `@DependsOn("sshTunnelManager")`로 선언되어 있다.** 즉 스케줄러(`_Church_`)와 JPA EntityManager 초기화 모두 SSH 터널 초기화 순서에 강하게 결합되어 있다(이전 분석에서 `DataSource`만 확인했으나, `EntityManagerFactory`도 동일한 의존관계였음을 추가 확인).
8. **`GlobalExceptionHandler.java`에 주석 처리된 대체 버전(JPA 전환 완료 시 사용할 코드)이 남아 있다.** 현재 활성 버전은 `DataAccessException`/`Exception` 두 가지만 처리한다.
9. **테스트 코드는 `SaintApplicationTests.java` 1개뿐이며 `contextLoads()`만 존재**(실질적으로 빈 테스트). 이 프로젝트에는 API 단위/통합 테스트가 없다 — 마이그레이션 검증을 코드 리뷰와 수동 테스트에 의존해야 한다는 뜻이다.

---

## 1. 전체 변경 계획 (단계별)

작업 원칙(사용자 지정 15개 원칙)을 지키기 위해, "API 표면(Controller/URL/DTO)은 그대로 두고, 그 아래의 실행 모델·DB 연결·인증 저장소·설정 방식만 서버리스에 맞게 교체"하는 방향으로 진행한다. 즉 **Controller를 OCI Functions 핸들러로 새로 쪼개어 쓰는 것이 아니라, 기존 Spring MVC 애플리케이션 전체를 하나의 OCI Function 안에서 그대로 구동시키는 "Lift & Adapt" 방식**을 기본 전략으로 한다(근거는 6번, 9번 항목에서 상세 설명).

| 단계 | 내용 | 비고 |
|---|---|---|
| 0단계 | 본 계획 문서 작성 및 승인 | 현재 단계 |
| 1단계 | 하드코딩된 시크릿 제거 (`S3Config`, `application.properties` 평문 값들) 및 환경변수/OCI 설정 방식으로 전환 | 8번 항목 |
| 2단계 | DB 연결 방식을 "OCI Functions → VCN 내부망 → 기존 Compute VM의 MySQL" 직접 연결 구조로 전환 (SSH 터널은 로컬 개발용으로만 유지, 삭제하지 않음) | 7번 항목 |
| 3단계 | 세션을 인메모리 `HttpSession`에서 분산 세션(Spring Session, MySQL 백엔드)으로 전환 — Controller/AOP 코드는 무변경 | 11번 원칙 대응 |
| 4단계 | Spring Boot 애플리케이션을 OCI Functions(Java FDK, Fn Project) 컨테이너 안에서 구동하는 어댑터 계층 추가 (기존 `SaintApplication`, `ServletInitializer`, Controller는 그대로 유지) | 9번 항목 |
| 5단계 | `@Scheduled` 배치(자정 작업 2건)를 OCI Functions 트리거 외부 스케줄(OCI Resource Scheduler 등)로 분리 | 5번 원칙 대응 |
| 6단계 | 로컬 테스트 환경 구성 (Fn CLI 로컬 실행) | 10번 항목 |
| 7단계 | OCI로 실제 배포 및 검증 | 11번 항목 |
| 8단계 | GitHub Actions 자동 배포 파이프라인 구성 | 12번 항목 |

**본 계획서 승인 후에도, 실제 코드 변경은 사용자의 별도 지시가 있을 때 단계별로 진행한다(사용자가 "계획을 작성한 후 변경 작업을 기다려라"라고 명시함).**

---

## 2. 변경해야 할 파일 목록

| 파일 | 변경 내용 | 이유 |
|---|---|---|
| `src/main/java/com/xaexal/app/Common/S3Config.java` | 하드코딩된 AWS Access Key/Secret Key 제거, 환경변수/`@Value` 기반으로 변경 (또는 `S3Service`와 통합해 중복 제거 검토) | 0-1번 항목. 시크릿 하드코딩 금지(작업 원칙 9) |
| `src/main/resources/application.properties` | ① DB 접속 정보(`spring.datasource.username/password`, `app.datasource.direct-host` 등)를 환경변수 참조로 전환 ② `app.datasource.mode` 기본값을 `direct`로 유지하되 OCI 배포 프로파일에서 사용할 실제 MySQL 사설 IP를 환경변수로 주입 ③ Kakao/Naver OAuth2 client-secret 등 평문 값을 환경변수 참조로 전환 ④ `server.ssl.*` 관련 설정을 OCI Functions/API Gateway 배포 프로파일에서는 비활성화할 수 있도록 프로파일 분리(`application-local.properties` 신설과 연계) ⑤ Spring Session 관련 설정 추가 | 작업 원칙 8, 9, 10 |
| `src/main/java/com/xaexal/app/Common/DataSourceConfig.java` | 코드 변경은 최소화(이미 `direct`/`tunnel` 분기 구조를 갖추고 있어 재사용 가능). 다만 `app.datasource.direct-host` 기본값 및 필수값 검증 로직 점검, 필요 시 OCI 배포용 프로파일 값 반영 | 7번 항목 — 기존 구조를 최대한 재사용 |
| `src/main/java/com/xaexal/app/Common/SshTunnelManager.java`, `Common/SshTunnel.java` | **코드 삭제하지 않음.** 로컬 개발 환경(개발자 PC에서 원격 DB 접속)에서는 계속 사용. 다만 OCI Functions 배포 프로파일에서는 `app.datasource.mode=direct`로 설정해 이 컴포넌트가 아예 활성화되지 않도록 함(코드 변경 없이 설정값으로 제어) | 작업 원칙 1, 12, 13 — 기존 기능 삭제 금지, 프로파일 분기로 해결 |
| `build.gradle` | ① OCI Functions(Fn Java FDK) 실행에 필요한 의존성 추가(9번 항목) ② Spring Session(JDBC) 의존성 추가(3번 원칙 대응, 아래 7번 항목) ③ 기존 의존성은 삭제하지 않음 | 작업 원칙 13 |
| `src/main/java/com/xaexal/app/SaintApplication.java` | `WebApplicationType`을 OCI Functions 어댑터에서 제어할 수 있도록 하는 부분만 검토(어댑터 설계 확정 후 최소 변경). 기존 `@EnableScheduling`, `entityManagerFactory` 빈 정의는 유지 | 9번 항목 |
| `Dockerfile` (신규 또는 Fn 표준 빌드팩 사용 여부 결정 후 확정) | OCI Functions 컨테이너 이미지 빌드 정의 | 9, 11번 항목 |
| `.github/workflows/*.yaml` | 기존 `test-ssh.yaml`은 유지하고, 신규 배포 워크플로우를 별도 파일로 추가(기존 워크플로우 변경/삭제하지 않음) | 12번 항목, 작업 원칙 12·13 |

> 이번 단계에서는 **41개 Controller의 `@RequestMapping`/`@LoginCheck`/DTO/응답 형식은 원칙적으로 변경하지 않는다.** 세션 저장소만 분산 저장소로 바뀌므로 `LoginAspect.java`, `OAuth2SuccessHandler.java`를 포함한 세션 관련 코드도 **코드 자체는 무변경**이 목표다(Spring Session이 `HttpSession` API를 그대로 위임 구현하기 때문— 3번 항목 참고).

---

## 3. 새로 만들어야 할 파일 목록

| 파일(경로는 확정 전 제안, 실제 작업 시 확정) | 목적 |
|---|---|
| `src/main/java/com/xaexal/app/Function/OciFunctionHandler.java` (가칭) | OCI Functions의 진입점(Fn Java FDK `HTTPGatewayFunction` 또는 표준 함수 핸들러). HTTP 요청을 받아 기존 Spring `DispatcherServlet`에 위임하고 응답을 다시 OCI Functions HTTP 응답으로 변환하는 어댑터. **기존 Controller 코드는 호출만 될 뿐 수정되지 않는다.** |
| `src/main/resources/application-oci.properties` (가칭) | OCI Functions 배포 전용 프로파일. `app.datasource.mode=direct`, MySQL 사설 IP, `server.ssl.enabled=false` 등 배포 환경 전용 값을 환경변수 참조로 정의. 기존 `application.properties`(로컬 실행용)는 그대로 둠 |
| `func.yaml` | OCI Functions(Fn Project) 표준 함수 메타데이터 파일(런타임, 메모리, 타임아웃, entrypoint 정의) |
| `Dockerfile` | Fn Java FDK 기반 컨테이너 이미지 빌드 정의 (Java 17 유지) |
| `.env.oci.example` 또는 `oci-functions.env.example` | OCI Functions Configuration으로 주입할 환경변수 키 목록 예시(값은 비워둠, 문서화 목적) |
| `.github/workflows/deploy-oci-functions.yaml` (가칭) | GitHub Actions를 통한 OCI Functions 자동 배포 워크플로우 (신규 추가, 기존 워크플로우는 유지) |
| `OCI_FUNCTIONS_LOCAL_TEST.md` (선택) | 로컬에서 Fn CLI로 함수를 실행/테스트하는 절차 문서화 |

Spring Session 도입에 따라 별도의 새 Java 클래스가 필요한지는 설정만으로 해결 가능한지 확인 후 결정(원칙적으로 `@EnableJdbcHttpSession` 애노테이션 하나와 스키마 테이블 추가만 필요할 가능성이 높음 — 실제 작업 단계에서 확정).

---

## 4. 삭제가 필요한 파일

**현재 시점에는 삭제가 필요한 파일이 없다.**

작업 원칙 1, 12, 13("원본 프로젝트 수정 금지", "기존 기능 삭제 금지", "불필요한 의존성/코드 임의 삭제 금지")에 따라, 이번 전환에서는 어떤 파일도 삭제하지 않는 것을 기본 방침으로 한다. 구체적으로:

- `Common/SshTunnelManager.java` / `Common/SshTunnel.java` / `Common/TestSshTennel.java`(컨트롤러) — OCI Functions 배포 시에는 사용하지 않을 가능성이 높지만(2번/7번 항목), **로컬 개발 환경에서는 계속 필요하므로 삭제하지 않는다.** 설정값(`app.datasource.mode`)으로 활성/비활성만 제어한다.
- `Controller/Private_.java`(빈 클래스) — 용도가 불명확하지만(이전 분석 문서 16번 항목), 임의로 삭제하지 않는다. 용도는 사용자에게 별도로 확인이 필요하다(확인 필요).
- `ServletInitializer.java`(war 배포용 흔적) — 현재 war 빌드에 사용되지 않는 것으로 보이나(이전 분석 3번 항목), 삭제 여부는 사용자 확인 후 결정한다(확인 필요 — 원칙상 임의 삭제 금지 대상).

---

## 5. 기존 코드를 그대로 유지할 수 있는 부분

| 구분 | 파일/범위 | 근거 |
|---|---|---|
| Entity | `Entity/*.java` (47개) | 순수 JPA 매핑, 실행 모델과 무관 |
| Repository | `Repository/*.java` (40개, Spring Data JPA) | DB 스키마가 그대로면 무변경 |
| DTO | `DTO/*.java` (44개) | 순수 데이터 객체 |
| Controller의 `@RequestMapping`/메서드 시그니처/응답 형식 | `Controller/*.java` (41개) | 작업 원칙 4 — URL/HTTP Method/요청·응답 형식 무변경 |
| `@LoginCheck` + `LoginAspect` 인증 검사 로직 | `Common/LoginCheck.java`, `Common/LoginAspect.java` | Spring Session이 `HttpSession` API를 그대로 대체 구현하므로 AOP 로직 자체는 무변경 가능 |
| `OAuth2SuccessHandler.java`의 세션 저장 로직 | `Common/OAuth2SuccessHandler.java` | 동일 — `session.setAttribute(...)` 호출부는 무변경 |
| `Common/Result.java`, `Common/Errata.java`, `Common/Message.java` | 순수 상수/응답 래퍼 | 실행 모델 무관 |
| `Common/GlobalExceptionHandler.java` | 예외→응답 매핑 로직 | `@RestControllerAdvice`가 어댑터 내부 `DispatcherServlet`에서도 동일하게 동작(9번 항목의 어댑터 방식 채택 시) |
| `Service/S3Service.java`(단, 자격증명 하드코딩 부분은 `S3Config`에만 있음—`S3Service`는 이미 `@Value` 기반) | 로컬 디스크 의존 없음 | 그대로 이식 가능 |
| `Member_`/`Board_`/`Bulletin_`의 S3 업로드 로직 | 파일 업로드가 이미 인메모리→S3 직접 전송 방식 | 서버리스 환경에 이미 적합(0-2번 항목) |
| `DataSourceConfig.java`의 `direct`/`tunnel` 분기 구조 | 이미 이원화되어 있음 | OCI 배포 시 `mode=direct` 설정만으로 목표 구조 달성, 코드 재작성 불필요 |

---

## 6. Spring Boot의 어떤 부분이 OCI Functions 실행 모델과 충돌하는가

1. **내장 서블릿 컨테이너(Tomcat) 상시 리스닝 모델**: `spring-boot-starter-tomcat`으로 `server.port=8443`에서 상시 대기하는 구조는 OCI Functions의 "요청이 올 때만 컨테이너가 뜨고 처리 후 유휴 상태가 되는" 실행 모델과 근본적으로 다르다. OCI Functions(Fn Project)는 함수 컨테이너가 요청을 받아야 기동되며, HTTP 트리거는 API Gateway를 통해 함수로 전달되는 방식(Fn의 `fn-http-gateway` 확장 또는 OCI API Gateway 통합)이지, Spring MVC가 기대하는 "톰캣이 포트를 열고 있는" 모델이 아니다.
2. **상주 백그라운드 스레드(SSH 터널 워치독)**: `SshTunnelManager`의 `@Scheduled(fixedDelay = 30_000)` 워치독은 애플리케이션이 계속 살아있음을 전제로 한다. OCI Functions 컨테이너는 유휴 시간이 지나면 종료될 수 있어(정확한 유휴 타임아웃 정책은 OCI 설정에 따름 — 확인 필요), 이런 상시 스레드에 의존하는 설계는 신뢰할 수 없다.
3. **인메모리 `HttpSession`**: 톰캣의 세션 저장소는 해당 JVM 프로세스(컨테이너 인스턴스) 안에서만 유효하다. OCI Functions는 동일 요청이 항상 같은 컨테이너 인스턴스로 가는 것을 보장하지 않고, 여러 인스턴스가 동시에 뜰 수도 있어(오토스케일), 로그인한 사용자의 세션이 다른 인스턴스에서는 보이지 않는 문제가 발생한다.
4. **`@Scheduled` 크론 작업**: `_Church_`의 자정 배치 2건은 애플리케이션이 자정에 "실행 중"이어야 동작하는데, OCI Functions는 애플리케이션을 상시 실행 상태로 유지하지 않으므로 이 방식으로는 스케줄이 보장되지 않는다.
5. **`@PostConstruct` 기반 무거운 초기화**: `SshTunnelManager.init()`, `S3Service.init()`, `S3Config.s3Client()`, `SaintApplication`의 `EntityManagerFactory` 빈(SSH 터널 의존) 등 다수의 초기화가 애플리케이션 시작 시점에 몰려 있다. 콜드스타트마다 이 초기화가 반복되면 응답 지연이 커진다.
6. **`ddl-auto=update`**: 매 기동 시 Hibernate가 스키마 diff를 검사/변경하려고 시도한다. 함수 인스턴스가 자주 새로 뜨는 환경에서는 불필요한 반복 작업이자, 동시에 여러 인스턴스가 뜰 경우 스키마 변경이 경합할 위험이 있다(확인된 사실이 아니라 구조상의 위험 — 추정).
7. **애플리케이션 레벨 TLS 종단(`server.ssl.*`)**: OCI Functions 앞단은 보통 API Gateway가 TLS를 종단 처리하므로, 함수 내부에서 별도로 HTTPS 리스너를 여는 것은 이 아키텍처와 맞지 않는다.

> 결론: 위 충돌 지점들은 대부분 "Spring MVC 자체(Controller/DispatcherServlet)"의 문제가 아니라 "톰캣 상시 구동 + SSH 터널 + 인메모리 세션 + 크론"이라는 **주변 인프라 가정**의 문제다. 따라서 Controller 계층을 재작성하지 않고도, 이 주변 가정들만 교체하면 기존 API 표면을 그대로 유지할 수 있다는 것이 이번 계획의 핵심 판단이다.

---

## 7. MySQL 연결을 OCI Functions에서 처리하는 방법 (결정)

**결정: SSH 터널을 사용하지 않고, OCI Functions를 MySQL이 설치된 Compute VM과 같은 VCN(또는 라우팅이 연결된 VCN) 안에 배포하여 사설 IP로 직접 접속한다.**

근거 및 상세:

- OCI Functions는 Application 생성 시 특정 VCN/서브넷에 연결되어, 해당 서브넷 안에서 실행되는 것이 OCI의 표준 구조다. 즉 Functions가 이미 "OCI 사설 네트워크 안"에 위치할 수 있으므로, 기존 Compute VM의 MySQL에 사설 IP:3306으로 직접 접속하는 것이 SSH 터널보다 단순하고 표준적인 방법이다.
- 이 프로젝트의 `DataSourceConfig.java`는 이미 `mode=direct`일 때 `app.datasource.direct-host`/`direct-port`로 직접 접속하는 코드 경로를 갖추고 있다(코드 변경 불필요, 설정값만 변경). 즉 **작업 원칙 8("localhost 사용 금지, OCI 네트워크를 통해 접속")을 이미 존재하는 `direct` 모드로 그대로 충족할 수 있다.**
- SSH 터널 관련 코드(`SshTunnelManager`, `SshTunnel`, `TestSshTennel`)는 삭제하지 않고 로컬 개발용 경로로 남겨둔다(4번 항목).
- **확인이 필요한 실제 인프라 값(현재 코드/문서에서 확인 불가능 — 사용자 확인 필요):**
  - MySQL이 설치된 Compute VM의 사설 IP 및 포트(현재 `application.properties`의 `direct-host=127.0.0.1`은 "같은 서버에서 실행할 때"를 가정한 값이며, VM의 실제 사설 IP로 교체해야 한다)
  - OCI Functions Application에 연결할 VCN/서브넷, 그리고 그 서브넷에서 MySQL VM으로의 보안 목록(Security List)/네트워크 보안 그룹(NSG) 3306 포트 인바운드 허용 여부
  - MySQL 사용자 계정에 Functions가 위치할 서브넷 대역에서의 접속 권한이 부여되어 있는지(현재 `xaexal` 계정의 host 제한 여부는 미확인)
  - 이 값들은 실제 OCI 콘솔/네트워크 구성을 확인해야 하며, 추측으로 채우지 않는다.
- HikariCP 풀 크기(`maximum-pool-size=10`)는 함수 동시 실행 인스턴스 수를 고려해 인스턴스당 풀 크기를 낮추는 조정이 필요할 수 있다(예: 인스턴스당 2~3) — 정확한 값은 실제 배포 후 동시성 테스트로 결정(추정, 확정값 아님).

---

## 8. 환경변수 / Configuration 설계 (결정)

**결정: OCI Functions Configuration(함수/애플리케이션 단위 환경변수)을 통해 값을 주입하고, Spring Boot의 `${ENV_VAR}` 플레이스홀더 방식(현재 `secrets.properties`/`application.properties`에 이미 쓰이고 있는 패턴)을 그대로 확장해서 사용한다.** 이는 기존 코드 스타일(`${MAIL_PASSWORD}`, `${TOSS_CLIENT_KEY}` 등)과 완전히 동일한 방식이라 코드 변경 없이 설정 매핑만 바꾸면 된다.

현재 `secrets.properties`에 정의된 키(값은 미기재, 키 이름만 확인):
`DB_PASSWORD`, `MAIL_PASSWORD`, `AWS_SECRET_KEY`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `TOSS_CLIENT_KEY`, `TOSS_SECRET_KEY`, `SSH_PRIVATE_KEY_BASE64`, `SSH_PRIVATE_KEY_PASSPHRASE`(0-3번 항목 — 뒤의 두 개는 현재 미사용)

OCI Functions Configuration으로 이전할 환경변수 목록(제안, 실제 작업 시 확정):

| 환경변수 | 현재 소스 | 비고 |
|---|---|---|
| `DB_HOST` | (신규) | MySQL VM 사설 IP — `app.datasource.direct-host`에 매핑 |
| `DB_PORT` | (신규, 기본 3306) | `app.datasource.direct-port` |
| `DB_NAME` | `app.datasource.db-name` | 기존값 `saint` 유지 |
| `DB_USERNAME` | `spring.datasource.username` | 현재 `application.properties`에 평문(`xaexal`) — 환경변수로 이전 검토 |
| `DB_PASSWORD` | `secrets.properties`의 `DB_PASSWORD` | 그대로 사용 |
| `AWS_ACCESS_KEY_ID` | `S3Config`에 하드코딩된 값 + `application.properties`의 `aws.access-key-id` | **하드코딩 제거 후 환경변수로 일원화(0-1번 항목)** |
| `AWS_SECRET_ACCESS_KEY` | `secrets.properties`의 `AWS_SECRET_KEY` + `S3Config` 하드코딩 | 동일 |
| `AWS_S3_REGION`, `AWS_S3_BUCKET_NAME` | `application.properties` | 값 자체는 민감정보 아니나 일관성을 위해 함께 이전 검토 |
| `MAIL_PASSWORD` | `secrets.properties` | 그대로 사용 |
| `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET` | `secrets.properties` | 그대로 사용 |
| `KAKAO_CLIENT_SECRET` | `application.properties`에 평문 하드코딩 | 환경변수로 이전 필요 |
| `NAVER_CLIENT_SECRET` | `application.properties`에 평문 하드코딩 | 환경변수로 이전 필요 |
| `TOSS_CLIENT_KEY`, `TOSS_SECRET_KEY` | `secrets.properties` | 그대로 사용 |
| `SSL_KEYSTORE_PASSWORD` | `application.properties`에 평문 하드코딩 | OCI 배포 프로파일에서는 `server.ssl.enabled=false`로 아예 미사용 예정(API Gateway가 TLS 종단), 로컬 프로파일에서는 계속 사용 |
| `APP_DATASOURCE_MODE` | `app.datasource.mode` | OCI 배포 시 `direct` 고정 |
| `CORS_ALLOWED_ORIGIN` | `WebConfig.java`에 하드코딩(`https://localhost:3000`) | 배포 도메인 값으로 환경변수화 검토(현재는 코드 하드코딩이라 이 부분은 코드 변경 필요) |
| `SPRING_SESSION_STORE` (신규) | 없음 | Spring Session 백엔드 선택용(예: `jdbc`) |

- `secrets.properties`는 로컬 개발용으로 유지(`spring.config.import=file:secrets.properties`)하고, OCI 배포 프로파일에서는 이 파일 대신 OCI Functions Configuration이 주입하는 환경변수를 직접 참조하도록 한다(Spring Boot의 `${ENV_VAR}` 치환은 프로퍼티 파일 존재 여부와 무관하게 동작하므로 구조 변경 최소화 가능).
- **AWS 자격증명, DB 접속정보 등 실제 값은 이번 계획 문서에는 기재하지 않으며(이미 소스에 하드코딩된 값이 있다면 이번 전환 작업 중 반드시 로테이션 권장 — 특히 `S3Config`에 노출된 AWS 키), 실제 주입은 OCI Vault 연동 또는 OCI Functions Configuration의 암호화된 설정으로 진행한다(구체적 방법은 8~9단계 실 작업 시 OCI 콘솔/CLI로 확정).**

---

## 9. Java 17 기반 OCI Functions 실행 구조 (결정)

**결정: OCI Functions의 Java FDK(Fn Project 기반, Java 17 런타임 지원)를 사용하여 컨테이너 이미지를 빌드하고, 그 안에서 기존 Spring Boot 애플리케이션 컨텍스트를 함수 컨테이너 생명주기에 맞춰 구동한다.** 구체적으로 아래 방식을 채택한다.

- **실행 모델**: OCI Functions는 컨테이너 기반(Fn Project)이며 Java 런타임 이미지를 직접 지정할 수 있어 Java 17 유지가 가능하다(작업 원칙 6 충족).
- **HTTP 진입점**: OCI API Gateway → OCI Functions 연동 시, 함수는 Fn의 HTTP Gateway 확장(`HTTPGatewayContext`)을 통해 원본 HTTP 메서드, 경로, 헤더, 바디를 그대로 전달받고, 응답도 상태코드/헤더/바디를 직접 구성해 돌려줄 수 있다. 이 메커니즘을 이용해 **기존 41개 Controller를 개별 함수로 쪼개지 않고, "하나의 함수가 모든 요청을 받아 내부적으로 기존 Spring MVC `DispatcherServlet`에 위임"하는 방식**을 택한다.
- **왜 이 방식인가 (Spring Cloud Function 방식과 비교)**: Spring Cloud Function처럼 각 엔드포인트를 `Function<In,Out>` 빈으로 재작성하는 방식은 41개 Controller, 170여 곳의 `@LoginCheck`, 다수의 `@RequestMapping`/경로변수/멀티파트 처리 코드를 전부 새로운 함수형 시그니처로 다시 작성해야 하므로, **작업 원칙 3("Controller 재사용")과 4("URL/Method/요청·응답 형식 불필요한 변경 금지")에 정면으로 배치된다.** 따라서 기존 서블릿 기반 Controller를 그대로 두고, 함수 진입점에서 `MockHttpServletRequest`/`MockHttpServletResponse`(Spring이 제공하는 서블릿 API 구현체)를 만들어 기존 `DispatcherServlet`에 그대로 전달하는 어댑터 방식을 채택한다. 이 방식은 AWS 서버리스 환경에서 Spring MVC 앱을 옮길 때 흔히 쓰이는 "임베디드 서블릿 브리지" 패턴과 동일한 원리이며, OCI에는 이를 위한 공식 어댑터 라이브러리가 없으므로 **직접 구현이 필요하다**(이 부분은 새로 작성해야 할 코드이며, 기존에 검증된 확정 레퍼런스가 있는 것은 아니므로 실제 작업 단계에서 프로토타입으로 먼저 동작을 검증한다 — 이 판단 자체가 설계 결정이며 "추정"이 아니라 "새로 만들 컴포넌트"임을 명확히 한다).
- **Spring `ApplicationContext` 재사용**: Fn 컨테이너는 동일 인스턴스가 연속 요청을 처리하는 동안 재사용될 수 있으므로(웜 컨테이너), `ApplicationContext`를 함수 클래스의 정적(static) 필드에 1회만 초기화해 보관하고, 이후 요청부터는 재사용한다. 이렇게 하면 매 요청마다 Spring 컨텍스트 전체를 다시 띄우는 비용을 피할 수 있다(콜드스타트는 최초 1회만 발생).
- **`WebApplicationType`**: 어댑터 내부에서 Spring을 구동할 때는 실제 톰캣 포트를 열 필요가 없으므로 `WebApplicationType.NONE` 또는 서블릿 컴포넌트만 등록하는 최소 구성으로 조정하는 방안을 검토한다(내장 톰캣 자체를 띄우지 않고 `DispatcherServlet`만 직접 사용). 이 부분은 `SaintApplication.java`의 최소 수정이 필요할 수 있다(2번 항목의 "변경 파일" 목록에 포함됨).
- **빌드**: `func.yaml` + `Dockerfile`로 Fn CLI(`fn build`/`fn deploy`)를 통해 컨테이너 이미지를 빌드하고 OCI Container Registry(OCIR)에 푸시하는 표준 Fn 배포 흐름을 따른다.

---

## 10. 로컬 테스트 방법 (결정)

1. **1차: 기존 방식 그대로 로컬 Spring Boot 실행 유지** — `./gradlew bootRun --args='--app.datasource.mode=tunnel'` (기존 `restart.bat`/`run-tunnel.bat` 방식, 원본 CLAUDE.md에 이미 기술된 절차)으로 Controller/Service 로직 자체의 회귀 여부를 우선 검증한다. 이 경로는 이번 전환 작업과 무관하게 계속 유효해야 한다(작업 원칙 14).
2. **2차: Fn CLI 로컬 실행** — OCI Functions와 동일한 Fn Project 런타임을 로컬 Docker에서 구동하는 `fn start` + `fn deploy --local`(또는 `fn invoke`)로, 실제 함수 컨테이너 형태로 이미지를 빌드/실행해 HTTP 요청-응답 흐름을 검증한다. 이때 DB는 `app.datasource.mode=tunnel`(개발자 PC → 원격 MySQL) 또는 로컬 MySQL 컨테이너로 연결해 테스트한다.
3. **3차: OCI API Gateway 로컬 모사** — 실제 API Gateway 없이도 `curl`로 Fn 함수에 직접 HTTP 요청을 보내 41개 엔드포인트의 URL/Method/응답 포맷이 기존과 동일한지 회귀 테스트한다(자동화된 테스트 스위트가 없으므로— 0-9번 항목 — 최소한 주요 엔드포인트에 대한 수동/스크립트 기반 스모크 테스트 목록을 별도로 작성하는 것을 권장).
4. 세션/쿠키 동작 확인: 프론트엔드(`saint-app`, `https://localhost:3000`)를 로컬에서 API Gateway 모사 엔드포인트 또는 Fn 로컬 실행 주소로 연결해 로그인 → 세션 유지 → `@LoginCheck` 통과 여부를 브라우저에서 직접 확인한다(이 저장소의 기존 피드백 메모리 — "실제 브라우저 검증 요구" — 를 따름).

---

## 11. OCI 배포 방법 (결정)

1. **OCI Functions Application 생성**: 대상 VCN/서브넷을 MySQL이 설치된 Compute VM과 통신 가능한 네트워크로 지정(7번 항목).
2. **OCIR(Container Registry)에 이미지 푸시**: `fn deploy` 명령이 내부적으로 Docker 이미지를 빌드하고 OCIR로 푸시, 이후 OCI Functions가 해당 이미지를 실행.
3. **환경변수 설정**: OCI Functions Application/Function Configuration에 8번 항목의 환경변수 목록을 등록(민감정보는 가능하면 OCI Vault 시크릿을 참조하는 방식 검토 — 세부 연동 방법은 실 작업 단계에서 OCI 문서/콘솔 기준으로 확정, 현재는 미확정).
4. **API Gateway 연동**: OCI API Gateway를 생성하고, 기존 41개 Controller의 `@RequestMapping` 경로 전체(`/**`)를 단일 백엔드(이번 함수)로 라우팅하는 Gateway Deployment를 구성해, **URL 경로/HTTP Method가 프론트엔드 입장에서 기존과 동일하게 보이도록** 한다(작업 원칙 4).
5. **CORS/도메인 설정**: `WebConfig.java`의 `allowedOrigins`를 실제 프론트엔드 배포 도메인으로 갱신(현재 `https://localhost:3000` 하드코딩 — 배포 시 반드시 변경 필요, 8번 항목의 `CORS_ALLOWED_ORIGIN` 환경변수화와 함께 처리).
6. **OAuth2 redirect-uri 갱신**: Google/Kakao/Naver 콘솔에 등록된 redirect-uri를 배포 도메인 기준으로 갱신(애플리케이션 외부 설정, 코드 변경 아님).
7. **DB/네트워크 보안 검증**: MySQL VM의 Security List/NSG에서 Functions 서브넷 대역의 3306 인바운드를 허용하는지 확인 후 실제 연결 테스트.
8. **점진적 전환 권장**: 기존 Compute VM 기반 Spring Boot 서버(현재 8443 포트, 상시 구동)를 즉시 종료하지 않고, 일부 트래픽 또는 스테이징 환경에서 먼저 OCI Functions 경로를 검증한 뒤 전환하는 것을 권장(작업 원칙 14 "기존 프로젝트 동작 최대한 보존"과 부합).

---

## 12. 향후 GitHub Actions 자동 배포 구조 (고려사항)

- 현재 저장소에는 배포 워크플로우가 없고(`test-ssh.yaml`은 SSH 연결 테스트 전용), `20260420` 브랜치에 자동 커밋/푸시가 이루어지는 것으로 보이는 로컬 스크립트(`.vscode/git-commit-sync.ps1`)가 별도로 존재한다(0-4, 0-5번 항목).
- 향후 배포 워크플로우(`deploy-oci-functions.yaml`, 가칭)는 다음을 고려해 설계한다:
  1. **트리거**: 특정 브랜치(예: 배포 전용 브랜치, 현재 자동커밋되는 `20260420` 브랜치와의 관계는 사용자 확인 필요 — 같은 브랜치를 배포 트리거로 쓸지 별도 브랜치를 둘지는 결정 필요)에 push될 때 실행.
  2. **인증**: OCI CLI/Fn CLI가 OCI에 인증하기 위한 API Key 또는 Instance Principal/Resource Principal을 GitHub Secrets로 등록(현재 `secrets.properties`와는 별개로, GitHub Actions 전용 Secrets 저장소를 사용해야 함).
  3. **빌드 단계**: `./gradlew build` → `fn build`(Dockerfile 기반 이미지 빌드) → OCIR 푸시 → `fn deploy`(또는 OCI CLI `oci fn function update`)로 함수 갱신.
  4. **환경변수/시크릿 배포**: 배포 워크플로우가 OCI Functions Configuration을 갱신할지, 아니면 최초 1회만 콘솔에서 수동 설정하고 이후 워크플로우는 코드/이미지만 갱신할지 결정 필요(권장: 시크릿은 콘솔/Vault에서 별도 관리, 워크플로우는 이미지 배포만 담당 — 시크릿을 CI 로그에 노출할 위험을 줄임).
  5. **기존 `test-ssh.yaml`과의 관계**: 삭제하지 않고 그대로 두되, 신규 배포 워크플로우와 트리거 조건이 겹치지 않도록 브랜치/경로 필터를 분리.
- 이 항목은 "고려사항" 단계이며, 실제 워크플로우 파일 작성은 4~8단계(어댑터/배포 구조 확정) 이후에 진행하는 것이 순서상 맞다.

---

## 확인이 필요한 사항 (실 작업 착수 전 사용자 확인 요청)

1. MySQL이 설치된 Compute VM의 사설 IP/포트, 그리고 그 VM이 속한 VCN/서브넷 정보
2. OCI Functions Application을 생성할 VCN/서브넷을 MySQL VM과 같은 네트워크로 둘 것인지, 아니면 VCN 피어링/라우팅으로 연결할 것인지
3. MySQL 계정(`xaexal`)이 Functions가 위치할 서브넷 대역에서의 접속을 허용하도록 host 제한이 되어 있는지
4. 시크릿 관리 방식 — OCI Vault를 사용할지, OCI Functions Configuration(암호화 옵션 포함)만으로 충분한지
5. `Controller/Private_.java`(빈 클래스)와 `ServletInitializer.java`(war 흔적)를 이번 전환에서 어떻게 처리할지(4번 항목 — 현재는 삭제하지 않는 것으로 잠정 결론)
6. 배포 브랜치 전략 — 현재 `20260420` 브랜치 자동커밋/푸시 흐름과 신규 배포 워크플로우 트리거를 어떻게 분리할지
7. OCI API Gateway의 요청 페이로드 크기 제한이 현재 설정된 멀티파트 업로드 한도(`spring.servlet.multipart.max-file-size=50MB`)를 그대로 수용 가능한지(제한을 초과할 경우 업로드 방식 자체를 재검토해야 함 — 확인 필요, 현재 문서에는 실제 제한값을 기재하지 않음)

---

**다음 단계**: 위 계획에 대한 승인 및 "확인이 필요한 사항"에 대한 답변을 받은 후, 1단계(하드코딩 시크릿 제거)부터 순차적으로 실제 코드 변경 작업을 진행한다.
