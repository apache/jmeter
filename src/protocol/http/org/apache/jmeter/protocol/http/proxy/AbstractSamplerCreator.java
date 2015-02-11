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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Base class for SamplerCreator
 */
public abstract class AbstractSamplerCreator implements SamplerCreator {

    protected static final String HTTP = "http"; // $NON-NLS-1$
    protected static final String HTTPS = "https"; // $NON-NLS-1$

    /** Filetype to be used for the temporary binary files*/
    private static final String binaryFileSuffix =
        JMeterUtils.getPropDefault("proxy.binary.filesuffix",// $NON-NLS-1$
                                   ".binary"); // $NON-NLS-1$

    /** Which content-types will be treated as binary (exact match) */
    private static final Set<String> binaryContentTypes = new HashSet<String>();

    /** Where to store the temporary binary files */
    private static final String binaryDirectory =
        JMeterUtils.getPropDefault("proxy.binary.directory",// $NON-NLS-1$
                System.getProperty("user.dir")); // $NON-NLS-1$ proxy.binary.filetype=binary

    static {
        String binaries = JMeterUtils.getPropDefault("proxy.binary.types", // $NON-NLS-1$
                "application/x-amf,application/x-java-serialized-object"); // $NON-NLS-1$
        if (binaries.length() > 0){
            StringTokenizer s = new StringTokenizer(binaries,"|, ");// $NON-NLS-1$
            while (s.hasMoreTokens()){
               binaryContentTypes.add(s.nextToken());
            }
        }
    }
    
    /*
     * Optionally number the requests
     */
    private static final boolean numberRequests =
        JMeterUtils.getPropDefault("proxy.number.requests", true); // $NON-NLS-1$

    private static volatile int requestNumber = 0;// running number
    

    /**
     * 
     */
    /**
     * 
     */
    public AbstractSamplerCreator() {
        super();
    }

    /**
     * @return int request number
     */
    protected static int getRequestNumber() {
        return requestNumber;
    }

    /**
     * Increment request number
     */
    protected static void incrementRequestNumber() {
        requestNumber++;
    }

    /**
     * @return boolean is numbering requests is required
     */
    protected static boolean isNumberRequests() {
        return numberRequests;
    }

    /**
     * @param contentType String content type
     * @return true if contentType is part of binary declared types
     */
    protected boolean isBinaryContent(String contentType) {
        if (contentType == null) {
            return false;
        }
        return binaryContentTypes.contains(contentType);
    }
    
    /**
     * @return String binary file suffix
     */
    protected String getBinaryFileSuffix() {
        return binaryFileSuffix;
    }

    /**
     * @return String binary directory
     */
    protected String getBinaryDirectory() {
        return binaryDirectory;
    }

    /**
     * @see org.apache.jmeter.protocol.http.proxy.SamplerCreator#postProcessSampler(org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase, org.apache.jmeter.samplers.SampleResult)
     */
    @Override
    public void postProcessSampler(HTTPSamplerBase sampler, SampleResult result) {
        // NOOP
    }

    /**
     * @see org.apache.jmeter.protocol.http.proxy.SamplerCreator#createAndPopulateSampler(org.apache.jmeter.protocol.http.proxy.HttpRequestHdr, java.util.Map, java.util.Map)
     */
    @Override
    public HTTPSamplerBase createAndPopulateSampler(HttpRequestHdr request,
            Map<String, String> pageEncodings, Map<String, String> formEncodings) throws Exception {
        HTTPSamplerBase sampler = createSampler(request, pageEncodings, formEncodings);
        populateSampler(sampler, request, pageEncodings, formEncodings);
        return sampler;
    }

    /**
     * Default implementation returns an empty list
     * @see SamplerCreator#createChildren(HTTPSamplerBase, SampleResult)
     */
    @Override
    public List<TestElement> createChildren(HTTPSamplerBase sampler, SampleResult result) {
        return Collections.emptyList();
    }
}
