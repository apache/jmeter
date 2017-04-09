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
 * Package to test FileServer methods 
 */
     
package org.apache.jmeter.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFileServer extends JMeterTestCase {

    private static final FileServer FS = FileServer.getFileServer();
    

    @Before
    public void setUp() throws IOException {
        FS.resetBase();        
    }

    @After
    public void tearDown() throws IOException{
        FS.closeFiles();
    }
    
    @Test
    public void testopen() throws Exception {
        try {
            FS.readLine("test");
            fail("Expected IOException");
        } catch (IOException ignored){
        }
        try {
            FS.write("test","");
            fail("Expected IOException");
        } catch (IOException ignored){
        }
        assertFalse("Should not have any files open",FS.filesOpen());
        FS.closeFile("xxx"); // Unrecognised files are ignored
        assertFalse("Should not have any files open",FS.filesOpen());
        String infile=findTestPath("testfiles/test.csv");
        FS.reserveFile(infile); // Does not open file
        assertFalse("Should not have any files open",FS.filesOpen());
        assertEquals("a1,b1,c1,d1",FS.readLine(infile));
        assertTrue("Should have some files open",FS.filesOpen());
        assertNotNull(FS.readLine(infile));
        assertNotNull(FS.readLine(infile));
        assertNotNull(FS.readLine(infile));
        assertEquals("a1,b1,c1,d1",FS.readLine(infile));// Re-read 1st line
        assertNotNull(FS.readLine(infile));
        try {
            FS.write(infile,"");// should not be able to write to it ...
            fail("Expected IOException");
        } catch (IOException ignored){
        }
        FS.closeFile(infile); // does not remove the entry
        assertFalse("Should not have any files open",FS.filesOpen());
        assertEquals("a1,b1,c1,d1",FS.readLine(infile));// Re-read 1st line
        assertTrue("Should have some files open",FS.filesOpen());
        FS.closeFiles(); // removes all entries
        assertFalse("Should not have any files open",FS.filesOpen());
        try {
            FS.readLine(infile);
            fail("Expected IOException");
        } catch (IOException ignored){
        }
        infile=findTestPath("testfiles/test.csv");
        FS.reserveFile(infile); // Does not open file
        assertFalse("Should not have any files open",FS.filesOpen());
        assertEquals("a1,b1,c1,d1",FS.readLine(infile));
        try {
            FS.setBasedir("x");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ignored){
        }
        FS.closeFile(infile);
        FS.setBasedir("y");
        FS.closeFiles();
    }
    
    @Test
    public void testRelative() throws Exception {
        final String base = FileServer.getDefaultBase();
        final File basefile = new File(base);
        FS.setBaseForScript(basefile);
        assertEquals(".",FS.getBaseDirRelative().toString());
        FS.setBaseForScript(basefile.getParentFile());
        assertEquals(".",FS.getBaseDirRelative().toString());
        FS.setBaseForScript(new File(basefile.getParentFile(),"abcd/defg.jmx"));
        assertEquals(".",FS.getBaseDirRelative().toString());
        File file = new File(basefile,"abcd/defg.jmx");
        FS.setBaseForScript(file);
        assertEquals("abcd",FS.getBaseDirRelative().toString());
    }

    @Test
    public void testHeaderMissingFile() throws Exception {
        final String missing = "no-such-file";
        final String alias = "missing";
        final String charsetName = "UTF-8";

        try {
            FS.reserveFile(missing,charsetName,alias,true);
            fail("Bad filename passed to FileService.reserveFile -> IllegalArgumentException: Could not read file header line for file no-such-file");
        } catch (IllegalArgumentException ignored) {
            assertEquals("Bad filename passed to FileService.reserveFile -> exception",
                    "Could not read file header line for file no-such-file",
                    ignored.getMessage());
            assertEquals("Bad filename passed to FileService.reserveFile -> exception",
                    "File no-such-file must exist and be readable", ignored.getCause().getMessage());
        }
        // Ensure second invocation gets same behaviour
        try {
            FS.reserveFile(missing,charsetName,alias,true);
            fail("Bad filename passed to FileService.reserveFile -> IllegalArgumentException: Could not read file header line for file no-such-file");
        } catch (IllegalArgumentException ignored) {
            assertEquals("Bad filename passed to FileService.reserveFile -> exception",
                    "Could not read file header line for file no-such-file",
                    ignored.getMessage());
            assertEquals("Bad filename passed to FileService.reserveFile -> exception",
                    "File no-such-file must exist and be readable", ignored.getCause().getMessage());
        }
    }

    @Test
    public void testHeaderEmptyFile() throws Exception {
        final String empty = findTestPath("testfiles/empty.csv");
        final String alias = "empty";
        final String charsetName = "UTF-8";

        try {
            String hdr= FS.reserveFile(empty,charsetName,alias,true);
            fail("Expected IllegalArgumentException|"+hdr+"|");
        } catch (IllegalArgumentException e) {
            assertTrue("Expected EOF", e.getCause() instanceof java.io.EOFException);
        }
        // Ensure second invocation gets same behaviour
        try {
            FS.reserveFile(empty,charsetName,alias,true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue("Expected EOF", e.getCause() instanceof java.io.EOFException);
        }
    }

    @Test
    public void testResolvingPaths() {
        final File anchor = new File(findTestPath("testfiles/empty.csv"));

        // absolute
        assertTrue(FS.getResolvedFile(anchor.getAbsolutePath()).exists());

        // relative
        assertTrue(FS.getResolvedFile(anchor.getParentFile().getPath() + "/../testfiles/empty.csv").exists());
        // test-plan-relative
        FS.setBaseForScript(anchor);
        assertTrue(FS.getResolvedFile(anchor.getName()).exists());
    }
}
