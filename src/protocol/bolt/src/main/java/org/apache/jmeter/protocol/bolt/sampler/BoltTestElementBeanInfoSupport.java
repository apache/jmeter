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

package org.apache.jmeter.protocol.bolt.sampler;

import java.beans.PropertyDescriptor;
import java.util.Arrays;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.neo4j.driver.AccessMode;

public abstract class BoltTestElementBeanInfoSupport extends BeanInfoSupport {
    /**
     * Construct a BeanInfo for the given class.
     *
     * @param beanClass class for which to construct a BeanInfo
     */
    protected BoltTestElementBeanInfoSupport(Class<? extends TestBean> beanClass) {
        super(beanClass);

        createPropertyGroup("query", new String[] { "cypher","params","recordQueryResults"});
        createPropertyGroup("options", new String[] { "accessMode","database", "txTimeout"});

        PropertyDescriptor propertyDescriptor = property("cypher", TypeEditor.TextAreaEditor);
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, "");

        propertyDescriptor = property("params", TypeEditor.TextAreaEditor);
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, "{\"paramName\":\"paramValue\"}");

        propertyDescriptor = property("recordQueryResults");
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, Boolean.FALSE);

        propertyDescriptor = property("accessMode", TypeEditor.ComboStringEditor);
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(NOT_EXPRESSION, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, AccessMode.WRITE.toString());
        propertyDescriptor.setValue(TAGS, getListAccessModes());

        propertyDescriptor = property("database", TypeEditor.ComboStringEditor);
        propertyDescriptor.setValue(DEFAULT, "neo4j");

        propertyDescriptor = property("txTimeout");
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, 60);
    }

    private static String[] getListAccessModes() {
        return Arrays.stream(AccessMode.values()).map(Enum::toString).toArray(String[]::new);
    }
}
