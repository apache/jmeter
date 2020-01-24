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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.jmeter.util.JMeterContextExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@ExtendWith(JMeterContextExtension.class)
public abstract class MessageRendererTest<T> {

    @TempDir
    public Path tmpDir;

    protected Cache<Object,Object> cache = Caffeine.newBuilder().build();

    protected Object getFirstCachedValue() {
        return cache.asMap().values().stream().findFirst().get();
    }

    protected abstract MessageRenderer<T> getRenderer();

    protected void assertValueFromFile(Consumer<T> assertion, String fileName, boolean hasVariable) {
        T actual = getRenderer().getValueFromFile(fileName, "UTF-8", hasVariable, cache);
        assertion.accept(actual);
    }

    protected String writeFile(String fileName, byte[] contents) throws IOException {
        Path filePath = tmpDir.resolve(fileName);
        Files.write(filePath, contents);
        return filePath.toString();
    }

    protected String writeFile(String fileName, String text) throws IOException {
        return writeFile(fileName, text.getBytes(StandardCharsets.UTF_8));
    }

    protected String writeFile(String fileName, String text, Charset charset) throws IOException {
        return writeFile(fileName, text.getBytes(charset));
    }
}
