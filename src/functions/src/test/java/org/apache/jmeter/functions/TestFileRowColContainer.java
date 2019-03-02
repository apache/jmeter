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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.NoSuchFileException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * File data container for CSV (and similar delimited) files Data is accessible
 * via row and column number
 * 
 */
public class TestFileRowColContainer extends JMeterTestCase implements JMeterSerialTest {

    private String defaultBase = null;

    @Before
    public void setUp() {
        defaultBase = FileServer.getDefaultBase();
        FileServer.getFileServer().setBase(new File(JMeterUtils.getJMeterHome() + "/bin"));
    }

    @After
    public void tearDown() {
        FileServer.getFileServer().setBase(new File(defaultBase));
    }

    @Test(expected = NoSuchFileException.class)
    public void testNull() throws Exception {
        new FileRowColContainer(findTestPath("testfiles/xyzxyz"));
    }

    @Test
    public void testrowNum() throws Exception {
        FileRowColContainer f = new FileRowColContainer(findTestPath("testfiles/unit/TestFileRowColContainer.csv"));
        assertNotNull(f);
        assertEquals("Expected 4 lines", 4, f.getSize());

        assertEquals(0, f.nextRow());
        assertEquals(1, f.nextRow());
        assertEquals(2, f.nextRow());
        assertEquals(3, f.nextRow());
        assertEquals(0, f.nextRow());

    }

    @Test
    public void testRowNumRelative() throws Exception {
        FileRowColContainer f = new FileRowColContainer("testfiles/unit/TestFileRowColContainer.csv");
        assertNotNull(f);
        assertEquals("Expected 4 lines", 4, f.getSize());

        assertEquals(0, f.nextRow());
        assertEquals(1, f.nextRow());
        assertEquals(2, f.nextRow());
        assertEquals(3, f.nextRow());
        assertEquals(0, f.nextRow());

    }

    @Test
    public void testColumns() throws Exception {
        FileRowColContainer f = new FileRowColContainer(findTestPath("testfiles/unit/TestFileRowColContainer.csv"));
        assertNotNull(f);
        assertTrue("Not empty", f.getSize() > 0);

        int myRow = f.nextRow();
        assertEquals(0, myRow);
        assertEquals("a1", f.getColumn(myRow, 0));
        assertEquals("d1", f.getColumn(myRow, 3));

        try {
            f.getColumn(myRow, 4);
            fail("Expected out of bounds");
        } catch (IndexOutOfBoundsException e) {
        }
        myRow = f.nextRow();
        assertEquals(1, myRow);
        assertEquals("b2", f.getColumn(myRow, 1));
        assertEquals("c2", f.getColumn(myRow, 2));
    }

    @Test
    public void testColumnsComma() throws Exception {
        FileRowColContainer f = new FileRowColContainer(findTestPath("testfiles/unit/TestFileRowColContainer.csv"), ",");
        assertNotNull(f);
        assertTrue("Not empty", f.getSize() > 0);

        int myRow = f.nextRow();
        assertEquals(0, myRow);
        assertEquals("a1", f.getColumn(myRow, 0));
        assertEquals("d1", f.getColumn(myRow, 3));

        try {
            f.getColumn(myRow, 4);
            fail("Expected out of bounds");
        } catch (IndexOutOfBoundsException e) {
        }
        myRow = f.nextRow();
        assertEquals(1, myRow);
        assertEquals("b2", f.getColumn(myRow, 1));
        assertEquals("c2", f.getColumn(myRow, 2));
    }

    @Test
    public void testColumnsTab() throws Exception {
        FileRowColContainer f = new FileRowColContainer(findTestPath("testfiles/test.tsv"), "\t");
        assertNotNull(f);
        assertTrue("Not empty", f.getSize() > 0);

        int myRow = f.nextRow();
        assertEquals(0, myRow);
        assertEquals("a1", f.getColumn(myRow, 0));
        assertEquals("d1", f.getColumn(myRow, 3));

        try {
            f.getColumn(myRow, 4);
            fail("Expected out of bounds");
        } catch (IndexOutOfBoundsException e) {
        }
        myRow = f.nextRow();
        assertEquals(1, myRow);
        assertEquals("b2", f.getColumn(myRow, 1));
        assertEquals("c2", f.getColumn(myRow, 2));
    }

    @Test
    public void testEmptyCols() throws Exception {
        FileRowColContainer f = new FileRowColContainer(findTestPath("testfiles/testempty.csv"));
        assertNotNull(f);
        assertEquals("Expected 4 lines", 4, f.getSize());

        int myRow = f.nextRow();
        assertEquals(0, myRow);
        assertEquals("", f.getColumn(myRow, 0));
        assertEquals("d1", f.getColumn(myRow, 3));

        myRow = f.nextRow();
        assertEquals(1, myRow);
        assertEquals("", f.getColumn(myRow, 1));
        assertEquals("c2", f.getColumn(myRow, 2));

        myRow = f.nextRow();
        assertEquals(2, myRow);
        assertEquals("b3", f.getColumn(myRow, 1));
        assertEquals("", f.getColumn(myRow, 2));

        myRow = f.nextRow();
        assertEquals(3, myRow);
        assertEquals("b4", f.getColumn(myRow, 1));
        assertEquals("c4", f.getColumn(myRow, 2));
        assertEquals("", f.getColumn(myRow, 3));
    }
}
