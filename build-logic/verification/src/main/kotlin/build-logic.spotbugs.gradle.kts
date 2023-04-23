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

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.SpotBugsTask
import com.github.vlsi.gradle.dsl.configureEach
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    id("com.github.spotbugs")
    id("build-logic.build-params")
}

spotbugs {
    // Below statement is for Renovate Bot since it does not support toolVersion.set("..") pattern yet
    val toolVersion = "4.7.3"
    this.toolVersion.set(toolVersion)

    providers.gradleProperty("spotbugs.version")
        .takeIf { it.isPresent }
        ?.let { this.toolVersion.set(it) }
    reportLevel.set(Confidence.HIGH)
    ignoreFailures.set(buildParameters.ignoreSpotBugsFailures)
}

dependencies {
    constraints {
        providers.gradleProperty("asm.version")
            .takeIf { it.isPresent }
            ?.let {
                val asmVersion = it.get()
                spotbugs("org.ow2.asm:asm:$asmVersion")
                spotbugs("org.ow2.asm:asm-all:$asmVersion")
                spotbugs("org.ow2.asm:asm-analysis:$asmVersion")
                spotbugs("org.ow2.asm:asm-commons:$asmVersion")
                spotbugs("org.ow2.asm:asm-tree:$asmVersion")
                spotbugs("org.ow2.asm:asm-util:$asmVersion")
            }
    }
}

fun reportsForHumans() = System.getenv()["CI"]?.toBoolean() != true

tasks.configureEach<SpotBugsTask> {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    if (!buildParameters.spotbugs) {
        description = "$description (skipped by default, to enable it add -Pspotbugs)"
    }
    reports {
        // xml goes for SonarQube, so we always create it just in case
        create("xml")
        if (reportsForHumans()) {
            create("html")
        }
    }
    enabled = buildParameters.spotbugs
}
