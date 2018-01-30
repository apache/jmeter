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

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * Factory to return the appropriate HTTPSampler for use with classes that need
 * an HTTPSampler; also creates the implementations for use with HTTPSamplerProxy.
 *
 */
public final class HTTPSamplerFactory {

    // N.B. These values are used in jmeter.properties (jmeter.httpsampler) - do not change
    // They can also be used as the implementation name
    /** Use the default Java HTTP implementation */
    public static final String HTTP_SAMPLER_JAVA = "HTTPSampler"; //$NON-NLS-1$

    /** Use Apache HTTPClient HTTP implementation */
    public static final String HTTP_SAMPLER_APACHE = "HTTPSampler2"; //$NON-NLS-1$

    //+ JMX implementation attribute values (also displayed in GUI) - do not change
    public static final String IMPL_HTTP_CLIENT4 = "HttpClient4";  // $NON-NLS-1$

    public static final String IMPL_HTTP_CLIENT3_1 = "HttpClient3.1"; // $NON-NLS-1$
    
    public static final String IMPL_JAVA = "Java"; // $NON-NLS-1$
    //- JMX

    public static final String DEFAULT_CLASSNAME =
        JMeterUtils.getPropDefault("jmeter.httpsampler", IMPL_HTTP_CLIENT4); //$NON-NLS-1$

    private HTTPSamplerFactory() {
        // Not intended to be instantiated
    }

    /**
     * Create a new instance of the default sampler
     *
     * @return instance of default sampler
     */
    public static HTTPSamplerBase newInstance() {
        return newInstance(DEFAULT_CLASSNAME);
    }

    /**
     * Create a new instance of the required sampler type
     *
     * @param alias HTTP_SAMPLER or HTTP_SAMPLER_APACHE or IMPL_HTTP_CLIENT3_1 or IMPL_HTTP_CLIENT4
     * @return the appropriate sampler
     * @throws UnsupportedOperationException if alias is not recognised
     */
    public static HTTPSamplerBase newInstance(String alias) {
        if (alias ==null || alias.length() == 0) {
            return new HTTPSamplerProxy();
        }
        if (alias.equals(HTTP_SAMPLER_JAVA) || alias.equals(IMPL_JAVA)) {
            return new HTTPSamplerProxy(IMPL_JAVA);
        }
        if (alias.equals(IMPL_HTTP_CLIENT4) || alias.equals(HTTP_SAMPLER_APACHE) || alias.equals(IMPL_HTTP_CLIENT3_1)) {
            return new HTTPSamplerProxy(IMPL_HTTP_CLIENT4);
        }
        throw new IllegalArgumentException("Unknown sampler type: '" + alias+"'");
    }

    public static String[] getImplementations(){
        return new String[]{IMPL_HTTP_CLIENT4,IMPL_JAVA};
    }

    public static HTTPAbstractImpl getImplementation(String impl, HTTPSamplerBase base){
        if (HTTPSamplerBase.PROTOCOL_FILE.equals(base.getProtocol())) {
            return new HTTPFileImpl(base);
        }
        if (JOrphanUtils.isBlank(impl)){
            impl = DEFAULT_CLASSNAME;
        }
        if (IMPL_JAVA.equals(impl) || HTTP_SAMPLER_JAVA.equals(impl)) {
            return new HTTPJavaImpl(base);
        } else if (IMPL_HTTP_CLIENT4.equals(impl) || IMPL_HTTP_CLIENT3_1.equals(impl)) {
            return new HTTPHC4Impl(base);
        } else {
            throw new IllegalArgumentException("Unknown implementation type: '"+impl+"'");
        }
    }

}
