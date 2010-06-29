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

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MultiPropertyConverter extends AbstractCollectionConverter {

    private static final String ATT_NAME = "name";  //$NON-NLS-1$

    /**
     * Returns the converter version; used to check for possible
     * incompatibilities
     */
    public static String getVersion() {
        return "$Revision$";  //$NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg0) { // superclass does not use types
        return arg0.equals(CollectionProperty.class) || arg0.equals(MapProperty.class);
    }

    /** {@inheritDoc} */
    @Override
    public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context) {
        MultiProperty prop = (MultiProperty) arg0;
        writer.addAttribute(ATT_NAME, ConversionHelp.encode(prop.getName()));
        PropertyIterator iter = prop.iterator();
        while (iter.hasNext()) {
            writeItem(iter.next(), context, writer);
        }

    }

    /** {@inheritDoc} */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        MultiProperty prop = (MultiProperty) createCollection(context.getRequiredType());
        prop.setName(ConversionHelp.decode(reader.getAttribute(ATT_NAME)));
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            JMeterProperty subProp = (JMeterProperty) readItem(reader, context, prop);
            prop.addProperty(subProp);
            reader.moveUp();
        }
        return prop;
    }

    /**
     * @param arg0
     */
    public MultiPropertyConverter(Mapper arg0) {
        super(arg0);
    }
}
