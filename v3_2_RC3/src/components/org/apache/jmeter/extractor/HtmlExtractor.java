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

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class HtmlExtractor extends AbstractScopedTestElement implements PostProcessor, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String EXTRACTOR_JSOUP = "JSOUP"; //$NON-NLS-1$

    public static final String EXTRACTOR_JODD = "JODD"; //$NON-NLS-1$

    public static final String DEFAULT_EXTRACTOR = ""; // $NON-NLS-1$

    private static final Logger log = LoggerFactory.getLogger(HtmlExtractor.class);

    private static final String EXPRESSION = "HtmlExtractor.expr"; // $NON-NLS-1$

    private static final String ATTRIBUTE = "HtmlExtractor.attribute"; // $NON-NLS-1$

    private static final String REFNAME = "HtmlExtractor.refname"; // $NON-NLS-1$

    private static final String MATCH_NUMBER = "HtmlExtractor.match_number"; // $NON-NLS-1$

    private static final String DEFAULT = "HtmlExtractor.default"; // $NON-NLS-1$

    private static final String EXTRACTOR_IMPL = "HtmlExtractor.extractor_impl"; // $NON-NLS-1$

    private static final String REF_MATCH_NR = "_matchNr"; // $NON-NLS-1$
    
    private static final String UNDERSCORE = "_";  // $NON-NLS-1$
    
    private static final String DEFAULT_EMPTY_VALUE = "HtmlExtractor.default_empty_value"; // $NON-NLS-1$

    private Extractor extractor;
    
    /**
     * Get the possible extractor implementations
     * @return Array containing the names of the possible extractors.
     */
    public static String[] getImplementations(){
        return new String[]{EXTRACTOR_JSOUP,EXTRACTOR_JODD};
    }


    /**
     * Parses the response data using CSS/JQuery expressions and saving the results
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
            log.debug("HtmlExtractor {}: processing result", getName());
        }
        // Fetch some variables
        JMeterVariables vars = context.getVariables();
        
        String refName = getRefName();
        String expression = getExpression();
        String attribute = getAttribute();
        int matchNumber = getMatchNumber();
        final String defaultValue = getDefaultValue();
        
        if (defaultValue.length() > 0  || isEmptyDefaultValue()){// Only replace default if it is provided or empty default value is explicitly requested
            vars.put(refName, defaultValue);
        }
        
        try {            
            List<String> matches = 
                    extractMatchingStrings(vars, expression, attribute, matchNumber, previousResult);
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
                log.warn("{}: Error while generating result. {}", getName(), e.toString());
            }
        }

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
            String expression, String attribute, int matchNumber,
            SampleResult previousResult) {
        int found = 0;
        List<String> result = new ArrayList<>();
        if (isScopeVariable()){
            String inputString=vars.get(getVariableName());
            if(!StringUtils.isEmpty(inputString)) {
                getExtractorImpl().extract(expression, attribute, matchNumber, inputString, result, found, "-1");
            } else {
                if(inputString==null) {
                    if (log.isWarnEnabled()) {
                        log.warn("No variable '{}' found to process by CSS/JQuery Extractor '{}', skipping processing",
                                getVariableName(), getName());
                    }
                }
                return Collections.emptyList();
            } 
        } else {
            List<SampleResult> sampleList = getSampleList(previousResult);
            int i=0;
            for (SampleResult sr : sampleList) {
                String inputString = sr.getResponseDataAsString();
                found = getExtractorImpl().extract(expression, attribute, matchNumber, inputString, result, found,
                        i>0 ? null : Integer.toString(i));
                i++;
                if (matchNumber > 0 && found == matchNumber){// no need to process further
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * @param impl Extractor implementation
     * @return Extractor
     */
    public static Extractor getExtractorImpl(String impl) {
        boolean useDefaultExtractor = DEFAULT_EXTRACTOR.equals(impl);
        if (useDefaultExtractor || EXTRACTOR_JSOUP.equals(impl)) {
            return new JSoupExtractor();
        } else if (EXTRACTOR_JODD.equals(impl)) {
            return new JoddExtractor();
        } else {
            throw new IllegalArgumentException("Extractor implementation:"+ impl+" is unknown");
        }
    }
    
    /**
     * 
     * @return Extractor
     */
    private Extractor getExtractorImpl() {
        if (extractor == null) {
            extractor = getExtractorImpl(getExtractor());
        }
        return extractor;
    }
    

    /**
     * Set the extractor. Has to be one of the list that can be obtained by
     * {@link HtmlExtractor#getImplementations()}
     * 
     * @param attribute
     *            The name of the extractor to be used
     */
    public void setExtractor(String attribute) {
        setProperty(EXTRACTOR_IMPL, attribute);
    }

    /**
     * Get the name of the currently configured extractor
     * @return The name of the extractor currently used
     */
    public String getExtractor() {
        return getPropertyAsString(EXTRACTOR_IMPL); // $NON-NLS-1$
    }

    
    public void setAttribute(String attribute) {
        setProperty(ATTRIBUTE, attribute);
    }

    public String getAttribute() {
        return getPropertyAsString(ATTRIBUTE, ""); // $NON-NLS-1$
    }

    public void setExpression(String regex) {
        setProperty(EXPRESSION, regex);
    }

    public String getExpression() {
        return getPropertyAsString(EXPRESSION);
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
}
