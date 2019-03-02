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
package org.apache.jmeter.report.core;

/**
 * The class ConvertException provides an exception when
 * ConvertStringMethod.execute fails.
 *
 * @since 3.0
 */
public class ConvertException extends Exception {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8837968211227052980L;
    private static final String MESSAGE_FORMAT = "Unable to convert \"%s\" to \"%s\"";

    /**
     * Instantiates a new convert exception.
     *
     * @param value
     *            the value
     * @param type
     *            the type
     */
    public ConvertException(String value, String type) {
        super(String.format(MESSAGE_FORMAT, value, type));
    }

    /**
     * Instantiates a new convert exception.
     *
     * @param value
     *            the value
     * @param type
     *            the type
     * @param cause
     *            the cause
     */
    public ConvertException(String value, String type, Throwable cause) {
        super(String.format(MESSAGE_FORMAT, value, type), cause);
    }

    /**
     * DO NOT USE - UNIT TEST ONLY
     * @deprecated UNIT TEST ONLY
     */
    @Deprecated // only for use by unit tests
    public ConvertException() {

    }
}
