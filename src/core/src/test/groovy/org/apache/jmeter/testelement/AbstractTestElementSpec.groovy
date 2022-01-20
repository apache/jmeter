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

package org.apache.jmeter.testelement

import org.apache.commons.lang3.NotImplementedException
import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apache.jmeter.testelement.property.MultiProperty
import org.apache.jmeter.testelement.property.PropertyIterator
import org.apache.jmeter.testelement.property.TestElementProperty

import spock.lang.Unroll

@Unroll
class AbstractTestElementSpec extends JMeterSpec {

    def "set outer properties as temporary when using a TestElementProperty"() {
        given:
            AbstractTestElement sut = Spy(AbstractTestElement.class)
            def outerElement = Mock(TestElement.class)
            def innerElement = Mock(TestElement.class)
            def outerProp = new TestElementProperty("outerProp", outerElement)
            def innerProp = new TestElementProperty("innerProp", innerElement)
            outerProp.addProperty(innerProp)
        when:
            sut.setTemporary(outerProp)
        then:
            sut.isTemporary(outerProp)
            !sut.isTemporary(innerProp)
    }

    def "set all properties as temporary when using a MultiProperty"() {
        given:
            AbstractTestElement sut = Spy(AbstractTestElement.class)
            def outerProp = new MinimalMultiProperty()
            def innerProp = new MinimalMultiProperty()
            outerProp.addProperty(innerProp)
        when:
            sut.setTemporary(outerProp)
        then:
            sut.isTemporary(outerProp)
            sut.isTemporary(innerProp)
    }

    private class MinimalMultiProperty extends MultiProperty {

        Set<JMeterProperty> props = new HashSet<>()

        @Override
        void recoverRunningVersion(TestElement owner) {
            throw new NotImplementedException()
        }

        @Override
        String getStringValue() {
            throw new NotImplementedException()
        }

        @Override
        Object getObjectValue() {
            return null
        }

        @Override
        void setObjectValue(Object value) {
            throw new NotImplementedException()
        }

        @Override
        PropertyIterator iterator() {
            return props.iterator() as PropertyIterator
        }

        @Override
        void addProperty(JMeterProperty prop) {
            props.add(prop)
        }

        @Override
        void clear() {
            props.clear()
        }
    }
}
