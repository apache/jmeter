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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.log.Logger;

/**
 * {@link SamplerCreator} factory
 */
public class SamplerCreatorFactory {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final SamplerCreator DEFAULT_SAMPLER_CREATOR = new DefaultSamplerCreator();

    private final Map<String, SamplerCreator> samplerCreatorMap = new HashMap<String, SamplerCreator>();

    /**
     * 
     */
    public SamplerCreatorFactory() {
        init();
    }
    
    /**
     * Initialize factory from classpath
     */
    private void init() {
        try {
            List<String> listClasses = ClassFinder.findClassesThatExtend(
                    JMeterUtils.getSearchPaths(), 
                    new Class[] {SamplerCreator.class }); 
            for (String strClassName : listClasses) {
                try {
                    if(log.isDebugEnabled()) {
                        log.debug("Loading class: "+ strClassName);
                    }
                    Class<?> commandClass = Class.forName(strClassName);
                    if (!Modifier.isAbstract(commandClass.getModifiers())) {
                        if(log.isDebugEnabled()) {
                            log.debug("Instantiating: "+ commandClass.getName());
                        }
                            SamplerCreator creator = (SamplerCreator) commandClass.newInstance();
                            String[] contentTypes = creator.getManagedContentTypes();
                            for (String contentType : contentTypes) {
                                if(log.isDebugEnabled()) {
                                    log.debug("Registering samplerCreator "+commandClass.getName()+" for content type:"+contentType);
                                }
                                SamplerCreator oldSamplerCreator = samplerCreatorMap.put(contentType, creator);
                                if(oldSamplerCreator!=null) {
                                    log.warn("A sampler creator was already registered for:"+contentType+", class:"+oldSamplerCreator.getClass()
                                            + ", it will be replaced");
                                }
                            }                        
                    }
                } catch (Exception e) {
                    log.error("Exception registering "+SamplerCreator.class.getName() + " with implementation:"+strClassName, e);
                }
            }
        } catch (IOException e) {
            log.error("Exception finding implementations of "+SamplerCreator.class, e);
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
