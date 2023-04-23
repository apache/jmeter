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

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.codehaus.groovy:groovy-bom:3.0.11"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.1"))

    constraints {
        // api means "the dependency is for both compilation and runtime"
        // runtime means "the dependency is only for runtime, not for compilation"
        // In other words, marking dependency as "runtime" would avoid accidental
        // dependency on it during compilation
        // Note: if there's at least single chance for the dependency to be needed on the
        // compilation classpath (e.g. it is used as a transitive by a third-party library)
        // then it should be declared as "api" here since we use useCompileClasspathVersions
        // to make runtime classpath consistent with the compile one.
        api("org.apache.tika:tika-parsers:1.28.5")
        api("org.ow2.asm:asm:9.3")

        // activemq-all should not be used as it provides secondary slf4j binding
        api("org.apache.activemq:activemq-broker:5.16.4")
        api("org.apache.activemq:activemq-client:5.16.4")
        api("org.apache.activemq:activemq-spring:5.16.4")
        api("org.springframework:spring-context:4.3.17.RELEASE")
        api("org.springframework:spring-beans:4.3.17.RELEASE")

        api("bsf:bsf:2.4.0")
        api("cglib:cglib-nodep:3.2.12")
        api("com.fasterxml.jackson.core:jackson-annotations:2.13.4")
        api("com.fasterxml.jackson.core:jackson-core:2.13.4")
        api("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
        api("com.fifesoft:rsyntaxtextarea:3.2.0")
        api("com.formdev:svgSalamander:1.1.2.4")
        api("com.github.ben-manes.caffeine:caffeine:2.9.3")
        api("com.github.tomakehurst:wiremock-jre8:2.32.0")
        api("com.github.weisj:darklaf-core:2.7.3")
        api("com.github.weisj:darklaf-theme:2.7.3")
        api("com.github.weisj:darklaf-property-loader:2.7.3")
        api("com.github.weisj:darklaf-extensions-rsyntaxarea:0.3.4")
        api("com.helger.commons:ph-commons:10.1.6")
        api("com.helger:ph-css:6.5.0")
        api("com.jayway.jsonpath:json-path:2.7.0")
        api("com.miglayout:miglayout-core:5.3")
        api("com.miglayout:miglayout-swing:5.3")
        api("com.sun.activation:javax.activation:1.2.0")
        api("com.thoughtworks.xstream:xstream:1.4.20")
        api("commons-codec:commons-codec:1.15")
        api("commons-collections:commons-collections:3.2.2")
        api("commons-io:commons-io:2.11.0")
        api("commons-lang:commons-lang:2.6")
        api("commons-logging:commons-logging:1.2")
        api("commons-net:commons-net:3.9.0")
        api("dnsjava:dnsjava:2.1.9")
        api("io.burt:jmespath-core:0.5.1")
        api("io.burt:jmespath-jackson:0.5.1")
        api("javax.activation:javax.activation-api:1.2.0")
        api("javax.mail:mail:1.5.0-b01")
        api("jcharts:jcharts:0.7.5")
        api("junit:junit:4.13.2")
        api("org.jetbrains:annotations:23.0.0")
        api("org.jetbrains.lets-plot:lets-plot-batik:2.2.1")
        api("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:3.1.1")
        api("org.junit.jupiter:junit-jupiter:5.8.2")
        api("org.junit.jupiter:junit-jupiter-api:5.8.2")
        api("org.junit.jupiter:junit-jupiter-params:5.8.2")
        api("net.minidev:accessors-smart:2.4.8")
        api("net.minidev:json-smart:2.4.8")
        api("net.sf.jtidy:jtidy:r938")
        api("net.sf.saxon:Saxon-HE:11.3")
        api("nl.jqno.equalsverifier:equalsverifier:3.10")
        api("org.apache-extras.beanshell:bsh:2.0b6")
        api("org.apache.commons:commons-collections4:4.4")
        api("org.apache.commons:commons-dbcp2:2.9.0")
        api("org.apache.commons:commons-jexl3:3.2.1")
        api("org.apache.commons:commons-jexl:2.1.1")
        api("org.apache.commons:commons-lang3:3.12.0")
        api("org.apache.commons:commons-math3:3.6.1")
        api("org.apache.commons:commons-pool2:2.11.1")
        api("org.apache.commons:commons-text:1.10.0")
        api("org.apache.ftpserver:ftplet-api:1.2.0")
        api("org.apache.ftpserver:ftpserver-core:1.2.0")
        api("org.apache.geronimo.specs:geronimo-jms_1.1_spec:1.1.1")
        api("org.apache.httpcomponents:httpasyncclient:4.1.5")
        api("org.apache.httpcomponents:httpclient:4.5.13")
        api("org.apache.httpcomponents:httpcore-nio:4.4.15")
        api("org.apache.httpcomponents:httpcore:4.4.15")
        api("org.apache.httpcomponents:httpmime:4.5.13")
        api("org.apache.logging.log4j:log4j-1.2-api:2.17.2")
        api("org.apache.logging.log4j:log4j-api:2.17.2")
        api("org.apache.logging.log4j:log4j-core:2.17.2")
        api("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
        api("org.apache.mina:mina-core:2.1.6")
        api("org.apache.rat:apache-rat:0.13")
        api("org.apache.tika:tika-core:1.28.5")
        api("org.apache.velocity:velocity:1.7")
        api("org.apache.xmlgraphics:xmlgraphics-commons:2.7")
        api("org.apiguardian:apiguardian-api:1.1.2")
        api("org.bouncycastle:bcmail-jdk15on:1.70")
        api("org.bouncycastle:bcpkix-jdk15on:1.70")
        api("org.bouncycastle:bcprov-jdk15on:1.70")
        api("org.brotli:dec:0.1.2")
        api("org.exparity:hamcrest-date:2.0.8")
        api("org.freemarker:freemarker:2.3.31")
        api("org.hamcrest:hamcrest:2.2")
        api("org.hamcrest:hamcrest-core:2.2")
        api("org.hamcrest:hamcrest-library:2.2")
        api("org.hsqldb:hsqldb:2.5.2")
        api("org.jdom:jdom:1.1.3")
        api("org.jodd:jodd-core:5.0.13")
        api("org.jodd:jodd-lagarto:5.0.13")
        api("org.jodd:jodd-log:5.0.13")
        api("org.jodd:jodd-props:5.0.13")
        api("org.jsoup:jsoup:1.15.3")
        api("org.mongodb:mongo-java-driver:2.11.3")
        api("org.mozilla:rhino:1.7.14")
        api("org.neo4j.driver:neo4j-java-driver:4.4.6")
        api("org.objenesis:objenesis:3.2")
        api("org.slf4j:jcl-over-slf4j:1.7.36")
        api("org.slf4j:slf4j-api:1.7.36")
        api("org.spockframework:spock-core:2.2-groovy-3.0")
        api("oro:oro:2.0.8")
        api("xalan:serializer:2.7.2")
        api("xalan:xalan:2.7.2")
        api("xerces:xercesImpl:2.12.2")
        api("xml-apis:xml-apis:1.4.01")
        api("xmlpull:xmlpull:1.1.3.1")
        api("xpp3:xpp3_min:1.1.4c")
    }
}
