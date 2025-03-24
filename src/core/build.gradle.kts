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
import com.github.vlsi.gradle.ide.IdeExtension
import java.util.jar.JarFile

plugins {
    id("java-test-fixtures")
    id("build-logic.jvm-published-library")
}

dependencies {
    api(projects.src.launcher)
    api(projects.src.jorphan)
    testImplementation(testFixtures(projects.src.jorphan))

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
    kapt("org.apache.logging.log4j:log4j-core") {
        because("Generates a plugin cache file for GuiLogEventAppender")
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
    api("xalan:serializer") {
        because("Xalan 2.7.3 misses xalan:serializer dependency in pom.xml, see https://issues.apache.org/jira/browse/XALANJ-2649")
    }
    api("xml-apis:xml-apis") {
        because("Xalan 2.7.3 misses xml-apis:xml-apis dependency in pom.xml, see https://issues.apache.org/jira/browse/XALANJ-2649")
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
    runtimeOnly("commons-collections:commons-collections") {
        because("Compatibility for old plugins")
    }
    implementation("org.jetbrains.lets-plot:lets-plot-batik")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm")
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
    implementation("org.brotli:dec")
    implementation("org.freemarker:freemarker")
    implementation("org.jodd:jodd-core")
    implementation("org.jodd:jodd-props")
    implementation("org.mozilla:rhino")
    implementation("org.slf4j:jcl-over-slf4j")
    // TODO: JMeter bundles Xerces, however the reason is unknown
    runtimeOnly("xerces:xercesImpl")
    runtimeOnly("xml-apis:xml-apis")

    testImplementation("commons-net:commons-net")
    testImplementation("io.mockk:mockk")

    testFixturesApi(testFixtures(projects.src.jorphan))
    testFixturesImplementation(projects.src.testkit)
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
}

val generatedVersionDir = layout.buildDirectory.dir("generated/sources/version")

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

// For some reason, using `ide { ... }` sometimes causes
// Caused by: java.lang.IllegalStateException: couldn't find inline method
// Lorg/gradle/kotlin/dsl/Accessorslkzxmv806rumtqvft7195qyhKt;.getIde(Lorg/gradle/api/Project;)Lcom/github/vlsi/gradle/ide/IdeExtension;
configure<IdeExtension> {
    generatedJavaSources(versionClass.get(), generatedVersionDir.get().asFile)
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
plugins.withId("org.jetbrains.kotlin.kapt") {
    // kapt adds kaptGenerateStubsKotlin in afterEvaluate, so we can't use just tasks.named here
    // This workaround is needed for Kotlin Gradle Plugin 1.9
    afterEvaluate {
        tasks.named("kaptGenerateStubsKotlin") {
            dependsOn(versionClass)
        }
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

// Checks the generated JAR for a Log4j plugin cache file.
tasks.jar {
    doLast {
        val jarFile = archiveFile.get().asFile
        JarFile(jarFile).use { jar ->
            val entryName = "META-INF/org/apache/logging/log4j/core/config/plugins/Log4j2Plugins.dat"
            if (jar.getJarEntry(entryName) == null) {
                throw IllegalStateException("$entryName was not found in $jarFile. The entry should be generated by log4j-core annotation processor")
            }
        }
    }
}
