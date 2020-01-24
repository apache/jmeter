/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.util.concurrent.CompletionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedAssertion;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.XPathUtil;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Checks if the result is a well-formed XML content and whether it matches an
 * XPath
 *
 */
public class XPath2Assertion extends AbstractScopedAssertion implements Serializable, Assertion {
    private static final long serialVersionUID = 241L;
    // + JMX file attributes
    private static final String XPATH_KEY = "XPath.xpath"; // $NON-NLS-1$
    private static final String NEGATE_KEY = "XPath.negate"; // $NON-NLS-1$
    private static final String NAMESPACES = "XPath.namespaces"; // $NON-NLS-1$
    // - JMX file attributes
    public static final String DEFAULT_XPATH = "/";

    /**
     * Returns the result of the Assertion. Checks if the result is well-formed XML,
     * and that the XPath expression is matched (or not, as the case may be)
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        // no error as default
        AssertionResult result = new AssertionResult(getName());
        result.setFailure(false);
        result.setFailureMessage("");
        String responseData = null;
        if (isScopeVariable()) {
            String inputString = getThreadContext().getVariables().get(getVariableName());
            if (!StringUtils.isEmpty(inputString)) {
                responseData = inputString;
            }
        } else {
            responseData = response.getResponseDataAsString();
        }
        if (responseData == null) {
            return result.setResultForNull();
        }
        try {
            XPathUtil.computeAssertionResultUsingSaxon(result, responseData, getXPathString(),
                    getNamespaces(),isNegated());
        } catch (CompletionException|SaxonApiException e) { // NOSONAR We handle exception within result failure message
            result.setError(true);
            // CompletionException happens if caching fails
            result.setFailureMessage("Exception occurred computing assertion with XPath:" + getXPathString() + ", error:" + e.getMessage());
            return result;
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
     * @param xpath String
     */
    public void setXPathString(String xpath) {
        setProperty(new StringProperty(XPATH_KEY, xpath));
    }

    public void setNegated(boolean negate) {
        setProperty(new BooleanProperty(NEGATE_KEY, negate));
    }

    /**
     * Negate the XPath test, that is return true if something is not found.
     *
     * @return boolean negated
     */
    public boolean isNegated() {
        return getPropertyAsBoolean(NEGATE_KEY, false);
    }

    public void setNamespaces(String namespaces) {
        setProperty(NAMESPACES, namespaces);
    }

    public String getNamespaces() {
        return getPropertyAsString(NAMESPACES);
    }
}
