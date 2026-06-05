# Apache JMeter — Threat Model (v1 draft)

> Built on Apache JMeter's existing security policy at
> <https://jmeter.apache.org/security.html>. That page's "Security Model"
> statements are lifted here verbatim as the *(documented)* core; this
> document adds the threat-model structure around them (adversary model,
> in/out scope, properties, known non-findings, triage dispositions).

## §1 Header

- **Project:** Apache JMeter (`apache/jmeter`), `master`, against which this draft was written.
- **Date:** 2026-06-02 (v0); **revised 2026-06-04** incorporating the full JMeter PMC review (vlsi, milamberspace). **Status:** draft v1 — Wave-1/2 questions ratified by the PMC; remaining items are tracked follow-ups (XStream allowlist enforcement; `security.html` backlink) flagged in §14. **Author:** ASF Security team (drafted via the Scovetta threat-model rubric, building on JMeter's `security.html`), for PMC ratification.
- **Version binding:** versioned with the project; a report against version *N* is triaged against the model as it stood at *N*.
- **Reporting cross-reference:** §8-property violations → report privately per ASF process (`security@apache.org` → `private@jmeter.apache.org`); §3/§9 findings are closed citing this document and `security.html`.
- **Provenance legend:** *(documented)* = JMeter's own docs/`security.html`/repo; *(maintainer)* = confirmed by a JMeter PMC member through this process; *(inferred)* = reasoned from architecture, not yet confirmed — each has a matching §14 open question.
- **Coexistence:** this model is a strict superset of `security.html`; nothing there is weakened. `security.html` stays the canonical reporting/policy page and should link here for the expanded model (the `xdocs/security.xml` backlink is a tracked follow-up — see §14 Q8). *(maintainer)*
- **Draft confidence:** ~10 documented / ~20 maintainer / ~6 inferred. The RMI-SSL posture, the open-vs-run line, the XStream policy gap, the Security-Manager/JDK-24 status, the HTTPS-recording-proxy surface, and the SUT scope were all confirmed from JMeter source by the PMC (vlsi, milamberspace) on 2026-06-03/04.
- **What JMeter is:** Apache JMeter is a Java load-/performance-testing tool. A user builds a **test plan** (a `.jmx` file) in the GUI or by hand, then runs it — in the GUI, in non-GUI/CLI mode, or distributed across a controller and remote engines — to drive load at a *system under test* and collect results. Test plans may contain scripting (JSR223/Groovy/BeanShell) and therefore arbitrary code. *(documented — README, security.html)*

## §2 Scope and intended use

- **Primary use:** a **user-run tool** — the person running JMeter authors (or obtains) the `.jmx`, points it at a target they are authorised to test, and runs it locally or across machines they control. *(documented — security.html)*
- **Secondary / dual-use — authorized penetration testing.** JMeter is used (and distributed in pentest toolkits) by security professionals for **authorized** security assessments — rate-limit testing, parameterized enumeration, fuzzing, and custom scenarios via scripting (JSR223/Groovy/BeanShell), parameterized datasets (CSV Dataset Config), and broad protocol support (HTTP/JDBC/LDAP/JMS/…). This is a recognised legitimate use; the operator-trust model is unchanged (the operator is still the trusted party, running a `.jmx` they control against a target they are authorised to test). Conducting such tests **without authorization** is a legal/ethical violation, already covered by §11. *(maintainer, milamberspace 2026-06-04)*
- **The user is the trusted operator.** The central design statement: *"The purpose of JMeter is to execute the workload specified in the input jmx file, which may include arbitrary code"* — so JMeter running the plan it is given is the intended behaviour, not an attack. *(documented — security.html)*
- **Roles** (a tool, but distributed mode and the recording proxy introduce network/trust surfaces):
  - **user/operator** — supplies the `.jmx`, runs JMeter. **Trusted.** *(documented)*
  - **distributed remote engine** (`jmeter-server`) — a node that accepts a test plan from a controller over RMI and executes it. A **listening network surface** in distributed mode. *(documented — distributed testing guidance)*
  - **HTTPS recording proxy** (`ProxyControl`) — a user-initiated local proxy that MITMs the operator's own HTTPS traffic to record it, backed by a locally-generated Root CA. A **local trust boundary** (the CA private key on disk). *(maintainer, milamberspace 2026-06-04)*
  - **system under test (SUT)** — the target JMeter sends requests to and whose responses it parses. JMeter is the *client* here; the SUT is the operator's own/authorised target. *(maintainer — SUT scope confirmed 2026-06-04)*

**Component-family table:**

| Family | Entry point | Touches outside process | In model? |
| --- | --- | --- | --- |
| Test-plan load + execution (GUI / non-GUI) | open/run a `.jmx`; JSR223/Groovy/BeanShell elements | runs arbitrary code **by design** | **In, but see §9 — executing the plan is by-design** *(documented)* |
| Distributed / remote testing | `jmeter-server` RMI controller↔engine | network (listens) | **In — the real network boundary** *(documented)* |
| HTTPS recording proxy | `ProxyControl` (GUI or CLI) | local filesystem (CA keystore) + MITM on local HTTPS traffic | **In — local trust boundary (CA private key)** *(maintainer)* |
| Protocol client + response processing | HTTP/JDBC/JMS/etc. samplers; XPath/JSON/regex extractors | network (egress to SUT); parses responses | **In as a client, but hostile-SUT response handling is out of model (§3/§9)** *(maintainer)* |
| Report/results generation | listeners, dashboard | filesystem | **In (parsing result files)** *(inferred)* |
| Plugins / properties / functions | `user.properties`, plugins, `__function` calls | varies | **In core; third-party plugins out** *(maintainer)* |
| `examples/`, test resources, demos | — | — | **Out** *(maintainer — see §3)* |

## §3 Out of scope (explicit non-goals)

- **Executing the test plan the user gives it** — including arbitrary scripting in that plan. This is the whole purpose of the tool. A report that "a `.jmx` / JSR223 element can run code" is **not a vulnerability**; it is the documented design. *(documented — security.html)*
- **Isolating untrusted `.jmx` files** — explicitly delegated to the user: *"If you want to use JMeter to evaluate untrusted jmx files, it is up to you to provide the required isolation."* *(documented — security.html)*
- **Attackers who already control the machine JMeter runs on, its `user.properties`, its plugins, or the controller in a distributed run.** Operator-trusted. *(inferred)*
- **The security of the system under test, and robustness against a *hostile* SUT.** JMeter drives load at a target the operator owns/is authorised to test; defending the SUT, and hardening JMeter's parsers against a malicious SUT's crafted responses (XXE/ReDoS/size bombs), is not a claimed property. *(maintainer, vlsi + milamberspace 2026-06-04)*
- **Unauthorized use.** Pointing JMeter at systems the operator is not authorised to test (load-testing/pentesting a third party without permission) is a legal/ethical violation, not a JMeter vulnerability (§11). *(maintainer, milamberspace 2026-06-04)*
- **`examples/`, demo/test resources, and third-party plugins.** *(maintainer, vlsi + milamberspace 2026-06-04)*

## §4 Trust boundaries and data flow

- **The `.jmx` is NOT a trust boundary for the code the operator chose to run — it is trusted input by design** (§3). The user vouches for the plan they run. The one in-model caveat is the *open*-time deserialization path (see the open-vs-run boundary below and §8). *(documented + maintainer)*
- **The distributed-testing RMI surface IS a trust boundary** — and one whose defaults are protective. `jmeter-server` accepts a serialized test plan + commands from a controller over RMI. Per the PMC's source review (`RmiUtils.java`): RMI-over-SSL is **on by default** (`server.rmi.ssl.disable=false`), the server hard-codes `setNeedClientAuth(true)` (**mutual TLS required**), the keystore (`rmi_keystore.jks`) is **not shipped** so the engine **won't start** until the operator generates one (or explicitly disables SSL). So an attacker cannot reach an unprotected engine **unless the operator has explicitly opted out** with `server.rmi.ssl.disable=true`. *(maintainer, 2026-06-03 — vlsi + milamberspace, from source)* The historical no-SSL RMI-deserialization exposure (CVE-2019-0187) is what these defaults close. (Interface-binding nuance: the RMI port is **not** restricted to a single interface by default — see §5a.)
- **The HTTPS-recording-proxy CA private key IS a local trust boundary.** In recording mode JMeter generates a local Root CA (`ApacheJMeterTemporaryRootCA.*`) stored in a keystore on the operator's machine, dynamically issues per-host certs signed by that CA, and requires the operator to install the CA into their OS/browser trust store. This is analogous to running a private CA on the workstation: if the CA private key is compromised while the CA is still trusted, an attacker could mint certs for any domain accepted silently by the browser (MITM). The default `proxy.cert.validity=7` days limits the exposure window (a sound default; long validity is an opt-in misconfiguration). *(maintainer, milamberspace 2026-06-04 — from `ProxyControl.java`)*
- **The SUT→JMeter response path is *not* an in-model boundary.** The model assumes the tester points JMeter only at systems they own/are authorised to test (§2, §10); a *hostile* SUT crafting responses to exploit JMeter's extractors (XXE/ReDoS/size bombs) is therefore **out of model** — robustness there is hardening, not a claimed security property. *(maintainer, vlsi + milamberspace 2026-06-04)* Classifying it in-model would amount to claiming "JMeter is safe to point at attacker-controlled infrastructure", which is not the intended use case.
- **The open-vs-run boundary.** *Opening/browsing* a `.jmx` deserializes the plan and calls getters/setters on the **existing** control/logic classes JMeter ships — that is safe **provided you trust all existing classes in the JMeter distribution**. What would be a **vulnerability** is opening (not running) a file that causes a class **outside** the JMeter distribution to be instantiated/executed (an XStream gadget chain or a "jmx trick" synthesising and running new code before the user clicks Run). See §8/§9 for the current implementation gap. *(maintainer, vlsi + milamberspace 2026-06-03/04)*
- **Reachability precondition:** a finding is **in-model** if it is reachable (a) by a network party against the distributed-RMI surface **with the default SSL/mutual-TLS protections in place** (i.e. without the operator having set `server.rmi.ssl.disable=true`), (b) pre-execution by *opening* a file that triggers instantiation/execution of a class **outside** the JMeter distribution (the open-vs-run boundary above), or (c) against the recording-proxy CA-key local boundary. Anything that requires the user to run a `.jmx` they chose to run, or a hostile SUT, is `OUT-OF-MODEL`/`BY-DESIGN`. *(maintainer 2026-06-04)*

## §5 Assumptions about the environment

- **Runtime:** JVM (JMeter 6.0 sets JDK 17 as the minimum; users may run on JDK 24+); runs as a desktop GUI app, a CLI process, or `jmeter-server` remote engine. *(documented — README; maintainer for the JDK-floor detail)*
- **The user controls the host, the `.jmx`, `user.properties`, installed plugins, and (in distributed mode) the controller and engines.** *(inferred)*
- **Java Security Manager — removed on modern JDKs; NOT the forward defense.** `security.html` historically recommends the Security Manager for distributed runs, but it is gone on the JDKs JMeter now targets and **cannot** be relied on going forward *(maintainer, milamberspace 2026-06-04 — the recommendation in `security.html` should be updated)*:

  | JDK | Security Manager status |
  | --- | --- |
  | 17 | Deprecated for removal (JEP 411) |
  | 18–23 | Deprecated, still functional (startup warning) |
  | **24+** | **Fully removed (JEP 486) — not available at all** |

  Since JMeter 6.0's minimum is JDK 17 and operators may run on JDK 24+, the documented Security-Manager isolation recommendation is **not actionable on JDK 24+**. The equivalent isolation must now come from **outside** the JVM (see §9 / §10 for the recommended OS-level replacements). The actual distributed-mode defense is the RMI-over-SSL + mutual-client-auth posture (§4), not the Security Manager.
- **Negative side-effects inventory:** JMeter makes outbound network requests to the SUT by design; in distributed mode it listens for RMI; in recording mode it listens on a local proxy port and writes a CA keystore; it reads/writes test plans, results, and properties on the local filesystem; it executes user-supplied scripting. *(maintainer / documented)*

## §5a Build-time and configuration variants

Security-relevant configuration *(documented unless noted):*

- **`server.rmi.ssl.disable`** — default **`false`**: RMI-over-SSL is on by default, and the server requires **mutual** client auth (`setNeedClientAuth(true)`). The keystore is not shipped, so the engine won't start until the operator provides one or sets this to `true`. *(maintainer, 2026-06-03)*
- **`jmeter-server` bind interface — *not* restricted by default.** The RMI registry and server socket bind to the address `java.rmi.server.hostname` resolves to (typically the machine's primary hostname/IP via `getLocalHost()`), **not** a loopback-only or single-interface binding. On a multi-homed or public-IP host this can expose the RMI port on all interfaces. Operators limit exposure with `java.rmi.server.hostname` (set to `127.0.0.1` for loopback-only) and/or perimeter firewall rules — especially important when SSL is disabled. *(maintainer, milamberspace 2026-06-04 — corrects the earlier "refuses loopback" wording)*

  | Property | Default | Purpose |
  | --- | --- | --- |
  | `java.rmi.server.hostname` | (machine hostname) | Which IP the RMI server advertises/binds to; set `127.0.0.1` for loopback-only |
  | `server.rmi.port` | `1099` | RMI registry port |
  | `server.rmi.localport` | (dynamic) | Server socket port for the remote engine object; fix it for precise firewall rules |

- **`proxy.cert.validity`** — default **`7`** days (`ProxyControl.java`): the recording-proxy CA's per-host certs are short-lived by default, limiting MITM-key exposure. A long value (e.g. `3650`) is an opt-in misconfiguration, not the default. *(maintainer, milamberspace 2026-06-04)*
- **Scripting elements** (JSR223/Groovy/BeanShell, `__groovy`/`__BeanShell` functions) — always available; gated only by who controls the `.jmx` (the user). *(documented design)*
- **XStream `.jmx` deserialization policy** — `setupXStreamSecurityPolicy()` currently sets `NoTypePermission.NONE` then immediately overrides it with `AnyTypePermission.ANY`, i.e. the effective policy is a **no-op** (any JVM class may be instantiated on open). A package-scoped allowlist is the intended fix but is complicated by third-party plugins (see §8/§9/§12). *(maintainer, milamberspace + vlsi 2026-06-04)*

**Insecure-default ruling (resolved):** the distributed-mode defaults are **secure** — an unprotected engine requires the operator to actively set `server.rmi.ssl.disable=true`. A report that "jmeter-server is unauthenticated / RCE-by-default" is therefore **not** `VALID` (the defaults refuse to run without SSL + keystore + client-auth). An exposed engine reached after the operator disabled SSL is `OUT-OF-MODEL: non-default-config` — note **config**, not *build*: it's a runtime property opt-out, no rebuild involved *(maintainer, vlsi 2026-06-03)*. (This closes the CVE-2019-0187 class.)

## §6 Assumptions about inputs

Per-surface trust table *(maintainer unless noted):*

| Surface | Input | Attacker-controllable? | Caller/operator must enforce |
| --- | --- | --- | --- |
| `.jmx` test plan (run) | plan XML + embedded scripts | **no — trusted by design**; **yes only if** the user runs an untrusted plan, which they must isolate | don't run untrusted `.jmx` without OS/JVM isolation |
| `.jmx` test plan (open/browse) | plan XML deserialized via XStream | **bounded** — safe w.r.t. *existing* distribution classes; an `OUT-OF-distribution` class instantiated on open is in-model (currently under-enforced, §8) | treat opening untrusted `.jmx` as not fully safe until the XStream allowlist lands |
| Distributed RMI (`jmeter-server`) | controller commands + serialized plan | **no by default** (SSL + mutual client-auth required, keystore not shipped → won't start unprotected); **yes only if** the operator set `server.rmi.ssl.disable=true` | keep `server.rmi.ssl.disable=false`; generate the keystore; restrict the bind interface; never expose an engine after disabling SSL |
| Recording-proxy CA keystore | local CA private key on disk | local — operator-trusted; risk is a stolen key while the CA stays trusted | keep `proxy.cert.validity` short; remove the CA from the trust store after recording |
| SUT responses | HTTP/JDBC/etc. response bodies | **out of model** — the tester points JMeter at systems they own/are authorised to test | test only systems you trust/own (hostile-SUT parser robustness is hardening, not a claimed property) |
| `user.properties` / plugins | local config | no — operator-trusted | vet plugins |
| Result/JTL files (re-opened) | results to render | **yes if a result file is from an untrusted source** | don't open untrusted result files |

- **Size/shape/rate:** response/result-file size handling and recursion (XML/regex) bounds are open as *hardening* against a hostile SUT (out of model — see §3/§9). *(maintainer)*

## §7 Adversary model

- **Distributed-mode network attacker** — can reach a `jmeter-server` RMI port or MITM the controller↔engine link; goal: code execution on a remote engine. The primary in-model adversary, but defeated by the default SSL + mutual-TLS posture (§4/§8). *(maintainer)*
- **Crafted-`.jmx`-on-open attacker** — supplies a `.jmx` that, when merely *opened*, instantiates a class outside the JMeter distribution (XStream gadget chain) to execute code before the user runs the plan. In-model; see §8/§9 for the current enforcement gap. *(maintainer, vlsi + milamberspace 2026-06-04)*
- **Recording-proxy CA-key thief** — obtains the locally-stored CA private key while the CA is still trusted by the operator's browser/OS, to MITM the operator's traffic. In-model as a local boundary; mitigated by short `proxy.cert.validity` and removing the CA after use. *(maintainer, milamberspace 2026-06-04)*
- **Malicious-`.jmx` social-engineering (run)** — tricks a user into *running* a hostile plan. JMeter's stance: this executes by design; isolation is the user's responsibility. An *acknowledged, disclaimed* threat, not a defended one. *(documented — security.html)*
- **Out of scope — malicious SUT.** A target server returning hostile responses to exploit JMeter's extractors is **not** in the adversary model: the tester chooses the target and is expected to test only systems they own/are authorised to test. Parser robustness against a hostile SUT is hardening, not a defended property. *(maintainer, vlsi + milamberspace 2026-06-04)*
- **Out of scope:** anyone with control of the JMeter host, the controller, `user.properties`, or installed plugins. *(inferred)*

## §8 Security properties the project provides

- **Distributed-mode network protection (by default).** `jmeter-server` requires RMI-over-SSL with **mutual** client auth and won't start without a keystore the operator generates — so a network party cannot drive a remote engine to execute code in the default configuration. *Violation symptom:* RCE on a `jmeter-server` engine by an unauthenticated/MITM network party **while SSL is enabled** (i.e. without `server.rmi.ssl.disable=true`). *Severity:* security-critical. *(maintainer, 2026-06-03 — confirmed from source)*
- **No unintended code execution from *opening* a non-executed file (aspirational; partially enforced).**
  - *Result/JTL files:* opening for display should not instantiate arbitrary classes — the stronger guarantee. *(inferred)*
  - *`.jmx` files:* the in-model guarantee is "opening is safe **with respect to the *existing* JMeter distribution classes**" (deserialization drives existing control/logic getters/setters, which you trust by trusting the distribution). If a crafted `.jmx` triggers instantiation of a class **outside** the JMeter distribution (an XStream gadget chain, or dynamically-generated/-compiled code) before the user clicks Run, that is treated as a **`VALID`** vulnerability. **Current status — not yet enforced:** `setupXStreamSecurityPolicy()` is effectively `AnyTypePermission.ANY` (a no-op, carrying a 12-year-old `CVE-2013-7285` TODO), so XStream will instantiate any classpath class during `.jmx` deserialization. The boundary is therefore **aspirational, not enforced**; a package/classloader-scoped allowlist is a tracked improvement (§12), complicated by the need to keep third-party-plugin classes deserializable (a static `org.apache.jmeter.**` allowlist would break plugin test plans — §12). *Violation symptom:* opening (not running) a `.jmx`/result file instantiates or executes a class outside the JMeter distribution. *Severity:* security-critical. *(maintainer, vlsi + milamberspace 2026-06-04)*
- **HTTPS-recording-proxy default exposure window is bounded.** The recording proxy issues short-lived per-host certs (`proxy.cert.validity=7` days by default), limiting the window in which a stolen CA key is useful. *(maintainer, milamberspace 2026-06-04)* This is a *default-hygiene* property, not a guarantee against a stolen-and-still-trusted CA key (§4/§10).
- **Hostile-SUT response handling is *not* a claimed property** — see §3/§7/§9. The SUT is the tester's own/authorised target; hardening the extractors against a malicious SUT is desirable (`VALID-HARDENING` on an internal-audit track) but not a security guarantee. *(maintainer, vlsi + milamberspace 2026-06-04)*

## §9 Security properties the project does *not* provide

- **JMeter does not isolate or sandbox the test plan it runs.** *"The JMeter security model assumes you trust jmx input files."* Running a `.jmx` runs its code, including scripting. *(documented — security.html)* **This is the single most important statement for triage.**
- **JMeter does not protect a user who opens/runs an untrusted `.jmx`** — *"it is up to you to provide the required isolation."* *(documented)*
- **XStream class-loading boundary not yet enforced.** `setupXStreamSecurityPolicy()` applies `NoTypePermission.NONE` then immediately overrides it with `AnyTypePermission.ANY`, allowing instantiation of any JVM class during `.jmx` deserialization. A scoped allowlist (classloader-based, or manifest-declared per plugin, or a targeted gadget denylist — to avoid breaking third-party plugins) is a tracked improvement (§12); until it lands, the §8 "opening is safe given trust in existing classes" property is aspirational, not enforced. A *secondary* item: `BeanShellTestElement.readResolve()` instantiates a BeanShell interpreter on open (it does not compile/run the script); making it lazy would shrink the open-time surface. (`JSR223TestElement` is already clean.) *(maintainer, milamberspace + vlsi 2026-06-04)*
- **JMeter does not defend the system under test, and does not claim hardened response-parsing against a *hostile* SUT.** The tester points JMeter at systems they own/are authorised to test; a malicious SUT crafting responses to exploit the extractors (XXE on the XPath Extractor, ReDoS on regex patterns, unbounded response sizes) is out of model — `VALID-HARDENING` at most, not a vulnerability. §8 "safe response handling — best-effort, not a claimed property" is the framing. *(maintainer, vlsi + milamberspace 2026-06-04)*
- **No reliance on the Java Security Manager.** It is fully removed on JDK 24+ (JEP 486) and not a forward defense; the `security.html` recommendation predates that and should be updated. **On JDK 24+, the Security Manager is unavailable; OS-level isolation (container, systemd hardening, or dedicated OS user) is the recommended replacement** (see §10). Distributed-mode protection comes from RMI-over-SSL + mutual client auth (§4/§8), not the Security Manager. *(maintainer, milamberspace 2026-06-04)*
- **JMeter does not guarantee the recording-proxy CA key cannot be misused if stolen while still trusted.** The operator must remove the CA from their trust store after recording and keep `proxy.cert.validity` short. *(maintainer, milamberspace 2026-06-04)*
- **False friend:** loading a `.jmx` in the GUI is *not* a guaranteed-safe "preview" — opening it deserializes the plan and, given the current XStream policy, "may in some cases trigger code execution". *(documented + maintainer)*

## §10 Downstream responsibilities (the user/operator)

- **Only open and run `.jmx` files you trust**; isolate (separate OS user / container / VM) if you must evaluate an untrusted plan. Treat *opening* an untrusted `.jmx` as not fully safe until the XStream allowlist lands (§8/§9). *(documented + maintainer)*
- **For distributed testing, keep the secure defaults** — leave `server.rmi.ssl.disable=false`, generate the RMI keystore (`create-rmi-keystore.sh`), restrict the bind interface (`java.rmi.server.hostname`, firewall `server.rmi.port`/`server.rmi.localport`), and do **not** set `server.rmi.ssl.disable=true` to "simplify setup" on an engine reachable from an untrusted network. *(maintainer, 2026-06-03/04)*
- **For distributed-mode isolation on JDK 24+ (Security Manager gone), use OS-level isolation**, in order of preference: (1) **container** (`docker`/`podman` with non-root user, `--read-only` root FS, `--tmpfs /tmp`, `--cap-drop=ALL`, network scoped to the controller); (2) **systemd hardening** (`NoNewPrivileges=true`, `CapabilityBoundingSet=`, `PrivateTmp=true`, `ProtectSystem=strict`, `ReadWritePaths=…`); (3) **dedicated locked-down OS user** (least privilege). A reference `Dockerfile`/unit for `jmeter-server` is a suggested PMC deliverable (tracked — §14 Q8). *(maintainer, milamberspace 2026-06-04)*
- **For HTTPS recording:** remove the JMeter CA certificate from your OS/browser trust store after recording sessions; do not set `proxy.cert.validity` longer than the session needs. *(maintainer, milamberspace 2026-06-04)*
- **Only point JMeter at systems you are authorised to test**, and treat responses from untrusted targets as potentially hostile to the parsers (hardening posture, not a guarantee). *(maintainer)*
- **Vet third-party plugins** before installing. *(inferred)*
- **Don't open untrusted result/JTL files.** *(inferred)*

## §11 Known misuse patterns

- Running a `.jmx` obtained from an untrusted source without isolation. *(documented as the user's risk)*
- **Disabling RMI SSL (`server.rmi.ssl.disable=true`) to simplify distributed setup, then exposing `jmeter-server` on an untrusted network** — a widespread quick-start/tutorial antipattern that turns off the default mutual-TLS protection. *(maintainer, milamberspace 2026-06-03)*
- **Leaving the JMeter recording-proxy CA certificate permanently installed in the OS/browser trust store** after a recording session, or setting `proxy.cert.validity` to a long duration (e.g. years) — both widen the MITM exposure of a stolen CA key. *(maintainer, milamberspace 2026-06-04)*
- Treating "open in GUI" as a safe way to inspect an untrusted plan (especially while the XStream allowlist is unenforced). *(documented + maintainer)*
- Load-testing or pentesting a third party's system **without authorisation** (a legal/ethical violation, not just a security risk; the negative counterpart of the §2 authorized dual-use). *(maintainer, milamberspace 2026-06-04)*

## §11a Known non-findings (recurring false positives)

*(The highest-leverage scan-suppression input; the `.jmx` items are documented.)*

- "A `.jmx` / JSR223 / BeanShell element can execute arbitrary code" — **by-design**; JMeter executes the plan it is given (§3/§9, security.html). `BY-DESIGN`, not a finding.
- "Opening a `.jmx` triggers code execution" — **split into two cases:** (a) execution via *existing* JMeter distribution classes during deserialization/UI initialisation is `BY-DESIGN`, consistent with `security.html`; (b) execution via a class *outside* the JMeter distribution (XStream gadget chain, dynamically-compiled script triggered before Run) is `VALID` — the boundary the model intends to hold (currently under-enforced; §8/§9). *(maintainer, vlsi + milamberspace 2026-06-04)*
- "Scripting/reflection/`Runtime.exec` present in the codebase" — needed to execute test plans; `Runtime.exec()` is used by the OS Process Sampler (by design — it runs commands the user configured) and by `KeyToolUtils` to invoke `keytool` for proxy keystore management; reflection is required for plugin loading and test-element instantiation. `BY-DESIGN`/`KNOWN-NON-FINDING` unless reachable from the distributed-RMI surface or the open-a-file-instantiates-an-out-of-distribution-class path. *(maintainer, milamberspace 2026-06-04)*
- "JMeter listens on a local port (proxy)" — that is the user-initiated HTTPS recording proxy (`ProxyControl`), `BY-DESIGN`; not an unintended listening service. *(maintainer, milamberspace 2026-06-04)*
- "Keystore default password `changeit` in properties" — scanners flag the `changeit` value in commented-out lines of `user.properties`/`jmeter.properties`; the `rmi_keystore.jks` is not shipped (operator-generated via `create-rmi-keystore.sh/bat`), so the comment is a `KNOWN-NON-FINDING` (operators should pick a strong password for their generated keystore). *(maintainer, milamberspace 2026-06-04)*
- "RMI deserialization in distributed mode / jmeter-server is unauthenticated" — **not** `VALID`: SSL + mutual client-auth are on by default and the engine won't start without a keystore. It only becomes reachable if the operator set `server.rmi.ssl.disable=true` → `OUT-OF-MODEL: non-default-config` (closes CVE-2019-0187). *(maintainer, 2026-06-03)*
- "Hostile SUT response (XXE/ReDoS/size bomb) crashes/exploits JMeter" — out of model; test only systems you own/are authorised to test (§3/§9). At most a `VALID-HARDENING` on the internal-audit track. *(maintainer, vlsi + milamberspace 2026-06-04)*
- "XML external-entity (XXE) configuration in JMeter's XML processors (XPath Extractor, XStream)" — relevant only against a hostile SUT (out of model) or as part of the open-time XStream boundary (§8/§9). A targeted audit of whether `FEATURE_SECURE_PROCESSING` is enforced everywhere is a `VALID-HARDENING` candidate, not an in-model external-reporter finding. *(maintainer, milamberspace 2026-06-04)*
- "JMeter makes outbound requests / can be pointed anywhere" — by-design; it is a load generator (§3).

## §12 Conditions that would change this model

- A change to the distributed-mode RMI/SSL defaults, or the JDK removing/altering an isolation primitive the model relies on. *(maintainer/inferred)*
- **Merging an XStream class-loading allowlist** (replacing `AnyTypePermission.ANY` with a classloader-based / manifest-declared / targeted-denylist scheme that still admits third-party-plugin classes) — would upgrade the §8 `.jmx`-open guarantee from aspirational to enforced and tighten the §11a open-time triage split. *(maintainer, vlsi + milamberspace 2026-06-04)*
- A new way to *open* (not run) a file that triggers instantiation/execution of a class outside the distribution. *(maintainer)*
- A decision to bring hostile-SUT response parsing **into** the model (currently out of model, §3/§9) — would add a new §8 claimed property. *(maintainer)*
- A change to the recording-proxy CA/cert-validity defaults. *(maintainer)*
- A report that cannot be routed to one §13 disposition → revise the model.

## §13 Triage dispositions

| Disposition | Meaning | Licensed by |
| --- | --- | --- |
| `VALID` | Violates a §8 property via an in-scope adversary (RCE on a distributed engine despite the documented protections; code exec from *opening* a non-run file via an out-of-distribution class; misuse of a stolen recording-proxy CA key as a local boundary). | §8, §6, §7 |
| `VALID-HARDENING` | No §8 property broken, but a §11 misuse / hostile-SUT parser robustness is worth hardening on an internal-audit track (XXE/ReDoS/size-limits; louder open-time warnings; safer parser defaults). | §11, §9 |
| `OUT-OF-MODEL: trusted-input` | Requires the user to run a `.jmx` they chose to run, or control of host/controller/properties/plugins. | §3, §6, §7 |
| `OUT-OF-MODEL: adversary-not-in-scope` | Requires a capability the model excludes (e.g. a hostile SUT). | §7 |
| `OUT-OF-MODEL: unsupported-component` | Lands in `examples/` / demos / third-party plugins. | §3 |
| `OUT-OF-MODEL: non-default-config` | Only manifests after the operator changed a runtime property away from its secure default — above all `server.rmi.ssl.disable=true` (turning off the default RMI mutual-TLS), or a very long `proxy.cert.validity`. No rebuild involved. | §5a |
| `OUT-OF-MODEL: non-default-build` | Only manifests under a non-default build/compile-time setting (distinct from the runtime config opt-out above). | §5a |
| `BY-DESIGN: property-disclaimed` | Concerns a §9-disclaimed property — above all, "JMeter executes the (trusted) test plan, including its code". | §9 |
| `KNOWN-NON-FINDING` | Matches a §11a entry. | §11a |
| `MODEL-GAP` | Cannot be cleanly routed — triggers §12. | §12 |

## §14 Open questions for the maintainers

Waves 1 and 2 were answered by the PMC (vlsi, milamberspace) on 2026-06-03/04 from a JMeter source review; the resolutions are folded into the sections above and the tags promoted to *(maintainer)*. Retained here as a ratification record.

**Wave 1 — security.html core + distributed defaults ✅ ANSWERED (2026-06-03/04)**
1. security.html core (trusted `.jmx`; opening may execute; untrusted-plan isolation is the user's job) — confirmed canonical; "`.jmx`/scripting runs code" is `BY-DESIGN`. → §3/§9/§11a.
2. **Distributed defaults** — confirmed from `RmiUtils.java`: `server.rmi.ssl.disable=false`, mutual client-auth (`setNeedClientAuth(true)`), keystore **not shipped** (engine won't start unprotected). The bind interface is **not** restricted by default (binds to the resolved `java.rmi.server.hostname`, can expose all interfaces on a multi-homed/public host). An unprotected engine requires the explicit `server.rmi.ssl.disable=true` opt-out → `OUT-OF-MODEL: non-default-config`. → §4/§5a/§8/§13. *(maintainer, vlsi + milamberspace)*
3. `examples/` / demos / third-party plugins out of scope — confirmed. → §2/§3. *(maintainer, vlsi + milamberspace)*

**Wave 2 — in-model boundaries ✅ ANSWERED (2026-06-03/04)**
4. **Opening vs running** — opening a `.jmx` is safe given trust in the distribution's **existing** classes (it deserializes + drives existing control/logic getters/setters); the in-model line is opening that instantiates/executes a class **outside** the distribution. The current `setupXStreamSecurityPolicy()` is effectively `AnyTypePermission.ANY` (a no-op), so this boundary is aspirational, not yet enforced — a scoped allowlist that still admits plugin classes is the tracked fix (Q8). → §4/§8/§9/§11a/§12. *(maintainer, vlsi + milamberspace)*
5. **Hostile SUT** — **out of model**, fully confirmed: the tester targets only systems they own/are authorised to test, so hostile-SUT parser robustness is `VALID-HARDENING` (internal audit), not a claimed property; no in-model carve-out. → §3/§7/§8/§9/§11a. *(maintainer, vlsi + milamberspace 2026-06-04)*
6. **Security Manager** — removed on JDK 24+ (JEP 486; deprecated JEP 411 from JDK 17), **not** the forward defense; `security.html` should be updated. The replacement on JDK 24+ is OS-level isolation (container / systemd hardening / dedicated OS user). Distributed protection is RMI-over-SSL + mutual client-auth. → §5/§9/§10. *(maintainer, milamberspace 2026-06-04)*
7. **HTTPS recording proxy** — modeled as a local trust boundary (CA private key on disk); default `proxy.cert.validity=7` days is a sound default; risk is a stolen-and-still-trusted CA key. → §2/§4/§5a/§8/§10/§11/§11a. *(maintainer, milamberspace 2026-06-04)*

**Wave 3 — tracked follow-ups (open; do not block the doc)**
8. **Tracked engineering/doc follow-ups** (GitHub issues, not threat-model blockers, per the PMC):
   - **XStream allowlist** — replace `AnyTypePermission.ANY` with a classloader-based / manifest-declared / targeted-denylist policy that still deserializes third-party-plugin classes (a static `org.apache.jmeter.**` list would break plugin test plans — vlsi). Closes the 12-year-old `CVE-2013-7285` TODO. Upgrades the §8 open-time property from aspirational to enforced. *(maintainer — direction agreed; exact mechanism open.)*
   - **`security.html` backlink** — add the THREAT_MODEL.md link to `xdocs/security.xml` (the Velocity source for `security.html`); without it the model is not discoverable from the canonical reporting page. *(maintainer, milamberspace — track via PR/issue.)*
   - **Reference `Dockerfile` / systemd unit** for hardened `jmeter-server` on JDK 24+ (the post-Security-Manager isolation deliverable). *(maintainer, milamberspace.)*
9. Any further recurring scanner/fuzzer false positives to seed §11a beyond those now listed (scripting engines, reflection, `Runtime.exec`, proxy port, `changeit`, XXE)? → §11a. *(inferred — kept open for future scan output.)*
10. **Residual operator-trust / result-file assumptions** — confirm the still-*(inferred)* items: that the operator fully controls the host / `user.properties` / plugins / (distributed) controller and engines, that those host-controlling parties are out of the adversary model, that result/JTL-file *display* does not instantiate arbitrary classes, and that re-opening an untrusted result/JTL file is the operator's responsibility. These are reasoned from architecture and not yet PMC-confirmed. → §2/§3/§5/§7/§8/§10.
