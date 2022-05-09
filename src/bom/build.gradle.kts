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
    `java-platform`
}

val String.v: String get() = rootProject.extra["$this.version"] as String

// Note: Gradle allows to declare dependency on "bom" as "api",
// and it makes the contraints to be transitively visible
// However Maven can't express that, so the approach is to use Gradle resolution
// and generate pom files with resolved versions
// See https://github.com/gradle/gradle/issues/9866

fun DependencyConstraintHandlerScope.apiv(
    notation: String,
    versionProp: String = notation.substringAfterLast(':')
) =
    "api"(notation + ":" + versionProp.v)

fun DependencyConstraintHandlerScope.runtimev(
    notation: String,
    versionProp: String = notation.substringAfterLast(':')
) =
    "runtime"(notation + ":" + versionProp.v)

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.codehaus.groovy:groovy-bom:${"groovy".v}"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:${"kotlinx-coroutines".v}"))

    // Parenthesis are needed here: https://github.com/gradle/gradle/issues/9248
    (constraints) {
        // api means "the dependency is for both compilation and runtime"
        // runtime means "the dependency is only for runtime, not for compilation"
        // In other words, marking dependency as "runtime" would avoid accidental
        // dependency on it during compilation
        // Note: if there's at least single chance for the dependency to be needed on the
        // compilation classpath (e.g. it is used as a transitive by a third-party library)
        // then it should be declared as "api" here since we use useCompileClasspathVersions
        // to make runtime classpath consistent with the compile one.
        apiv("org.apache.tika:tika-parsers", "tika")
        apiv("org.ow2.asm:asm")

        // activemq-all should not be used as it provides secondary slf4j binding
        apiv("org.apache.activemq:activemq-broker", "activemq")
        apiv("org.apache.activemq:activemq-client", "activemq")
        apiv("org.apache.activemq:activemq-spring", "activemq")
        apiv("org.springframework:spring-context", "springframework")
        apiv("org.springframework:spring-beans", "springframework")

        apiv("bsf:bsf")
        apiv("cglib:cglib-nodep")
        apiv("com.fasterxml.jackson.core:jackson-annotations", "jackson")
        apiv("com.fasterxml.jackson.core:jackson-core", "jackson")
        apiv("com.fasterxml.jackson.core:jackson-databind", "jackson-databind")
        apiv("com.fifesoft:rsyntaxtextarea")
        apiv("com.formdev:svgSalamander")
        apiv("com.github.ben-manes.caffeine:caffeine")
        apiv("com.github.tomakehurst:wiremock-jre8")
        apiv("com.github.weisj:darklaf-core", "darklaf")
        apiv("com.github.weisj:darklaf-theme", "darklaf")
        apiv("com.github.weisj:darklaf-property-loader", "darklaf")
        apiv("com.github.weisj:darklaf-extensions-rsyntaxarea", "darklaf.extensions")
        apiv("com.helger.commons:ph-commons")
        apiv("com.helger:ph-css")
        apiv("com.jayway.jsonpath:json-path")
        apiv("com.miglayout:miglayout-core", "miglayout")
        apiv("com.miglayout:miglayout-swing", "miglayout")
        apiv("com.sun.activation:javax.activation", "javax.activation")
        apiv("com.thoughtworks.xstream:xstream")
        apiv("commons-codec:commons-codec")
        apiv("commons-collections:commons-collections")
        apiv("commons-io:commons-io")
        apiv("commons-lang:commons-lang")
        apiv("commons-logging:commons-logging")
        apiv("commons-net:commons-net")
        apiv("dnsjava:dnsjava")
        apiv("io.burt:jmespath-core")
        apiv("io.burt:jmespath-jackson")
        apiv("javax.activation:javax.activation-api", "javax.activation")
        apiv("javax.mail:mail")
        apiv("jcharts:jcharts")
        apiv("junit:junit", "junit4")
        apiv("org.jetbrains:annotations", "jetbrains-annotations")
        apiv("org.jetbrains.lets-plot:lets-plot-batik")
        apiv("org.jetbrains.lets-plot:lets-plot-kotlin-jvm")
        apiv("org.junit.jupiter:junit-jupiter", "junit5")
        apiv("org.junit.jupiter:junit-jupiter-api", "junit5")
        apiv("org.junit.jupiter:junit-jupiter-params", "junit5")
        apiv("net.minidev:accessors-smart")
        apiv("net.minidev:json-smart")
        apiv("net.sf.jtidy:jtidy")
        apiv("net.sf.saxon:Saxon-HE")
        apiv("nl.jqno.equalsverifier:equalsverifier")
        apiv("org.apache-extras.beanshell:bsh")
        apiv("org.apache.commons:commons-collections4")
        apiv("org.apache.commons:commons-dbcp2")
        apiv("org.apache.commons:commons-jexl3")
        apiv("org.apache.commons:commons-jexl")
        apiv("org.apache.commons:commons-lang3")
        apiv("org.apache.commons:commons-math3")
        apiv("org.apache.commons:commons-pool2")
        apiv("org.apache.commons:commons-text")
        apiv("org.apache.ftpserver:ftplet-api")
        apiv("org.apache.ftpserver:ftpserver-core")
        apiv("org.apache.geronimo.specs:geronimo-jms_1.1_spec")
        apiv("org.apache.httpcomponents:httpasyncclient")
        apiv("org.apache.httpcomponents:httpclient")
        apiv("org.apache.httpcomponents:httpcore-nio")
        apiv("org.apache.httpcomponents:httpcore")
        apiv("org.apache.httpcomponents:httpmime")
        apiv("org.apache.logging.log4j:log4j-1.2-api", "log4j")
        apiv("org.apache.logging.log4j:log4j-api", "log4j")
        apiv("org.apache.logging.log4j:log4j-core", "log4j")
        apiv("org.apache.logging.log4j:log4j-slf4j-impl", "log4j")
        apiv("org.apache.mina:mina-core")
        apiv("org.apache.rat:apache-rat")
        apiv("org.apache.tika:tika-core", "tika")
        apiv("org.apache.velocity:velocity")
        apiv("org.apache.xmlgraphics:xmlgraphics-commons")
        apiv("org.apiguardian:apiguardian-api")
        apiv("org.bouncycastle:bcmail-jdk15on", "bouncycastle")
        apiv("org.bouncycastle:bcpkix-jdk15on", "bouncycastle")
        apiv("org.bouncycastle:bcprov-jdk15on", "bouncycastle")
        apiv("org.brotli:dec")
        apiv("org.exparity:hamcrest-date")
        apiv("org.freemarker:freemarker")
        apiv("org.hamcrest:hamcrest")
        apiv("org.hamcrest:hamcrest-core", "hamcrest")
        apiv("org.hamcrest:hamcrest-library", "hamcrest")
        apiv("org.hsqldb:hsqldb")
        apiv("org.jdom:jdom")
        apiv("org.jodd:jodd-core", "jodd")
        apiv("org.jodd:jodd-lagarto", "jodd")
        apiv("org.jodd:jodd-log", "jodd")
        apiv("org.jodd:jodd-props", "jodd")
        apiv("org.jsoup:jsoup")
        apiv("org.mongodb:mongo-java-driver")
        apiv("org.mozilla:rhino")
        apiv("org.neo4j.driver:neo4j-java-driver")
        apiv("org.objenesis:objenesis")
        apiv("org.slf4j:jcl-over-slf4j", "slf4j")
        apiv("org.slf4j:slf4j-api", "slf4j")
        apiv("org.spockframework:spock-core")
        apiv("oro:oro")
        apiv("xalan:serializer", "xalan")
        apiv("xalan:xalan", "xalan")
        apiv("xerces:xercesImpl")
        apiv("xml-apis:xml-apis")
        apiv("xmlpull:xmlpull")
        apiv("xpp3:xpp3_min")
    }
}
