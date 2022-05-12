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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.assertions.DurationAssertion;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.Calculator;
import org.apache.jmeter.util.JMeterUtils;
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

    class SummaryReportForTest extends SummaryReport {
        final Deque<Calculator> newRows = new ConcurrentLinkedDeque<>();

        @Override
        protected Function<String, Calculator> getStringCalculatorFunction() {
            return label -> {
                Calculator newRow = new Calculator(label);
                newRows.add(newRow);
                return newRow;
            };
        }
    }

    @Test
    void add() {
        JMeterUtils.setProperty("summary_report.add_sub_results", "false");
        SummaryReportForTest summaryReport = new SummaryReportForTest();
        sampleResult.addSubResult(new SampleResult(System.currentTimeMillis(), 1000));
        sampleResult.addSubResult(new SampleResult(System.currentTimeMillis(), 1000));
        sampleResult.addSubResult(new SampleResult(System.currentTimeMillis(), 1000));
        summaryReport.add(sampleResult);
        assertEquals(1, summaryReport.newRows.size());
        JMeterUtils.setProperty("summary_report.add_sub_results", "true");
        summaryReport = new SummaryReportForTest();
        summaryReport.add(sampleResult);
        assertEquals(4, summaryReport.newRows.size());
        JMeterUtils.setProperty("summary_report.sub_result_leaf_nodes_only", "true");
        summaryReport = new SummaryReportForTest();
        summaryReport.add(sampleResult);
        assertEquals(3, summaryReport.newRows.size());
    }
}
