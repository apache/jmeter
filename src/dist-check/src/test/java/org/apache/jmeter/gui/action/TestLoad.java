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

package org.apache.jmeter.gui.action;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * Test JMX files to check that they can be loaded OK.
 */
public class TestLoad  {

    private static final String basedir = new File(System.getProperty("user.dir")).getParentFile().getParent();
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

    public static Stream<Arguments> inputFiles() {
        return Stream.concat(
                scanFiles(testfiledir),
                scanFiles(demofiledir));
    }

    private static Stream<Arguments> scanFiles(File parent) {
        String dir = parent.getName();
        File[] testFiles = parent.listFiles(jmxFilter);
        if (testFiles == null) {
            fail("*.jmx files for test should be present in folder " + parent);
        }
        return Stream.of(testFiles)
                .map(file -> arguments(dir, file));
    }

    @ParameterizedTest
    @MethodSource("inputFiles")
    public void checkTestFile(String parent, File testFile) throws Exception{
        HashTree tree = getTree(testFile);
        assertTree(tree, parent, testFile);
    }

    private void assertTree(HashTree tree, String parent, File testFile) throws Exception {
        assertNotNull(tree, parent+": "+ testFile.getName()+" caused null tree: ");
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
