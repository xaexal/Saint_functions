# OCI Support 문의용 진단 자료 — OCI Functions → Compute VM NoRouteToHostException

작성일: 2026-09-07
작성 목적: OCI Functions에서 같은 VCN 안의 Compute 인스턴스로 나가는 사설망 접속이 실패하는 문제를 OCI 지원팀에 문의하기 위한 진단 자료 모음.

---

## 1. 문제 요약

OCI Functions로 배포한 Function 컨테이너가, **같은 VCN 안에 있는** Compute 인스턴스의 사설 IP(`10.0.0.14:3306`, MySQL)로 TCP 접속을 시도하면 다음 예외가 발생하며 즉시 실패합니다.

```
java.net.NoRouteToHostException: No route to host
```

- Function 자체는 정상적으로 기동되고, HTTP 요청도 정상적으로 수신함(Spring Boot 애플리케이션 컨텍스트까지 정상 로딩됨을 로그로 확인).
- **DB로 TCP 커넥션을 여는 시점에만** 위 예외가 발생하며, 타임아웃이 아니라 3~4초 내 즉시 실패함(연결 자체가 라우팅 단계에서 거부되는 패턴).
- **서로 다른 두 가지 서브넷 구성(아래 4절)으로 각각 재현했으며, 두 경우 모두 완전히 동일하게 실패**했습니다. 이는 저희가 조정 가능한 서브넷/라우트 테이블/게이트웨이/Security List 설정의 문제가 아니라, OCI Functions 서비스가 보조 VNIC을 통해 VCN 내부로 트래픽을 내보내는 방식 자체의 문제로 추정됩니다.

## 2. 환경 정보

| 항목 | 값 |
|---|---|
| Tenancy OCID | `ocid1.tenancy.oc1..aaaaaaaafjpfmaj4g5m2rsixwqwktyrpg4tspvzp3waaibgvosufgi5le6ra` |
| Region | `ap-seoul-1` (Seoul/ICN) |
| VCN | `vcn-20200902-1408` / `ocid1.vcn.oc1.ap-seoul-1.amaaaaaaglncviqa6m6doup744xvqnxkltc5igvdowp6fcdwy3vauc66taba` (CIDR `10.0.0.0/16`) |
| Compute 인스턴스 사설 IP | `10.0.0.14` (서브넷: `Public Subnet`, `10.0.0.0/24`) |
| Compute 인스턴스 역할 | MySQL 서버(3306 포트 리스닝, `0.0.0.0` 바인딩 확인됨) |
| 함수 런타임 | Docker 기반 Fn Java FDK (`fnproject/fn-java-fdk:jre17-1.1.22`), Java 17, Spring Boot 3.4.2 |

## 3. 재현 절차

1. OCI Functions Application/Function을 생성하고(아래 4절 리소스 참고), 이미지에는 Spring Boot 애플리케이션(내장 MySQL JDBC 드라이버, HikariCP)을 포함시킴.
2. Function을 invoke(`oci fn function invoke`)하여 HTTP 요청을 전달.
3. 애플리케이션 컨텍스트 부팅 도중 DataSource가 `10.0.0.14:3306`으로 JDBC 커넥션을 열려고 시도.
4. 이 시점에 `java.net.NoRouteToHostException: No route to host` 예외로 즉시 실패, Function은 HTTP 502(`FunctionInvokeExecutionFailed`)를 반환.

가장 최근 재현 (2026-09-07):
- Application: `saint-functions-app` (Public 서브넷 구성, 아래 4-A절)
- Function OCID: `ocid1.fnfunc.oc1.ap-seoul-1.amaaaaaaglncviqar3o2pp45z2hlohladj44pzox4tnuae3b4l2ohgurtqpa`
- `opc-request-id`: `DAABB95068DC4F3DA9A564A5DF166C74/01M1XKWSFW0000000000099AV9/01M1XKWSFW0000000000099AVA`
- 발생 시각: `2026-09-07T09:41:25.207Z`
- HTTP 응답: `502 FunctionInvokeExecutionFailed`, "function failed", 처리 시간 3.782초
- OCI Logging 원문(STDERR): `Caused by: java.net.NoRouteToHostException: No route to host`

## 4. 시도한 두 가지 네트워크 구성 (둘 다 동일하게 실패)

### 4-A. Public Subnet 구성 (최초 시도)

| 리소스 | 이름 | OCID |
|---|---|---|
| Subnet | `Functions Subnet` | `ocid1.subnet.oc1.ap-seoul-1.aaaaaaaahzsbdjvb52vi27jgebhov6v6igbhrqrcspoqoxs7svpqyg5j4w2q` (`10.0.1.0/24`) |
| Route Table | `Default Route Table for vcn-20200902-1408` (기존 것 재사용) | `ocid1.routetable.oc1.ap-seoul-1.aaaaaaaal3au2kc6hd4w4v6v4mien4h6x33pdzbu6okdvbkbq4lu5kjn47uq` — 규칙 1개: `0.0.0.0/0 → Internet Gateway` |
| Application | `saint-functions-app` | `ocid1.fnapp.oc1.ap-seoul-1.amaaaaaaglncviqaie7kckudqwrts3u4pcm5suiqgu2mnmlxyjzeesuckw3q` |
| Function | `saint-functions` | `ocid1.fnfunc.oc1.ap-seoul-1.amaaaaaaglncviqar3o2pp45z2hlohladj44pzox4tnuae3b4l2ohgurtqpa` |

→ **재현: `NoRouteToHostException`**

### 4-B. Private Subnet 구성 (전혀 다른 라우팅 경로로 재시도)

Public 구성 문제인지 확인하기 위해, NAT Gateway + Service Gateway를 갖춘 완전히 새로운 Private 서브넷/Route Table/Application을 생성해서 재시도했습니다.

| 리소스 | 이름 | OCID |
|---|---|---|
| Subnet | `Functions Private Subnet` | `ocid1.subnet.oc1.ap-seoul-1.aaaaaaaaethwlpyul3wbqt6dfbcxiwughj23l6zbuwce44ahn55bvqhles2q` (`10.0.2.0/24`) |
| Route Table | `saint-functions-private-rt` | `ocid1.routetable.oc1.ap-seoul-1.aaaaaaaasbfq2iwz2h3fzvd3rzsslx7aso6woqex7s7dey5bktkdahwumyja` — 규칙 2개: `0.0.0.0/0 → NAT Gateway`, `all-icn-services-in-oracle-services-network → Service Gateway` |
| NAT Gateway | `saint-functions-natgw` | `ocid1.natgateway.oc1.ap-seoul-1.aaaaaaaaxsopi5xqk2eqzcded5y3qna6vovkhtw37aaumj2aske3my4ktnqq` (Public IP `146.56.45.56`) |
| Service Gateway | `saint-functions-sgw` | `ocid1.servicegateway.oc1.ap-seoul-1.aaaaaaaab4h7betugrku6o2s6glojoopewzqo3b3yx4ar3bhysxlftane5ca` |
| Application | `saint-functions-app-private` | `ocid1.fnapp.oc1.ap-seoul-1.amaaaaaaglncviqao6qe72v6sve34pm3cnxtzimhietzwrolm4scxmydkf3a` |
| Function | `saint-functions` (동일 이미지) | `ocid1.fnfunc.oc1.ap-seoul-1.amaaaaaaglncviqa4y6vyusfnmpcasn7thovckkwowr3hpgp5qx6cy3fo2ja` |

→ **재현: 동일한 `NoRouteToHostException`** (Public 구성과 완전히 같은 실패 양상)

**해석**: 두 서브넷은 라우트 테이블/게이트웨이 구성이 완전히 다른데(Internet Gateway 경유 vs NAT+Service Gateway 경유) 결과가 동일하다는 것은, 문제가 서브넷의 라우팅 설정이 아니라 **Function의 보조 VNIC이 애초에 같은 VCN 내부의 다른 사설 IP로 트래픽을 내보내지 못하는** 더 근본적인 부분에 있다는 뜻으로 판단됩니다.

## 5. 관련 네트워크 리소스 실측 정보

### 5-A. Function의 보조 VNIC (Private 서브넷 구성 기준, 실행 중 실측)

```
VNIC OCID: ocid1.vnic.oc1.ap-seoul-1.abuwgljrgs6rghyaixns5yrstxj6jwqsi4kbr2zw5asqwfuiz3wqv2uiw2wq
private-ip: 10.0.2.87
is-primary: false
nsg-ids: [] (연결된 NSG 없음)
subnet-id: ocid1.subnet.oc1.ap-seoul-1.aaaaaaaaethwlpyul3wbqt6dfbcxiwughj23l6zbuwce44ahn55bvqhles2q (Functions Private Subnet)
route-table-id: null
skip-source-dest-check: false
defined-tags: Oracle-Tags.CreatedBy = "faas"
time-created: 2026-09-07T09:52:01.920Z
```

- `is-primary: false`인 보조 VNIC이며, `route-table-id`가 `null`로 표시됩니다(서브넷 자체의 Route Table을 상속받는 것으로 추정되나 VNIC 레벨에서는 확인 불가).
- NSG는 연결되어 있지 않음(`nsg-ids: []`) — NSG 규칙 부재로 인한 차단 가능성은 배제됨.

### 5-B. 대상 Security List (3개 서브넷 — Public Subnet, Functions Subnet, Functions Private Subnet — 이 전부 공유)

Ingress 규칙 중 관련 항목만 발췌:
```
protocol: TCP(6), source: 0.0.0.0/0, destination-port: 3306   ← MySQL, 이미 전체 공개
protocol: TCP(6), source: 0.0.0.0/0, destination-port: 22
protocol: TCP(6), source: 0.0.0.0/0, destination-port: 80/443/8000/8080/8085/8086/8087/8088
protocol: ICMP(1), source: 0.0.0.0/0, type 3 code 4
protocol: ICMP(1), source: 10.0.0.0/16, type 3
```
Egress: `protocol: all, destination: 0.0.0.0/0` (전체 허용)

→ **3306 포트가 이미 `0.0.0.0/0`(인터넷 전체)에 열려 있는 상태**이므로, Security List가 원인일 가능성은 없습니다.

### 5-C. Compute 인스턴스(MySQL 서버) 측 확인

- 사설 IP `10.0.0.14`, 서브넷 `Public Subnet`(`10.0.0.0/24`, `ocid1.subnet.oc1.ap-seoul-1.aaaaaaaa5zfssj62fy7jyfqcctludcinfcvzctygg46nrhndf3f6axlglvfq`)
- MySQL은 `0.0.0.0:3306`으로 리슨 중(전체 인터페이스 바인딩 확인됨)
- **Compute 인스턴스 → Functions Subnet(`10.0.1.58`) 방향으로 역방향 테스트 시 "Connection refused"까지 도달** — 즉 이 방향은 VCN 라우팅 자체가 정상 동작함을 의미(단순히 그 시점에 리스닝 중인 서비스가 없어서 refused가 난 것일 뿐, 라우팅 경로 자체는 살아있음). **오직 Function → Compute 방향만 편도로 막혀 있습니다.**

## 6. 로그 조회 정보

- Log Group: `ocid1.loggroup.oc1.ap-seoul-1.amaaaaaaglncviqajff2irlp5fr26thol2enzgmubk3ipvr5ijg56auxkgbq`
- Log: `ocid1.log.oc1.ap-seoul-1.amaaaaaaglncviqayl5tdxijj37mbjuvsamq6n4cl3dwxj2fzihdywvadsya`
- 조회 쿼리 예:
  ```
  search "<tenancy-ocid>/<log-group-ocid>/<log-ocid>" | where data.message contains 'NoRouteToHostException' | sort by datetime desc
  ```

## 7. 문의하고 싶은 내용 (OCI 지원팀에게)

1. OCI Functions의 보조 VNIC이 같은 VCN 안의 다른 사설 IP(Compute 인스턴스)로 통신하려면, 고객이 콘솔/API에서 설정할 수 있는 것 외에 **OCI 플랫폼 내부적으로 추가 승인/구성이 필요한 부분이 있는지** 확인 요청.
2. 위 4-A/4-B처럼 **서로 다른 두 서브넷 구성(Public+IGW / Private+NAT+SGW)에서 동일하게 실패**하는 것이 이 계정/리전/컴파트먼트에 알려진 이슈나 제한사항과 관련이 있는지 확인 요청.
3. Function의 보조 VNIC(5-A절 OCID)의 실제 라우팅 테이블/정책 라우팅 상태를 OCI 내부 관점에서 진단 요청.

---

*이 문서는 진단 목적으로만 작성되었으며, 여기 기재된 값은 전부 OCID/사설 IP 등 식별자이고 비밀번호·API 키 등 자격증명은 포함하지 않습니다.*
