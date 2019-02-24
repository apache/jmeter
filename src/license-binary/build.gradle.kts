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

import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.filter.LicenseBundleNormalizer
import org.apache.jmeter.buildtools.license.BriefLicenseRenderer
import java.io.FileOutputStream

plugins {
    jmeterbuild.license
}

val licenseDir = File(buildDir, "reports/license")
val summaryLicenseFile = File(licenseDir, "licenseSummary")

licenseReport {
    projects = arrayOf(project(":src:dist"))
    configurations = arrayOf("runtime")
    renderers = arrayOf(BriefLicenseRenderer(
        summaryLicenseFile = summaryLicenseFile,
        // This is in line with manually-mentioned licenses in previous $rootDir/LICENSE file
        overrides = mapOf(
            "org.ow2.asm:asm:7.0" to BriefLicenseRenderer.APACHE_LICENCE_2,
            "org.apache.commons:commons-pool2:2.6.0" to BriefLicenseRenderer.APACHE_LICENCE_2,
            "org.codehaus.groovy:groovy-all:2.4.16" to BriefLicenseRenderer.APACHE_LICENCE_2,
            "xml-apis:xml-apis:1.4.01" to BriefLicenseRenderer.APACHE_LICENCE_2,
            "javax.activation:javax.activation-api:1.2.0" to "CDDL Version 1.1",
            "javax.mail:mail:1.5.0-b01" to "CDDL Version 1.0",
            "javax.activation:activation:1.1" to "CDDL Version 1.1",
            "xpp3:xpp3_min:1.1.4c" to "Indiana University Extreme! Lab Software License 1.1.1"
        ),
        extra = arrayOf(
            BriefLicenseRenderer.ModuleLicense(
                "com.github.bulenkov.darcula",
                "darcula",
                "e208efb96f70e4be9dc362fbb46f6e181ef501dd",
                BriefLicenseRenderer.APACHE_LICENCE_2
            )
        )
    ))
    filters = arrayOf(LicenseBundleNormalizer())
    // There's no pom file for bulenkov:darcula, so it is excluded here to avoid exceptions during build
    // The dependency is added to BriefLicenseRenderer.extra above
    excludes = arrayOf("com.github.bulenkov.darcula:darcula")
}

tasks.named("generateLicenseReport") {
    // generateLicenseReport must be re-executed if summary file is corrupted/removed
    outputs.file(summaryLicenseFile)

    // See https://github.com/jk1/Gradle-License-Report/issues/141
    // We add explicit input dependency on dist.runtime, so the task is re-executed when dependencies change
    inputs.files(project(":src:dist").configurations["runtime"])

    doFirst {
        // Avoid leak of stale dependnecies
        delete(project.the<LicenseReportExtension>().outputDir)
    }
}

val generateLicense by tasks.registering {
    group = "License"
    description = "Generates license file for binary distribution (AL2.0 + summary for dependencies)"

    val rootLicense = File(rootDir, "licenses/apache2.txt")
    val jsDependenciesLicenses = File(rootDir, "licenses/license.for.third.party.dependencies.txt")
    val result = File(licenseDir, "LICENSE")

    dependsOn(tasks.named("generateLicenseReport"))
    inputs.file(rootLicense)
    inputs.file(summaryLicenseFile)
    inputs.file(jsDependenciesLicenses)
    outputs.file(result)

    doLast {
        rootLicense.copyTo(result, overwrite = true)
        FileOutputStream(result, /*append=*/true).use {
            it.run {
                write("\n".toByteArray())
                write(jsDependenciesLicenses.readBytes())
                write(
                    """

                        Binary distributions additionally contain software included under various licenses,
                        see below.


                        """.trimIndent().toByteArray()
                )
                write(summaryLicenseFile.readBytes())
            }
        }
    }
}
