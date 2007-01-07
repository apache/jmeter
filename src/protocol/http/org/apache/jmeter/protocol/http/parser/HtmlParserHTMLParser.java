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

package org.apache.jmeter.protocol.http.parser;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import org.htmlparser.Node;
import org.htmlparser.NodeReader;
import org.htmlparser.Parser;
import org.htmlparser.scanners.AppletScanner;
import org.htmlparser.scanners.BaseHrefScanner;
import org.htmlparser.scanners.BgSoundScanner;
import org.htmlparser.scanners.BodyScanner;
import org.htmlparser.scanners.FrameScanner;
import org.htmlparser.scanners.InputTagScanner;
import org.htmlparser.scanners.LinkScanner;
import org.htmlparser.scanners.LinkTagScanner;
import org.htmlparser.scanners.ScriptScanner;
import org.htmlparser.tags.AppletTag;
import org.htmlparser.tags.BaseHrefTag;
import org.htmlparser.tags.BgSoundTag;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.LinkTagTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.util.DefaultParserFeedback;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

/**
 * HtmlParser implementation using SourceForge's HtmlParser.
 * 
 * @version $Revision$ updated on $Date$
 */
class HtmlParserHTMLParser extends HTMLParser {
    /** Used to store the Logger (used for debug and error messages). */
	private static final Logger log = LoggingManager.getLoggerForClass();

    protected HtmlParserHTMLParser() {
		super();
        log.info("Using htmlparser implementation provided with JMeter");
	}

	protected boolean isReusable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.parser.HtmlParser#getEmbeddedResourceURLs(byte[],
	 *      java.net.URL)
	 */
	public Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl, URLCollection urls) throws HTMLParseException {
        
        if (log.isDebugEnabled()) log.debug("Parsing html of: " + baseUrl);
        
        Parser htmlParser = null;
		try {
			String contents = new String(html);
			StringReader reader = new StringReader(contents);
			NodeReader nreader = new NodeReader(reader, contents.length());
			htmlParser = new Parser(nreader, new DefaultParserFeedback());
			addTagListeners(htmlParser);
		} catch (Exception e) {
			throw new HTMLParseException(e);
		}

		// Now parse the DOM tree

		// look for applets

		// This will only work with an Applet .class file.
		// Ideally, this should be upgraded to work with Objects (IE)
		// and archives (.jar and .zip) files as well.

		try {
			// we start to iterate through the elements
			for (NodeIterator e = htmlParser.elements(); e.hasMoreNodes();) {
				Node node = e.nextNode();
				String binUrlStr = null;

				// first we check to see if body tag has a
				// background set and we set the NodeIterator
				// to the child elements inside the body
				if (node instanceof BodyTag) {
					BodyTag body = (BodyTag) node;
					binUrlStr = body.getAttribute(ATT_BACKGROUND);
					// if the body tag exists, we get the elements
					// within the body tag. if we don't we won't
					// see the body of the page. The only catch
					// with this is if there are images after the
					// closing body tag, it won't get parsed. If
					// someone puts it outside the body tag, it
					// is probably a mistake. Plus it's bad to
					// have important content after the closing
					// body tag. Peter Lin 10-9-03
					e = body.elements();
				} else if (node instanceof BaseHrefTag) {
					BaseHrefTag baseHref = (BaseHrefTag) node;
					String baseref = baseHref.getBaseUrl();
					try {
						if (!baseref.equals(""))// Bugzilla 30713 // $NON-NLS-1$
						{
							baseUrl = new URL(baseUrl, baseref);
						}
					} catch (MalformedURLException e1) {
						throw new HTMLParseException(e1);
					}
				} else if (node instanceof ImageTag) {
					ImageTag image = (ImageTag) node;
					binUrlStr = image.getImageURL();
				} else if (node instanceof AppletTag) {
					AppletTag applet = (AppletTag) node;
					binUrlStr = applet.getAppletClass();
				} else if (node instanceof InputTag) {
					InputTag input = (InputTag) node;
					// we check the input tag type for image
					String strType = input.getAttribute(ATT_TYPE);
					if (strType != null && strType.equalsIgnoreCase(ATT_IS_IMAGE)) {
						// then we need to download the binary
						binUrlStr = input.getAttribute(ATT_SRC);
					}
				} else if (node instanceof LinkTag) {
					LinkTag link = (LinkTag) node;
					if (link.getChild(0) instanceof ImageTag) {
						ImageTag img = (ImageTag) link.getChild(0);
						binUrlStr = img.getImageURL();
					}
				} else if (node instanceof ScriptTag) {
					ScriptTag script = (ScriptTag) node;
					binUrlStr = script.getAttribute(ATT_SRC);
				} else if (node instanceof FrameTag) {
					FrameTag tag = (FrameTag) node;
					binUrlStr = tag.getAttribute(ATT_SRC);
				} else if (node instanceof LinkTagTag) {
					LinkTagTag script = (LinkTagTag) node;
					if (script.getAttribute(ATT_REL).equalsIgnoreCase(STYLESHEET)) {
						binUrlStr = script.getAttribute(ATT_HREF);
					}
				} else if (node instanceof FrameTag) {
					FrameTag script = (FrameTag) node;
					binUrlStr = script.getAttribute(ATT_SRC);
				} else if (node instanceof BgSoundTag) {
					BgSoundTag script = (BgSoundTag) node;
					binUrlStr = script.getAttribute(ATT_SRC);
                } else if (node instanceof Tag) {
                    Tag tag = (Tag) node;
                    String tagname=tag.getTagName();
                    if (tagname.equalsIgnoreCase(TAG_EMBED)){
                        binUrlStr = tag.getAttribute(ATT_SRC);  
                    } else {
                        binUrlStr = tag.getAttribute(ATT_BACKGROUND);
                    }
                }

				if (binUrlStr == null) {
					continue;
				}

				urls.addURL(binUrlStr, baseUrl);
			}
			log.debug("End   : parseNodes");
		} catch (ParserException e) {
			throw new HTMLParseException(e);
		}

		return urls.iterator();
	}

	/**
	 * Returns a node representing a whole xml given an xml document.
	 * 
	 * @param text
	 *            an xml document
	 * @return a node representing a whole xml
	 * 
	 * @throws SAXException
	 *             indicates an error parsing the xml document
	 */
	private static void addTagListeners(Parser parser) {
		log.debug("Start : addTagListeners");
		// add body tag scanner
		parser.addScanner(new BodyScanner());
		// add BaseHRefTag scanner
		parser.addScanner(new BaseHrefScanner());
		// add ImageTag and BaseHrefTag scanners
		LinkScanner linkScanner = new LinkScanner(LinkTag.LINK_TAG_FILTER);
		// parser.addScanner(linkScanner);
		parser.addScanner(linkScanner.createImageScanner(ImageTag.IMAGE_TAG_FILTER));
		parser.addScanner(linkScanner.createBaseHREFScanner("-b")); // $NON-NLS-1$
		// Taken from org.htmlparser.Parser
		// add input tag scanner
		parser.addScanner(new InputTagScanner());
		// add applet tag scanner
		parser.addScanner(new AppletScanner());
		parser.addScanner(new ScriptScanner());
		parser.addScanner(new LinkTagScanner());
		parser.addScanner(new FrameScanner());
		parser.addScanner(new BgSoundScanner());
	}
}
