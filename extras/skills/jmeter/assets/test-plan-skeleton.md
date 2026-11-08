<!-- SPDX-License-Identifier: Apache-2.0 -->

# JMeter test-plan skeleton: `<name>`

## Status

- **Authorization:** `<confirmed by user | not confirmed>`
- **Confidence:** `<high | medium | low>`
- **Unknowns:** `<decision-relevant missing information>`
- **Assumptions:** `<assumption and expected impact>`
- **Production safeguards:** `<not applicable | approved window, on-call, monitoring, abort/stop, recovery>`

## Objective

`<Business outcome and test type>`

## Environment

| Field | Value |
|---|---|
| Target environment | `<environment>` |
| Application build | `<version>` |
| JMeter version | `<version>` |
| Monitoring | `<APM/infrastructure/load-generator telemetry>` |
| Constraints | `<test window, rate limit, shared resources>` |

## Success criteria

| Metric | Scope | Threshold | Evaluation window |
|---|---|---:|---|
| `<p95 response time>` | `<transaction>` | `<threshold>` | `<steady state>` |
| `<error rate>` | `<overall/transaction>` | `<threshold>` | `<steady state>` |
| `<throughput>` | `<overall/transaction>` | `<minimum>` | `<steady state>` |

## Workload model

- **Model:** `<closed concurrency | open arrivals>`
- **Rationale:** `<why it matches production demand>`
- **Target:** `<users, transactions/s, or arrivals/s>`
- **Ramp:** `<start rate or duration>`
- **Steady state:** `<duration>`
- **Ramp-down:** `<duration or stop behavior>`
- **Think time/pacing:** `<distribution and source>`

## Business scenarios

| Transaction | Traffic share/rate | Steps | State | Test data | Correlation |
|---|---:|---|---|---|---|
| `<name>` | `<value>` | `<method/path sequence>` | `<state>` | `<data>` | `<values>` |

## JMeter component tree

```text
Test Plan: <name>
├── User Defined Variables
├── HTTP Request Defaults
├── <shared managers>
└── <thread group selected for workload model>
    ├── <data configuration>
    ├── Transaction Controller: <transaction>
    │   ├── HTTP Request: <step>
    │   │   ├── <extractor if needed>
    │   │   └── <assertions>
    │   ├── <timer>
    │   └── HTTP Request: <step>
    └── <additional transactions>
```

## Operator properties

| Property | Safe default | Purpose |
|---|---|---|
| `protocol` | `https` | Target protocol |
| `host` | `example.invalid` | Target host |
| `port` | `443` | Target port |
| `threads` | `1` | Closed-model users |
| `ramp_up` | `1` | Ramp-up seconds |
| `duration` | `60` | Scheduled duration seconds |
| `<property>` | `<default>` | `<purpose>` |

Do not include secret values. Reference an operator-managed property or data source.

## Test data and correlation

- `<data source, uniqueness, sharing mode, exhaustion behavior>`
- `<dynamic value, extractor, default/missing behavior, reuse location>`
- `<cleanup or reset requirements>`

## Assertions

| Request/transaction | Assertion | Failure meaning |
|---|---|---|
| `<name>` | `<response code/content/schema>` | `<business failure detected>` |

## Human validation sequence

1. Review the target, data, authorization, abort conditions, stop procedure, and recovery readiness.
2. Build the plan from the component tree with JMeter's GUI or a bundled template.
3. Run one user and one iteration while debugging assertions and correlation.
4. Remove or disable heavy GUI listeners.
5. Establish a small-load baseline and verify load-generator monitoring.
6. Run incremental authorized tests before the target workload.

## Operator commands—not executed by this skill

```sh
jmeter -n -t <plan.jmx> -l <results.jtl> -e -o <empty-report-directory> \
  -Jprotocol=<protocol> -Jhost=<authorized-host> -Jport=<port> \
  -Jthreads=<threads> -Jramp_up=<seconds> -Jduration=<seconds>
```

## Review checklist

- [ ] Target authorization is confirmed.
- [ ] Unknowns and assumptions are accepted.
- [ ] Scenario mix represents the intended workload.
- [ ] Test data and correlation are validated.
- [ ] Assertions detect business failures.
- [ ] Delays and pacing are realistic.
- [ ] Secrets remain external and are absent from command-line arguments.
- [ ] Monitoring, abort thresholds, and the stop procedure are ready.
- [ ] Production tests have an approved window, informed on-call owners, and a rollback or recovery plan.
