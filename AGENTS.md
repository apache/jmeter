Security model: [SECURITY.md](./SECURITY.md)

## Building and testing

JMeter builds with Gradle. Use `./gradlew --quiet ...` to reduce output verbosity.

After modifying Java code, before reporting the change as done (handing
control back to the user), run `./gradlew --quiet classes style` and fix any
reported issues.

JMeter uses Gradle toolchains; JDKs are found locally or auto-provisioned. The
code targets JDK 17. To test under a specific JDK pass build parameters:

- `-PjdkTestVersion=<n>` — JDK to run **tests** with; `0` means "use the current Java"
- `-PjdkTestVendor=<vendor>` / `-PjdkTestImplementation=<impl>` — pin the vendor / VM implementation (jdkTestVersion is enough for most cases)

List every available build parameter with `./gradlew parameters`.
