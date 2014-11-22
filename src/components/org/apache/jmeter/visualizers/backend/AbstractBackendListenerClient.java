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

package org.apache.jmeter.visualizers.backend;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * An abstract implementation of the BackendListenerClient interface. This
 * implementation provides default implementations of most of the methods in the
 * interface, as well as some convenience methods, in order to simplify
 * development of BackendListenerClient implementations.
 * 
 * While it may be necessary to make changes to the BackendListenerClient interface
 * from time to time (therefore requiring changes to any implementations of this
 * interface), we intend to make this abstract class provide reasonable
 * implementations of any new methods so that subclasses do not necessarily need
 * to be updated for new versions. Therefore, when creating a new
 * BackendListenerClient implementation, developers are encouraged to subclass this
 * abstract class rather than implementing the BackendListenerClient interface
 * directly. Implementing BackendListenerClient directly will continue to be
 * supported for cases where extending this class is not possible (for example,
 * when the client class is already a subclass of some other class).
 * <p>
 * The handleSampleResult() method of BackendListenerClient does not have a default
 * implementation here, so subclasses must define at least this method. It may
 * be useful to override other methods as well.
 *
 * @see BackendListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
 * @since 2.13
 */
public abstract class AbstractBackendListenerClient implements BackendListenerClient {

    private static final Logger LOGGER = LoggingManager.getLoggerForClass();
    
    private ConcurrentHashMap<String, SamplerMetric> metricsPerSampler = new ConcurrentHashMap<String, SamplerMetric>();

    /* Implements BackendListenerClient.setupTest(BackendListenerContext) */
    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        LOGGER.debug(getClass().getName() + ": setupTest");
    }

    /* Implements BackendListenerClient.teardownTest(BackendListenerContext) */
    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        LOGGER.debug(getClass().getName() + ": teardownTest");
        metricsPerSampler.clear();
    }

    /* Implements BackendListenerClient.getDefaultParameters() */
    @Override
    public Arguments getDefaultParameters() {
        return null;
    }

    /**
     * Get a Logger instance which can be used by subclasses to log information.
     *
     * @return a Logger instance which can be used for logging
     */
    protected Logger getLogger() {
        return LOGGER;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.backend.BackendListenerClient#createSampleResult(org.apache.jmeter.samplers.SampleResult)
     */
    @Override
    public SampleResult createSampleResult(BackendListenerContext context, SampleResult result) {
        SampleResult sampleResult = (SampleResult) result.clone();
        return sampleResult;
    }

    /**
     * 
     * @param sampleLabel
     * @return SamplerMetric
     */
    protected SamplerMetric getSamplerMetric(String sampleLabel) {
        SamplerMetric samplerMetric = metricsPerSampler.get(sampleLabel);
        if(samplerMetric == null) {
            samplerMetric = new SamplerMetric();
            SamplerMetric oldValue = metricsPerSampler.putIfAbsent(sampleLabel, samplerMetric);
            if(oldValue != null ){
                samplerMetric = oldValue;
            }
        }
        return samplerMetric;
    }
    
    /**
     * 
     * @return Map<String, SamplerMetric>
     */
    protected Map<String, SamplerMetric> getMetricsPerSampler() {
        return metricsPerSampler;
    }

}
