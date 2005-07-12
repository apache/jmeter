// $Header$
/*
 * ====================================================================
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.
package org.htmlparser.tests;

import java.util.Enumeration;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tests.scannersTests.FormScannerTest;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.SimpleNodeIterator;

public class InstanceofPerformanceTest {
	FormTag formTag;

	Vector formChildren;

	public void setUp() throws Exception {
		Parser parser = Parser.createParser(FormScannerTest.FORM_HTML);
		parser.registerScanners();
		NodeIterator e = parser.elements();
		Node node = e.nextNode();
		formTag = (FormTag) node;
		formChildren = new Vector();
		for (SimpleNodeIterator se = formTag.children(); se.hasMoreNodes();) {
			formChildren.addElement(se.nextNode());
		}
	}

	public void doInstanceofTest(long[] time, int index, long numTimes) {
		System.out.println("doInstanceofTest(" + index + ")");
		long start = System.currentTimeMillis();
		for (long i = 0; i < numTimes; i++) {
			for (Enumeration e = formChildren.elements(); e.hasMoreElements();) {
				Node node = (Node) e.nextElement();
			}
		}
		long end = System.currentTimeMillis();
		time[index] = end - start;
	}

	public void doGetTypeTest(long[] time, int index, long numTimes) {
		System.out.println("doGetTypeTest(" + index + ")");
		long start = System.currentTimeMillis();
		for (long i = 0; i < numTimes; i++) {
			for (SimpleNodeIterator e = formTag.children(); e.hasMoreNodes();) {
				Node node = e.nextNode();
			}
		}
		long end = System.currentTimeMillis();
		time[index] = end - start;
	}

	public void perform() {
		int numTimes = 30;
		long time1[] = new long[numTimes], time2[] = new long[numTimes];

		for (int i = 0; i < numTimes; i++)
			doInstanceofTest(time1, i, i * 10000);

		for (int i = 0; i < numTimes; i++)
			doGetTypeTest(time2, i, i * 10000);

		print(time1, time2);
	}

	public void print(long[] time1, long[] time2) {
		for (int i = 0; i < time1.length; i++) {
			System.out.println(i * 1000000 + ":" + "," + time1[i] + "  " + time2[i]);
		}
	}

	public static void main(String[] args) throws Exception {
		InstanceofPerformanceTest test = new InstanceofPerformanceTest();
		test.setUp();
		test.perform();
	}
}
