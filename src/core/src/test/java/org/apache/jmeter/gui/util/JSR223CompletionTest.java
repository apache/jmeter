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

package org.apache.jmeter.gui.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JTextArea;

import org.fife.ui.autocomplete.Completion;
import org.junit.jupiter.api.Test;

class JSR223CompletionTest {

    @Test
    void includesCommonBindingsButNotSamplerSpecificResult() {
        Set<String> completions = completions(false);

        assertTrue(completions.contains("sampler"));
        assertTrue(completions.contains("prev.getResponseCode"));
        assertTrue(completions.contains("vars.put"));
        assertFalse(completions.contains("SampleResult"));
    }

    @Test
    void includesSampleResultOnlyWhenBound() {
        Set<String> completions = completions(true);

        assertTrue(completions.contains("SampleResult.getResponseCode"));
    }

    @Test
    void matchesPartialAndQualifiedBindingPrefixes() {
        assertTrue(completions(false, "p").contains("prev"));
        assertTrue(completions(false, "pr").contains("prev"));
        assertTrue(completions(false, "pre").contains("prev"));
        assertTrue(completions(false, "prev").contains("prev"));
        assertTrue(completions(false, "prev.").contains("prev.getResponseCode"));
    }

    private static Set<String> completions(boolean hasSampleResultBinding) {
        return completions(hasSampleResultBinding, "");
    }

    private static Set<String> completions(boolean hasSampleResultBinding, String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setCaretPosition(text.length());
        return JSR223Completion.createProvider(hasSampleResultBinding).getCompletions(textArea)
                .stream().map(Completion::getInputText).collect(Collectors.toSet());
    }
}
