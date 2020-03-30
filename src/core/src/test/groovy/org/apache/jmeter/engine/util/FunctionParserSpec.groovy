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

package org.apache.jmeter.engine.util

import org.apache.jmeter.functions.Function
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.samplers.Sampler

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class FunctionParserSpec extends Specification {
    def "function '#value' gets compiled"() {
        given:
            CompoundVariable.functions.put('__func', Func.class)
            def parser = new FunctionParser()
        when:
            def result = parser.compileString(value)
        then:
            "$result" == "$expected"
        where:
            value           | expected
            '${__func()}'   | [new Func()]
            '${ __func()}'  | [new Func()]
            '${__func() }'  | [new Func()]
            '${ __func() }' | [new Func()]
    }

    public static class Func implements Function {
        void setParameters(Collection params) {
            // do nothing
        }
        String getReferenceKey() {
            return "__func"
        }
        List<String> getArgumentDesc() {
            return Collections.emptyList()
        }
        String execute(SampleResult result, Sampler sampler) {
            return "done"
        }
        String toString() {
            return "__func()"
        }
    }
}
