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

package org.apache.jmeter.save;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class TestSaveService extends JMeterTestCase {
    
    // testLoadAndSave test files
    private static final String[] FILES = new String[] {
        "AssertionTestPlan.jmx",
        "AuthManagerTestPlan.jmx",
        "HeaderManagerTestPlan.jmx",
        "InterleaveTestPlan2.jmx", 
        "InterleaveTestPlan.jmx",
        "LoopTestPlan.jmx",
        "Modification Manager.jmx",
        "OnceOnlyTestPlan.jmx",
        "proxy.jmx",
        "ProxyServerTestPlan.jmx",
        "SimpleTestPlan.jmx",
        "GuiTest.jmx", 
        "GuiTest231.jmx",
        "GenTest27.jmx",
        };

    // Test files for testLoad; output will generally be different in size
    private static final String[] FILES_LOAD_ONLY = new String[] {
        "GuiTest_original.jmx", 
        "GuiTest231_original.jmx",
        "GenTest22.jmx",
        "GenTest231.jmx",
        "GenTest24.jmx",
        "GenTest25.jmx", // GraphAccumVisualizer obsolete, BSFSamplerGui now a TestBean
        "GenTest251.jmx", // GraphAccumVisualizer obsolete, BSFSamplerGui now a TestBean
        "GenTest26.jmx", // GraphAccumVisualizer now obsolete
        };

    private static final boolean saveOut = JMeterUtils.getPropDefault("testsaveservice.saveout", false);

    public TestSaveService(String name) {
        super(name);
    }
    public void testPropfile() throws Exception {
        assertTrue("Property Version mismatch, ensure you update SaveService#PROPVERSION field with _version property value from saveservice.properties", SaveService.checkPropertyVersion());            
        assertTrue("Property File Version mismatch, ensure you update SaveService#FILEVERSION field with revision id of saveservice.properties", SaveService.checkFileVersion());
    }
    
    public void testVersions() throws Exception {
        assertTrue("Unexpected version found", SaveService.checkVersions());
    }

    public void testLoadAndSave() throws Exception {
        boolean failed = false; // Did a test fail?

        for (int i = 0; i < FILES.length; i++) {
            final File testFile = findTestFile("testfiles/" + FILES[i]);

            int [] orig = readFile(new BufferedReader(new FileReader(testFile)));

            InputStream in = null;
            HashTree tree = null;
            try {
                in = new FileInputStream(testFile);
                tree = SaveService.loadTree(in);
            } finally {
                if(in != null) {
                    in.close();
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream(1000000);
            try {
                SaveService.saveTree(tree, out);
            } finally {
                out.close(); // Make sure all the data is flushed out
            }

            ByteArrayInputStream ins = new ByteArrayInputStream(out.toByteArray());
            
            int [] output = readFile(new BufferedReader(new InputStreamReader(ins)));
            // We only check the length of the result. Comparing the
            // actual result (out.toByteArray==original) will usually
            // fail, because the order of the properties within each
            // test element may change. Comparing the lengths should be
            // enough to detect most problem cases...
            if (orig[0] != output[0] || orig[1] != output[1]) {
                failed = true;
                System.out.println();
                System.out.println("Loading file testfiles/" + FILES[i] + " and "
                        + "saving it back changes its size from " + orig[0] + " to " + output[0] + ".");
                System.out.println("Number of lines changes from " + orig[1] + " to " + output[1]);
                if (saveOut) {
                    final File outFile = findTestFile("testfiles/" + FILES[i] + ".out");
                    System.out.println("Write " + outFile);
                    FileOutputStream outf = null;
                    try {
                        outf = new FileOutputStream(outFile);
                        outf.write(out.toByteArray());
                    } finally {
                        if(outf != null) {
                            outf.close();
                        }
                    }
                    System.out.println("Wrote " + outFile);
                }
            }

            // Note this test will fail if a property is added or
            // removed to any of the components used in the test
            // files. The way to solve this is to appropriately change
            // the test file.
        }
        if (failed) // TODO make these separate tests?
        {
            fail("One or more failures detected");
        }
    }
    
    /**
     * Calculate size and line count ignoring EOL and 
     * "jmeterTestPlan" element which may vary because of 
     * different attributes/attribute lengths.
     */
    private int[] readFile(BufferedReader br) throws Exception {
        try {
            int length=0;
            int lines=0;
            String line;
            while((line=br.readLine()) != null) {
                lines++;
                if (!line.startsWith("<jmeterTestPlan")) {
                    length += line.length();
                }
            }
            return new int []{length, lines};
        } finally {
            br.close();
        }
    }

    public void testLoad() throws Exception {
        for (int i = 0; i < FILES_LOAD_ONLY.length; i++) {
            InputStream in = null;
            try {
                in = new FileInputStream(findTestFile("testfiles/" + FILES_LOAD_ONLY[i]));
                HashTree tree =SaveService.loadTree(in);
                assertNotNull(tree);
            } finally {
                if(in != null) {
                    in.close();
                }
            }
        }

    }

    public void testClasses(){
        List<String> missingClasses = SaveService.checkClasses();
        if(missingClasses.size()>0) {
            fail("One or more classes not found:"+missingClasses);
        }
    }
}
