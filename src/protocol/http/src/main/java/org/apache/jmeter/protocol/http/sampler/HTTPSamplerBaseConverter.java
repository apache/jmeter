/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 * Created on Sep 14, 2004
 *
 */
package org.apache.jmeter.protocol.http.sampler;

import org.apache.jmeter.save.converters.TestElementConverter;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Class for XStream conversion of HTTPResult
 *
 */
public class HTTPSamplerBaseConverter extends TestElementConverter {

    /**
     * Returns the converter version; used to check for possible
     * incompatibilities
     *
     * @return the version of this component
     */
    public static String getVersion() {
        return "$Revision$";  //$NON-NLS-1$
    }

    public HTTPSamplerBaseConverter(Mapper arg0) {
        super(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg0) { // superclass does not support types
        return HTTPSamplerBase.class.isAssignableFrom(arg0);
    }

    /**
     * Override TestElementConverter; convert HTTPSamplerBase to merge
     * the two means of providing file names into a single list.
     *
     * {@inheritDoc}
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final HTTPSamplerBase httpSampler = (HTTPSamplerBase) super.unmarshal(reader, context);
        // Help convert existing JMX files which use HTTPSampler[2] nodes
        String nodeName = reader.getNodeName();
        if (nodeName.equals(HTTPSamplerFactory.HTTP_SAMPLER_JAVA)){
            httpSampler.setImplementation(HTTPSamplerFactory.IMPL_JAVA);
        }
        if (nodeName.equals(HTTPSamplerFactory.HTTP_SAMPLER_APACHE)){
            httpSampler.setImplementation(HTTPSamplerFactory.IMPL_HTTP_CLIENT4);
        }
        httpSampler.mergeFileProperties();
        return httpSampler;
    }
}
