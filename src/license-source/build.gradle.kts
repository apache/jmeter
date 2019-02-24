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

import java.io.FileOutputStream

val generateLicense by tasks.registering {
    group = "License"
    description = "Generates license file for source distribution (AL2.0 + summary for dependencies)"

    val licenseDir = File(buildDir, "reports/license")
    val rootLicense = File(rootDir, "licenses/apache2.txt")
    val jsDependenciesLicenses = File(rootDir, "licenses/license.for.third.party.dependencies.txt")
    val result = File(licenseDir, "LICENSE")

    inputs.file(rootLicense)
    inputs.file(jsDependenciesLicenses)
    outputs.file(result)

    doLast {
        rootLicense.copyTo(result, overwrite = true)
        FileOutputStream(result, /*append=*/true).use {
            it.write("\n".toByteArray())
            it.write(jsDependenciesLicenses.readBytes())
        }
    }
}
