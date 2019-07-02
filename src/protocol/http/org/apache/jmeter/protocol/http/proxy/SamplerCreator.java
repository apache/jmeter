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

package org.apache.jmeter.protocol.http.proxy;

import java.util.List;
import java.util.Map;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;

/**
 * Factory of sampler
 */
public interface SamplerCreator {

    /**
     * @return String[] array of Content types managed by Factory
     */
    String[] getManagedContentTypes();

    /**
     * Create HTTPSamplerBase
     * @param request {@link HttpRequestHdr}
     * @param pageEncodings Map of page encodings
     * @param formEncodings Map of form encodings
     * @return {@link HTTPSamplerBase}
     */
    HTTPSamplerBase createSampler(HttpRequestHdr request,
            Map<String, String> pageEncodings, Map<String, String> formEncodings);

    /**
     * Populate sampler from request
     * @param sampler {@link HTTPSamplerBase}
     * @param request {@link HttpRequestHdr}
     * @param pageEncodings Map of page encodings
     * @param formEncodings Map of form encodings
     * @throws Exception when something fails
     */
    void populateSampler(HTTPSamplerBase sampler,
            HttpRequestHdr request, Map<String, String> pageEncodings,
            Map<String, String> formEncodings)
                    throws Exception;

    /**
     * Post process sampler
     * Called after sampling
     * @param sampler HTTPSamplerBase
     * @param result SampleResult
     * @since 2.9
     */
    void postProcessSampler(HTTPSamplerBase sampler, SampleResult result);

    /**
     * Default implementation calls:
     * <ol>
     *  <li>{@link SamplerCreator}{@link #createSampler(HttpRequestHdr, Map, Map)}</li>
     *  <li>{@link SamplerCreator}{@link #populateSampler(HTTPSamplerBase, HttpRequestHdr, Map, Map)}</li>
     * </ol>
     * @param request {@link HttpRequestHdr}
     * @param pageEncodings Map of page encodings
     * @param formEncodings Map of form encodings
     * @return {@link HTTPSamplerBase}
     * @throws Exception when something fails
     * @since 2.9
     */
    HTTPSamplerBase createAndPopulateSampler(HttpRequestHdr request,
            Map<String, String> pageEncodings, Map<String, String> formEncodings)
                    throws Exception;

    /**
     * Create sampler children.
     * This method can be used to add PostProcessor or ResponseAssertions by
     * implementations of {@link SamplerCreator}.
     * Return empty list if nothing to create
     * @param sampler {@link HTTPSamplerBase}
     * @param result {@link SampleResult}
     * @return List
     */
    List<TestElement> createChildren(HTTPSamplerBase sampler, SampleResult result);
}
