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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.cli.avalon;

/**
 * Token handles tokenizing the CLI arguments
 *
 */
class Token {
    /** Type for a separator token */
    public static final int TOKEN_SEPARATOR = 0;

    /** Type for a text token */
    public static final int TOKEN_STRING = 1;

    private final int type;

    private final String value;

    /**
     * New Token object with a type and value
     *
     * @param type
     *            type of the token
     * @param value
     *            value of the token
     */
    Token(final int type, final String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Get the value of the token
     *
     * @return value of the token
     */
    final String getValue() {
        return this.value;
    }

    /**
     * Get the type of the token
     *
     * @return type of the token
     */
    final int getType() {
        return this.type;
    }

    /**
     * Convert to a string
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.type);
        sb.append(":");
        sb.append(this.value);
        return sb.toString();
    }
}
