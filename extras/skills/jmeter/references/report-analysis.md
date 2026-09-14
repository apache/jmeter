<!-- SPDX-License-Identifier: Apache-2.0 -->

# Results analysis and reporting

Generate a report from evidence supplied by the user. Prefer JMeter's dashboard `statistics.json` for aggregate transaction metrics and request additional dashboard or monitoring artifacts only when the requested conclusion needs them.

## Required context

Collect:

- test objective and pass/fail criteria
- JMeter version and plan identifier
- environment and application build
- planned workload, ramp, steady-state window, and duration
- actual test start/end time
- aborts, incidents, or configuration changes during the run
- load-generator health
- application and infrastructure telemetry available

Without this context, label the report preliminary or inconclusive.

## `statistics.json` fields

Interpret each transaction and the `Total` entry:

| Field | Meaning |
|---|---|
| `sampleCount` | Recorded samples |
| `errorCount` | Failed samples |
| `errorPct` | Failed samples as a percentage |
| `meanResTime` | Arithmetic mean elapsed time in milliseconds |
| `medianResTime` | Median elapsed time in milliseconds |
| `minResTime` | Minimum elapsed time in milliseconds |
| `maxResTime` | Maximum elapsed time in milliseconds |
| `pct1ResTime` | First configured response-time percentile |
| `pct2ResTime` | Second configured response-time percentile |
| `pct3ResTime` | Third configured response-time percentile |
| `throughput` | Samples per second over the measured interval |
| `receivedKBytesPerSec` | Mean received KiB per second |
| `sentKBytesPerSec` | Mean sent KiB per second |

Do not assume the three percentile labels. Confirm `aggregate_rpt_pct1`, `aggregate_rpt_pct2`, and `aggregate_rpt_pct3`; defaults can be overridden.

## Analysis sequence

1. **Validate the evidence** — confirm the result file, time window, sample filters, percentile configuration, and whether the test completed.
2. **Compare offered and achieved load** — a response-time pass is not meaningful if throughput was below target.
3. **Evaluate SLOs** — show pass, fail, or not evaluated for each transaction and criterion.
4. **Rank transaction impact** — identify high error counts, failed SLOs, and large percentile spread without converting correlation into causation.
5. **Inspect errors** — use dashboard error summaries or logs when provided; do not infer error types from `errorPct` alone.
6. **Inspect time series** — use dashboard graphs or exported metrics when provided; aggregate JSON alone cannot show warm-up, spikes, recovery, or gradual degradation.
7. **Correlate telemetry** — align JMeter timestamps with application, infrastructure, database, and network metrics before proposing a bottleneck.
8. **Document limitations** — identify evidence that was absent or conditions that reduce confidence.

## Facts and hypotheses

Keep these sections distinct.

A fact is directly supported by supplied data:

```text
Checkout p95 was 820 ms against a 500 ms SLO during the evaluated window.
```

A hypothesis requires verification:

```text
The response-time increase may coincide with database saturation; verify against
connection-pool wait time and database CPU for the same timestamps.
```

Never state that JMeter alone proved a server, database, network, garbage-collection, or autoscaling root cause.

## Validity warnings

Call out conditions such as:

- target throughput was not achieved
- load-generator resources or network were saturated
- the result window includes ramp-up when the SLO applies only to steady state
- assertions were absent or errors were ignored
- retries changed the intended workload
- test data was exhausted or heavily reused
- caches, pools, or autoscaling were not warmed as intended
- environment capacity or configuration differed from the target environment
- the sample count or test duration was too small for the requested percentile confidence
- clocks were not aligned across telemetry sources

## Privacy and sensitive data

JTL files, logs, request labels, URLs, and response data can contain identifiers or secrets. Ask for the minimum necessary aggregate artifacts, recommend redaction, and do not reproduce sensitive values in the report.

## Canonical JMeter documentation

- [Generating Dashboard Report](https://jmeter.apache.org/usermanual/generating-dashboard.html)
- [Listeners](https://jmeter.apache.org/usermanual/listeners.html)
- [CLI Mode](https://jmeter.apache.org/usermanual/get-started.html#non_gui)
- [Real-time Results](https://jmeter.apache.org/usermanual/realtime-results.html)
