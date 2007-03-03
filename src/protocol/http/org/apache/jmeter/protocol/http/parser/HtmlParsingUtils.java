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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

// For Junit tests @see TestHtmlParsingUtils

/**
 * @author Michael Stover Created June 14, 2001
 */
public final class HtmlParsingUtils {
	transient private static Logger log = LoggingManager.getLoggerForClass();

	/*
	 * NOTUSED private int compilerOptions = Perl5Compiler.CASE_INSENSITIVE_MASK |
	 * Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK;
	 */

	/**
	 * Private constructor to prevent instantiation.
	 */
	private HtmlParsingUtils() {
	}

	public static synchronized boolean isAnchorMatched(HTTPSamplerBase newLink, HTTPSamplerBase config)
	{
		boolean ok = true;
		Perl5Matcher matcher = JMeterUtils.getMatcher();
		PropertyIterator iter = config.getArguments().iterator();

		String query = null;
		try {
			query = URLDecoder.decode(newLink.getQueryString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 unsupported? You must be joking!
			log.error("UTF-8 encoding not supported!");
			throw new Error("Should not happen: " + e.toString());
		}

		if (query == null && config.getArguments().getArgumentCount() > 0) {
			return false;
		}

		while (iter.hasNext()) {
			Argument item = (Argument) iter.next().getObjectValue();
			if (query.indexOf(item.getName() + "=") == -1) {
				if (!(ok = ok
						&& matcher.contains(query, JMeterUtils.getPatternCache()
								.getPattern(item.getName(), Perl5Compiler.READ_ONLY_MASK)))) {
					return false;
				}
			}
		}

		if (config.getDomain() != null && config.getDomain().length() > 0
				&& !newLink.getDomain().equals(config.getDomain())) {
			if (!(ok = ok
					&& matcher.matches(newLink.getDomain(), JMeterUtils.getPatternCache().getPattern(config.getDomain(),
							Perl5Compiler.READ_ONLY_MASK)))) {
				return false;
			}
		}

		if (!newLink.getPath().equals(config.getPath())
				&& !matcher.matches(newLink.getPath(), JMeterUtils.getPatternCache().getPattern("[/]*" + config.getPath(),
						Perl5Compiler.READ_ONLY_MASK))) {
			return false;
		}

		if (!(ok = ok
				&& matcher.matches(newLink.getProtocol(), JMeterUtils.getPatternCache().getPattern(config.getProtocol(),
						Perl5Compiler.READ_ONLY_MASK)))) {
			return false;
		}

		return ok;
	}

	public static synchronized boolean isArgumentMatched(Argument arg, Argument patternArg) {
		Perl5Matcher matcher = JMeterUtils.getMatcher();
		return (arg.getName().equals(patternArg.getName()) || matcher.matches(arg.getName(), JMeterUtils.getPatternCache().getPattern(
				patternArg.getName(), Perl5Compiler.READ_ONLY_MASK)))
				&& (arg.getValue().equals(patternArg.getValue()) || matcher.matches(arg.getValue(), JMeterUtils.getPatternCache()
						.getPattern(patternArg.getValue(), Perl5Compiler.READ_ONLY_MASK)));
	}

	/**
	 * Returns <code>tidy</code> as HTML parser.
	 * 
	 * @return a <code>tidy</code> HTML parser
	 */
	public static Tidy getParser() {
		log.debug("Start : getParser1");
		Tidy tidy = new Tidy();
		tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);

		if (log.isDebugEnabled()) {
			log.debug("getParser1 : tidy parser created - " + tidy);
		}

		log.debug("End : getParser1");

		return tidy;
	}

	/**
	 * Returns a node representing a whole xml given an xml document.
	 * 
	 * @param text
	 *            an xml document
	 * @return a node representing a whole xml
	 */
	public static Node getDOM(String text) {
		log.debug("Start : getDOM1");

		try {
			Node node = getParser().parseDOM(new ByteArrayInputStream(text.getBytes("UTF-8")), null);// $NON-NLS-1$

			if (log.isDebugEnabled()) {
				log.debug("node : " + node);
			}

			log.debug("End : getDOM1");

			return node;
		} catch (UnsupportedEncodingException e) {
			log.error("getDOM1 : Unsupported encoding exception - " + e);
			log.debug("End : getDOM1");
			throw new RuntimeException("UTF-8 encoding failed");
		}
	}

	public static Document createEmptyDoc() {
		return Tidy.createEmptyDocument();
	}

	/**
	 * Create a new Sampler based on an HREF string plus a contextual URL
	 * object. Given that an HREF string might be of three possible forms, some
	 * processing is required.
	 */
	public static HTTPSamplerBase createUrlFromAnchor(String parsedUrlString, URL context) throws MalformedURLException {
		if (log.isDebugEnabled()) {
			log.debug("Creating URL from Anchor: " + parsedUrlString + ", base: " + context);
		}
		URL url = new URL(context, parsedUrlString);
		HTTPSamplerBase sampler =HTTPSamplerFactory.newInstance();
		sampler.setDomain(url.getHost());
		sampler.setProtocol(url.getProtocol());
		sampler.setPort(url.getPort());
		sampler.setPath(url.getPath());
		sampler.parseArguments(url.getQuery());

		return sampler;
	}

	public static List createURLFromForm(Node doc, URL context) {
		String selectName = null;
		LinkedList urlConfigs = new LinkedList();
		recurseForm(doc, urlConfigs, context, selectName, false);
		/*
		 * NamedNodeMap atts = formNode.getAttributes();
		 * if(atts.getNamedItem("action") == null) { throw new
		 * MalformedURLException(); } String action =
		 * atts.getNamedItem("action").getNodeValue(); UrlConfig url =
		 * createUrlFromAnchor(action, context); recurseForm(doc, url,
		 * selectName,true,formStart);
		 */
		return urlConfigs;
	}

    // N.B. Since the tags are extracted from an HTML Form, any values must already have been encoded
	private static boolean recurseForm(Node tempNode, LinkedList urlConfigs, URL context, String selectName,
			boolean inForm) {
		NamedNodeMap nodeAtts = tempNode.getAttributes();
		String tag = tempNode.getNodeName();
		try {
			if (inForm) {
				HTTPSamplerBase url = (HTTPSamplerBase) urlConfigs.getLast();
				if (tag.equalsIgnoreCase("form")) {
					try {
						urlConfigs.add(createFormUrlConfig(tempNode, context));
					} catch (MalformedURLException e) {
						inForm = false;
					}
				} else if (tag.equalsIgnoreCase("input")) {
					url.addEncodedArgument(getAttributeValue(nodeAtts, "name"), getAttributeValue(nodeAtts, "value"));
				} else if (tag.equalsIgnoreCase("textarea")) {
					try {
						url.addEncodedArgument(getAttributeValue(nodeAtts, "name"), tempNode.getFirstChild().getNodeValue());
					} catch (NullPointerException e) {
						url.addArgument(getAttributeValue(nodeAtts, "name"), "");
					}
				} else if (tag.equalsIgnoreCase("select")) {
					selectName = getAttributeValue(nodeAtts, "name");
				} else if (tag.equalsIgnoreCase("option")) {
					String value = getAttributeValue(nodeAtts, "value");
					if (value == null) {
						try {
							value = tempNode.getFirstChild().getNodeValue();
						} catch (NullPointerException e) {
							value = "";
						}
					}
					url.addEncodedArgument(selectName, value);
				}
			} else if (tag.equalsIgnoreCase("form")) {
				try {
					urlConfigs.add(createFormUrlConfig(tempNode, context));
					inForm = true;
				} catch (MalformedURLException e) {
					inForm = false;
				}
				// I can't see the point for this code being here. Looks like
				// a really obscure performance optimization feature :-)
				// Seriously: I'll comment it out... I just don't dare to
				// remove it completely, in case there *is* a reason.
				/*
				 * try { Thread.sleep(5000); } catch (Exception e) { }
				 */
			}
		} catch (Exception ex) {
			log.warn("Some bad HTML " + printNode(tempNode), ex);
		}
		NodeList childNodes = tempNode.getChildNodes();
		for (int x = 0; x < childNodes.getLength(); x++) {
			inForm = recurseForm(childNodes.item(x), urlConfigs, context, selectName, inForm);
		}
		return inForm;
	}

	private static String getAttributeValue(NamedNodeMap att, String attName) {
		try {
			return att.getNamedItem(attName).getNodeValue();
		} catch (Exception ex) {
			return "";
		}
	}

	private static String printNode(Node node) {
		StringBuffer buf = new StringBuffer();
		buf.append("<");
		buf.append(node.getNodeName());
		NamedNodeMap atts = node.getAttributes();
		for (int x = 0; x < atts.getLength(); x++) {
			buf.append(" ");
			buf.append(atts.item(x).getNodeName());
			buf.append("=\"");
			buf.append(atts.item(x).getNodeValue());
			buf.append("\"");
		}

		buf.append(">");

		return buf.toString();
	}

	private static HTTPSamplerBase createFormUrlConfig(Node tempNode, URL context) throws MalformedURLException {
		NamedNodeMap atts = tempNode.getAttributes();
		if (atts.getNamedItem("action") == null) {
			throw new MalformedURLException();
		}
		String action = atts.getNamedItem("action").getNodeValue();
		HTTPSamplerBase url = createUrlFromAnchor(action, context);
		return url;
	}
	
	public static void extractStyleURLs(final URL baseUrl, final URLCollection urls, String styleTagStr) {
		Perl5Matcher matcher = JMeterUtils.getMatcher();
		Pattern pattern = JMeterUtils.getPatternCache().getPattern(
				"URL\\(\\s*('|\")(.*)('|\")\\s*\\)",
				Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.SINGLELINE_MASK | Perl5Compiler.READ_ONLY_MASK);
		PatternMatcherInput input = null;
		input = new PatternMatcherInput(styleTagStr);
		while (matcher.contains(input, pattern)) {
		    MatchResult match = matcher.getMatch();
		    // The value is in the second group
		    String styleUrl = match.group(2);
		    urls.addURL(styleUrl, baseUrl);
		}
	}
}
