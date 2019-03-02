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

package org.apache.jmeter.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Implements regular expression parsing of sample results and variables
 * @since 1.X
 */

// @see TestRegexFunction for unit tests

public class RegexFunction extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(RegexFunction.class);

    public static final String ALL = "ALL"; //$NON-NLS-1$

    public static final String RAND = "RAND"; //$NON-NLS-1$

    public static final String KEY = "__regexFunction"; //$NON-NLS-1$

    private Object[] values;// Parameters are stored here

    private static final List<String> desc = new LinkedList<>();

    private static final String TEMPLATE_PATTERN = "\\$(\\d+)\\$";  //$NON-NLS-1$
    /** initialised to the regex \$(\d+)\$ */
    private final Pattern templatePattern;

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 2;

    private static final int MAX_PARAMETER_COUNT = 7;
    static {
        desc.add(JMeterUtils.getResString("regexfunc_param_1"));// regex //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("regexfunc_param_2"));// template //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("regexfunc_param_3"));// which match //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("regexfunc_param_4"));// between text //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("regexfunc_param_5"));// default text //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); // output variable name //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("regexfunc_param_7"));// input variable //$NON-NLS-1$
    }

    public RegexFunction() {
        templatePattern = JMeterUtils.getPatternCache().getPattern(TEMPLATE_PATTERN,
                Perl5Compiler.READ_ONLY_MASK);
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String valueIndex = ""; //$NON-NLS-1$
        String defaultValue = ""; //$NON-NLS-1$
        String between = ""; //$NON-NLS-1$
        String name = ""; //$NON-NLS-1$
        String inputVariable = ""; //$NON-NLS-1$
        Pattern searchPattern;
        Object[] tmplt;
        try {
            searchPattern = JMeterUtils.getPatternCache().getPattern(((CompoundVariable) values[0]).execute(),
                    Perl5Compiler.READ_ONLY_MASK);
            tmplt = generateTemplate(((CompoundVariable) values[1]).execute());

            if (values.length > 2) {
                valueIndex = ((CompoundVariable) values[2]).execute();
            }
            if (valueIndex.length() == 0) {
                valueIndex = "1"; //$NON-NLS-1$
            }

            if (values.length > 3) {
                between = ((CompoundVariable) values[3]).execute();
            }

            if (values.length > 4) {
                String dv = ((CompoundVariable) values[4]).execute();
                if (dv.length() != 0) {
                    defaultValue = dv;
                }
            }

            if (values.length > 5) {
                name = ((CompoundVariable) values[5]).execute();
            }

            if (values.length > 6) {
                inputVariable = ((CompoundVariable) values[6]).execute();
            }
        } catch (MalformedCachePatternException e) {
            log.error("Malformed cache pattern:{}", values[0], e);
            throw new InvalidVariableException("Malformed cache pattern:"+values[0], e);
        }

        // Relatively expensive operation, so do it once
        JMeterVariables vars = getVariables();

        if (vars == null){// Can happen if called during test closedown
            return defaultValue;
        }

        if (name.length() > 0) {
            vars.put(name, defaultValue);
        }

        String textToMatch=null;

        if (inputVariable.length() > 0){
            textToMatch=vars.get(inputVariable);
        } else if (previousResult != null){
            textToMatch = previousResult.getResponseDataAsString();
        }

        if (textToMatch == null || textToMatch.length() == 0) {
            return defaultValue;
        }

        List<MatchResult> collectAllMatches = new ArrayList<>();
        try {
            PatternMatcher matcher = JMeterUtils.getMatcher();
            PatternMatcherInput input = new PatternMatcherInput(textToMatch);
            while (matcher.contains(input, searchPattern)) {
                MatchResult match = matcher.getMatch();
                if(match != null) {
                    collectAllMatches.add(match);
                }
            }
        } finally {
            if (name.length() > 0){
                vars.put(name + "_matchNr", Integer.toString(collectAllMatches.size())); //$NON-NLS-1$
            }
        }

        if (collectAllMatches.isEmpty()) {
            return defaultValue;
        }

        if (valueIndex.equals(ALL)) {
            StringBuilder value = new StringBuilder();
            Iterator<MatchResult> it = collectAllMatches.iterator();
            boolean first = true;
            while (it.hasNext()) {
                if (!first) {
                    value.append(between);
                } else {
                    first = false;
                }
                value.append(generateResult(it.next(), name, tmplt, vars));
            }
            return value.toString();
        } else if (valueIndex.equals(RAND)) {
            MatchResult result = collectAllMatches.get(ThreadLocalRandom.current().nextInt(collectAllMatches.size()));
            return generateResult(result, name, tmplt, vars);
        } else {
            try {
                int index = Integer.parseInt(valueIndex) - 1;
                if(index >= collectAllMatches.size()) {
                    return defaultValue;
                }
                MatchResult result = collectAllMatches.get(index);
                return generateResult(result, name, tmplt, vars);
            } catch (NumberFormatException e) {
                float ratio = Float.parseFloat(valueIndex);
                MatchResult result = collectAllMatches
                        .get((int) (collectAllMatches.size() * ratio + .5) - 1);
                return generateResult(result, name, tmplt, vars);
            }
        }

    }

    private void saveGroups(MatchResult result, String namep, JMeterVariables vars) {
        for (int x = 0; x < result.groups(); x++) {
            vars.put(namep + "_g" + x, result.group(x)); //$NON-NLS-1$
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

    private String generateResult(MatchResult match, String namep, Object[] template, JMeterVariables vars) {
        saveGroups(match, namep, vars);
        StringBuilder result = new StringBuilder();
        for (Object t : template) {
            if (t instanceof String) {
                result.append(t);
            } else {
                result.append(match.group(((Integer) t).intValue()));
            }
        }
        if (namep.length() > 0){
            vars.put(namep, result.toString());
        }
        return result.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray();
    }

    private Object[] generateTemplate(String rawTemplate) {
        List<String> pieces = new ArrayList<>();
        // String or Integer
        List<Object> combined = new LinkedList<>();
        PatternMatcher matcher = JMeterUtils.getMatcher();
        Util.split(pieces, matcher, templatePattern, rawTemplate);
        PatternMatcherInput input = new PatternMatcherInput(rawTemplate);
        boolean startsWith = isFirstElementGroup(rawTemplate);
        if (startsWith) {
            pieces.remove(0);// Remove initial empty entry
        }
        Iterator<String> iter = pieces.iterator();
        while (iter.hasNext()) {
            boolean matchExists = matcher.contains(input, templatePattern);
            if (startsWith) {
                if (matchExists) {
                    combined.add(Integer.valueOf(matcher.getMatch().group(1)));
                }
                combined.add(iter.next());
            } else {
                combined.add(iter.next());
                if (matchExists) {
                    combined.add(Integer.valueOf(matcher.getMatch().group(1)));
                }
            }
        }
        if (matcher.contains(input, templatePattern)) {
            combined.add(Integer.valueOf(matcher.getMatch().group(1)));
        }
        return combined.toArray();
    }

    private boolean isFirstElementGroup(String rawData) {
        Pattern pattern = JMeterUtils.getPatternCache().getPattern("^\\$\\d+\\$",  //$NON-NLS-1$
                Perl5Compiler.READ_ONLY_MASK);
        return JMeterUtils.getMatcher().contains(rawData, pattern);
    }

}
