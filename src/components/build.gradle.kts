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

plugins {
    id("java-test-fixtures")
    id("build-logic.build-params")
    id("build-logic.jvm-published-library")
}

dependencies {
    api(projects.src.core)
    testImplementation(testFixtures(projects.src.core))

    api("org.apache-extras.beanshell:bsh") {
        because(
            """
            BeanShell is not required for JMeter, however it is commonly used in the jmx scripts.
            New scripts should refrain from using BeanShell though and migrate to Groovy or other
            faster engines
            """.trimIndent()
        )
    }

    api("com.sun.mail:javax.mail") {
        exclude("javax.activation", "activation")
    }
    // There's no javax.activation:activation:1.2.0, so we use com.sun...
    runtimeOnly("com.sun.activation:javax.activation")
    // This is an API-only jar. javax.activation is present in Java 8,
    // however it is not there in Java 9
    compileOnly("javax.activation:javax.activation-api")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("io.burt:jmespath-core")
    implementation("io.burt:jmespath-jackson")
    implementation("jcharts:jcharts")
    implementation("oro:oro")
    implementation("net.minidev:json-smart")
    implementation("net.minidev:accessors-smart")
    implementation("org.apache.commons:commons-pool2")
    implementation("commons-codec:commons-codec")
    implementation("org.ow2.asm:asm")
    implementation("org.jodd:jodd-log") {
        exclude("ch.qos.logback")
        exclude("commons-logging")
        exclude("org.apache.logging.log4j")
    }
    implementation("org.jodd:jodd-lagarto") {
        exclude("ch.qos.logback")
        exclude("commons-logging")
        exclude("org.apache.logging.log4j")
    }
    implementation("com.jayway.jsonpath:json-path")
    implementation("org.apache.httpcomponents:httpasyncclient")
    implementation("org.apache.httpcomponents:httpcore-nio")
    implementation("org.jsoup:jsoup")
    implementation("org.apache.commons:commons-lang3")
    implementation("net.sf.jtidy:jtidy")
    implementation("org.apache.commons:commons-collections4")
    implementation("org.apache.commons:commons-math3")
    implementation("commons-io:commons-io") {
        because("IOUtils")
    }
    implementation("org.apache.commons:commons-text") {
        because("StringEscapeUtils")
    }
    implementation("com.miglayout:miglayout-swing")
    // we use bcmail for compilation only, and bcmail is not shipped in the release
    compileOnly("org.bouncycastle:bcmail-jdk15on")
    compileOnly("org.bouncycastle:bcpkix-jdk15on")
    compileOnly("org.bouncycastle:bcprov-jdk15on")
    // Certain tests use bouncycastle, so it is added to test classpath
    testRuntimeOnly("org.bouncycastle:bcmail-jdk15on")
    testRuntimeOnly("org.bouncycastle:bcpkix-jdk15on")
    testRuntimeOnly("org.bouncycastle:bcprov-jdk15on")
    testImplementation("nl.jqno.equalsverifier:equalsverifier")
    testImplementation(testFixtures(projects.src.testkitWiremock))
    testFixturesImplementation(testFixtures(projects.src.core))
    testImplementation("io.mockk:mockk")
}

if (!buildParameters.enableJavaFx) {
    // JavaFX is not present in Maven Central, so exclude the file unless explicitly asked by
    // -PenableJavaFx
    logger.lifecycle("RenderInBrowser is excluded from compilation. If you want to compile it, add -PenableJavaFx")
    sourceSets {
        main {
            java {
                exclude("org/apache/jmeter/visualizers/RenderInBrowser.java")
            }
        }
    }
}
