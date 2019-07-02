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

import org.apache.jmeter.testelement.property.LongProperty;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class LongPropertyConverter implements Converter {

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
        return LongProperty.class.equals(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext arg2) {
        LongProperty prop = (LongProperty) obj;
        writer.addAttribute(ConversionHelp.ATT_NAME, ConversionHelp.encode(prop.getName()));
        writer.setValue(prop.getStringValue());
    }

    /** {@inheritDoc} */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final String name = ConversionHelp.getPropertyName(reader, context);
        if (name == null) {
            return null;
        }
        LongProperty prop = new LongProperty(name, Long.parseLong(reader.getValue()));
        return prop;
    }
}
