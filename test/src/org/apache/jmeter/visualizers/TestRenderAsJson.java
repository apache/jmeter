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

package org.apache.jmeter.visualizers;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class TestRenderAsJson {

    private Method prettyJSON;
    private final String TAB = "    ";

    private String prettyJSON(String prettify) throws Exception {
        return (String) prettyJSON.invoke(null, prettify);
    }

    @Before
    public void setUp() throws Exception {
        prettyJSON = RenderAsJSON.class.getDeclaredMethod("prettyJSON",
                String.class);
        prettyJSON.setAccessible(true);
    }

    @Test
    public void testRenderResultWithLongStringBug54826() throws Exception {
        StringBuilder json = new StringBuilder();
        json.append("\"customData\": \"");
        for (int i = 0; i < 100; i++) {
            json.append("somenotsorandomtext");
        }
        json.append("\"");

        assertEquals("{\n" + TAB + json.toString() + "\n}",
                prettyJSON("{" + json.toString() + "}"));
    }

    @Test
    public void testRenderResultSimpleObject() throws Exception {
        assertEquals("{\n}", prettyJSON("{}"));
    }

    @Test
    public void testRenderResultComplexArray() throws Exception {
        assertEquals("[\n" + TAB + "1,\n" + TAB + "{\n" + TAB + TAB + "\"A\": \"B\"\n" + TAB + "}\n]", prettyJSON("[1,{\"A\":\"B\"}]"));
    }
    @Test
    public void testRenderResultSimpleArray() throws Exception {
        assertEquals("[\n]", prettyJSON("[]"));
    }

    @Test
    public void testRenderArrayInObject() throws Exception {
        assertEquals("{\n" + TAB + "\"foo\": [\n" + TAB + "]\n}",
                prettyJSON("{\"foo\":[]}"));
    }

    @Test
    public void testRenderResultSimpleNumber() throws Exception {
        assertEquals("42", prettyJSON("42"));
    }

    @Test
    public void testRenderResultSimpleString() throws Exception {
        assertEquals("Hello World", prettyJSON("Hello World"));
    }

    @Test
    public void testRenderResultSimpleStructure() throws Exception {
        assertEquals(
                "{\n" + TAB + "\"Hello\": \"World\",\n" + TAB + "\"more\": [\n"
                        + TAB + TAB + "\"Something\",\n" + TAB
                        + TAB + "\"else\"\n" + TAB + "]\n}",
                prettyJSON("{\"Hello\": \"World\", \"more\": [\"Something\", \"else\", ]}"));
    }

}
