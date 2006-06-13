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

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class PerformanceTest {
	private int numTimes;

	private String file;

	/**
	 * Try to parse the given page the given no of times Print average time
	 * taken
	 * 
	 * @param file
	 *            File to be parsed
	 * @param numTimes
	 *            number of times the test should be repeated
	 */
	public PerformanceTest(String file, int numTimes) {
		this.file = file;
		this.numTimes = numTimes;
	}

	public void beginTestWithoutScanners() throws ParserException {
		Parser parser;
		long sumTimes = 0;
		double avg = 0;
		System.out.println("***************************************");
		System.out.println("*  Test Without Scanners Registered   *");
		System.out.println("***************************************");
		for (int i = 0; i <= numTimes; i++) {
			// Create the parser object
			parser = new Parser(file, new DefaultParserFeedback());
			Node node;
			long start = System.currentTimeMillis();
			for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
				node = e.nextNode();
			}
			long elapsedTime = System.currentTimeMillis() - start;
			if (i != 0)
				sumTimes += elapsedTime;
			System.out.print("Iteration " + i);
			if (i == 0)
				System.out.print(" (not counted)");
			System.out.println(" : time taken = " + elapsedTime + " ms");
		}
		avg = sumTimes / (float) numTimes;
		System.out.println("***************************************");
		System.out.println("Average Time : " + avg + " ms");
		System.out.println("***************************************");
	}

	public void beginTestWithScanners() throws ParserException {
		Parser parser;
		long sumTimes = 0;
		double avg = 0;
		System.out.println("***************************************");
		System.out.println("*    Test With Scanners Registered    *");
		System.out.println("***************************************");
		for (int i = 0; i <= numTimes; i++) {
			// Create the parser object
			parser = new Parser(file, new DefaultParserFeedback());
			parser.registerScanners();
			Node node;
			long start = System.currentTimeMillis();
			for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
				node = e.nextNode();
			}
			long elapsedTime = System.currentTimeMillis() - start;
			if (i != 0)
				sumTimes += elapsedTime;
			System.out.print("Iteration " + i);
			if (i == 0)
				System.out.print(" (not counted)");
			System.out.println(" : time taken = " + elapsedTime + " ms");
		}
		avg = sumTimes / (float) numTimes;
		System.out.println("***************************************");
		System.out.println("Average Time : " + avg + " ms");
		System.out.println("***************************************");
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Syntax Error.");
			System.err.println("Params needed for test : <file/url to be parsed> <number of iterations>");
			System.exit(-1);
		}
		String file = args[0];
		String numTimesString = args[1];
		int numTimes = Integer.decode(numTimesString).intValue();
		PerformanceTest pt = new PerformanceTest(file, numTimes);
		try {
			pt.beginTestWithoutScanners();
			pt.beginTestWithScanners();
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}
}
