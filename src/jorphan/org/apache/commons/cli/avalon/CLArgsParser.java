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

import java.text.ParseException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Parser for command line arguments.
 *
 * This parses command lines according to the standard (?) of GNU utilities.
 *
 * Note: This is still used in 1.1 libraries so do not add 1.2+ dependencies.
 *
 * Note that CLArgs uses a backing hashtable for the options index and so
 * duplicate arguments are only returned by getArguments().
 *
 * @see ParserControl
 * @see CLOption
 * @see CLOptionDescriptor
 */
public final class CLArgsParser {
    // cached character == Integer.MAX_VALUE when invalid
    private static final int INVALID = Integer.MAX_VALUE;

    private static final int STATE_NORMAL = 0;

    private static final int STATE_REQUIRE_2ARGS = 1;

    private static final int STATE_REQUIRE_ARG = 2;

    private static final int STATE_OPTIONAL_ARG = 3;

    private static final int STATE_NO_OPTIONS = 4;

    private static final int STATE_OPTION_MODE = 5;

    // Values for creating tokens
    private static final int TOKEN_SEPARATOR = 0;

    private static final int TOKEN_STRING = 1;

    private static final char[] ARG_SEPARATORS = new char[] { (char) 0, '=' };

    private static final char[] NULL_SEPARATORS = new char[] { (char) 0 };

    private final CLOptionDescriptor[] optionDescriptors;

    private final Vector<CLOption> options;

    // Key is String or Integer
    private Hashtable<Object, CLOption> optionIndex;

    private final ParserControl control;

    private String errorMessage;

    private String[] unparsedArgs = new String[] {};

    // variables used while parsing options.
    private char ch;

    private String[] args;

    private boolean isLong;

    private int argIndex;

    private int stringIndex;

    private int stringLength;

    private int lastChar = INVALID;

    private int lastOptionId;

    private CLOption option;

    private int state = STATE_NORMAL;

    /**
     * Retrieve an array of arguments that have not been parsed due to the
     * parser halting.
     *
     * @return an array of unparsed args
     */
    public final String[] getUnparsedArgs() {
        return this.unparsedArgs;
    }

    /**
     * Retrieve a list of options that were parsed from command list.
     *
     * @return the list of options
     */
    public final Vector<CLOption> getArguments() {
        return this.options;
    }

    /**
     * Retrieve the {@link CLOption} with specified id, or <code>null</code>
     * if no command line option is found.
     *
     * @param id
     *            the command line option id
     * @return the {@link CLOption} with the specified id, or <code>null</code>
     *         if no CLOption is found.
     * @see CLOption
     */
    public final CLOption getArgumentById(final int id) {
        return this.optionIndex.get(Integer.valueOf(id));
    }

    /**
     * Retrieve the {@link CLOption} with specified name, or <code>null</code>
     * if no command line option is found.
     *
     * @param name
     *            the command line option name
     * @return the {@link CLOption} with the specified name, or
     *         <code>null</code> if no CLOption is found.
     * @see CLOption
     */
    public final CLOption getArgumentByName(final String name) {
        return this.optionIndex.get(name);
    }

    /**
     * Get Descriptor for option id.
     *
     * @param id
     *            the id
     * @return the descriptor
     */
    private CLOptionDescriptor getDescriptorFor(final int id) {
        for (CLOptionDescriptor optionDescriptor : this.optionDescriptors) {
            if (optionDescriptor.getId() == id) {
                return optionDescriptor;
            }
        }

        return null;
    }

    /**
     * Retrieve a descriptor by name.
     *
     * @param name
     *            the name
     * @return the descriptor
     */
    private CLOptionDescriptor getDescriptorFor(final String name) {
        for (CLOptionDescriptor optionDescriptor : this.optionDescriptors) {
            if (optionDescriptor.getName().equals(name)) {
                return optionDescriptor;
            }
        }

        return null;
    }

    /**
     * Retrieve an error message that occurred during parsing if one existed.
     *
     * @return the error string
     */
    public final String getErrorString() {
        return this.errorMessage;
    }

    /**
     * Require state to be placed in for option.
     *
     * @param descriptor
     *            the Option Descriptor
     * @return the state
     */
    private int getStateFor(final CLOptionDescriptor descriptor) {
        final int flags = descriptor.getFlags();
        if ((flags & CLOptionDescriptor.ARGUMENTS_REQUIRED_2) == CLOptionDescriptor.ARGUMENTS_REQUIRED_2) {
            return STATE_REQUIRE_2ARGS;
        } else if ((flags & CLOptionDescriptor.ARGUMENT_REQUIRED) == CLOptionDescriptor.ARGUMENT_REQUIRED) {
            return STATE_REQUIRE_ARG;
        } else if ((flags & CLOptionDescriptor.ARGUMENT_OPTIONAL) == CLOptionDescriptor.ARGUMENT_OPTIONAL) {
            return STATE_OPTIONAL_ARG;
        } else {
            return STATE_NORMAL;
        }
    }

    /**
     * Create a parser that can deal with options and parses certain args.
     *
     * @param args
     *            the args, typically that passed to the
     *            <code>public static void main(String[] args)</code> method.
     * @param optionDescriptors
     *            the option descriptors
     * @param control
     *            the parser control used determine behaviour of parser
     */
    public CLArgsParser(final String[] args, final CLOptionDescriptor[] optionDescriptors, final ParserControl control) {
        this.optionDescriptors = optionDescriptors;
        this.control = control;
        this.options = new Vector<>();
        this.args = args;

        try {
            parse();
            checkIncompatibilities(this.options);
            buildOptionIndex();
        } catch (final ParseException pe) {
            this.errorMessage = pe.getMessage();
        }
    }

    /**
     * Check for duplicates of an option. It is an error to have duplicates
     * unless appropriate flags is set in descriptor.
     *
     * @param arguments
     *            the arguments
     */
    private void checkIncompatibilities(final Vector<CLOption> arguments) throws ParseException {
        final int size = arguments.size();

        for (int i = 0; i < size; i++) {
            final CLOption option = arguments.elementAt(i);
            final int id = option.getDescriptor().getId();
            final CLOptionDescriptor descriptor = getDescriptorFor(id);

            // this occurs when id == 0 and user has not supplied a descriptor
            // for arguments
            if (null == descriptor) {
                continue;
            }

            final int[] incompatible = descriptor.getIncompatible();

            checkIncompatible(arguments, incompatible, i);
        }
    }

    private void checkIncompatible(final Vector<CLOption> arguments, final int[] incompatible, final int original)
            throws ParseException {
        final int size = arguments.size();

        for (int i = 0; i < size; i++) {
            if (original == i) {
                continue;
            }

            final CLOption option = arguments.elementAt(i);
            final int id = option.getDescriptor().getId();

            for (int anIncompatible : incompatible) {
                if (id == anIncompatible) {
                    final CLOption originalOption = arguments.elementAt(original);
                    final int originalId = originalOption.getDescriptor().getId();

                    String message = null;

                    if (id == originalId) {
                        message = "Duplicate options for " + describeDualOption(originalId) + " found.";
                    } else {
                        message = "Incompatible options -" + describeDualOption(id) + " and "
                                + describeDualOption(originalId) + " found.";
                    }
                    throw new ParseException(message, 0);
                }
            }
        }
    }

    private String describeDualOption(final int id) {
        final CLOptionDescriptor descriptor = getDescriptorFor(id);
        if (null == descriptor) {
            return "<parameter>";
        } else {
            final StringBuilder sb = new StringBuilder();
            boolean hasCharOption = false;

            if (Character.isLetter((char) id)) {
                sb.append('-');
                sb.append((char) id);
                hasCharOption = true;
            }

            final String longOption = descriptor.getName();
            if (null != longOption) {
                if (hasCharOption) {
                    sb.append('/');
                }
                sb.append("--");
                sb.append(longOption);
            }

            return sb.toString();
        }
    }

    /**
     * Create a parser that deals with options and parses certain args.
     *
     * @param args
     *            the args
     * @param optionDescriptors
     *            the option descriptors
     */
    public CLArgsParser(final String[] args, final CLOptionDescriptor[] optionDescriptors) {
        this(args, optionDescriptors, null);
    }

    /**
     * Create a string array that is subset of input array. The sub-array should
     * start at array entry indicated by index. That array element should only
     * include characters from charIndex onwards.
     *
     * @param array
     *            the original array
     * @param index
     *            the cut-point in array
     * @param charIndex
     *            the cut-point in element of array
     * @return the result array
     */
    private String[] subArray(final String[] array, final int index, final int charIndex) {
        final int remaining = array.length - index;
        final String[] result = new String[remaining];

        if (remaining > 1) {
            System.arraycopy(array, index + 1, result, 1, remaining - 1);
        }

        result[0] = array[index].substring(charIndex - 1);

        return result;
    }

    /**
     * Actually parse arguments
     */
    private void parse() throws ParseException {
        if (0 == this.args.length) {
            return;
        }

        this.stringLength = this.args[this.argIndex].length();

        while (true) {
            this.ch = peekAtChar();

            if (this.argIndex >= this.args.length) {
                break;
            }

            if (null != this.control && this.control.isFinished(this.lastOptionId)) {
                // this may need mangling due to peeks
                this.unparsedArgs = subArray(this.args, this.argIndex, this.stringIndex);
                return;
            }

            if (STATE_OPTION_MODE == this.state) {
                // if get to an arg barrier then return to normal mode
                // else continue accumulating options
                if (0 == this.ch) {
                    getChar(); // strip the null
                    this.state = STATE_NORMAL;
                } else {
                    parseShortOption();
                }
            } else if (STATE_NORMAL == this.state) {
                parseNormal();
            } else if (STATE_NO_OPTIONS == this.state) {
                // should never get to here when stringIndex != 0
                addOption(new CLOption(this.args[this.argIndex++]));
            } else {
                parseArguments();
            }
        }

        // Reached end of input arguments - perform final processing
        if (this.option != null) {
            if (STATE_OPTIONAL_ARG == this.state) {
                this.options.addElement(this.option);
            } else if (STATE_REQUIRE_ARG == this.state) {
                final CLOptionDescriptor descriptor = getDescriptorFor(this.option.getDescriptor().getId());
                final String message = "Missing argument to option " + getOptionDescription(descriptor);
                throw new ParseException(message, 0);
            } else if (STATE_REQUIRE_2ARGS == this.state) {
                if (1 == this.option.getArgumentCount()) {
                    this.option.addArgument("");
                    this.options.addElement(this.option);
                } else {
                    final CLOptionDescriptor descriptor = getDescriptorFor(this.option.getDescriptor().getId());
                    final String message = "Missing argument to option " + getOptionDescription(descriptor);
                    throw new ParseException(message, 0);
                }
            } else {
                throw new ParseException("IllegalState " + this.state + ": " + this.option, 0);
            }
        }
    }

    private String getOptionDescription(final CLOptionDescriptor descriptor) {
        if (this.isLong) {
            return "--" + descriptor.getName();
        } else {
            return "-" + (char) descriptor.getId();
        }
    }

    private char peekAtChar() {
        if (INVALID == this.lastChar) {
            this.lastChar = readChar();
        }
        return (char) this.lastChar;
    }

    private char getChar() {
        if (INVALID != this.lastChar) {
            final char result = (char) this.lastChar;
            this.lastChar = INVALID;
            return result;
        } else {
            return readChar();
        }
    }

    private char readChar() {
        if (this.stringIndex >= this.stringLength) {
            this.argIndex++;
            this.stringIndex = 0;

            if (this.argIndex < this.args.length) {
                this.stringLength = this.args[this.argIndex].length();
            } else {
                this.stringLength = 0;
            }

            return 0;
        }

        if (this.argIndex >= this.args.length) {
            return 0;
        }

        return this.args[this.argIndex].charAt(this.stringIndex++);
    }

    private char tokesep; // Keep track of token separator

    private Token nextToken(final char[] separators) {
        this.ch = getChar();

        if (isSeparator(this.ch, separators)) {
            this.tokesep = this.ch;
            this.ch = getChar();
            return new Token(TOKEN_SEPARATOR, null);
        }

        final StringBuilder sb = new StringBuilder();

        do {
            sb.append(this.ch);
            this.ch = getChar();
        } while (!isSeparator(this.ch, separators));

        this.tokesep = this.ch;
        return new Token(TOKEN_STRING, sb.toString());
    }

    private boolean isSeparator(final char ch, final char[] separators) {
        for (char separator : separators) {
            if (ch == separator) {
                return true;
            }
        }

        return false;
    }

    private void addOption(final CLOption option) {
        this.options.addElement(option);
        this.lastOptionId = option.getDescriptor().getId();
        this.option = null;
    }

    private void parseOption(final CLOptionDescriptor descriptor, final String optionString)
            throws ParseException {
        if (null == descriptor) {
            throw new ParseException("Unknown option " + optionString, 0);
        }

        this.state = getStateFor(descriptor);
        this.option = new CLOption(descriptor);

        if (STATE_NORMAL == this.state) {
            addOption(this.option);
        }
    }

    private void parseShortOption() throws ParseException {
        this.ch = getChar();
        final CLOptionDescriptor descriptor = getDescriptorFor(this.ch);
        this.isLong = false;
        parseOption(descriptor, "-" + this.ch);

        if (STATE_NORMAL == this.state) {
            this.state = STATE_OPTION_MODE;
        }
    }

    private void parseArguments() throws ParseException {
        if (STATE_REQUIRE_ARG == this.state) {
            if ('=' == this.ch || 0 == this.ch) {
                getChar();
            }

            final Token token = nextToken(NULL_SEPARATORS);
            this.option.addArgument(token.getValue());

            addOption(this.option);
            this.state = STATE_NORMAL;
        } else if (STATE_OPTIONAL_ARG == this.state) {
            if ('-' == this.ch || 0 == this.ch) {
                getChar(); // consume stray character
                addOption(this.option);
                this.state = STATE_NORMAL;
                return;
            }

            if (this.isLong && '=' != this.tokesep){ // Long optional arg must have = as separator
                addOption(this.option);
                this.state = STATE_NORMAL;
                return;
            }

            if ('=' == this.ch) {
                getChar();
            }

            final Token token = nextToken(NULL_SEPARATORS);
            this.option.addArgument(token.getValue());

            addOption(this.option);
            this.state = STATE_NORMAL;
        } else if (STATE_REQUIRE_2ARGS == this.state) {
            if (0 == this.option.getArgumentCount()) {
                /*
                 * Fix bug: -D arg1=arg2 was causing parse error; however
                 * --define arg1=arg2 is OK This seems to be because the parser
                 * skips the terminator for the long options, but was not doing
                 * so for the short options.
                 */
                if (!this.isLong) {
                    if (0 == peekAtChar()) {
                        getChar();
                    }
                }
                final Token token = nextToken(ARG_SEPARATORS);

                if (TOKEN_SEPARATOR == token.getType()) {
                    final CLOptionDescriptor descriptor = getDescriptorFor(this.option.getDescriptor().getId());
                    final String message = "Unable to parse first argument for option "
                            + getOptionDescription(descriptor);
                    throw new ParseException(message, 0);
                } else {
                    this.option.addArgument(token.getValue());
                }
                // Are we about to start a new option?
                if (0 == this.ch && '-' == peekAtChar()) {
                    // Yes, so the second argument is missing
                    this.option.addArgument("");
                    this.options.addElement(this.option);
                    this.state = STATE_NORMAL;
                }
            } else // 2nd argument
            {
                final StringBuilder sb = new StringBuilder();

                this.ch = getChar();
                while (!isSeparator(this.ch, NULL_SEPARATORS)) {
                    sb.append(this.ch);
                    this.ch = getChar();
                }

                final String argument = sb.toString();

                this.option.addArgument(argument);
                addOption(this.option);
                this.option = null;
                this.state = STATE_NORMAL;
            }
        }
    }

    /**
     * Parse Options from Normal mode.
     */
    private void parseNormal() throws ParseException {
        if ('-' != this.ch) {
            // Parse the arguments that are not options
            final String argument = nextToken(NULL_SEPARATORS).getValue();
            addOption(new CLOption(argument));
            this.state = STATE_NORMAL;
        } else {
            getChar(); // strip the -

            if (0 == peekAtChar()) {
                throw new ParseException("Malformed option -", 0);
            } else {
                this.ch = peekAtChar();

                // if it is a short option then parse it else ...
                if ('-' != this.ch) {
                    parseShortOption();
                } else {
                    getChar(); // strip the -
                    // -- sequence .. it can either mean a change of state
                    // to STATE_NO_OPTIONS or else a long option

                    if (0 == peekAtChar()) {
                        getChar();
                        this.state = STATE_NO_OPTIONS;
                    } else {
                        // its a long option
                        final String optionName = nextToken(ARG_SEPARATORS).getValue();
                        final CLOptionDescriptor descriptor = getDescriptorFor(optionName);
                        this.isLong = true;
                        parseOption(descriptor, "--" + optionName);
                    }
                }
            }
        }
    }

    /**
     * Build the this.optionIndex lookup map for the parsed options.
     */
    private void buildOptionIndex() {
        final int size = this.options.size();
        this.optionIndex = new Hashtable<>(size * 2);

        for (final CLOption option : this.options) {
            final CLOptionDescriptor optionDescriptor = getDescriptorFor(option.getDescriptor().getId());

            this.optionIndex.put(Integer.valueOf(option.getDescriptor().getId()), option);

            if (null != optionDescriptor && null != optionDescriptor.getName()) {
                this.optionIndex.put(optionDescriptor.getName(), option);
            }
        }
    }
}
