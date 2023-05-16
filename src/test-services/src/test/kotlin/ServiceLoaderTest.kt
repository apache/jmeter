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

import org.apache.jmeter.util.services.AbstractServiceInterface
import org.apache.jmeter.util.services.NotImplementedInterface
import org.apache.jmeter.util.services.ServiceThrowingExceptionInterface
import org.apache.jmeter.util.services.ServiceWithPrivateConstructorInterface
import org.apache.jmeter.util.services.WorkableServiceInterface
import org.apache.jmeter.util.services.loadServices
import org.apache.jorphan.reflect.CollectServiceLoadExceptionHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class ServiceLoaderTest {
    @Test
    fun `service without public constructor`() {
        assertServiceLoad<ServiceWithPrivateConstructorInterface>(
            "[]",
            "[service: org.apache.jmeter.util.services.ServiceWithPrivateConstructorInterface, implementationClass: org.apache.jmeter.util.services.ServiceWithPrivateConstructor]"
        )
    }

    @Test
    fun `service not implementing interface`() {
        assertServiceLoad<NotImplementedInterface>(
            "[]",
            "[service: org.apache.jmeter.util.services.NotImplementedInterface, implementationClass: org.apache.jmeter.util.services.ServiceNotImplementingInterface]"
        )
    }

    @Test
    fun `service failing in constructor`() {
        assertServiceLoad<ServiceThrowingExceptionInterface>(
            "[]",
            "[service: org.apache.jmeter.util.services.ServiceThrowingExceptionInterface, implementationClass: org.apache.jmeter.util.services.ServiceThrowingException]"
        )
    }

    @Test
    fun `abstract service`() {
        assertServiceLoad<AbstractServiceInterface>(
            "[]",
            "[service: org.apache.jmeter.util.services.AbstractServiceInterface, implementationClass: org.apache.jmeter.util.services.AbstractService]"
        )
    }

    @Test
    fun `service loads`() {
        assertServiceLoad<WorkableServiceInterface>(
            "[test service]",
            "[]"
        )
    }

    private inline fun <reified S : Any> assertServiceLoad(
        successMessage: String,
        failureMessage: String,
    ) {
        val failures = CollectServiceLoadExceptionHandler<S>()
        val successes = loadServices<S>(failures)
        val allFailures = failures.toCollection()
        assertAll(
            {
                assertEquals(successMessage, successes.toString()) {
                    "Successfully loaded services for ${S::class.java.name}"
                }
            },
            {
                try {
                    assertEquals(
                        failureMessage,
                        allFailures.map {
                            // it.throwable is not included here as it is not stable across different JVM implementations
                            "service: ${it.service.name}, " +
                                "implementationClass: ${it.className}"
                        }.toString(),
                    ) {
                        "All failures when loading service ${S::class.java.name} are $allFailures"
                    }
                } catch (e: Throwable) {
                    // Attach load failure exceptions to the assertion failure, so
                    // the failure message includes the instantiation failure reason
                    allFailures.forEach { loadFailure ->
                        loadFailure.throwable?.let { e.addSuppressed(it) }
                    }
                    throw e
                }
            }
        )
    }
}
