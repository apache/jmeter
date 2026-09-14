---
name: jmeter
description: Design Apache JMeter HTTP and API performance tests, size virtual-user and arrival-rate workloads, and turn JMeter dashboard statistics into structured reports. Use when a user asks for a JMeter test scenario, test-plan skeleton, thread or ramp-up calculation, workload model, results interpretation, or load-test report. This skill is advisory and must never execute a load test.
license: Apache-2.0
compatibility: Requires an Agent Skills-compatible client. Apache JMeter is needed only when the user later validates or runs a plan; this skill must not execute JMeter or send traffic.
metadata:
  version: "1.0"
---

<!-- SPDX-License-Identifier: Apache-2.0 -->

# Apache JMeter performance testing

Use this skill to turn incomplete performance-testing requests into explicit, reviewable plans and reports. Ask only for information that is missing, show assumptions, and avoid false precision.

## Non-negotiable boundaries

- Never start JMeter, invoke a test plan, or send traffic to a target.
- Before providing an operator command for a real target, confirm that the user is authorized to test it. If authorization is not confirmed, provide no executable command; continue with design-only output and use `example.invalid`.
- For a production target, require an approved test window, informed on-call owners, live monitoring, explicit abort thresholds and a stop procedure, and a rollback or recovery plan before providing an operator command.
- Label commands as **operator commands—not executed by this skill**.
- Never request, reproduce, or store passwords, tokens, cookies, private keys, or production data. Use placeholders and operator-managed secret sources; do not place secrets in command-line arguments. If a user supplies a secret, do not repeat it; advise them to revoke or rotate it and remove it from chat, logs, and generated artifacts.
- Treat a JMX file as trusted executable input. JMeter plans can contain JSR223, BeanShell, OS Process, and other elements that execute code. Warn the user before they run a plan from an untrusted source.
- Do not hand-write or mutate raw JMX XML. Produce a component-tree skeleton that a user can implement with JMeter's GUI or bundled templates.
- Use core HTTP/HTTPS components for the initial design. Do not silently require plugins, distributed execution, JSR223, BeanShell, or OS Process Samplers.
- Do not claim a bottleneck or root cause from JMeter statistics alone. Separate observed facts from hypotheses that need server-side telemetry.

## Select the workflow

A request can use one or more workflows:

1. **Scenario and test-plan design** — load [references/test-plan-design.md](references/test-plan-design.md) and format the result with [assets/test-plan-skeleton.md](assets/test-plan-skeleton.md).
2. **Workload sizing** — load [references/workload-modeling.md](references/workload-modeling.md).
3. **Results analysis and reporting** — load [references/report-analysis.md](references/report-analysis.md) and format the result with [assets/load-test-report.md](assets/load-test-report.md).

When the user asks for an end-to-end plan, run the workflows in that order. Do not block later design on unknown information: mark unknowns and explain how they affect confidence.

## Common intake

Collect or infer the following, then ask concise follow-up questions for missing decision-critical fields:

- confirmation that the target is owned by the user or that testing is explicitly authorized
- test objective and test type
- environment and whether it is production-like
- protocol and business scenarios
- expected transaction mix or arrival rates
- current response-time baseline and think time or pacing
- duration, ramp shape, and success criteria
- authentication, test data, and correlation needs, without collecting secret values
- available application, infrastructure, and load-generator monitoring
- for production, the approved window, on-call ownership, abort and stop conditions, and rollback or recovery plan

Summarize the answers before designing the workload. Clearly distinguish user-provided facts, calculated values, recommendations, and unresolved assumptions.

## Scenario and test-plan workflow

1. Define the business outcome and measurable pass/fail criteria.
2. Model business transactions rather than an unstructured list of requests.
3. Select a closed or open workload model before choosing thread-group values.
4. Produce a core-component JMeter tree, a property table, test-data needs, correlation rules, assertions, timers, and validation steps.
5. Parameterize hosts, ports, credentials references, thread counts, ramp-up, and duration. Do not embed environment-specific values or secrets.
6. Include a one-user functional validation step and a small-load baseline step for the human operator.
7. Recommend CLI mode for load execution and exclude heavy GUI listeners from the load plan.
8. If the user requests a `.jmx` file, explain that the MVP produces a JMX-ready component tree rather than fragile raw XML. Point them to JMeter's bundled templates or GUI to create and save the plan.

## Workload-sizing workflow

1. Identify whether the target is user concurrency, transaction arrival rate, request rate, or throughput per business scenario.
2. Show the formula, substitutions, units, and rounding.
3. For closed workloads, calculate each scenario independently when cycle times differ, then sum the concurrency estimates.
4. Treat ramp-up as a workload-policy input, not an output of Little's Law. Ask for a user-start rate or desired warm-up duration.
5. State the steady-state and response-time assumptions, and provide a validation plan rather than presenting the estimate as guaranteed capacity.
6. If required inputs are missing, provide the formula and a bounded example instead of inventing values.

## Results-analysis workflow

1. Prefer JMeter dashboard `statistics.json` plus test metadata, planned load, SLOs, and relevant time-series or error artifacts.
2. Verify the configured percentile labels before interpreting `pct1ResTime`, `pct2ResTime`, and `pct3ResTime`.
3. Compare planned and achieved load before evaluating response-time targets.
4. Report overall and per-transaction sample count, errors, percentiles, throughput, and available network rates.
5. Identify invalid or incomplete evidence, including load-generator saturation, insufficient steady state, missing monitoring, or changed test data.
6. Separate observations, hypotheses, limitations, and recommended next tests.
7. If only aggregate statistics are available, do not invent time-based trends, error causes, or resource bottlenecks.

## Output quality checks

Before responding, verify that:

- every calculated value has inputs and units
- every assumption is visible
- transaction rate and request rate are not conflated
- the plan includes assertions, realistic delays, parameterization, and correlation where needed
- no secret or real credential appears in the output
- no command was executed
- report conclusions are supported by supplied evidence
- official Apache JMeter terminology and component names are used

## Canonical sources

Prefer the Apache JMeter user manual, component reference, bundled templates, properties, and repository source. When source material and memory conflict, use the project documentation and identify version-sensitive behavior.
