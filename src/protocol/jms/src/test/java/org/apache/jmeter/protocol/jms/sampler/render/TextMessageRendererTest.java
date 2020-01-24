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

package org.apache.jmeter.protocol.jms.sampler.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TextMessageRendererTest extends MessageRendererTest<String> {

    private TextMessageRenderer render = RendererFactory.getInstance().getText();
    @Override
    protected MessageRenderer<String> getRenderer() {
        return render;
    }

    @ParameterizedTest
    @ValueSource(strings = {"UTF-8", "Cp1252"})
    public void testGetContent(String encoding) throws UnsupportedEncodingException, IOException {
        String value = "éè€";
        byte[] expected = value.getBytes(encoding);
        String filename = writeFile(encoding, expected);

        // The tested method
        String actual = render.getContent(new FileKey(filename, encoding));

        assertEquals(value, actual);
    }

    @Test
    public void getValueFromFileWithNoVar() throws IOException {
        String fileName = writeFile("noVar", "noVar");
        assertValueFromFile("noVar", fileName, true);
    }

    @Test
    public void getValueFromFileWithOneVar(JMeterVariables vars) throws IOException {
        String fileName = writeFile("oneVar.txt", "${oneVar}");
        vars.put("oneVar", "foobar");
        assertValueFromFile("foobar", fileName, true);
    }

    @Test
    public void checkCache(JMeterVariables vars) throws IOException {
        String fileName = writeFile("oneVar.txt", "${oneVar}");
        vars.put("oneVar", "foo");
        assertValueFromFile("foo", fileName, true);
        assertEquals("${oneVar}", getFirstCachedValue());

        vars.put("oneVar", "bar");
        assertValueFromFile("bar", fileName, true);
        assertEquals("${oneVar}", getFirstCachedValue());
    }

    @Test
    public void checkNoVariable(JMeterVariables vars) throws IOException {
        String fileName = writeFile("oneVar.txt", "${oneVar}");
        vars.put("oneVar", "RAW");
        assertValueFromFile("${oneVar}", fileName, false);
    }

    @Test
    public void getValueFromText() {
        for (String text : Arrays.asList("a", null, "b", "")) {
            assertSame(text, render.getValueFromText(text));
        }
    }

    protected void assertValueFromFile(String expected, String fileName, boolean hasVariable) {
        assertValueFromFile(actual -> assertEquals(expected, actual), fileName, hasVariable);
    }
}
