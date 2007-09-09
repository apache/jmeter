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

package org.apache.jmeter.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * author Justin Spears jspears@astrology.com
 * 
 * This class provides a few utility methods for dealing with XML/XPath. Might
 * think about creating an interface for the setup, but, works fine now...
 * 
 */
public class XPathUtil {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private XPathUtil() {
		super();
	}

	private static DocumentBuilderFactory documentBuilderFactory;

	/**
	 * Returns a suitable document builder factory.
	 * Caches the factory in case the next caller wants the same options.
	 * 
	 * @param validate should the parser validate documents?
	 * @param whitespace should the parser eliminate whitespace in element content?
	 * @param namespace should the parser be namespace aware?
	 * 
	 * @return javax.xml.parsers.DocumentBuilderFactory
	 */
	private static synchronized DocumentBuilderFactory makeDocumentBuilderFactory(boolean validate, boolean whitespace,
			boolean namespace) {
		if (XPathUtil.documentBuilderFactory == null || documentBuilderFactory.isValidating() != validate
				|| documentBuilderFactory.isNamespaceAware() != namespace
				|| documentBuilderFactory.isIgnoringElementContentWhitespace() != whitespace) {
			// configure the document builder factory
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(validate);
			documentBuilderFactory.setNamespaceAware(namespace);
			documentBuilderFactory.setIgnoringElementContentWhitespace(whitespace);
		}
		return XPathUtil.documentBuilderFactory;
	}

	/**
	 * Create a DocumentBuilder using the makeDocumentFactory func.
	 * 
	 * @param validate should the parser validate documents?
	 * @param whitespace should the parser eliminate whitespace in element content?
	 * @param namespace should the parser be namespace aware?
	 * @return document builder
	 * @throws ParserConfigurationException
	 */
	public static DocumentBuilder makeDocumentBuilder(boolean validate, boolean whitespace, boolean namespace)
			throws ParserConfigurationException {
		DocumentBuilder builder = makeDocumentBuilderFactory(validate, whitespace, namespace).newDocumentBuilder();
		builder.setErrorHandler(new MyErrorHandler(validate, false));
		return builder;
	}

	/**
	 * Utility function to get new Document
	 * 
	 * @param stream
	 *            Document Input stream
	 * @param validate
	 *            Validate Document
	 * @param whitespace
	 *            Element Whitespace
	 * @param namespace
	 *            Is Namespace aware.
	 * @param tolerant
	 *            Is tolerant - i.e. use the Tidy parser
	 *
	 * @return document
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static Document makeDocument(InputStream stream, boolean validate, boolean whitespace, boolean namespace,
			boolean tolerant) throws ParserConfigurationException, SAXException, IOException {
		Document doc;
		if (tolerant) {
			doc = tidyDoc(stream);
			// doc=makeTolerantDocumentBuilder().parse(new
			// InputStreamReader(stream));
		} else {
			doc = makeDocumentBuilder(validate, whitespace, namespace).parse(stream);
		}
		return doc;
	}

	// private static HTMLDocumentBuilder makeTolerantDocumentBuilder()
	// throws ParserConfigurationException, SAXException, IOException {
	// HTMLDocumentBuilder builder = new HTMLDocumentBuilder(
	// new TolerantSaxDocumentBuilder(makeDocumentBuilder(false,false,false)
	// ));
	// return builder;
	// }

	private static Document tidyDoc(InputStream stream) {
		Document doc = null;
		doc = makeTidyParser().parseDOM(stream, null);
		doc.normalize();
		// remove the document declaration cause I think it causes
		// issues this is only needed for JDOM, since I am not
		// using it... But in case we change.
		// Node name = doc.getDoctype();
		// doc.removeChild(name);

		return doc;
	}

	private static Tidy makeTidyParser() {
		Tidy tidy = new Tidy();
		tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		tidy.setMakeClean(true);
		tidy.setXmlTags(false); // Input is not valid XML
		// tidy.setShowErrors(1);
		return tidy;
	}

	// Not used
	// public static Document makeDocument(InputStream stream)
	// throws ParserConfigurationException, SAXException, IOException {
	// return makeDocumentBuilder( false, false, false).parse(stream);
	// }

	static class MyErrorHandler implements ErrorHandler {
		private final boolean val, tol;

		private final String type;

		MyErrorHandler(boolean validate, boolean tolerate) {
			val = validate;
			tol = tolerate;
			type = "Val=" + val + " Tol=" + tol;
		}

		public void warning(SAXParseException ex) throws SAXException {
			log.info("Type=" + type + " " + ex);
			if (val && !tol)
				throw new SAXException(ex);
		}

		public void error(SAXParseException ex) throws SAXException {
			log.warn("Type=" + type + " " + ex);
			if (val && !tol)
				throw new SAXException(ex);
		}

		public void fatalError(SAXParseException ex) throws SAXException {
			log.error("Type=" + type + " " + ex);
			if (val && !tol)
				throw new SAXException(ex);
		}
	}
}