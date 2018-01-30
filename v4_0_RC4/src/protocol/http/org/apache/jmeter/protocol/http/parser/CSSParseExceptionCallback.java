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

import java.net.URL;

import org.apache.commons.lang3.Validate;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.css.handler.ICSSParseExceptionCallback;
import com.helger.css.parser.ParseException;
import com.helger.css.reader.errorhandler.LoggingCSSParseErrorHandler;

public class CSSParseExceptionCallback implements ICSSParseExceptionCallback {

    private static final long serialVersionUID = -4277276398858139449L;
    private static final Logger LOG = LoggerFactory.getLogger(CSSParseExceptionCallback.class);
    private static final boolean IGNORE_UNRECOVERABLE_PARSING_ERROR = JMeterUtils
            .getPropDefault(
                    "httpsampler.ignore_failed_embedded_resource", false); //$NON-NLS-1$

    private final URL baseUrl;

    public CSSParseExceptionCallback(URL baseUrl) {
        this.baseUrl = Validate.notNull(baseUrl);
    }

    /**
     * @deprecated - only for use from unit test code
     */
    @Deprecated
    public CSSParseExceptionCallback() {
        this.baseUrl = null;
        LOG.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }

    @Override
    public void onException(ParseException ex) {
        final String message = "Failed to parse CSS: " + baseUrl + ", "
                + LoggingCSSParseErrorHandler.createLoggingStringParseError(ex);
        if (IGNORE_UNRECOVERABLE_PARSING_ERROR) {
            LOG.warn(message);
        } else {
            throw new IllegalArgumentException(
                    "Unrecoverable error met during parsing, "
                    + "you can ignore such errors by setting property:"
                    + "'httpsampler.ignore_failed_embedded_resource' to true, message:"
                    + message);
        }

    }

}
