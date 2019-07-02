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

package org.apache.jmeter.save;

import org.apache.jmeter.save.converters.ConversionHelp;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Handles XStream conversion of Test Scripts
 */
public class ScriptWrapperConverter implements Converter {

    private static final String ATT_PROPERTIES = "properties"; // $NON-NLS-1$
    private static final String ATT_VERSION = "version"; // $NON-NLS-1$
    private static final String ATT_JMETER = "jmeter"; // $NON-NLS-1$

    /**
     * Returns the converter version; used to check for possible
     * incompatibilities
     *
     * @return the version of the converter
     */
    public static String getVersion() {
        return "$Revision$"; // $NON-NLS-1$
    }

    private final Mapper classMapper;

    public ScriptWrapperConverter(Mapper classMapper) {
        this.classMapper = classMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg0) { // superclass is not typed
        return arg0.equals(ScriptWrapper.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context) {
        ScriptWrapper wrap = (ScriptWrapper) arg0;
        String version = SaveService.getVERSION();
        ConversionHelp.setOutVersion(version);// Ensure output follows version
        writer.addAttribute(ATT_VERSION, version);
        writer.addAttribute(ATT_PROPERTIES, SaveService.getPropertiesVersion());
        writer.addAttribute(ATT_JMETER, JMeterUtils.getJMeterVersion());
        writer.startNode(classMapper.serializedClass(wrap.testPlan.getClass()));
        context.convertAnother(wrap.testPlan);
        writer.endNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ScriptWrapper wrap = new ScriptWrapper();
        wrap.version = reader.getAttribute(ATT_VERSION);
        ConversionHelp.setInVersion(wrap.version);// Make sure decoding
                                                    // follows input file
        reader.moveDown();
        // Catch errors and rethrow as ConversionException so we get location details
        try {
            wrap.testPlan = (HashTree) context.convertAnother(wrap, getNextType(reader));
        } catch (NoClassDefFoundError | Exception e) {
            throw createConversionException(e);
        }
        return wrap;
    }

    private ConversionException createConversionException(Throwable e) {
        final ConversionException conversionException = new ConversionException(e);
        StackTraceElement[] ste = e.getStackTrace();
        if (ste!=null){
            for(StackTraceElement top : ste){
                String className=top.getClassName();
                if (className.startsWith("org.apache.jmeter.")){
                    conversionException.add("first-jmeter-class", top.toString());
                    break;
                }
            }
        }
        return conversionException;
    }

    protected Class<?> getNextType(HierarchicalStreamReader reader) {
        String classAttribute = reader.getAttribute(ConversionHelp.ATT_CLASS);
        Class<?> type;
        if (classAttribute == null) {
            type = classMapper.realClass(reader.getNodeName());
        } else {
            type = classMapper.realClass(classAttribute);
        }
        return type;
    }
}
