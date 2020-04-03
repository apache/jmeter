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

import kotlinx.coroutines.cancelFutureOnCancellation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient
import org.apache.hc.client5.http.impl.async.HttpAsyncClients
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.core5.concurrent.FutureCallback
import org.apache.hc.core5.reactor.IOReactorConfig
import org.apache.hc.core5.util.TimeValue
import org.apache.jmeter.protocol.http.control.Header
import org.apache.jmeter.protocol.http.control.HeaderManager
import org.apache.jmeter.protocol.http.util.HTTPConstants
import org.apache.jmeter.threads.JMeterContextService
import org.apache.jmeter.threads.JMeterVariables
import java.net.URI
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

const val VAR_APACHE_HTTPCLIENT5_CLIENT = "_jv_httpclient5_client"

private val reactor by lazy {
    IOReactorConfig.custom()
        .setIoThreadCount(10)
//        .setSoTimeout(5, TimeUnit.SECONDS)
        .build()
}
private val sharedHttpClient by lazy {
    HttpAsyncClients.custom()
        .setIOReactorConfig(reactor)
        .evictExpiredConnections()
        .setConnectionManager(
            PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnPerRoute(100500)
                .setMaxConnTotal(100500)
                .build()
        )
        .evictIdleConnections(TimeValue.of(5, TimeUnit.SECONDS))
        .build().also {
            it.start()
        }
}

private val JMeterVariables.httpClient: CloseableHttpAsyncClient
    get() = sharedHttpClient /* {
        val cache = getObject(VAR_APACHE_HTTPCLIENT5_CLIENT)
        if (cache != null) {
            return cache as CloseableHttpAsyncClient
        }
        return HttpAsyncClients.custom()
            .setIOReactorConfig(reactor)
            .evictExpiredConnections()
            .evictIdleConnections(TimeValue.of(5, TimeUnit.SECONDS))
            .build()
            .also {
                putObject(VAR_APACHE_HTTPCLIENT5_CLIENT, it)
                it.start()
            }
    }*/

private suspend fun CloseableHttpAsyncClient.send(sampleResult: HTTPSampleResult, uri: URI, maxLength: Int) =
    suspendCancellableCoroutine<SimpleHttpResponse> { continuation ->
        val future = execute(SimpleRequestProducer.create(
            SimpleHttpRequests.GET.create(uri)
        ), SimpleResponseConsumer.create(), object : FutureCallback<SimpleHttpResponse> {
            override fun cancelled() {
                continuation.cancel()
            }

            override fun completed(result: SimpleHttpResponse) {
                continuation.resume(result)
            }

            override fun failed(ex: java.lang.Exception?) {
                continuation.resumeWithException(ex ?: Exception("unknown exception"))
            }
        })
        continuation.cancelFutureOnCancellation(future)
    }

class HttpApacheHttpClient5Impl(testElement: HTTPSamplerBase) : HTTPAbstractImpl(testElement),
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
        val client = vars.httpClient

        try {
            val response = client.send(sampleResult, url.toURI(), 2 * 1024 * 1024)

            sampleResult.connectEnd()
            sampleResult.latencyEnd()
            sampleResult.sampleEnd()
            sampleResult.responseCode = response.code.toString()
            sampleResult.responseHeaders = response.headers.joinToString { it.toString() }
            if (sampleResult.isRedirect) {
                sampleResult.redirectLocation = response.getHeader(HTTPConstants.HEADER_LOCATION).value
            }
            sampleResult.isSuccessful = true
            val content = response.bodyBytes
            sampleResult.setBodySize(content.size.toLong())
            sampleResult.responseData = content
            response.contentType?.also { contentType ->
                sampleResult.setDataEncoding(contentType.charset?.name())
                val mediaType = contentType.mimeType
                sampleResult.dataType = when {
                    mediaType == null || HTTPSampleResult.isBinaryType(mediaType) -> HTTPSampleResult.BINARY
                    else -> HTTPSampleResult.TEXT
                }
            }
        } catch (e: Exception) {
            sampleResult.connectEnd()
            sampleResult.latencyEnd()
            sampleResult.sampleEnd()
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
