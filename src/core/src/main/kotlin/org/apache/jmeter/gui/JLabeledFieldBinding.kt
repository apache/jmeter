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

package org.apache.jmeter.gui

import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.schema.PropertyDescriptor
import org.apache.jorphan.gui.JLabeledField
import org.apiguardian.api.API

/**
 * Binds a [JLabeledField] to a [PropertyDescriptor], so JMeter can automatically update the test element
 * from the UI state and vice versa.
 * @since 5.6.3
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6.3")
public class JLabeledFieldBinding(
    private val labeledField: JLabeledField,
    private val propertyDescriptor: PropertyDescriptor<*, *>,
) : Binding {
    override fun updateElement(testElement: TestElement) {
        testElement[propertyDescriptor] = labeledField.text.takeIf { it.isNotEmpty() }
    }

    override fun updateUi(testElement: TestElement) {
        labeledField.text =
            if (testElement.getPropertyOrNull(propertyDescriptor) == null) {
                ""
            } else {
                testElement.getString(propertyDescriptor)
            }
    }
}
