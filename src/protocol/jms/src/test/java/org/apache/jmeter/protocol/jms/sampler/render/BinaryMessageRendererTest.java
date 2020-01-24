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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.internal.matchers.ThrowableCauseMatcher.hasCause;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.apache.jmeter.threads.JMeterVariables;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BinaryMessageRendererTest extends MessageRendererTest<byte[]> {

    private BinaryMessageRenderer render = RendererFactory.getInstance().getBinary();

    @Override
    protected MessageRenderer<byte[]> getRenderer() {
        return render;
    }

    @Test
    public void getValueFromText() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> render.getValueFromText(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UTF-8", "Cp1252"})
    public void testGetContent(String encoding) throws UnsupportedEncodingException, IOException {
        String value = "éè€";
        byte[] expected = value.getBytes(encoding);
        String filename = writeFile(encoding, expected);

        // The tested method
        byte[] actual = render.getContent(filename);

        assertArrayEquals(expected, actual, "value: " + value);
    }

    @Test
    public void readNonExistingContent() {
        RuntimeException ex = Assertions.assertThrows(
                RuntimeException.class,
                () -> render.getContent("__file_that_may_not_exists_else_it_will_fail")
        );

        MatcherAssert.assertThat(ex, allOf(
                hasMessage(containsString("Can't read content of __file_that_may_not_exists_else_it_will_fail")),
                hasCause(instanceOf(IOException.class))
        ));
    }

    @Test
    public void getValueFromFile_withNoVar() throws IOException {
        String text = "éè€";
        String fileName = writeFile("utf8.txt", text);
        assertValueFromFile(text, fileName, true);
        assertCacheContentInString(text);
    }

    @Test
    public void getValueFromFile_withOneVar(JMeterVariables vars) throws IOException {
        String fileName = writeFile("oneVar.txt", "${oneVar}");
        String value = "éè€";
        vars.put("oneVar", value);
        assertValueFromFile(value, fileName, true);
        assertCacheContentInString("${oneVar}");
    }

    @Test
    public void getValueFromFile_withInvalidEncoding() {
        RuntimeException ex = Assertions.assertThrows(
                RuntimeException.class,
                () -> render.getValueFromFile("utf8.txt", "banana", true, cache)
        );
        MatcherAssert.assertThat(
                ex,
                allOf(
                        hasMessage(containsString("utf8.txt")),
                        hasCause(instanceOf(UnsupportedEncodingException.class))
                )
        );
    }

    @Test
    public void getValueFromFile_inRawMode() throws IOException {
        String text = "${oneVar}";
        String fileName = writeFile("oneVar.txt", text);
        assertValueFromFile(text, fileName, false);
        assertCacheContentInBytes(text);
    }

    protected void assertValueFromFile(String expected, String fileName, boolean hasVariable) {
        assertValueFromFile(actual -> assertBytesEquals(expected, actual), fileName, hasVariable);
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
