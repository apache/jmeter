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

package org.apache.jmeter.protocol.jms.sampler.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.junit.Test;

public class TextMessageRendererTest extends MessageRendererTest<String> {

    private TextMessageRenderer render = RendererFactory.getInstance().getText();
    @Override
    protected MessageRenderer<String> getRenderer() {
        return render;
    }

    @Test
    public void readUTF8File() {
        assertContent("utf8.txt", "UTF-8");
    }

    @Test
    public void readCP1252File() {
        assertContent("cp1252.txt", "Cp1252");
    }

    private void assertContent(String resource, String encoding) {
        String filename = getResourceFile(resource);
        String actual = render.getContent(new FileKey(filename, encoding));
        assertEquals("éè€", actual);
    }

    @Test
    public void getValueFromFileWithNoVar() {
        assertValueFromFile("noVar", "noVar.txt", true);
    }

    @Test
    public void getValueFromFileWithOneVar() {
        jmeterCtxService.get().getVariables().put("oneVar", "foobar");
        assertValueFromFile("foobar", "oneVar.txt", true);
    }

    @Test
    public void checkCache() {
        jmeterCtxService.get().getVariables().put("oneVar", "foo");
        assertValueFromFile("foo", "oneVar.txt", true);
        assertEquals("${oneVar}", getFirstCachedValue());

        jmeterCtxService.get().getVariables().put("oneVar", "bar");
        assertValueFromFile("bar", "oneVar.txt", true);
        assertEquals("${oneVar}", getFirstCachedValue());
    }

    @Test
    public void checkNoVariable() {
        jmeterCtxService.get().getVariables().put("oneVar", "RAW");
        assertValueFromFile("${oneVar}", "oneVar.txt", false);
    }

    @Test
    public void getValueFromText() {
        for (String text : Arrays.asList("a", null, "b", "")) {
            assertSame(text, render.getValueFromText(text));
        }
    }

    protected void assertValueFromFile(String expected, String resource, boolean hasVariable) {
        assertValueFromFile(actual -> assertEquals(expected, actual), resource, hasVariable);
    }
}
