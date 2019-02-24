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

import versions.*

dependencies {
    compile(project(":src:core"))
    testCompile(project(":src:core", "testClasses"))

    compile(Libs.jcharts)
    compile(Libs.json_smart)
    compile(Libs.accessors_smart)
    compile(Libs.commons_pool2)
    compile(Libs.commons_codec)
    compile(Libs.jodd_log)
    compile(Libs.jodd_lagarto)
    compile(Libs.json_path)
    compile(Libs.httpasyncclient)
    compile(Libs.httpcore_nio)
    compile(Libs.jsoup)
    compile(Libs.bcmail)
    compile(Libs.bcpkix)
    compile(Libs.bcprov)
    compile(Libs.bsh) {
        because("""
            BeanShell is not required for JMeter, however it is commonly used in the jmx scripts.
            New scripts should refrain from using BeanShell though and migrate to Groovy or other
            faster engines
        """.trimIndent())
    }

    compile(Libs.mail)
    compile(Libs.javax_activation_api)
}

if (JavaVersion.current() >= JavaVersion.VERSION_11) {
    // JavaFX is not present in Java11, so just skip compilation of the visualizer
    sourceSets {
        main {
            java {
                exclude("org/apache/jmeter/visualizers/RenderInBrowser.java")
            }
        }
    }
}
