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
 * Created on May 15, 2004
 */
package org.apache.jmeter.protocol.jini.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class JiniBeanInfo extends BeanInfoSupport {

    public JiniBeanInfo() {
        super(JiniTestElement.class);

        createPropertyGroup("varName", new String[] { "remoteServiceConfiguration" });

        createPropertyGroup("remoteServiceDetails", new String[] { "rmiRegistryUrl", "serviceName", "serviceInterface" });

        createPropertyGroup("methodDetails", new String[] { "methodName", "methodParamTypes", "methodArguments" });

        PropertyDescriptor p = property("remoteServiceConfiguration");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p = property("rmiRegistryUrl");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "jini://localhost:4160");
        p = property("serviceName");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "MyService");
        p = property("serviceInterface");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "com.foo.MyService");
        p = property("methodName");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "someMethod");
        p = property("methodParamTypes");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "java.lang.Long,java.lang.String");
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);

        p = property("methodArguments");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "1,abc");
    }

}
