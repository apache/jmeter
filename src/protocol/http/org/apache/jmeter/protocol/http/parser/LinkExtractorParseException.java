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

package org.apache.jmeter.protocol.http.parser;

/**
 * Exception used with {@link LinkExtractorParser}
 * @since 3.0
 */
public class LinkExtractorParseException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 6816968619973437826L;

    /**
     * 
     */
    public LinkExtractorParseException() {
    }

    /**
     * @param message text describing the cause of the exception
     */
    public LinkExtractorParseException(String message) {
        super(message);
    }

    /**
     * @param cause of the exception
     */
    public LinkExtractorParseException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message text describing the cause of the exception
     * @param cause exception, that lead to this exception
     */
    public LinkExtractorParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message text describing the cause of the exception
     * @param cause exception, that lead to this exception
     * @param enableSuppression whether or not suppression is enabled
     * @param writableStackTrace whether or not the stacktrace should be writable
     */
    public LinkExtractorParseException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
