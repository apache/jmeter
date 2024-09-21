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

import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings

plugins {
    id("build-logic.jvm-published-library")
}

val srcLicense by configurations.creating

dependencies {
    srcLicense(project(":src:licenses", "srcLicense"))
}

tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
    into("META-INF") {
        // License file should include licenses for bundled third-party components
        CrLfSpec(LineEndings.LF).run {
            // Note: license content is taken from "/build/..", so gitignore should not be used
            // Note: this is a "license + third-party licenses", not just Apache-2.0
            from(files(srcLicense))
        }
    }
    into("bin") {
        filteringCharset = "UTF-8"
        CrLfSpec(LineEndings.LF).run {
            textFrom("$rootDir/bin") {
                text("*.bshrc")
                text("*.groovy")
                text("*.parameters")
                text("*.properties")
                text("*.groovy")
                text("jaas.conf")
                text("krb5.conf")
                text("log4j2.xml")
                binary("proxyserver.jks")
                text("users.dtd")
                text("users.xml")
            }
            into("templates") {
                textFrom("$rootDir/bin/templates") {
                    text("templates.dtd")
                    text("templates.xml")
                    text("*.jmx")
                }
            }
            into("report-template") {
                textFrom("$rootDir/bin/report-template") {
                    text("**/*") // all except binary
                    binary("**/*.png", "**/*.ttf", "**/*.woff", "**/*.woff2", "**/*.eot", "**/*.otf")
                }
            }
        }
    }
}
