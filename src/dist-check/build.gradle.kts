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

import org.apache.jmeter.buildtools.batchtest.BatchTest
import org.apache.jmeter.buildtools.batchtest.BatchTestServer
import versions.Libs

plugins {
    jmeterbuild.batchtest
}

val extraTestDependencies by configurations.creating
val loggingClasspath by configurations.creating

dependencies {
    compile(project(":src:dist"))
    testCompile(project(":src:dist", "testClasses"))

    Libs.activemq.map { extraTestDependencies(it) }
    extraTestDependencies(Libs.hsqldb)
    extraTestDependencies(Libs.mina_core)
    extraTestDependencies(Libs.ftplet_api)
    extraTestDependencies(Libs.ftpserver_core)

    // Slf4j is used by activemq and mina, so slf4j binding must be placed on the top-level
    // classpath since extraTestDependencies is added to the classpath
    // This is not required for regular ./bin/jmeter because activemq/mina is not on the top-level
    // classpath but all the jars are loaded by a custom classloader which is instantiated by NewDriver
    // TODO: implement "extra classpath folder" in DynamicClassLoader
    loggingClasspath(Libs.jcl_over_slf4j)
    loggingClasspath(Libs.log4j_api)
    loggingClasspath(Libs.log4j_core)
    loggingClasspath(Libs.log4j_12_api)
    loggingClasspath(Libs.log4j_slf4j_impl)
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

// For now lib/opt is hard-coded in some of the JMX files
val extraTestJarsDir = rootProject.layout.projectDirectory.dir("lib").dir("opt")

val createDist by project(":src:dist").tasks.existing(Task::class)

val copyExtraTestLibs by tasks.registering(Sync::class) {
    dependsOn(createDist, populateLibs)

    into(extraTestJarsDir)
    with(libOpt)
    preserve {
        include("README.txt")
    }
}

val allBatchTests by tasks.registering() {
    group = BatchTest.BATCH_TESTS_GROUP_NAME
    description = "Executes all batch tests"
}

val jacoco = project.extensions.findByType<JacocoPluginExtension>()

inline fun <reified T : BatchTest> createBatchTask(
    name: String,
    suffix: String = "",
    noinline action: (T.() -> Unit)? = null
) =
    tasks.register("batch" + (if (T::class == BatchTestServer::class) "Server" else "") +
            name.capitalize() + suffix.capitalize(), T::class) {
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
        allBatchTests.configure {
            dependsOn(it)
        }
    }

fun createBatchTestTask(name: String, suffix: String = "", action: (BatchTest.() -> Unit)? = null) =
    createBatchTask(name, suffix, action)

fun createBatchServerTestTask(name: String, suffix: String = "", action: (BatchTestServer.() -> Unit)? = null) =
    createBatchTask(name, suffix, action)

arrayOf(
    "BatchTestLocal",
    "Bug52310",
    "Bug62239", "Bug52968", "Bug50898",
    "Bug56243",
    // StackOverflowError with ModuleController in Non-GUI mode if its name is the same as the target node
    "Bug55375",
    "Bug56811",
    "TEST_HTTPS",
    "TestCookieManager",
    "JMS_TESTS",
    "OS_TESTS",
    "Bug60607",
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
createBatchServerTestTask("BatchTestLocal")

tasks.named(JavaPlugin.TEST_TASK_NAME).configure {
    // Test examine JAR contents in /lib/..., so we need to copy jars to the projectRoot/lib/
    dependsOn(createDist)
    // This is a convenience, so batch tests are executed as a part of default "test" task
    dependsOn(allBatchTests)
}
