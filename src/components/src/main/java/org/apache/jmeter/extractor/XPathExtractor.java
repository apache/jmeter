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
package org.apache.jmeter.extractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.TidyException;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.util.JMeterError;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Extracts text from (X)HTML response using XPath query language
 * Example XPath queries:
 * <dl>
 * <dt>/html/head/title</dt>
 *     <dd>extracts Title from HTML response</dd>
 * <dt>//form[@name='countryForm']//select[@name='country']/option[text()='Czech Republic'])/@value
 *     <dd>extracts value attribute of option element that match text 'Czech Republic'
 *                 inside of select element with name attribute  'country' inside of
 *                 form with name attribute 'countryForm'</dd>
 * <dt>//head</dt>
 *     <dd>extracts the XML fragment for head node.</dd>
 * <dt>//head/text()</dt>
 *     <dd>extracts the text content for head node.</dd>
 * </dl>
  see org.apache.jmeter.extractor.TestXPathExtractor for unit tests
 */
public class XPathExtractor extends AbstractScopedTestElement implements
        PostProcessor, Serializable {
    private static final Logger log = LoggerFactory.getLogger(XPathExtractor.class);

    private static final long serialVersionUID = 242L;
    
    private static final int DEFAULT_VALUE = -1;
    public static final String DEFAULT_VALUE_AS_STRING = Integer.toString(DEFAULT_VALUE);

    private static final String REF_MATCH_NR    = "matchNr"; // $NON-NLS-1$

    //+ JMX file attributes
    private static final String XPATH_QUERY     = "XPathExtractor.xpathQuery"; // $NON-NLS-1$
    private static final String REFNAME         = "XPathExtractor.refname"; // $NON-NLS-1$
    private static final String DEFAULT         = "XPathExtractor.default"; // $NON-NLS-1$
    private static final String TOLERANT        = "XPathExtractor.tolerant"; // $NON-NLS-1$
    private static final String NAMESPACE       = "XPathExtractor.namespace"; // $NON-NLS-1$
    private static final String QUIET           = "XPathExtractor.quiet"; // $NON-NLS-1$
    private static final String REPORT_ERRORS   = "XPathExtractor.report_errors"; // $NON-NLS-1$
    private static final String SHOW_WARNINGS   = "XPathExtractor.show_warnings"; // $NON-NLS-1$
    private static final String DOWNLOAD_DTDS   = "XPathExtractor.download_dtds"; // $NON-NLS-1$
    private static final String WHITESPACE      = "XPathExtractor.whitespace"; // $NON-NLS-1$
    private static final String VALIDATE        = "XPathExtractor.validate"; // $NON-NLS-1$
    private static final String FRAGMENT        = "XPathExtractor.fragment"; // $NON-NLS-1$
    private static final String MATCH_NUMBER    = "XPathExtractor.matchNumber"; // $NON-NLS-1$
    //- JMX file attributes


    private String concat(String s1,String s2){
        return s1 + "_" + s2; // $NON-NLS-1$
    }

    private String concat(String s1, int i){
        return s1 + "_" + i; // $NON-NLS-1$
    }

    /**
     * Do the job - extract value from (X)HTML response using XPath Query.
     * Return value as variable defined by REFNAME. Returns DEFAULT value
     * if not found.
     */
    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        final SampleResult previousResult = context.getPreviousResult();
        if (previousResult == null){
            return;
        }
        JMeterVariables vars = context.getVariables();
        String refName = getRefName();
        vars.put(refName, getDefaultValue());
        final String matchNR = concat(refName,REF_MATCH_NR);
        int prevCount=0; // number of previous matches
        try {
            prevCount=Integer.parseInt(vars.get(matchNR));
        } catch (NumberFormatException e) {
            // ignored
        }
        vars.put(matchNR, "0"); // In case parse fails // $NON-NLS-1$
        vars.remove(concat(refName,"1")); // In case parse fails // $NON-NLS-1$

        int matchNumber = getMatchNumber();
        List<String> matches = new ArrayList<>();
        try{
            if (isScopeVariable()){
                String inputString=vars.get(getVariableName());
                if(inputString != null) {
                    if(inputString.length()>0) {
                        Document d =  parseResponse(inputString);
                        getValuesForXPath(d,getXPathQuery(), matches, matchNumber);
                    }
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("No variable '{}' found to process by XPathExtractor '{}', skipping processing",
                                getVariableName(), getName());
                    }
                }
            } else {
                List<SampleResult> samples = getSampleList(previousResult);
                for (SampleResult res : samples) {
                    Document d = parseResponse(res.getResponseDataAsString());
                    getValuesForXPath(d,getXPathQuery(), matches, matchNumber);
                }
            }
            final int matchCount = matches.size();
            vars.put(matchNR, String.valueOf(matchCount));
            if (matchCount > 0){
                String value = matches.get(0);
                if (value != null) {
                    vars.put(refName, value);
                }
                for(int i=0; i < matchCount; i++){
                    value = matches.get(i);
                    if (value != null) {
                        vars.put(concat(refName,i+1),matches.get(i));
                    }
                }
            }
            vars.remove(concat(refName,matchCount+1)); // Just in case
            // Clear any other remaining variables
            for(int i=matchCount+2; i <= prevCount; i++) {
                vars.remove(concat(refName,i));
            }
        }catch(IOException e){// e.g. DTD not reachable
            log.error("IOException on ({})", getXPathQuery(), e);
            AssertionResult ass = new AssertionResult(getName());
            ass.setError(true);
            ass.setFailureMessage("IOException: " + e.getLocalizedMessage());
            previousResult.addAssertionResult(ass);
            previousResult.setSuccessful(false);
        } catch (ParserConfigurationException e) {// Should not happen
            final String errrorMessage = "ParserConfigurationException while processing ("+getXPathQuery()+")";
            log.error(errrorMessage,e);
            throw new JMeterError(errrorMessage,e);
        } catch (SAXException e) {// Can happen for bad input document
            if (log.isWarnEnabled()) {
                log.warn("SAXException while processing ({}). {}", getXPathQuery(), e.getLocalizedMessage());
            }
            addAssertionFailure(previousResult, e, false); // Should this also fail the sample?
        } catch (TransformerException e) {// Can happen for incorrect XPath expression
            if (log.isWarnEnabled()) {
                log.warn("TransformerException while processing ({}). {}", getXPathQuery(), e.getLocalizedMessage());
            }
            addAssertionFailure(previousResult, e, false);
        } catch (TidyException e) {
            // Will already have been logged by XPathUtil
            addAssertionFailure(previousResult, e, true); // fail the sample
        }
    }

    private void addAssertionFailure(final SampleResult previousResult,
            final Throwable thrown, final boolean setFailed) {
        AssertionResult ass = new AssertionResult(getName()); // $NON-NLS-1$
        ass.setFailure(true);
        ass.setFailureMessage(thrown.getLocalizedMessage()+"\nSee log file for further details.");
        previousResult.addAssertionResult(ass);
        if (setFailed){
            previousResult.setSuccessful(false);
        }
    }

    /*============= object properties ================*/
    public void setXPathQuery(String val){
        setProperty(XPATH_QUERY,val);
    }

    public String getXPathQuery(){
        return getPropertyAsString(XPATH_QUERY);
    }

    public void setRefName(String refName) {
        setProperty(REFNAME, refName);
    }

    public String getRefName() {
        return getPropertyAsString(REFNAME);
    }

    public void setDefaultValue(String val) {
        setProperty(DEFAULT, val);
    }

    public String getDefaultValue() {
        return getPropertyAsString(DEFAULT);
    }

    public void setTolerant(boolean val) {
        setProperty(new BooleanProperty(TOLERANT, val));
    }

    public boolean isTolerant() {
        return getPropertyAsBoolean(TOLERANT);
    }

    public void setNameSpace(boolean val) {
        setProperty(new BooleanProperty(NAMESPACE, val));
    }

    public boolean useNameSpace() {
        return getPropertyAsBoolean(NAMESPACE);
    }

    public void setReportErrors(boolean val) {
            setProperty(REPORT_ERRORS, val, false);
    }

    public boolean reportErrors() {
        return getPropertyAsBoolean(REPORT_ERRORS, false);
    }

    public void setShowWarnings(boolean val) {
        setProperty(SHOW_WARNINGS, val, false);
    }

    public boolean showWarnings() {
        return getPropertyAsBoolean(SHOW_WARNINGS, false);
    }

    public void setQuiet(boolean val) {
        setProperty(QUIET, val, true);
    }

    public boolean isQuiet() {
        return getPropertyAsBoolean(QUIET, true);
    }

    /**
     * Should we return fragment as text, rather than text of fragment?
     * @return true if we should return fragment rather than text
     */
    public boolean getFragment() {
        return getPropertyAsBoolean(FRAGMENT, false);
    }

    /**
     * Should we return fragment as text, rather than text of fragment?
     * @param selected true to return fragment.
     */
    public void setFragment(boolean selected) {
        setProperty(FRAGMENT, selected, false);
    }

    /*================= internal business =================*/
    /**
     * Converts (X)HTML response to DOM object Tree.
     * This version cares of charset of response.
     * @param unicodeData
     * @return the parsed document
     *
     */
    private Document parseResponse(String unicodeData)
      throws IOException, ParserConfigurationException,SAXException,TidyException
    {
      //TODO: validate contentType for reasonable types?

      // NOTE: responseData encoding is server specific
      //       Therefore we do byte -> unicode -> byte conversion
      //       to ensure UTF-8 encoding as required by XPathUtil
      // convert unicode String -> UTF-8 bytes
      byte[] utf8data = unicodeData.getBytes(StandardCharsets.UTF_8);
      ByteArrayInputStream in = new ByteArrayInputStream(utf8data);
      boolean isXML = JOrphanUtils.isXML(utf8data);
      // this method assumes UTF-8 input data
      return XPathUtil.makeDocument(in,false,false,useNameSpace(),isTolerant(),isQuiet(),showWarnings(),reportErrors()
              ,isXML, isDownloadDTDs());
    }

    /**
     * Extract value from Document d by XPath query.
     * @param d the document
     * @param query the query to execute
     * @param matchStrings list of matched strings (may include nulls)
     * @param matchNumber int Match Number
     *
     * @throws TransformerException
     */
    private void getValuesForXPath(Document d,String query, List<String> matchStrings, int matchNumber)
        throws TransformerException {
        XPathUtil.putValuesForXPathInList(d, query, matchStrings, getFragment(), matchNumber);
    }

    public void setWhitespace(boolean selected) {
        setProperty(WHITESPACE, selected, false);
    }

    public boolean isWhitespace() {
        return getPropertyAsBoolean(WHITESPACE, false);
    }

    public void setValidating(boolean selected) {
        setProperty(VALIDATE, selected);
    }

    public boolean isValidating() {
        return getPropertyAsBoolean(VALIDATE, false);
    }

    public void setDownloadDTDs(boolean selected) {
        setProperty(DOWNLOAD_DTDS, selected, false);
    }

    public boolean isDownloadDTDs() {
        return getPropertyAsBoolean(DOWNLOAD_DTDS, false);
    }
    
    /**
     * Set which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning random.
     *
     * @param matchNumber The number of the match to be used
     */
    public void setMatchNumber(int matchNumber) {
        setProperty(new IntegerProperty(MATCH_NUMBER, matchNumber));
    }

    /**
     * Set which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning random.
     *
     * @param matchNumber The number of the match to be used
     */
    public void setMatchNumber(String matchNumber) {
        setProperty(MATCH_NUMBER, matchNumber);
    }

    /**
     * Return which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning random.
     *
     * @return matchNumber The number of the match to be used
     */
    public int getMatchNumber() {
        return getPropertyAsInt(MATCH_NUMBER, DEFAULT_VALUE);
    }

    /**
     * Return which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning random.
     *
     * @return matchNumber The number of the match to be used
     */
    public String getMatchNumberAsString() {
        return getPropertyAsString(MATCH_NUMBER, DEFAULT_VALUE_AS_STRING);
    }
}
