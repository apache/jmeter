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
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Checks if the result is a well-formed XML content and whether it matches an
 * XPath
 * 
 * author <a href="mailto:jspears@astrology.com">Justin Spears </a>
 */
public class XPathAssertion extends AbstractTestElement implements Serializable, Assertion {
	private static final Logger log = LoggingManager.getLoggerForClass();

	// private static XPathAPI xpath = null;

	private static final String XPATH_KEY = "XPath.xpath";

	private static final String WHITESPACE_KEY = "XPath.whitespace";

	private static final String VALIDATE_KEY = "XPath.validate";

	private static final String TOLERANT_KEY = "XPath.tolerant";

	private static final String NEGATE_KEY = "XPath.negate";

	private static final String NAMESPACE_KEY = "XPath.namespace";

	public static final String DEFAULT_XPATH = "/";

	/**
	 * Returns the result of the Assertion. Checks if the result is well-formed
	 * XML, and that the XPath expression is matched (or not, as the case may
	 * be)
	 */
	public AssertionResult getResult(SampleResult response) {
		// no error as default
		AssertionResult result = new AssertionResult();
		byte[] responseData = response.getResponseData();
		if (responseData.length == 0) {
			return result.setResultForNull();
		}
		result.setFailure(false);
		result.setFailureMessage("");

		if (log.isDebugEnabled()) {
			log.debug(new StringBuffer("Validation is set to ").append(isValidating()).toString());
			log.debug(new StringBuffer("Whitespace is set to ").append(isWhitespace()).toString());
			log.debug(new StringBuffer("Tolerant is set to ").append(isTolerant()).toString());
		}

		Document doc = null;

		try {
			doc = XPathUtil.makeDocument(new ByteArrayInputStream(responseData), isValidating(),
					isWhitespace(), isNamespace(), isTolerant());
		} catch (SAXException e) {
			log.debug("Caught sax exception: " + e);
			result.setError(true);
			result.setFailureMessage(new StringBuffer("SAXException: ").append(e.getMessage()).toString());
			return result;
		} catch (IOException e) {
			log.warn("Cannot parse result content", e);
			result.setError(true);
			result.setFailureMessage(new StringBuffer("IOException: ").append(e.getMessage()).toString());
			return result;
		} catch (ParserConfigurationException e) {
			log.warn("Cannot parse result content", e);
			result.setError(true);
			result.setFailureMessage(new StringBuffer("ParserConfigurationException: ").append(e.getMessage())
					.toString());
			return result;
		}

		if (doc == null || doc.getDocumentElement() == null) {
			result.setError(true);
			result.setFailureMessage("Document is null, probably not parsable");
			return result;
		}

		NodeList nodeList = null;

		try {
			nodeList = XPathAPI.selectNodeList(doc, getXPathString());
		} catch (TransformerException e) {
			result.setError(true);
			result.setFailureMessage(new StringBuffer("TransformerException: ").append(e.getMessage()).toString());
			return result;
		}

		if (nodeList == null || nodeList.getLength() == 0) {
			log.debug(new StringBuffer("nodeList null no match  ").append(getXPathString()).toString());
			result.setFailure(!isNegated());
			result.setFailureMessage("No Nodes Matched " + getXPathString());
			return result;
		}
		log.debug("nodeList length " + nodeList.getLength());
		if (log.isDebugEnabled() & !isNegated()) {
			for (int i = 0; i < nodeList.getLength(); i++)
				log.debug(new StringBuffer("nodeList[").append(i).append("] ").append(nodeList.item(i)).toString());
		}
		result.setFailure(isNegated());
		if (isNegated())
			result.setFailureMessage("Specified XPath was found... Turn off negate if this is not desired");
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
	 * Set the XPath String this will be used as an xpath
	 * 
	 * @param xpath
	 *            String
	 */
	public void setXPathString(String xpath) {
		setProperty(new StringProperty(XPATH_KEY, xpath));
	}

	/**
	 * Set whether to ignore element whitespace
	 * 
	 * @param whitespace
	 */
	public void setWhitespace(boolean whitespace) {
		setProperty(new BooleanProperty(WHITESPACE_KEY, whitespace));
	}

	/**
	 * Set use validation
	 * 
	 * @param validate
	 */
	public void setValidating(boolean validate) {
		setProperty(new BooleanProperty(VALIDATE_KEY, validate));
	}

	/**
	 * Set whether this is namespace aware
	 * 
	 * @param namespace
	 */
	public void setNamespace(boolean namespace) {
		setProperty(new BooleanProperty(NAMESPACE_KEY, namespace));
	}

	/**
	 * Set tolerant mode if required
	 * 
	 * @param tolerant
	 *            true/false
	 */
	public void setTolerant(boolean tolerant) {
		setProperty(new BooleanProperty(TOLERANT_KEY, tolerant));
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
	 * Is this namespace aware?
	 * 
	 * @return boolean
	 */
	public boolean isNamespace() {
		return getPropertyAsBoolean(NAMESPACE_KEY, false);
	}

	/**
	 * Is this using tolerant mode?
	 * 
	 * @return boolean
	 */
	public boolean isTolerant() {
		return getPropertyAsBoolean(TOLERANT_KEY, false);
	}

	/**
	 * Negate the XPath test, that is return true if something is not found.
	 * 
	 * @return boolean negated
	 */
	public boolean isNegated() {
		return getPropertyAsBoolean(NEGATE_KEY, false);
	}

}