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

import com.github.autostyle.gradle.AutostyleTask
import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import org.gradle.kotlin.dsl.apply
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    id("build-logic.build-params")
}

if (!buildParameters.skipAutostyle) {
    apply(plugin = "build-logic.autostyle")
}

// OpenRewrite Gradle plugin applies to allprojects when it is applied to the root project
// So the workaround is to avoid applying openrewrite to the root
val skipOpenrewrite = project == rootProject || buildParameters.skipOpenrewrite

if (!skipOpenrewrite) {
    apply(plugin = "build-logic.openrewrite")
}

val skipCheckstyle = buildParameters.skipCheckstyle || run {
    logger.info("Checkstyle requires Java 11+")
    buildParameters.buildJdkVersion < 11
}

if (!skipCheckstyle) {
    apply(plugin = "build-logic.checkstyle")
}

plugins.withId("java-base") {
    if (!buildParameters.skipForbiddenApis) {
        apply(plugin = "build-logic.forbidden-apis")
    }
    if (buildParameters.enableCheckerframework) {
        apply(plugin = "build-logic.checkerframework")
    }
    if (buildParameters.enableErrorprone) {
        apply(plugin = "build-logic.errorprone")
    }
    if (buildParameters.spotbugs) {
        apply(plugin = "build-logic.spotbugs")
    }
    if (buildParameters.coverage) {
        apply(plugin = "build-logic.jacoco")
    }
}

// Autostyle produces more meaningful error messages, so we ensure it is executed before Checkstyle
if (!skipCheckstyle && !buildParameters.skipAutostyle) {
    tasks.withType<Checkstyle>().configureEach {
        mustRunAfter("autostyleApply")
        mustRunAfter("autostyleCheck")
    }
}

if (!buildParameters.skipAutostyle || !skipCheckstyle || !buildParameters.skipForbiddenApis || !skipOpenrewrite) {
    tasks.register("style") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Formats code (license header, import order, whitespace at end of line, ...) and executes Checkstyle verifications"
        if (!buildParameters.skipAutostyle) {
            dependsOn("autostyleApply")
        }
        if (!skipOpenrewrite) {
            dependsOn("rewriteRun")
        }
        if (!skipCheckstyle) {
            dependsOn("checkstyleAll")
        }
        if (!buildParameters.skipForbiddenApis) {
            dependsOn("forbiddenApis")
        }
    }
    tasks.register("styleCheck") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Report code style violations (license header, import order, whitespace at end of line, ...)"
        if (!buildParameters.skipAutostyle) {
            dependsOn("autostyleCheck")
        }
        if (!skipOpenrewrite) {
            dependsOn("rewriteDryRun")
        }
        if (!skipCheckstyle) {
            dependsOn("checkstyleAll")
        }
        if (!buildParameters.skipForbiddenApis) {
            dependsOn("forbiddenApis")
        }
    }

}

tasks.register("checkstyle") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Executes Checkstyle verifications"
    if (!skipCheckstyle) {
        dependsOn("checkstyleAll")
    }
    if (!buildParameters.skipAutostyle) {
        dependsOn("autostyleCheck")
    }
    if (!buildParameters.skipForbiddenApis) {
        dependsOn("forbiddenApis")
    }
}

// OpenRewrite fixes many warnings, so it should run the first
if (!skipOpenrewrite) {
    if (!buildParameters.skipForbiddenApis) {
        tasks.withType<CheckForbiddenApis>().configureEach {
            mustRunAfter("rewriteRun", "rewriteDryRun")
        }
    }
    if (!buildParameters.skipCheckstyle) {
        tasks.withType<Checkstyle>().configureEach {
            mustRunAfter("rewriteRun", "rewriteDryRun")
        }
    }
    if (!buildParameters.skipAutostyle) {
        tasks.withType<AutostyleTask>().configureEach {
            mustRunAfter("rewriteRun", "rewriteDryRun")
        }
    }
}
