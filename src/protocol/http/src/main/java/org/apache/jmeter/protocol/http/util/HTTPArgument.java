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

package org.apache.jmeter.protocol.http.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//For unit tests, @see TestHTTPArgument

/*
 *
 * Represents an Argument for HTTP requests.
 */
public class HTTPArgument extends Argument implements Serializable {
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private static final Logger log = LoggerFactory.getLogger(HTTPArgument.class);

    private static final long serialVersionUID = 241L;

    private static final String ALWAYS_ENCODE = "HTTPArgument.always_encode";

    private static final String USE_EQUALS = "HTTPArgument.use_equals";

    private static final String CONTENT_TYPE = "HTTPArgument.content_type";

    private static final EncoderCache cache = new EncoderCache(1000);

    /**
     * Constructor for the Argument object.
     * <p>
     * The value is assumed to be not encoded.
     *
     * @param name
     *            name of the parameter
     * @param value
     *            value of the parameter
     * @param metadata
     *            the separator to use between name and value
     */
    public HTTPArgument(String name, String value, String metadata) {
        this(name, value, false);
        this.setMetaData(metadata);
    }

    public void setUseEquals(boolean ue) {
        if (ue) {
            setMetaData("=");
        } else {
            setMetaData("");
        }
        setProperty(new BooleanProperty(USE_EQUALS, ue));
    }

    public boolean isUseEquals() {
        boolean eq = getPropertyAsBoolean(USE_EQUALS);
        if (getMetaData().equals("=") || (getValue() != null && getValue().length() > 0)) {
            setUseEquals(true);
            return true;
        }
        return eq;

    }

    public void setContentType(String ct) {
        setProperty(CONTENT_TYPE, ct, HTTPArgument.DEFAULT_CONTENT_TYPE);
    }

    public String getContentType() {
        return getPropertyAsString(CONTENT_TYPE, HTTPArgument.DEFAULT_CONTENT_TYPE);
    }

    public void setAlwaysEncoded(boolean ae) {
        setProperty(new BooleanProperty(ALWAYS_ENCODE, ae));
    }

    public boolean isAlwaysEncoded() {
        return getPropertyAsBoolean(ALWAYS_ENCODE);
    }

    /**
     * Constructor for the Argument object.
     * <p>
     * The value is assumed to be not encoded.
     *
     * @param name
     *            name of the parameter
     * @param value
     *            value of the parameter
     */
    public HTTPArgument(String name, String value) {
        this(name, value, false);
    }

    /**
     * @param name
     *            name of the parameter
     * @param value
     *            value of the parameter
     * @param alreadyEncoded
     *            <code>true</code> if the value is already encoded, in which
     *            case they are decoded before storage
     */
    public HTTPArgument(String name, String value, boolean alreadyEncoded) {
        // We assume the argument value is encoded according to the HTTP spec, i.e. UTF-8
        this(name, value, alreadyEncoded, EncoderCache.URL_ARGUMENT_ENCODING);
    }

    /**
     * Construct a new HTTPArgument instance; alwaysEncoded is set to true.
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @param alreadyEncoded true if the name and value is already encoded, in which case they are decoded before storage.
     * @param contentEncoding the encoding used for the parameter value
     */
    public HTTPArgument(String name, String value, boolean alreadyEncoded, String contentEncoding) {
        setAlwaysEncoded(true);
        if (alreadyEncoded) {
            try {
                // We assume the name is always encoded according to spec
                if(log.isDebugEnabled()) {
                    log.debug("Decoding name, calling URLDecoder.decode with '{}' and contentEncoding: '{}'", name,
                            EncoderCache.URL_ARGUMENT_ENCODING);
                }
                name = URLDecoder.decode(name, EncoderCache.URL_ARGUMENT_ENCODING);
                // The value is encoded in the specified encoding
                if(log.isDebugEnabled()) {
                    log.debug("Decoding value, calling URLDecoder.decode with '{}' and contentEncoding: '{}'", value,
                            contentEncoding);
                }
                value = URLDecoder.decode(value, contentEncoding);
            } catch (UnsupportedEncodingException e) {
                log.error("{} encoding not supported!", contentEncoding);
                throw new Error(e.toString(), e);
            }
        }
        setName(name);
        setValue(value);
        setMetaData("=");
    }

    /**
     * Construct a new HTTPArgument instance
     *
     * @param name
     *            the name of the parameter
     * @param value
     *            the value of the parameter
     * @param metaData
     *            the separator to use between name and value
     * @param alreadyEncoded
     *            true if the name and value is already encoded
     */
    public HTTPArgument(String name, String value, String metaData, boolean alreadyEncoded) {
        // We assume the argument value is encoded according to the HTTP spec, i.e. UTF-8
        this(name, value, metaData, alreadyEncoded, EncoderCache.URL_ARGUMENT_ENCODING);
    }

    /**
     * Construct a new HTTPArgument instance
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @param metaData the separator to use between name and value
     * @param alreadyEncoded true if the name and value is already encoded
     * @param contentEncoding the encoding used for the parameter value
     */
    public HTTPArgument(String name, String value, String metaData, boolean alreadyEncoded, String contentEncoding) {
        this(name, value, alreadyEncoded, contentEncoding);
        setMetaData(metaData);
    }

    public HTTPArgument(Argument arg) {
        this(arg.getName(), arg.getValue(), arg.getMetaData());
    }

    /**
     * Constructor for the Argument object
     */
    public HTTPArgument() {
    }

    /**
     * Sets the Name attribute of the Argument object.
     *
     * @param newName
     *            the new Name value
     */
    @Override
    public void setName(String newName) {
        if (newName == null || !newName.equals(getName())) {
            super.setName(newName);
        }
    }

    /**
     * Get the argument value encoded using UTF-8
     *
     * @return the argument value encoded in UTF-8
     */
    public String getEncodedValue() {
        // Encode according to the HTTP spec, i.e. UTF-8
        try {
            return getEncodedValue(EncoderCache.URL_ARGUMENT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            // This can't happen (how should utf8 not be supported!?!),
            // so just throw an Error:
            throw new Error("Should not happen: " + e.toString());
        }
    }

    /**
     * Get the argument value encoded in the specified encoding
     *
     * @param contentEncoding the encoding to use when encoding the argument value
     * @return the argument value encoded in the specified encoding
     * @throws UnsupportedEncodingException of the encoding is not supported
     */
    public String getEncodedValue(String contentEncoding) throws UnsupportedEncodingException {
        if (isAlwaysEncoded()) {
            return cache.getEncoded(getValue(), contentEncoding);
        } else {
            return getValue();
        }
    }

    public String getEncodedName() {
        if (isAlwaysEncoded()) {
            return cache.getEncoded(getName());
        } else {
            return getName();
        }

    }

    /**
     * Converts all {@link Argument} entries in the collection to {@link HTTPArgument} entries.
     *
     * @param args collection of {@link Argument} and/or {@link HTTPArgument} entries
     */
    public static void convertArgumentsToHTTP(Arguments args) {
        List<Argument> newArguments = new LinkedList<>();
        for (JMeterProperty jMeterProperty : args.getArguments()) {
            Argument arg = (Argument) jMeterProperty.getObjectValue();
            if (!(arg instanceof HTTPArgument)) {
                newArguments.add(new HTTPArgument(arg));
            } else {
                newArguments.add(arg);
            }
        }
        args.removeAllArguments();
        args.setArguments(newArguments);
    }
}
