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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.TidyException;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterError;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//@see org.apache.jmeter.extractor.TestXPathExtractor for unit tests

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
 */
 /* This file is inspired by RegexExtractor.
 * author <a href="mailto:hpaluch@gitus.cz">Henryk Paluch</a>
 *            of <a href="http://www.gitus.com">Gitus a.s.</a>
 *
 * See Bugzilla: 37183
 */
public class XPathExtractor extends AbstractScopedTestElement implements
        PostProcessor, Serializable, Searchable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    private static final String MATCH_NR = "matchNr"; // $NON-NLS-1$

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
    //- JMX file attributes


    private String concat(String s1,String s2){
        return new StringBuilder(s1).append("_").append(s2).toString(); // $NON-NLS-1$
    }

    private String concat(String s1, int i){
        return new StringBuilder(s1).append("_").append(i).toString(); // $NON-NLS-1$
    }

    /**
     * Do the job - extract value from (X)HTML response using XPath Query.
     * Return value as variable defined by REFNAME. Returns DEFAULT value
     * if not found.
     */
    public void process() {
        JMeterContext context = getThreadContext();
        final SampleResult previousResult = context.getPreviousResult();
        if (previousResult == null){
            return;
        }
        JMeterVariables vars = context.getVariables();
        String refName = getRefName();
        vars.put(refName, getDefaultValue());
        final String matchNR = concat(refName,MATCH_NR);
        int prevCount=0; // number of previous matches
        try {
            prevCount=Integer.parseInt(vars.get(matchNR));
        } catch (NumberFormatException e) {
            // ignored
        }
        vars.put(matchNR, "0"); // In case parse fails // $NON-NLS-1$
        vars.remove(concat(refName,"1")); // In case parse fails // $NON-NLS-1$

        List<String> matches = new ArrayList<String>();
        try{
            if (isScopeVariable()){
                String inputString=vars.get(getVariableName());
                Document d =  parseResponse(inputString);
                getValuesForXPath(d,getXPathQuery(),matches);
            } else {
                List<SampleResult> samples = getSampleList(previousResult);
                for (SampleResult res : samples) {
                    Document d = parseResponse(res.getResponseDataAsString());
                    getValuesForXPath(d,getXPathQuery(),matches);
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
            final String errorMessage = "IOException on ("+getXPathQuery()+")";
            log.error(errorMessage,e);
            AssertionResult ass = new AssertionResult(getName());
            ass.setError(true);
            ass.setFailureMessage(new StringBuilder("IOException: ").append(e.getLocalizedMessage()).toString());
            previousResult.addAssertionResult(ass);
            previousResult.setSuccessful(false);
        } catch (ParserConfigurationException e) {// Should not happen
            final String errrorMessage = "ParserConfigurationException while processing ("+getXPathQuery()+")";
            log.error(errrorMessage,e);
            throw new JMeterError(errrorMessage,e);
        } catch (SAXException e) {// Can happen for bad input document
            log.warn("SAXException while processing ("+getXPathQuery()+") "+e.getLocalizedMessage());
            addAssertionFailure(previousResult, e, false); // Should this also fail the sample?
        } catch (TransformerException e) {// Can happen for incorrect XPath expression
            log.warn("TransformerException while processing ("+getXPathQuery()+") "+e.getLocalizedMessage());
            addAssertionFailure(previousResult, e, false);
        } catch (TidyException e) {
            // Will already have been logged by XPathUtil
            addAssertionFailure(previousResult, e, true); // fail the sample
        }
    }

    private void addAssertionFailure(final SampleResult previousResult,
            final Throwable thrown, final boolean setFailed) {
        AssertionResult ass = new AssertionResult(thrown.getClass().getSimpleName()); // $NON-NLS-1$
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
     * @return
     *
     */
    private Document parseResponse(String unicodeData)
      throws UnsupportedEncodingException, IOException, ParserConfigurationException,SAXException,TidyException
    {
      //TODO: validate contentType for reasonable types?

      // NOTE: responseData encoding is server specific
      //       Therefore we do byte -> unicode -> byte conversion
      //       to ensure UTF-8 encoding as required by XPathUtil
      // convert unicode String -> UTF-8 bytes
      byte[] utf8data = unicodeData.getBytes("UTF-8"); // $NON-NLS-1$
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
     *
     * @throws TransformerException
     */
    private void getValuesForXPath(Document d,String query, List<String> matchStrings)
        throws TransformerException {
        String val = null;
        XObject xObject = XPathAPI.eval(d, query);
        final int objectType = xObject.getType();
        if (objectType == XObject.CLASS_NODESET) {
            NodeList matches = xObject.nodelist();
            int length = matches.getLength();
            for (int i = 0 ; i < length; i++) {
                Node match = matches.item(i);
                if ( match instanceof Element){
                    if (getFragment()){
                        val = getValueForNode(match);
                    } else {
                        // elements have empty nodeValue, but we are usually interested in their content
                        final Node firstChild = match.getFirstChild();
                        if (firstChild != null) {
                            val = firstChild.getNodeValue();
                        } else {
                            val = match.getNodeValue(); // TODO is this correct?
                        }
                    }
                } else {
                   val = match.getNodeValue();
                }
                matchStrings.add(val);
            }
        } else if (objectType == XObject.CLASS_NULL
                || objectType == XObject.CLASS_UNKNOWN
                || objectType == XObject.CLASS_UNRESOLVEDVARIABLE) {
            log.warn("Unexpected object type: "+xObject.getTypeString()+" returned for: "+getXPathQuery());
        } else {
            val = xObject.toString();
            matchStrings.add(val);
      }
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

    private String getValueForNode(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException e) {
            sw.write(e.getMessageAndLocation());
        }
        return sw.toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean searchContent(String textToSearch) throws Exception {
        String searchedTextLowerCase = textToSearch.toLowerCase();
        if(testField(getComment(), searchedTextLowerCase)) {
            return true;
        }
        if(testField(getVariableName(), searchedTextLowerCase)) {
            return true;
        }
        if(testField(getRefName(), searchedTextLowerCase)) {
            return true;
        }
        if(testField(getDefaultValue(), searchedTextLowerCase)) {
            return true;
        }
        if(testField(getXPathQuery(), searchedTextLowerCase)) {
            return true;
        }
        return false;
    }
}
