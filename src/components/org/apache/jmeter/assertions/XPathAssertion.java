/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.jmeter.assertions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Checks if the result is a well-formed XML content. and whether it matches an
 * XPath
 * 
 * author <a href="mailto:jspears@astrology.com">Justin Spears </a>
 */
public class XPathAssertion extends AbstractTestElement implements
		Serializable, Assertion {
	private static final Logger log = LoggingManager.getLoggerForClass();

	public static final String DEFAULT_XPATH = "/";

	private static XPathAPI xpath = null;

	// one builder for all requests
	private static DocumentBuilder builder = null;

	// one factory for all requests
	private static DocumentBuilderFactory factory = null;

	private static final String XPATH_KEY = "XPath.xpath";

	private static final String WHITESPACE_KEY = "XPath.whitespace";

	private static final String VALIDATE_KEY = "XPath.validate";

	private static final String JTIDY_KEY = "XPath.jtidy";

	private static final String NEGATE_KEY = "XPath.negate";

	/**
	 * Returns the result of the Assertion. Checks if the result is well-formed
	 * XML, and that the XPath expression is matched (or not, as the case may
	 * be)
	 */
	public AssertionResult getResult(SampleResult response) {
		// no error as default
		AssertionResult result = new AssertionResult();
		if (response.getResponseData() == null) {
			return setResultForNull(result);
		}
		result.setFailure(false);

		/*
		 * create a new builder if something changes and/or the builder has not
		 * been set
		 */

		if (log.isDebugEnabled()) {
			log.debug(new StringBuffer("Validation is set to ").append(
					isValidating()).toString());
			log.debug(new StringBuffer("Whitespace is set to ").append(
					isWhitespace()).toString());
			log.debug(new StringBuffer("Jtidy is set to ").append(isJTidy())
					.toString());
		}
		Document doc = null;

		try {
			if (isJTidy()) {
				doc = makeTidyParser().parseDOM(
						new ByteArrayInputStream(response.getResponseData()),
						null);
				if (log.isDebugEnabled()) {
					log.debug("node : " + doc);
				}
				doc.normalize();
				// remove the document declaration cause I think it causes
				// issues this is only needed for JDOM, since I am not
				// using it... But in case we change.
				// Node name = doc.getDoctype();
				// doc.removeChild(name);

			} else {
				doc = parse(isValidating(), isWhitespace(), true,
						new ByteArrayInputStream(response.getResponseData()));
			}
		} catch (SAXException e) {
			log.warn("Cannot parse result content", e);
			result.setFailure(true);
			result.setFailureMessage(e.getMessage());
			return result;
		} catch (IOException e) {
			log.warn("Cannot parse result content", e);
			result.setError(true);
			result.setFailureMessage(e.getMessage());
			return result;
		} catch (ParserConfigurationException e) {
			log.warn("Cannot parse result content", e);
			result.setError(true);
			result.setFailureMessage(e.getMessage());
			return result;
		}

		if (doc == null) {
			result.setError(true);
			result.setFailureMessage("Document is null, probably not parsable");
			return result;
		}

		NodeList nodeList = null;

		try {
			nodeList = XPathAPI.selectNodeList(doc, getXPathString());
		} catch (TransformerException e) {
			log.warn("Cannot extract XPath", e);
			result.setError(true);
			result.setFailureMessage(e.getLocalizedMessage());
			return result;
		}

		if (nodeList == null || nodeList.getLength() == 0) {
			if (isNegated()) {
				return result;
			} else {
				result.setFailure(true);
				result
						.setFailureMessage("No Nodes Matched "
								+ getXPathString());
				return result;
			}
		}
		// At this point, we have matched one or more nodes
		if (isNegated()) {// should we have found a match?
			result.setFailure(true);
			result.setFailureMessage("One or more Nodes Matched "
					+ getXPathString());
		}

		if (log.isDebugEnabled()) {
			// if (!isNegated()) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				log.debug(new StringBuffer("nodeList[").append(i).append("] ")
						.append(nodeList.item(i)).toString());
			}
			// }
		}
		return result;
	}

	private AssertionResult setResultForNull(AssertionResult result) {
		result.setError(false);
		result.setFailure(true);
		result.setFailureMessage("Response was null");
		return result;
	}

	/**
	 * Get The XPath String that will be used in matching the document
	 * 
	 * @return String xpath String
	 */
	public String getXPathString() {
		return getPropertyAsString(XPATH_KEY, DEFAULT_XPATH);
	}

	/**
	 * Get a Property or return the defualt string
	 * 
	 * @param key
	 *            Property Key
	 * @param defaultValue
	 *            Default Value
	 * @return String property
	 */
	private String getPropertyAsString(String key, String defaultValue) {
		String str = getPropertyAsString(key);
		return (str == null || str.length() == 0) ? defaultValue : str;
	}

	/**
	 * Set the XPath String this will be used as an xpath
	 * 
	 * @param String
	 *            xpath
	 */
	public void setXPathString(String xpath) {
		setProperty(new StringProperty(XPATH_KEY, xpath));
	}

	/**
	 * Set whether to ignore element whitespace
	 * 
	 * @param boolean
	 *            whitespace
	 */
	public void setWhitespace(boolean whitespace) {
		setProperty(new BooleanProperty(WHITESPACE_KEY, whitespace));
	}

	/**
	 * Set use validation
	 * 
	 * @param boolean
	 *            validate
	 */
	public void setValidating(boolean validate) {
		setProperty(new BooleanProperty(VALIDATE_KEY, validate));
	}

	/**
	 * Feed Document through JTidy. In order to use xpath against XML.
	 * 
	 * @param jtidy
	 */
	public void setJTidy(boolean jtidy) {
		setProperty(new BooleanProperty(JTIDY_KEY, jtidy));
	}

	public void setNegated(boolean negate) {
		setProperty(new BooleanProperty(NEGATE_KEY, negate));
	}

	/**
	 * Is this whitepsace ignored.
	 * 
	 * @return boolean
	 */
	public boolean isWhitespace() {
		return getPropertyAsBoolean(WHITESPACE_KEY, false);
	}

	/**
	 * Is this validating
	 * 
	 * @return boolean
	 */
	public boolean isValidating() {
		return getPropertyAsBoolean(VALIDATE_KEY, false);
	}

	/**
	 * Is this using JTidy
	 * 
	 * @return boolean
	 */
	public boolean isJTidy() {
		return getPropertyAsBoolean(JTIDY_KEY, false);
	}

	/**
	 * Negate the XPath test, that is return true if something is not found.
	 * 
	 * @return boolean negated
	 */
	public boolean isNegated() {
		return getPropertyAsBoolean(NEGATE_KEY, false);
	}

	private Tidy makeTidyParser() {
		log.debug("Start : getParser");
		Tidy tidy = new Tidy();
		tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);
		tidy.setMakeClean(true);
		tidy.setXmlTags(true);
		return tidy;
	}

	private static synchronized Document parse(boolean validating,
			boolean whitespace, boolean namespace, InputStream is)
			throws SAXException, IOException, ParserConfigurationException {
		return makeDocumentBuilder(validating, whitespace, namespace).parse(is);
	}

	private static synchronized DocumentBuilderFactory makeDocumentFactory(
			boolean validating, boolean whitespace, boolean namespace) {
		// (Re)create the factory only if it has changed
		if (factory == null || factory.isValidating() != validating
				|| factory.isIgnoringElementContentWhitespace() != whitespace
				|| factory.isNamespaceAware() != namespace) {
			// configure the document builder factory
			factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(validating);
			factory.setNamespaceAware(namespace);
			factory.setIgnoringElementContentWhitespace(whitespace);
			builder = null;
		}
		return factory;
	}

	private static synchronized DocumentBuilder makeDocumentBuilder(
			boolean validating, boolean whitespace, boolean namespace)
			throws ParserConfigurationException {
		factory = makeDocumentFactory(validating, whitespace, namespace);

		if (builder == null) {
			builder = factory.newDocumentBuilder();
			if (validating) {
				builder.setErrorHandler(new ErrorHandler() {

					public void warning(SAXParseException exception)
							throws SAXException {
						// TODO - should this be enabled?
						// throw new SAXException(exception);
					}

					public void error(SAXParseException exception)
							throws SAXException {
						throw new SAXException(exception);
					}

					public void fatalError(SAXParseException exception)
							throws SAXException {
						// TODO - should this be enabled?
						// throw new SAXException(exception);
					}
				});
			}
		}
		return builder;
	}
}