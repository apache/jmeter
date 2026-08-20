<!-- SPDX-License-Identifier: Apache-2.0 -->

# Workload modeling and sizing

Use calculations to make assumptions visible, not to promise system capacity. First establish whether demand is modeled by concurrent users or by independent arrivals.

## Select the model

### Closed model

A fixed population completes an iteration, waits or paces, and starts another iteration. Use it for interactive users or clients that wait for a transaction to complete before continuing.

Required inputs:

- target completed business transactions per second
- mean end-to-end iteration duration under the expected load
- think time and pacing included in that duration
- transaction mix when scenarios have different cycle times

### Open model

New iterations arrive according to a schedule regardless of earlier completion. Use it for independent API calls, events, or externally imposed arrival rates.

Specify the arrival schedule directly. Little's Law can estimate expected in-flight work and required injector headroom, but a fixed thread count is not the workload definition.

## Closed-model concurrency

For one scenario in steady state:

```text
N ≈ X × R
```

Where:

- `N` is average concurrent iterations or users
- `X` is completed iterations per second
- `R` is mean iteration cycle time in seconds, including response time, think time, and pacing delays

Round the operational thread count up only after showing the unrounded result.

If the input is total request rate, first convert it to business-transaction rate. For a simple iteration with `k` requests:

```text
transaction_rate = request_rate / requests_per_iteration
```

Do not use that shortcut when branches, retries, cached resources, or weighted transactions make requests per iteration variable.

For multiple scenarios with distinct rates and cycle times:

```text
N_total ≈ Σ (X_i × R_i)
```

Calculate each scenario separately and show the sum.

## Ramp-up

Little's Law does not determine ramp-up. Choose either a start rate or a warm-up duration.

Given a user-start rate:

```text
ramp_up_seconds = target_threads / threads_started_per_second
```

Given a desired ramp duration:

```text
threads_started_per_second = target_threads / ramp_up_seconds
```

Round conservatively and state how rounding changes the start rate. The selected ramp should reflect cache and connection-pool warm-up, expected login/session creation, autoscaling behavior, and the test objective. Do not hide a deliberate spike inside an ordinary load test.

## Worked example

Inputs:

- target: `60 transactions/s`
- mean transaction response time: `0.4 s`
- think time: `1.6 s`
- start rate: `4 users/s`

Calculation:

```text
R = 0.4 s + 1.6 s = 2.0 s
N = 60 transactions/s × 2.0 s = 120 users
ramp-up = 120 users / 4 users/s = 30 s
```

Result: start with an estimate of 120 concurrent users and a 30-second ramp under those assumptions. Validate with a small baseline and incremental runs because response time and achieved throughput can change as the system approaches saturation.

## Required caveats

Always state the applicable caveats:

- Little's Law describes a stable long-run relationship; ramp-up, ramp-down, and short spikes are not steady state.
- Response time is load-dependent. A low-load baseline can underestimate concurrency and injector demand at target load.
- Average concurrency does not describe peak concurrency or a safe maximum.
- Target throughput cannot be guaranteed by adding threads when the system or injector is saturated.
- Retries can amplify offered load and should be measured separately.
- Load-generator CPU, memory, sockets, bandwidth, DNS, and TLS costs can limit achieved load.
- The steady-state measurement window must be long enough to represent the workload and exclude warm-up where appropriate.

## Validation sequence

Recommend that the human operator:

1. validate one iteration with one user;
2. establish a low-load response-time baseline;
3. run incremental steps while observing achieved throughput and response-time percentiles;
4. monitor the load generator as well as the system under test;
5. revise `R` and the concurrency estimate with measurements from the intended load range;
6. stop when abort criteria are reached rather than continuing to the planned maximum.

## Canonical JMeter documentation

- [Thread Group](https://jmeter.apache.org/usermanual/component_reference.html#Thread_Group)
- [Open Model Thread Group](https://jmeter.apache.org/usermanual/component_reference.html#Open_Model_Thread_Group)
- [Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [Remote Testing](https://jmeter.apache.org/usermanual/remote-test.html)
