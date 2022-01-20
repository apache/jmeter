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

package org.apache.jmeter.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TestStringtoFile extends JMeterTestCase {

    protected AbstractFunction function;
    private SampleResult result;
    private static final String FILENAME = "test.txt";
    private static final String STRING_TO_WRITE = "test";
    private static final String ENCODING = StandardCharsets.UTF_8.toString();

    @BeforeEach
    public void setUp() {
        function = new StringToFile();
        result = new SampleResult();
        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
    }

    @Test
    public void testParameterCount() throws Exception {
        checkInvalidParameterCounts(function, 2, 4);
    }

    @Test
    public void testWriteToFile(@TempDir Path tempDir) throws Exception {
        String tempAbsolutePath = tempDir.resolve("output.txt").toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, STRING_TO_WRITE, "true", ENCODING));
        String returnValue = function.execute(result, null);
        assertTrue(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should have successfully run");
    }

    @Test
    public void testWriteToFileWhenDirectoryDoesntExist() throws Exception {
        String pathname = Paths.get("/does/not.exist", FILENAME).toString();
        function.setParameters(functionParams(pathname, STRING_TO_WRITE, "true", ENCODING));
        String returnValue = function.execute(result, null);
        assertFalse(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should fail to run since directory does not exist");
    }

    @Test
    public void testWriteToFileWhenDirectoryExist(@TempDir Path tempDir) throws Exception {
        String pathname = tempDir.resolve(FILENAME).toString();
        function.setParameters(functionParams(pathname, STRING_TO_WRITE, "true", ENCODING));
        String returnValue = function.execute(result, null);
        assertTrue(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should have successfully run if parent directory already exists");
    }

    @Test
    public void testWriteToFileOptParamWayToWriteIsNull(@TempDir Path tempDir) throws Exception {
        String tempAbsolutePath = tempDir.resolve("output.txt").toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, STRING_TO_WRITE));
        String returnValue = function.execute(result, null);
        assertTrue(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should have successfully run with empty append");
    }

    @Test
    public void testWriteToFileOptParamEncodingIsNull(@TempDir Path tempDir) throws Exception {
        String tempAbsolutePath = tempDir.resolve("output.txt").toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, STRING_TO_WRITE, "true"));
        String returnValue = function.execute(result, null);
        assertTrue(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should have successfully run with no charset");
    }

    @Test
    public void testWriteToFileEncodingNotSupported(@TempDir Path tempDir) throws Exception {
        String tempAbsolutePath = tempDir.resolve("output.txt").toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, STRING_TO_WRITE, "true", "UTF-20"));
        String returnValue = function.execute(result, null);
        assertFalse(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should have failed to run with wrong charset");
    }

    @Test
    public void testWriteToFileEncodingNotLegal(@TempDir Path tempDir) throws Exception {
        String tempAbsolutePath = tempDir.resolve("output.txt").toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, STRING_TO_WRITE, "true", "UTFéé"));
        String returnValue = function.execute(result, null);
        assertFalse(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should have failed to run with illegal chars in charset");
    }

    @Test
    public void testWriteToFileIOException(@TempDir Path tempDir) throws Exception {
        File file = new File(tempDir.toAbsolutePath() + "/output_ro.txt");
        try {
            assertTrue(file.createNewFile() && file.setWritable(false), file + " should be set read-only");
            String tempAbsolutePath = file.getAbsolutePath();
            function.setParameters(functionParams(tempAbsolutePath, STRING_TO_WRITE, "true", ENCODING));
            String returnValue = function.execute(result, null);
            assertFalse(Boolean.parseBoolean(returnValue),
                    "This method 'Stringtofile' should have failed to run with non writable folder");
        } finally {
            file.setWritable(true);
        }
    }

    @Test
    public void testWriteToFileRequiredFilePathIsNull() throws Exception {
        function.setParameters(functionParams(null, STRING_TO_WRITE, "true", ENCODING));
        String returnValue = function.execute(result, null);
        assertFalse(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should fail to run with null file");
    }

    @Test
    public void testWriteToFileRequiredStringIsNull(@TempDir Path tempDir) throws Exception {
        String tempAbsolutePath = tempDir.resolve("output.txt").toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, "", "true", ENCODING));
        String returnValue = function.execute(result, null);
        assertTrue(Boolean.parseBoolean(returnValue),
                "This method 'Stringtofile' should succeed with empty String to write");
    }

    @Test
    public void testOverwrite(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("output.txt");
        String tempAbsolutePath = file.toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, STRING_TO_WRITE, "false", ENCODING));
        String returnValue = function.execute(result, null);
        assertTrue(Boolean.parseBoolean(returnValue), "This method 'Stringtofile' should have successfully run");
        String res = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
        assertEquals("test", res, "The string should be 'test'");
    }

    @Test
    public void testAppend(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("output.txt");
        String tempAbsolutePath = file.toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, STRING_TO_WRITE, "true", ENCODING));
        assertTrue(Boolean.parseBoolean(function.execute(result, null)),
                "First call to 'Stringtofile' should succeed");
        assertTrue(Boolean.parseBoolean(function.execute(result, null)),
                "Second call to 'Stringtofile' should succeed");
        String res = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
        assertEquals( "testtest", res);
    }

    private Collection<CompoundVariable> functionParams(String... args) {
        return Stream.of(args).map(CompoundVariable::new).collect(Collectors.toList());
    }

    @Test
    public void testDescription() {
        assertEquals(JMeterUtils.getResString("string_to_file_pathname"), function.getArgumentDesc().get(0),
                "Function 'stringtofile' should have successfully reading the configuration file 'messages.properties'");
    }

    @Test
    public void testLineBreak(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("output.txt");
        String tempAbsolutePath = file.toAbsolutePath().toString();
        function.setParameters(functionParams(tempAbsolutePath, "test\\\\ntest", "true", ENCODING));
        function.execute();
        String res = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).trim();
        assertEquals("test" + System.lineSeparator() + "test", res,
                "When the user type '\n', ine break should be saved in file");
        assertTrue(res.contains(System.lineSeparator()),
                "When the user type '\\n',line break should be saved in file");
    }
}
