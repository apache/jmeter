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

package org.apache.jmeter.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.schema.PropertiesAccessor;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.Document;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexExtractor extends AbstractScopedTestElement implements PostProcessor, Serializable {

    private static final long serialVersionUID = 242L;

    private static final Logger log = LoggerFactory.getLogger(RegexExtractor.class);

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

    private static final String REF_MATCH_NR = "_matchNr"; // $NON-NLS-1$

    private static final String UNDERSCORE = "_";  // $NON-NLS-1$

    private static final boolean USE_JAVA_REGEX = !JMeterUtils.getPropDefault(
            "jmeter.regex.engine", "oro").equalsIgnoreCase("oro");

    private transient List<Object> template;

    @Override
    public RegexExtractorSchema getSchema() {
        return RegexExtractorSchema.INSTANCE;
    }

    @Override
    public PropertiesAccessor<? extends RegexExtractor, ? extends RegexExtractorSchema> getProps() {
        return new PropertiesAccessor<>(this, getSchema());
    }

    /**
     * Parses the response data using regular expressions and saving the results
     * into variables for use later in the test.
     *
     * @see org.apache.jmeter.processor.PostProcessor#process()
     */
    @Override
    public void process() {
        initTemplate();
        JMeterContext context = getThreadContext();
        SampleResult previousResult = context.getPreviousResult();
        if (previousResult == null) {
            return;
        }
        log.debug("RegexExtractor processing result");

        // Fetch some variables
        JMeterVariables vars = context.getVariables();
        String refName = getRefName();
        int matchNumber = getMatchNumber();

        final String defaultValue = getDefaultValue();
        if (!defaultValue.isEmpty() || isEmptyDefaultValue()) {// Only replace default if it is provided or empty default value is explicitly requested
            vars.put(refName, defaultValue);
        }

        if (USE_JAVA_REGEX) {
            extractWithJavaRegex(previousResult, vars, refName, matchNumber);
        } else {
            extractWithOroRegex(previousResult, vars, refName, matchNumber);
        }
    }

    private void extractWithOroRegex(SampleResult previousResult, JMeterVariables vars, String refName, int matchNumber) {
        Perl5Matcher matcher = JMeterUtils.getMatcher();
        String regex = getRegex();
        Pattern pattern = null;
        try {
            pattern = JMeterUtils.getPatternCache().getPattern(regex, Perl5Compiler.READ_ONLY_MASK);
            List<MatchResult> matches = processMatches(pattern, regex, previousResult, matchNumber, vars);
            int prevCount = 0;
            String prevString = vars.get(refName + REF_MATCH_NR);
            if (prevString != null) {
                vars.remove(refName + REF_MATCH_NR);// ensure old value is not left defined
                try {
                    prevCount = Integer.parseInt(prevString);
                } catch (NumberFormatException nfe) {
                    log.warn("Could not parse number: '{}'", prevString);
                }
            }
            int matchCount=0;// Number of refName_n variable sets to keep
            try {
                MatchResult match;
                if (matchNumber >= 0) {// Original match behaviour
                    match = getCorrectMatch(matches, matchNumber);
                    if (match != null) {
                        vars.put(refName, generateResult(match));
                        saveGroups(vars, refName, match);
                    } else {
                        // refname has already been set to the default (if present)
                        removeGroups(vars, refName);
                    }
                } else // < 0 means we save all the matches
                {
                    removeGroups(vars, refName); // remove any single matches
                    matchCount = matches.size();
                    vars.put(refName + REF_MATCH_NR, Integer.toString(matchCount));// Save the count
                    for (int i = 1; i <= matchCount; i++) {
                        match = getCorrectMatch(matches, i);
                        if (match != null) {
                            final String refName_n = refName + UNDERSCORE + i;
                            vars.put(refName_n, generateResult(match));
                            saveGroups(vars, refName_n, match);
                        }
                    }
                }
                // Remove any left-over variables
                for (int i = matchCount + 1; i <= prevCount; i++) {
                    final String refName_n = refName + UNDERSCORE + i;
                    vars.remove(refName_n);
                    removeGroups(vars, refName_n);
                }
            } catch (RuntimeException e) {
                log.warn("Error while generating result");
            }
        } catch (MalformedCachePatternException e) {
            log.error("Error in pattern: '{}'", regex);
        } finally {
            JMeterUtils.clearMatcherMemory(matcher, pattern);
        }
    }

    private void extractWithJavaRegex(SampleResult previousResult, JMeterVariables vars, String refName, int matchNumber) {
        String regex = getRegex();
        java.util.regex.Pattern pattern = null;
        try {
            pattern = JMeterUtils.compilePattern(regex);
            List<java.util.regex.MatchResult> matches = processMatches(pattern, previousResult, matchNumber, vars);
            int prevCount = 0;
            String prevString = vars.get(refName + REF_MATCH_NR);
            if (prevString != null) {
                vars.remove(refName + REF_MATCH_NR);// ensure old value is not left defined
                try {
                    prevCount = Integer.parseInt(prevString);
                } catch (NumberFormatException nfe) {
                    log.warn("Could not parse number: '{}'", prevString);
                }
            }
            int matchCount=0;// Number of refName_n variable sets to keep
            try {
                java.util.regex.MatchResult match;
                if (matchNumber >= 0) {// Original match behaviour
                    match = getCorrectMatchJavaRegex(matches, matchNumber);
                    if (match != null) {
                        vars.put(refName, generateResult(match));
                        saveGroups(vars, refName, match);
                    } else {
                        // refname has already been set to the default (if present)
                        removeGroups(vars, refName);
                    }
                } else // < 0 means we save all the matches
                {
                    removeGroups(vars, refName); // remove any single matches
                    matchCount = matches.size();
                    vars.put(refName + REF_MATCH_NR, Integer.toString(matchCount));// Save the count
                    for (int i = 1; i <= matchCount; i++) {
                        match = getCorrectMatchJavaRegex(matches, i);
                        if (match != null) {
                            final String refName_n = refName + UNDERSCORE + i;
                            vars.put(refName_n, generateResult(match));
                            saveGroups(vars, refName_n, match);
                        }
                    }
                }
                // Remove any left-over variables
                for (int i = matchCount + 1; i <= prevCount; i++) {
                    final String refName_n = refName + UNDERSCORE + i;
                    vars.remove(refName_n);
                    removeGroups(vars, refName_n);
                }
            } catch (RuntimeException e) {
                log.warn("Error while generating result");
            }
        } catch (PatternSyntaxException e) {
            log.error("Error in pattern: '{}'", regex);
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

    private List<MatchResult> processMatches(Pattern pattern, String regex, SampleResult result, int matchNumber, JMeterVariables vars) {
        log.debug("Regex = '{}'", regex);

        Perl5Matcher matcher = JMeterUtils.getMatcher();
        List<MatchResult> matches = new ArrayList<>();
        int found = 0;

        if (isScopeVariable()){
            String inputString=vars.get(getVariableName());
            if(inputString == null) {
                if (log.isWarnEnabled()) {
                    log.warn("No variable '{}' found to process by RegexExtractor '{}', skipping processing",
                            getVariableName(), getName());
                }
                return Collections.emptyList();
            }
            matchStrings(matchNumber, matcher, pattern, matches, found,
                    inputString);
        } else {
            List<SampleResult> sampleList = getSampleList(result);
            for (SampleResult sr : sampleList) {
                String inputString = getInputString(sr);
                found = matchStrings(matchNumber, matcher, pattern, matches, found,
                        inputString);
                if (matchNumber > 0 && found == matchNumber){// no need to process further
                    break;
                }
            }
        }
        return Collections.unmodifiableList(matches);
    }

    private List<java.util.regex.MatchResult> processMatches(
            java.util.regex.Pattern pattern, SampleResult result, int matchNumber, JMeterVariables vars) {
        log.debug("Regex = '{}'", pattern.pattern());

        List<java.util.regex.MatchResult> matches = new ArrayList<>();
        int found = 0;

        if (isScopeVariable()) {
            String inputString=vars.get(getVariableName());
            if(inputString == null) {
                if (log.isWarnEnabled()) {
                    log.warn("No variable '{}' found to process by RegexExtractor '{}', skipping processing",
                            getVariableName(), getName());
                }
                return Collections.emptyList();
            }
            matchStrings(matchNumber, pattern, matches, found, inputString);
        } else {
            List<SampleResult> sampleList = getSampleList(result);
            for (SampleResult sr : sampleList) {
                String inputString = getInputString(sr);
                found = matchStrings(matchNumber, pattern, matches, found, inputString);
                if (matchNumber > 0 && found == matchNumber) {// no need to process further
                    break;
                }
            }
        }
        return Collections.unmodifiableList(matches);
    }

    private static int matchStrings(int matchNumber, Perl5Matcher matcher,
            Pattern pattern, List<? super MatchResult> matches, int found,
            String inputString) {
        PatternMatcherInput input = new PatternMatcherInput(inputString);
        while (matchNumber <=0 || found != matchNumber) {
            if (matcher.contains(input, pattern)) {
                log.debug("RegexExtractor: Match found!");
                matches.add(matcher.getMatch());
                found++;
            } else {
                break;
            }
        }
        return found;
    }

    private static int matchStrings(int matchNumber, java.util.regex.Pattern pattern,
            List<? super java.util.regex.MatchResult> matches, int found,
            String inputString) {
        Matcher matcher = pattern.matcher(inputString);
        while (matchNumber <=0 || found != matchNumber) {
            if (matcher.find()) {
                log.debug("RegexExtractor: Match found!");
                matches.add(matcher.toMatchResult());
                found++;
            } else {
                break;
            }
        }
        return found;
    }

    /**
     * Creates the variables:<br/>
     * basename_gn, where n=0...# of groups<br/>
     * basename_g = number of groups (apart from g0)
     */
    private static void saveGroups(JMeterVariables vars, String basename, MatchResult match) {
        StringBuilder buf = new StringBuilder();
        buf.append(basename);
        buf.append("_g"); // $NON-NLS-1$
        int pfxlen=buf.length();
        String prevString=vars.get(buf.toString());
        int previous=0;
        if (prevString!=null){
            try {
                previous=Integer.parseInt(prevString);
            } catch (NumberFormatException nfe) {
                log.warn("Could not parse number: '{}'.", prevString);
            }
        }
        //Note: match.groups() includes group 0
        final int groups = match.groups();
        for (int x = 0; x < groups; x++) {
            buf.append(x);
            vars.put(buf.toString(), match.group(x));
            buf.setLength(pfxlen);
        }
        vars.put(buf.toString(), Integer.toString(groups-1));
        for (int i = groups; i <= previous; i++){
            buf.append(i);
            vars.remove(buf.toString());// remove the remaining _gn vars
            buf.setLength(pfxlen);
        }
    }

    private static void saveGroups(JMeterVariables vars, String basename, java.util.regex.MatchResult match) {
        StringBuilder buf = new StringBuilder();
        buf.append(basename);
        buf.append("_g"); // $NON-NLS-1$
        int pfxlen=buf.length();
        String prevString=vars.get(buf.toString());
        int previous=0;
        if (prevString!=null){
            try {
                previous=Integer.parseInt(prevString);
            } catch (NumberFormatException nfe) {
                log.warn("Could not parse number: '{}'.", prevString);
            }
        }
        //Note: match.groups() includes group 0, groupCount() not
        final int groups = match.groupCount() + 1;
        for (int x = 0; x < groups; x++) {
            buf.append(x);
            vars.put(buf.toString(), match.group(x));
            buf.setLength(pfxlen);
        }
        vars.put(buf.toString(), Integer.toString(groups-1));
        for (int i = groups; i <= previous; i++){
            buf.append(i);
            vars.remove(buf.toString());// remove the remaining _gn vars
            buf.setLength(pfxlen);
        }
    }

    /**
     * Removes the variables:<br/>
     * basename_gn, where n=0...# of groups<br/>
     * basename_g = number of groups (apart from g0)
     */
    private static void removeGroups(JMeterVariables vars, String basename) {
        StringBuilder buf = new StringBuilder();
        buf.append(basename);
        buf.append("_g"); // $NON-NLS-1$
        int pfxlen=buf.length();
        // How many groups are there?
        int groups;
        try {
            groups=Integer.parseInt(vars.get(buf.toString()));
        } catch (NumberFormatException e) {
            groups=0;
        }
        vars.remove(buf.toString());// Remove the group count
        for (int i = 0; i <= groups; i++) {
            buf.append(i);
            vars.remove(buf.toString());// remove the g0,g1...gn vars
            buf.setLength(pfxlen);
        }
    }

    private String generateResult(MatchResult match) {
        StringBuilder result = new StringBuilder();
        for (Object obj : template) {
            if(log.isDebugEnabled()) {
                log.debug("RegexExtractor: Template piece {} ({})", obj, obj.getClass());
            }
            if (obj instanceof Integer) {
                result.append(match.group((Integer) obj));
            } else {
                result.append(obj);
            }
        }
        log.debug("Regex Extractor result = '{}'", result);
        return result.toString();
    }

    private String generateResult(java.util.regex.MatchResult match) {
        StringBuilder result = new StringBuilder();
        for (Object obj : template) {
            if(log.isDebugEnabled()) {
                log.debug("RegexExtractor: Template piece {} ({})", obj, obj.getClass());
            }
            if (obj instanceof Integer) {
                result.append(match.group((Integer) obj));
            } else {
                result.append(obj);
            }
        }
        log.debug("Regex Extractor result = '{}'", result);
        return result.toString();
    }

    private void initTemplate() {
        if (template != null) {
            return;
        }
        // Contains Strings and Integers
        List<Object> combined = new ArrayList<>();
        String rawTemplate = getTemplate();
        PatternMatcher matcher = JMeterUtils.getMatcher();
        Pattern templatePattern = JMeterUtils.getPatternCache().getPattern("\\$(\\d+)\\$"  // $NON-NLS-1$
                , Perl5Compiler.READ_ONLY_MASK
                | Perl5Compiler.SINGLELINE_MASK);
        if (log.isDebugEnabled()) {
            log.debug("Pattern = '{}', template = '{}'", templatePattern.getPattern(), rawTemplate);
        }
        int beginOffset = 0;
        MatchResult currentResult;
        PatternMatcherInput pinput = new PatternMatcherInput(rawTemplate);
        while(matcher.contains(pinput, templatePattern)) {
            currentResult = matcher.getMatch();
            final int beginMatch = currentResult.beginOffset(0);
            if (beginMatch > beginOffset) { // string is not empty
                combined.add(rawTemplate.substring(beginOffset, beginMatch));
            }
            combined.add(Integer.valueOf(currentResult.group(1)));// add match as Integer
            beginOffset = currentResult.endOffset(0);
        }

        if (beginOffset < rawTemplate.length()) { // trailing string is not empty
            combined.add(rawTemplate.substring(beginOffset));
        }
        if (log.isDebugEnabled()) {
            log.debug("Template item count: {}", combined.size());
            int i = 0;
            for (Object o : combined) {
                log.debug("Template item-{}: {} '{}'", i++, o.getClass(), o);
            }
        }
        template = combined;
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
    private static MatchResult getCorrectMatch(List<? extends MatchResult> matches, int entry) {
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

    private static java.util.regex.MatchResult getCorrectMatchJavaRegex(List<? extends java.util.regex.MatchResult> matches, int entry) {
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

    /**
     * Set the regex to be used
     * @param regex The string representation of the regex
     */
    public void setRegex(String regex) {
        set(getSchema().getRegularExpression(), regex);
    }

    /**
     * Get the regex which is to be used
     * @return string representing the regex
     */
    public String getRegex() {
        return get(getSchema().getRegularExpression());
    }

    /**
     * Set the prefix name of the variable to be used to store the regex matches
     * @param refName prefix of the variables to be used
     */
    public void setRefName(String refName) {
        set(getSchema().getReferenceName(), refName);
    }

    /**
     * Get the prefix name of the variable to be used to store the regex matches
     * @return The prefix of the variables to be used
     */
    public String getRefName() {
        return get(getSchema().getReferenceName());
    }

    /**
     * Set which Match to use. This can be any positive number, indicating the
     * exact match to use, or <code>0</code>, which is interpreted as meaning
     * random.
     *
     * @param matchNumber
     *            The number of the match to be used, or <code>0</code> if a
     *            random match should be used.
     */
    public void setMatchNumber(int matchNumber) {
        set(getSchema().getMatchNumber(), matchNumber);
    }

    public void setMatchNumber(String matchNumber) {
        set(getSchema().getMatchNumber(), matchNumber);
    }

    public int getMatchNumber() {
        return get(getSchema().getMatchNumber());
    }

    public String getMatchNumberAsString() {
        return getString(getSchema().getMatchNumber());
    }

    /**
     * Sets the value of the variable if no matches are found
     *
     * @param defaultValue The default value for the variable
     */
    public void setDefaultValue(String defaultValue) {
        set(getSchema().getDefault(), defaultValue);
    }

    /**
     * Set default value to "" value when if it's empty
     *
     * @param defaultEmptyValue The default value for the variable
     */
    public void setDefaultEmptyValue(boolean defaultEmptyValue) {
        set(getSchema().getDefaultIsEmpty(), defaultEmptyValue);
    }

    /**
     * Get the default value for the variable, which should be used, if no
     * matches are found
     *
     * @return The default value for the variable
     */
    public String getDefaultValue() {
        return get(getSchema().getDefault());
    }

    /**
     * Do we set default value to "" value when if it's empty
     * @return true if we should set default value to "" if variable cannot be extracted
     */
    public boolean isEmptyDefaultValue() {
        return get(getSchema().getDefaultIsEmpty());
    }

    public void setTemplate(String template) {
        set(getSchema().getTemplate(), template);
    }

    public String getTemplate() {
        return get(getSchema().getTemplate());
    }

    private String getMatchTarget() {
        return get(getSchema().getMatchTarget());
    }

    public boolean useHeaders() {
        return USE_HDRS.equalsIgnoreCase(getMatchTarget());
    }

    public boolean useRequestHeaders() {
        return USE_REQUEST_HDRS.equalsIgnoreCase(getMatchTarget());
    }

    // Allow for property not yet being set (probably only applies to Test cases)
    public boolean useBody() {
        String prop = getMatchTarget();
        return prop.isEmpty() || USE_BODY.equalsIgnoreCase(prop);// $NON-NLS-1$
    }

    public boolean useUnescapedBody() {
        String prop = getMatchTarget();
        return USE_BODY_UNESCAPED.equalsIgnoreCase(prop);// $NON-NLS-1$
    }

    public boolean useBodyAsDocument() {
        String prop = getMatchTarget();
        return USE_BODY_AS_DOCUMENT.equalsIgnoreCase(prop);// $NON-NLS-1$
    }

    public boolean useUrl() {
        String prop = getMatchTarget();
        return USE_URL.equalsIgnoreCase(prop);
    }

    public boolean useCode() {
        String prop = getMatchTarget();
        return USE_CODE.equalsIgnoreCase(prop);
    }

    public boolean useMessage() {
        String prop = getMatchTarget();
        return USE_MESSAGE.equalsIgnoreCase(prop);
    }

    public void setUseField(String actionCommand) {
        set(getSchema().getMatchTarget(), actionCommand);
    }
}
