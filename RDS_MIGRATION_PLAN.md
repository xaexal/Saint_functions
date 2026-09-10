# OCI → AWS 완전 이전 계획 (DB: MySQL → RDS)

## 배경 및 목표

지금까지의 AWS Lambda 전환은 "서버리스 계층만"이라는 명시적으로 좁힌 범위였다 — DB(OCI Compute VM의 MySQL, `193.123.234.59:3306`)와 Compute는 그대로 OCI에 남겨두고, Lambda가 SSH 터널(22번 포트, 유일하게 외부에서 정상 동작 확인된 경로)을 통해 그 DB에 접속하는 구조였다.

이 구조는 실제로 동작하지만(로그인/DB조회 curl로 검증 완료), 실제 브라우저로 테스트하는 과정에서 **콜드스타트마다 SSH 핸드셰이크가 추가되어 API Gateway의 30초 하드 타임아웃을 가끔 초과(503)하는 문제**가 발견됐다. 근본 해결은 SSH 터널 자체를 없애는 것이고, 그러려면 DB를 AWS 안(RDS)으로 옮겨 Lambda와 같은 VPC에서 직접 접속해야 한다.

사용자의 최종 목표는 이보다 크다: **궁극적으로 OCI를 완전히 버린다** (Compute VM + MySQL 전부). 이 문서는 그 첫 단계인 DB의 RDS 이전을 계획한다.

## 운영 전략 (사용자 확정, 2026-09-10)

- **원본 `Saint` 프로젝트(OCI Compute VM에서 서비스 중)는 코드/기능을 절대 건드리지 않는다.**
- 원본 Saint와 Saint_functions(AWS)를 당분간 병렬로 실제 서비스하지 않는다 — **실시간 복제(replication) 구조는 만들지 않는다.**
- 대신 **한번에 컷오버(cutover)** 방식: 어느 시점에 OCI DB에 대한 실제 쓰기를 멈추고, 그 순간의 데이터를 RDS로 최종 이전한 뒤, 그 이후로는 Saint_functions(AWS)만 실제 서비스로 쓴다. 원본 Saint는 그 시점 이후 사용이 중단되거나 별도로 이관된다(이 문서의 범위 밖).
- 컷오버 전까지, Saint_functions는 **테스트/검증 목적으로만** RDS의 스냅샷 데이터를 사용해도 된다 — 실사용자 데이터를 실시간으로 반영할 필요는 없다.

## 단계별 계획

### 0단계 — 조사 (완료, 2026-09-10)
- [x] **OCI MySQL 실제 스키마/데이터 크기**: 테이블 59개, 전체 18.17MB(데이터 13.81MB + 인덱스 4.36MB). **매우 작은 DB** — 덤프/복원이 수 초 안에 끝나는 규모라 컷오버 다운타임은 사실상 무시 가능한 수준. 가장 큰 테이블도 `board`(4.5MB, 16,592행) 정도.
- [x] **버전/문자셋/콜레이션**: MySQL `8.0.46`(Ubuntu 빌드), `utf8mb4` / `utf8mb4_0900_ai_ci`. 이 콜레이션은 MySQL 8.0 자체의 기본값이라 RDS MySQL 8.0으로 옮겨도 파라미터 그룹을 특별히 손볼 필요 없음. RDS는 정확히 8.0.46 패치버전을 선택 못할 수 있으나(RDS가 지원하는 8.0.x 중 가장 가까운 버전 선택), 메이저.마이너(8.0)만 맞으면 호환성 문제 없음.
- [x] **Lambda를 VPC에 넣을 때 필요한 것**: 코드 전수조사 결과, 백엔드가 인터넷으로 나가는 곳은 정확히 5곳 — SMTP(`smtp.naver.com`, 메일발송), Google/Kakao/Naver OAuth(인가·토큰·사용자정보 엔드포인트), Toss결제(`api.tosspayments.com`), 그리고 AWS S3(버킷 `xaexal`). **S3만 VPC 게이트웨이 엔드포인트로 무료 우회 가능하고, 나머지 4곳(메일/OAuth 3사/Toss)은 전부 순수 인터넷이라 NAT Gateway 없이는 도달 불가 — NAT Gateway 필요 확정.**

### 1단계 — RDS 인스턴스 생성 (완료, 2026-09-10)
- 리전 기본 VPC(`vpc-07fdd8e0bee36ecbb`, `172.31.0.0/16`) 재사용 — Lambda도 2단계에서 이 VPC로 들어감
- DB 서브넷 그룹 `saint-functions-db-subnet-group` (2개 AZ: ap-northeast-2a/2b 서브넷)
- 전용 보안그룹 `saint-functions-rds-sg`(`sg-0a18cf76cb04d7235`) — 인바운드 규칙은 아직 없음(2단계에서 Lambda 보안그룹만 3306 허용 예정, 그 전까진 아무도 접속 불가)
- RDS 인스턴스 `saint-functions-db` 생성: MySQL `8.0.46`(OCI와 동일 패치버전까지 일치), `db.t4g.micro`, `gp3` 20GB, 퍼블릭 접근 차단(`--no-publicly-accessible`), Multi-AZ 미사용(비용 절감, 단일 AZ), 백업 보존 1일, DB명 `saint`
- **마스터 비밀번호는 직접 다루지 않음** — `--manage-master-user-password`로 생성해서 AWS Secrets Manager가 자동 생성/보관(채팅에 노출된 적 없음)
- **엔드포인트**: `saint-functions-db.crsskumgam32.ap-northeast-2.rds.amazonaws.com:3306` (`available` 상태 확인, 2026-09-10)

### 2단계 — Lambda를 VPC 안으로 재구성 (완료, 2026-09-10)
- 퍼블릭 서브넷(2c, `subnet-0456c47891755a0d2`, 기존 메인 라우트테이블=IGW 유지)에 **NAT Gateway**(`nat-0812229b60d7b6a33`) 생성, 탄력적 IP(`3.38.11.148`) 연결
- 새 프라이빗 라우트테이블 `saint-functions-private-rt`(`rtb-0288258cb834074fa`, 기본경로 0.0.0.0/0 → NAT Gateway) 생성 후, RDS가 이미 쓰고 있던 2a/2b 서브넷을 여기로 연결(메인=IGW 라우팅에서 분리) → 이 두 서브넷이 이제 진짜 "프라이빗"이 됨
- 전용 보안그룹 `saint-functions-lambda-sg`(`sg-00357101796bf0508`) 생성, RDS 보안그룹(`sg-0a18cf76cb04d7235`)의 인바운드 3306을 이 Lambda SG로부터만 허용하도록 추가
- Lambda 실행 역할에 `AWSLambdaVPCAccessExecutionRole` 정책 추가(ENI 생성/관리 권한, VPC 연결에 필수)
- `saint-functions` Lambda 함수에 VPC 설정 적용(2a/2b 프라이빗 서브넷 + Lambda SG)
- **검증**: VPC 재구성 후에도 기존 SSH 터널 경로(`GET /` → 200 "home")와 OAuth2(`GET /oauth2/authorization/google` → 302, NAT 경유 인터넷 아웃바운드)가 모두 정상 동작함을 확인 — NAT Gateway가 예상대로 인터넷 접근을 대체함
- **아직 안 한 것(3단계로 이월)**: `application-aws.properties`를 RDS 엔드포인트로 전환(SSH 터널 제거)은 이번 단계에 포함 안 함 — 지금은 VPC/NAT 인프라만 갖춘 상태이고, 앱이 실제로 RDS를 쓰도록 바꾸는 건 3단계(테스트 데이터 이전 후) 진행

### 3단계 — 테스트 데이터로 검증 (완료, 2026-09-10)
- OCI MySQL의 59개 테이블 전체(스키마+데이터, 약 90초 소요)를 RDS로 1회성 복사(순수 JDBC로 구현한 임시 이전 코드 사용, 완료 후 삭제)
- RDS 전용 앱 계정(`xaexal`@`%`, `saint` 스키마 한정 권한) 신규 생성 — 마스터(admin) 계정을 앱이 직접 쓰지 않음. 비밀번호는 Lambda 환경변수(`RDS_DB_PASSWORD`)로 주입, 채팅에 노출된 적 없음
- `application-aws.properties`를 `app.datasource.mode=direct` + RDS 엔드포인트로 전환(SSH 터널 완전히 안 씀)
- **기능 검증**: `GET /` → 200, 로그인(`POST /member/doLogin`) → 200 → 세션 쿠키로 `GET /church/85`(로그인 필요) → 200. RDS로 옮긴 실제 데이터 기준으로 정상 동작 확인.

### ⚠️ 콜드스타트 근본 원인 재확인 (2026-09-10) — SSH 터널이 다가 아니었음
RDS 전환 후에도 콜드스타트가 여전히 **약 24초**(2회 반복 테스트로 재현) 걸리는 것을 발견함. 로그 분석 결과:
- DB 연결 자체는 SSH 터널 제거 덕분에 실제로 빨라짐(Hikari 커넥션 생성까지 0.2초 — 예전 SSH 핸드셰이크 포함 시간 대비 크게 단축)
- 하지만 **Spring Boot/Hibernate/JPA 컨텍스트 초기화 자체가 매번 약 10초 가까이 걸리고, Lambda의 INIT 단계 10초 제한에 걸려 조용히 재시도되는 패턴**이 그대로 남아있음 — 첫 시도 10초를 날리고 두 번째 시도(~13~14초)에서 성공, 합쳐서 총 24초. 이건 SSH/네트워크가 아니라 **JVM+Spring Boot 자체의 콜드스타트 특성**이라 DB 접속 방식과 무관하게 발생함.
- API Gateway 하드 타임아웃(30초)까지 여유가 6초 정도로 줄었을 뿐, 근본적으로 해소되진 않음 — 동시 요청이 몰리거나 조금만 더 느려지면 다시 503이 날 수 있음.
- **가능한 추가 해결책(아직 미착수, 사용자 결정 필요)**: (1) Lambda 메모리를 늘려 CPU 할당량 증가(간단/저비용, 효과 미검증), (2) AWS Lambda SnapStart(Java 전용, JVM 콜드스타트를 스냅샷으로 우회 — 가장 근본적이지만 적용 조건/제약 검토 필요), (3) Provisioned Concurrency(이전에 논의됐던 방법, 상시 비용 발생).

### 4단계 — 실제 컷오버 (사용자가 별도로 시점 결정)
- OCI DB에 대한 쓰기를 멈추는 시점 확정
- 그 시점의 최종 데이터를 RDS로 다시 이전(최신화)
- Saint_functions(AWS)를 정식 서비스로 전환
- 원본 Saint / OCI Compute VM 처리는 이 문서 범위 밖(사용자가 별도 결정)

## 아직 결정 안 된 것
- RDS 인스턴스 사양/비용 (단, DB가 18MB로 매우 작아 최소 사양(db.t3.micro/db.t4g.micro)으로 충분할 가능성 높음)
- NAT Gateway 비용 감수 여부 (필요성 자체는 0단계에서 확정됨 — 시간당 과금+데이터 처리 요금 발생)
- 컷오버 시점
- 원본 Saint / OCI Compute VM의 최종 처리 방법과 시점
