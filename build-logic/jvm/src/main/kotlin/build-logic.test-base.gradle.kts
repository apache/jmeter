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

import com.github.vlsi.gradle.dsl.configureEach
import com.github.vlsi.gradle.properties.dsl.props
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.project

plugins {
    id("java-library")
}

dependencies {
    findProject(":src:testkit")?.let {
        testImplementation(testFixtures(it))
    }
}

tasks.configureEach<Test> {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
    // Pass the property to tests
    fun passProperty(name: String, default: String? = null) {
        val value = System.getProperty(name) ?: default
        value?.let { systemProperty(name, it) }
    }
    System.getProperties().filter {
        it.key.toString().startsWith("jmeter.properties.")
    }.forEach {
        systemProperty(it.key.toString().substring("jmeter.properties.".length), it.value)
    }
    props.string("testExtraJvmArgs").trim().takeIf { it.isNotBlank() }?.let {
        jvmArgs(it.split(" ::: "))
    }
    props.string("testDisableCaching").trim().takeIf { it.isNotBlank() }?.let {
        outputs.doNotCacheIf(it) {
            true
        }
    }
    passProperty("java.awt.headless")
    passProperty("skip.test_TestDNSCacheManager.testWithCustomResolverAnd1Server")
    // Spock tests use cglib proxies that access ClassLoader.defineClass reflectively
    // See https://github.com/apache/jmeter/pull/5763
    if (JavaVersion.current().isJava9Compatible) {
        jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    }
}
