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

import javax.swing.JEditorPane;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderAsHTML extends SamplerResultTab implements ResultRenderer {
    private static final Logger log = LoggerFactory.getLogger(RenderAsHTML.class);

    private static final String TEXT_HTML = "text/html"; // $NON-NLS-1$

    // Keep copies of the two editors needed
    private static final EditorKit customisedEditor = new LocalHTMLEditorKit();

    private static final EditorKit defaultHtmlEditor = JEditorPane.createEditorKitForContentType(TEXT_HTML);

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        // get the text response and image icon
        // to determine which is NOT null
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        showRenderedResponse(response, sampleResult);
    }

    protected void showRenderedResponse(String response, SampleResult res) {
        showRenderedResponse(response, res, false);
    }

    protected void showRenderedResponse(String response, SampleResult res, boolean embedded) {
        if (response == null) {
            results.setText("");
            return;
        }

        int htmlIndex = response.indexOf("<HTML"); // could be <HTML lang=""> // $NON-NLS-1$

        // Look for a case variation
        if (htmlIndex < 0) {
            htmlIndex = response.indexOf("<html"); // ditto // $NON-NLS-1$
        }

        // If we still can't find it, just try using all of the text
        if (htmlIndex < 0) {
            htmlIndex = 0;
        }

        String html = response.substring(htmlIndex);

        /*
         * To disable downloading and rendering of images and frames, enable the
         * editor-kit. The Stream property can then be
         */
        // Must be done before setContentType
        results.setEditorKitForContentType(TEXT_HTML, embedded ? defaultHtmlEditor : customisedEditor);

        results.setContentType(TEXT_HTML);

        if (embedded) {
            // Allow JMeter to render frames (and relative images)
            // Must be done after setContentType [Why?]
            results.getDocument().putProperty(Document.StreamDescriptionProperty, res.getURL());
        }
        /*
         * Get round problems parsing <META http-equiv='content-type'
         * content='text/html; charset=utf-8'> See
         * <a href="http://bz.apache.org/bugzilla/show_bug.cgi?id=23315">Bug 23315</a>
         *
         * Is this due to a bug in Java?
         */
        results.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE); // $NON-NLS-1$

        try {
            results.setText(html); // Bug can generate RTE
        } catch (RuntimeException rte) {
            results.setText("Failed to parse HTML: " + rte.getMessage());
        }
        results.setCaretPosition(0);
        try {
            resultsScrollPane.setViewportView(results);
        } catch (NumberFormatException e) {
            // Java Bug : http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=9001188.
            // See https://bz.apache.org/bugzilla/show_bug.cgi?id=54586
            log.warn("An error occurred rendering html code", e);
            results.setText("Failed to render HTML: " + e.getMessage() +", use Text renderer");            
        }
    }

    private static class LocalHTMLEditorKit extends HTMLEditorKit {

        private static final long serialVersionUID = -3399554318202905392L;

        private static final ViewFactory defaultFactory = new LocalHTMLFactory();

        @Override
        public ViewFactory getViewFactory() {
            return defaultFactory;
        }

        private static class LocalHTMLFactory extends javax.swing.text.html.HTMLEditorKit.HTMLFactory {
            /*
             * Provide dummy implementations to suppress download and display of
             * related resources: - FRAMEs - IMAGEs TODO create better dummy
             * displays TODO suppress LINK somehow
             */
            @Override
            public View create(Element elem) {
                Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
                if (o instanceof HTML.Tag) {
                    HTML.Tag kind = (HTML.Tag) o;
                    if (kind == HTML.Tag.FRAME) {
                        return new ComponentView(elem);
                    } else if (kind == HTML.Tag.IMG) {
                        return new ComponentView(elem);
                    }
                }
                return super.create(elem);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("view_results_render_html"); // $NON-NLS-1$
    }

}
