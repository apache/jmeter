<!-- SPDX-License-Identifier: Apache-2.0 -->

# HTTP/API test-plan design

Use this reference to produce a JMX-ready component tree. The initial scope is HTTP and HTTPS with Apache JMeter core components.

## Requirements to capture

Record the following as facts, assumptions, or unknowns:

- authorization to test the target
- objective and test type
- environment and release/build under test
- business scenarios and relative frequency
- endpoint method and path, without secret values
- session, authentication, and correlation behavior
- unique and reusable test-data requirements
- target concurrency or arrival rate
- response-time, throughput, and error-rate criteria
- baseline response and iteration times
- think time, pacing, duration, and ramp policy
- monitoring available for the application, infrastructure, network, and load generator

Do not turn daily users, page views, or total requests into concurrency without a time distribution and scenario model.

## Model business transactions

For each transaction, define:

| Field | Purpose |
|---|---|
| Name | Stable label used in JMeter results and reports |
| Business outcome | What completion means to a user or upstream system |
| Request sequence | Ordered methods and paths |
| Traffic share | Percentage or arrival rate |
| State | Anonymous, authenticated, or session-bound |
| Test data | Unique, recyclable, or read-only data |
| Correlation | Values extracted and reused |
| Assertions | Response code and content checks |
| Delay | Think time or pacing between actions |
| SLO | Percentile, error-rate, and throughput criteria |

Percentages must total 100% when they describe one traffic mix. Keep destructive or state-changing scenarios separate when they require data cleanup or stricter authorization.

## Core component tree

Adapt this structure to the scenarios rather than adding every component automatically:

```text
Test Plan
├── User Defined Variables
├── HTTP Request Defaults
├── HTTP Cookie Manager                 # session-based applications
├── HTTP Cache Manager                  # browser-like caching when applicable
├── HTTP Header Manager                 # non-secret common headers
└── Thread Group or Open Model Thread Group
    ├── CSV Data Set Config             # external test data
    ├── Transaction Controller: <business transaction>
    │   ├── HTTP Request: <step>
    │   │   ├── extractor               # only for dynamic values
    │   │   └── assertions
    │   ├── timer                       # realistic user delay
    │   └── HTTP Request: <next step>
    └── additional transactions
```

For accurate dashboard reporting, leave Transaction Controller **Generate parent sample** unchecked, which is the default. Header, Cookie, and Authorization Manager settings are not merged; ensure a sampler has only one manager of each type in scope.

Use the standard Thread Group for a closed, user-concurrency model. Consider the core Open Model Thread Group for a declared arrival schedule, while noting that it has been experimental since JMeter 5.5 and may change across versions.

## Parameterization

Use JMeter properties for values supplied by the operator:

```text
${__P(protocol,https)}
${__P(host,example.invalid)}
${__P(port,443)}
${__P(threads,1)}
${__P(ramp_up,1)}
${__P(duration,60)}
```

Use variables for values created inside the plan, such as CSV columns or extracted identifiers. Do not place passwords, access tokens, or private keys in the plan or in command-line arguments. Reference an operator-managed secret source instead, and keep actual values out of generated plans, commands, logs, and reports.

## Correlation and assertions

- Extract tokens, IDs, and session values from the response that creates them.
- Give every extractor an explicit missing-value behavior and validate the extracted value before reuse.
- Add response-code and business-content assertions. A successful transport response alone does not prove the transaction succeeded.
- Avoid regular expressions for structured JSON, HTML, or XML when a purpose-built extractor is available.
- Do not add JSR223 or BeanShell merely to avoid learning a core component.

## Timers, pacing, and scheduling

Place timers at the narrowest scope matching the modeled delay. Distinguish:

- **think time** — delay between user actions
- **pacing** — controls the interval between iteration starts
- **ramp-up** — controls how users or arrivals enter the test
- **steady state** — interval used for SLO evaluation

Zero delay is appropriate only when modeling a client or batch process that actually behaves that way.

## Results collection

Use the GUI and View Results Tree only while debugging a small functional run. Exclude memory-heavy listeners from a load plan. Have the operator run in CLI mode and write a JTL file with `-l`; generate the dashboard with `-e -o` or later with `-g -o`.

## JMX output boundary

Do not generate raw JMX XML in this skill. JMX serialization contains version-sensitive element properties and paired tree nodes. Instead:

1. produce the component tree and parameter table;
2. recommend the closest template under JMeter's **File > Templates** menu;
3. have the user create and save the plan with JMeter;
4. review the resulting plan structure before a one-user validation.

## Completion checklist

- [ ] Authorization status is explicit.
- [ ] Objective and SLOs are measurable.
- [ ] Workload model matches how demand arrives.
- [ ] Transaction mix and test-data lifecycle are defined.
- [ ] Hosts, load settings, and secrets are not hardcoded.
- [ ] Dynamic values are correlated and validated.
- [ ] Assertions detect business failures.
- [ ] Transaction Controller **Generate parent sample** remains unchecked for dashboard reporting.
- [ ] Think time or pacing is realistic.
- [ ] Load execution uses CLI mode without heavy listeners.
- [ ] A one-user validation and small baseline precede the target load.

## Canonical JMeter documentation

- [Building a Test Plan](https://jmeter.apache.org/usermanual/build-test-plan.html)
- [Elements of a Test Plan](https://jmeter.apache.org/usermanual/test_plan.html)
- [Component Reference](https://jmeter.apache.org/usermanual/component_reference.html)
- [Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [Generating Dashboard Report](https://jmeter.apache.org/usermanual/generating-dashboard.html#transaction_controller_requirements)
- [Customizable Templates](https://jmeter.apache.org/creating-templates.html)
