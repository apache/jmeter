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
 */
package org.apache.jmeter.protocol.http.sampler;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.Interruptible;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 * This sampler uses the Apache HttpClient implementation
 */
class HTTPSampler3 extends HTTPSamplerBase implements Interruptible {

    private static final long serialVersionUID = 241L;

    private final transient HTTPHC4Impl hc;

    public HTTPSampler3(){
        hc = new HTTPHC4Impl(this);
    }

    @Override
    public boolean interrupt() {
        return hc.interrupt();
    }

    @Override
    protected HTTPSampleResult sample(java.net.URL u, String method,
            boolean areFollowingRedirect, int depth) {
        return hc.sample(u, method, areFollowingRedirect, depth);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    @Override
    public void testIterationStart(LoopIterationEvent event) {
        hc.notifyFirstSampleAfterLoopRestart();
    }
}
