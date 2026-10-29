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

package org.apache.jmeter.protocol.http.sampler;

import java.nio.charset.StandardCharsets;

/**
 * Sizes of the parts of an HTTP/1.1 request.
 * <p>
 * The lengths are the ones the message has on the wire, where the request line and the headers are
 * encoded with ISO-8859-1, one byte per character.
 */
final class HTTPMessageSizes {

    /** Length of the empty line that separates the headers from the body. */
    static final long EMPTY_LINE = 2;

    private HTTPMessageSizes() {
        // utility class
    }

    /**
     * Length of the request line, that is {@code METHOD URI VERSION} with a space between its
     * three parts and the CRLF that terminates it.
     *
     * @param method  HTTP method of the request
     * @param uri     request target
     * @param version HTTP version of the request
     * @return the number of bytes the request line takes on the wire
     */
    static long requestLineLength(String method, String uri, String version) {
        return length(method) + 1 + length(uri) + 1 + length(version) + 2;
    }

    /**
     * Length of a header line, that is {@code name: value} and the CRLF that terminates it.
     *
     * @param name  name of the header
     * @param value value of the header
     * @return the number of bytes the header line takes on the wire
     */
    static long headerLength(String name, String value) {
        return length(name) + 2 + length(value) + 2;
    }

    /**
     * @param text text to measure, may be {@code null}
     * @return the number of bytes the text takes on the wire, {@code 0} for {@code null}
     */
    static long length(String text) {
        return text == null ? 0 : text.getBytes(StandardCharsets.ISO_8859_1).length;
    }
}
