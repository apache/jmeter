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

package org.apache.jmeter.protocol.http.sampler;

import java.net.URL;

/**
 * Dummy HTTPSampler class for use by classes that need an HTTPSampler, but that
 * don't need an actual sampler, e.g. for Parsing testing.
 */
public final class HTTPNullSampler extends HTTPSamplerBase {

    private static final long serialVersionUID = 240L;

    /**
     * Returns a sample Result with the request fields filled in.
     * 
     * {@inheritDoc}
     */
    @Override
    protected HTTPSampleResult sample(URL u, String method, boolean areFollowingRedirec, int depth) {
        HTTPSampleResult res = new HTTPSampleResult();
        res.sampleStart();
        res.setURL(u);
        res.sampleEnd();
        return res;
//        throw new UnsupportedOperationException("For test purposes only");
    }

}
