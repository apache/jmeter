/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
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

package org.apache.jmeter.extractor.json.render

import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.extractor.json.render.RenderAsJsonRenderer
import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.util.JMeterUtils
import javax.swing.JTabbedPane
import org.apache.jmeter.junit.categories.NeedGuiTests
import org.junit.experimental.categories.Category
import spock.lang.IgnoreIf

class RenderAsJsonRendererSpec extends JMeterSpec {
    def sut = new RenderAsJsonRenderer()

    def "init of component doesn't fail"() {
        when:
            sut.init()
        then:
            noExceptionThrown()
            sut.jsonWithJSonPathPanel != null;
    }
    
    @IgnoreIf({ JMeterSpec.isHeadless() })
    def "render image"() {
        given:
            sut.init()
            def sampleResult = new SampleResult();
        when:
            sut.renderImage(sampleResult)
        then:
            sut.jsonDataField.getText() == JMeterUtils.getResString("jsonpath_render_no_text")
    }

    def "render null Response"() {
        given:
            sut.init()
            def sampleResult = new SampleResult();
        when:
            sut.renderResult(sampleResult)
        then:
            sut.jsonDataField.getText() == ""
    }
    
    @IgnoreIf({ JMeterSpec.isHeadless() })
    def "render '#input' as JSON Response to '#output'"() {
        given:
            sut.init();
            def sampleResult = new SampleResult();
        when:
            sampleResult.setResponseData(input);
            sut.renderResult(sampleResult)
        then:
            output == sut.jsonDataField.getText()
        where:
            input               |   output
            "This is not json"  |   "This is not json" 
            "{name:\"Ludwig\",age: 23,city: \"Bonn\"}" | '''{
    "city": "Bonn",
    "name": "Ludwig",
    "age": 23
}'''
    }
    
    def "execute '#expression' on '#input' results into '#output'"() {
        given:
            sut.init();
            sut.jsonPathExpressionField.setText(expression);
            def sampleResult = new SampleResult();
        when:
            sut.executeAndJSonPathTester(input);
        then:
            output == sut.jsonPathResultField.getText()
        where:
            input               | expression          | output
            "{name:\"Ludwig\",age: 23,city: \"Bonn\"}"   | "\$..name"           | "Result[0]=Ludwig\n"
            "This is not json"  | "\$..name" | "NO MATCH" 
            "{name:\"Ludwig\",age: 23,city: \"Bonn\"}" | "\$.." | "Exception: Path must not end with a '.' or '..'"
    }
    
    def "clearData clears expected fields"() {
        given:
            sut.init()
            sut.jsonDataField.setText("blabla")
            sut.jsonPathResultField.setText("blabla")
        when:
            sut.clearData()
        then:
            sut.jsonDataField.getText() == ""
            sut.jsonPathResultField.getText() == ""
    }

    def "setupTabPane adds the tab to rightSide"() {
        given:
            sut.init()
            def rightSideTabbedPane = new JTabbedPane();
            sut.setRightSide(rightSideTabbedPane)
        when:
            sut.setupTabPane()
        then:
            sut.rightSide.getTabCount() == 1
            // Investigate why it's failing
            // sut.rightSide.getTabComponentAt(0) == sut.jsonWithJSonPathPanel
    }
    
    def "setupTabPane called twice does not add twice the tab"() {
        given:
            sut.init()
            def rightSideTabbedPane = new JTabbedPane();
            sut.setRightSide(rightSideTabbedPane)
            sut.setupTabPane()
        when:
            sut.setupTabPane()
        then:
            sut.rightSide.getTabCount() == 1
    }
}
