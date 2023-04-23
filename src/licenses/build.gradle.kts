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

import com.github.vlsi.gradle.license.GatherLicenseTask
import com.github.vlsi.gradle.license.api.SimpleLicense
import com.github.vlsi.gradle.license.api.SpdxLicense
import com.github.vlsi.gradle.license.api.SpdxLicenseException
import com.github.vlsi.gradle.license.api.and
import com.github.vlsi.gradle.license.api.asExpression
import com.github.vlsi.gradle.license.api.with
import com.github.vlsi.gradle.release.Apache2LicenseRenderer
import com.github.vlsi.gradle.release.ArtifactType
import com.github.vlsi.gradle.release.AsfLicenseCategory
import com.github.vlsi.gradle.release.ExtraLicense
import com.github.vlsi.gradle.release.dsl.dependencyLicenses
import com.github.vlsi.gradle.release.dsl.licensesCopySpec

plugins {
    base
    // jvm-ecosystem workarounds issue with "ambiguous variants for caffeine"
    `jvm-ecosystem`
}

// https://github.com/gradle/gradle/pull/16627
inline fun <reified T : Named> AttributeContainer.attribute(attr: Attribute<T>, value: String) =
    attribute(attr, objects.named<T>(value))

val binaryDependencies by configurations.creating {
    isCanBeConsumed = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, Category.LIBRARY)
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, LibraryElements.JAR)
        attribute(Usage.USAGE_ATTRIBUTE, Usage.JAVA_RUNTIME)
        attribute(Bundling.BUNDLING_ATTRIBUTE, Bundling.EXTERNAL)
    }
}
val binLicense by configurations.creating {
    isCanBeResolved = false
}
val srcLicense by configurations.creating {
    isCanBeResolved = false
}

dependencies {
    binaryDependencies(project(":src:dist", "runtimeElements"))
}

fun gradleWrapperVersion(wrapperProps: String) =
    `java.util`.Properties().run {
        file(wrapperProps).inputStream().buffered().use { load(it) }
        getProperty("distributionUrl").replace(Regex(".*gradle-(\\d[^-]+)-.*"), "$1")
    }

val gatherSourceLicenses by tasks.registering(GatherLicenseTask::class) {
    val wrapperProps = "$rootDir/gradle/wrapper/gradle-wrapper.properties"
    inputs.file(wrapperProps).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("wrapper.props")
    addDependency("org.gradle:gradle-wrapper:${gradleWrapperVersion(wrapperProps)}", SpdxLicense.Apache_2_0)
    addDependency(":bootstrap:3.3.4", SpdxLicense.MIT)
    addDependency(":bootstrap-social:4.8.0", SpdxLicense.MIT)
    addDependency(":datatables:1.10.9", SpdxLicense.MIT)
    addDependency(":datatables-plugins:1.0.1", SpdxLicense.MIT)
    addDependency(":datatables-responsive:1.0.5", SpdxLicense.MIT)
    addDependency(":flot:0.8.3", SpdxLicense.MIT)
    addDependency(":flot-axislabels:0.8.3", SpdxLicense.MIT)
    addDependency(":flot.tooltip:0.8.4", SpdxLicense.MIT)
    addDependency(":font-awesome-code:4.2.0", SpdxLicense.MIT)
    addDependency(":font-awesome-font:4.2.0", SpdxLicense.OFL_1_1)
    addDependency(":jquery:2.1.3", SpdxLicense.MIT)
    addDependency(":metisMenu:1.1.3", SpdxLicense.MIT)
    addDependency(":start-bootstrap-admin2:1.0.7", SpdxLicense.Apache_2_0)
    addDependency(":openiconlibrary:", SpdxLicense.CC_BY_SA_3_0)
}

val gatherBinaryLicenses by tasks.registering(GatherLicenseTask::class) {
    configuration(binaryDependencies)
    ignoreMissingLicenseFor.add(SpdxLicense.Apache_2_0.expression)
    defaultTextFor.add(SpdxLicense.MPL_2_0.expression)
    // There are three major cases here:
    // 1. License id needs to be overridden (e.g. "BSD style" -> BSD-3-Clause)
    // 2. Jar file misses LICENSE/NOTICE files, thus we need to specify local folder with relevant files (e.g. licenses/rsyntaxtextarea)
    // 3. 1 and 2

    // Note we don't use Libs.* below for artifact references to avoid accidental updates
    // Whenever you update a dependency that has "license overrides", you need to double-check
    // if license conditions still apply.
    // For instance, if "dnsjava:dnsjava:2.1.8" was written here as `Libs.dec`, then
    // license override might be silently updated as `Libs` class is edited.
    // For that purpose, most of the below declarations have expectedLicense so the task
    // would fail in case actual license does not meet expectations.
    // That enables to have "version-independent" MIT license in licenses/slf4j-api, and
    // it would be copied provided the detected license for slf4j-api is MIT.

    overrideLicense("dnsjava:dnsjava:2.1.9") {
        expectedLicense = SpdxLicense.BSD_2_Clause
    }

    overrideLicense("com.formdev:svgSalamander") {
        // See https://github.com/blackears/svgSalamander/blob/d6b6fe9a8ece7d0e0e7aeb3de82f027a38a6fe25/www/license/license-bsd.txt
        effectiveLicense = SpdxLicense.BSD_3_Clause
    }

    overrideLicense("org.swinglabs:jxlayer") {
        // See https://repo1.maven.org/maven2/org/swinglabs/jxlayer/3.0.4/jxlayer-3.0.4-sources.jar
        effectiveLicense = SpdxLicense.BSD_3_Clause
    }

    for (mig in listOf("com.miglayout:miglayout-core", "com.miglayout:miglayout-swing")) {
        overrideLicense(mig) {
            expectedLicense = SimpleLicense("BSD", uri("http://www.debian.org/misc/bsd.license"))
            effectiveLicense = SpdxLicense.BSD_3_Clause
            licenseFiles = "miglayout"
        }
    }

    for (jodd in listOf("jodd-core", "jodd-lagarto", "jodd-log", "jodd-props")) {
        overrideLicense("org.jodd:$jodd:5.0.13") {
            expectedLicense = SpdxLicense.BSD_2_Clause // SimpleLicense("The BSD 2-Clause License", uri("http://jodd.org/license.html"))
            licenseFiles = "jodd"
        }
    }

    overrideLicense("xpp3:xpp3_min:1.1.4c") {
        // pom.xml contains multiple licenses
        expectedLicense = SpdxLicense.CC0_1_0 and
            SimpleLicense(
                "Indiana University Extreme! Lab Software License, vesion 1.1.1",
                uri("http://www.extreme.indiana.edu/viewcvs/~checkout~/XPP3/java/LICENSE.txt")
            )
        effectiveLicense = SpdxLicense.CC0_1_0 and ExtraLicense.Indiana_University_1_1_1
    }

    overrideLicense("org.brotli:dec:0.1.2") {
        expectedLicense = SpdxLicense.MIT
    }

    overrideLicense("org.slf4j:slf4j-api:1.7.30") {
        expectedLicense = SpdxLicense.MIT
    }

    overrideLicense("com.sun.mail:all:1.5.0-b01") {
        // Multiple licenses, specify explicitly
        expectedLicense = SimpleLicense("CDDL", uri("http://www.sun.com/cddl")) and SimpleLicense("GPLv2+CE", uri("https://glassfish.java.net/public/CDDL+GPL_1_1.html"))
        effectiveLicense = SpdxLicense.CDDL_1_0 and (SpdxLicense.GPL_2_0_or_later with SpdxLicenseException.Classpath_exception_2_0)
    }
    overrideLicense("com.sun.activation:javax.activation:1.2.0") {
        expectedLicense = SimpleLicense("CDDL/GPLv2+CE", uri("https://github.com/javaee/activation/blob/master/LICENSE.txt"))
        effectiveLicense = SpdxLicense.CDDL_1_0
    }
    overrideLicense("xml-apis:xml-apis:1.4.01") {
        // Multiple licenses, select explicit one
        expectedLicense = SpdxLicense.Apache_2_0 and SpdxLicense.SAX_PD and SimpleLicense("The W3C License", uri("http://www.w3.org/TR/2004/REC-DOM-Level-3-Core-20040407/java-binding.zip"))
        effectiveLicense = SpdxLicense.Apache_2_0
    }
    for (lib in listOf("hamcrest-core", "hamcrest")) {
        overrideLicense("org.hamcrest:$lib:2.2") {
            // https://github.com/hamcrest/JavaHamcrest/issues/264
            // pom.xml lists "New BSD License", however it is BSD_3
            expectedLicense = SpdxLicense.BSD_3_Clause
            licenseFiles = "hamcrest"
        }
    }
    overrideLicense("net.sf.jtidy:jtidy:r938") {
        expectedLicense = SimpleLicense("Java HTML Tidy License", uri("http://jtidy.svn.sourceforge.net/viewvc/jtidy/trunk/jtidy/LICENSE.txt?revision=95"))
        effectiveLicense = SpdxLicense.BSD_3_Clause
    }
    // https://github.com/typetools/checker-framework/issues/2798
    overrideLicense("org.checkerframework:checker-qual:2.10.0") {
        expectedLicense = SpdxLicense.MIT
    }
}

val renderLicenseForSource by tasks.registering(Apache2LicenseRenderer::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Generate LICENSE file for source distribution"
    failOnIncompatibleLicense.set(false)
    artifactType.set(ArtifactType.SOURCE)
    metadata.from(gatherSourceLicenses)
}

val renderLicenseForBinary by tasks.registering(Apache2LicenseRenderer::class) {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Generate LICENSE file for binary distribution"
    failOnIncompatibleLicense.set(false)
    artifactType.set(ArtifactType.BINARY)
    metadata.from(gatherSourceLicenses)
    metadata.from(gatherBinaryLicenses)
    licenseCategory.put(ExtraLicense.Indiana_University_1_1_1.asExpression(), AsfLicenseCategory.A)
}

tasks.build.configure {
    dependsOn(renderLicenseForSource, renderLicenseForBinary)
}

// Below is to populate configurations with licenses
// Note: configuration artifacts consist of files and directories
// Here directories are used because it simplifies the use (the use site does not have to unzip)
val binLicenseSpec = licensesCopySpec(renderLicenseForBinary)
val srcLicenseSpec = licensesCopySpec(renderLicenseForSource)

val binLicenseDir by tasks.registering(Sync::class) {
    into("$buildDir/$name")
    dependencyLicenses(binLicenseSpec)
}

val srcLicenseDir by tasks.registering(Sync::class) {
    into("$buildDir/$name")
    dependencyLicenses(srcLicenseSpec)
}

artifacts {
    add(binLicense.name, buildDir.resolve(binLicenseDir.name)) {
        builtBy(binLicenseDir)
    }
    add(srcLicense.name, buildDir.resolve(srcLicenseDir.name)) {
        builtBy(srcLicenseDir)
    }
}
