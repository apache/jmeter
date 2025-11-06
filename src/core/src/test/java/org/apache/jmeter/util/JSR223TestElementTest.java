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

package org.apache.jmeter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

public class JSR223TestElementTest {

    @SuppressWarnings("serial")
    private JSR223TestElement element = new JSR223TestElement() {
    };

    @Test
    @DisabledForJreRange(min = JRE.JAVA_15, max = JRE.OTHER, disabledReason = "The default JavaScript engine has been removed in Java 15+")
    public void testGetScriptEngineJS() throws Exception {
        element.setScriptLanguage("JavaScript");
        String languageName = element.getScriptEngine().getFactory().getLanguageName();
        assertTrue(languageName.contains("Script"),
                () -> "getFactory().getLanguageName() should contain Script, got " + languageName);
    }

    @Test
    public void testGetScriptEngineDefault() throws Exception {
        element.setScriptLanguage("");
        assertEquals("Groovy", element.getScriptEngine().getFactory().getLanguageName());
    }

}
