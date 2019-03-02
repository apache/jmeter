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
 * Basic class describing an type of option. Typically, one creates a static
 * array of <code>CLOptionDescriptor</code>s, and passes it to
 * {@link CLArgsParser#CLArgsParser(String[], CLOptionDescriptor[])}.
 *
 * @see CLArgsParser
 * @see CLUtil
 */
public final class CLOptionDescriptor {
    /** Flag to say that one argument is required */
    public static final int ARGUMENT_REQUIRED = 1 << 1;

    /** Flag to say that the argument is optional */
    public static final int ARGUMENT_OPTIONAL = 1 << 2;

    /** Flag to say this option does not take arguments */
    public static final int ARGUMENT_DISALLOWED = 1 << 3;

    /** Flag to say this option requires 2 arguments */
    public static final int ARGUMENTS_REQUIRED_2 = 1 << 4;

    /** Flag to say this option may be repeated on the command line */
    public static final int DUPLICATES_ALLOWED = 1 << 5;

    private final int id;

    private final int flags;

    private final String name;

    private final String description;

    private final int[] incompatible;

    /**
     * Constructor.
     *
     * @param name
     *            the name/long option
     * @param flags
     *            the flags
     * @param id
     *            the id/character option
     * @param description
     *            description of option usage
     */
    public CLOptionDescriptor(final String name, final int flags, final int id, final String description) {

        checkFlags(flags);

        this.id = id;
        this.name = name;
        this.flags = flags;
        this.description = description;
        this.incompatible = ((flags & DUPLICATES_ALLOWED) != 0) ? new int[0] : new int[] { id };
    }


    /**
     * Constructor.
     *
     * @param name
     *            the name/long option
     * @param flags
     *            the flags
     * @param id
     *            the id/character option
     * @param description
     *            description of option usage
     * @param incompatible
     *            descriptors for incompatible options
     */
    public CLOptionDescriptor(final String name, final int flags, final int id, final String description,
            final CLOptionDescriptor[] incompatible) {

        checkFlags(flags);

        this.id = id;
        this.name = name;
        this.flags = flags;
        this.description = description;

        this.incompatible = new int[incompatible.length];
        for (int i = 0; i < incompatible.length; i++) {
            this.incompatible[i] = incompatible[i].getId();
        }
    }

    private void checkFlags(final int flags) {
        int modeCount = 0;
        if ((ARGUMENT_REQUIRED & flags) == ARGUMENT_REQUIRED) {
            modeCount++;
        }
        if ((ARGUMENT_OPTIONAL & flags) == ARGUMENT_OPTIONAL) {
            modeCount++;
        }
        if ((ARGUMENT_DISALLOWED & flags) == ARGUMENT_DISALLOWED) {
            modeCount++;
        }
        if ((ARGUMENTS_REQUIRED_2 & flags) == ARGUMENTS_REQUIRED_2) {
            modeCount++;
        }

        if (0 == modeCount) {
            final String message = "No mode specified for option " + this;
            throw new IllegalStateException(message);
        } else if (1 != modeCount) {
            final String message = "Multiple modes specified for option " + this;
            throw new IllegalStateException(message);
        }
    }

    /**
     * Get the array of incompatible option ids.
     *
     * @return the array of incompatible option ids
     */
    protected final int[] getIncompatible() {
        return this.incompatible;
    }

    /**
     * Retrieve textual description.
     *
     * @return the description
     */
    public final String getDescription() {
        return this.description;
    }

    /**
     * Retrieve flags about option. Flags include details such as whether it
     * allows parameters etc.
     *
     * @return the flags
     */
    public final int getFlags() {
        return this.flags;
    }

    /**
     * Retrieve the id for option. The id is also the character if using single
     * character options.
     *
     * @return the id
     */
    public final int getId() {
        return this.id;
    }

    /**
     * Retrieve name of option which is also text for long option.
     *
     * @return name/long option
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Convert to String.
     *
     * @return the converted value to string.
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[OptionDescriptor ");
        sb.append(this.name);
        sb.append(", ");
        sb.append(this.id);
        sb.append(", ");
        sb.append(this.flags);
        sb.append(", ");
        sb.append(this.description);
        sb.append(" ]");
        return sb.toString();
    }
}
