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

package org.apache.jmeter.loadsave

import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.TestElementTraverser
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apache.jmeter.testelement.property.StringProperty

/**
 * Normalize "enabled" property to boolean if the property is set.
 * JMeter loads "enabled" as [StringProperty], see
 * [org.apache.jmeter.save.converters.ConversionHelp.restoreSpecialProperties],
 * so we need to normalize it back if we want comparing elements
 */
object IsEnabledNormalizer : TestElementTraverser {
    override fun startTestElement(el: TestElement) {
        el.getPropertyOrNull(el.schema.enabled)
            ?.stringValue
            ?.toBooleanStrictOrNull()
            ?.let { el.isEnabled = it }
    }

    override fun endTestElement(el: TestElement) {
    }

    override fun startProperty(key: JMeterProperty) {
    }

    override fun endProperty(key: JMeterProperty) {
    }
}
