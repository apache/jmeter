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

/*
 * Created on May 4, 2003
 */
package org.apache.jmeter.engine.util;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.StringUtilities;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.StringSubstitution;
import org.apache.oro.text.regex.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms strings into variable references (in spite of the name, which
 * suggests the opposite!)
 *
 */
public class ReplaceFunctionsWithStrings extends AbstractTransformer {
    private static final Logger log = LoggerFactory.getLogger(ReplaceFunctionsWithStrings.class);

    /**
     * Functions are wrapped in ${ and }
     */
    private static final String FUNCTION_REF_PREFIX = "${"; //$NON-NLS-1$
    /**
     * Functions are wrapped in ${ and }
     */
    private static final String FUNCTION_REF_SUFFIX = "}"; //$NON-NLS-1$

    private final boolean regexMatch;// Should we match using regexes?

    public ReplaceFunctionsWithStrings(CompoundVariable masterFunction, Map<String, String> variables) {
        this(masterFunction, variables, false);
    }

    public ReplaceFunctionsWithStrings(CompoundVariable masterFunction, Map<String, String> variables, boolean regexMatch) {
        super();
        setMasterFunction(masterFunction);
        setVariables(variables);
        this.regexMatch = regexMatch;
    }

    @Override
    public JMeterProperty transformValue(JMeterProperty prop) throws InvalidVariableException {
        PatternMatcher pm = JMeterUtils.getMatcher();
        PatternCompiler compiler = new Perl5Compiler();
        String input = prop.getStringValue();
        if(input == null) {
            return prop;
        }
        for(Entry<String, String> entry : getVariables().entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if (regexMatch) {
                try {
                    Pattern pattern = compiler.compile(constructPattern(value));
                    input = Util.substitute(pm, pattern,
                            new StringSubstitution(FUNCTION_REF_PREFIX + key + FUNCTION_REF_SUFFIX),
                            input, Util.SUBSTITUTE_ALL);
                } catch (MalformedPatternException e) {
                    log.warn("Malformed pattern: {}", value);
                }
            } else {
                input = StringUtilities.substitute(input, value, FUNCTION_REF_PREFIX + key + FUNCTION_REF_SUFFIX);
            }
        }
        return new StringProperty(prop.getName(), input);
    }

    /**
     * Normal regexes will be surrounded by boundary character matches to make life easier for users.
     * If a user doesn't want that behaviour, he can prevent the modification by giving a regex, that
     * starts and ends with a parenthesis.
     *
     * @param value given by user
     * @return regex surrounded by boundary character matches, if value is not included in parens
     */
    private String constructPattern(String value) {
        if (value.startsWith("(") && value.endsWith(")")) {
            return value;
        }
        return "\\b(" + value + ")\\b";
    }

}
