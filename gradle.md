# Gradle Command-Line

Useful commands (`gw` comes from https://github.com/dougborg/gdub, otherwise `./gradlew` can be used instead):

## Build and run

      # Build and start JMeter GUI
      gw runGui

      # Build project and copy relevant jars to rootDir/lib, and start JMeter
      gw createDist; ./bin/jmeter

      # Build all distributions (source, binary)
      gw :src:dist:assemble

## Base project info

      # Display all submodules
      gw projects

      # Different tasks for current module
      gw tasks

## Cleaning build directories

Technically `clean` should not be required, every time it is required it might be a bug.
However, it might be useful to perform a "clean" build:

      # Cleans current project (submodule)
      gw clean

      # Cleans the specified project
      gw :src:core:clean

## Dependencies

      # Displays dependencies. Gradle's "configurations" are something like different classpaths.
      gw dependencies

      # Displays dependencies for all projects
      gw allDependencies

      # Analyze why the project depends on `org.ow2.asm:asm`
      gw dependencyInsight --dependency org.ow2.asm:asm

      # Verify checksums of dependencies
      # Checksum verification is done by default
      # Expected checksums are stored in /checksum.properties file
      # Actual checksums are stored in /build/checksum/computed.checksum.properties

      # Update expected dependencies after updating a dependency version
      gw -PupdateExpectedJars check

## Static checks

### Release Audit Tool

      # Run RAT
      gw rat

### Code Formatting

      # Run spotlessApply and checkstyleAll
      gw style

      # Run checkstlye for all
      gw checkstyleAll

#### Fine Grained Formatting Commands

      # Run checkstyle for main (non-test) code
      gw checkstyleMain

      # Run checkstyle for test code
      gw checkstyleTest

      # Run Spotless checks
      gw spotlessCheck

      # Fix any issues found by Spotless
      gw spotlessApply

## Compiling Code

      gw compileJava
      gw compileTestJava
      ...

## Build Project

      # Just build jar (see build/libs/*.jar)
      gw jar

      # "build" is a default task to "execute all the actions"
      gw build

      # Test might be skipped by `-x test` (Gradle's default way to skip task by name)
      gw -x test build

      # Build project in parallel
      gw build --parallel

## Tests

Gradle automatically tracks task dependencies, so if you modify a file in `/src/jorphan/*`,
then you can invoke `gw check` at project level or in `core`, and Gradle will automatically
build only the required jars and files.

      # Runs all the tests (unit tests, checkstyle, etc)
      gw check

      # Runs just unit tests
      gw test

      # Runs just core tests
      gw :src:core:test

## Coverage

      # Generates code coverage report for the test task to build/reports/jacoco/test/html
      gw jacocoTestReport -Pcoverage

      # Generate combined coverage report
      gw jacocoReport -Pcoverage

## Generate Javadocs

      # Builds javadoc to build/docs/javadoc subfolder
      gw javadoc

      # Builds javadoc jar to build/libs/jorphan-javadoc.jar
      gw javadocJar

## Site

      # Creates preview of a site to src/dist/build/site
      gw :src:dist:previewSite

      # Builds and publishes site preview to a Git repository
      gw :src:dist:pushPreviewSite

## Maven

      # publishes Maven artifact to local repository
      gw publishToMavenLocal

      # Generate all pom files (pom-default.xml)
      # The files are placed under the individual src/**/build/publications folders
      gw generatePom

## Release Artifacts

      # Builds ZIP and TGZ artifacts for the release
      gw :src:dist:assemble

## Signing

It is implemented via [gradle signing plugin](https://docs.gradle.org/5.2.1/userguide/signing_plugin.html),
so it is done automatically provided credentials are specified via
[signatory credentials](https://docs.gradle.org/5.2.1/userguide/signing_plugin.html#sec:signatory_credentials)

      # Signs all the artifacts of the current module
      # see results in build/**/*.asc
      gw sign
> **Note:** signing is performed as a part of *release artifact build*, so it will be
> performed with `gw :src:dist:assemble`

## Releasing

      # Builds the project, pushes artifacts to svn://.../dev,
      # stages artifacts to Nexus staging repository
      gw prepareVote -Prc=1

> **Note:** The above step uses [an asf-like release environment](https://github.com/vlsi/asflike-release-environment),
> so it does not alter public repositories

      # Prepare another release candidate
      gw prepareVote -Prc=2 -Pasf

      # Release staged artifacts to SVN and Nexus
      gw publishDist -Prc=2 -Pasf
