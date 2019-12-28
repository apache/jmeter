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

import com.github.vlsi.gradle.release.ReleaseExtension
import com.github.vlsi.gradle.release.ReleaseParams

rootProject.configure<ReleaseExtension> {
    voteText.set { it.voteTextGen() }
}

val String.prop: String? get() = System.getProperty(this)

fun ReleaseParams.voteTextGen(): String = """
The [RC NUMBER] release candidate for JMeter $version ($shortGitSha) has been
prepared, and your votes are solicited.

This release... TO BE COMPLETED

Please, test this release candidate (with load tests and/or functional
tests) using Java 8+ on Linux/Windows/macOS, especially on the changes.
Feedback is very welcome within the next 72 hours.

You can read the New and Noteworthy section with some screenshots to
illustrate improvements and full list of changes at:
$previewSiteUri/site/changes.html

JMeter is a Java desktop application designed to load test functional
behavior and measure performance. The current version targets Java 8+

Download - Archives/hashes/sigs:
$svnStagingUri
(dist revision $svnStagingRevision)

RAT report:
$previewSiteUri/rat/rat-report.txt

SHA512 hashes of archives for this vote: see footnote [1]

Site preview is here:
$previewSiteUri/site/

JavaDoc API preview is here:
$previewSiteUri/site/api

Maven staging repository is accessible here:
$nexusRepositoryUri/org/apache/$tlpUrl/

Tag:
$sourceCodeTagUrl

Keys are here:
https://www.apache.org/dist/$tlpUrl/KEYS

N.B.
To create the distribution and test $tlp: "./gradlew build -Prelease -PskipSign".

$tlp $version requires Java 8 or later to run.

The artifacts were built with
  ${"java.runtime.name".prop} ${"java.vendor".prop} (build ${"java.runtime.version".prop})
  ${"java.vm.name".prop} ${"java.vm.vendor".prop} (build ${"java.vm.version".prop}, ${"java.vm.info".prop})

Some known issues and incompatible changes are listed on changes page.
$previewSiteUri/site/changes.html#Known%20problems%20and%20workarounds


All feedback and vote are welcome.

[  ] +1  I support this release
[  ] +0  I am OK with this release
[  ] -0  OK, but....
[  ] -1  I do not support this release (please indicate why)

The vote will remain open for at least 72 hours.

The PMC members please indicate the mention "(binding)" with your vote.


Note: If the vote passes, the intention is to release the archive files
and rename the RC tag as the release tag.

Thanks in advance!

===
[1] SHA512 hashes of archives for this vote:

${artifacts.joinToString(System.lineSeparator()) { it.sha512 + System.lineSeparator() + "*" + it.name }}
""".trimIndent()
