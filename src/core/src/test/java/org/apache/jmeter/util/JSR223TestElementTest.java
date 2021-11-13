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

package org.apache.jmeter.util;

import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.lang3.JavaVersion;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class JSR223TestElementTest {

    @SuppressWarnings("serial")
    private JSR223TestElement element = new JSR223TestElement() {
    };

    @Test
    public void testGetScriptEngineJS() throws Exception {
        Assumptions.assumeTrue(
                JavaVersion.JAVA_RECENT.atMost(JavaVersion.JAVA_14),
                "The default JavaScript engine has been removed in Java 15+, current Java is " + JavaVersion.JAVA_RECENT
        );
        element.setScriptLanguage("JavaScript");
        assertThat(element.getScriptEngine().getFactory().getLanguageName(),
                CoreMatchers.containsString("Script"));
    }

    @Test
    public void testGetScriptEngineDefault() throws Exception {
        element.setScriptLanguage("");
        assertThat(element.getScriptEngine().getFactory().getLanguageName(),
                CoreMatchers.is("Groovy"));
    }

}
