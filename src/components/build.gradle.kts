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

dependencies {
    api(project(":src:core"))
    testCompile(project(":src:core", "testClasses"))

    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("jcharts:jcharts")
    implementation("oro:oro")
    implementation("net.minidev:json-smart")
    implementation("net.minidev:accessors-smart")
    implementation("org.apache.commons:commons-pool2")
    implementation("commons-codec:commons-codec")
    implementation("org.jodd:jodd-log")
    implementation("org.jodd:jodd-lagarto")
    implementation("com.jayway.jsonpath:json-path")
    implementation("org.apache.httpcomponents:httpasyncclient")
    implementation("org.apache.httpcomponents:httpcore-nio")
    implementation("org.jsoup:jsoup")
    implementation("org.apache.commons:commons-lang3")
    implementation("net.sf.jtidy:jtidy")
    implementation("commons-collections:commons-collections")
    implementation("org.apache.commons:commons-math3")
    implementation("commons-io:commons-io") {
        because("IOUtils")
    }
    implementation("org.apache.commons:commons-text") {
        because("StringEscapeUtils")
    }
    // we use bcmail for compilation only, and bcmail is not shipped in the release
    compileOnly("org.bouncycastle:bcmail-jdk15on")
    compileOnly("org.bouncycastle:bcpkix-jdk15on")
    compileOnly("org.bouncycastle:bcprov-jdk15on")
    // Certain tests use bouncycastle, so it is added to test classpath
    testRuntimeOnly("org.bouncycastle:bcmail-jdk15on")
    testRuntimeOnly("org.bouncycastle:bcpkix-jdk15on")
    testRuntimeOnly("org.bouncycastle:bcprov-jdk15on")
    api("org.apache-extras.beanshell:bsh:2.0b6") {
        because("""
            BeanShell is not required for JMeter, however it is commonly used in the jmx scripts.
            New scripts should refrain from using BeanShell though and migrate to Groovy or other
            faster engines
        """.trimIndent())
    }

    api("javax.mail:mail")
    api("javax.activation:javax.activation-api")
}

fun String?.toBool(nullAs: Boolean, blankAs: Boolean, default: Boolean) =
    when {
        this == null -> nullAs
        isBlank() -> blankAs
        default -> !equals("false", ignoreCase = true)
        else -> equals("true", ignoreCase = true)
    }

if ((project.findProperty("enableJavaFx") as? String)
        .toBool(nullAs = true, blankAs = true, default = false)
) {
    // JavaFX is not present in Maven Central, so exclude the file unless explicitly asked by
    // -PenableJavaFx
    logger.debug("RenderInBrowser is excluded from compilation. If you want to compile it, add -PenableJavaFx")
    sourceSets {
        main {
            java {
                exclude("org/apache/jmeter/visualizers/RenderInBrowser.java")
            }
        }
    }
}
