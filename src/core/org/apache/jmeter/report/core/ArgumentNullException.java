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
 * The class ArgumentNullException provides an exception thrown when a required
 * argument is null.
 * 
 * @since 2.14
 */
public class ArgumentNullException extends IllegalArgumentException {
    private static final long serialVersionUID = -1386650939198336456L;
    private static final String MESSAGE_FMT = "%s cannot be null.";

    /**
     * Instantiates a new argument null exception.
     */
    public ArgumentNullException() {
    }

    /**
     * Instantiates a new argument null exception.
     *
     * @param parameter
     *            the name of the parameter
     */
    public ArgumentNullException(String parameter) {
        super(String.format(MESSAGE_FMT, parameter));
    }

    /**
     * Instantiates a new argument null exception.
     *
     * @param cause
     *            the inner cause
     */
    public ArgumentNullException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new argument null exception.
     *
     * @param parameter
     *            The name of the parameter
     * @param cause
     *            the inner cause
     */
    public ArgumentNullException(String parameter, Throwable cause) {
        super(String.format(MESSAGE_FMT, parameter), cause);
    }

}
