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

import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class TestElementConverter extends AbstractCollectionConverter {
    private static final Logger log = LoggingManager.getLoggerForClass();


    /**
     * Returns the converter version; used to check for possible
     * incompatibilities
     */
    public static String getVersion() {
        return "$Revision$"; //$NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg0) { // superclass does not use types
        return TestElement.class.isAssignableFrom(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context) {
        TestElement el = (TestElement) arg0;
        if (SaveService.IS_TESTPLAN_FORMAT_22){
            ConversionHelp.saveSpecialProperties(el,writer);
        }
        PropertyIterator iter = el.propertyIterator();
        while (iter.hasNext()) {
            JMeterProperty jmp=iter.next();
            // Skip special properties if required
            if (!SaveService.IS_TESTPLAN_FORMAT_22 || !ConversionHelp.isSpecialProperty(jmp.getName())) {
                // Don't save empty comments - except for the TestPlan (to maintain compatibility)
                   if (!(
                           TestElement.COMMENTS.equals(jmp.getName())
                           && jmp.getStringValue().length()==0
                           && !el.getClass().equals(TestPlan.class)
                       ))
                   {
                    writeItem(jmp, context, writer);
                   }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        String classAttribute = reader.getAttribute(ConversionHelp.ATT_CLASS);
        Class<?> type;
        if (classAttribute == null) {
            type = mapper().realClass(reader.getNodeName());
        } else {
            type = mapper().realClass(classAttribute);
        }
        try {
            TestElement el = (TestElement) type.newInstance();
            // No need to check version, just process the attributes if present
            ConversionHelp.restoreSpecialProperties(el, reader);
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                JMeterProperty prop = (JMeterProperty) readItem(reader, context, el);
                el.setProperty(prop);
                reader.moveUp();
            }
            return el;
        } catch (Exception e) {
            log.error("TestElement not instantiable: " + type, e);
            return null;
        }
    }

    /**
     * @param arg0
     */
    public TestElementConverter(Mapper arg0) {
        super(arg0);
    }
}
