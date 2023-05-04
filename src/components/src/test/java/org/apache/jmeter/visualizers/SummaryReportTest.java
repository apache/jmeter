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

package org.apache.jmeter.visualizers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.Calculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SummaryReportTest extends JMeterTestCase {

    private SampleResult sampleResult;

    @BeforeEach
    public void setUp() {
        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        sampleResult = new SampleResult();
    }

    static class SummaryReportForTest extends SummaryReport {
        final Deque<Calculator> newRows = new ConcurrentLinkedDeque<>();

        @Override
        protected Function<String, Calculator> getCalculatorFunctionForLabel() {
            return label -> {
                Calculator newRow = new Calculator(label);
                newRows.add(newRow);
                return newRow;
            };
        }
    }

    @Test
    void add() {
        SummaryReportForTest summaryReport = new SummaryReportForTest();
        ResultCollector resultCollector = new ResultCollector();
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_PARENT, true);
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_ALL, false);
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_CHILDREN, false);
        summaryReport.configure(resultCollector);
        sampleResult.addSubResult(new SampleResult(System.currentTimeMillis(), 1000));
        sampleResult.addSubResult(new SampleResult(System.currentTimeMillis(), 1000));
        sampleResult.addSubResult(new SampleResult(System.currentTimeMillis(), 1000));
        summaryReport.add(sampleResult);
        assertEquals(1, summaryReport.newRows.size());
        summaryReport = new SummaryReportForTest();
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_PARENT, false);
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_ALL, true);
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_CHILDREN, false);
        summaryReport.configure(resultCollector);
        summaryReport.add(sampleResult);
        assertEquals(4, summaryReport.newRows.size());
        summaryReport = new SummaryReportForTest();
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_PARENT, false);
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_ALL, false);
        resultCollector.setProperty(SummaryReport.SAMPLE_SCOPE_CHILDREN, true);
        summaryReport.configure(resultCollector);
        summaryReport.add(sampleResult);
        assertEquals(3, summaryReport.newRows.size());
    }
}
