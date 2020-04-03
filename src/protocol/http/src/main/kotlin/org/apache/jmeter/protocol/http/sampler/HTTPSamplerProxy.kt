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

import org.apache.jmeter.engine.event.LoopIterationEvent
import org.apache.jmeter.samplers.Interruptible
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.samplers.SuspendingSampler
import java.net.URL

/**
 * Proxy class that dispatches to the appropriate HTTP sampler.
 *
 *
 * This class is stored in the test plan, and holds all the configuration settings.
 * The actual implementation is created at run-time, and is passed a reference to this class
 * so it can get access to all the settings stored by HTTPSamplerProxy.
 */
class HTTPSamplerProxy : HTTPSamplerBase, Interruptible, SuspendingHttpSampler, SuspendingSampler {
    @Transient
    private var impl: HTTPAbstractImpl? = null

    constructor() : super() {}

    /**
     * Convenience method used to initialise the implementation.
     *
     * @param impl the implementation to use.
     */
    constructor(impl: String) : super() {
        implementation = impl
    }

    /** {@inheritDoc}  */
    override fun sample(
        u: URL,
        method: String,
        areFollowingRedirect: Boolean,
        depth: Int
    ): HTTPSampleResult {
        // When Retrieve Embedded resources + Concurrent Pool is used
        // as the instance of Proxy is cloned, we end up with impl being null
        // testIterationStart will not be executed but it's not a problem for 51380 as it's download of resources
        // so SSL context is to be reused
        if (impl == null) { // Not called from multiple threads, so this is OK
            try {
                impl = HTTPSamplerFactory.getImplementation(implementation, this)
            } catch (ex: Exception) {
                return errorResult(ex, HTTPSampleResult())
            }
        }
        return impl!!.sample(u, method, areFollowingRedirect, depth)
    }

    override suspend fun suspendingSample(
        url: URL,
        method: String,
        followRedirects: Boolean,
        depth: Int
    ): HTTPSampleResult {
        if (impl == null) { // Not called from multiple threads, so this is OK
            try {
                impl = HTTPSamplerFactory.getImplementation(implementation, this)
            } catch (ex: Exception) {
                return errorResult(ex, HTTPSampleResult())
            }
        }
        val impl = impl
        if (impl is SuspendingHttpSampler) {
            return impl.suspendingSample(url, method, followRedirects, depth)
        }
        return impl!!.sample(url, method, followRedirects, depth)
    }

    override suspend fun suspendingSample(): SampleResult = try {
        suspendingSample(url, method, false, 0)
    } catch (e: Exception) {
        errorResult(e, HTTPSampleResult())
    }.also {
        it.sampleLabel = name
    }

    // N.B. It's not possible to forward threadStarted() to the implementation class.
    // This is because Config items are not processed until later, and HTTPDefaults may define the implementation

    override fun threadFinished() {
        impl?.threadFinished()
    }

    override fun interrupt() = impl?.interrupt() ?: false

    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    override fun testIterationStart(event: LoopIterationEvent) {
        impl?.notifyFirstSampleAfterLoopRestart()
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
