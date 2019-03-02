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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class RenderAsJSON extends SamplerResultTab implements ResultRenderer {
    private static final String TAB_SEPARATOR = "    "; //$NON-NLS-1$

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        showRenderJSONResponse(response);
    }

    private void showRenderJSONResponse(String response) {
        results.setContentType("text/plain"); // $NON-NLS-1$
        setTextOptimized(response == null ? "" : prettyJSON(response));
        results.setCaretPosition(0);
        resultsScrollPane.setViewportView(results);
    }

    // It might be useful also to make this available in the 'Request' tab, for
    // when posting JSON.
    /**
     * Pretty-print JSON text
     * @param json input text
     * @return prettied json string
     */
    public static String prettyJSON(String json) {
        return prettyJSON(json, TAB_SEPARATOR);
    }

    /**
     * Pretty-print JSON text
     * @param json input text
     * @param tabSeparator String tab separator
     * @return prettied json string
     */
    public static String prettyJSON(String json, String tabSeparator) {
        try {
            Object o = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE)
                    .parse(json);
            if (o instanceof JSONObject) {
                return ((JSONObject) o)
                        .toJSONString(new PrettyJSONStyle(tabSeparator));
            } else if (o instanceof JSONArray) {
                return ((JSONArray) o)
                        .toJSONString(new PrettyJSONStyle(tabSeparator));
            }
        } catch (ParseException e) {
            return json;
        }
        return json;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("view_results_render_json"); // $NON-NLS-1$
    }

    private static class PrettyJSONStyle extends JSONStyle {
        private int level = 0;
        private String indentString = TAB_SEPARATOR;

        public PrettyJSONStyle(String indentString) {
            this.indentString = indentString;
        }

        private void indent(Appendable out) throws IOException {
            out.append('\n');
            out.append(StringUtils.repeat(indentString, level));
        }

        @Override
        public void objectStart(Appendable out) throws IOException {
            super.objectStart(out);
            level++;
        }

        @Override
        public void objectStop(Appendable out) throws IOException {
            level--;
            indent(out);
            super.objectStop(out);
        }

        @Override
        public void objectNext(Appendable out) throws IOException {
            super.objectNext(out);
            indent(out);
        }

        @Override
        public void objectEndOfKey(Appendable out) throws IOException {
            super.objectEndOfKey(out);
            out.append(' ');
        }

        @Override
        public void objectFirstStart(Appendable out) throws IOException {
            indent(out);
            super.objectFirstStart(out);
        }

        @Override
        public void arrayfirstObject(Appendable out) throws IOException {
            indent(out);
            super.arrayfirstObject(out);
        }

        @Override
        public void arrayNextElm(Appendable out) throws IOException {
            super.arrayNextElm(out);
            indent(out);
        }

        @Override
        public void arrayStart(Appendable out) throws IOException {
            super.arrayStart(out);
            level++;
        }

        @Override
        public void arrayStop(Appendable out) throws IOException {
            level--;
            indent(out);
            super.arrayStop(out);
        }

    }
}
