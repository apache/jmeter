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
    id("java-base")
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.8"
    providers.gradleProperty("jacoco.version")
        .takeIf { it.isPresent }
        ?.let { toolVersion = it.get() }
}

val testTasks = tasks.withType<Test>()
val javaExecTasks = tasks.withType<JavaExec>()
    .matching { it.name != "runGui" }

// This configuration must be postponed since JacocoTaskExtension might be added inside
// configure block of a task (== before this code is run)
afterEvaluate {
    for (t in arrayOf(testTasks, javaExecTasks)) {
        t.configureEach {
            extensions.findByType<JacocoTaskExtension>()?.apply {
                // We don't want to collect coverage for third-party classes
                includes?.add("org.apache.jmeter.*")
                includes?.add("org.apache.jorphan.*")
                includes?.add("org.apache.commons.cli.*")
            }
        }
    }
}

val jacocoReport by rootProject.tasks.existing(JacocoReport::class)
val mainCode = sourceSets["main"]

// TODO: rework with provide-consume configurations
jacocoReport {
    // Note: this creates a lazy collection
    // Some projects might fail to create a file (e.g. no tests or no coverage),
    // So we check for file existence. Otherwise, JacocoMerge would fail
    val execFiles =
        files(testTasks, javaExecTasks).filter { it.exists() && it.name.endsWith(".exec") }
    executionData(execFiles)
    additionalSourceDirs.from(mainCode.allJava.srcDirs)
    sourceDirectories.from(mainCode.allSource.srcDirs)
    classDirectories.from(mainCode.output)
}

// TODO: check which reports do we need
// tasks.configureEach<JacocoReport> {
//    reports {
//        html.required.set(reportsForHumans())
//        xml.required.set(!reportsForHumans())
//    }
// }
