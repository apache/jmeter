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

import java.io.IOException;

import org.apache.jmeter.junit.JMeterTestCase;

public class TestFileServer extends JMeterTestCase {

    private static final FileServer FS = FileServer.getFileServer();
    
    public TestFileServer() {
        super();
    }

    public TestFileServer(String arg0) {
        super(arg0);
    }

    @Override
    public void tearDown() throws IOException{
        FS.closeFiles();
        FS.resetBase();
    }
    
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
            fail("Expected IOException");
        } catch (IOException ignored){
        }
        FS.closeFile(infile);
        FS.setBasedir("y");
        FS.closeFiles();
        FS.setBasedir(System.getProperty("user.dir"));
    }
}
