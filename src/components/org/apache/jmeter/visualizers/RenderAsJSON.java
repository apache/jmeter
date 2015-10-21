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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

public class RenderAsJSON extends SamplerResultTab implements ResultRenderer {
    private static final String TAB_SEPARATOR = ":   "; //$NON-NLS-1$

    private static final String ESC_CHAR_REGEX = "\\\\[\"\\\\/bfnrt]|\\\\u[0-9A-Fa-f]{4}"; // $NON-NLS-1$

    private static final String NORMAL_CHARACTER_REGEX = "[^\"\\\\]";  // $NON-NLS-1$

    private static final String STRING_REGEX = "\"(" + ESC_CHAR_REGEX + "|" + NORMAL_CHARACTER_REGEX + ")*+\""; // $NON-NLS-1$

    // This 'other value' regex is deliberately weak, even accepting an empty string, to be useful when reporting malformed data.
    private static final String OTHER_VALUE_REGEX = "[^\\{\\[\\]\\}\\,]*"; // $NON-NLS-1$

    private static final String VALUE_OR_PAIR_REGEX = "((" + STRING_REGEX + "\\s*:)?\\s*(" + STRING_REGEX + "|" + OTHER_VALUE_REGEX + ")\\s*,?\\s*)"; // $NON-NLS-1$

    private static final Pattern VALUE_OR_PAIR_PATTERN = Pattern.compile(VALUE_OR_PAIR_REGEX);

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        showRenderJSONResponse(response);
    }

    private void showRenderJSONResponse(String response) {
        results.setContentType("text/plain"); // $NON-NLS-1$
        results.setText(response == null ? "" : prettyJSON(response));
        results.setCaretPosition(0);
        resultsScrollPane.setViewportView(results);
    }

    // It might be useful also to make this available in the 'Request' tab, for
    // when posting JSON.
    /**
     * Pretty-print JSON text
     * @param json input text
     * @return prettyfied json
     */
    public static String prettyJSON(String json) {
        return prettyJSON(json, RenderAsJSON.TAB_SEPARATOR);
    }
    
    /**
     * Pretty-print JSON text
     * @param json input text
     * @param tabSeparator String tab separator
     * @return prettyfied json
     */
    public static String prettyJSON(String json, String tabSeparator) {
        StringBuilder pretty = new StringBuilder(json.length() * 2); // Educated guess

        final String tab = tabSeparator; // $NON-NLS-1$
        StringBuilder index = new StringBuilder();
        String nl = ""; // $NON-NLS-1$

        Matcher valueOrPair = VALUE_OR_PAIR_PATTERN.matcher(json);

        boolean misparse = false;

        for (int i = 0; i < json.length(); ) {
            final char currentChar = json.charAt(i);
            if ((currentChar == '{') || (currentChar == '[')) {
                pretty.append(nl).append(index).append(currentChar);
                i++;
                index.append(tab);
                misparse = false;
            }
            else if ((currentChar == '}') || (currentChar == ']')) {
                if (index.length() > 0) {
                    index.delete(0, tab.length());
                }
                pretty.append(nl).append(index).append(currentChar);
                i++;
                int j = i;
                while ((j < json.length()) && Character.isWhitespace(json.charAt(j))) {
                    j++;
                }
                if ((j < json.length()) && (json.charAt(j) == ',')) {
                    pretty.append(","); // $NON-NLS-1$
                    i=j+1;
                }
                misparse = false;
            }
            else if (valueOrPair.find(i) && valueOrPair.group().length() > 0) {
                pretty.append(nl).append(index).append(valueOrPair.group());
                i=valueOrPair.end();
                misparse = false;
            }
            else {
                if (!misparse) {
                    pretty.append(nl).append("- Parse failed from:");
                }
                pretty.append(currentChar);
                i++;
                misparse = true;
            }
            nl = "\n"; // $NON-NLS-1$
        }
        return pretty.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("view_results_render_json"); // $NON-NLS-1$
    }

}
