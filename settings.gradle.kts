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

// This is the name of a current project
// Note: it cannot be inferred from the directory name as developer might clone JMeter to jmeter_tmp folder
rootProject.name = "jmeter"

include(
        "src:bom",
        "src:launcher",
        "src:components",
        "src:config",
        "src:core",
        "src:examples",
        "src:functions",
        "src:generator",
        "src:jorphan",
        "src:licenses",
        "src:protocol:ftp",
        "src:protocol:http",
        "src:protocol:java",
        "src:protocol:jdbc",
        "src:protocol:jms",
        "src:protocol:junit",
        "src:protocol:junit-sample",
        "src:protocol:ldap",
        "src:protocol:mail",
        "src:protocol:mongodb",
        "src:protocol:native",
        "src:protocol:tcp",
        "src:release",
        "src:dist",
        "src:dist-check")

// See https://github.com/gradle/gradle/issues/1348#issuecomment-284758705 and
// https://github.com/gradle/gradle/issues/5321#issuecomment-387561204
// Gradle inherits Ant "default excludes", however we do want to archive those files
org.apache.tools.ant.DirectoryScanner.removeDefaultExclude("**/.gitattributes")
org.apache.tools.ant.DirectoryScanner.removeDefaultExclude("**/.gitignore")

if (startParameter.projectProperties["localReleasePlugins"]
                ?.equals("false", ignoreCase = true)
                ?.not() == true
) {
        // This enables to use local clone of vlsi-release-plugins for debugging purposes
        includeBuild("../vlsi-release-plugins")
}
