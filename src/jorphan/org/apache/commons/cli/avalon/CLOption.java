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

import java.util.Arrays;

/**
 * Basic class describing an instance of option.
 *
 */
public final class CLOption {
    /**
     * Value of {@link CLOptionDescriptor#getId} when the option is a text argument.
     */
    public static final int TEXT_ARGUMENT = 0;

    /**
     * Default descriptor. Required, since code assumes that getDescriptor will
     * never return null.
     */
    private static final CLOptionDescriptor TEXT_ARGUMENT_DESCRIPTOR = new CLOptionDescriptor(null,
            CLOptionDescriptor.ARGUMENT_OPTIONAL, TEXT_ARGUMENT, null);

    private String[] arguments;

    private CLOptionDescriptor descriptor = TEXT_ARGUMENT_DESCRIPTOR;

    /**
     * Retrieve argument to option if it takes arguments.
     *
     * @return the (first) argument
     */
    public final String getArgument() {
        return getArgument(0);
    }

    /**
     * Retrieve indexed argument to option if it takes arguments.
     *
     * @param index
     *            The argument index, from 0 to {@link #getArgumentCount()}-1.
     * @return the argument
     */
    public final String getArgument(final int index) {
        if (null == this.arguments || index < 0 || index >= this.arguments.length) {
            return null;
        } else {
            return this.arguments[index];
        }
    }

    public final CLOptionDescriptor getDescriptor() {
        return this.descriptor;
    }

    /**
     * Constructor taking an descriptor
     *
     * @param descriptor
     *            the descriptor iff null, will default to a "text argument"
     *            descriptor.
     */
    public CLOption(final CLOptionDescriptor descriptor) {
        if (descriptor != null) {
            this.descriptor = descriptor;
        }
    }

    /**
     * Constructor taking argument for option.
     *
     * @param argument
     *            the argument
     */
    public CLOption(final String argument) {
        this((CLOptionDescriptor) null);
        addArgument(argument);
    }

    /**
     * Mutator of Argument property.
     *
     * @param argument
     *            the argument
     */
    public final void addArgument(final String argument) {
        if (null == this.arguments) {
            this.arguments = new String[] { argument };
        } else {
            final String[] arguments = new String[this.arguments.length + 1];
            System.arraycopy(this.arguments, 0, arguments, 0, this.arguments.length);
            arguments[this.arguments.length] = argument;
            this.arguments = arguments;
        }
    }

    /**
     * Get number of arguments.
     *
     * @return the number of arguments
     */
    public final int getArgumentCount() {
        if (null == this.arguments) {
            return 0;
        } else {
            return this.arguments.length;
        }
    }

    /**
     * Convert to String.
     *
     * @return the string value
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        final char id = (char) this.descriptor.getId();
        if (id == TEXT_ARGUMENT) {
            sb.append("TEXT ");
        } else {
            sb.append("Option ");
            sb.append(id);
        }

        if (null != this.arguments) {
            sb.append(", ");
            sb.append(Arrays.asList(this.arguments));
        }

        sb.append(" ]");

        return sb.toString();
    }

    /*
     * Convert to a shorter String for test purposes
     *
     * @return the string value
     */
    final String toShortString() {
        final StringBuilder sb = new StringBuilder();
        final char id = (char) this.descriptor.getId();
        if (id != TEXT_ARGUMENT) {
            sb.append("-");
            sb.append(id);
        }

        if (null != this.arguments) {
            if (id != TEXT_ARGUMENT) {
                sb.append("=");
            }
            sb.append(Arrays.asList(this.arguments));
        }
        return sb.toString();
    }
}
