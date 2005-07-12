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
package org.htmlparser.tests.codeMetrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

public class LineCounter {

	public int count(File file) {
		System.out.println("Handling " + file.getName());
		int count = 0;
		// Get all files in current directory
		if (file.isDirectory()) {
			// Get the listing in this directory
			count = recurseDirectory(file, count);
		} else {
			// It is a file
			count = countLinesIn(file);
		}
		return count;
	}

	/**
	 * Counts code excluding comments and blank lines in the given file
	 * 
	 * @param file
	 * @return int
	 */
	public int countLinesIn(File file) {
		int count = 0;
		System.out.println("Counting " + file.getName());
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
			String line = null;
			do {
				line = reader.readLine();
				if (line != null && line.indexOf("*") == -1 && line.indexOf("//") == -1 && line.length() > 0)
					count++;
			} while (line != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	public int recurseDirectory(File file, int count) {
		File[] files = file.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.getName().indexOf(".java") != -1 || file.isDirectory()) {
					return true;
				} else {
					return false;
				}
			}
		});
		for (int i = 0; i < files.length; i++) {
			count += count(files[i]);
		}
		return count;
	}

	public static void main(String[] args) {
		LineCounter lc = new LineCounter();
		System.out.println("Line Count = " + lc.count(new File(args[0])));
	}
}
