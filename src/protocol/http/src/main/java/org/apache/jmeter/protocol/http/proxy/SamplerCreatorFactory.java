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

package org.apache.jmeter.protocol.http.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.LogAndIgnoreServiceLoadExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SamplerCreator} factory
 */
public class SamplerCreatorFactory {
    private static final Logger log = LoggerFactory.getLogger(SamplerCreatorFactory.class);

    private static final SamplerCreator DEFAULT_SAMPLER_CREATOR = new DefaultSamplerCreator();

    private final Map<String, SamplerCreator> samplerCreatorMap = new HashMap<>();

    public SamplerCreatorFactory() {
        init();
    }

    /**
     * Set the counter for all available {@link SamplerCreator}s.
     * <p>
     * <em>The only implementation that is currently available, increments the counter before it is used!</em>
     * @param value to initialize the creators
     */
    public void setCounter(int value) {
        DEFAULT_SAMPLER_CREATOR.setCounter(value);
        for (SamplerCreator samplerCreator: samplerCreatorMap.values()) {
            samplerCreator.setCounter(value);
        }
    }

    /**
     * Initialize factory from classpath
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        for (SamplerCreator creator : JMeterUtils.loadServicesAndScanJars(
                SamplerCreator.class,
                ServiceLoader.load(SamplerCreator.class),
                Thread.currentThread().getContextClassLoader(),
                new LogAndIgnoreServiceLoadExceptionHandler(log)
        )) {
            try {
                String[] contentTypes = creator.getManagedContentTypes();
                for (String contentType : contentTypes) {
                    log.debug("Registering samplerCreator {} for content type:{}",
                            creator.getClass().getName(), contentType);
                    SamplerCreator oldSamplerCreator = samplerCreatorMap.put(contentType, creator);
                    if (oldSamplerCreator != null) {
                        log.warn("A sampler creator was already registered for:{}, class:{}, it will be replaced",
                                contentType, oldSamplerCreator.getClass());
                    }
                }
            } catch (Exception e) {
                log.error("Exception registering {} with implementation:{}",
                        SamplerCreator.class.getName(), creator.getClass(), e);
            }
        }
    }

    /**
     * Gets {@link SamplerCreator} for content type, if none is found returns {@link DefaultSamplerCreator}
     * @param request {@link HttpRequestHdr} from which the content type should be used
     * @param pageEncodings Map of pageEncodings
     * @param formEncodings  Map of formEncodings
     * @return SamplerCreator for the content type of the <code>request</code>, or {@link DefaultSamplerCreator} when none is found
     */
    public SamplerCreator getSamplerCreator(HttpRequestHdr request,
            Map<String, String> pageEncodings, Map<String, String> formEncodings) {
        SamplerCreator creator = samplerCreatorMap.get(request.getContentType());
        if(creator == null) {
            return DEFAULT_SAMPLER_CREATOR;
        }
        return creator;
    }
}
