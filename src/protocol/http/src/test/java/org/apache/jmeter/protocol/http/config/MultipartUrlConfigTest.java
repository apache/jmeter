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

package org.apache.jmeter.protocol.http.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MultipartUrlConfigTest {

    @Test
    @SuppressWarnings("deprecation")
    void testConstructors() {
        MultipartUrlConfig muc = new MultipartUrlConfig();
        assertEquals(0, muc.getArguments().getArgumentCount());
        assertEquals(0, muc.getHTTPFileArgs().getHTTPFileArgCount());
        muc = new MultipartUrlConfig("boundary");
        assertEquals(0, muc.getArguments().getArgumentCount());
        assertEquals(0, muc.getHTTPFileArgs().getHTTPFileArgCount());
        assertEquals("boundary", muc.getBoundary());
    }

    // TODO - should LF-only EOL be allowed?
    @Test
    void testParseArgumentsLF() {
        String queryString
            = """
                Content-Disposition: form-data; name="aa"
                Content-Type: text/plain; charset=ISO-8859-1
                Content-Transfer-Encoding: 8bit

                bb
                --7d159c1302d0y0
                Content-Disposition: form-data; name="xx"
                Content-Type: text/plain; charset=ISO-8859-1
                Content-Transfer-Encoding: 8bit

                yy
                --7d159c1302d0y0
                Content-Disposition: form-data; name="abc"
                Content-Type: text/plain; charset=ISO-8859-1
                Content-Transfer-Encoding: 8bit

                xyz \s
                xyz \s
                --7d159c1302d0y0
                Content-Disposition: form-data; name="param1"; filename="file1"
                Content-Type: text/plain
                Content-Transfer-Encoding: binary

                file content

                """;
        MultipartUrlConfig muc = new MultipartUrlConfig("7d159c1302d0y0");
        muc.parseArguments(queryString);
        HTTPFileArgs files = muc.getHTTPFileArgs();
        assertEquals(1, files.getHTTPFileArgCount());
        HTTPFileArg file = (HTTPFileArg) files.iterator().next().getObjectValue();
        assertEquals("file1", file.getPath());
        assertEquals("param1", file.getParamName());
        assertEquals("text/plain", file.getMimeType());
        Arguments args = muc.getArguments();
        assertEquals(3, args.getArgumentCount());
        Argument arg = args.getArgument(0);
        assertEquals("aa", arg.getName());
        assertEquals("bb", arg.getValue());
        arg = args.getArgument(1);
        assertEquals("xx", arg.getName());
        assertEquals("yy", arg.getValue());
        arg = args.getArgument(2);
        assertEquals("abc", arg.getName());
        assertEquals("xyz  \nxyz  ", arg.getValue());
    }

    @Test
    void testParseArgumentsCRLF() {
        String queryString
            = """
                Content-Disposition: form-data; name="aa"\r
                Content-Type: text/plain; charset=ISO-8859-1\r
                Content-Transfer-Encoding: 8bit\r
                \r
                bb\r
                --7d159c1302d0y0\r
                Content-Disposition: form-data; name="xx"\r
                Content-Type: text/plain; charset=ISO-8859-1\r
                Content-Transfer-Encoding: 8bit\r
                \r
                yy\r
                --7d159c1302d0y0\r
                Content-Disposition: form-data; name="abc"\r
                Content-Type: text/plain; charset=ISO-8859-1\r
                Content-Transfer-Encoding: 8bit\r
                \r
                xyz  \r
                xyz  \r
                --7d159c1302d0y0\r
                Content-Disposition: form-data; name="param1"; filename="file1"\r
                Content-Type: text/plain\r
                Content-Transfer-Encoding: binary\r
                \r
                file content\r
                \r
                """;
        MultipartUrlConfig muc = new MultipartUrlConfig("7d159c1302d0y0");
        muc.parseArguments(queryString);
        HTTPFileArgs files = muc.getHTTPFileArgs();
        assertEquals(1, files.getHTTPFileArgCount());
        HTTPFileArg file = (HTTPFileArg) files.iterator().next().getObjectValue();
        assertEquals("file1", file.getPath());
        assertEquals("param1", file.getParamName());
        assertEquals("text/plain", file.getMimeType());
        Arguments args = muc.getArguments();
        assertEquals(3, args.getArgumentCount());
        Argument arg = args.getArgument(0);
        assertEquals("aa", arg.getName());
        assertEquals("bb", arg.getValue());
        arg = args.getArgument(1);
        assertEquals("xx", arg.getName());
        assertEquals("yy", arg.getValue());
        arg = args.getArgument(2);
        assertEquals("abc", arg.getName());
        assertEquals("xyz  \r\nxyz  ", arg.getValue());
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> quotedMultiPartArgs() {
        String boundary = "7d159c1302d0y0";
        HTTPFileArgs fileArgs = new HTTPFileArgs();
        List<Argument> args = new ArrayList<>();
        List<String> queryLines = new ArrayList<>();
        int counter = 1;
        for (boolean quoteName: Arrays.asList(true, false)) {
            String paramName = "abc" + counter;
            counter++;
            String quoteStringName = quoteName ? '"' + paramName + '"' : paramName;
            String value = "some value for " + paramName;
            queryLines.add(String.format("Content-Disposition: form-data; name=%s", quoteStringName));
            queryLines.add("Content-Type: text/plain; charset=ISO-8859-1");
            queryLines.add("Content-Transfer-Encoding: 8bit");
            queryLines.add("");
            queryLines.add(value);
            queryLines.add("--" + boundary);
            args.add(new Argument(paramName, value));
            for (boolean quoteFilename: Arrays.asList(true, false)) {
                String filenameName = "def" + counter;
                counter++;
                String quoteStringFile = quoteFilename ? '"' +filenameName + '"' : filenameName ;
                String content = "some value for " + paramName + " and " + filenameName ;
                queryLines.add(String.format("Content-Disposition: form-data; name=%s; filename=%s",
                        quoteStringName,
                        quoteStringFile
                        ));
                queryLines.add("Content-Type: text/plain");
                queryLines.add("Content-Transfer-Encoding: binary");
                queryLines.add("");
                queryLines.add(content);
                queryLines.add("");
                queryLines.add("--" + boundary);
                fileArgs.addHTTPFileArg(filenameName, paramName, "text/plain");
            }
        }
        queryLines.remove(queryLines.size()-1);
        queryLines.add("");
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        boundary, String.join("\n", queryLines), fileArgs, args),
                org.junit.jupiter.params.provider.Arguments.of(
                        boundary, String.join("\r\n", queryLines), fileArgs, args));
    }

    @ParameterizedTest()
    @MethodSource("quotedMultiPartArgs")
    void testParseArgumentsQuotingStyle(String boundary, String queryString, HTTPFileArgs expectedFiles, List<Argument> expectedArgs) {
        MultipartUrlConfig muc = new MultipartUrlConfig(boundary);
        muc.parseArguments(queryString);
        HTTPFileArgs files = muc.getHTTPFileArgs();
        assertEquals(expectedFiles.getHTTPFileArgCount(), files.getHTTPFileArgCount());
        for (int i=0; i<files.getHTTPFileArgCount(); i++) {
            HTTPFileArg got = files.getHTTPFileArg(i);
            HTTPFileArg expected = expectedFiles.getHTTPFileArg(i);
            assertEquals(expected.getParamName(), got.getParamName());
            assertEquals(expected.getPath(), got.getPath());
            assertEquals(expected.getMimeType(), got.getMimeType());
        }
        Arguments args = muc.getArguments();
        assertEquals(expectedArgs.size(), args.getArgumentCount());
        for (int i=0; i<args.getArgumentCount(); i++) {
            Argument got = args.getArgument(i);
            Argument expected = expectedArgs.get(i);
            assertEquals(expected.getName(), got.getName());
            assertEquals(expected.getValue(), got.getValue());
        }
    }
}
