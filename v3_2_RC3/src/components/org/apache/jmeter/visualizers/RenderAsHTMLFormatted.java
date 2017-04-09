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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.jsoup.Jsoup;

public class RenderAsHTMLFormatted extends SamplerResultTab implements ResultRenderer {

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        showHTMLFormattedResponse(response);
    }

    private void showHTMLFormattedResponse(String response) {
        results.setContentType("text/plain"); // $NON-NLS-1$
        results.setText(response == null ? "" : Jsoup.parse(response).html()); // $NON-NLS-1$
        results.setCaretPosition(0);
        resultsScrollPane.setViewportView(results);
        // Bug 55111 - Refresh JEditor pane size depending on the presence or absence of scrollbars
        resultsScrollPane.setPreferredSize(resultsScrollPane.getMinimumSize());
        results.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("view_results_render_html_formatted"); // $NON-NLS-1$
    }

}
