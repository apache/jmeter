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
 */
package org.apache.jmeter.testbeans.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import org.junit.Test;

/**
 * Test class to check that the JVM provides sensible behaviour for the boolean PropertyEditor, i.e.
 * that getAsText() can only return values that match getTags().
 *
 * Also checks that BooleanPropertyEditor behaves in the same way.
 */
public class TestBooleanPropertyEditor {

    /*
     * N.B.
     * These values are NOT the same as Boolean.FALSE|TRUE.toString()
     * which returns lower-case only. The getAsText() method converts
     * the result to mixed case.
     */
    private static final String FALSE = "False"; // $NON-NLS-1$
    private static final String TRUE  = "True";  // $NON-NLS-1$


    @Test
    public void testBooleanEditor(){
        PropertyEditor propertyEditor = PropertyEditorManager.findEditor(boolean.class);
        testBooleanEditor(propertyEditor);
    }

    @Test
    public void testBooleanPropertyEditor() {
        PropertyEditor propertyEditor = new BooleanPropertyEditor();
        testBooleanEditor(propertyEditor);
    }

    private void testBooleanEditor(PropertyEditor propertyEditor) {
        assertNotNull("Expected to find property editor", propertyEditor);
        String[] tags = propertyEditor.getTags();
        assertEquals(2,tags.length);
        assertEquals(TRUE,tags[0]);
        assertEquals(FALSE,tags[1]);

        propertyEditor.setValue(Boolean.FALSE);
        assertEquals(FALSE,propertyEditor.getAsText());
        propertyEditor.setAsText(FALSE);
        assertEquals(FALSE,propertyEditor.getAsText());
        propertyEditor.setAsText("false");
        assertEquals(FALSE,propertyEditor.getAsText());
        propertyEditor.setAsText("False");
        assertEquals(FALSE,propertyEditor.getAsText());
        propertyEditor.setAsText("FALSE");
        assertEquals(FALSE,propertyEditor.getAsText());

        propertyEditor.setValue(Boolean.TRUE);
        assertEquals(TRUE,propertyEditor.getAsText());
        propertyEditor.setAsText(TRUE);
        assertEquals(TRUE,propertyEditor.getAsText());
        propertyEditor.setAsText("true");
        assertEquals(TRUE,propertyEditor.getAsText());
        propertyEditor.setAsText("True");
        assertEquals(TRUE,propertyEditor.getAsText());
        propertyEditor.setAsText("TRUE");
        assertEquals(TRUE,propertyEditor.getAsText());
    }
}
