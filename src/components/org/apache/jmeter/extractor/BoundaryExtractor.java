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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * Extracts Strings from a text response between a start and end boundary.
 */
public class BoundaryExtractor extends AbstractScopedTestElement implements PostProcessor, Serializable {

    private static final Logger log = LoggerFactory.getLogger(BoundaryExtractor.class);

    private static final long serialVersionUID = 2L;

    private static final String REFNAME = "BoundaryExtractor.refname"; // $NON-NLS-1$
    private static final String MATCH_NUMBER = "BoundaryExtractor.match_number"; // $NON-NLS-1$
    private static final String L_BOUNDARY = "BoundaryExtractor.lboundary"; // $NON-NLS-1$
    private static final String R_BOUNDARY = "BoundaryExtractor.rboundary"; // $NON-NLS-1$
    private static final String DEFAULT_EMPTY_VALUE = "BoundaryExtractor.default_empty_value"; // $NON-NLS-1$
    private static final String DEFAULT = "BoundaryExtractor.default"; // $NON-NLS-1$
    private static final String REF_MATCH_NR = "_matchNr"; // $NON-NLS-1$
    private static final char UNDERSCORE = '_';  // $NON-NLS-1$

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
    private static final String USE_HDRS = "true"; // $NON-NLS-1$
    private static final String USE_REQUEST_HDRS = "request_headers"; // $NON-NLS-1$
    private static final String USE_BODY = "false"; // $NON-NLS-1$
    private static final String USE_BODY_UNESCAPED = "unescaped"; // $NON-NLS-1$
    private static final String USE_BODY_AS_DOCUMENT = "as_document"; // $NON-NLS-1$
    private static final String USE_URL = "URL"; // $NON-NLS-1$
    private static final String USE_CODE = "code"; // $NON-NLS-1$
    private static final String USE_MESSAGE = "message"; // $NON-NLS-1$

    /**
     * Parses the response data using Boundaries and saving the results
     * into variables for use later in the test.
     *
     * @see PostProcessor#process()
     */
    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        SampleResult previousResult = context.getPreviousResult();
        if (previousResult == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Boundary Extractor {}: processing result", getName());
        }
        if (StringUtils.isAnyEmpty(getLeftBoundary(), getRightBoundary(), getRefName())) {
            throw new IllegalArgumentException(
                    "One of the mandatory properties is missing in Boundary Extractor:" + getName());
        }

        JMeterVariables vars = context.getVariables();

        String refName = getRefName();
        final String defaultValue = getDefaultValue();

        if (StringUtils.isNotBlank(defaultValue) || isEmptyDefaultValue()) {
            vars.put(refName, defaultValue);
        }

        int matchNumber = getMatchNumber();
        int prevCount = 0;
        int matchCount = 0;
        try {
            prevCount = removePrevCount(vars, refName);
            List<String> matches = extractMatches(previousResult, vars, matchNumber);
            matchCount = saveMatches(vars, refName, matchNumber, matches);
        } catch (RuntimeException e) { // NOSONAR
            if (log.isWarnEnabled()) {
                log.warn("{}: Error while generating result. {}", getName(), e.toString()); // NOSONAR We don't want to be too verbose
            }
        } finally {
            // Remove any left-over variables
            for (int i = matchCount + 1; i <= prevCount; i++) {
                vars.remove(refName + UNDERSCORE + i);
            }
        }
    }

    private int removePrevCount(JMeterVariables vars, String refName) {
        int prevCount = 0;
        String prevString = vars.get(refName + REF_MATCH_NR);
        if (prevString != null) {
            // ensure old value is not left defined
            vars.remove(refName + REF_MATCH_NR);
            try {
                prevCount = Integer.parseInt(prevString);
            } catch (NumberFormatException nfe) {
                if (log.isWarnEnabled()) {
                    log.warn("{}: Could not parse number: '{}'.", getName(), prevString);
                }
            }
        }
        return prevCount;
    }

    private List<String> extractMatches(SampleResult previousResult, JMeterVariables vars, int matchNumber) {
        if (isScopeVariable()) {
            String inputString = vars.get(getVariableName());
            if (inputString == null && log.isWarnEnabled()) {
                log.warn("No variable '{}' found to process by Boundary Extractor '{}', skipping processing",
                        getVariableName(), getName());
            }
            return extract(getLeftBoundary(), getRightBoundary(), matchNumber, inputString);
        } else {
            Stream<String> inputs = getSampleList(previousResult).stream().map(this::getInputString);
            return extract(getLeftBoundary(), getRightBoundary(), matchNumber, inputs);
        }
    }

    /**
     * @param vars {@link JMeterVariables}
     * @param refName Var name
     * @param matchNumber number of matches
     * @param matches List of String
     * @return 0 if there is only one match, else the number of matches, this is used to remove
     */
    private int saveMatches(JMeterVariables vars, String refName, int matchNumber, List<String> matches) {
        int matchCount = 0;
        if (matchNumber == 0) {
            saveRandomMatch(vars, refName, matches);
        } else if (matchNumber > 0) {
            saveOneMatch(vars, refName, matches);
        } else {
            matchCount = matches.size();
            saveAllMatches(vars, refName, matches);
        }
        return matchCount;
    }

    private void saveRandomMatch(JMeterVariables vars, String refName, List<String> matches) {
        String match = matches.get(JMeterUtils.getRandomInt(matches.size()));
        if (match != null) {
            vars.put(refName, match);
        }
    }

    private void saveOneMatch(JMeterVariables vars, String refName, List<String> matches) {
        if (matches.size() == 1) { // if not then invalid matchNum was likely supplied
            String match = matches.get(0);
            if (match != null) {
                vars.put(refName, match);
            }
        }
    }

    private void saveAllMatches(JMeterVariables vars, String refName, List<String> matches) {
        vars.put(refName + REF_MATCH_NR, Integer.toString(matches.size()));
        for (int i = 0; i < matches.size(); i++) {
            String match = matches.get(i);
            if (match != null) {
                int varNum = i + 1;
                vars.put(refName + UNDERSCORE + varNum, match);
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

    private List<String> extract(
            String leftBoundary, String rightBoundary, int matchNumber, Stream<String> previousResults) {
        boolean allItems = matchNumber <= 0;
        return previousResults
                .flatMap(input -> extractAll(leftBoundary, rightBoundary, input).stream())
                .skip(allItems ? 0L : matchNumber - 1)
                .limit(allItems ? Long.MAX_VALUE : 1L)
                .collect(Collectors.toList());
    }

    /**
     * Extracts text fragments, that are between the boundaries, into {@code result}.
     * The number of extracted fragments can be controlled by {@code matchNumber}
     *
     * @param leftBoundary  fragment representing the left boundary of the searched text
     * @param rightBoundary fragment representing the right boundary of the searched text
     * @param matchNumber   if {@code <=0}, all found matches will be returned, else only
     *                      up to {@code matchNumber} matches
     * @param inputString   text in which to look for the fragments
     * @return list where the found text fragments will be placed
     */
    private List<String> extract(String leftBoundary, String rightBoundary, int matchNumber, String inputString) {
        if (StringUtils.isBlank(inputString)) {
            return Collections.emptyList();
        }
        Objects.requireNonNull(leftBoundary);
        Objects.requireNonNull(rightBoundary);

        List<String> matches = new ArrayList<>();
        int leftBoundaryLen = leftBoundary.length();
        boolean collectAll = matchNumber <= 0;
        int found = 0;

        for (int startIndex = 0;
             (startIndex = inputString.indexOf(leftBoundary, startIndex)) != -1;
             startIndex += leftBoundaryLen) {
            int endIndex = inputString.indexOf(rightBoundary, startIndex + leftBoundaryLen);
            if (endIndex >= 0) {
                found++;
                if (collectAll) {
                    matches.add(inputString.substring(startIndex + leftBoundaryLen, endIndex));
                } else if (found == matchNumber) {
                    return Collections.singletonList(inputString.substring(startIndex + leftBoundaryLen, endIndex));
                }
            } else {
                break;
            }
        }

        return matches;
    }

    public List<String> extractAll(
            String leftBoundary, String rightBoundary, String textToParse) {
        return extract(leftBoundary, rightBoundary, -1, textToParse);
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
     *
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
        return USE_HDRS.equalsIgnoreCase(getPropertyAsString(MATCH_AGAINST));
    }

    public boolean useRequestHeaders() {
        return USE_REQUEST_HDRS.equalsIgnoreCase(getPropertyAsString(MATCH_AGAINST));
    }

    public boolean useBody() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return prop.length() == 0 || USE_BODY.equalsIgnoreCase(prop);
    }

    public boolean useUnescapedBody() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return USE_BODY_UNESCAPED.equalsIgnoreCase(prop);
    }

    public boolean useBodyAsDocument() {
        String prop = getPropertyAsString(MATCH_AGAINST);
        return USE_BODY_AS_DOCUMENT.equalsIgnoreCase(prop);
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
        setProperty(MATCH_AGAINST, actionCommand);
    }
}
