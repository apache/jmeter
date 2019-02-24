# Gradle command-line

Useful commands (gw comes from https://github.com/dougborg/gdub, otherwise `./gradlew` can be used instead):

* Build and run

      # Build and start JMeter GUI
      gw runGui

      # Build project and copy relevant jars to rootDir/lib, and start JMeter
      gw createDist; ./bin/jmeter

      # Build all distributions (source, binary)
      gw :src:dist:assemble

* Base project info

      # Display all submodules
      gw projects
      # Different tasks for current module
      gw tasks

* Cleaning build directories
Technically speaking, `clean` should not be required (every time it is required it might be just a bug), however it might be useful to perform a "clean" build 

      # Cleans current project (submodule)
      gw clean
      # Cleans the specified project
      gw :src:core:clean

* Dependencies

      # Displays dependencies. Gradle's "configurations" are something like different classpaths.
      gw dependencies

      # Analyze why the project depends on `org.ow2.asm:asm`
      gw dependencyInsight --dependency org.ow2.asm:asm

      # Verify checksums of dependencies
      gw verifyChecksums

      # Print current checksums like in gradle/dependencyVerification.gradle.kts
      gw calculateChecksums

* Static checks

      # Run RAT
      gw rat
      # Run checkstyle for main (non-test) code
      gw checkstyleMain
      # Run checkstyle for test code
      gw checkstyleTest

* Compiling code

      gw compileJava
      gw compileTestJava
      ...

* Build project

      # Just build jar (see build/libs/*.jar)
      gw jar
      # "build" is a default task to "execute all the actions"
      gw build
      # Test might be skipped by `-x test` (Gradle's default way to skip task by name)
      gw -x test build
      # Build project in parallel
      gw build --parallel

* Tests
Gradle automatically tracks task dependencies, so if you modify a file in `/src/jorphan/*`, then you can just invoke `gw check` at project level or in `core` module, and Gradle will automatically build the required jars and files.

      # Runs all the tests (unit tests, checkstyle, etc)
      gw check
      # Runs just unit tests
      gw test
      # Runs just core tests
      gw :src:core:test

* Coverage

      # Generates code coverage report for the test task to build/reports/jacoco/test/html
      gw jacocoTestReport
      # Generate combined coverage report
      gw jacocoReport

* Generate Javadocs

      # Builds javadoc to build/docs/javadoc subfolder
      gw javadoc
      # Builds javadoc jar to build/libs/jorphan-javadoc.jar
      gw javadocJar

* Site

      # Creates preview of a site to build/docs/site
      gw :src:dist:previewSite

      # Builds and publishes site preview to a Git repository
      gw :src:dist:pushPreviewSite

* Maven

      # publishes Maven artifact to local repository
      gw publishToMavenLocal

      # generates pom file to
      # src/protocol/http/build/publications/http
      gw :src:protocol:http:generatePomFileForHttpPublication

* Release artifacts

      # Builds ZIP and TGZ artifacts for the release
      gw :src:dist:build

* Signing
It is implemented via https://docs.gradle.org/5.2.1/userguide/signing_plugin.html, so it is done automatically provided credentials are specified via https://docs.gradle.org/5.2.1/userguide/signing_plugin.html#sec:signatory_credentials

      # Signs all the artifacts of the current module (see results in build/**/*.asc
      gw sign

* Releasing
It is not yet fully implemented, however basic bits can be tested.
Use https://github.com/vlsi/asflike-release-environment to start a local "release environment" (==SVN, Nexus, etc)

      # Builds the project, pushes artifacts to svn://.../dev, stages artifacts to Nexus staging repository
      # Note: it uses https://github.com/vlsi/asflike-release-environment, so it does not alter public repositories
      gw -Prelease prepareVote

      # Publishes staged artifacts to SVN and Nexus
      gw -Prelease publishDist
