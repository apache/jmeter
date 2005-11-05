//$Header$
/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.jmeter.report;

import javax.swing.JComponent;


public interface ReportChart {
    /**
     * The idea is a report table will be passed to a ReportChart
     * TestElement. The ReportChart is responsible for choosing which
     * columns/rows it needs and generate a chart for it. The chart
     * object is a JComponent.
     * @param element
     * @return
     */
	JComponent renderChart(ReportTable element);
}
