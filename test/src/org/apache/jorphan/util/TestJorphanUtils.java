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

/**
 * Package to test JOrphanUtils methods 
 */
     
package org.apache.jorphan.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

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
        assertThat("Ignore trailing split chars", JOrphanUtils.split("a,bc,,", ",", true),
                CoreMatchers.equalTo(new String[] { "a", "bc" }));
    }

    @Test
    public void testSplitStringStringFalseWithTrailingSplitChars() {
         // Test ignore trailing split characters
        assertThat("Include the trailing split chars", JOrphanUtils.split("a,bc,,", ",", false),
                CoreMatchers.equalTo(new String[] { "a", "bc", "", "" }));
    }

    @Test
    public void testSplitStringStringTrueWithLeadingSplitChars() {
        // Test leading split characters
        assertThat("Ignore leading split chars", JOrphanUtils.split(",,a,bc", ",", true),
                CoreMatchers.equalTo(new String[] { "a", "bc" }));
    }

    @Test
    public void testSplitStringStringFalseWithLeadingSplitChars() {
        // Test leading split characters
        assertThat("Include leading split chars", JOrphanUtils.split(",,a,bc", ",", false),
                CoreMatchers.equalTo(new String[] { "", "", "a", "bc" }));
    }
    
    @Test
    public void testSplit3() {
        String in = "a,bc,,"; // Test ignore trailing split characters
        String[] out = JOrphanUtils.split(in, ",",true);// Ignore adjacent delimiters
        assertThat(out, CoreMatchers.equalTo(new String[] { "a", "bc" }));
        out = JOrphanUtils.split(in, ",",false);
        assertThat(out, CoreMatchers.equalTo(new String[] { "a", "bc", "", "" }));
    }

    @Test
    public void testSplitStringStringTrueWithLeadingComplexSplitCharacters() {
        // Test leading split characters
        assertThat(JOrphanUtils.split(" , ,a ,bc", " ,", true), CoreMatchers.equalTo(new String[] { "a", "bc" }));
    }

    @Test
    public void testSplitStringStringFalseWithLeadingComplexSplitCharacters() {
        // Test leading split characters
        assertThat(JOrphanUtils.split(" , ,a ,bc", " ,", false),
                CoreMatchers.equalTo(new String[] { "", "", "a", "bc" }));
    }
    
    @Test
    public void testSplitStringStringTrueTruncate() throws Exception
    {
        assertThat(JOrphanUtils.split("a;,b;,;,;,d;,e;,;,f", ";,", true),
                CoreMatchers.equalTo(new String[] { "a", "b", "d", "e", "f" }));
    }

    @Test
    public void testSplitStringStringFalseTruncate() throws Exception
    {
        assertThat(JOrphanUtils.split("a;,b;,;,;,d;,e;,;,f", ";,", false),
                CoreMatchers.equalTo(new String[] { "a", "b", "", "", "d", "e", "", "f" }));
    }

    @Test
    public void testSplitStringStringTrueDoubledSplitChar() throws Exception
    {
        assertThat(JOrphanUtils.split("a;;b;;;;;;d;;e;;;;f", ";;", true),
                CoreMatchers.equalTo(new String[] { "a", "b", "d", "e", "f" }));
    }

    @Test
    public void testSplitStringStringFalseDoubledSplitChar() throws Exception
    {
        assertThat(JOrphanUtils.split("a;;b;;;;;;d;;e;;;;f", ";;", false),
                CoreMatchers.equalTo(new String[] { "a", "b", "", "", "d", "e", "", "f" }));
        
    }

    // Empty string
    @Test
    public void testEmpty(){
        String[] out = JOrphanUtils.split("", ",", false);
        assertEquals(0, out.length);
    }

    // Tests for split(String,String,String)
    @Test
    public void testSplitSSSSingleDelimiterWithDefaultValue() {
        // Test non-empty parameters
        assertThat(JOrphanUtils.split("a,bc,,", ",", "?"), CoreMatchers.equalTo(new String[] { "a", "bc", "?", "?" }));
    }

    @Test
    public void testSplitSSSSingleDelimiterWithEmptyValue() {
        // Empty default
        assertThat(JOrphanUtils.split("a,bc,,", ",", ""), CoreMatchers.equalTo(new String[] { "a", "bc", "", "" }));
    }

    @Test
    public void testSplitSSSEmptyDelimiter() {
        String in = "a,bc,,"; // Empty delimiter
        assertThat(JOrphanUtils.split(in, "", "?"), CoreMatchers.equalTo(new String[] { in }));
    }

    @Test
    public void testSplitSSSMultipleDelimCharsWithDefaultValue() {
        // Multiple delimiters
        assertThat(JOrphanUtils.split("a,b;c,,", ",;", "?"),
                CoreMatchers.equalTo(new String [] { "a", "b", "c", "?", "?" }));
    }

    @Test
    public void testSplitSSSMultipleDelimCharsWithEmptyValue() {
        // Multiple delimiters
        assertThat(JOrphanUtils.split("a,b;c,,", ",;", ""), CoreMatchers.equalTo(new String[] { "a", "b", "c", "", "" }));
    }

    @Test
    public void testSplitSSSSameDelimiterAsDefaultValue() {
        assertThat(JOrphanUtils.split("a,bc,,", ",", ","), CoreMatchers.equalTo(new String[] { "a", "bc", ",", "," }));
    }

    @Test(expected=NullPointerException.class)
    public void testSplitNullStringString() {
        JOrphanUtils.split(null, ",","?");
    }

    @Test(expected=NullPointerException.class)
    public void testSplitStringNullString() {
        JOrphanUtils.split("a,bc,,", null, "?");
    }

    @Test
    public void testSplitStringStringNullWithSingleDelimiter() {
        assertThat(JOrphanUtils.split("a,bc,,", ",", null), CoreMatchers.equalTo(new String[] { "a", "bc" }));
    }

    @Test
    public void testSplitStringStringNullWithMultipleDelimiter() {
        assertThat(JOrphanUtils.split("a,;bc,;,", ",;", null), CoreMatchers.equalTo(new String[] { "a", "bc" }));
    }

    @Test
    public void testSplitSSSWithEmptyInput() {
        String[] out = JOrphanUtils.split("", "," ,"x");
        assertEquals(0, out.length);
    }

    @Test
    public void testSplitSSSWithEmptyDelimiter() {
        final String in = "a,;bc,;,";
        assertThat(JOrphanUtils.split(in, "", "x"), CoreMatchers.equalTo(new String[] { in }));
    }

    @Test
    public void testreplaceAllChars(){
        assertEquals("", JOrphanUtils.replaceAllChars("",' ', "+"));
        assertEquals("source", JOrphanUtils.replaceAllChars("source",' ', "+"));
        assertEquals("so+rce", JOrphanUtils.replaceAllChars("source",'u', "+"));
        assertEquals("+so+urc+", JOrphanUtils.replaceAllChars("esoeurce",'e', "+"));
        assertEquals("AZAZsoAZurcAZ", JOrphanUtils.replaceAllChars("eesoeurce",'e', "AZ"));
        assertEquals("A+B++C+", JOrphanUtils.replaceAllChars("A B  C ",' ', "+"));
        assertEquals("A%20B%20%20C%20", JOrphanUtils.replaceAllChars("A B  C ",' ', "%20"));
    }

    @Test
    public void testTrim(){
        assertEquals("",JOrphanUtils.trim("", " ;"));
        assertEquals("",JOrphanUtils.trim(" ", " ;"));
        assertEquals("",JOrphanUtils.trim("; ", " ;"));
        assertEquals("",JOrphanUtils.trim(";;", " ;"));
        assertEquals("",JOrphanUtils.trim("  ", " ;"));
        assertEquals("abc",JOrphanUtils.trim("abc ;", " ;"));
    }
    
    @Test
    public void testbaToHexString(){
        assertEquals("",JOrphanUtils.baToHexString(new byte[]{}));
        assertEquals("00",JOrphanUtils.baToHexString(new byte[]{0}));
        assertEquals("0f107f8081ff",JOrphanUtils.baToHexString(new byte[]{15,16,127,-128,-127,-1}));
    }

    @Test
    public void testbaToByte() throws Exception{
        assertEqualsArray(new byte[]{},JOrphanUtils.baToHexBytes(new byte[]{}));
        assertEqualsArray(new byte[]{'0','0'},JOrphanUtils.baToHexBytes(new byte[]{0}));
        assertEqualsArray("0f107f8081ff".getBytes(StandardCharsets.UTF_8),
                JOrphanUtils.baToHexBytes(new byte[] { 15, 16, 127, -128, -127, -1 }));
    }

    private void assertEqualsArray(byte[] expected, byte[] actual){
        assertEquals("arrays must be same length",expected.length, actual.length);
        for(int i=0; i < expected.length; i++){
            assertEquals("values must be the same for index: "+i,expected[i],actual[i]);
        }
    }
    
    @Test
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
    }

    @Test
    public void testReplaceAllWithRegexWithSearchValueContainedInReplaceValue() {
        // Bug 61054
        Assert.assertArrayEquals(new Object[] { "abcd", 1 },
                JOrphanUtils.replaceAllWithRegex("abc", "abc", "abcd", true));
    }

    @Test
    public void testReplaceAllWithRegex() {
        Assert.assertArrayEquals(new Object[] {"toto", 0}, 
                JOrphanUtils.replaceAllWithRegex("toto","ti", "ta", true));
        Assert.assertArrayEquals(new Object[] {"toto", 0},
                JOrphanUtils.replaceAllWithRegex("toto","TO", "TI", true));
        Assert.assertArrayEquals(new Object[] {"TITI", 2},
                JOrphanUtils.replaceAllWithRegex("toto","TO", "TI", false));
        Assert.assertArrayEquals(new Object[] {"TITI", 2},
                JOrphanUtils.replaceAllWithRegex("toto","to", "TI", true));
        Assert.assertArrayEquals(new Object[] {"TITIti", 2},
                JOrphanUtils.replaceAllWithRegex("tototi","to", "TI", true));
        Assert.assertArrayEquals(new Object[] {"TOTIti", 1},
                JOrphanUtils.replaceAllWithRegex("TOtoti","to", "TI", true));
        Assert.assertArrayEquals(new Object[] {"TOTI", 1},
                JOrphanUtils.replaceAllWithRegex("TOtoti","to.*", "TI", true));
        Assert.assertArrayEquals(new Object[] {"TOTI", 1},
                JOrphanUtils.replaceAllWithRegex("TOtoti","to.*ti", "TI", true));
        Assert.assertArrayEquals(new Object[] {"TOTITITITIaTITITIti", 7},
                JOrphanUtils.replaceAllWithRegex("TO1232a123ti","[0-9]", "TI", true));
        Assert.assertArrayEquals(new Object[] {"TOTIaTIti", 2},
                JOrphanUtils.replaceAllWithRegex("TO1232a123ti","[0-9]+", "TI", true));
        
        Assert.assertArrayEquals(new Object[] {"TO${var}2a${var}ti", 2},
                JOrphanUtils.replaceAllWithRegex("TO1232a123ti","123", "${var}", true));
        
        Assert.assertArrayEquals(new Object[] {"TO${var}2a${var}ti${var2}", 2},
                JOrphanUtils.replaceAllWithRegex("TO1232a123ti${var2}","123", "${var}", true));

    }
}
