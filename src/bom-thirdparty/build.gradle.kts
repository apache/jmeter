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
    id("build-logic.java-published-platform")
}

description = "A collection of versions of third-party libraries used by Apache JMeter"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.apache.groovy:groovy-bom:5.0.2"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.10.2"))
    api(platform("com.fasterxml.jackson:jackson-bom:2.20.1"))

    constraints {
        // api means "the dependency is for both compilation and runtime"
        // runtime means "the dependency is only for runtime, not for compilation"
        // In other words, marking dependency as "runtime" would avoid accidental
        // dependency on it during compilation
        // Note: if there's at least single chance for the dependency to be needed on the
        // compilation classpath (e.g. it is used as a transitive by a third-party library)
        // then it should be declared as "api" here since we use useCompileClasspathVersions
        // to make runtime classpath consistent with the compile one.
        api("org.ow2.asm:asm:9.9")

        api("bsf:bsf:2.4.0")
        api("cglib:cglib-nodep:3.3.0")
        api("com.fifesoft:rsyntaxtextarea:3.6.0")
        api("com.github.ben-manes.caffeine:caffeine:2.9.3")
        api("com.github.weisj:darklaf-core:3.1.1")
        api("com.github.weisj:darklaf-extensions-rsyntaxarea:0.4.1")
        api("com.github.weisj:darklaf-property-loader:3.1.1")
        api("com.github.weisj:darklaf-theme:3.1.1")
        api("com.google.auto.service:auto-service-annotations:1.1.1")
        api("com.google.auto.service:auto-service:1.1.1")
        api("com.google.errorprone:error_prone_annotations:2.43.0")
        api("com.helger.commons:ph-commons:12.0.4")
        api("com.helger:ph-css:8.0.1")
        api("com.jayway.jsonpath:json-path:2.10.0")
        api("com.miglayout:miglayout-core:5.3")
        api("com.miglayout:miglayout-swing:5.3")
        api("com.sun.activation:javax.activation:1.2.0")
        api("com.thoughtworks.xstream:xstream:1.4.21")
        api("commons-codec:commons-codec:1.19.0")
        api("commons-collections:commons-collections:3.2.2")
        api("commons-io:commons-io:2.20.0")
        api("commons-lang:commons-lang:2.6")
        api("commons-logging:commons-logging:1.3.5")
        api("commons-net:commons-net:3.12.0")
        api("dnsjava:dnsjava:3.6.3")
        api("io.burt:jmespath-core:0.6.0")
        api("io.burt:jmespath-jackson:0.6.0")
        api("javax.activation:javax.activation-api:1.2.0")
        api("javax.mail:mail:1.5.0-b01")
        api("jcharts:jcharts:0.7.5")
        api("junit:junit:4.13.2") {
            because("ApacheJMeter_junit depends on junit4")
        }
        api("org.checkerframework:checker-qual:3.51.1")
        api("org.hamcrest:hamcrest-core:3.0") {
            because("ApacheJMeter_junit depends on junit4")
        }
        api("org.hamcrest:hamcrest-library:3.0") {
            because("ApacheJMeter_junit depends on junit4")
        }
        api("org.hamcrest:hamcrest:3.0") {
            because("ApacheJMeter_junit depends on junit4")
        }
        api("net.minidev:accessors-smart:2.6.0")
        api("net.minidev:json-smart:2.6.0")
        api("net.sf.jtidy:jtidy:r938")
        api("net.sf.saxon:Saxon-HE:12.9")
        api("org.apache-extras.beanshell:bsh:2.0b6")
        api("org.apache.commons:commons-collections4:4.5.0")
        api("org.apache.commons:commons-dbcp2:2.9.0")
        api("org.apache.commons:commons-jexl3:3.2.1")
        api("org.apache.commons:commons-jexl:2.1.1")
        api("org.apache.commons:commons-lang3:3.19.0") {
            because("User might still rely on commons-lang3")
        }
        api("org.apache.commons:commons-math3:3.6.1")
        api("org.apache.commons:commons-pool2:2.12.1")
        api("org.apache.commons:commons-text:1.14.0")
        api("org.apache.geronimo.specs:geronimo-jms_1.1_spec:1.1.1")
        api("org.apache.httpcomponents.client5:httpclient5:5.5.1")
        api("org.apache.httpcomponents:httpasyncclient:4.1.5")
        api("org.apache.httpcomponents:httpclient:4.5.14")
        api("org.apache.httpcomponents:httpcore-nio:4.4.16")
        api("org.apache.httpcomponents:httpcore:4.4.16")
        api("org.apache.httpcomponents:httpmime:4.5.14")
        api("org.apache.logging.log4j:log4j-1.2-api:2.25.2")
        api("org.apache.logging.log4j:log4j-api:2.25.2")
        api("org.apache.logging.log4j:log4j-core:2.25.2")
        api("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.2")
        api("org.apache.rat:apache-rat:0.17")
        api("org.apache.tika:tika-core:3.2.3")
        api("org.apache.tika:tika-parsers:3.2.3")
        api("org.apache.velocity:velocity:1.7")
        api("org.apache.xmlgraphics:xmlgraphics-commons:2.11")
        api("org.apiguardian:apiguardian-api:1.1.2")
        api("org.bouncycastle:bcmail-jdk18on:1.82")
        api("org.bouncycastle:bcpkix-jdk18on:1.82")
        api("org.bouncycastle:bcprov-jdk18on:1.82")
        api("org.brotli:dec:0.1.2")
        api("org.freemarker:freemarker:2.3.34")
        api("org.jdom:jdom:1.1.3")
        api("org.jetbrains.lets-plot:lets-plot-batik:4.7.3")
        api("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.11.2")
        api("org.jetbrains:annotations:26.0.2-1")
        api("org.jodd:jodd-core:5.3.0")
        api("org.jodd:jodd-lagarto:6.0.6")
        api("org.jodd:jodd-log:5.1.6")
        api("org.jodd:jodd-props:6.0.2")
        api("org.jsoup:jsoup:1.21.2")
        api("org.mozilla:rhino:1.8.0")
        api("org.neo4j.driver:neo4j-java-driver:6.0.1")
        api("org.slf4j:jcl-over-slf4j:1.7.36")
        api("org.slf4j:slf4j-api:1.7.36")
        api("oro:oro:2.0.8")
        api("xalan:serializer:2.7.3")
        api("xalan:xalan:2.7.3")
        api("xml-apis:xml-apis:1.4.01")
        api("xmlpull:xmlpull:1.1.3.1")
    }
}
