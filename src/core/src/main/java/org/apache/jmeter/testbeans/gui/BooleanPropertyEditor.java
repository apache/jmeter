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

package org.apache.jmeter.testbeans.gui;

import java.beans.PropertyEditorSupport;

/**
 * Property Editor which handles Boolean properties.
 */
public class BooleanPropertyEditor extends PropertyEditorSupport {

    // These are the mixed-case values as returned by the RI JVM boolean property editor
    // However, they are different from the lower-case values returned by e.g. Boolean.FALSE.toString()
    private static final String FALSE = "False"; // $NON-NLS-1$
    private static final String TRUE  = "True";  // $NON-NLS-1$

    private static final String[] TAGS = {TRUE, FALSE};

    // Make sure we return one of the TAGS
    @Override
    public String getAsText() {
        Object value = getValue();
        return value instanceof Boolean ?  toString((Boolean) value) : null;
    }

    private static String toString(Boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public void setAsText(String text) {
        this.setValue(text);
    }

    @Override
    public void setValue(Object value){
        if (value instanceof String) {
            super.setValue(Boolean.valueOf((String) value));
        } else if (value == null || value instanceof Boolean) {
            super.setValue(value); // not sure if null is passed in but no harm in setting it
        } else {
            throw new java.lang.IllegalArgumentException("Unexpected type: "+value.getClass().getName());
        }
    }

    @Override
    public String[] getTags() {
        return TAGS;
    }
}
