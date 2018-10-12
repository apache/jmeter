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
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedAssertion;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.TidyException;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Checks if the result is a well-formed XML content and whether it matches an
 * XPath
 *
 */
public class XPathAssertion extends AbstractScopedAssertion implements Serializable, Assertion {
    private static final Logger log = LoggerFactory.getLogger(XPathAssertion.class);

    private static final long serialVersionUID = 241L;

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
    @Override
    public AssertionResult getResult(SampleResult response) {
        // no error as default
        AssertionResult result = new AssertionResult(getName());
        result.setFailure(false);
        result.setFailureMessage("");

        byte[] responseData = null;
        Document doc = null;

        try {
            if (isScopeVariable()){
                String inputString=getThreadContext().getVariables().get(getVariableName());
                if (!StringUtils.isEmpty(inputString)) {
                    responseData = inputString.getBytes(StandardCharsets.UTF_8);
                } 
            } else {
                responseData = response.getResponseData();
            }
            
            if (responseData == null || responseData.length == 0) {
                return result.setResultForNull();
            }

            if(log.isDebugEnabled()) {
                log.debug("Validation is set to {}, Whitespace is set to {}, Tolerant is set to {}", isValidating(),
                    isWhitespace(), isTolerant());
            }
            boolean isXML = JOrphanUtils.isXML(responseData);

            doc = XPathUtil.makeDocument(new ByteArrayInputStream(responseData), isValidating(),
                    isWhitespace(), isNamespace(), isTolerant(), isQuiet(), showWarnings() , reportErrors(), isXML
                    , isDownloadDTDs());
        } catch (SAXException e) {
            log.debug("Caught sax exception.", e);
            result.setError(true);
            result.setFailureMessage("SAXException: " + e.getMessage());
            return result;
        } catch (IOException e) {
            log.warn("Cannot parse result content.", e);
            result.setError(true);
            result.setFailureMessage("IOException: " + e.getMessage());
            return result;
        } catch (ParserConfigurationException e) {
            log.warn("Cannot parse result content.", e);
            result.setError(true);
            result.setFailureMessage("ParserConfigurationException: " + e.getMessage());
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
        XPathUtil.computeAssertionResult(result, doc, getXPathString(), isNegated());
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
     * @param whitespace Flag whether whitespace elements should be ignored
     */
    public void setWhitespace(boolean whitespace) {
        setProperty(new BooleanProperty(WHITESPACE_KEY, whitespace));
    }

    /**
     * Set use validation
     *
     * @param validate Flag whether validation should be used
     */
    public void setValidating(boolean validate) {
        setProperty(new BooleanProperty(VALIDATE_KEY, validate));
    }

    /**
     * Set whether this is namespace aware
     *
     * @param namespace Flag whether namespace should be used
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
     * Is this whitespace ignored.
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
