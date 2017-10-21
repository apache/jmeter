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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.Document;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class BoundaryExtractor extends AbstractScopedTestElement implements PostProcessor, Serializable {

    private static final Logger log = LoggerFactory.getLogger(BoundaryExtractor.class);

    private static final long serialVersionUID = 1L;

    private static final String REFNAME = "BoundaryExtractor.refname"; // $NON-NLS-1$

    private static final String MATCH_NUMBER = "BoundaryExtractor.match_number"; // $NON-NLS-1$

    private static final String L_BOUNDARY = "BoundaryExtractor.lboundary"; // $NON-NLS-1$

    private static final String R_BOUNDARY = "BoundaryExtractor.rboundary"; // $NON-NLS-1$

    private static final String DEFAULT_EMPTY_VALUE = "BoundaryExtractor.default_empty_value"; // $NON-NLS-1$

    private static final String DEFAULT = "BoundaryExtractor.default"; // $NON-NLS-1$

    private static final String REF_MATCH_NR = "_matchNr"; // $NON-NLS-1$
    
    private static final String UNDERSCORE = "_";  // $NON-NLS-1$
    
    // What to match against. N.B. do not change the string value or test plans will break!
    private static final String MATCH_AGAINST = "BoundaryExtractor.useHeaders"; // $NON-NLS-1$
    /*
     * Permissible values:
     *  true - match against headers
     *  false or absent - match against body (this was the original default)
     *  URL - match against URL
     *  These are passed to the setUseField() method
     *
     *  Do not change these values!
    */
    public static final String USE_HDRS = "true"; // $NON-NLS-1$
    public static final String USE_REQUEST_HDRS = "request_headers"; // $NON-NLS-1$
    public static final String USE_BODY = "false"; // $NON-NLS-1$
    public static final String USE_BODY_UNESCAPED = "unescaped"; // $NON-NLS-1$
    public static final String USE_BODY_AS_DOCUMENT = "as_document"; // $NON-NLS-1$
    public static final String USE_URL = "URL"; // $NON-NLS-1$
    public static final String USE_CODE = "code"; // $NON-NLS-1$
    public static final String USE_MESSAGE = "message"; // $NON-NLS-1$

    /**
     * Parses the response data using Boundaries and saving the results
     * into variables for use later in the test.
     *
     * @see org.apache.jmeter.processor.PostProcessor#process()
     */
    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        SampleResult previousResult = context.getPreviousResult();
        if (previousResult == null) {
            return;
        }
        if(log.isDebugEnabled()) {
            log.debug("Boundary Extractor {}: processing result", getName());
        }
        if(StringUtils.isEmpty(getLeftBoundary()) ||
                StringUtils.isEmpty(getRightBoundary()) ||
                StringUtils.isEmpty(getRefName())
                ) {
            throw new IllegalArgumentException("One of the mandatory properties is missing in Boundary Extractor:"+
                getName());
        }
        // Fetch some variables
        JMeterVariables vars = context.getVariables();
        
        String refName = getRefName();
        int matchNumber = getMatchNumber();
        final String defaultValue = getDefaultValue();
        
        if (defaultValue.length() > 0  || isEmptyDefaultValue()){// Only replace default if it is provided or empty default value is explicitly requested
            vars.put(refName, defaultValue);
        }
        
        try {            
            List<String> matches = 
                    extractMatchingStrings(vars, getLeftBoundary(), getRightBoundary(), matchNumber, previousResult);
            int prevCount = 0;
            String prevString = vars.get(refName + REF_MATCH_NR);
            if (prevString != null) {
                vars.remove(refName + REF_MATCH_NR);// ensure old value is not left defined
                try {
                    prevCount = Integer.parseInt(prevString);
                } catch (NumberFormatException nfe) {
                    if (log.isWarnEnabled()) {
                        log.warn("{}: Could not parse number: '{}'.", getName(), prevString);
                    }
                }
            }
            int matchCount=0;// Number of refName_n variable sets to keep
            String match;
            if (matchNumber >= 0) {// Original match behaviour
                match = getCorrectMatch(matches, matchNumber);
                if (match != null) {
                    vars.put(refName, match);
                } 
            } else // < 0 means we save all the matches
            {
                matchCount = matches.size();
                vars.put(refName + REF_MATCH_NR, Integer.toString(matchCount));// Save the count
                for (int i = 1; i <= matchCount; i++) {
                    match = getCorrectMatch(matches, i);
                    if (match != null) {
                        final String refNameN = new StringBuilder(refName).append(UNDERSCORE).append(i).toString();
                        vars.put(refNameN, match);
                    }
                }
            }
            // Remove any left-over variables
            for (int i = matchCount + 1; i <= prevCount; i++) {
                final String refNameN = new StringBuilder(refName).append(UNDERSCORE).append(i).toString();
                vars.remove(refNameN);
            }
        } catch (RuntimeException e) {
            if (log.isWarnEnabled()) {
                log.warn("{}: Error while generating result. {}", getName(), e.toString()); // NOSONAR We don't want to be too verbose
            }
        }
    }

    private String getInputString(SampleResult result) {
        String inputString = useUrl() ? result.getUrlAsString() // Bug 39707
                : useHeaders() ? result.getResponseHeaders()
                : useRequestHeaders() ? result.getRequestHeaders()
                : useCode() ? result.getResponseCode() // Bug 43451
                : useMessage() ? result.getResponseMessage() // Bug 43451
                : useUnescapedBody() ? StringEscapeUtils.unescapeHtml4(result.getResponseDataAsString())
                : useBodyAsDocument() ? Document.getTextFromDocument(result.getResponseData())
                : result.getResponseDataAsString() // Bug 36898
                ;
       log.debug("Input = '{}'", inputString);
       return inputString;
    }
    /**
     * Grab the appropriate result from the list.
     *
     * @param matches
     *            list of matches
     * @param entry
     *            the entry number in the list
     * @return MatchResult
     */
    private String getCorrectMatch(List<String> matches, int entry) {
        int matchSize = matches.size();

        if (matchSize <= 0 || entry > matchSize){
            return null;
        }

        if (entry == 0) // Random match
        {
            return matches.get(JMeterUtils.getRandomInt(matchSize));
        }

        return matches.get(entry - 1);
    }

    private List<String> extractMatchingStrings(JMeterVariables vars,
            String leftBoundary, String rightBoundary, int matchNumber,
            SampleResult previousResult) {
        int found = 0;
        List<String> result = new ArrayList<>();
        if (isScopeVariable()){
            String inputString=vars.get(getVariableName());
            if(!StringUtils.isEmpty(inputString)) {
                extract(leftBoundary, rightBoundary, matchNumber, inputString, result, found);
            } else {
                if(inputString==null) {
                    if (log.isWarnEnabled()) {
                        log.warn("No variable '{}' found to process by Boundary Extractor '{}', skipping processing",
                                getVariableName(), getName());
                    }
                }
                return Collections.emptyList();
            } 
        } else {
            List<SampleResult> sampleList = getSampleList(previousResult);
            for (SampleResult sr : sampleList) {
                String inputString = getInputString(sr);
                found = extract(leftBoundary, rightBoundary, matchNumber, inputString, result, found);
                if (matchNumber > 0 && found == matchNumber){// no need to process further
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 
     * @param leftBoundary
     * @param rightBoundary
     * @param matchNumber
     * @param inputString
     * @param result
     * @param found
     * @return int found updated
     */
    private int extract(String leftBoundary, String rightBoundary, int matchNumber, String inputString,
            List<String> result, int found) {
        int startIndex = -1;
        int endIndex;
        int newFound = found;
        List<String> matches = new ArrayList<>();
        while(true) {
            startIndex = inputString.indexOf(leftBoundary, startIndex+1);
            if(startIndex >= 0) {
                endIndex = inputString.indexOf(rightBoundary, startIndex+leftBoundary.length());
                if(endIndex >= 0) {
                    matches.add(inputString.substring(startIndex+leftBoundary.length(), endIndex));
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        
        for (String element : matches) {
            if (matchNumber <= 0 || newFound != matchNumber) {
                result.add(element);
                newFound++;
            } else {
                break;
            }
        }
        return newFound;
    }

    public void setRefName(String refName) {
        setProperty(REFNAME, refName);
    }

    public String getRefName() {
        return getPropertyAsString(REFNAME);
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

    public void setMatchNumber(String matchNumber) {
        setProperty(MATCH_NUMBER, matchNumber);
    }

    public int getMatchNumber() {
        return getPropertyAsInt(MATCH_NUMBER);
    }

    public String getMatchNumberAsString() {
        return getPropertyAsString(MATCH_NUMBER);
    }
    
    public void setLeftBoundary(String leftBoundary) {
        setProperty(L_BOUNDARY, leftBoundary);
    }
    
    public String getLeftBoundary() {
        return getPropertyAsString(L_BOUNDARY);
    }
    
    public void setRightBoundary(String rightBoundary) {
        setProperty(R_BOUNDARY, rightBoundary);
    }
    
    public String getRightBoundary() {
        return getPropertyAsString(R_BOUNDARY);
    }

    /**
     * Sets the value of the variable if no matches are found
     *
     * @param defaultValue The default value for the variable
     */
    public void setDefaultValue(String defaultValue) {
        setProperty(DEFAULT, defaultValue);
    }

    /**
     * @param defaultEmptyValue boolean set value to "" if not found
     */
    public void setDefaultEmptyValue(boolean defaultEmptyValue) {
        setProperty(DEFAULT_EMPTY_VALUE, defaultEmptyValue);
    }
    
    /**
     * Get the default value for the variable if no matches are found
     * @return The default value for the variable
     */
    public String getDefaultValue() {
        return getPropertyAsString(DEFAULT);
    }
    
    /**
     * @return boolean set value to "" if not found
     */
    public boolean isEmptyDefaultValue() {
        return getPropertyAsBoolean(DEFAULT_EMPTY_VALUE);
    }


    public boolean useHeaders() {
        return USE_HDRS.equalsIgnoreCase( getPropertyAsString(MATCH_AGAINST));
    }

    public boolean useRequestHeaders() {
        return USE_REQUEST_HDRS.equalsIgnoreCase(getPropertyAsString(MATCH_AGAINST));
    }

    // Allow for property not yet being set (probably only applies to Test cases)
    public boolean useBody() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return prop.length()==0 || USE_BODY.equalsIgnoreCase(prop);// $NON-NLS-1$
    }

    public boolean useUnescapedBody() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return USE_BODY_UNESCAPED.equalsIgnoreCase(prop);// $NON-NLS-1$
    }

    public boolean useBodyAsDocument() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return USE_BODY_AS_DOCUMENT.equalsIgnoreCase(prop);// $NON-NLS-1$
    }

    public boolean useUrl() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return USE_URL.equalsIgnoreCase(prop);
    }

    public boolean useCode() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return USE_CODE.equalsIgnoreCase(prop);
    }

    public boolean useMessage() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return USE_MESSAGE.equalsIgnoreCase(prop);
    }

    public void setUseField(String actionCommand) {
        setProperty(MATCH_AGAINST,actionCommand);
    }
}
