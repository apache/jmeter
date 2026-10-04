<!-- SPDX-License-Identifier: Apache-2.0 -->

# Apache JMeter Agent Skill

This directory contains a vendor-neutral [Agent Skill](https://agentskills.io/) for planning Apache JMeter HTTP/API performance tests, sizing workloads, and producing structured reports from JMeter dashboard statistics.

The skill provides guidance and creates reviewable text artifacts. It does not run JMeter or send load to a target.

## Capabilities

- Gather requirements with an interactive performance-testing questionnaire.
- Design business scenarios and a JMX-ready component-tree skeleton using core JMeter elements.
- Calculate initial closed-model concurrency and explicit ramp-up values with assumptions and caveats.
- Distinguish closed concurrency models from open arrival-rate models.
- Interpret JMeter dashboard `statistics.json` data and produce a structured Markdown report.

## Installation

Agent Skills clients discover a directory containing a `SKILL.md` file. The Agent Skills specification defines the package format but does not require one installation location. Many local clients support the cross-client `.agents/skills/` convention.

### Project installation

Copy this `jmeter` directory into the project that will use it:

```text
<project>/.agents/skills/jmeter/
```

For example, from an unpacked JMeter distribution:

```sh
mkdir -p .agents/skills
cp -R "$JMETER_HOME/extras/skills/jmeter" .agents/skills/jmeter
```

PowerShell equivalent:

```powershell
New-Item -ItemType Directory -Force .agents/skills | Out-Null
Copy-Item -Recurse "$env:JMETER_HOME/extras/skills/jmeter" .agents/skills/jmeter
```

### User installation

For clients that support user-level discovery, copy the directory to:

```text
~/.agents/skills/jmeter/
```

Client-specific directories and cloud upload procedures vary. Consult the client's documentation in the [Agent Skills client directory](https://agentskills.io/clients). Upload or copy the complete `jmeter` directory so its `references/` and `assets/` remain available.

## Verify discovery

Start a new agent session and ask:

```text
Help me design a JMeter API load test and calculate the initial workload.
```

The agent should gather missing requirements before proposing thread counts or a test-plan structure.

## Example requests

```text
Design a JMeter test-plan skeleton for login, browse, and checkout scenarios.
```

```text
Estimate the users and ramp-up needed for 60 transactions per second, a
400 ms average transaction time, 1.6 seconds of think time, and a start rate
of four users per second.
```

```text
Use this JMeter statistics.json and our p95/error-rate SLOs to write a load-test report.
```

## Evaluation

`evals/evals.json` contains behavior cases for missing workload inputs, closed- and open-model sizing, dashboard reporting, the no-execution safety boundary, and raw JMX requests. Run each case in a clean agent session and grade its assertions against the produced output.

## Scope and safety

- HTTP and HTTPS scenarios using core JMeter components are the initial scope.
- The skill produces a component-tree design rather than raw JMX XML.
- Commands shown by the skill are instructions for an authorized human operator; the skill must not execute them.
- Production commands require an approved window, on-call ownership, monitoring, abort and stop conditions, and recovery readiness.
- JMX files can contain elements that execute arbitrary code. Review plans from untrusted sources before opening or running them.
- Keep credentials and sensitive test data outside plans and reports.

See `SKILL.md` for the complete workflow and safety rules.
