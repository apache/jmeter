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
package org.apache.jmeter.protocol.http.util;

import java.net.URL;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.save.converters.SampleResultConverter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Class for XStream conversion of HTTPResult
 */
public class HTTPResultConverter extends SampleResultConverter {

    /**
     * Returns the converter version; used to check for possible
     * incompatibilities
     *
     * @return the version of this component
     */
    public static String getVersion() {
        return "$Revision$";  //$NON-NLS-1$
    }

    /**
     * @param arg0 the mapper
     */
    public HTTPResultConverter(Mapper arg0) {
        super(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg0) { // superclass does not support types
        return HTTPSampleResult.class.equals(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        HTTPSampleResult res = (HTTPSampleResult) obj;
        SampleSaveConfiguration save = res.getSaveConfig();
        setAttributes(writer, context, res, save);
        saveAssertions(writer, context, res, save);
        saveSubResults(writer, context, res, save);
        saveResponseHeaders(writer, context, res, save);
        saveRequestHeaders(writer, context, res, save);
        saveResponseData(writer, context, res, save);
        saveSamplerData(writer, context, res, save);
    }

    private void saveSamplerData(HierarchicalStreamWriter writer, MarshallingContext context, HTTPSampleResult res,
            SampleSaveConfiguration save) {
        if (save.saveSamplerData(res)) {
            writeString(writer, TAG_COOKIES, res.getCookies());
            writeString(writer, TAG_METHOD, res.getHTTPMethod());
            writeString(writer, TAG_QUERY_STRING, res.getQueryString());
            writeString(writer, TAG_REDIRECT_LOCATION, res.getRedirectLocation());
        }
        if (save.saveUrl()) {
            writeItem(res.getURL(), context, writer);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        HTTPSampleResult res = (HTTPSampleResult) createCollection(context.getRequiredType());
        retrieveAttributes(reader, context, res);
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            Object subItem = readItem(reader, context, res);
            if (!retrieveItem(reader, context, res, subItem)) {
                retrieveHTTPItem(reader, res, subItem);
            }
            reader.moveUp();
        }

        // If we have a file, but no data, then read the file
        String resultFileName = res.getResultFileName();
        if (resultFileName.length()>0
        &&  res.getResponseData().length == 0) {
            readFile(resultFileName,res);
        }
        return res;
    }

    private void retrieveHTTPItem(HierarchicalStreamReader reader,
            HTTPSampleResult res, Object subItem) {
        if (subItem instanceof URL) {
            res.setURL((URL) subItem);
        } else {
            String nodeName = reader.getNodeName();
            if (nodeName.equals(TAG_COOKIES)) {
                res.setCookies((String) subItem);
            } else if (nodeName.equals(TAG_METHOD)) {
                res.setHTTPMethod((String) subItem);
            } else if (nodeName.equals(TAG_QUERY_STRING)) {
                res.setQueryString((String) subItem);
            } else if (nodeName.equals(TAG_REDIRECT_LOCATION)) {
                res.setRedirectLocation((String) subItem);
            }
        }
    }
}
