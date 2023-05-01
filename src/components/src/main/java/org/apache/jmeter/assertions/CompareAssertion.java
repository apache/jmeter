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

package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.StringSubstitution;
import org.apache.oro.text.regex.Util;

@TestElementMetadata(labelResource = "displayName")
public class CompareAssertion extends AbstractTestElement implements Assertion, TestBean, Serializable,
        LoopIterationListener {

    private static final long serialVersionUID = 240L;

    private transient List<SampleResult> responses;

    private final transient StringSubstitution emptySub = new StringSubstitution(""); //$NON-NLS-1$

    private boolean compareContent = true;

    private long compareTime = -1;

    private Collection<SubstitutionElement> stringsToSkip;

    private static final boolean USE_JAVA_REGEX = !JMeterUtils.getPropDefault(
            "jmeter.regex.engine", "oro").equalsIgnoreCase("oro");

    public CompareAssertion() {
        super();
    }

    @Override
    public AssertionResult getResult(SampleResult response) {
        responses.add(response);
        if (responses.size() > 1) {
            CompareAssertionResult result = new CompareAssertionResult(getName());
            compareContent(result);
            compareTime(result);
            return result;
        } else {
            return new AssertionResult(getName());
        }
    }

    private void compareTime(CompareAssertionResult result) {
        if (compareTime < 0) {
            return;
        }
        long prevTime = -1;
        SampleResult prevResult = null;
        for (SampleResult currentResult : responses) {
            long currentTime = currentResult.getTime();
            if (prevTime != -1) {
                boolean failure = Math.abs(prevTime - currentTime) > compareTime;
                if (failure) {
                    markTimeFailure(result, prevResult, prevTime, currentResult, currentTime);
                    return;
                }
            }
            prevResult = currentResult;
            prevTime = currentTime;
        }
    }

    private void markTimeFailure(CompareAssertionResult result, SampleResult prevResult, long prevTime,
            SampleResult currentResult, long currentTime) {
        result.setFailure(true);
        StringBuilder sb = new StringBuilder();
        appendResultDetails(sb, prevResult);
        sb.append(JMeterUtils.getResString("comparison_response_time")).append(prevTime); //$NON-NLS-1$
        result.addToBaseResult(sb.toString());
        sb.setLength(0);
        appendResultDetails(sb, currentResult);
        sb.append(JMeterUtils.getResString("comparison_response_time")).append(currentTime); //$NON-NLS-1$
        result.addToSecondaryResult(sb.toString());
        result.setFailureMessage(
                JMeterUtils.getResString("comparison_differ_time") + //$NON-NLS-1$
                        compareTime +
                        JMeterUtils.getResString("comparison_unit")); //$NON-NLS-1$
    }

    private void compareContent(CompareAssertionResult result) {
        if (!compareContent) {
            return;
        }
        String prevContent = null;
        SampleResult prevResult = null;
        for (SampleResult currentResult : responses) {
            String currentContent = currentResult.getResponseDataAsString();
            currentContent = filterString(currentContent);
            if (prevContent != null) {
                boolean failure = !prevContent.equals(currentContent);
                if (failure) {
                    markContentFailure(result, prevContent, prevResult, currentResult, currentContent);
                    return;
                }
            }
            prevResult = currentResult;
            prevContent = currentContent;
        }
    }

    private static void markContentFailure(CompareAssertionResult result, String prevContent, SampleResult prevResult,
            SampleResult currentResult, String currentContent) {
        result.setFailure(true);
        StringBuilder sb = new StringBuilder();
        appendResultDetails(sb, prevResult);
        sb.append(prevContent);
        result.addToBaseResult(sb.toString());
        sb.setLength(0);
        appendResultDetails(sb, currentResult);
        sb.append(currentContent);
        result.addToSecondaryResult(sb.toString());
        result.setFailureMessage(JMeterUtils.getResString("comparison_differ_content")); //$NON-NLS-1$
    }

    private static void appendResultDetails(StringBuilder buf, SampleResult result) {
        final String samplerData = result.getSamplerData();
        if (samplerData != null) {
            buf.append(samplerData.trim());
        }
        buf.append("\n"); //$NON-NLS-1$
        final String requestHeaders = result.getRequestHeaders();
        if (requestHeaders != null) {
            buf.append(requestHeaders);
        }
        buf.append("\n\n"); //$NON-NLS-1$
    }

    private String filterString(final String content) {
        if (stringsToSkip == null || stringsToSkip.isEmpty()) {
            return content;
        }

        if (USE_JAVA_REGEX) {
            String result = content;
            for (SubstitutionElement element: stringsToSkip) {
                result = result.replaceAll(element.getRegex(), element.getSubstitute());
            }
            return result;
        } else {
            String result = content;
            for (SubstitutionElement regex : stringsToSkip) {
                emptySub.setSubstitution(regex.getSubstitute());
                result = Util.substitute(
                        JMeterUtils.getMatcher(),
                        JMeterUtils.getPatternCache().getPattern(regex.getRegex()),
                        emptySub,
                        result,
                        Util.SUBSTITUTE_ALL);
            }
            return result;
        }
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        responses = new ArrayList<>();
    }

    /**
     * @return Returns the compareContent.
     */
    public boolean isCompareContent() {
        return compareContent;
    }

    /**
     * @param compareContent
     *            The compareContent to set.
     */
    public void setCompareContent(boolean compareContent) {
        this.compareContent = compareContent;
    }

    /**
     * @return Returns the compareTime.
     */
    public long getCompareTime() {
        return compareTime;
    }

    /**
     * @param compareTime
     *            The compareTime to set.
     */
    public void setCompareTime(long compareTime) {
        this.compareTime = compareTime;
    }

    /**
     * @return Returns the stringsToSkip.
     */
    public Collection<SubstitutionElement> getStringsToSkip() {
        return stringsToSkip;
    }

    /**
     * @param stringsToSkip
     *            The stringsToSkip to set.
     */
    public void setStringsToSkip(Collection<SubstitutionElement> stringsToSkip) {
        this.stringsToSkip = stringsToSkip;
    }

}
