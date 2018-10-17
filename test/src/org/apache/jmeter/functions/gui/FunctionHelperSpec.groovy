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

package org.apache.jmeter.functions.gui;

import java.awt.event.ActionEvent

import org.apache.jmeter.config.Argument
import org.apache.jmeter.config.Arguments
import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jorphan.gui.GuiUtils

import spock.lang.IgnoreIf
import spock.lang.Unroll

@Unroll
class FunctionHelperSpec extends JMeterSpec {

    @IgnoreIf({ Boolean.valueOf(System.properties['java.awt.headless']) })
    def "construct correct call string for parameters #parameters"() {
        setup:
          def functionHelper = new FunctionHelper()
        when:
          def args = new Arguments()
          args.setArguments(parameters.collect { new Argument("dummy${it}", it)})
        then:
          functionHelper.buildFunctionCallString(functionName, args).toString() == combined
        where:
          functionName | parameters    | combined
          "fname"      | []            | "\${fname}"
          "fname"      | ["a"]         | "\${fname(a)}"
          "fname"      | ["a,b"]       | "\${fname(a\\,b)}"
          "fname"      | ["a,b,c"]     | "\${fname(a\\,b\\,c)}"
          "fname"      | ["a", "b"]    | "\${fname(a,b)}"
          "fname"      | ["a,b", "c"]  | "\${fname(a\\,b,c)}"
    }
}

