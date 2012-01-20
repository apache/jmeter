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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

public class TestSaveService extends JMeterTestCase {
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
        };

    private static final boolean saveOut = JMeterUtils.getPropDefault("testsaveservice.saveout", false);

    public TestSaveService(String name) {
        super(name);
    }
    public void testPropfile() throws Exception {
        assertTrue("Property Version mismatch", SaveService.checkPropertyVersion());            
        assertTrue("Property File Version mismatch", SaveService.checkFileVersion());
    }
    
    public void testVersions() throws Exception {
        assertTrue("Unexpected version found", SaveService.checkVersions());
    }

    public void testLoadAndSave() throws Exception {
        byte[] original = new byte[1000000];

        boolean failed = false; // Did a test fail?

        for (int i = 0; i < FILES.length; i++) {
            InputStream in = new FileInputStream(findTestFile("testfiles/" + FILES[i]));
            int len = in.read(original);

            in.close();

            in = new ByteArrayInputStream(original, 0, len);
            HashTree tree = SaveService.loadTree(in);

            in.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream(1000000);

            SaveService.saveTree(tree, out);
            out.close(); // Make sure all the data is flushed out

            // We only check the length of the result. Comparing the
            // actual result (out.toByteArray==original) will usually
            // fail, because the order of the properties within each
            // test element may change. Comparing the lengths should be
            // enough to detect most problem cases...
            int outsz=out.size();
            // Allow for input in CRLF and output in LF only
            int lines=0;
            byte ba[]=out.toByteArray();
            for(int j=0;j<ba.length;j++) {
                if (ba[j] == '\n'){
                    lines++;
                }
            }
            if (len != outsz && len != outsz+lines) {
                failed = true;
                System.out.println();
                System.out.println("Loading file testfiles/" + FILES[i] + " and "
                        + "saving it back changes its size from " + len + " to " + outsz + ".");
                System.out.println("Diff "+(len-outsz)+" lines "+lines);
                if (saveOut) {
                    String outfile = "testfiles/" + FILES[i] + ".out";
                    System.out.println("Write " + outfile);
                    FileOutputStream outf = new FileOutputStream(new File(outfile));
                    outf.write(out.toByteArray());
                    outf.close();
                    System.out.println("Wrote " + outfile);
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
    
    public void testClasses(){
        assertTrue("One or more classes not found - see log file",SaveService.checkClasses());
    }
}
