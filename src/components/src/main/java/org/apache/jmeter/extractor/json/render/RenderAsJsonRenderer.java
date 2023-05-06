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

package org.apache.jmeter.extractor.json.render;

import java.util.List;

import org.apache.jmeter.extractor.json.jsonpath.JSONManager;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ResultRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * Implement ResultsRender for JSON Path tester
 * @since 3.0
 */
@AutoService(ResultRenderer.class)
public class RenderAsJsonRenderer extends AbstractRenderAsJsonRenderer {
    private static final Logger log = LoggerFactory.getLogger(RenderAsJsonRenderer.class);

    @Override
    protected String getTabLabel() {
        return JMeterUtils.getResString("jsonpath_tester_title");
    }

    @Override
    protected String getTestButtonLabel() {
        return JMeterUtils.getResString("jsonpath_tester_button_test");
    }

    @Override
    protected String getExpressionLabel() {
        return JMeterUtils.getResString("jsonpath_tester_field");
    }

    @Override
    protected String process(String textToParse) {
        String expression = getExpression();
        try {
            List<Object> matchStrings = extractWithTechnology(textToParse, expression);
            if (matchStrings.isEmpty()) {
                return NO_MATCH; //$NON-NLS-1$
            } else {
                StringBuilder builder = new StringBuilder();
                int i = 0;
                for (Object obj : matchStrings) {
                    String objAsString =
                            obj != null ? obj.toString() : ""; //$NON-NLS-1$
                    builder.append("Result[").append(i++).append("]=").append(objAsString).append("\n"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
                }

                return builder.toString();
            }
        } catch (Exception e) { // NOSONAR We handle it through return message
            log.debug("Exception extracting from '{}' with expression '{}'", textToParse, expression);
            return "Exception: " + e.getMessage(); //$NON-NLS-1$
        }
    }

    private static List<Object> extractWithTechnology(String textToParse, String expression) throws Exception {
        JSONManager jsonManager = new JSONManager();
        return jsonManager.extractWithJsonPath(textToParse, expression);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("jsonpath_renderer"); // $NON-NLS-1$
    }
}
