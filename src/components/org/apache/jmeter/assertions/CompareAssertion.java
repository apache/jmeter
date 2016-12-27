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

package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.StringSubstitution;
import org.apache.oro.text.regex.Util;

public class CompareAssertion extends AbstractTestElement implements Assertion, TestBean, Serializable,
        LoopIterationListener {

    private static final long serialVersionUID = 240L;

    private transient List<SampleResult> responses;

    private final transient StringSubstitution emptySub = new StringSubstitution(""); //$NON-NLS-1$

    private boolean compareContent = true;

    private long compareTime = -1;

    private Collection<SubstitutionElement> stringsToSkip;

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
        if (compareTime >= 0) {
            long prevTime = -1;
            SampleResult prevResult = null;
            boolean success = true;
            StringBuilder buf = new StringBuilder();
            for(SampleResult sResult : responses) {
                long currentTime = sResult.getTime();
                if (prevTime != -1) {
                    success = Math.abs(prevTime - currentTime) <= compareTime;
                    prevResult = sResult;
                }
                if (!success) {
                    result.setFailure(true);
                    buf.setLength(0);
                    appendResultDetails(buf, prevResult);
                    buf.append(JMeterUtils.getResString("comparison_response_time")).append(prevTime); //$NON-NLS-1$
                    result.addToBaseResult(buf.toString());
                    buf.setLength(0);
                    appendResultDetails(buf, sResult);
                    buf.append(JMeterUtils.getResString("comparison_response_time")).append(currentTime); //$NON-NLS-1$
                    result.addToSecondaryResult(buf.toString());
                   result.setFailureMessage(
                           JMeterUtils.getResString("comparison_differ_time")+ //$NON-NLS-1$
                           compareTime+
                           JMeterUtils.getResString("comparison_unit")); //$NON-NLS-1$
                    break;
                }
                prevResult = sResult;
                prevTime = currentTime;
            }
        }
    }

    private void compareContent(CompareAssertionResult result) {
        if (compareContent) {
            String prevContent = null;
            SampleResult prevResult = null;
            boolean success = true;
            StringBuilder buf = new StringBuilder();
            for (SampleResult sResult : responses) {
                String currentContent = sResult.getResponseDataAsString();
                currentContent = filterString(currentContent);
                if (prevContent != null) {
                    success = prevContent.equals(currentContent);
                }
                if (!success) {
                    result.setFailure(true);
                    buf.setLength(0);
                    appendResultDetails(buf, prevResult);
                    buf.append(prevContent);
                    result.addToBaseResult(buf.toString());
                    buf.setLength(0);                    
                    appendResultDetails(buf, sResult);
                    buf.append(currentContent);
                    result.addToSecondaryResult(buf.toString());
                    result.setFailureMessage(JMeterUtils.getResString("comparison_differ_content")); //$NON-NLS-1$
                    break;
                }
                prevResult = sResult;
                prevContent = currentContent;
            }
        }
    }

    private void appendResultDetails(StringBuilder buf, SampleResult result) {
        final String samplerData = result.getSamplerData();
        if (samplerData != null){
            buf.append(samplerData.trim());
        }
        buf.append("\n"); //$NON-NLS-1$
        final String requestHeaders = result.getRequestHeaders();
        if (requestHeaders != null){
            buf.append(requestHeaders);
        }
        buf.append("\n\n"); //$NON-NLS-1$
    }

    private String filterString(final String content) {
        if (stringsToSkip == null || stringsToSkip.isEmpty()) {
            return content;
        } else {
            String result = content;
            for (SubstitutionElement regex : stringsToSkip) {
                emptySub.setSubstitution(regex.getSubstitute());
                result = Util.substitute(JMeterUtils.getMatcher(), JMeterUtils.getPatternCache().getPattern(regex.getRegex()),
                        emptySub, result, Util.SUBSTITUTE_ALL);
            }
            return result;
        }
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        responses = new LinkedList<>();
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
