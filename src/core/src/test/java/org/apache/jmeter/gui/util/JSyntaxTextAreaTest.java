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

package org.apache.jmeter.gui.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.HeadlessException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Properties;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.junit.Test;

public class JSyntaxTextAreaTest extends JMeterTestCase {

    @Test
    public void testSetLanguage() {
        try {
            @SuppressWarnings("deprecation") // test code
            JSyntaxTextArea textArea = new JSyntaxTextArea(30, 50, false);
            textArea.setLanguage(null);
            assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE, textArea.getSyntaxEditingStyle());
        } catch (HeadlessException he) {
            // Does not work in headless mode
        }
    }

    @Test
    public void testSyntaxNames() throws IllegalArgumentException,
            IllegalAccessException {
        HashSet<String> values = new HashSet<>();
        for (Field field : SyntaxConstants.class.getFields()) {
            int modifiers = field.getModifiers();
            if (field.getType().equals(String.class)
                    && Modifier.isStatic(modifiers)
                    && Modifier.isPublic(modifiers)) {
                values.add((String) field.get(null));
            }
        }
        final Properties languageProperties = JMeterUtils
                .loadProperties("org/apache/jmeter/gui/util/textarea.properties"); //$NON-NLS-1$;
        for (Object s : languageProperties.values()) {
            if (!values.contains(s)) {
                fail("Invalid property value: " + s);
            }
        }
    }
}
