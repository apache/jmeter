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
    `java-library`
    id("build-logic.test-base")
}

dependencies {
    testImplementation("org.spockframework:spock-core")
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
