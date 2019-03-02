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
 * CLUtil offers basic utility operations for use both internal and external to
 * package.
 *
 * @see CLOptionDescriptor
 */
public final class CLUtil {
    private static final int MAX_DESCRIPTION_COLUMN_LENGTH = 60;

    /**
     * Private Constructor so that no instance can ever be created.
     *
     */
    private CLUtil() {
    }

    /**
     * Format options into StringBuilder and return. This is typically used to
     * print "Usage" text in response to a "--help" or invalid option.
     *
     * @param options
     *            the option descriptors
     * @return the formatted description/help for options
     */
    public static StringBuilder describeOptions(final CLOptionDescriptor[] options) {
        final String lSep = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();

        for (CLOptionDescriptor option : options) {
            final char ch = (char) option.getId();
            final String name = option.getName();
            String description = option.getDescription();
            int flags = option.getFlags();
            boolean argumentOptional = (flags & CLOptionDescriptor.ARGUMENT_OPTIONAL) == CLOptionDescriptor.ARGUMENT_OPTIONAL;
            boolean argumentRequired = (flags & CLOptionDescriptor.ARGUMENT_REQUIRED) == CLOptionDescriptor.ARGUMENT_REQUIRED;
            boolean twoArgumentsRequired = (flags & CLOptionDescriptor.ARGUMENTS_REQUIRED_2) == CLOptionDescriptor.ARGUMENTS_REQUIRED_2;
            boolean needComma = false;
            if (twoArgumentsRequired) {
                argumentRequired = true;
            }

            sb.append('\t');

            if (Character.isLetter(ch)) {
                sb.append("-");
                sb.append(ch);
                needComma = true;
            }

            if (null != name) {
                if (needComma) {
                    sb.append(", ");
                }

                sb.append("--");
                sb.append(name);
            }

            if (argumentOptional) {
                sb.append(" [<argument>]");
            }
            if (argumentRequired) {
                sb.append(" <argument>");
            }
            if (twoArgumentsRequired) {
                sb.append("=<value>");
            }
            sb.append(lSep);

            if (null != description) {
                while (description.length() > MAX_DESCRIPTION_COLUMN_LENGTH) {
                    final String descriptionPart = description.substring(0, MAX_DESCRIPTION_COLUMN_LENGTH);
                    description = description.substring(MAX_DESCRIPTION_COLUMN_LENGTH);
                    sb.append("\t\t");
                    sb.append(descriptionPart);
                    sb.append(lSep);
                }

                sb.append("\t\t");
                sb.append(description);
                sb.append(lSep);
            }
        }
        return sb;
    }
}
