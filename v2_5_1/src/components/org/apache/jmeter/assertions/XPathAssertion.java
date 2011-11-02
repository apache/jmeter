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
import org.apache.jmeter.util.TidyException;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Checks if the result is a well-formed XML content and whether it matches an
 * XPath
 *
 */
public class XPathAssertion extends AbstractTestElement implements Serializable, Assertion {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    // private static XPathAPI xpath = null;

    //+ JMX file attributes
    private static final String XPATH_KEY         = "XPath.xpath"; // $NON-NLS-1$
    private static final String WHITESPACE_KEY    = "XPath.whitespace"; // $NON-NLS-1$
    private static final String VALIDATE_KEY      = "XPath.validate"; // $NON-NLS-1$
    private static final String TOLERANT_KEY      = "XPath.tolerant"; // $NON-NLS-1$
    private static final String NEGATE_KEY        = "XPath.negate"; // $NON-NLS-1$
    private static final String NAMESPACE_KEY     = "XPath.namespace"; // $NON-NLS-1$
    private static final String QUIET_KEY         = "XPath.quiet"; // $NON-NLS-1$
    private static final String REPORT_ERRORS_KEY = "XPath.report_errors"; // $NON-NLS-1$
    private static final String SHOW_WARNINGS_KEY = "XPath.show_warnings"; // $NON-NLS-1$
    private static final String DOWNLOAD_DTDS     = "XPath.download_dtds"; // $NON-NLS-1$
    //- JMX file attributes

    public static final String DEFAULT_XPATH = "/";

    /**
     * Returns the result of the Assertion. Checks if the result is well-formed
     * XML, and that the XPath expression is matched (or not, as the case may
     * be)
     */
    public AssertionResult getResult(SampleResult response) {
        // no error as default
        AssertionResult result = new AssertionResult(getName());
        byte[] responseData = response.getResponseData();
        if (responseData.length == 0) {
            return result.setResultForNull();
        }
        result.setFailure(false);
        result.setFailureMessage("");

        if (log.isDebugEnabled()) {
            log.debug(new StringBuilder("Validation is set to ").append(isValidating()).toString());
            log.debug(new StringBuilder("Whitespace is set to ").append(isWhitespace()).toString());
            log.debug(new StringBuilder("Tolerant is set to ").append(isTolerant()).toString());
        }

        Document doc = null;

        boolean isXML = JOrphanUtils.isXML(responseData);

        try {
            doc = XPathUtil.makeDocument(new ByteArrayInputStream(responseData), isValidating(),
                    isWhitespace(), isNamespace(), isTolerant(), isQuiet(), showWarnings() , reportErrors(), isXML
                    , isDownloadDTDs());
        } catch (SAXException e) {
            log.debug("Caught sax exception: " + e);
            result.setError(true);
            result.setFailureMessage(new StringBuilder("SAXException: ").append(e.getMessage()).toString());
            return result;
        } catch (IOException e) {
            log.warn("Cannot parse result content", e);
            result.setError(true);
            result.setFailureMessage(new StringBuilder("IOException: ").append(e.getMessage()).toString());
            return result;
        } catch (ParserConfigurationException e) {
            log.warn("Cannot parse result content", e);
            result.setError(true);
            result.setFailureMessage(new StringBuilder("ParserConfigurationException: ").append(e.getMessage())
                    .toString());
            return result;
        } catch (TidyException e) {
            result.setError(true);
            result.setFailureMessage(e.getMessage());
            return result;
        }

        if (doc == null || doc.getDocumentElement() == null) {
            result.setError(true);
            result.setFailureMessage("Document is null, probably not parsable");
            return result;
        }

        NodeList nodeList = null;

        final String pathString = getXPathString();
        try {
            XObject xObject = XPathAPI.eval(doc, pathString);
            switch (xObject.getType()) {
                case XObject.CLASS_NODESET:
                    nodeList = xObject.nodelist();
                    break;
                case XObject.CLASS_BOOLEAN:
                    if (!xObject.bool()){
                        result.setFailure(!isNegated());
                        result.setFailureMessage("No Nodes Matched " + pathString);
                    }
                    return result;
                default:
                    result.setFailure(true);
                    result.setFailureMessage("Cannot understand: " + pathString);
                    return result;
            }
        } catch (TransformerException e) {
            result.setError(true);
            result.setFailureMessage(
                    new StringBuilder("TransformerException: ")
                    .append(e.getMessage())
                    .append(" for:")
                    .append(pathString)
                    .toString());
            return result;
        }

        if (nodeList == null || nodeList.getLength() == 0) {
            if (log.isDebugEnabled()) {
                log.debug(new StringBuilder("nodeList null no match  ").append(pathString).toString());
            }
            result.setFailure(!isNegated());
            result.setFailureMessage("No Nodes Matched " + pathString);
            return result;
        }
        if (log.isDebugEnabled()) {
            log.debug("nodeList length " + nodeList.getLength());
            if (!isNegated()) {
                for (int i = 0; i < nodeList.getLength(); i++){
                    log.debug(new StringBuilder("nodeList[").append(i).append("] ").append(nodeList.item(i)).toString());
                }
            }
        }
        result.setFailure(isNegated());
        if (isNegated()) {
            result.setFailureMessage("Specified XPath was found... Turn off negate if this is not desired");
        }
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

    public void setReportErrors(boolean val) {
        setProperty(REPORT_ERRORS_KEY, val, false);
    }

    public boolean reportErrors() {
        return getPropertyAsBoolean(REPORT_ERRORS_KEY, false);
    }

    public void setShowWarnings(boolean val) {
        setProperty(SHOW_WARNINGS_KEY, val, false);
    }

    public boolean showWarnings() {
        return getPropertyAsBoolean(SHOW_WARNINGS_KEY, false);
    }

    public void setQuiet(boolean val) {
        setProperty(QUIET_KEY, val, true);
    }

    public boolean isQuiet() {
        return getPropertyAsBoolean(QUIET_KEY, true);
    }

    public void setDownloadDTDs(boolean val) {
        setProperty(DOWNLOAD_DTDS, val, false);
    }

    public boolean isDownloadDTDs() {
        return getPropertyAsBoolean(DOWNLOAD_DTDS, false);
    }

}
