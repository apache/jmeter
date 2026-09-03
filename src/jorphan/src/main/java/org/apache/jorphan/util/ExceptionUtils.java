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

package org.apache.jorphan.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public class ExceptionUtils {
    private ExceptionUtils() {
    }

    /**
     * Converts the stack trace of a {@code Throwable} into a string representation.
     *
     * @param throwable the {@code Throwable} whose stack trace is to be converted to a string
     * @return the string representation of the stack trace of the specified {@code Throwable}
     */
    @API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
    public static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }
        return sw.toString();
    }

    /**
     * Converts the stack trace of a {@code Throwable} into a byte array representation using PrintStream.
     *
     * @param throwable the {@code Throwable} whose stack trace is to be converted to bytes
     * @param charset the character set to be used for encoding the stack trace
     * @return the byte array representation of the stack trace of the specified {@code Throwable}
     */
    @API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
    public static byte[] getStackTraceAsBytes(Throwable throwable, Charset charset) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, false, charset)) {
            throwable.printStackTrace(ps);
        }
        return baos.toByteArray();
    }

    /**
     * Helper method to get the root cause message from a throwable.
     *
     * @param t the throwable
     * @return the root cause message
     */
    @API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
    public static String getRootCauseMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}
