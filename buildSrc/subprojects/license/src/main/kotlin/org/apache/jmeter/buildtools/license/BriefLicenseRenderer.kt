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

package org.apache.jmeter.buildtools.license

import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import org.gradle.api.GradleException
import java.io.File
import java.util.*

/**
 * This class converts license analysis of com.github.jk1.license to a LICENSE-compatible format
 */
class BriefLicenseRenderer(
    private val summaryLicenseFile: File, private val overrides: Map<String, String> = mapOf(),
    private val extra: Array<ModuleLicense>
) : ReportRenderer {
    companion object {
        const val APACHE_LICENCE_2 = "Apache License, Version 2.0"
    }

    enum class LicenseGroup {
        UNCLEAR, ASF_AL, ASF_OTHER, AL, OTHER
    }

    class ModuleLicense(
        val group: String,
        val name: String,
        val version: String,
        val license: String
    ) {
        val ref = "$group:$name:$version"
    }

    private val asfGroups = setOf(
        "org.codehaus.groovy",
        "oro",
        "xalan",
        "xerces"
    )

    private val approvedLicenses = setOf(
        APACHE_LICENCE_2,
        "Apache Software License, Version 1.1",
        "BSD style",
        "Bouncy Castle Licence",
        "CDDL Version 1.0",
        "CDDL Version 1.1",
        "Eclipse Public License - v 1.0",
        "Indiana University Extreme! Lab Software License 1.1.1",
        "Java HTML Tidy License",
        "MIT License",
        "Modified BSD License",
        "Mozilla Public License Version 2.0",
        "Mozilla Public License, Version 2.0",
        "New BSD License",
        "Public Domain",
        "PUBLIC DOMAIN",
        "The Apache Software License, Version 1.1",
        "The 2-Clause BSD License",
        "The BSD 2-Clause License",
        "The MIT License"
    )

    fun ModuleData.findLicense(): ModuleLicense {
        val licensesInFiles =
            licenseFiles
                .asSequence()
                .flatMap { it.fileDetails.asSequence() }
                .filter { it.license != null }
                .map { it.license }
                .toSet()

        val licensesInManifest =
            manifests
                .asSequence()
                .filter { it.hasPackagedLicense }
                .map { it.license }
                .toSet()

        val licensesInPom =
            poms
                .asSequence()
                .flatMap { it.licenses.asSequence() }
                .map { it.name }
                .toSet()

        val license =
            overrides.getOrElse("$group:$name:$version") {
                val allLicenses = licensesInFiles.plus(licensesInManifest).plus(licensesInPom)
                when (allLicenses.size) {
                    0 -> "? Unknown license ?"
                    1 -> allLicenses.first()
                    else -> "Unclear license. " +
                            mapOf(
                                "License files" to licensesInFiles,
                                "License manifests" to licensesInManifest,
                                "License in pom.xml" to licensesInPom
                            )
                                .entries
                                .filter { it.value.isNotEmpty() }
                                .joinToString(". ") { it.key + ": " + it.value.joinToString("; ") }
                }
            }
        return ModuleLicense(
            group = group,
            name = name,
            version = version,
            license = license
        )
    }

    override fun render(data: ProjectData?) {
        if (data == null) {
            throw GradleException("License data is missing")
        }

        val allLicenses =
            data.allDependencies
                .asSequence()
                .map { it.findLicense() }
                .plus(extra)
                .sortedBy { it.ref }

        summaryLicenseFile.parentFile.mkdirs()
        summaryLicenseFile.bufferedWriter().use { out ->
            allLicenses
                .groupByTo(TreeMap()) {
                    when {
                        it.license.startsWith("Unclear") -> LicenseGroup.UNCLEAR
                        it.group.startsWith("org.apache") or (it.group in asfGroups) or
                                ((it.group == it.name) and (it.group.startsWith("commons-"))) ->
                            if (it.license == APACHE_LICENCE_2)
                                LicenseGroup.ASF_AL
                            else
                                LicenseGroup.ASF_OTHER
                        it.license == APACHE_LICENCE_2 -> LicenseGroup.AL
                        else -> LicenseGroup.OTHER
                    }
                }.forEach {
                    val licenseGroup = it.key
                    out.appendln(
                        when (licenseGroup) {
                            LicenseGroup.UNCLEAR -> "- Software with unclear license. Please analyze the license and specify manually"
                            LicenseGroup.ASF_AL -> "- Software produced at the ASF which is available under AL 2.0 (as above)"
                            LicenseGroup.ASF_OTHER -> "- Software produced at the ASF which is available under other licenses (not AL 2.0)"
                            LicenseGroup.AL -> "- Software produced outside the ASF which is available under AL 2.0 (as above)"
                            LicenseGroup.OTHER -> "- Software produced outside the ASF which is available under other licenses (not AL 2.0)"
                        }
                    )
                    it.value
                        .groupByTo(TreeMap()) { it.license }
                        .forEach {
                            out.appendln()
                            if (it.key != APACHE_LICENCE_2) {
                                out.appendln(it.key)
                            }
                            it.value.forEach { out.appendln("* ${it.ref}") }
                        }

                    if (licenseGroup != LicenseGroup.OTHER) {
                        out.newLine()
                    }
                }
        }

        // This is placed after license file creation to enable review the result even though unapproved licenses detected
        val badLicenses = allLicenses
            .filter { it.license !in approvedLicenses }
            .groupByTo(TreeMap(), { it.license }, { it.ref })
        if (!badLicenses.isEmpty()) {
            throw GradleException(
                "The following unapproved licenses were found in dependencies: ${badLicenses.keys}.\n" +
                        "Dependencies: ${badLicenses.entries.joinToString(",\n\n") { e ->
                            e.value.joinToString(",\n") { "    \"$it\" to \"${e.key}\"" }
                        }}"
            )
        }
    }
}
