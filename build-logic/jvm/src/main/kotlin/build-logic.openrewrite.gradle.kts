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
    id("build-logic.openrewrite-base")
}

dependencies {
    openrewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:latest.integration"))
    openrewrite("org.openrewrite.recipe:rewrite-static-analysis")
    openrewrite("org.openrewrite.recipe:rewrite-testing-frameworks")
}

openrewrite {
    configFile = project.rootProject.file("config/openrewrite/rewrite.yml")
    failOnDryRunResults = false
    activeStyles.add("org.apache.jmeter.style.Style")
    // See config/openrewrite/rewrite.yml
    activeRecipes.add("org.apache.jmeter.staticanalysis.CodeCleanup")
    // See https://github.com/openrewrite/rewrite-static-analysis/blob/8c803a9c50b480841a4af031f60bac5ee443eb4e/src/main/resources/META-INF/rewrite/common-static-analysis.yml#L21
    activeRecipes.add("org.apache.jmeter.staticanalysis.CommonStaticAnalysis")
    plugins.withId("build-logic.test-junit5") {
        // See https://github.com/openrewrite/rewrite-testing-frameworks/blob/47ccd370247f1171fa9df005da8a9a3342d19f3f/src/main/resources/META-INF/rewrite/junit5.yml#L18C7-L18C62
        activeRecipes.add("org.openrewrite.java.testing.junit5.JUnit5BestPractices")
        // See https://github.com/openrewrite/rewrite-testing-frameworks/blob/47ccd370247f1171fa9df005da8a9a3342d19f3f/src/main/resources/META-INF/rewrite/junit5.yml#L255C7-L255C60
        activeRecipes.add("org.openrewrite.java.testing.junit5.CleanupAssertions")
    }
}

//// See https://github.com/openrewrite/rewrite-gradle-plugin/issues/255
//tasks.withType<RewriteDryRunTask>().configureEach {
//    doFirst {
//        if (reportPath.exists()) {
//            // RewriteDryRunTask keeps the report file if there are no violations, so we remove it
//            reportPath.delete()
//        }
//    }
//    doLast {
//        if (reportPath.exists()) {
//            throw GradleException(
//                "The following files have format violations. " +
//                        "Execute ./gradlew ${path.replace("Dry", "")} to apply the changes:\n" +
//                        reportPath.readText()
//            )
//        }
//    }
//}
