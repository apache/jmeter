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

import com.github.vlsi.gradle.properties.dsl.props
import org.apache.jmeter.buildtools.batchtest.BatchTest
import org.apache.jmeter.buildtools.batchtest.BatchTestServer
import java.time.Duration

plugins {
    id("java-test-fixtures")
    id("build-logic.batchtest")
    id("com.github.vlsi.gradle-extensions")
    id("build-logic.jvm-library")
}

val extraTestDependencies by configurations.creating
val loggingClasspath by configurations.creating

dependencies {
    api(projects.src.dist)

    testImplementation(testFixtures(projects.src.core))
    testImplementation("org.apache.commons:commons-lang3") {
        because("StringUtils")
    }
    testImplementation("commons-io:commons-io") {
        because("IOUtils")
    }
    testImplementation("com.fasterxml.jackson.core:jackson-databind") {
        because("It is used in ReportGeneratorSpec and HtmlReportGeneratorSpec")
    }

    extraTestDependencies(platform(projects.src.bomThirdparty))
    extraTestDependencies(platform(projects.src.bomTesting))
    extraTestDependencies("org.hsqldb:hsqldb::jdk8")
    extraTestDependencies("org.apache.mina:mina-core")
    extraTestDependencies("org.apache.ftpserver:ftplet-api")
    extraTestDependencies("org.apache.ftpserver:ftpserver-core")
    // activemq-all should not be used as it provides secondary slf4j binding
    extraTestDependencies("org.apache.activemq:activemq-broker")
    extraTestDependencies("org.apache.activemq:activemq-client")
    extraTestDependencies("org.apache.activemq:activemq-spring")
    extraTestDependencies("org.springframework:spring-context")
    extraTestDependencies("org.springframework:spring-beans")
//    extraTestDependencies("com.fasterxml.jackson.core:jackson-annotations")
    extraTestDependencies("org.apache.commons:commons-pool2")

    // Slf4j is used by activemq and mina, so slf4j binding must be placed on the top-level
    // classpath since extraTestDependencies is added to the classpath
    // This is not required for regular ./bin/jmeter because activemq/mina is not on the top-level
    // classpath but all the jars are loaded by a custom classloader which is instantiated by NewDriver
    // TODO: implement "extra classpath folder" in DynamicClassLoader
    loggingClasspath(platform(projects.src.bomThirdparty))
    loggingClasspath("org.slf4j:jcl-over-slf4j")
    loggingClasspath("org.apache.logging.log4j:log4j-api")
    loggingClasspath("org.apache.logging.log4j:log4j-core")
    loggingClasspath("org.apache.logging.log4j:log4j-1.2-api")
    loggingClasspath("org.apache.logging.log4j:log4j-slf4j-impl")
}

val libOpt = copySpec {
    // Extra dependencies for testing purposes, to be placed in lib/opt
}

val populateLibs by tasks.registering {
    dependsOn(extraTestDependencies)
    doLast {
        val deps = extraTestDependencies.resolvedConfiguration.resolvedArtifacts
        with(libOpt) {
            fileMode = "644".toInt(8)
            dirMode = "755".toInt(8)
            from(deps.map { it.file })
        }
    }
}

// Add dependency from libOpt to populateLibs
libOpt.from(populateLibs)

// For now lib/opt is hard-coded in some of the JMX files
val extraTestJarsDir = rootProject.layout.projectDirectory.dir("lib").dir("opt")

val createDist = ":src:dist:createDist"

val copyExtraTestLibs by tasks.registering(Sync::class) {
    dependsOn(createDist)

    into(extraTestJarsDir)
    with(libOpt)
    preserve {
        include("README.txt")
    }
}

val detailBatchTasks = findProperty("allBatch") is String

val batchTests by tasks.registering() {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Executes all the batch tests" +
        if (detailBatchTasks) "" else " (add -PallBatch to see individual batch tasks)"
}

val jacoco = project.extensions.findByType<JacocoPluginExtension>()

inline fun <reified T : BatchTest> createBatchTask(
    name: String,
    suffix: String = "",
    noinline action: (T.() -> Unit)? = null
) =
    tasks.register(
        "batch" + (if (T::class == BatchTestServer::class) "Server" else "") +
            name.replaceFirstChar { it.titlecaseChar() } + suffix.replaceFirstChar { it.titlecaseChar() },
        T::class
    ) {
        group = when {
            detailBatchTasks -> LifecycleBasePlugin.VERIFICATION_GROUP
            else -> ""
        }
        testName.set(name)
        dependsOn(copyExtraTestLibs)

        // Log4j must be on the classpath because NewDriver initializes logging
        classpath(loggingClasspath)
        classpath(extraTestJarsDir.asFileTree.matching { include("*.jar") })

        jacoco?.applyTo(this)

        if (action != null) {
            action()
        }
    }.also {
        batchTests.configure {
            dependsOn(it)
        }
    }

fun createBatchTestTask(name: String, suffix: String = "", action: (BatchTest.() -> Unit)? = null) =
    createBatchTask(name, suffix, action)

fun createBatchServerTestTask(name: String, suffix: String = "", action: (BatchTestServer.() -> Unit)? = null) =
    createBatchTask(name, suffix, action)

arrayOf(
    "BatchTestLocal",
    "Bug62239", "Bug52968", "Bug50898",
    "Bug56243",
    // StackOverflowError with ModuleController in Non-GUI mode if its name is the same as the target node
    "Bug55375",
    "Bug56811",
    "TEST_HTTPS",
    "TestCookieManager",
    "JMS_TESTS",
    "OS_TESTS",
    "TestKeepAlive",
    "ResponseDecompression",
    "TestSchedulerWithTimer",
    "Http4ImplPreemptiveBasicAuth",
    "Http4ImplDigestAuth",
    "BUG_62847",
    "HTMLParserTestFile_2",
    "TestResultStatusAction",
    "TestRedirectionPolicies"
).map { createBatchTestTask(it) }

// Certain errors are expected in those tests as they examine failure cases as well
arrayOf(
    "TCP_TESTS",
    "FTP_TESTS",
    "JDBC_TESTS"
).map {
    createBatchTestTask(it) {
        ignoreErrorLogs.set(true)
    }
}

createBatchTestTask("Bug54685") {
    jmeterArgument("sample_variables", "REFERENCE,JSESSIONID")
}

createBatchTestTask("Http4ImplPreemptiveBasicAuth", "Java") {
    jmeterArgument("jmeter.httpsampler", "Java")
}

for (impl in arrayOf("Java", "HttpClient4")) {
    createBatchTestTask("SlowCharsFeature", impl) {
        csvFile.set(outputDirectory.file("${testName.get()}_$impl.csv"))
        xmlFile.set(outputDirectory.file("${testName.get()}_$impl.xml"))
        jmeterArgument("jmeter.httpsampler", impl)
    }

    createBatchTestTask("TestHeaderManager", impl) {
        jmeterArgument("jmeter.httpsampler", impl)
    }

    createBatchTestTask("TEST_HTTP", impl) {
        jmeterArgument("jmeter.httpsampler", impl)
        csvFile.set(outputDirectory.file("${testName.get()}_$impl.csv"))
        xmlFile.set(outputDirectory.file("${testName.get()}_$impl.xml"))
        if (impl == "Java") {
            ignoreErrorLogs.set(true)
        }
    }
}

// Note: original build.xml seem to use Bug54685 test, however in fact batchtestserver target
// just ignored the given filename
val batchTestServerStartupTimeout: String? by project
val batchTestServerStartupTimeoutDuration =
    batchTestServerStartupTimeout?.let {
        try {
            Duration.parse(it)
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Unable to parse the value of batchTestServerStartupTimeout property as duration $it." +
                    " Please ensure it follows java.time.Duration format (e.g. PT5S)",
                e
            )
        }
    }

createBatchServerTestTask("BatchTestLocal") {
    batchTestServerStartupTimeoutDuration?.let { startupTimeout.set(it) }
}

tasks.test {
    // Test examine JAR contents in /lib/..., so we need to copy jars to the projectRoot/lib/
    dependsOn(createDist)
}

tasks.check {
    dependsOn(batchTests)
}

val flakyTests = listOf(
    "batchHttp4ImplDigestAuth",
    "batchHttp4ImplPreemptiveBasicAuthJava",
    "batchSlowCharsFeatureHttpClient4",
    "batchSlowCharsFeatureJava",
    "batchTCP_TESTS",
    "batchTestKeepAlive",
    "batchTestRedirectionPolicies"
)

if (props.bool("enableFlaky", default = false)) {
    println("The following tests are known to be flaky as they depend on the external services: $flakyTests")
} else {
    println("Certain tests will be skipped as they depend on external services and fail too often. Please add -PenableFlaky to enable them: $flakyTests")
    for (test in flakyTests) {
        tasks.named(test) {
            enabled = false
        }
    }
}
