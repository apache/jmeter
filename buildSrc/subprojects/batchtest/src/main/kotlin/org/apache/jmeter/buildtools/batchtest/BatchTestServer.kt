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

package org.apache.jmeter.buildtools.batchtest

import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.property
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import java.io.File
import java.net.ConnectException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

open class BatchTestServer @Inject constructor(objects: ObjectFactory) : BatchTest(objects) {
    private val executor =
        Executors.newFixedThreadPool(project.gradle.startParameter.maxWorkerCount)

    @Internal
    val serverLogFile = objects.fileProperty()
        .convention(outputDirectory.file(testName.map { "${it}Server.log" }))

    @OutputFile
    val jacocoExecFile = objects.fileProperty()
        .convention(project.layout.buildDirectory.file("jacoco/$name.exec"))

    @Input
    val startupTimeout = objects.property<Duration>()
        .convention(Duration.ofSeconds(15))

    private fun deleteWorkfiles() {
        project.delete(serverLogFile)
    }

    private fun getFreePort(): Int =
        ServerSocket(0).use {
            return it.localPort
        }

    private fun waitForPort(host: String, port: Int, timeout: Duration): Boolean {
        val deadline = System.nanoTime() + timeout.toNanos()
        while (System.nanoTime() < deadline) {
            try {
                Socket(host, port).close()
                return true
            } catch (e: ConnectException) {
                /* ignore */
                Thread.sleep(50)
            }
        }
        throw GradleException("Unable to connect to $host:$port with timeout of $timeout")
    }

    override fun checkErrors(summary: MutableList<String>) {
        super.checkErrors(summary)
        catLogFile(serverLogFile.get().asFile, summary)
    }

    override fun exec() {
        deleteWorkfiles()

        val client = this
        val serverPort = getFreePort()
        val serverHost = InetAddress.getLocalHost().canonicalHostName
        args("-R$serverHost:$serverPort")
        val jacoco = extensions.findByType<JacocoTaskExtension>()
        // The extension might not exist, so don't fail if so
        // When the extension is present, we get javaagent argumet from it
        val jvmarg = jacoco?.takeIf { it.isEnabled }?.let {
            val dst = it.destinationFile ?: throw GradleException("destinationFile is not configured for task $this")
            val serverExec = jacocoExecFile.get().asFile
            it.setDestinationFile(serverExec)
            if (serverExec.exists()) {
                // We want to capture new coverage, not a merged result from previous runs
                // so previous .exec should be deleted if exists
                serverExec.delete()
            }
            val jvmarg = it.asJvmArg

            it.setDestinationFile(dst)
            jvmarg
        }
        val server = executor.submit {
            project.javaexec {
                workingDir = File(project.rootDir, "bin")
                mainClass.set("org.apache.jmeter.NewDriver")
                classpath(client.classpath)
                standardOutput = System.out.writer().withPrefix("[server] ")
                errorOutput = System.err.writer().withPrefix("[server] ")
                maxHeapSize = client.maxHeapSize
                jvmArgs("-Xss256k", "-XX:MaxMetaspaceSize=128m")
                if (jvmarg != null) {
                    jvmArgs(jvmarg)
                }
                systemProperty("java.rmi.server.hostname", serverHost)
                systemProperty("server_port", serverPort)
                systemProperty("java.awt.headless", "true")
                systemProperty("user.language", userLanguage.get())
                systemProperty("user.region", userRegion.get())
                systemProperty("user.country", userCountry.get())

                args("-pjmeter.properties")
                args("-q", batchProperties.get())
                args("-i", log4jXml.get())
                args("-j", serverLogFile.get())
                args("-s", "-Jserver.exitaftertest=true")
            }
                .rethrowFailure()
                .assertNormalExitValue()
        }

        waitForPort(serverHost, serverPort, startupTimeout.get())
        super.exec()

        try {
            server.get(1, TimeUnit.SECONDS)
        } catch (e: TimeoutException) {
            server.cancel(true)
        }

        deleteWorkfiles()
    }
}
