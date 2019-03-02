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
 * Thrown when some sample processing occurred
 * 
 * @since 3.0
 */
public class SampleException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -6186024584671619389L;

    /**
     * Instantiates a new sample exception.
     */
    public SampleException() {
    }

    /**
     * Instantiates a new sample exception with the specified message.
     *
     * @param message
     *            the message
     */
    public SampleException(String message) {
        super(message);
    }

    /**
     * Instantiates a new sample exception.
     *
     * @param cause
     *            the cause
     */
    public SampleException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new sample exception.
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public SampleException(String message, Throwable cause) {
        super(message, cause);
    }
}
