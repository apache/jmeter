<!-- SPDX-License-Identifier: Apache-2.0 -->

# Load-test report: `<test name>`

## Executive summary

- **Decision:** `<pass | fail | inconclusive>`
- **Objective:** `<what the test evaluated>`
- **Result:** `<concise evidence-based outcome>`
- **Primary limitation:** `<most important confidence constraint, or none>`

## Test metadata

| Field | Value |
|---|---|
| Test plan/version | `<value>` |
| JMeter version | `<value>` |
| Application build | `<value>` |
| Environment | `<value>` |
| Start/end | `<timestamps and timezone>` |
| Ramp/steady state | `<windows>` |
| Result source | `<statistics.json and other artifacts>` |
| Monitoring | `<available telemetry>` |

## Workload achievement

| Measure | Planned | Achieved | Assessment |
|---|---:|---:|---|
| `<users/arrival rate>` | `<value>` | `<value>` | `<met/not met/not known>` |
| `<transaction throughput>` | `<value>` | `<value>` | `<assessment>` |
| Steady-state duration | `<value>` | `<value>` | `<assessment>` |

## SLO assessment

| Transaction | Criterion | Target | Observed | Result |
|---|---|---:|---:|---|
| `<name>` | `<p95/error rate/throughput>` | `<value>` | `<value>` | `<pass/fail/not evaluated>` |

## Transaction statistics

Record the configured percentile labels rather than assuming defaults.

| Transaction | Samples | Errors | Error % | Mean ms | Median ms | `<pct1>` ms | `<pct2>` ms | `<pct3>` ms | Throughput/s |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| `<name>` | `<value>` | `<value>` | `<value>` | `<value>` | `<value>` | `<value>` | `<value>` | `<value>` | `<value>` |

## Errors

| Error/category | Count | Affected transactions | Evidence |
|---|---:|---|---|
| `<error>` | `<value>` | `<transactions>` | `<dashboard/log source>` |

If error details were not supplied, state that `errorPct` alone cannot identify the cause.

## Time-series observations

- `<Fact supported by a supplied dashboard graph or metric>`
- `<Fact with time window and units>`

If only aggregate statistics were supplied, state that trends, warm-up behavior, spikes, and recovery were not evaluated.

## Observed facts

1. `<Directly supported finding with metric, units, scope, and window>`
2. `<Directly supported finding>`

## Hypotheses requiring verification

| Hypothesis | Supporting signal | Evidence needed |
|---|---|---|
| `<possible explanation>` | `<observed correlation>` | `<APM/resource/log evidence>` |

Do not present hypotheses as root causes.

## Test validity and limitations

- `<load-generator health>`
- `<environment representativeness>`
- `<sample size and duration>`
- `<missing monitoring or result artifacts>`
- `<deviations from the planned workload>`

## Recommendations

1. `<Action tied to an observed result>`
2. `<Next test that confirms or rejects a hypothesis>`
3. `<Measurement or instrumentation improvement>`

## Appendix

- Percentile configuration: `<aggregate_rpt_pct1/2/3>`
- JMeter report filters: `<filters>`
- Redactions applied: `<details>`
- Referenced artifacts: `<paths or identifiers without secrets>`
