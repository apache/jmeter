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

package org.apache.jmeter.testbeans.gui;

import java.beans.PropertyEditorSupport;

/**
 * Property Editor which handles Long properties.
 * Uses {@link Long#decode(String)} so supports hex and octal input.
 */
public class LongPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        this.setValue(text);
    }

    @Override
    public void setValue(Object value){
        if (value instanceof String) {
            super.setValue(Long.decode((String) value)); // handles hex as well
        } else if (value == null || value instanceof Long) {
            super.setValue(value); // not sure if null is passed in but no harm in setting it
        } else {
            throw new java.lang.IllegalArgumentException("Unexpected type: "+value.getClass().getName());
        }
    }
}
