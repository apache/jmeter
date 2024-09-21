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

package org.apache.jmeter.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.junit.jupiter.api.Test;

public class TestCSVSaveService extends JMeterTestCase {

    private void checkSplitString(String input, char delim, String[] expected) throws Exception {
        String[] out = CSVSaveService.csvSplitString(input, delim);
        checkStrings(expected, out);
    }

    private void checkStrings(String[] expected, String[] out) {
        assertEquals(expected.length, out.length, "Incorrect number of strings returned");
        for(int i = 0; i < out.length; i++){
           assertEquals(expected[i], out[i], "Incorrect entry returned");
        }
    }

    // This is what JOrphanUtils.split() does
    @Test
    public void testSplitEmpty() throws Exception {
        checkSplitString("",         ',', new String[]{});
    }

    // These tests should agree with those for JOrphanUtils.split() as far as possible

    @Test
    public void testSplitUnquoted() throws Exception {
        checkSplitString("a",         ',', new String[]{"a"});
        checkSplitString("a,bc,d,e", ',', new String[]{"a","bc","d","e"});
        checkSplitString(",bc,d,e",  ',', new String[]{"","bc","d","e"});
        checkSplitString("a,,d,e",   ',', new String[]{"a","","d","e"});
        checkSplitString("a,bc, ,e", ',', new String[]{"a","bc"," ","e"});
        checkSplitString("a,bc,d, ", ',', new String[]{"a","bc","d"," "});
        checkSplitString("a,bc,d,",  ',', new String[]{"a","bc","d",""});
        checkSplitString("a,bc,,",   ',', new String[]{"a","bc","",""});
        checkSplitString("a,,,",     ',', new String[]{"a","","",""});
        checkSplitString("a,bc,d,\n",',', new String[]{"a","bc","d",""});

        // \u00e7 = LATIN SMALL LETTER C WITH CEDILLA
        // \u00e9 = LATIN SMALL LETTER E WITH ACUTE
        checkSplitString("a,b\u00e7,d,\u00e9", ',', new String[]{"a","b\u00e7","d","\u00e9"});
    }

    @Test
    public void testSplitQuoted() throws Exception {
        checkSplitString("a,bc,d,e",     ',', new String[]{"a","bc","d","e"});
        checkSplitString(",bc,d,e",      ',', new String[]{"","bc","d","e"});
        checkSplitString("\"\",bc,d,e",  ',', new String[]{"","bc","d","e"});
        checkSplitString("a,,d,e",       ',', new String[]{"a","","d","e"});
        checkSplitString("a,\"\",d,e",   ',', new String[]{"a","","d","e"});
        checkSplitString("a,bc, ,e",     ',', new String[]{"a","bc"," ","e"});
        checkSplitString("a,bc,\" \",e", ',', new String[]{"a","bc"," ","e"});
        checkSplitString("a,bc,d, ",     ',', new String[]{"a","bc","d"," "});
        checkSplitString("a,bc,d,\" \"", ',', new String[]{"a","bc","d"," "});
        checkSplitString("a,bc,d,",      ',', new String[]{"a","bc","d",""});
        checkSplitString("a,bc,d,\"\"",  ',', new String[]{"a","bc","d",""});
        checkSplitString("a,bc,d,\"\"\n",',', new String[]{"a","bc","d",""});

        // \u00e7 = LATIN SMALL LETTER C WITH CEDILLA
        // \u00e9 = LATIN SMALL LETTER E WITH ACUTE
        checkSplitString("\"a\",\"b\u00e7\",\"d\",\"\u00e9\"", ',', new String[]{"a","b\u00e7","d","\u00e9"});
    }

    @Test
    public void testSplitBadQuote() throws Exception {
        assertThrows(IOException.class, () -> checkSplitString("a\"b", ',', new String[] {}));
    }

    @Test
    public void testSplitMultiLine() throws Exception  {
        String line="a,,\"c\nd\",e\n,,f,g,\n\n";
        String[] out;
        BufferedReader br = new BufferedReader(new StringReader(line));
        out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{"a","","c\nd","e"}, out);
        out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{"","","f","g",""}, out);
        out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{""}, out); // Blank line
        assertEquals(-1, br.read(), "Expected to be at EOF");
        // Empty strings at EOF
        out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{}, out);
        out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{}, out);
    }

    @Test
    public void testBlankLine() throws Exception  {
        BufferedReader br = new BufferedReader(new StringReader("\n"));
        String[] out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{""}, out);
        assertEquals(-1, br.read(), "Expected to be at EOF");
    }

    @Test
    public void testBlankLineQuoted() throws Exception  {
        BufferedReader br = new BufferedReader(new StringReader("\"\"\n"));
        String[] out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{""}, out);
        assertEquals(-1, br.read(), "Expected to be at EOF");
    }

    @Test
    public void testEmptyFile() throws Exception  {
        BufferedReader br = new BufferedReader(new StringReader(""));
        String[] out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{}, out);
        assertEquals(-1, br.read(), "Expected to be at EOF");
    }

    @Test
    public void testShortFile() throws Exception  {
        BufferedReader br = new BufferedReader(new StringReader("a"));
        String[] out = CSVSaveService.csvReadFile(br, ',');
        checkStrings(new String[]{"a"}, out);
        assertEquals(-1, br.read(), "Expected to be at EOF");
    }

    @Test
    // header text should not change unexpectedly
    // if this test fails, check whether the default was intentionally changed or not
    public void testHeader() {
        final String HDR = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,"
                + "failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";
        assertEquals(HDR, CSVSaveService.printableFieldNamesToString(), "Header text has changed");
    }

    @Test
    // sample format should not change unexpectedly
    // if this test fails, check whether the default was intentionally changed or not
    public void testSample() throws MalformedURLException {
        final String RESULT = "1,2,3,4,5,6,7,true,,8,9,10,11,https://jmeter.apache.org,12,13,14";
        SampleResult result = new SampleResult();
        result.setSaveConfig(new SampleSaveConfiguration());
        result.setStampAndTime(1, 2);
        result.setSampleLabel("3");
        result.setResponseCode("4");
        result.setResponseMessage("5");
        result.setThreadName("6");
        result.setDataType("7");
        result.setSuccessful(true);
        result.setBytes(8L);
        result.setURL(new URL("https://jmeter.apache.org"));
        result.setSentBytes(9);
        result.setGroupThreads(10);
        result.setAllThreads(11);
        result.setLatency(12);
        result.setIdleTime(13);
        result.setConnectTime(14);

        assertEquals(RESULT, CSVSaveService.resultToDelimitedString(new SampleEvent(result,"")), "Result text has changed");
    }
}
