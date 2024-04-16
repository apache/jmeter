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

import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.kotlin.dsl.withType
import java.io.File

plugins {
    id("checkstyle")
}

configurations.checkstyle {
    // See https://github.com/gradle/gradle/issues/27035#issuecomment-1814997295
    // TODO: remove the workaround as https://github.com/checkstyle/checkstyle/issues/14123 is resolved
    resolutionStrategy.capabilitiesResolution.withCapability("com.google.collections:google-collections") {
        select("com.google.guava:guava:0")
    }
}

checkstyle {
    // TOOD: move to /config
    val configDir = File(rootDir, "config/checkstyle")

    toolVersion = "10.12.6"
    configProperties = mapOf(
        "cache_file" to layout.buildDirectory.dir("checkstyle/cacheFile").get().asFile.relativeTo(configDir)
    )

    providers.gradleProperty("checkstyle.version")
        .takeIf { it.isPresent }
        ?.let { toolVersion = it.get() }

    isShowViolations = true

    configDirectory.set(configDir)
    configFile = configDir.resolve("checkstyle.xml")
}

val checkstyleTasks = tasks.withType<Checkstyle>()
checkstyleTasks.configureEach {
    // Checkstyle 8.26 does not need classpath, see https://github.com/gradle/gradle/issues/14227
    classpath = files()
}

tasks.register("checkstyleAll") {
    dependsOn(checkstyleTasks)
}
