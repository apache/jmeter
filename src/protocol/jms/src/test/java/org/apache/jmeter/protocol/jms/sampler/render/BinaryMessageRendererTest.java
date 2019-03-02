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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BinaryMessageRendererTest extends MessageRendererTest<byte[]> {

    @Rule
    public ExpectedException error = ExpectedException.none();

    BinaryMessageRenderer render = RendererFactory.getInstance().getBinary();
    @Override
    protected MessageRenderer<byte[]> getRenderer() {
        return render;
    }

    @Test(expected=UnsupportedOperationException.class)
    public void getValueFromText() {
        render.getValueFromText("");
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
        byte[] actual = render.getContent(filename);
        try {
            assertArrayEquals("éè€".getBytes(encoding), actual);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void readNonExistingContent() {
        error.expect(RuntimeException.class);
        error.expectCause(instanceOf(IOException.class));
        error.expectMessage("Can't read content of __file_that_may_not_exists_else_it_will_fail");
        render.getContent("__file_that_may_not_exists_else_it_will_fail");
    }

    @Test
    public void getValueFromFile_withNoVar() {
        String text = "éè€";
        assertValueFromFile(text, "utf8.txt", true);
        assertCacheContentInString(text);
    }

    @Test
    public void getValueFromFile_withOneVar() {
        String value = "éè€";
        jmeterCtxService.get().getVariables().put("oneVar", value);
        assertValueFromFile(value, "oneVar.txt", true);
        assertCacheContentInString("${oneVar}");
    }

    @Test
    public void getValueFromFile_withInvalidEncoding() {
        error.expect(UnsupportedCharsetException.class);
        render.getValueFromFile(getResourceFile("utf8.txt"), "banana", true, cache);
    }

    @Test
    public void getValueFromFile_inRawMode() {
        String text = "${oneVar}";
        assertValueFromFile(text, "oneVar.txt", false);
        assertCacheContentInBytes(text);
    }

    protected void assertValueFromFile(String expected, String resource, boolean hasVariable) {
        assertValueFromFile(actual -> assertBytesEquals(expected, actual), resource, hasVariable);
    }

    protected void assertCacheContentInBytes(String expected) {
        assertBytesEquals(expected, (byte[]) getFirstCachedValue());
    }
    protected void assertCacheContentInString(String expected) {
        assertEquals(expected, getFirstCachedValue());
    }
    protected void assertBytesEquals(String expected, byte[] actual) {
        assertArrayEquals(expected.getBytes(StandardCharsets.UTF_8), actual);
    }
}
