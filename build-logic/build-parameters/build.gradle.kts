/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("org.gradlex.build-parameters") version "1.4.3"
    id("com.github.vlsi.gradle-extensions") version "1.89"
    id("build-logic.kotlin-dsl-gradle-plugin")
}

buildParameters {
    // Other plugins can contribute parameters, so below list is not exhaustive
    enableValidation.set(false)
    pluginId("build-logic.build-params")
    bool("enableMavenLocal") {
        defaultValue.set(true)
        description.set("Add mavenLocal() to repositories")
    }
    bool("coverage") {
        defaultValue.set(false)
        description.set("Collect test coverage")
    }
    bool("spotbugs") {
        defaultValue.set(false)
        description.set("Run SpotBugs verifications")
    }
    bool("sonarqube") {
        defaultValue.set(false)
        description.set("Report verification results to Sonarqube")
    }
    bool("ignoreSpotBugsFailures") {
        defaultValue.set(false)
        description.set("Ignore SpotBugs failures")
    }
    bool("enableCheckerframework") {
        defaultValue.set(false)
        description.set("Run CheckerFramework (nullness) verifications")
    }
    bool("skipAutostyle") {
        defaultValue.set(false)
        description.set("Skip AutoStyle verifications")
    }
    bool("skipCheckstyle") {
        defaultValue.set(false)
        description.set("Skip Checkstyle verifications")
    }
    bool("skipDist") {
        defaultValue.set(false)
        description.set("Allow to skip building source/binary distributions")
    }
    bool("skipForbiddenApis") {
        defaultValue.set(true)
        description.set("Skip forbidden-apis verifications")
    }
    bool("suppressPomMetadataWarnings") {
        defaultValue.set(true)
        description.set("Skip suppressPomMetadataWarningsFor warnings triggered by inability to map test fixtures dependences to Maven pom.xml")
    }
    bool("enableErrorprone") {
        // By default, disable errorProne in CI so we don't perform the same checks in several jobs
        defaultValue.set(System.getenv("CI") != "true")
        description.set("Enable ErrorProne verifications")
    }
    bool("skipJavadoc") {
        defaultValue.set(false)
        description.set("Skip javadoc generation")
    }
    bool("failOnJavadocWarning") {
        defaultValue.set(true)
        description.set("Fail build on javadoc warnings")
    }
    bool("enableGradleMetadata") {
        defaultValue.set(false)
        description.set("Generate and publish Gradle Module Metadata")
    }
    bool("useGpgCmd") {
        defaultValue.set(false)
        description.set("By default use Java implementation to sign artifacts. When useGpgCmd=true, then gpg command line tool is used for signing artifacts")
    }
    bool("werror") {
        defaultValue.set(true)
        description.set("Treat javac, javadoc, kotlinc warnings as errors")
    }
}
