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

package org.apache.jmeter.gui.action;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.junit.JMeterTestCaseJUnit;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;

import junit.framework.TestSuite;

/**
 * 
 * Test JMX files to check that they can be loaded OK.
 */
public class TestLoad extends JMeterTestCaseJUnit {

    private static final String basedir = new File(System.getProperty("user.dir")).getParent();
    private static final File testfiledir = new File(basedir,"bin/testfiles");
    private static final File demofiledir = new File(basedir,"xdocs/demos");
    
    private static final Set<String> notTestPlan = new HashSet<>();// not full test plans
    
    static{
        notTestPlan.add("load_bug_list.jmx");// used by TestAnchorModifier
        notTestPlan.add("Load_JMeter_Page.jmx");// used by TestAnchorModifier
        notTestPlan.add("ProxyServerTestPlan.jmx");// used by TestSaveService
    }

    private static final FilenameFilter jmxFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jmx");
        }
    };

    private final File testFile;
    private final String parent;
    
    public TestLoad(String name) {
        super(name);
        testFile=null;
        parent=null;
    }

    public TestLoad(String name, File file, String dir) {
        super(name);
        testFile=file;
        parent=dir;
    }

    public static TestSuite suite(){
        TestSuite suite = new TestSuite("Load Test");
        scanFiles(suite, testfiledir);
        scanFiles(suite, demofiledir);
        return suite;
    }

    private static void scanFiles(TestSuite suite, File parent) {
        String dir = parent.getName();
        File[] testFiles = parent.listFiles(jmxFilter);
        for (File file : testFiles) {
            suite.addTest(new TestLoad("checkTestFile", file, dir));
        }
    }
    
    public void checkTestFile() throws Exception{
        HashTree tree = null;
        try {
            tree =getTree(testFile);
        } catch (Exception e) {
            fail(parent+": "+ testFile.getName()+" caused "+e);
        }
        assertTree(tree);
    }
    
    private void assertTree(HashTree tree) throws Exception {
        assertNotNull(parent+": "+ testFile.getName()+" caused null tree: ",tree);
        final Object object = tree.getArray()[0];
        final String name = testFile.getName();
        
        if (! (object instanceof org.apache.jmeter.testelement.TestPlan) && !notTestPlan.contains(name)){
            fail(parent+ ": " +name+" tree should be TestPlan, but is "+object.getClass().getName());
        }
    }

    private HashTree getTree(File f) throws Exception {
        HashTree tree = SaveService.loadTree(f);
        return tree;
    }
}
