# Diagnostic Report for OCI Support — OCI Functions → Compute VM NoRouteToHostException

Date: 2026-09-07
Purpose: Diagnostic information to accompany an OCI Support ticket regarding a private-network connectivity failure from OCI Functions to a Compute instance in the same VCN.

---

## 1. Problem Summary

A Function deployed on OCI Functions fails immediately with the following exception whenever it attempts a TCP connection to a Compute instance's private IP (`10.0.0.14:3306`, MySQL) **in the same VCN**:

```
java.net.NoRouteToHostException: No route to host
```

- The Function itself starts up correctly and receives the HTTP request normally (logs confirm the Spring Boot application context fully initializes, including JPA/Hibernate/Security beans).
- The exception occurs **only when opening the TCP connection to the database**, and it fails almost immediately (within 3–4 seconds) rather than timing out — consistent with the connection being rejected at the routing layer rather than simply being slow.
- **We reproduced this with two structurally different subnet configurations (see Section 4), and both failed identically.** This strongly suggests the issue is not caused by any subnet/route-table/gateway/security-list setting under our control, but by how the OCI Functions service itself routes egress traffic from its secondary VNIC within the customer VCN.

## 2. Environment

| Item | Value |
|---|---|
| Tenancy OCID | `ocid1.tenancy.oc1..aaaaaaaafjpfmaj4g5m2rsixwqwktyrpg4tspvzp3waaibgvosufgi5le6ra` |
| Region | `ap-seoul-1` (Seoul/ICN) |
| VCN | `vcn-20200902-1408` / `ocid1.vcn.oc1.ap-seoul-1.amaaaaaaglncviqa6m6doup744xvqnxkltc5igvdowp6fcdwy3vauc66taba` (CIDR `10.0.0.0/16`) |
| Compute instance private IP | `10.0.0.14` (subnet: `Public Subnet`, `10.0.0.0/24`) |
| Compute instance role | MySQL server (listening on port 3306, bound to `0.0.0.0`, confirmed) |
| Function runtime | Docker-based Fn Java FDK (`fnproject/fn-java-fdk:jre17-1.1.22`), Java 17, Spring Boot 3.4.2 |

## 3. Reproduction Steps

1. Create an OCI Functions Application/Function (resources listed in Section 4) using an image containing a Spring Boot application (with a bundled MySQL JDBC driver and HikariCP).
2. Invoke the Function (`oci fn function invoke`) to send it an HTTP request.
3. During application-context startup, the DataSource attempts to open a JDBC connection to `10.0.0.14:3306`.
4. At this point, `java.net.NoRouteToHostException: No route to host` is thrown immediately, and the Function invocation returns HTTP 502 (`FunctionInvokeExecutionFailed`).

Most recent reproduction (2026-09-07):
- Application: `saint-functions-app` (Public subnet configuration, Section 4-A below)
- Function OCID: `ocid1.fnfunc.oc1.ap-seoul-1.amaaaaaaglncviqar3o2pp45z2hlohladj44pzox4tnuae3b4l2ohgurtqpa`
- `opc-request-id`: `DAABB95068DC4F3DA9A564A5DF166C74/01M1XKWSFW0000000000099AV9/01M1XKWSFW0000000000099AVA`
- Timestamp: `2026-09-07T09:41:25.207Z`
- HTTP response: `502 FunctionInvokeExecutionFailed`, "function failed", 3.782s processing time
- OCI Logging output (STDERR): `Caused by: java.net.NoRouteToHostException: No route to host`

## 4. Two Network Configurations Tried (Both Fail Identically)

### 4-A. Public Subnet Configuration (initial attempt)

| Resource | Name | OCID |
|---|---|---|
| Subnet | `Functions Subnet` | `ocid1.subnet.oc1.ap-seoul-1.aaaaaaaahzsbdjvb52vi27jgebhov6v6igbhrqrcspoqoxs7svpqyg5j4w2q` (`10.0.1.0/24`) |
| Route Table | `Default Route Table for vcn-20200902-1408` (existing, reused) | `ocid1.routetable.oc1.ap-seoul-1.aaaaaaaal3au2kc6hd4w4v6v4mien4h6x33pdzbu6okdvbkbq4lu5kjn47uq` — 1 rule: `0.0.0.0/0 → Internet Gateway` |
| Application | `saint-functions-app` | `ocid1.fnapp.oc1.ap-seoul-1.amaaaaaaglncviqaie7kckudqwrts3u4pcm5suiqgu2mnmlxyjzeesuckw3q` |
| Function | `saint-functions` | `ocid1.fnfunc.oc1.ap-seoul-1.amaaaaaaglncviqar3o2pp45z2hlohladj44pzox4tnuae3b4l2ohgurtqpa` |

→ **Result: `NoRouteToHostException`**

### 4-B. Private Subnet Configuration (completely different routing path, tried as a follow-up test)

To rule out something specific to the public-subnet setup, we built an entirely new private subnet with a NAT Gateway + Service Gateway and a new Application, then retried.

| Resource | Name | OCID |
|---|---|---|
| Subnet | `Functions Private Subnet` | `ocid1.subnet.oc1.ap-seoul-1.aaaaaaaaethwlpyul3wbqt6dfbcxiwughj23l6zbuwce44ahn55bvqhles2q` (`10.0.2.0/24`) |
| Route Table | `saint-functions-private-rt` | `ocid1.routetable.oc1.ap-seoul-1.aaaaaaaasbfq2iwz2h3fzvd3rzsslx7aso6woqex7s7dey5bktkdahwumyja` — 2 rules: `0.0.0.0/0 → NAT Gateway`, `all-icn-services-in-oracle-services-network → Service Gateway` |
| NAT Gateway | `saint-functions-natgw` | `ocid1.natgateway.oc1.ap-seoul-1.aaaaaaaaxsopi5xqk2eqzcded5y3qna6vovkhtw37aaumj2aske3my4ktnqq` (public IP `146.56.45.56`) |
| Service Gateway | `saint-functions-sgw` | `ocid1.servicegateway.oc1.ap-seoul-1.aaaaaaaab4h7betugrku6o2s6glojoopewzqo3b3yx4ar3bhysxlftane5ca` |
| Application | `saint-functions-app-private` | `ocid1.fnapp.oc1.ap-seoul-1.amaaaaaaglncviqao6qe72v6sve34pm3cnxtzimhietzwrolm4scxmydkf3a` |
| Function | `saint-functions` (same image) | `ocid1.fnfunc.oc1.ap-seoul-1.amaaaaaaglncviqa4y6vyusfnmpcasn7thovckkwowr3hpgp5qx6cy3fo2ja` |

→ **Result: same `NoRouteToHostException`** (identical failure mode to the public configuration)

**Interpretation**: The two subnets have completely different route-table/gateway configurations (Internet Gateway path vs. NAT+Service Gateway path), yet the outcome is identical. This indicates the problem is not in subnet-level routing configuration, but in something more fundamental about how the Function's secondary VNIC reaches other private IPs within the same VCN.

## 5. Network Resource Details

### 5-A. Function's Secondary VNIC (from the Private-subnet configuration, captured live during a run)

```
VNIC OCID: ocid1.vnic.oc1.ap-seoul-1.abuwgljrgs6rghyaixns5yrstxj6jwqsi4kbr2zw5asqwfuiz3wqv2uiw2wq
private-ip: 10.0.2.87
is-primary: false
nsg-ids: [] (no NSG attached)
subnet-id: ocid1.subnet.oc1.ap-seoul-1.aaaaaaaaethwlpyul3wbqt6dfbcxiwughj23l6zbuwce44ahn55bvqhles2q (Functions Private Subnet)
route-table-id: null
skip-source-dest-check: false
defined-tags: Oracle-Tags.CreatedBy = "faas"
time-created: 2026-09-07T09:52:01.920Z
```

- This is a secondary VNIC (`is-primary: false`) with `route-table-id` reported as `null` at the VNIC level (presumably inheriting the subnet's route table, but this cannot be confirmed from the VNIC object itself).
- No NSG is attached (`nsg-ids: []`), ruling out an NSG rule as the cause.

### 5-B. Security List Applied to the Relevant Subnets (shared by Public Subnet, Functions Subnet, and Functions Private Subnet)

Relevant ingress rules:
```
protocol: TCP(6), source: 0.0.0.0/0, destination-port: 3306   ← MySQL, already open to the entire internet
protocol: TCP(6), source: 0.0.0.0/0, destination-port: 22
protocol: TCP(6), source: 0.0.0.0/0, destination-port: 80/443/8000/8080/8085/8086/8087/8088
protocol: ICMP(1), source: 0.0.0.0/0, type 3 code 4
protocol: ICMP(1), source: 10.0.0.0/16, type 3
```
Egress: `protocol: all, destination: 0.0.0.0/0` (fully open)

→ Since port 3306 is **already open to `0.0.0.0/0`**, the Security List cannot be the cause.

### 5-B-2. NSG on the Compute Instance's Primary VNIC (checked and ruled out)

The Compute instance's primary VNIC also has an NSG attached (`ig-quick-action-NSG`, OCID `ocid1.networksecuritygroup.oc1.ap-seoul-1.aaaaaaaao5fvyrogfl2oki72rzeej6vsk4hmblgnab3qyukayxrd5kg23ffq`), which originally only allowed TCP 22 (ingress/egress) — no rule for 3306. Since OCI enforces Security List AND NSG rules together (both must allow the traffic), we added explicit NSG ingress rules for TCP 3306 from both Function subnets (`10.0.1.0/24` and `10.0.2.0/24`) and re-invoked the Function.

→ **Result: identical `java.net.NoRouteToHostException` still occurs.** This rules out the destination NSG as the cause as well.

### 5-C. Compute Instance (MySQL Server) Side

- Private IP `10.0.0.14`, subnet `Public Subnet` (`10.0.0.0/24`, `ocid1.subnet.oc1.ap-seoul-1.aaaaaaaa5zfssj62fy7jyfqcctludcinfcvzctygg46nrhndf3f6axlglvfq`)
- MySQL is listening on `0.0.0.0:3306` (bound to all interfaces, confirmed)
- **A reverse test — connecting from the Compute instance toward the Functions Subnet (`10.0.1.58`) — reached "Connection refused"**, meaning basic VCN routing in that direction works correctly (refused simply because nothing was listening at that moment, not because the packet was dropped en route). **Only the Function → Compute direction fails.**

## 6. Log Query Information

- Log Group: `ocid1.loggroup.oc1.ap-seoul-1.amaaaaaaglncviqajff2irlp5fr26thol2enzgmubk3ipvr5ijg56auxkgbq`
- Log: `ocid1.log.oc1.ap-seoul-1.amaaaaaaglncviqayl5tdxijj37mbjuvsamq6n4cl3dwxj2fzihdywvadsya`
- Example query:
  ```
  search "<tenancy-ocid>/<log-group-ocid>/<log-ocid>" | where data.message contains 'NoRouteToHostException' | sort by datetime desc
  ```

## 7. Questions for OCI Support

1. Does an OCI Functions secondary VNIC require any platform-side configuration or approval beyond what is available to customers via the console/API in order to reach another private IP (a Compute instance) in the same VCN?
2. Is the fact that we reproduced identical failures across two structurally different subnet configurations (Public+IGW in 4-A vs. Private+NAT+SGW in 4-B) consistent with a known issue or limitation in this tenancy/region/compartment?
3. Could you check the actual routing/policy-routing state of the Function's secondary VNIC (OCID in Section 5-A) from OCI's internal perspective?

## 8. OCI Support Clarification Q&A (2026-09-07)

OCI Support asked for the following clarification. Answers below.

**Q: Whether the Function is in a private subnet or public subnet**
A: We tested **both**, and both reproduce the identical error (see Section 4-A/4-B).

**Q: The subnet CIDR for the Compute instance**
A: `10.0.0.0/24` ("Public Subnet"), part of VCN CIDR `10.0.0.0/16`. **This does NOT use `172.17.0.0/16` anywhere** — none of our VCN, subnets, or the Compute instance touch that range.

**Q: The route table rules on the Function subnet**
A: Public config: 1 rule — `0.0.0.0/0 → Internet Gateway`. Private config: 2 rules — `0.0.0.0/0 → NAT Gateway`, `all-icn-services-in-oracle-services-network (SERVICE_CIDR_BLOCK) → Service Gateway`. (Full detail in Section 4.)

**Q: The security list rules on the Function subnet**
A: A single Security List is shared across all three subnets (Compute's, and both Function subnets). Ingress: TCP 22, 80, 443, 3306, 8000, 8080, 8085–8088 from `0.0.0.0/0`, plus a couple of ICMP type-3 rules. Egress: all protocols to `0.0.0.0/0`. We also added an explicit NSG ingress rule for TCP 3306 on the Compute instance's NSG (Section 5-B-2) and retested — same failure persists.

**Q: Whether the Function needs access to internal OCI services only / public URLs / a Compute instance private IP in the same VCN**
A: A Compute instance's **private IP in the same VCN** — `10.0.0.14:3306` (MySQL). Not an OCI service endpoint, not a public URL — purely intra-VCN private traffic.

**Q: The exact error details, including whether the Compute subnet uses `172.17.0.0/16`**
A: `java.net.NoRouteToHostException: No route to host`, thrown by the JVM/MySQL Connector-J when opening the JDBC connection. Fails in ~3–4 seconds (not a 30s+ timeout — looks like an immediate rejection at the routing layer, not a black-holed packet). The Compute subnet is `10.0.0.0/24`, **not** `172.17.0.0/16`, so if OCI's known issue in that range is the intended pointer, it may not directly apply here — we asked OCI Support to confirm whether a related/underlying issue matches this symptom regardless.

## 9. Narrative Summary Provided to OCI Support Chat (2026-09-07)

When OCI Support offered a live chat before opening a formal SR, we provided the following description:

> We're migrating a Spring Boot backend to OCI Functions. The Function needs to connect to a MySQL database running on a Compute instance that's in the same VCN. The Function starts up fine and Spring Boot's application context fully initializes — the problem only happens the moment the Function tries to open a JDBC/TCP connection to the Compute instance's private IP (`10.0.0.14:3306`). At that point it fails almost instantly (3–4 seconds, not a timeout) with `java.net.NoRouteToHostException: No route to host`, and the invocation returns HTTP 502.
>
> What makes this confusing is that we've systematically ruled out everything on our side:
>
> - **Security Lists**: port 3306 is already open from `0.0.0.0/0` on the relevant subnets — not the cause.
> - **NSGs**: the Compute instance's VNIC had an NSG that only allowed port 22. We added an explicit NSG rule allowing TCP 3306 from the Function's subnets and re-tested — same exact error, so NSG isn't it either.
> - **Subnet type**: we first tried this with the Function's Application on a public subnet (route: `0.0.0.0/0 → Internet Gateway`). Then, specifically to rule out something about the public-subnet path, we built a brand-new private subnet with its own NAT Gateway + Service Gateway + route table, and a second Application pointed at that subnet. **Both configurations fail identically.** Two completely different routing paths, same exact exception.
> - **Basic VCN routing works**: we tested the reverse direction (Compute instance → Function subnet) and got "Connection refused" — meaning packets do get there, there's just nothing listening. So the VCN's routing fabric itself is fine; it's specifically the Function → Compute direction that's broken.
> - **Not the `172.17.0.0/16` known issue**: our VCN is entirely on `10.0.0.0/16`, the Compute subnet is `10.0.0.0/24` — nothing touches `172.17.0.0/16`.
>
> So at this point, having eliminated Security Lists, NSGs, subnet routing config, and the known Docker-bridge-range issue, this really looks like something about how the Function's own secondary VNIC (`is-primary: false`) routes egress traffic to another private IP inside the same VCN — something we can't inspect or configure from the customer side via console/API.
>
> What I'd love help with: can someone check, from OCI's internal view, whether this Function's secondary VNIC is actually able to route to other private IPs in the VCN at all, or whether there's a platform-side gap here? I'm happy to share the exact resource OCIDs (VCN, both subnets, both Functions Applications, the Compute instance, the NSG, request IDs and timestamps of failed invocations) — I have them all written up already.

---

*This document is for diagnostic purposes only. All values listed are identifiers (OCIDs, private IPs, etc.) — no passwords, API keys, or other credentials are included.*
