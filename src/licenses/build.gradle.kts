/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.github.vlsi.gradle.license.GatherLicenseTask
import com.github.vlsi.gradle.license.api.*
import com.github.vlsi.gradle.release.Apache2LicenseRenderer
import com.github.vlsi.gradle.release.ArtifactType
import com.github.vlsi.gradle.release.AsfLicenseCategory
import com.github.vlsi.gradle.release.ExtraLicense

plugins {
    id("com.github.vlsi.stage-vote-release")
}

// See https://docs.gradle.org/current/userguide/troubleshooting_dependency_resolution.html#sub:configuration_resolution_constraints
// Gradle forbids to resolve configurations from other projects, so
// we create our own copy of the configuration which belongs to the current project
// This is the official recommendation:
// In most cases, the deprecation warning can be fixed by defining a configuration in
// the project where the resolution is occurring and setting it to extend from the configuration
// in the other project.
val binaryDependencies by configurations.creating() {
    extendsFrom(project(":src:dist").configurations.runtimeClasspath.get())
}

val gatherSourceLicenses by tasks.registering(GatherLicenseTask::class) {
    addDependency("org.gradle:gradle-wrapper:5.5.1", SpdxLicense.Apache_2_0)
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
    ignoreMissingLicenseFor.add(SpdxLicense.Apache_2_0.asExpression())
    defaultTextFor.add(SpdxLicense.MPL_2_0.asExpression())
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

    // Library is not present in Maven Central
    overrideLicense("com.github.bulenkov.darcula:darcula:e208efb96f70e4be9dc362fbb46f6e181ef501dd", SpdxLicense.Apache_2_0)

    overrideLicense("dnsjava:dnsjava:2.1.9") {
        expectedLicense = SimpleLicense("BSD", uri("https://github.com/dnsjava/dnsjava/blob/master/LICENSE"))
        effectiveLicense = SpdxLicense.BSD_2_Clause
    }

    overrideLicense("com.fifesoft:rsyntaxtextarea:3.0.4") {
        // https://github.com/bobbylight/RSyntaxTextArea/issues/299
        expectedLicense = SimpleLicense(
            "Modified BSD License",
            uri("https://github.com/bobbylight/RSyntaxTextArea/blob/master/RSyntaxTextArea/src/main/resources/META-INF/LICENSE")
        )
        effectiveLicense = SpdxLicense.BSD_3_Clause
    }

    overrideLicense("com.thoughtworks.xstream:xstream:1.4.11") {
        expectedLicense = SimpleLicense("BSD style", uri("http://x-stream.github.io/license.html"))
        // https://github.com/x-stream/xstream/issues/151
        // https://github.com/x-stream/xstream/issues/153
        effectiveLicense = SpdxLicense.BSD_3_Clause
    }

    overrideLicense("org.ow2.asm:asm:7.1") {
        // pom.xml lists license as BSD
        expectedLicense = SimpleLicense("BSD", uri("http://asm.ow2.org/license.html"))
        effectiveLicense = SpdxLicense.BSD_3_Clause
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

    overrideLicense("org.jsoup:jsoup:1.12.1") {
        expectedLicense = SimpleLicense("MIT", uri("https://github.com/jhy/jsoup/blob/master/LICENSE"))
        effectiveLicense = SpdxLicense.MIT
    }

    overrideLicense("org.slf4j:jcl-over-slf4j:1.7.25") {
        expectedLicense = SpdxLicense.MIT
        // See https://github.com/qos-ch/slf4j/blob/v_1.7.25/jcl-over-slf4j/LICENSE.txt
        effectiveLicense = SpdxLicense.Apache_2_0
    }

    overrideLicense("org.slf4j:slf4j-api:1.7.25") {
        expectedLicense = SpdxLicense.MIT
    }

    overrideLicense("net.sf.saxon:Saxon-HE:9.9.1-1") {
        expectedLicense = SpdxLicense.MPL_2_0
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
    overrideLicense("org.hamcrest:hamcrest-core:1.3") {
        // https://github.com/hamcrest/JavaHamcrest/issues/264
        // pom.xml lists "New BSD License", however it is BSD_3
        expectedLicense = SimpleLicense("New BSD License", uri("http://www.opensource.org/licenses/bsd-license.php"))
        effectiveLicense = SpdxLicense.BSD_3_Clause
    }
    overrideLicense("org.exparity:hamcrest-date:2.0.4") {
        // https://github.com/eXparity/hamcrest-date/issues/26
        // pom.xml lists "New BSD License", however it is BSD_3
        expectedLicense = SimpleLicense("New BSD License", uri("http://www.opensource.org/licenses/bsd-license.php"))
        effectiveLicense = SpdxLicense.BSD_3_Clause
    }
    overrideLicense("net.sf.jtidy:jtidy:r938") {
        expectedLicense = SimpleLicense("Java HTML Tidy License", uri("http://jtidy.svn.sourceforge.net/viewvc/jtidy/trunk/jtidy/LICENSE.txt?revision=95"))
        effectiveLicense = SpdxLicense.BSD_3_Clause
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
