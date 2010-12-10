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

// Renamed from org.apache.avalon.excalibur.cli

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

    private final CLOptionDescriptor[] m_optionDescriptors;

    private final Vector<CLOption> m_options;

    // Key is String or Integer
    private Hashtable<Object, CLOption> m_optionIndex;

    private final ParserControl m_control;

    private String m_errorMessage;

    private String[] m_unparsedArgs = new String[] {};

    // variables used while parsing options.
    private char m_ch;

    private String[] m_args;

    private boolean m_isLong;

    private int m_argIndex;

    private int m_stringIndex;

    private int m_stringLength;

    private int m_lastChar = INVALID;

    private int m_lastOptionId;

    private CLOption m_option;

    private int m_state = STATE_NORMAL;

    /**
     * Retrieve an array of arguments that have not been parsed due to the
     * parser halting.
     *
     * @return an array of unparsed args
     */
    public final String[] getUnparsedArgs() {
        return m_unparsedArgs;
    }

    /**
     * Retrieve a list of options that were parsed from command list.
     *
     * @return the list of options
     */
    public final Vector<CLOption> getArguments() {
        // System.out.println( "Arguments: " + m_options );
        return m_options;
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
        return m_optionIndex.get(Integer.valueOf(id));
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
        return m_optionIndex.get(name);
    }

    /**
     * Get Descriptor for option id.
     *
     * @param id
     *            the id
     * @return the descriptor
     */
    private final CLOptionDescriptor getDescriptorFor(final int id) {
        for (int i = 0; i < m_optionDescriptors.length; i++) {
            if (m_optionDescriptors[i].getId() == id) {
                return m_optionDescriptors[i];
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
    private final CLOptionDescriptor getDescriptorFor(final String name) {
        for (int i = 0; i < m_optionDescriptors.length; i++) {
            if (m_optionDescriptors[i].getName().equals(name)) {
                return m_optionDescriptors[i];
            }
        }

        return null;
    }

    /**
     * Retrieve an error message that occured during parsing if one existed.
     *
     * @return the error string
     */
    public final String getErrorString() {
        // System.out.println( "ErrorString: " + m_errorMessage );
        return m_errorMessage;
    }

    /**
     * Require state to be placed in for option.
     *
     * @param descriptor
     *            the Option Descriptor
     * @return the state
     */
    private final int getStateFor(final CLOptionDescriptor descriptor) {
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
        m_optionDescriptors = optionDescriptors;
        m_control = control;
        m_options = new Vector<CLOption>();
        m_args = args;

        try {
            parse();
            checkIncompatibilities(m_options);
            buildOptionIndex();
        } catch (final ParseException pe) {
            m_errorMessage = pe.getMessage();
        }

        // System.out.println( "Built : " + m_options );
        // System.out.println( "From : " + Arrays.asList( args ) );
    }

    /**
     * Check for duplicates of an option. It is an error to have duplicates
     * unless appropriate flags is set in descriptor.
     *
     * @param arguments
     *            the arguments
     */
    private final void checkIncompatibilities(final Vector<CLOption> arguments) throws ParseException {
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

    private final void checkIncompatible(final Vector<CLOption> arguments, final int[] incompatible, final int original)
            throws ParseException {
        final int size = arguments.size();

        for (int i = 0; i < size; i++) {
            if (original == i) {
                continue;
            }

            final CLOption option = arguments.elementAt(i);
            final int id = option.getDescriptor().getId();

            for (int j = 0; j < incompatible.length; j++) {
                if (id == incompatible[j]) {
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

    private final String describeDualOption(final int id) {
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
    private final String[] subArray(final String[] array, final int index, final int charIndex) {
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
    private final void parse() throws ParseException {
        if (0 == m_args.length) {
            return;
        }

        m_stringLength = m_args[m_argIndex].length();

        while (true) {
            m_ch = peekAtChar();

            if (m_argIndex >= m_args.length) {
                break;
            }

            if (null != m_control && m_control.isFinished(m_lastOptionId)) {
                // this may need mangling due to peeks
                m_unparsedArgs = subArray(m_args, m_argIndex, m_stringIndex);
                return;
            }

            if (STATE_OPTION_MODE == m_state) {
                // if get to an arg barrier then return to normal mode
                // else continue accumulating options
                if (0 == m_ch) {
                    getChar(); // strip the null
                    m_state = STATE_NORMAL;
                } else {
                    parseShortOption();
                }
            } else if (STATE_NORMAL == m_state) {
                parseNormal();
            } else if (STATE_NO_OPTIONS == m_state) {
                // should never get to here when stringIndex != 0
                addOption(new CLOption(m_args[m_argIndex++]));
            } else {
                parseArguments();
            }
        }

        // Reached end of input arguments - perform final processing
        if (m_option != null) {
            if (STATE_OPTIONAL_ARG == m_state) {
                m_options.addElement(m_option);
            } else if (STATE_REQUIRE_ARG == m_state) {
                final CLOptionDescriptor descriptor = getDescriptorFor(m_option.getDescriptor().getId());
                final String message = "Missing argument to option " + getOptionDescription(descriptor);
                throw new ParseException(message, 0);
            } else if (STATE_REQUIRE_2ARGS == m_state) {
                if (1 == m_option.getArgumentCount()) {
                    m_option.addArgument("");
                    m_options.addElement(m_option);
                } else {
                    final CLOptionDescriptor descriptor = getDescriptorFor(m_option.getDescriptor().getId());
                    final String message = "Missing argument to option " + getOptionDescription(descriptor);
                    throw new ParseException(message, 0);
                }
            } else {
                throw new ParseException("IllegalState " + m_state + ": " + m_option, 0);
            }
        }
    }

    private final String getOptionDescription(final CLOptionDescriptor descriptor) {
        if (m_isLong) {
            return "--" + descriptor.getName();
        } else {
            return "-" + (char) descriptor.getId();
        }
    }

    private final char peekAtChar() {
        if (INVALID == m_lastChar) {
            m_lastChar = readChar();
        }
        return (char) m_lastChar;
    }

    private final char getChar() {
        if (INVALID != m_lastChar) {
            final char result = (char) m_lastChar;
            m_lastChar = INVALID;
            return result;
        } else {
            return readChar();
        }
    }

    private final char readChar() {
        if (m_stringIndex >= m_stringLength) {
            m_argIndex++;
            m_stringIndex = 0;

            if (m_argIndex < m_args.length) {
                m_stringLength = m_args[m_argIndex].length();
            } else {
                m_stringLength = 0;
            }

            return 0;
        }

        if (m_argIndex >= m_args.length) {
            return 0;
        }

        return m_args[m_argIndex].charAt(m_stringIndex++);
    }

    private char m_tokesep; // Keep track of token separator

    private final Token nextToken(final char[] separators) {
        m_ch = getChar();

        if (isSeparator(m_ch, separators)) {
            m_tokesep=m_ch;
            m_ch = getChar();
            return new Token(TOKEN_SEPARATOR, null);
        }

        final StringBuilder sb = new StringBuilder();

        do {
            sb.append(m_ch);
            m_ch = getChar();
        } while (!isSeparator(m_ch, separators));

        m_tokesep=m_ch;
        return new Token(TOKEN_STRING, sb.toString());
    }

    private final boolean isSeparator(final char ch, final char[] separators) {
        for (int i = 0; i < separators.length; i++) {
            if (ch == separators[i]) {
                return true;
            }
        }

        return false;
    }

    private final void addOption(final CLOption option) {
        m_options.addElement(option);
        m_lastOptionId = option.getDescriptor().getId();
        m_option = null;
    }

    private final void parseOption(final CLOptionDescriptor descriptor, final String optionString)
            throws ParseException {
        if (null == descriptor) {
            throw new ParseException("Unknown option " + optionString, 0);
        }

        m_state = getStateFor(descriptor);
        m_option = new CLOption(descriptor);

        if (STATE_NORMAL == m_state) {
            addOption(m_option);
        }
    }

    private final void parseShortOption() throws ParseException {
        m_ch = getChar();
        final CLOptionDescriptor descriptor = getDescriptorFor(m_ch);
        m_isLong = false;
        parseOption(descriptor, "-" + m_ch);

        if (STATE_NORMAL == m_state) {
            m_state = STATE_OPTION_MODE;
        }
    }

    private final void parseArguments() throws ParseException {
        if (STATE_REQUIRE_ARG == m_state) {
            if ('=' == m_ch || 0 == m_ch) {
                getChar();
            }

            final Token token = nextToken(NULL_SEPARATORS);
            m_option.addArgument(token.getValue());

            addOption(m_option);
            m_state = STATE_NORMAL;
        } else if (STATE_OPTIONAL_ARG == m_state) {
            if ('-' == m_ch || 0 == m_ch) {
                getChar(); // consume stray character
                addOption(m_option);
                m_state = STATE_NORMAL;
                return;
            }

            if (m_isLong && '=' != m_tokesep){ // Long optional arg must have = as separator
                addOption(m_option);
                m_state = STATE_NORMAL;
                return;
            }

            if ('=' == m_ch) {
                getChar();
            }

            final Token token = nextToken(NULL_SEPARATORS);
            m_option.addArgument(token.getValue());

            addOption(m_option);
            m_state = STATE_NORMAL;
        } else if (STATE_REQUIRE_2ARGS == m_state) {
            if (0 == m_option.getArgumentCount()) {
                /*
                 * Fix bug: -D arg1=arg2 was causing parse error; however
                 * --define arg1=arg2 is OK This seems to be because the parser
                 * skips the terminator for the long options, but was not doing
                 * so for the short options.
                 */
                if (!m_isLong) {
                    if (0 == peekAtChar()) {
                        getChar();
                    }
                }
                final Token token = nextToken(ARG_SEPARATORS);

                if (TOKEN_SEPARATOR == token.getType()) {
                    final CLOptionDescriptor descriptor = getDescriptorFor(m_option.getDescriptor().getId());
                    final String message = "Unable to parse first argument for option "
                            + getOptionDescription(descriptor);
                    throw new ParseException(message, 0);
                } else {
                    m_option.addArgument(token.getValue());
                }
                // Are we about to start a new option?
                if (0 == m_ch && '-' == peekAtChar()) {
                    // Yes, so the second argument is missing
                    m_option.addArgument("");
                    m_options.addElement(m_option);
                    m_state = STATE_NORMAL;
                }
            } else // 2nd argument
            {
                final StringBuilder sb = new StringBuilder();

                m_ch = getChar();
                while (!isSeparator(m_ch, NULL_SEPARATORS)) {
                    sb.append(m_ch);
                    m_ch = getChar();
                }

                final String argument = sb.toString();

                // System.out.println( "Arguement:" + argument );

                m_option.addArgument(argument);
                addOption(m_option);
                m_option = null;
                m_state = STATE_NORMAL;
            }
        }
    }

    /**
     * Parse Options from Normal mode.
     */
    private final void parseNormal() throws ParseException {
        if ('-' != m_ch) {
            // Parse the arguments that are not options
            final String argument = nextToken(NULL_SEPARATORS).getValue();
            addOption(new CLOption(argument));
            m_state = STATE_NORMAL;
        } else {
            getChar(); // strip the -

            if (0 == peekAtChar()) {
                throw new ParseException("Malformed option -", 0);
            } else {
                m_ch = peekAtChar();

                // if it is a short option then parse it else ...
                if ('-' != m_ch) {
                    parseShortOption();
                } else {
                    getChar(); // strip the -
                    // -- sequence .. it can either mean a change of state
                    // to STATE_NO_OPTIONS or else a long option

                    if (0 == peekAtChar()) {
                        getChar();
                        m_state = STATE_NO_OPTIONS;
                    } else {
                        // its a long option
                        final String optionName = nextToken(ARG_SEPARATORS).getValue();
                        final CLOptionDescriptor descriptor = getDescriptorFor(optionName);
                        m_isLong = true;
                        parseOption(descriptor, "--" + optionName);
                    }
                }
            }
        }
    }

    /**
     * Build the m_optionIndex lookup map for the parsed options.
     */
    private final void buildOptionIndex() {
        final int size = m_options.size();
        m_optionIndex = new Hashtable<Object, CLOption>(size * 2);

        for (int i = 0; i < size; i++) {
            final CLOption option = m_options.get(i);
            final CLOptionDescriptor optionDescriptor = getDescriptorFor(option.getDescriptor().getId());

            m_optionIndex.put(Integer.valueOf(option.getDescriptor().getId()), option);

            if (null != optionDescriptor && null != optionDescriptor.getName()) {
                m_optionIndex.put(optionDescriptor.getName(), option);
            }
        }
    }
}
