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

import versions.Libs

val skipMavenPublication = setOf(
    ":src:dist",
    ":src:dist-check",
    ":src:examples",
    ":src:generator",
    ":src:license-binary",
    ":src:license-source"
)

subprojects {
    val groovyUsed = file("src/main/groovy").isDirectory || file("src/test/groovy").isDirectory

    apply<JavaPlugin>()
    if (groovyUsed) {
        apply<GroovyPlugin>()
    }
    apply<MavenPublishPlugin>()
    apply<JacocoPlugin>()

    dependencies {
        val testImplementation by configurations
        testImplementation(Libs.junit)
        if (groovyUsed) {
            testImplementation(Libs.groovy_all) {
                because("We want to enable Groovy-based tests")
            }
            testImplementation(Libs.spock_core)
        }
        testImplementation(Libs.cglib_nodep) {
            because("""
                org.spockframework.mock.CannotCreateMockException: Cannot create mock for
                 class org.apache.jmeter.report.processor.AbstractSummaryConsumer${'$'}SummaryInfo.
                 Mocking of non-interface types requires a code generation library.
                 Please put an up-to-date version of byte-buddy or cglib-nodep on the class path.""".trimIndent())
        }
        testImplementation(Libs.objenesis) {
            because("""
                org.spockframework.mock.CannotCreateMockException: Cannot create mock for
                 class org.apache.jmeter.report.core.Sample. To solve this problem,
                 put Objenesis 1.2 or higher on the class path (recommended),
                 or supply constructor arguments (e.g. 'constructorArgs: [42]') that allow to construct
                 an object of the mocked type.""".trimIndent())
        }
    }

    // Note: jars below do not normalize line endings.
    // Those jars, however are not included to source/binary distributions
    // so the normailzation is not that important

    val testJar by tasks.registering(Jar::class) {
        val sourceSets: SourceSetContainer by project
        archiveClassifier.set("test")
        from(sourceSets["test"].output)
    }

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
        extendsFrom(configurations["testRuntime"])
    }

    // Parenthesis needed to use Project#getArtifacts
    (artifacts) {
        testClasses(testJar)
    }

    val achiveBaseName = when (name) {
        "jorphan" -> name
        "launcher" -> "ApacheJMeter"
        else -> "ApacheJMeter_$name"
    }
    setProperty("archivesBaseName", achiveBaseName)

    // See https://stackoverflow.com/a/53661897/1261287
    // Subprojects can't use "publishing" since that accessor is not available at parent project
    // evaluation time
    configure<PublishingExtension> {
        if (project.path in skipMavenPublication) {
            return@configure
        }
        publications {
            create<MavenPublication>(project.name) {
                artifactId = achiveBaseName
                version = rootProject.version.toString()
                from(components["java"])

                // Eager task creation is required due to
                // https://github.com/gradle/gradle/issues/6246
                artifact(sourcesJar.get())
                artifact(javadocJar.get())

                pom {
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
