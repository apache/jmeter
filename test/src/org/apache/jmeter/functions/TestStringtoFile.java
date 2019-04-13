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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link StringToFile}
 */
public class TestStringtoFile extends JMeterTestCase {
    protected AbstractFunction function;
    private SampleResult result;
    private Collection<CompoundVariable> params;
    private static final Logger log = LoggerFactory.getLogger(TestSimpleFunctions.class);
    private static final String DIR_NAME = "dirTest";
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
        params = new LinkedList<>();
    }

    @Before
    @After
    public void deleteFileBeforeAfterTest() {
        File file = new File(FILENAME);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            Assert.fail("File " + FILENAME + "should not exist");
        }
    }

    @Test
    public void testParameterCount() throws Exception {
        checkInvalidParameterCounts(function, 2, 4);
    }

    @Test
    public void testWriteToFile() throws Exception {
        params.add(new CompoundVariable(FILENAME));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable(ENCODING));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should have successfully run",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileWhenDirectoryDoesntExist() throws Exception {
        String pathDirectory = File.separator + DIR_NAME;
        File dir = new File(pathDirectory);
        if (dir.exists()) {
            deleteDir(dir);
        }
        String pathname = pathDirectory + File.separator + FILENAME;
        params.add(new CompoundVariable(pathname));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable(ENCODING));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should fail to run since directory does not exist", 
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileWhenDirectoryExist() throws InvalidVariableException {
        File dir = null;
        try {
            dir = tempFolder.newFolder(DIR_NAME);
        } catch (IOException e1) {
            Assert.fail("can't create the directory");
        }
        String pathname = dir.getAbsolutePath() + File.separator + FILENAME;
        params.add(new CompoundVariable(pathname));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable(ENCODING));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should have successfully run if parent directory already exists",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileOptParamWayToWriteIsNull() throws Exception {
        params.add(new CompoundVariable(FILENAME));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should have successfully run with empty append",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileOptParamEncodingIsNull() throws Exception {
        params.add(new CompoundVariable(FILENAME));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should have successfully run with no charset",
                Boolean.parseBoolean(returnValue));

    }

    @Test
    public void testWriteToFileEncodingNotSupported() throws Exception {
        File file = null;
        try {
            file = tempFolder.newFile(FILENAME);
        } catch (IOException e1) {
            Assert.fail("Can't create the file successfully");
        }
        params.add(new CompoundVariable(file.getAbsolutePath()));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable("UTF-20"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should have failed to run with wrong charset",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileEncodingNotLegal() throws Exception {
        File file = null;
        try {
            file = tempFolder.newFile(FILENAME);
        } catch (IOException e1) {
            Assert.fail("Can't create the file successfully");
        }
        params.add(new CompoundVariable(file.getAbsolutePath()));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable("UTFéé"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should have failed to run with illegal chars in charset",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileIOException() throws Exception {
        File file = null;
        try {
            file = tempFolder.newFile(FILENAME);
        } catch (IOException e1) {
            Assert.fail("Can't create the file successfully");
        }
        file.setWritable(false);
        params.add(new CompoundVariable(file.getAbsolutePath()));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable("UTF-8"));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should have failed to run with non writable folder",
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileRequiredFilePathIsNull() throws Exception {
        params.add(new CompoundVariable(null));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable(ENCODING));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertFalse("This method 'Stringtofile' should fail to run with null file", Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testWriteToFileRequiredStringIsNull() throws Exception {
        File file = null;
        try {
            file = tempFolder.newFile(FILENAME);
        } catch (IOException e1) {
            Assert.fail("Can't create the file successfully");
        }
        params.add(new CompoundVariable(file.getAbsolutePath()));
        params.add(new CompoundVariable(""));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable(ENCODING));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should succeed with empty String to write", 
                Boolean.parseBoolean(returnValue));
    }

    @Test
    public void testOverwrite() throws Exception {
        File file = null;
        try {
            file = tempFolder.newFile(FILENAME);
        } catch (IOException e1) {
            Assert.fail("Can't create the file successfully");
        }
        params.add(new CompoundVariable(file.getAbsolutePath()));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("false"));
        params.add(new CompoundVariable(ENCODING));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should have successfully run",
                Boolean.parseBoolean(returnValue));
        String res = FileUtils.readFileToString(file, ENCODING).trim();
        Assert.assertEquals("The string should be 'test'", "test", res);
    }

    @Test
    public void testAppend() throws Exception {
        File file = null;
        try {
            file = tempFolder.newFile(FILENAME);
        } catch (IOException e1) {
            Assert.fail("Can't create the file successfully");
        }
        params.add(new CompoundVariable(file.getAbsolutePath()));
        params.add(new CompoundVariable(STRING_TO_WRITE));
        params.add(new CompoundVariable("true"));
        params.add(new CompoundVariable(ENCODING));
        function.setParameters(params);
        String returnValue = function.execute(result, null);
        returnValue = function.execute(result, null);
        Assert.assertTrue("This method 'Stringtofile' should have successfully run",
                Boolean.parseBoolean(returnValue));
        String res = FileUtils.readFileToString(file, ENCODING).trim();
        Assert.assertEquals("The string should be 'testtest'", "testtest", res);
    }

    @Test
    public void testDescription() {
        Assert.assertEquals("Function 'stringtofile' should have successfully reading the configuration file 'messages.properties'", 
                JMeterUtils.getResString("string_to_file_pathname"),
                function.getArgumentDesc().get(0));
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        try {
            Files.deleteIfExists(dir.toPath());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
