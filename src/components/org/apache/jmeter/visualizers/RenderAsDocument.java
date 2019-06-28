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
import org.apache.jmeter.util.Document;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderAsDocument extends SamplerResultTab implements ResultRenderer {

    private static final Logger log = LoggerFactory.getLogger(RenderAsDocument.class);

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        try {
            showDocumentResponse(sampleResult);
        } catch (Exception e) {
            results.setText(e.toString());
            log.error("Error while rendering document.", e); // $NON-NLS-1$
        }
    }

    private void showDocumentResponse(SampleResult sampleResult) {
        String response = Document.getTextFromDocument(sampleResult.getResponseData());

        results.setContentType("text/plain"); // $NON-NLS-1$
        setTextOptimized(response);
        results.setCaretPosition(0);
        resultsScrollPane.setViewportView(results);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("view_results_render_document"); // $NON-NLS-1$
    }

}
