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
package org.apache.jmeter.functions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link StringToFile}
 */
public class TestStringtoFile extends JMeterTestCase {
    protected AbstractFunction function;
    private SampleResult result;
    private static final String FILENAME = "test.txt";
    private static final String STRING_TO_WRITE = "test";
    private static final String ENCODING = StandardCharsets.UTF_8.toString();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
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
    public void testWriteToFile() throws Exception {
        try {
            function.setParameters(functionParams(FILENAME, STRING_TO_WRITE, "true", ENCODING));
            String returnValue = function.execute(result, null);
            Assert.assertTrue("This method 'Stringtofile' should have successfully run",
                    Boolean.parseBoolean(returnValue));
        } finally {
            Files.deleteIfExists(new File(FileServer.resolveBaseRelativeName(FILENAME)).toPath());
        }
    }

    @Test
    public void testWriteToFileWhenDirectoryDoesntExist() throws Exception {
        File dir = tempFolder.newFolder();
        Files.delete(dir.toPath());
        String pathname = Paths.get(dir.getAbsolutePath(), FILENAME).toString();
        function.setParameters(functionParams(pathname, STRING_TO_WRITE, "true", ENCODING));
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should fail to run since directory does not exist",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileWhenDirectoryExist() throws Exception {
        File dir = tempFolder.newFolder();
        dir.deleteOnExit();
        String pathname = Paths.get(dir.getAbsolutePath(), FILENAME).toString();
        function.setParameters(functionParams(pathname, STRING_TO_WRITE, "true", ENCODING));
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should have successfully run if parent directory already exists",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileOptParamWayToWriteIsNull() throws Exception {
        try {
            function.setParameters(functionParams(FILENAME, STRING_TO_WRITE));
            String returnValue = function.execute(result, null);
            Assert.assertTrue("This method 'Stringtofile' should have successfully run with empty append",
                    Boolean.parseBoolean(returnValue));
        } finally {
            Files.deleteIfExists(new File(FileServer.resolveBaseRelativeName(FILENAME)).toPath());
        }
    }

    @Test
    public void testWriteToFileOptParamEncodingIsNull() throws Exception {
        try {
            function.setParameters(functionParams(FILENAME, STRING_TO_WRITE, "true"));
            String returnValue = function.execute(result, null);
            Assert.assertTrue("This method 'Stringtofile' should have successfully run with no charset",
                    Boolean.parseBoolean(returnValue));
        } finally {
            Files.deleteIfExists(new File(FileServer.resolveBaseRelativeName(FILENAME)).toPath());
        }
    }

    @Test
    public void testWriteToFileEncodingNotSupported() throws Exception {
        File file = tempFolder.newFile();
        file.deleteOnExit();
        function.setParameters(functionParams(file.getAbsolutePath(), STRING_TO_WRITE, "true", "UTF-20"));
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should have failed to run with wrong charset",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileEncodingNotLegal() throws Exception {
        File file = tempFolder.newFile();
        file.deleteOnExit();
        function.setParameters(functionParams(file.getAbsolutePath(), STRING_TO_WRITE, "true", "UTFéé"));
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should have failed to run with illegal chars in charset",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileIOException() throws Exception {
        File file = tempFolder.newFile();
        file.deleteOnExit();
        Assert.assertTrue(file.getAbsolutePath() + " should be set read-only", file.setWritable(false));
        function.setParameters(functionParams(file.getAbsolutePath(), STRING_TO_WRITE, "true", ENCODING));
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should have failed to run with non writable folder",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileRequiredFilePathIsNull() throws Exception {
        function.setParameters(functionParams(null, STRING_TO_WRITE, "true", ENCODING));
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should fail to run with null file", Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileRequiredStringIsNull() throws Exception {
        File file = tempFolder.newFile();
        file.deleteOnExit();
        function.setParameters(functionParams(file.getAbsolutePath(), "", "true", ENCODING));
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should succeed with empty String to write",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testOverwrite() throws Exception {
        File file = tempFolder.newFile();
        file.deleteOnExit();
        function.setParameters(functionParams(file.getAbsolutePath(), STRING_TO_WRITE, "false", ENCODING));
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should have successfully run",
                Boolean.parseBoolean(returnValue));
        String res = FileUtils.readFileToString(file, ENCODING).trim();
        Assert.assertEquals("The string should be 'test'", "test", res);
    }

    @Test
    public void testAppend() throws Exception {
        File file = tempFolder.newFile();
        file.deleteOnExit();
        function.setParameters(functionParams(file.getAbsolutePath(), STRING_TO_WRITE, "true", ENCODING));
        Assert.assertTrue("First call to 'Stringtofile' should succeed",
                Boolean.parseBoolean(function.execute(result, null)));
        Assert.assertTrue("Second call to 'Stringtofile' should succeed",
                Boolean.parseBoolean(function.execute(result, null)));
        String res = FileUtils.readFileToString(file, ENCODING).trim();
        Assert.assertEquals("The string should be 'testtest'", "testtest", res);
    }

    private Collection<CompoundVariable> functionParams(String... args) {
        return Arrays.asList(args).stream()
                .map(CompoundVariable::new)
                .collect(Collectors.toList());
    }

    @Test
    public void testDescription() {
        Assert.assertEquals("Function 'stringtofile' should have successfully reading the configuration file 'messages.properties'",
                JMeterUtils.getResString("string_to_file_pathname"),
                function.getArgumentDesc().get(0));
    }

}
