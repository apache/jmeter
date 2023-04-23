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

import com.github.autostyle.gradle.AutostyleTask

plugins {
    id("build-logic.jvm-published-library")
}

dependencies {
    api(projects.src.launcher)
    api(projects.src.jorphan)
    testImplementation(project(":src:jorphan", "testClasses"))

    api("bsf:bsf") {
        because("protected BSFManager BSFTestElement#getManager()")
    }
    api("com.fifesoft:rsyntaxtextarea") {
        because("JSyntaxTextArea extends RSyntaxTextArea")
    }
    api("net.sf.jtidy:jtidy") {
        because("public static Tidy XPathUtil#makeTidyParser()")
    }
    api("com.thoughtworks.xstream:xstream") {
        because("XStream in used in public API")
    }
    api("org.apache.logging.log4j:log4j-1.2-api")
    api("org.apache.logging.log4j:log4j-api")
    api("org.apache.logging.log4j:log4j-core") {
        because("GuiLogEventAppender is using log4j-core to implement GUI-based log appender")
    }
    api("org.apache.logging.log4j:log4j-slf4j-impl") {
        because("Both log4j and slf4j are included, so it makes sense to just add log4j->slf4j bridge as well")
    }
    api("org.apiguardian:apiguardian-api")
    api("oro:oro") {
        because("Perl5Matcher org.apache.jmeter.util.JMeterUtils.getMatcher()")
    }
    api("xalan:xalan") {
        because("PropertiesBasedPrefixResolver extends PrefixResolverDefault")
    }
    // Note: Saxon should go AFTER xalan so xalan XSLT is used
    // org.apache.jmeter.util.XPathUtilTest.testFormatXmlSimple assumes xalan transformer
    api("net.sf.saxon:Saxon-HE") {
        because("XPathUtil: throws SaxonApiException")
    }

    runtimeOnly("org.codehaus.groovy:groovy") {
        because("Groovy is a default JSR232 engine")
    }
    arrayOf("dateutil", "datetime", "jmx", "json", "jsr223", "sql", "templates").forEach {
        runtimeOnly("org.codehaus.groovy:groovy-$it") {
            because("Groovy is a default JSR232 engine")
        }
    }

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.formdev:svgSalamander")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("com.github.weisj:darklaf-core")
    implementation("com.github.weisj:darklaf-theme")
    implementation("com.github.weisj:darklaf-property-loader")
    implementation("com.github.weisj:darklaf-extensions-rsyntaxarea")
    implementation("com.miglayout:miglayout-swing")
    implementation("commons-codec:commons-codec") {
        because("DigestUtils")
    }
    implementation("commons-collections:commons-collections") {
        because("Compatibility for old plugins")
    }
    implementation("org.jetbrains.lets-plot:lets-plot-batik") {
        // See https://github.com/JetBrains/lets-plot/issues/471
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm") {
        // See https://github.com/JetBrains/lets-plot/issues/471
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
    implementation("org.apache.commons:commons-collections4")
    implementation("org.apache.commons:commons-math3") {
        because("Mean, DescriptiveStatistics")
    }
    implementation("org.apache.commons:commons-text")
    // For some reason JMeter bundles just tika-core and tika-parsers without transitive
    // dependencies. So we exclude those
    implementation("org.apache.tika:tika-core") {
        isTransitive = false
    }
    runtimeOnly("org.apache.tika:tika-parsers") {
        isTransitive = false
    }
    implementation("org.apache.xmlgraphics:xmlgraphics-commons")
    implementation("org.freemarker:freemarker")
    implementation("org.jodd:jodd-core")
    implementation("org.jodd:jodd-props")
    implementation("org.mozilla:rhino")
    implementation("org.slf4j:jcl-over-slf4j")
    // TODO: JMeter bundles Xerces, however the reason is unknown
    runtimeOnly("xerces:xercesImpl")
    runtimeOnly("xml-apis:xml-apis")

    testImplementation("commons-net:commons-net")
    testRuntimeOnly("org.spockframework:spock-core")
}

val generatedVersionDir = File(buildDir, "generated/sources/version")

val versionClass by tasks.registering(Sync::class) {
    val lastEditYear: String by rootProject.extra
    val displayVersion: String by rootProject.extra
    inputs.property("@VERSION@", displayVersion)
    inputs.property("@YEAR@", lastEditYear)
    outputs.dir(generatedVersionDir)

    from("$projectDir/src/main/version") {
        include("**/*.java")
        filter { x: String ->
            x.replace("@VERSION@", displayVersion)
                .replace("@YEAR@", lastEditYear)
        }
    }
    into(generatedVersionDir)
}

ide {
    generatedJavaSources(versionClass.get(), generatedVersionDir)
}

// <editor-fold defaultstate="collapsed" desc="Gradle can't infer task dependencies, however it sees they use the same directories. So we add the dependencies">
tasks.sourcesJar {
    dependsOn(versionClass)
}

plugins.withId("org.jetbrains.kotlin.jvm") {
    tasks.named("compileKotlin") {
        dependsOn(versionClass)
    }
}

tasks.withType<Checkstyle>().matching { it.name == "checkstyleMain" }
    .configureEach {
        mustRunAfter(versionClass)
    }

tasks.withType<AutostyleTask>().configureEach {
    mustRunAfter(versionClass)
}
// </editor-fold>

tasks.jar {
    into("org/apache/jmeter/images") {
        from("$rootDir/xdocs/images/logo.svg")
    }
}
