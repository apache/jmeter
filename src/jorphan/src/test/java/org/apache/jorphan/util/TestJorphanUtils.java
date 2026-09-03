/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jorphan.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestJorphanUtils {

    @Test
    public void testReplace1() {
        assertEquals("xyzdef", JOrphanUtils.replaceFirst("abcdef", "abc", "xyz"));
    }

    @Test
    public void testReplace2() {
        assertEquals("axyzdef", JOrphanUtils.replaceFirst("abcdef", "bc", "xyz"));
    }

    @Test
    public void testReplace3() {
        assertEquals("abcxyz", JOrphanUtils.replaceFirst("abcdef", "def", "xyz"));
    }

    @Test
    public void testReplace4() {
        assertEquals("abcdef", JOrphanUtils.replaceFirst("abcdef", "bce", "xyz"));
    }

    @Test
    public void testReplace5() {
        assertEquals("abcdef", JOrphanUtils.replaceFirst("abcdef", "alt=\"\" ", ""));
    }

    @Test
    public void testReplace6() {
        assertEquals("abcdef", JOrphanUtils.replaceFirst("abcdef", "alt=\"\" ", ""));
    }

    @Test
    public void testReplace7() {
        assertEquals("alt=\"\"", JOrphanUtils.replaceFirst("alt=\"\"", "alt=\"\" ", ""));
    }

    @Test
    public void testReplace8() {
        assertEquals("img src=xyz ", JOrphanUtils.replaceFirst("img src=xyz alt=\"\" ", "alt=\"\" ", ""));
    }

    // Note: the split tests should agree as far as possible with CSVSaveService.csvSplitString()

    // Tests for split(String,String,boolean)
    @Test
    public void testSplitStringStringTrueWithTrailingSplitChars() {
        // Test ignore trailing split characters
        // Ignore adjacent delimiters
        assertArrayEquals(
                new String[]{"a", "bc"},
                JOrphanUtils.split("a,bc,,", ",", true),
                "Ignore trailing split chars");
    }

    @Test
    public void testSplitStringStringFalseWithTrailingSplitChars() {
        // Test ignore trailing split characters
        assertArrayEquals(
                new String[]{"a", "bc", "", ""},
                JOrphanUtils.split("a,bc,,", ",", false),
                "Include the trailing split chars");
    }

    @Test
    public void testSplitStringStringTrueWithLeadingSplitChars() {
        // Test leading split characters
        assertArrayEquals(
                new String[]{"a", "bc"},
                JOrphanUtils.split(",,a,bc", ",", true),
                "Ignore leading split chars");
    }

    @Test
    public void testSplitStringStringFalseWithLeadingSplitChars() {
        // Test leading split characters
        assertArrayEquals(
                new String[]{"", "", "a", "bc"},
                JOrphanUtils.split(",,a,bc", ",", false),
                "Include leading split chars");
    }

    @Test
    public void testSplit3() {
        String in = "a,bc,,"; // Test ignore trailing split characters
        String[] out = JOrphanUtils.split(in, ",", true);// Ignore adjacent delimiters
        assertArrayEquals(new String[]{"a", "bc"}, out);
        out = JOrphanUtils.split(in, ",", false);
        assertArrayEquals(new String[]{"a", "bc", "", ""}, out);
    }

    @Test
    public void testSplitStringStringTrueWithLeadingComplexSplitCharacters() {
        // Test leading split characters
        assertArrayEquals(new String[]{"a", "bc"}, JOrphanUtils.split(" , ,a ,bc", " ,", true));
    }

    @Test
    public void testSplitStringStringFalseWithLeadingComplexSplitCharacters() {
        // Test leading split characters
        assertArrayEquals(new String[]{"", "", "a", "bc"},
                JOrphanUtils.split(" , ,a ,bc", " ,", false));
    }

    @Test
    public void testSplitStringStringTrueTruncate() throws Exception {
        assertArrayEquals(new String[]{"a", "b", "d", "e", "f"},
                JOrphanUtils.split("a;,b;,;,;,d;,e;,;,f", ";,", true));
    }

    @Test
    public void testSplitStringStringFalseTruncate() throws Exception {
        assertArrayEquals(new String[]{"a", "b", "", "", "d", "e", "", "f"},
                JOrphanUtils.split("a;,b;,;,;,d;,e;,;,f", ";,", false));
    }

    @Test
    public void testSplitStringStringTrueDoubledSplitChar() throws Exception {
        assertArrayEquals(new String[]{"a", "b", "d", "e", "f"},
                JOrphanUtils.split("a;;b;;;;;;d;;e;;;;f", ";;", true));
    }

    @Test
    public void testSplitStringStringFalseDoubledSplitChar() throws Exception {
        assertArrayEquals(new String[]{"a", "b", "", "", "d", "e", "", "f"},
                JOrphanUtils.split("a;;b;;;;;;d;;e;;;;f", ";;", false));
    }

    // Empty string
    @Test
    public void testEmpty() {
        String[] out = JOrphanUtils.split("", ",", false);
        assertEquals(0, out.length);
    }

    // Tests for split(String,String,String)
    @Test
    public void testSplitSSSSingleDelimiterWithDefaultValue() {
        // Test non-empty parameters
        assertArrayEquals(new String[]{"a", "bc", "?", "?"}, JOrphanUtils.split("a,bc,,", ",", "?"));
    }

    @Test
    public void testSplitSSSSingleDelimiterWithEmptyValue() {
        // Empty default
        assertArrayEquals(new String[]{"a", "bc", "", ""}, JOrphanUtils.split("a,bc,,", ",", ""));
    }

    @Test
    public void testSplitSSSEmptyDelimiter() {
        String in = "a,bc,,"; // Empty delimiter
        assertArrayEquals(new String[]{in}, JOrphanUtils.split(in, "", "?"));
    }

    @Test
    public void testSplitSSSMultipleDelimCharsWithDefaultValue() {
        // Multiple delimiters
        assertArrayEquals(new String[]{"a", "b", "c", "?", "?"},
                JOrphanUtils.split("a,b;c,,", ",;", "?"));
    }

    @Test
    public void testSplitSSSMultipleDelimCharsWithEmptyValue() {
        // Multiple delimiters
        assertArrayEquals(new String[]{"a", "b", "c", "", ""}, JOrphanUtils.split("a,b;c,,", ",;", ""));
    }

    @Test
    public void testSplitSSSSameDelimiterAsDefaultValue() {
        assertArrayEquals(new String[]{"a", "bc", ",", ","}, JOrphanUtils.split("a,bc,,", ",", ","));
    }

    @Test
    public void testSplitNullStringString() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> JOrphanUtils.split(null, ",", "?"));
    }

    @Test
    public void testSplitStringNullString() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> JOrphanUtils.split("a,bc,,", null, "?"));
    }

    @Test
    public void testSplitStringStringNullWithSingleDelimiter() {
        assertArrayEquals(new String[]{"a", "bc"}, JOrphanUtils.split("a,bc,,", ",", null));
    }

    @Test
    public void testSplitStringStringNullWithMultipleDelimiter() {
        assertArrayEquals(new String[]{"a", "bc"}, JOrphanUtils.split("a,;bc,;,", ",;", null));
    }

    @Test
    public void testSplitSSSWithEmptyInput() {
        String[] out = JOrphanUtils.split("", ",", "x");
        assertEquals(0, out.length);
    }

    @Test
    public void testSplitSSSWithEmptyDelimiter() {
        final String in = "a,;bc,;,";
        assertArrayEquals(new String[]{in}, JOrphanUtils.split(in, "", "x"));
    }

    @Test
    public void testreplaceAllChars() {
        assertEquals("", JOrphanUtils.replaceAllChars("", ' ', "+"));
        assertEquals("source", JOrphanUtils.replaceAllChars("source", ' ', "+"));
        assertEquals("so+rce", JOrphanUtils.replaceAllChars("source", 'u', "+"));
        assertEquals("+so+urc+", JOrphanUtils.replaceAllChars("esoeurce", 'e', "+"));
        assertEquals("AZAZsoAZurcAZ", JOrphanUtils.replaceAllChars("eesoeurce", 'e', "AZ"));
        assertEquals("A+B++C+", JOrphanUtils.replaceAllChars("A B  C ", ' ', "+"));
        assertEquals("A%20B%20%20C%20", JOrphanUtils.replaceAllChars("A B  C ", ' ', "%20"));
    }

    @Test
    public void testTrim() {
        assertEquals("", JOrphanUtils.trim("", " ;"));
        assertEquals("", JOrphanUtils.trim(" ", " ;"));
        assertEquals("", JOrphanUtils.trim("; ", " ;"));
        assertEquals("", JOrphanUtils.trim(";;", " ;"));
        assertEquals("", JOrphanUtils.trim("  ", " ;"));
        assertEquals("abc", JOrphanUtils.trim("abc ;", " ;"));
    }

    @Test
    public void testGetByteArraySlice() throws Exception {
        assertArrayEquals(new byte[]{1, 2},
                JOrphanUtils.getByteArraySlice(new byte[]{0, 1, 2, 3}, 1, 2));
    }

    @Test
    public void testbaToHexString() {
        assertEquals("", JOrphanUtils.baToHexString(new byte[]{}));
        assertEquals("00", JOrphanUtils.baToHexString(new byte[]{0}));
        assertEquals("0f107f8081ff", JOrphanUtils.baToHexString(new byte[]{15, 16, 127, -128, -127, -1}));
    }

    @Test
    public void testBaToHexStringSeparator() {
        assertEquals("", JOrphanUtils.baToHexString(new byte[]{}, '-'));
        assertEquals("00", JOrphanUtils.baToHexString(new byte[]{0}, '-'));
        assertEquals("0f-10-7f-80-81-ff", JOrphanUtils.baToHexString(new byte[]{15, 16, 127, -128, -127, -1}, '-'));
    }

    @Test
    public void testbaToByte() throws Exception {
        assertArrayEquals(new byte[]{}, JOrphanUtils.baToHexBytes(new byte[]{}));
        assertArrayEquals(new byte[]{'0', '0'}, JOrphanUtils.baToHexBytes(new byte[]{0}));
        assertArrayEquals("0f107f8081ff".getBytes(StandardCharsets.UTF_8), JOrphanUtils.baToHexBytes(new byte[]{15, 16, 127, -128, -127, -1}));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testNullifyIfEmptyTrimmed() {
        assertNull(JOrphanUtils.nullifyIfEmptyTrimmed(null));
        assertNull(JOrphanUtils.nullifyIfEmptyTrimmed("\u0001"));
        assertEquals("1234", JOrphanUtils.nullifyIfEmptyTrimmed("1234"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testIsBlank() {
        assertTrue(JOrphanUtils.isBlank(""));
        assertTrue(JOrphanUtils.isBlank(null));
        assertTrue(JOrphanUtils.isBlank("    "));
        assertFalse(JOrphanUtils.isBlank(" zdazd dzd "));
    }

    @Test
    public void testRightAlign() {
        StringBuilder in = new StringBuilder("AZE");
        assertEquals("   AZE", JOrphanUtils.rightAlign(in, 6).toString());
        in = new StringBuilder("AZERTY");
        assertEquals("AZERTY", JOrphanUtils.rightAlign(in, 6).toString());
        in = new StringBuilder("baulpismuth");
        assertEquals("baulpismuth", JOrphanUtils.rightAlign(in, 6).toString());
        in = new StringBuilder("A");
        assertEquals("       A", JOrphanUtils.rightAlign(in, 8).toString());
        assertEquals("                                 foo",
                JOrphanUtils.rightAlign(new StringBuilder("foo"), 39).toString());
    }

    @Test
    public void testLeftAlign() {
        assertEquals("foo  ",
                JOrphanUtils.leftAlign(new StringBuilder("foo"), 5).toString());
        assertEquals("foo",
                JOrphanUtils.leftAlign(new StringBuilder("foo"), 2).toString());
        assertEquals("foo                                 ",
                JOrphanUtils.leftAlign(new StringBuilder("foo"), 39).toString());
    }

    @Test
    public void testBooleanToSTRING() {
        assertEquals("TRUE", JOrphanUtils.booleanToSTRING(true));
        assertEquals("FALSE", JOrphanUtils.booleanToSTRING(false));
    }

    @Test
    public void testReplaceAllWithRegexWithSearchValueContainedInReplaceValue() {
        // Bug 61054
        assertArrayEquals(new Object[]{"abcd", 1},
                JOrphanUtils.replaceAllWithRegex("abc", "abc", "abcd", true));
    }

    @Test
    public void testReplaceAllWithRegex() {
        assertArrayEquals(new Object[]{"toto", 0},
                JOrphanUtils.replaceAllWithRegex("toto", "ti", "ta", true));
        assertArrayEquals(new Object[]{"toto", 0},
                JOrphanUtils.replaceAllWithRegex("toto", "TO", "TI", true));
        assertArrayEquals(new Object[]{"TITI", 2},
                JOrphanUtils.replaceAllWithRegex("toto", "TO", "TI", false));
        assertArrayEquals(new Object[]{"TITI", 2},
                JOrphanUtils.replaceAllWithRegex("toto", "to", "TI", true));
        assertArrayEquals(new Object[]{"TITIti", 2},
                JOrphanUtils.replaceAllWithRegex("tototi", "to", "TI", true));
        assertArrayEquals(new Object[]{"TOTIti", 1},
                JOrphanUtils.replaceAllWithRegex("TOtoti", "to", "TI", true));
        assertArrayEquals(new Object[]{"TOTI", 1},
                JOrphanUtils.replaceAllWithRegex("TOtoti", "to.*", "TI", true));
        assertArrayEquals(new Object[]{"TOTI", 1},
                JOrphanUtils.replaceAllWithRegex("TOtoti", "to.*ti", "TI", true));
        assertArrayEquals(new Object[]{"TOTITITITIaTITITIti", 7},
                JOrphanUtils.replaceAllWithRegex("TO1232a123ti", "[0-9]", "TI", true));
        assertArrayEquals(new Object[]{"TOTIaTIti", 2},
                JOrphanUtils.replaceAllWithRegex("TO1232a123ti", "[0-9]+", "TI", true));

        assertArrayEquals(new Object[]{"TO${var}2a${var}ti", 2},
                JOrphanUtils.replaceAllWithRegex("TO1232a123ti", "123", "${var}", true));

        assertArrayEquals(new Object[]{"TO${var}2a${var}ti${var2}", 2},
                JOrphanUtils.replaceAllWithRegex("TO1232a123ti${var2}", "123", "${var}", true));
    }

    @Test
    public void testReplaceValueWithNullValue() {
        assertEquals(0, JOrphanUtils.replaceValue(null, null, false, null, null));
    }

    @Test
    public void testReplaceValueWithValidValueAndValidSetter() {
        Holder h = new Holder();
        assertEquals(1, JOrphanUtils.replaceValue("\\d+", "${port}", true, "80", s -> h.value = s));
        assertEquals("${port}", h.value);
    }

    private static class Holder {
        String value;
    }

    @Test
    public void testReplaceValueWithNullSetterThatGetsCalled() {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> JOrphanUtils.replaceValue("\\d+", "${port}", true, "80", null));
    }

    @Test
    public void testUnsplit() {
        assertEquals("", JOrphanUtils.unsplit(new Object[]{null, null}, 0));
        assertEquals("11", JOrphanUtils.unsplit(new Object[]{null, 1}, 1));
        assertEquals("-26738698", JOrphanUtils.unsplit(new Object[]{-26_738_698}, 1));
    }

    @Test
    public void testGenerateRandomAlphanumericPassword20() {
        String password = JOrphanUtils.generateRandomAlphanumericPassword(20);
        assertTrue(
                password.matches("[A-Za-z0-9]{20}"),
                () -> "generateRandomAlphanumericPassword(20) should match pattern \"[A-Za-z0-9]{20}\", got " + password);
    }
}
