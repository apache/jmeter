Security model: [SECURITY.md](./SECURITY.md)

## Building and testing (for coding agents)

JMeter builds with Gradle. Use the wrapper `./gradlew` (or `gw` from
[gdub](https://github.com/dougborg/gdub)). The full command list is in
[`gradle.md`](gradle.md), and toolchain details are in the
[Test builds](README.md#test-builds) section of `README.md`. The essentials:

- **Build everything (incl. tests + static checks):** `./gradlew build`
- **Unit tests only:** `./gradlew test` — single module, e.g. `./gradlew :src:core:test`
- **All checks (tests, checkstyle, spotless, …):** `./gradlew check`
- **Run the GUI from source:** `./gradlew runGui`
- **Assemble a runnable dist:** `./gradlew createDist && ./bin/jmeter`
- **Format before committing:** `./gradlew style` (spotlessApply + checkstyleAll); check-only: `./gradlew spotlessCheck checkstyleAll`
- **Skip tests:** append `-x test`

### Selecting the JDK for build and tests

JMeter uses Gradle [toolchains](https://docs.gradle.org/current/userguide/toolchains.html),
so JDKs are found locally or auto-provisioned. The default build JDK is 17
(artifacts target Java 8). To build or test under a specific JDK — the way CI
runs its matrix — pass build parameters:

- `-PjdkBuildVersion=<n>` — JDK to build with (e.g. `21`)
- `-PjdkTestVersion=<n>` — JDK to run **tests** with; `0` means "use the current Java"
- `-PjdkTestVendor=<vendor>` / `-PjdkTestImplementation=<impl>` — pin the vendor / VM implementation

Example — run the test suite on JDK 21 (Corretto):

```sh
./gradlew test -PjdkTestVersion=21 -PjdkTestVendor=corretto
```

List every available build parameter with `./gradlew parameters`.

### Other useful tasks

- After bumping a dependency version, refresh the expected checksums:
  `./gradlew -PupdateExpectedJars check`
- Coverage report: `./gradlew jacocoTestReport -Pcoverage`
- Release Audit Tool (license-header check): `./gradlew rat`

CI mirrors these across a JDK × vendor × OS × timezone × locale matrix (see
[`.github/workflows/main.yml`](.github/workflows/main.yml)); the Error Prone
static-analysis job runs on JDK 21.
