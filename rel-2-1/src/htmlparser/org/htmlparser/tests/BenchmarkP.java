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

package org.htmlparser.tests;

import java.io.File;
import java.net.MalformedURLException;

import org.htmlparser.*;
import org.htmlparser.scanners.*;
import org.htmlparser.tags.*;
import org.htmlparser.util.*;

/**
 * Title: Apache Jakarta JMeter<br>
 * Copyright: Copyright (c) Apache<br>
 * Company: Apache<br>
 * License:<br>
 * <br>
 * The license is at the top!<br>
 * <br>
 * Description:<br>
 * <br>
 * <p>
 * Author: pete<br>
 * Version: 0.1<br>
 * Created on: Sep 30, 2003<br>
 * Last Modified: 4:45:28 PM<br>
 */

public class BenchmarkP {

	/**
	 * 
	 */
	public BenchmarkP() {
		super();
	}

	public static void main(String[] args) {
		if (args != null && args.length > 0) {
			String strurl = args[0];
			boolean addLink = true;
			if (args.length == 2) {
				if (args[1].equals("f")) {
					addLink = false;
				}
			}
			if (strurl.indexOf("http") < 0) {
				File input = new File(strurl);
				try {
					strurl = input.toURL().toString();
					System.out.println("file converted to URL: " + args[0]);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			try {
				Parser parser = new Parser(strurl, new DefaultParserFeedback());

				LinkScanner linkScanner = new LinkScanner(LinkTag.LINK_TAG_FILTER);
				if (addLink) {
					parser.addScanner(linkScanner);
				}
				parser.addScanner(linkScanner.createImageScanner(ImageTag.IMAGE_TAG_FILTER));
				parser.addScanner(new BodyScanner());
				long start = System.currentTimeMillis();
				for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
					Node node = e.nextNode();
					if (node instanceof BodyTag) {
						BodyTag btag = (BodyTag) node;
						System.out.println("body url: " + btag.getAttribute("background"));
						for (NodeIterator ee = btag.elements(); ee.hasMoreNodes();) {
							Node cnode = ee.nextNode();
							if (cnode instanceof ImageTag) {
								ImageTag iTag = (ImageTag) cnode;
								System.out.println("image url: " + iTag.getImageURL());
							}
						}
					}
				}
				System.out.println("Elapsed Time ms: " + (System.currentTimeMillis() - start));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
