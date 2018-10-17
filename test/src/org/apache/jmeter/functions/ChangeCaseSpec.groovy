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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.functions

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ChangeCaseSpec extends Specification {

    def "convert '#input' using mode #mode to '#output'"() {
        given:
            def changeCase = new ChangeCase();
            def jMCtx = JMeterContextService.getContext();
            def result = new SampleResult();
            result.setResponseData("dummy data", null);
            jMCtx.setVariables(new JMeterVariables());
            jMCtx.setPreviousResult(result);
        when:
            changeCase.setParameters([new CompoundVariable(input), new CompoundVariable(mode)]);
        then:
            output == changeCase.execute(result, null)
        where:
            input               | mode               | output
            "simple"            | "lower"            | "simple"
            "simple"            | "upper"            | "SIMPLE"
            "simple"            | "capitalize"       | "Simple"
            "simple"            | ""                 | "SIMPLE"
            " with space "      | "lower"            | " with space "
            " with space "      | "upper"            | " WITH SPACE "
            " with space "      | "capitalize"       | " with space "
            "#_with-signs."     | "lower"            | "#_with-signs."
            "#_with-signs."     | "upper"            | "#_WITH-SIGNS."
            "#_with-signs."     | "capitalize"       | "#_with-signs."
            "m4u file"          | "lower"            | "m4u file"
            "m4u file"          | "upper"            | "M4U FILE"
            "m4u file"          | "capitalize"       | "M4u file"
            "WITH Ümläuts"      | "lower"            | "with ümläuts"
            "WITH Ümläuts"      | "upper"            | "WITH ÜMLÄUTS"
            "WITH Ümläuts"      | "capitalize"       | "WITH Ümläuts"
            "+ - special space" | "lower"            | "+ - special space"
            "+ - special space" | "upper"            | "+ - SPECIAL SPACE"
            "+ - special space" | "capitalize"       | "+ - special space"
            " "                 | "lower"            | " "
            " "                 | "upper"            | " "
            " "                 | "capitalize"       | " "
    }

}

