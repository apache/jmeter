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

package org.apache.jmeter.protocol.bolt.config;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoltConnectionElementBeanInfo extends BeanInfoSupport {

    private static final Logger log = LoggerFactory.getLogger(BoltConnectionElementBeanInfo.class);

    public BoltConnectionElementBeanInfo() {
        super(BoltConnectionElement.class);

        createPropertyGroup("connection", new String[] { "boltUri", "username", "password", "maxConnectionPoolSize" });

        PropertyDescriptor propertyDescriptor =  property("boltUri");
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, "bolt://localhost:7687");
        propertyDescriptor = property("username");
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, "neo4j");
        propertyDescriptor = property("password", TypeEditor.PasswordEditor);
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, "");
        propertyDescriptor = property("maxConnectionPoolSize");
        propertyDescriptor.setValue(NOT_UNDEFINED, Boolean.TRUE);
        propertyDescriptor.setValue(DEFAULT, 100);

        if(log.isDebugEnabled()) {
            String descriptorsAsString = Arrays.stream(getPropertyDescriptors())
                    .map(pd -> pd.getName() + "=" + pd.getDisplayName())
                    .collect(Collectors.joining(", "));
            log.debug(descriptorsAsString);
        }

    }
}
