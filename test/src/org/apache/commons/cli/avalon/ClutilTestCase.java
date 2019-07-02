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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// Renamed from org.apache.avalon.excalibur.cli

import java.util.List;

import org.junit.Test;

/**
 *
 */
public final class ClutilTestCase {
    private static final String[] ARGLIST1 = new String[] { "--you", "are", "--all", "-cler", "kid" };

    private static final String[] ARGLIST2 = new String[] { "-Dstupid=idiot", "are", "--all", "here", "-d" };

    private static final String[] ARGLIST3 = new String[] {
    // duplicates
            "-Dstupid=idiot", "are", "--all", "--all", "here" };

    private static final String[] ARGLIST4 = new String[] {
    // incompatible (blee/all)
            "-Dstupid", "idiot", "are", "--all", "--blee", "here" };

    private static final String[] ARGLIST5 = new String[] { "-f", "myfile.txt" };

    private static final int DEFINE_OPT = 'D';

    private static final int CASE_CHECK_OPT = 'd';

    private static final int YOU_OPT = 'y';

    private static final int ALL_OPT = 'a';

    private static final int CLEAR1_OPT = 'c';

    private static final int CLEAR2_OPT = 'l';

    private static final int CLEAR3_OPT = 'e';

    private static final int CLEAR5_OPT = 'r';

    private static final int BLEE_OPT = 'b';

    private static final int FILE_OPT = 'f';

    private static final int TAINT_OPT = 'T';

    private static final CLOptionDescriptor DEFINE = new CLOptionDescriptor("define",
            CLOptionDescriptor.ARGUMENTS_REQUIRED_2, DEFINE_OPT, "define");

    private static final CLOptionDescriptor DEFINE_MANY = new CLOptionDescriptor("define",
            CLOptionDescriptor.ARGUMENTS_REQUIRED_2 | CLOptionDescriptor.DUPLICATES_ALLOWED, DEFINE_OPT, "define");

    private static final CLOptionDescriptor CASE_CHECK = new CLOptionDescriptor("charCheck",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, CASE_CHECK_OPT, "check character case sensitivity");

    private static final CLOptionDescriptor YOU = new CLOptionDescriptor("you", CLOptionDescriptor.ARGUMENT_DISALLOWED,
            YOU_OPT, "you");

    private static final CLOptionDescriptor CLEAR1 = new CLOptionDescriptor("c",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, CLEAR1_OPT, "c");

    private static final CLOptionDescriptor CLEAR2 = new CLOptionDescriptor("l",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, CLEAR2_OPT, "l");

    private static final CLOptionDescriptor CLEAR3 = new CLOptionDescriptor("e",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, CLEAR3_OPT, "e");

    private static final CLOptionDescriptor CLEAR5 = new CLOptionDescriptor("r",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, CLEAR5_OPT, "r");

    private static final CLOptionDescriptor BLEE = new CLOptionDescriptor("blee",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, BLEE_OPT, "blee");

    private static final CLOptionDescriptor ALL = new CLOptionDescriptor("all",
            CLOptionDescriptor.ARGUMENT_DISALLOWED,
            ALL_OPT, "all", new CLOptionDescriptor[] { BLEE });

    private static final CLOptionDescriptor FILE = new CLOptionDescriptor("file",
            CLOptionDescriptor.ARGUMENT_REQUIRED, FILE_OPT, "the build file.");

    private static final CLOptionDescriptor TAINT = new CLOptionDescriptor("taint",
            CLOptionDescriptor.ARGUMENT_OPTIONAL, TAINT_OPT, "turn on tainting checks (optional level).");

    private static final CLOptionDescriptor [] OPTIONS = new CLOptionDescriptor [] {
            new CLOptionDescriptor("none",
                    CLOptionDescriptor.ARGUMENT_DISALLOWED | CLOptionDescriptor.DUPLICATES_ALLOWED,
                    '0', "no parameter"),

            new CLOptionDescriptor("optional",
                    CLOptionDescriptor.ARGUMENT_OPTIONAL | CLOptionDescriptor.DUPLICATES_ALLOWED,
                    '?', "optional parameter"),

            new CLOptionDescriptor("one",
                    CLOptionDescriptor.ARGUMENT_REQUIRED | CLOptionDescriptor.DUPLICATES_ALLOWED,
                    '1', "one parameter"),

            new CLOptionDescriptor("two",
                    CLOptionDescriptor.ARGUMENTS_REQUIRED_2 | CLOptionDescriptor.DUPLICATES_ALLOWED,
                    '2', "two parameters")
    };

    @Test
    public void testOptionalArgWithSpace() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { ALL, TAINT };

        final String[] args = new String[] { "-T", "param", "-a" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals("Option count", 3, size);

        final CLOption option0 = clOptions.get(0);
        assertEquals("Option Code: " + option0.getDescriptor().getId(), TAINT_OPT, option0.getDescriptor().getId());
        assertEquals("Option Arg: " + option0.getArgument(0), null, option0.getArgument(0));

        final CLOption option1 = clOptions.get(1);
        assertEquals(option1.getDescriptor().getId(), CLOption.TEXT_ARGUMENT);
        assertEquals(option1.getArgument(0), "param");

        final CLOption option2 = clOptions.get(2);
        assertEquals(option2.getDescriptor().getId(), ALL_OPT);
        assertEquals(option2.getArgument(0), null);
    }

    @Test
    public void testOptionalArgLong() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { ALL, TAINT };

        // Check that optional args work woth long options
        final String[] args = new String[] { "--taint", "param", "-a" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals("Option count", 3, size);

        final CLOption option0 = clOptions.get(0);
        assertEquals("Option Code: " + option0.getDescriptor().getId(), TAINT_OPT, option0.getDescriptor().getId());
        assertEquals("Option Arg: " + option0.getArgument(0), null, option0.getArgument(0));

        final CLOption option1 = clOptions.get(1);
        assertEquals(CLOption.TEXT_ARGUMENT, option1.getDescriptor().getId());
        assertEquals("param", option1.getArgument(0));

        final CLOption option2 = clOptions.get(2);
        assertEquals(option2.getDescriptor().getId(), ALL_OPT);
        assertEquals(option2.getArgument(0), null);
    }

    @Test
    public void testOptionalArgLongEquals() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { ALL, TAINT };

        // Check that optional args work with long options
        final String[] args = new String[] { "--taint=param", "-a" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals("Option count", 2, size);

        final CLOption option0 = clOptions.get(0);
        assertEquals("Option Code: " + option0.getDescriptor().getId(), TAINT_OPT, option0.getDescriptor().getId());
        assertEquals("Option Arg: " + option0.getArgument(0), "param", option0.getArgument(0));

        final CLOption option2 = clOptions.get(1);
        assertEquals(option2.getDescriptor().getId(), ALL_OPT);
        assertEquals(option2.getArgument(0), null);
    }

    @Test
    public void testShortOptArgUnenteredBeforeOtherOpt() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { ALL, TAINT };

        final String[] args = new String[] { "-T", "-a" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals("Option count", 2, size);

        final CLOption option0 = clOptions.get(0);
        assertEquals("Option Code: " + option0.getDescriptor().getId(), TAINT_OPT, option0.getDescriptor().getId());
        assertEquals("Option Arg: " + option0.getArgument(0), null, option0.getArgument(0));

        final CLOption option1 = clOptions.get(1);
        assertEquals(option1.getDescriptor().getId(), ALL_OPT);
        assertEquals(option1.getArgument(0), null);
    }

    @Test
    public void testOptionalArgsWithArgShortBeforeOtherOpt() {
        // "-T3","-a"
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { ALL, TAINT };

        final String[] args = new String[] { "-T3", "-a" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 2);
        final CLOption option0 = clOptions.get(0);
        assertEquals(option0.getDescriptor().getId(), TAINT_OPT);
        assertEquals(option0.getArgument(0), "3");

        final CLOption option1 = clOptions.get(1);
        assertEquals(ALL_OPT, option1.getDescriptor().getId());
        assertEquals(null, option1.getArgument(0));
    }

    @Test
    public void testOptionalArgsWithArgShortEqualsBeforeOtherOpt() {
        // "-T3","-a"
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { ALL, TAINT };

        final String[] args = new String[] { "-T=3", "-a" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 2);
        final CLOption option0 = clOptions.get(0);
        assertEquals(option0.getDescriptor().getId(), TAINT_OPT);
        assertEquals(option0.getArgument(0), "3");

        final CLOption option1 = clOptions.get(1);
        assertEquals(ALL_OPT, option1.getDescriptor().getId());
        assertEquals(null, option1.getArgument(0));
    }

    @Test
    public void testOptionalArgsNoArgShortBeforeOtherOpt() {
        // "-T","-a"
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { ALL, TAINT };

        final String[] args = new String[] { "-T", "-a" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 2);
        final CLOption option0 = clOptions.get(0);
        assertEquals(TAINT_OPT, option0.getDescriptor().getId());
        assertEquals(null, option0.getArgument(0));

        final CLOption option1 = clOptions.get(1);
        assertEquals(ALL_OPT, option1.getDescriptor().getId());
        assertEquals(null, option1.getArgument(0));
    }

    @Test
    public void testFullParse() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { YOU, ALL, CLEAR1, CLEAR2, CLEAR3, CLEAR5 };

        final CLArgsParser parser = new CLArgsParser(ARGLIST1, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 8);
        assertEquals(clOptions.get(0).getDescriptor().getId(), YOU_OPT);
        assertEquals(clOptions.get(1).getDescriptor().getId(), 0);
        assertEquals(clOptions.get(2).getDescriptor().getId(), ALL_OPT);
        assertEquals(clOptions.get(3).getDescriptor().getId(), CLEAR1_OPT);
        assertEquals(clOptions.get(4).getDescriptor().getId(), CLEAR2_OPT);
        assertEquals(clOptions.get(5).getDescriptor().getId(), CLEAR3_OPT);
        assertEquals(clOptions.get(6).getDescriptor().getId(), CLEAR5_OPT);
        assertEquals(clOptions.get(7).getDescriptor().getId(), 0);
    }

    @Test
    public void testDuplicateOptions() {
        // "-Dstupid=idiot","are","--all","--all","here"
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE, ALL, CLEAR1 };

        final CLArgsParser parser = new CLArgsParser(ARGLIST3, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 5);
        assertEquals(clOptions.get(0).getDescriptor().getId(), DEFINE_OPT);
        assertEquals(clOptions.get(1).getDescriptor().getId(), 0);
        assertEquals(clOptions.get(2).getDescriptor().getId(), ALL_OPT);
        assertEquals(clOptions.get(3).getDescriptor().getId(), ALL_OPT);
        assertEquals(clOptions.get(4).getDescriptor().getId(), 0);
    }

    @Test
    public void testIncompatableOptions() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE, ALL, CLEAR1, BLEE };

        final CLArgsParser parser = new CLArgsParser(ARGLIST4, options);

        assertNotNull(parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 5);
        assertEquals(clOptions.get(0).getDescriptor().getId(), DEFINE_OPT);
        assertEquals(clOptions.get(1).getDescriptor().getId(), 0);
        assertEquals(clOptions.get(2).getDescriptor().getId(), ALL_OPT);
        assertEquals(clOptions.get(3).getDescriptor().getId(), BLEE_OPT);
        assertEquals(clOptions.get(4).getDescriptor().getId(), 0);
    }

    @Test
    public void testSingleArg() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { FILE };

        final CLArgsParser parser = new CLArgsParser(ARGLIST5, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 1);
        assertEquals(clOptions.get(0).getDescriptor().getId(), FILE_OPT);
        assertEquals(clOptions.get(0).getArgument(), "myfile.txt");
    }

    @Test
    public void testSingleArg2() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-f-=,=-" } // Check
                                                                                // delimiters
                                                                                // are
                                                                                // allowed
                , options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(FILE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals("-=,=-", clOptions.get(0).getArgument());
    }

    @Test
    public void testSingleArg3() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "--file=-=,-" } // Check
                                                                                    // delimiters
                                                                                    // are
                                                                                    // allowed
                , options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(FILE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals("-=,-", clOptions.get(0).getArgument());
    }

    @Test
    public void testSingleArg4() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "--file", "myfile.txt" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(FILE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals("myfile.txt", clOptions.get(0).getArgument());
    }

    @Test
    public void testSingleArg5() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-f", "myfile.txt" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(FILE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals("myfile.txt", clOptions.get(0).getArgument());
    }

    @Test
    public void testSingleArg6() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-f", "-=-" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(FILE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals("-=-", clOptions.get(0).getArgument());
    }

    @Test
    public void testSingleArg7() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "--file=-=-" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(FILE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals("-=-", clOptions.get(0).getArgument());
    }

    @Test
    public void testSingleArg8() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "--file", "-=-" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(FILE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals("-=-", clOptions.get(0).getArgument());
    }

    @Test
    public void testCombinedArgs1() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { BLEE, TAINT };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-bT", "rest" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();
        assertEquals(3, size);
        assertEquals(BLEE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals(TAINT_OPT, clOptions.get(1).getDescriptor().getId());
        assertEquals(0, clOptions.get(2).getDescriptor().getId());
        assertEquals("rest", clOptions.get(2).getArgument());
    }

    @Test
    public void testCombinedArgs2() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { BLEE, TAINT, FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-bT", "-fa" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();
        assertEquals(3, size);
        assertEquals(BLEE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals(TAINT_OPT, clOptions.get(1).getDescriptor().getId());
        assertEquals(FILE_OPT, clOptions.get(2).getDescriptor().getId());
        assertEquals("a", clOptions.get(2).getArgument());
    }

    @Test
    public void testCombinedArgs3() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { BLEE, TAINT, FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-bT", "--", "-fa" }// Should
                                                                                        // not
                                                                                        // detect
                                                                                        // trailing
                                                                                        // option
                , options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();
        assertEquals(3, size);
        assertEquals(BLEE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals(TAINT_OPT, clOptions.get(1).getDescriptor().getId());
        assertEquals(0, clOptions.get(2).getDescriptor().getId());
        assertEquals("-fa", clOptions.get(2).getArgument());
    }

    @Test
    public void testCombinedArgs4() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { BLEE, TAINT, FILE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-bT", "rest", "-fa" } // should
                                                                                            // detect
                                                                                            // trailing
                                                                                            // option
                , options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();
        assertEquals(4, size);
        assertEquals(BLEE_OPT, clOptions.get(0).getDescriptor().getId());
        assertEquals(TAINT_OPT, clOptions.get(1).getDescriptor().getId());
        assertEquals(0, clOptions.get(2).getDescriptor().getId());
        assertEquals("rest", clOptions.get(2).getArgument());
        assertEquals(FILE_OPT, clOptions.get(3).getDescriptor().getId());
        assertEquals("a", clOptions.get(3).getArgument());
    }

    @Test
    public void test2ArgsParse() {
        // "-Dstupid=idiot","are","--all","here"
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE, ALL, CLEAR1, CASE_CHECK };

        final CLArgsParser parser = new CLArgsParser(ARGLIST2, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 5);
        assertEquals(clOptions.get(0).getDescriptor().getId(), DEFINE_OPT);
        assertEquals(clOptions.get(1).getDescriptor().getId(), 0);
        assertEquals(clOptions.get(2).getDescriptor().getId(), ALL_OPT);
        assertEquals(clOptions.get(3).getDescriptor().getId(), 0);
        assertEquals(clOptions.get(4).getDescriptor().getId(), CASE_CHECK_OPT);

        final CLOption option = clOptions.get(0);
        assertEquals("stupid", option.getArgument(0));
        assertEquals("idiot", option.getArgument(1));
    }

    @Test
    public void test2ArgsParse2() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "--define", "a-b,c=d-e,f" }, // Check
                                                                                                    // "-"
                                                                                                    // is
                                                                                                    // allowed
                                                                                                    // in
                                                                                                    // arg2
                options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(DEFINE_OPT, clOptions.get(0).getDescriptor().getId());

        final CLOption option = clOptions.get(0);
        assertEquals("a-b,c", option.getArgument(0));
        assertEquals("d-e,f", option.getArgument(1));
    }

    @Test
    public void test2ArgsParse3() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-D", "A-b,c", "G-e,f" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(1, size);
        assertEquals(DEFINE_OPT, clOptions.get(0).getDescriptor().getId());

        final CLOption option = clOptions.get(0);
        assertEquals("A-b,c", option.getArgument(0));
        assertEquals("G-e,f", option.getArgument(1));
    }

    @Test
    public void test2ArgsParse4() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE_MANY };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-Dval1=-1", "-D", "val2=-2", "--define=val-3=-3",
                "--define", "val4-=-4" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(4, size);
        for (CLOption clOption : clOptions) {
            assertEquals(DEFINE_OPT, clOption.getDescriptor().getId());
        }

        CLOption option;
        option = clOptions.get(0);
        assertEquals("val1", option.getArgument(0));
        assertEquals("-1", option.getArgument(1));

        option = clOptions.get(1);
        assertEquals("val2", option.getArgument(0));
        assertEquals("-2", option.getArgument(1));

        option = clOptions.get(2);
        assertEquals("val-3", option.getArgument(0));
        assertEquals("-3", option.getArgument(1));

        option = clOptions.get(3);
        assertEquals("val4-", option.getArgument(0));
        assertEquals("-4", option.getArgument(1));
    }

    @Test
    public void testPartParse() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { YOU };

        final ParserControl control = new AbstractParserControl() {
            @Override
            public boolean isFinished(int lastOptionCode) {
                return lastOptionCode == YOU_OPT;
            }
        };

        final CLArgsParser parser = new CLArgsParser(ARGLIST1, options, control);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 1);
        assertEquals(clOptions.get(0).getDescriptor().getId(), YOU_OPT);
    }

    @Test
    public void test2PartParse() {
        final CLOptionDescriptor[] options1 = new CLOptionDescriptor[] { YOU };

        final CLOptionDescriptor[] options2 = new CLOptionDescriptor[] { ALL, CLEAR1, CLEAR2, CLEAR3, CLEAR5 };

        final ParserControl control1 = new AbstractParserControl() {
            @Override
            public boolean isFinished(int lastOptionCode) {
                return lastOptionCode == YOU_OPT;
            }
        };

        final CLArgsParser parser1 = new CLArgsParser(ARGLIST1, options1, control1);

        assertNull(parser1.getErrorString(), parser1.getErrorString());

        final List<CLOption> clOptions1 = parser1.getArguments();
        final int size1 = clOptions1.size();

        assertEquals(size1, 1);
        assertEquals(clOptions1.get(0).getDescriptor().getId(), YOU_OPT);

        final CLArgsParser parser2 = new CLArgsParser(parser1.getUnparsedArgs(), options2);

        assertNull(parser2.getErrorString(), parser2.getErrorString());

        final List<CLOption> clOptions2 = parser2.getArguments();
        final int size2 = clOptions2.size();

        assertEquals(size2, 7);
        assertEquals(clOptions2.get(0).getDescriptor().getId(), 0);
        assertEquals(clOptions2.get(1).getDescriptor().getId(), ALL_OPT);
        assertEquals(clOptions2.get(2).getDescriptor().getId(), CLEAR1_OPT);
        assertEquals(clOptions2.get(3).getDescriptor().getId(), CLEAR2_OPT);
        assertEquals(clOptions2.get(4).getDescriptor().getId(), CLEAR3_OPT);
        assertEquals(clOptions2.get(5).getDescriptor().getId(), CLEAR5_OPT);
        assertEquals(clOptions2.get(6).getDescriptor().getId(), 0);
    }

    @Test
    public void test2PartPartialParse() {
        final CLOptionDescriptor[] options1 = new CLOptionDescriptor[] { YOU, ALL, CLEAR1 };

        final CLOptionDescriptor[] options2 = new CLOptionDescriptor[] {};

        final ParserControl control1 = new AbstractParserControl() {
            @Override
            public boolean isFinished(final int lastOptionCode) {
                return lastOptionCode == CLEAR1_OPT;
            }
        };

        final CLArgsParser parser1 = new CLArgsParser(ARGLIST1, options1, control1);

        assertNull(parser1.getErrorString(), parser1.getErrorString());

        final List<CLOption> clOptions1 = parser1.getArguments();
        final int size1 = clOptions1.size();

        assertEquals(size1, 4);
        assertEquals(clOptions1.get(0).getDescriptor().getId(), YOU_OPT);
        assertEquals(clOptions1.get(1).getDescriptor().getId(), 0);
        assertEquals(clOptions1.get(2).getDescriptor().getId(), ALL_OPT);
        assertEquals(clOptions1.get(3).getDescriptor().getId(), CLEAR1_OPT);

        assertEquals("ler",parser1.getUnparsedArgs()[0]);

        final CLArgsParser parser2 = new CLArgsParser(parser1.getUnparsedArgs(), options2);

        assertNull(parser2.getErrorString(), parser2.getErrorString());

        final List<CLOption> clOptions2 = parser2.getArguments();
        final int size2 = clOptions2.size();

        assertEquals(size2, 2);
        assertEquals(clOptions2.get(0).getDescriptor().getId(), 0);
        assertEquals(clOptions2.get(1).getDescriptor().getId(), 0);
    }

    @Test
    public void testDuplicatesFail() {
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { YOU, ALL, CLEAR1, CLEAR2, CLEAR3, CLEAR5 };

        final CLArgsParser parser = new CLArgsParser(ARGLIST1, options);

        assertNull(parser.getErrorString(), parser.getErrorString());
    }

    @Test
    public void testIncomplete2Args() {
        // "-Dstupid="
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE };

        final CLArgsParser parser = new CLArgsParser(new String[] { "-Dstupid=" }, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 1);
        final CLOption option = clOptions.get(0);
        assertEquals(option.getDescriptor().getId(), DEFINE_OPT);
        assertEquals(option.getArgument(0), "stupid");
        assertEquals(option.getArgument(1), "");
    }

    @Test
    public void testIncomplete2ArgsMixed() {
        // "-Dstupid=","-c"
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE, CLEAR1 };

        final String[] args = new String[] { "-Dstupid=", "-c" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 2);
        assertEquals(clOptions.get(1).getDescriptor().getId(), CLEAR1_OPT);
        final CLOption option = clOptions.get(0);
        assertEquals(option.getDescriptor().getId(), DEFINE_OPT);
        assertEquals(option.getArgument(0), "stupid");
        assertEquals(option.getArgument(1), "");
    }

    @Test
    public void testIncomplete2ArgsMixedNoEq() {
        // "-Dstupid","-c"
        final CLOptionDescriptor[] options = new CLOptionDescriptor[] { DEFINE, CLEAR1 };

        final String[] args = new String[] { "-DStupid", "-c" };

        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();

        assertEquals(size, 2);
        assertEquals(clOptions.get(1).getDescriptor().getId(), CLEAR1_OPT);
        final CLOption option = clOptions.get(0);
        assertEquals(option.getDescriptor().getId(), DEFINE_OPT);
        assertEquals(option.getArgument(0), "Stupid");
        assertEquals(option.getArgument(1), "");
    }

    /**
     * Test the getArgumentById and getArgumentByName lookup methods.
     */
    @Test
    public void testArgumentLookup() {
        final String[] args = { "-f", "testarg" };
        final CLOptionDescriptor[] options = { FILE };
        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        CLOption optionById = parser.getArgumentById(FILE_OPT);
        assertNotNull(optionById);
        assertEquals(FILE_OPT, optionById.getDescriptor().getId());
        assertEquals("testarg", optionById.getArgument());

        CLOption optionByName = parser.getArgumentByName(FILE.getName());
        assertNotNull(optionByName);
        assertEquals(FILE_OPT, optionByName.getDescriptor().getId());
        assertEquals("testarg", optionByName.getArgument());
    }

    /**
     * Test that you can have null long forms.
     */
    @Test
    public void testNullLongForm() {
        final CLOptionDescriptor test = new CLOptionDescriptor(null, CLOptionDescriptor.ARGUMENT_DISALLOWED, 'n',
                "test null long form");

        final String[] args = { "-n", "testarg" };
        final CLOptionDescriptor[] options = { test };
        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final CLOption optionByID = parser.getArgumentById('n');
        assertNotNull(optionByID);
        assertEquals('n', optionByID.getDescriptor().getId());

        final CLOption optionByName = parser.getArgumentByName(FILE.getName());
        assertNull("Looking for non-existent option by name", optionByName);
    }

    /**
     * Test that you can have null descriptions.
     */
    @Test
    public void testNullDescription() {
        final CLOptionDescriptor test = new CLOptionDescriptor("nulltest", CLOptionDescriptor.ARGUMENT_DISALLOWED, 'n',
                null);

        final String[] args = { "-n", "testarg" };
        final CLOptionDescriptor[] options = { test };
        final CLArgsParser parser = new CLArgsParser(args, options);

        assertNull(parser.getErrorString(), parser.getErrorString());

        final CLOption optionByID = parser.getArgumentById('n');
        assertNotNull(optionByID);
        assertEquals('n', optionByID.getDescriptor().getId());

        final StringBuilder sb = CLUtil.describeOptions(options);
        final String lineSeparator = System.getProperty("line.separator");
        assertEquals("Testing display of null description", "\t-n, --nulltest" + lineSeparator, sb.toString());
    }

    @Test
    public void testCombinations() throws Exception {
        check(new String [] {},"");
        check(new String [] {"--none",
                             "-0"
                             },
                             "-0 -0"); // Canonical form
        check(new String [] {"--one=a",
                             "--one","A",
                             "-1b",
                             "-1=c",
                             "-1","d"
                             },
                             "-1=[a] -1=[A] -1=[b] -1=[c] -1=[d]");
        check(new String [] {"-2n=v",
                             "-2","N=V"
                             },
                             "-2=[n, v] -2=[N, V]");
        check(new String [] {"--two=n=v",
                             "--two","N=V"
                             },
                             "-2=[n, v] -2=[N, V]");
        // Test optional arguments
        check(new String [] {"-?",
                             "A", // Separate argument
                             "-?=B",
                             "-?C",
                             "-?"
                            },
                             "-? [A] -?=[B] -?=[C] -?");
        check(new String [] {"--optional=A", // OK
                             "--optional","B", // should treat B as separate
                             "--optional" // Should have no arg
                             },
                             "-?=[A] -? [B] -?");
    }

    private void check(String[] args, String canon){
        final CLArgsParser parser = new CLArgsParser(args, OPTIONS);

        assertNull(parser.getErrorString(),parser.getErrorString());

        final List<CLOption> clOptions = parser.getArguments();
        final int size = clOptions.size();
        StringBuilder sb = new StringBuilder();
        for (int i=0; i< size; i++){
            if (i>0) {
                sb.append(" ");
            }
            sb.append(clOptions.get(i).toShortString());
        }
        assertEquals("Canonical form ("+size+")",canon,sb.toString());
    }
    /*
     * TODO add tests to check for: - name clash - long option abbreviations
     * (match shortest unique abbreviation)
     */

}
