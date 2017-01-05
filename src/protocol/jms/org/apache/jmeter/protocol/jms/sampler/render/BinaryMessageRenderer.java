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

import static java.lang.String.format;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jmeter.protocol.jms.control.gui.JMSPublisherGui;

import com.github.benmanes.caffeine.cache.Cache;

class BinaryMessageRenderer implements MessageRenderer<byte[]> {

    private TextMessageRenderer delegate;

    public BinaryMessageRenderer(TextMessageRenderer delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte[] getValueFromText(String text) {
        throw new UnsupportedOperationException(format("Type of input not handled: %s", JMSPublisherGui.USE_TEXT_RSC));
    }

    @Override
    public byte[] getValueFromFile(String filename, String encoding, boolean hasVariable, Cache<Object,Object> cache) {
        byte[] bytes;

        if (hasVariable) {
            String stringValue = delegate.getValueFromFile(filename, encoding, hasVariable, cache);
            try {
                bytes = stringValue.getBytes(encoding);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            bytes = (byte[]) cache.get(filename, _p -> getContent(filename));
        }

        return bytes;
    }

    byte[] getContent(String filename) {
        try {
            return Files.readAllBytes(Paths.get(filename));
        } catch (IOException e) {
            throw new RuntimeException(format("Can't read content of %s", filename), e);
        }
    }
}
