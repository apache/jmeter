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
    // Bring Kotlin plugin to the build script class path, so it can be found when required
    // in apply(plugin = ...) below
    kotlin("jvm") apply false
}
val skipMavenPublication = setOf(
    ":src:bshclient",
    ":src:dist",
    ":src:dist-check",
    ":src:examples",
    ":src:generator",
    ":src:licenses",
    ":src:protocol",
    ":src:release",
    ":src:testkit",
    ":src:testkit-wiremock"
)

fun Project.boolProp(name: String) =
    findProperty(name)
        // Project properties include tasks, extensions, etc, and we want only String properties
        // We don't want to use "task" as a boolean property
        ?.let { it as? String }
        ?.equals("false", ignoreCase = true)?.not()

val skipJavadoc by extra {
    boolProp("skipJavadoc") ?: false
}

subprojects {
    if (path == ":src:bom") {
        return@subprojects
    }

    val groovyUsed = file("src/main/groovy").isDirectory || file("src/test/groovy").isDirectory
    val kotlinUsed = file("src/main/kotlin").isDirectory || file("src/test/kotlin").isDirectory
    val testsPresent = file("src/test").isDirectory

    apply<JavaLibraryPlugin>()
    if (groovyUsed) {
        apply<GroovyPlugin>()
    }
    if (kotlinUsed) {
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }
    if (project.path !in skipMavenPublication) {
        apply<MavenPublishPlugin>()
    }
    apply<JacocoPlugin>()

    dependencies {
        val api by configurations
        api(platform(project(":src:bom")))

        if (!testsPresent) {
            // No tests => no dependencies required
            return@dependencies
        }
        val implementation by configurations
        val testImplementation by configurations
        val testRuntimeOnly by configurations
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.hamcrest:hamcrest")
        testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
        testImplementation("junit:junit")
        testImplementation(testFixtures(project(":src:testkit")))
        if (groovyUsed) {
            testImplementation("org.spockframework:spock-core")
        }
        if (kotlinUsed) {
            testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        }
        testRuntimeOnly("cglib:cglib-nodep") {
            because(
                """
                org.spockframework.mock.CannotCreateMockException: Cannot create mock for
                 class org.apache.jmeter.report.processor.AbstractSummaryConsumer${'$'}SummaryInfo.
                 Mocking of non-interface types requires a code generation library.
                 Please put an up-to-date version of byte-buddy or cglib-nodep on the class path.
                """.trimIndent()
            )
        }
        testRuntimeOnly("org.objenesis:objenesis") {
            because(
                """
                org.spockframework.mock.CannotCreateMockException: Cannot create mock for
                 class org.apache.jmeter.report.core.Sample. To solve this problem,
                 put Objenesis 1.2 or higher on the class path (recommended),
                 or supply constructor arguments (e.g. 'constructorArgs: [42]') that allow to construct
                 an object of the mocked type.
                """.trimIndent()
            )
        }
    }

    // Note: jars below do not normalize line endings.
    // Those jars, however are not included to source/binary distributions
    // so the normailzation is not that important

    val sourcesJar by tasks.registering(Jar::class) {
        val sourceSets: SourceSetContainer by project
        from(sourceSets["main"].allJava)
        archiveClassifier.set("sources")
    }

    val javadocJar by tasks.registering(Jar::class) {
        from(tasks.named(JavaPlugin.JAVADOC_TASK_NAME))
        archiveClassifier.set("javadoc")
    }

    val testClasses by configurations.creating {
    }

    if (testsPresent) {
        // Do not generate test jars when src/test folder is missing (e.g. "config.jar")
        val testJar by tasks.registering(Jar::class) {
            val sourceSets: SourceSetContainer by project
            archiveClassifier.set("test")
            from(sourceSets["test"].output)
        }

        // Parenthesis needed to use Project#getArtifacts
        (artifacts) {
            testClasses(testJar)
        }
    }

    val archivesBaseName = when (name) {
        "jorphan", "bshclient" -> name
        "launcher" -> "ApacheJMeter"
        else -> "ApacheJMeter_$name"
    }
    setProperty("archivesBaseName", archivesBaseName)

    if (project.path in skipMavenPublication) {
        return@subprojects
    }
    // See https://stackoverflow.com/a/53661897/1261287
    // Subprojects can't use "publishing" since that accessor is not available at parent project
    // evaluation time
    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(project.name) {
                artifactId = archivesBaseName
                version = rootProject.version.toString()
                from(components["java"])

                if (!skipJavadoc) {
                    // Eager task creation is required due to
                    // https://github.com/gradle/gradle/issues/6246
                    artifact(sourcesJar.get())
                    artifact(javadocJar.get())
                }

                // Use the resolved versions in pom.xml
                // Gradle might have different resolution rules, so we set the versions
                // that were used in Gradle build/test.
                versionMapping {
                    usage(Usage.JAVA_RUNTIME) {
                        fromResolutionResult()
                    }
                    usage(Usage.JAVA_API) {
                        fromResolutionOf("runtimeClasspath")
                    }
                }

                pom {
                    withXml {
                        val sb = asString()
                        var s = sb.toString()
                        // <scope>compile</scope> is Maven default, so delete it
                        s = s.replace("<scope>compile</scope>", "")
                        // Cut <dependencyManagement> because all dependencies have the resolved versions
                        s = s.replace(
                            Regex(
                                "<dependencyManagement>.*?</dependencyManagement>",
                                RegexOption.DOT_MATCHES_ALL
                            ),
                            ""
                        )
                        sb.setLength(0)
                        sb.append(s)
                        // Re-format the XML
                        asNode()
                    }
                    name.set("Apache JMeter ${project.name.capitalize()}")
                    description.set(project.description)
                    inceptionYear.set("1998")
                    url.set("http://jmeter.apache.org/")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            comments.set("A business-friendly OSS license")
                        }
                    }
                    issueManagement {
                        system.set("bugzilla")
                        url.set("https://bz.apache.org/bugzilla/describecomponents.cgi?product=JMeter")
                    }
                    scm {
                        connection.set("scm:git:https://gitbox.apache.org/repos/asf/jmeter.git")
                        developerConnection.set("scm:git:https://gitbox.apache.org/repos/asf/jmeter.git")
                        url.set("https://github.com/apache/jmeter")
                        tag.set("HEAD")
                    }
                }
            }
        }
    }
}
