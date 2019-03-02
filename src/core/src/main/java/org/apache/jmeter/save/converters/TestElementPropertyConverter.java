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

package org.apache.jmeter.save.converters;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class TestElementPropertyConverter extends AbstractCollectionConverter {
    private static final Logger log = LoggerFactory.getLogger(TestElementPropertyConverter.class);

    private static final String HEADER_CLASSNAME
        = "org.apache.jmeter.protocol.http.control.Header"; // $NON-NLS-1$

    /**
     * Returns the converter version; used to check for possible
     * incompatibilities
     *
     * @return the version of this converter
     */
    public static String getVersion() {
        return "$Revision$"; // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg0) { // superclass does not use types
        return TestElementProperty.class.equals(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context) {
        TestElementProperty prop = (TestElementProperty) arg0;
        writer.addAttribute(ConversionHelp.ATT_NAME, ConversionHelp.encode(prop.getName()));
        Class<?> clazz = prop.getObjectValue().getClass();
        writer.addAttribute(ConversionHelp.ATT_ELEMENT_TYPE,
                mapper().serializedClass(clazz));
        TestElement te = (TestElement)prop.getObjectValue();
        ConversionHelp.saveSpecialProperties(te,writer);
        for (JMeterProperty jmp : prop) {
            // Skip special properties if required
            if (!ConversionHelp.isSpecialProperty(jmp.getName()))
            {
                // Don't save empty comments
                if (!(TestElement.COMMENTS.equals(jmp.getName())
                        && jmp.getStringValue().isEmpty()))
                {
                    writeItem(jmp, context, writer);
                }
            }
        }
        //TODO clazz is probably always the same as testclass
    }

    /*
     * TODO - convert to work more like upgrade.properties/NameUpdater.java
     *
     * Special processing is carried out for the Header Class The String
     * property TestElement.name is converted to Header.name for example:
     * <elementProp name="User-Agent"
     * elementType="org.apache.jmeter.protocol.http.control.Header"> <stringProp
     * name="Header.value">Mozilla%2F4.0+%28compatible%3B+MSIE+5.5%3B+Windows+98%29</stringProp>
     * <stringProp name="TestElement.name">User-Agent</stringProp>
     * </elementProp> becomes <elementProp name="User-Agent"
     * elementType="org.apache.jmeter.protocol.http.control.Header"> <stringProp
     * name="Header.value">Mozilla%2F4.0+%28compatible%3B+MSIE+5.5%3B+Windows+98%29</stringProp>
     * <stringProp name="Header.name">User-Agent</stringProp> </elementProp>
     */
    /** {@inheritDoc} */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        try {
            TestElementProperty prop = (TestElementProperty) createCollection(context.getRequiredType());
            prop.setName(ConversionHelp.decode(reader.getAttribute(ConversionHelp.ATT_NAME)));
            String element = reader.getAttribute(ConversionHelp.ATT_ELEMENT_TYPE);
            boolean isHeader = HEADER_CLASSNAME.equals(element);
            prop.setObjectValue(mapper().realClass(element).getDeclaredConstructor().newInstance());// Always decode
            TestElement te = (TestElement)prop.getObjectValue();
            // No need to check version, just process the attributes if present
            ConversionHelp.restoreSpecialProperties(te, reader);
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                JMeterProperty subProp = (JMeterProperty) readItem(reader, context, prop);
                if (subProp != null) { // could be null if it has been deleted via NameUpdater
                    if (isHeader) {
                        String name = subProp.getName();
                        if (TestElement.NAME.equals(name)) {
                            subProp.setName("Header.name");// $NON-NLS-1$
                            // Must be same as Header.HNAME - but that is built
                            // later
                        }
                    }
                    prop.addProperty(subProp);
                }
                reader.moveUp();
            }
            return prop;
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            log.error("Couldn't unmarshall TestElementProperty", e);
            return new TestElementProperty("ERROR", new ConfigTestElement());// $NON-NLS-1$
        }
    }

    /**
     * @param arg0 the mapper
     */
    public TestElementPropertyConverter(Mapper arg0) {
        super(arg0);
    }
}
