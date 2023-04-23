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

import jacoco.DefaultJacocoAggregateReportSpec
import jacoco.JacocoAggregateReportSpec

plugins {
    `reporting-base`
    `jvm-ecosystem`
    jacoco
}

val jacocoAggregation by configurations.creating {
    description = "Collects project dependencies for JaCoCo coverage report aggregation"
    isVisible = false
    isTransitive = true
    isCanBeResolved = false
    isCanBeConsumed = false
}

val jacocoAggregationCodeCoverageReportResults by configurations.creating {
    description = "Actual dependencies for JaCoCo coverage aggregation"
    isCanBeConsumed = false
    isCanBeResolved = true
    extendsFrom(jacocoAggregation)
}

// https://github.com/gradle/gradle/pull/16627
inline fun <reified T : Named> AttributeContainer.attribute(attr: Attribute<T>, value: String) =
    attribute(attr, objects.named<T>(value))

var allSourceDirectories = jacocoAggregationCodeCoverageReportResults.incoming.artifactView {
    withVariantReselection()
    componentFilter { it is ProjectComponentIdentifier }
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, Category.VERIFICATION)
        attribute(Bundling.BUNDLING_ATTRIBUTE, Bundling.EXTERNAL)
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, VerificationType.MAIN_SOURCES)
    }
}

var allClassDirectories = jacocoAggregationCodeCoverageReportResults.incoming.artifactView {
    withVariantReselection()
    componentFilter { it is ProjectComponentIdentifier }
    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, LibraryElements.CLASSES)
    }
}

reporting {
    reports.registerBinding(JacocoAggregateReportSpec::class, DefaultJacocoAggregateReportSpec::class)
    reports.withType<JacocoAggregateReportSpec>().configureEach {
        reportTask.configure {
            val execData = jacocoAggregationCodeCoverageReportResults.incoming.artifactView {
                withVariantReselection()
                componentFilter { it is ProjectComponentIdentifier }
                attributes {
                    attribute(Category.CATEGORY_ATTRIBUTE, Category.VERIFICATION)
                    attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, VerificationType.JACOCO_RESULTS)
                    attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.BINARY_DATA_TYPE)
                }
            }
            executionData.from(
                execData.files
            )
            classDirectories.from(allClassDirectories.files)
            sourceDirectories.from(allSourceDirectories.files)
        }
    }
    reports.create<JacocoAggregateReportSpec>(JacocoAggregateReportSpec.CODE_COVERAGE_REPORT_NAME)
}
