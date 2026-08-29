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
import org.gradle.kotlin.dsl.apply
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    id("build-logic.build-params")
}

if (!buildParameters.skipAutostyle) {
    apply(plugin = "build-logic.autostyle")
}

// The OpenRewrite Gradle plugin applies its recipes to allprojects when it is
// applied to the root project, so the workaround is to avoid applying it to the root.
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
    if (buildParameters.enableErrorprone) {
        apply(plugin = "build-logic.errorprone")
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

// OpenRewrite fixes many warnings, so it should run before the other verifications
if (!skipOpenrewrite) {
    if (!buildParameters.skipAutostyle) {
        tasks.withType<AutostyleTask>().configureEach {
            mustRunAfter("rewriteRun", "rewriteDryRun")
        }
    }
    if (!skipCheckstyle) {
        tasks.withType<Checkstyle>().configureEach {
            mustRunAfter("rewriteRun", "rewriteDryRun")
        }
    }
}

if (!buildParameters.skipAutostyle || !skipCheckstyle || !buildParameters.skipForbiddenApis || !skipOpenrewrite) {
    tasks.register("style") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Formats code (license header, import order, whitespace at end of line, ...) and executes Checkstyle verifications"
        if (!skipOpenrewrite) {
            dependsOn("rewriteRun")
        }
        if (!buildParameters.skipAutostyle) {
            dependsOn("autostyleApply")
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
        description = "Reports code style violations (license header, import order, whitespace at end of line, ...)"
        if (!skipOpenrewrite) {
            dependsOn("rewriteDryRun")
        }
        if (!buildParameters.skipAutostyle) {
            dependsOn("autostyleCheck")
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
