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

package org.apache.jmeter.examples.testbeans.example3;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class Example3BeanInfo extends BeanInfoSupport {

    private PropertyDescriptor getprop(String name) {
        final PropertyDescriptor property = property(name);
        property.setValue(NOT_UNDEFINED, Boolean.FALSE); // Ensure it is not flagged as 'unconfigured'
        return property;
    }

    private PropertyDescriptor getprop(String name, Object deflt) {
        PropertyDescriptor p = property(name);
        p.setValue(DEFAULT, deflt);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        return p;
    }

    public Example3BeanInfo() {
        super(Example3.class);
        getprop("mybool", Boolean.TRUE); // Must use defaults for primitive types
        getprop("myBoolean1");
        getprop("myBoolean2", Boolean.TRUE);
        getprop("myInt", Integer.valueOf(77)); // Must use defaults for primitive types
        getprop("myInteger1");
        getprop("myInteger2", Integer.valueOf(123));
        getprop("mylong", Long.valueOf(99)); // Must use defaults for primitive types
        getprop("myLong1");
        getprop("myLong2", Long.valueOf(456));
        getprop("myString1");
        getprop("myString2","abcd");
        getprop("myFile1");
        property("myFile2", TypeEditor.FileEditor);
    }
}
