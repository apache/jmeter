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

package org.apache.jmeter.protocol.http.sampler

import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.jmeter.protocol.http.control.Header
import org.apache.jmeter.protocol.http.control.HeaderManager
import org.apache.jmeter.protocol.http.util.HTTPConstants
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.Request
import org.eclipse.jetty.client.api.Response
import org.eclipse.jetty.client.api.Result
import org.eclipse.jetty.client.util.BufferingResponseListener
import org.eclipse.jetty.util.thread.QueuedThreadPool
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

const val VAR_JETTY_CLIENT = "_jv_jetty_client"

val jettyThreadPool by lazy { QueuedThreadPool(15) }
val jettyClnt by lazy {
    HttpClient().also {
        // We don't want each client to create its own thread pool
        it.executor = jettyThreadPool
        // No idea if that is needed
        it.start()
        it.maxConnectionsPerDestination = 100500
    }
}

private val JMeterVariables.jettyClient: HttpClient
    get() = jettyClnt/* {
        val cache = getObject(VAR_JETTY_CLIENT)
        if (cache != null) {
            return cache as HttpClient
        }
        return HttpClient().also {
            putObject(VAR_JETTY_CLIENT, it)
            // We don't want each client to create its own thread pool
            it.executor = jettyThreadPool
            // No idea if that is needed
            it.start()
        }
    }*/

private suspend fun Request.send(sampleResult: HTTPSampleResult, maxLength: Int) {
    suspendCancellableCoroutine<Unit> { continuation ->
        continuation.invokeOnCancellation { cause -> abort(cause) }
        send(object : BufferingResponseListener(maxLength) {
            override fun onBegin(response: Response) {
                super.onBegin(response)
                sampleResult.connectEnd()
                sampleResult.latencyEnd()
            }
            override fun onComplete(result: Result) {
                try {
                    sampleResult.sampleEnd()
                    sampleResult.isSuccessful = result.isSucceeded
                    val response = result.response
                    sampleResult.responseCode = response.status.toString()
                    sampleResult.responseHeaders = response.headers.toString()
                    if (sampleResult.isRedirect) {
                        sampleResult.redirectLocation = response.headers[HTTPConstants.HEADER_LOCATION]
                    }
                    sampleResult.headersSize = sampleResult.responseHeaders.length
                    if (result.isSucceeded) {
                        val content = content
                        sampleResult.setBodySize(content.size.toLong())
                        sampleResult.responseData = content
                        sampleResult.setDataEncoding(encoding)
                        sampleResult.dataType = when {
                            mediaType == null || HTTPSampleResult.isBinaryType(mediaType) -> HTTPSampleResult.BINARY
                            else -> HTTPSampleResult.TEXT
                        }
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                } finally {
                    continuation.resume(Unit)
                }
            }
        })
    }
}

class HttpJettyHttpClientImpl(testElement: HTTPSamplerBase) : HTTPAbstractImpl(testElement),
    SuspendingHttpSampler {
    override suspend fun suspendingSample(
        url: URL,
        method: String,
        followRedirects: Boolean,
        depth: Int
    ): HTTPSampleResult {
        val sampleResult = HTTPSampleResult().also {
            configureSampleLabel(it, url)
            it.url = url
            it.httpMethod = method
            it.sampleStart() // Count the retries as well in the time
        }

        // Check cache for an entry with an Expires header in the future
        val cacheManager = cacheManager
        if (cacheManager != null && HTTPConstants.GET.equals(method, ignoreCase = true)) {
            if (cacheManager.inCache(url, headerManager?.headersArray ?: emptyArray())) {
                return updateSampleResultForResourceInCache(sampleResult)
            }
        }

        val threadContext = JMeterContextService.getContext()
        val vars = threadContext.variables
        val client = vars.jettyClient.also {
            it.isFollowRedirects = followRedirects
            it.connectTimeout = connectTimeout.toLong()
            it.addressResolutionTimeout = connectTimeout.toLong()
        }

        try {
            client.newRequest(url.toURI())
                .timeout(responseTimeout.toLong(), TimeUnit.MILLISECONDS)
                .send(sampleResult, 2 * 1024 * 1024)
        } catch (e: Exception) {
            return errorResult(e, sampleResult)
        }
        return sampleResult
    }

    private val HeaderManager.headersArray: Array<Header>
        get() {
            val headers = headers ?: return emptyArray()
            return headers
                .map { it.objectValue as Header }
                .toTypedArray()
        }

    override fun interrupt(): Boolean = false

    override fun sample(
        url: URL?,
        method: String?,
        areFollowingRedirect: Boolean,
        frameDepth: Int
    ): HTTPSampleResult {
        TODO("not implemented")
    }
}
