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

package org.apache.jmeter.protocol.http.modifier;

import java.io.Serializable;
import java.util.function.Function;
import java.util.regex.Matcher;

import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

//For unit tests, @see TestURLRewritingModifier

public class URLRewritingModifier extends AbstractTestElement implements Serializable, PreProcessor {

    private static final long serialVersionUID = 233L;

    private static final String SEMI_COLON = ";"; // $NON-NLS-1$

    private transient Function<String, String> pathExtensionEqualsQuestionmarkExtractor;
    private transient Function<String, String> pathExtensionEqualsNoQuestionmarkExtractor;
    private transient Function<String, String> pathExtensionNoEqualsQuestionmarkExtractor;
    private transient Function<String, String> pathExtensionNoEqualsNoQuestionmarkExtractor;
    private transient Function<String, String> parameterExtractor;

    private static final String ARGUMENT_NAME = "argument_name"; // $NON-NLS-1$

    private static final String PATH_EXTENSION = "path_extension"; // $NON-NLS-1$

    private static final String PATH_EXTENSION_NO_EQUALS = "path_extension_no_equals"; // $NON-NLS-1$

    private static final String PATH_EXTENSION_NO_QUESTIONMARK = "path_extension_no_questionmark"; // $NON-NLS-1$

    private static final String SHOULD_CACHE = "cache_value"; // $NON-NLS-1$

    private static final String ENCODE = "encode"; // $NON-NLS-1$

    private static final boolean USE_JAVA_REGEX = !JMeterUtils.getPropDefault(
            "jmeter.regex.engine", "oro").equalsIgnoreCase("oro");

    // PreProcessors are cloned per-thread, so this will be saved per-thread
    private transient String savedValue = ""; // $NON-NLS-1$

    @Override
    public void process() {
        JMeterContext ctx = getThreadContext();
        Sampler sampler = ctx.getCurrentSampler();
        if (!(sampler instanceof HTTPSamplerBase)) {// Ignore non-HTTP samplers
            return;
        }
        SampleResult responseText = ctx.getPreviousResult();
        if (responseText == null) {
            return;
        }
        initRegex(getArgumentName());
        String text = responseText.getResponseDataAsString();
        String value;
        if (isPathExtension() && isPathExtensionNoEquals() && isPathExtensionNoQuestionmark()) {
            value = pathExtensionNoEqualsNoQuestionmarkExtractor.apply(text);
        } else if (isPathExtension() && isPathExtensionNoEquals()) { // && !isPathExtensionNoQuestionmark()
            value = pathExtensionNoEqualsQuestionmarkExtractor.apply(text);
        } else if (isPathExtension() && isPathExtensionNoQuestionmark()) { // && !isPathExtensionNoEquals()
            value = pathExtensionEqualsNoQuestionmarkExtractor.apply(text);
        } else if (isPathExtension()) { // && !isPathExtensionNoEquals() && !isPathExtensionNoQuestionmark()
            value = pathExtensionEqualsQuestionmarkExtractor.apply(text);
        } else { // if ! isPathExtension()
            value = parameterExtractor.apply(text);
        }

        // Bug 15025 - save session value across samplers
        if (shouldCache()) {
            if (value == null || value.isEmpty()) {
                value = savedValue;
            } else {
                savedValue = value;
            }
        }
        modify((HTTPSamplerBase) sampler, value);
    }

    private void modify(HTTPSamplerBase sampler, String value) {
        if (isPathExtension()) {
            String oldPath = sampler.getPath();
            int indexOfSessionId = oldPath.indexOf(SEMI_COLON + getArgumentName());
            if(oldPath.contains(SEMI_COLON + getArgumentName())) {
                int indexOfQuestionMark = oldPath.indexOf('?');
                if(indexOfQuestionMark < 0) {
                    oldPath = oldPath.substring(0, indexOfSessionId);
                } else {
                    oldPath = oldPath.substring(0, indexOfSessionId)+
                            oldPath.substring(indexOfQuestionMark);
                }
            }
            if (isPathExtensionNoEquals()) {
                sampler.setPath(oldPath + SEMI_COLON + getArgumentName() + value); // $NON-NLS-1$
            } else {
                sampler.setPath(oldPath + SEMI_COLON + getArgumentName() + "=" + value); // $NON-NLS-1$ // $NON-NLS-2$
            }
        } else {
            sampler.getArguments().removeArgument(getArgumentName());
            sampler.getArguments().addArgument(new HTTPArgument(getArgumentName(), value, !encode()));
        }
    }

    public void setArgumentName(String argName) {
        setProperty(ARGUMENT_NAME, argName);
    }

    private void initRegex(String argName) {
        String quotedArg = Perl5Compiler.quotemeta(argName);// Don't get tripped up by RE chars in the arg name
        pathExtensionEqualsQuestionmarkExtractor = generateExtractor(
                SEMI_COLON + quotedArg + "=([^\"'<>&\\s;]*)" // $NON-NLS-1$
        );
        pathExtensionEqualsNoQuestionmarkExtractor = generateExtractor(
                SEMI_COLON + quotedArg + "=([^\"'<>&\\s;?]*)" // $NON-NLS-1$
        );
        pathExtensionNoEqualsQuestionmarkExtractor = generateExtractor(
                SEMI_COLON + quotedArg + "([^\"'<>&\\s;]*)"// $NON-NLS-1$
        );
        pathExtensionNoEqualsNoQuestionmarkExtractor = generateExtractor(
                SEMI_COLON + quotedArg + "([^\"'<>&\\s;?]*)" // $NON-NLS-1$
        );

        parameterExtractor = generateFirstMatchExtractor(
                // ;sessionid=value
                "[;\\?&]" + quotedArg + "=([^\"'<>&\\s;\\\\]*)"  // $NON-NLS-1$

                        // name="sessionid" value="value"
                        +  "|\\s[Nn][Aa][Mm][Ee]\\s*=\\s*[\"']" + quotedArg
                        + "[\"']" + "[^>]*"  // $NON-NLS-1$
                        + "\\s[vV][Aa][Ll][Uu][Ee]\\s*=\\s*[\"']" // $NON-NLS-1$
                        + "([^\"']*)" + "[\"']" // $NON-NLS-1$

                        //  value="value" name="sessionid"
                        + "|\\s[vV][Aa][Ll][Uu][Ee]\\s*=\\s*[\"']" // $NON-NLS-1$
                        + "([^\"']*)" + "[\"']" + "[^>]*" // $NON-NLS-1$ // $NON-NLS-2$ // $NON-NLS-3$
                        + "\\s[Nn][Aa][Mm][Ee]\\s*=\\s*[\"']"  // $NON-NLS-1$
                        + quotedArg + "[\"']" // $NON-NLS-1$
        );
        // NOTE: the handling of simple- vs. double-quotes could be formally
        // more accurate, but I can't imagine a session id containing
        // either, so we should be OK. The whole set of expressions is a
        // quick hack anyway, so who cares.
    }

    private static Function<String, String> generateExtractor(String regex) {
        if (USE_JAVA_REGEX) {
            return generateExtractorWithJavaRegex(regex);
        }
        return generateExtractorWithOroRegex(regex);
    }

    private static Function<String, String> generateExtractorWithJavaRegex(String regex) {
        java.util.regex.Pattern pattern = JMeterUtils.compilePattern(
                regex,
                java.util.regex.Pattern.MULTILINE);
        return text -> {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return "";
        };
    }

    private static Function<String, String> generateExtractorWithOroRegex(String regex) {
        Pattern pattern = JMeterUtils.getPatternCache().getPattern(
                regex,
                Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);
        Perl5Matcher matcher = JMeterUtils.getMatcher();
        return text -> {
            if (matcher.contains(text, pattern)) {
                MatchResult result = matcher.getMatch();
                return result.group(1);
            }
            return "";
        };
    }

    private static Function<String, String> generateFirstMatchExtractor(String regex) {
        if (USE_JAVA_REGEX) {
            return generateFirstMatchExtractorWithJavaRegex(regex);
        }
        return generateFirstMatchExtractorWithOroRegex(regex);
    }

    private static Function<String, String> generateFirstMatchExtractorWithJavaRegex(String regex) {
        java.util.regex.Pattern pattern = JMeterUtils.compilePattern(
                regex,
                java.util.regex.Pattern.MULTILINE);
        return text -> {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String value = matcher.group(i);
                    if (value != null) {
                        return value;
                    }
                }
            }
            return "";
        };
    }

    private static Function<String, String> generateFirstMatchExtractorWithOroRegex(String regex) {
        Pattern pattern = JMeterUtils.getPatternCache().getPattern(
                regex,
                Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK);
        Perl5Matcher matcher = JMeterUtils.getMatcher();
        return text -> {
            if (matcher.contains(text, pattern)) {
                MatchResult result = matcher.getMatch();
                for (int i = 1; i < result.groups(); i++) {
                    String value = result.group(i);
                    if (value != null) {
                        return value;
                    }
                }
            }
            return "";
        };
    }

    public String getArgumentName() {
        return getPropertyAsString(ARGUMENT_NAME);
    }

    public void setPathExtension(boolean pathExt) {
        setProperty(new BooleanProperty(PATH_EXTENSION, pathExt));
    }

    public void setPathExtensionNoEquals(boolean pathExtNoEquals) {
        setProperty(new BooleanProperty(PATH_EXTENSION_NO_EQUALS, pathExtNoEquals));
    }

    public void setPathExtensionNoQuestionmark(boolean pathExtNoQuestionmark) {
        setProperty(new BooleanProperty(PATH_EXTENSION_NO_QUESTIONMARK, pathExtNoQuestionmark));
    }

    public void setShouldCache(boolean b) {
        setProperty(new BooleanProperty(SHOULD_CACHE, b));
    }

    public boolean isPathExtension() {
        return getPropertyAsBoolean(PATH_EXTENSION);
    }

    public boolean isPathExtensionNoEquals() {
        return getPropertyAsBoolean(PATH_EXTENSION_NO_EQUALS);
    }

    public boolean isPathExtensionNoQuestionmark() {
        return getPropertyAsBoolean(PATH_EXTENSION_NO_QUESTIONMARK);
    }

    public boolean shouldCache() {
        return getPropertyAsBoolean(SHOULD_CACHE,true);
    }

    protected Object readResolve(){
        savedValue = "";
        return this;
    }

    public boolean encode() {
        return getPropertyAsBoolean(ENCODE, false);
    }
    public void setEncode(boolean b) {
        setProperty(new BooleanProperty(ENCODE, b));
    }

}
