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
import java.io.FileInputStream;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;

/**
 * 
 * @version $Revision$ Last updated: $Date$
 */
public class TestLoad extends JMeterTestCase {
	File testFile1, testFile2, testFile3, testFile4, testFile5, testFile6, testFile7, testFile8, testFile9, testFile10,
			testFile11, testFile12, testFile13;

	static Load loader = new Load();

	public TestLoad(String name) {
		super(name);
	}

	public void setUp() {
//		testFile1 = // Old-style format; no longer used
//		new File(System.getProperty("user.dir") + "/testfiles", "Test Plan.jmx");
		testFile2 =
		new File(System.getProperty("user.dir") + "/testfiles", "Modification Manager.jmx");
		testFile3 = new File(System.getProperty("user.dir") + "/testfiles", "proxy.jmx");
		testFile4 = new File(System.getProperty("user.dir") + "/testfiles", "AssertionTestPlan.jmx");
		testFile5 = new File(System.getProperty("user.dir") + "/testfiles", "AuthManagerTestPlan.jmx");
		testFile6 = new File(System.getProperty("user.dir") + "/testfiles", "HeaderManagerTestPlan.jmx");
		testFile7 = new File(System.getProperty("user.dir") + "/testfiles", "InterleaveTestPlan.jmx");
		testFile8 = new File(System.getProperty("user.dir") + "/testfiles", "InterleaveTestPlan2.jmx");
		testFile9 = new File(System.getProperty("user.dir") + "/testfiles", "LoopTestPlan.jmx");
		testFile10 = new File(System.getProperty("user.dir") + "/testfiles", "OnceOnlyTestPlan.jmx");
		testFile11 = new File(System.getProperty("user.dir") + "/testfiles", "ProxyServerTestPlan.jmx");
		testFile12 = new File(System.getProperty("user.dir") + "/testfiles", "SimpleTestPlan.jmx");
		// Incomplete file
//		testFile13 =
//		new File(System.getProperty("user.dir") + "/testfiles", "URLRewritingExample.jmx");
	}

//	public void testFile1() throws Exception {
//		assertTree(getTree(testFile1));
//	}

	public void testFile2() throws Exception {
		assertTree(getTree(testFile2));
	}

	public void testFile3() throws Exception {
		assertTree(getTree(testFile3));
	}

	private void assertTree(HashTree tree) throws Exception {
		final Object object = tree.getArray()[0];
		if (! (object instanceof org.apache.jmeter.testelement.TestPlan)){
			fail("Hash tree should be TestPlan, but is "+object.getClass().getName());
		}
	}

	public void testFile4() throws Exception {
		assertTree(getTree(testFile4));
	}

	public void testFile5() throws Exception {
		assertTree(getTree(testFile5));
	}

	public void testFile6() throws Exception {
		assertTree(getTree(testFile6));
	}

	public void testFile7() throws Exception {
		assertTree(getTree(testFile7));
	}

	public void testFile8() throws Exception {
		assertTree(getTree(testFile8));
	}

	public void testFile9() throws Exception {
		assertTree(getTree(testFile9));
	}

	public void testFile10() throws Exception {
		assertTree(getTree(testFile10));
	}

	public void testFile11() throws Exception {
		assertTree(getTree(testFile11));
	}

	public void testFile12() throws Exception {
		assertTree(getTree(testFile12));
	}

//	public void testFile13() throws Exception {
//		assertTree(getTree(testFile13));
//	}

	private HashTree getTree(File f) throws Exception {
		HashTree tree = SaveService.loadTree(new FileInputStream(f));
		return tree;
	}
}