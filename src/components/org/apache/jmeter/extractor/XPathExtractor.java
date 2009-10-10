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
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
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
 * </dl>
 */
 /* This file is inspired by RegexExtractor.
 * author <a href="mailto:hpaluch@gitus.cz">Henryk Paluch</a>
 *            of <a href="http://www.gitus.com">Gitus a.s.</a>
 *
 * See Bugzilla: 37183
 */
public class XPathExtractor extends AbstractTestElement implements
        PostProcessor, Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();
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
    //- JMX file attributes


    private String concat(String s1,String s2){
        return new StringBuilder(s1).append("_").append(s2).toString(); // $NON-NLS-1$
    }

    /**
     * Do the job - extract value from (X)HTML response using XPath Query.
     * Return value as variable defined by REFNAME. Returns DEFAULT value
     * if not found.
     */
    public void process() {
        JMeterContext context = getThreadContext();
        JMeterVariables vars = context.getVariables();
        String refName = getRefName();
        vars.put(refName, getDefaultValue());
        vars.put(concat(refName,MATCH_NR), "0"); // In case parse fails // $NON-NLS-1$
        vars.remove(concat(refName,"1")); // In case parse fails // $NON-NLS-1$
        final SampleResult previousResult = context.getPreviousResult();

        try{
            Document d = parseResponse(previousResult);
            getValuesForXPath(d,getXPathQuery(),vars, refName);
        }catch(IOException e){// e.g. DTD not reachable
            final String errorMessage = "IOException on ("+getXPathQuery()+")";
            log.error(errorMessage,e);
            AssertionResult ass = new AssertionResult(getName());
            ass.setError(true);
            ass.setFailureMessage(new StringBuilder("IOException: ").append(e.getLocalizedMessage()).toString());
            previousResult.addAssertionResult(ass);
            previousResult.setSuccessful(false);
        } catch (ParserConfigurationException e) {// Should not happen
            final String errrorMessage = "error on ("+getXPathQuery()+")";
            log.error(errrorMessage,e);
            throw new JMeterError(errrorMessage,e);
        } catch (SAXException e) {// Can happen for bad input document
            log.warn("error on ("+getXPathQuery()+")"+e.getLocalizedMessage());
        } catch (TransformerException e) {// Can happen for incorrect XPath expression
            log.warn("error on ("+getXPathQuery()+")"+e.getLocalizedMessage());
        } catch (TidyException e) {
            AssertionResult ass = new AssertionResult("TidyException"); // $NON-NLS-1$
            ass.setFailure(true);
            ass.setFailureMessage(e.getLocalizedMessage());
            previousResult.addAssertionResult(ass);
            previousResult.setSuccessful(false);
        }
    }

    @Override
    public Object clone() {
        XPathExtractor cloned = (XPathExtractor) super.clone();
        return cloned;
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

    /*================= internal business =================*/
    /**
     * Converts (X)HTML response to DOM object Tree.
     * This version cares of charset of response.
     * @param result
     * @return
     *
     */
    private Document parseResponse(SampleResult result)
      throws UnsupportedEncodingException, IOException, ParserConfigurationException,SAXException,TidyException
    {
      //TODO: validate contentType for reasonable types?

      //TODO: is it really necessary to recode the data?
      // NOTE: responseData encoding is server specific
      //       Therefore we do byte -> unicode -> byte conversion
      //       to ensure UTF-8 encoding as required by XPathUtil
      String unicodeData = result.getResponseDataAsString();
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
     * @param d
     * @param query
     * @throws TransformerException
     */
    private void getValuesForXPath(Document d,String query, JMeterVariables vars, String refName)
     throws TransformerException
    {
        String val = null;
         XObject xObject = XPathAPI.eval(d, query);
        final int objectType = xObject.getType();
        if (objectType == XObject.CLASS_NODESET) {
            NodeList matches = xObject.nodelist();
            int length = matches.getLength();
            vars.put(concat(refName,MATCH_NR), String.valueOf(length));
            for (int i = 0 ; i < length; i++) {
                Node match = matches.item(i);
                if ( match instanceof Element){
                // elements have empty nodeValue, but we are usually interested in their content
                   final Node firstChild = match.getFirstChild();
                   if (firstChild != null) {
                       val = firstChild.getNodeValue();
                   } else {
                       val = match.getNodeValue(); // TODO is this correct?
                   }
                } else {
                   val = match.getNodeValue();
                }
                if ( val!=null){
                    if (i==0) {// Treat 1st match specially
                        vars.put(refName,val);
                    }
                    vars.put(concat(refName,String.valueOf(i+1)),val);
                }
            }
            vars.remove(concat(refName,String.valueOf(length+1)));
        } else if (objectType == XObject.CLASS_NULL
                || objectType == XObject.CLASS_UNKNOWN
                || objectType == XObject.CLASS_UNRESOLVEDVARIABLE) {
            log.warn("Unexpected object type: "+xObject.getTypeString()+" returned for: "+getXPathQuery());
         } else {
            val = xObject.toString();
            vars.put(concat(refName, MATCH_NR), "1");
            vars.put(refName, val);
            vars.put(concat(refName, "1"), val);
            vars.remove(concat(refName, "2"));
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
}
