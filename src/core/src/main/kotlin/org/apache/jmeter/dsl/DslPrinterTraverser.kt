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

package org.apache.jmeter.dsl

import org.apache.jmeter.gui.tree.JMeterTreeNode
import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.property.BooleanProperty
import org.apache.jmeter.testelement.property.CollectionProperty
import org.apache.jmeter.testelement.property.DoubleProperty
import org.apache.jmeter.testelement.property.FloatProperty
import org.apache.jmeter.testelement.property.IntegerProperty
import org.apache.jmeter.testelement.property.JMeterProperty
import org.apache.jmeter.testelement.property.LongProperty
import org.apache.jmeter.testelement.property.MultiProperty
import org.apache.jmeter.testelement.property.StringProperty
import org.apache.jmeter.testelement.property.TestElementProperty
import org.apache.jmeter.testelement.schema.PropertyDescriptor
import org.apache.jorphan.collections.HashTree
import org.apache.jorphan.collections.HashTreeTraverser
import org.apiguardian.api.API
import java.io.StringWriter

/**
 * Prints [HashTree] or [TestElement] as JMeter DSL.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class DslPrinterTraverser(
    private val detailLevel: DetailLevel = DetailLevel.NON_DEFAULT
) : HashTreeTraverser {
    public enum class DetailLevel {
        ALL, NON_DEFAULT
    }
    private companion object {
        val SPECIAL_CHARS = Regex("[\"\n\r\t\b\\\\$]")
    }

    private val sw = StringWriter()
    private var level = 0
    private var plusPosition = -1

    override fun toString(): String = sw.toString()

    public fun append(testElement: TestElement): DslPrinterTraverser {
        addNode(testElement, HashTree())
        subtractNode()
        return this
    }

    override fun addNode(node: Any, subTree: HashTree) {
        if (sw.buffer.isNotEmpty()) {
            append('\n')
        }
        when (node) {
            is TestElement -> appendElement(node)
            is JMeterTreeNode -> appendElement(node.testElement)
        }
    }

    private fun appendElement(te: TestElement) {
        indent()
        plusPosition = sw.buffer.length
        append(te::class.java.name).append("::class {\n")
        level += 1
        appendProperties(te, true)
    }

    override fun subtractNode() {
        level -= 1
        // Omit empty braces in case the element did not have properties
        // Note: if all the properties are default, we do not print them,
        // so we don't use "properties.isEmpty" condition
        if (sw.buffer.endsWith(" {\n")) {
            sw.buffer.setLength(sw.buffer.length - " {\n".length)
            sw.buffer.insert(plusPosition, '+')
            append("\n")
        } else {
            indent().append("}\n")
        }
    }

    override fun processPath() {
    }

    private fun appendProperties(te: TestElement, canSkipTestClass: Boolean) {
        val emptyTe = te::class.java.getDeclaredConstructor().newInstance()

        val schema = te.schema
        val schemaProps = mutableMapOf<PropertyDescriptor<*, *>, JMeterProperty>()
        val otherProps = mutableListOf<JMeterProperty>()
        for (property in te.propertyIterator()) {
            if (emptyTe.getPropertyOrNull(property.name) == property && detailLevel != DetailLevel.ALL) {
                // If the property is the same as in newly created element, avoid printing it in the DSL
                continue
            }
            val prop = schema.properties[property.name]
            if (detailLevel != DetailLevel.ALL) {
                val stringValue = property.stringValue
                if (prop == TestElementSchema.testClass && stringValue == te::class.java.name && canSkipTestClass) {
                    continue
                }
                if ((property is StringProperty && stringValue.isNullOrEmpty()) ||
                    stringValue == prop?.defaultValueAsString
                ) {
                    continue
                }
            }

            if (prop == null) {
                otherProps += property
                continue
            }

            schemaProps[prop] = property
        }
        if (schemaProps.isNotEmpty()) {
            indent().append("props {\n")
            level += 1
            for ((prop, value) in schemaProps) {
                indent().append("it[")
                for (item in schema.getGroupPath(prop)) {
                    append(item).append('.')
                }
                append(prop.shortName).append("] = ")
                appendPropertyValue(value)
                append('\n')
            }
            level -= 1
            indent().append("}\n")
        }

        for (property in otherProps) {
            if (property is StringProperty && property.stringValue == "") {
                continue
            }
            indent()
                .append("setProperty(")
            // For TestElementProperty we have to use setProperty(Property) method
            // which lacks property name argument, so we generate property name argument
            // only for simple properties
            val usePropertyConstructor = property is TestElementProperty || property is MultiProperty
            if (usePropertyConstructor) {
                append(property::class.java.simpleName).append('(')
            }
            appendLiteral(property.name).append(", ")
            appendPropertyValue(property)
            if (usePropertyConstructor) {
                append(')')
            }
            append(")\n")
        }
    }

    private fun appendPropertyValue(property: JMeterProperty): DslPrinterTraverser = apply {
        when (property) {
            is BooleanProperty, is IntegerProperty -> append(property.stringValue)
            is DoubleProperty -> append(property.stringValue)
            is FloatProperty -> append(property.stringValue).append('f')
            is LongProperty -> append(property.stringValue).append('d')
            is StringProperty -> appendLiteral(property.stringValue)

            is TestElementProperty -> {
                val element = property.element
                append(element::class.java.name).append("()").append(".apply {\n")
                level += 1
                appendProperties(element, canSkipTestClass = false)
                level -= 1
                if (sw.buffer.endsWith(".apply {\n")) {
                    sw.buffer.setLength(sw.buffer.length - ".apply {\n".length)
                } else {
                    indent().append("}")
                }
            }

            is CollectionProperty -> {
                append("listOf(\n")
                level += 1
                for (property1 in property.iterator()) {
                    indent()
                    appendPropertyValue(property1)
                    append(",\n")
                }
                level -= 1
                if (sw.buffer.endsWith("(\n")) {
                    sw.buffer.setLength(sw.buffer.length - 1)
                    append(")")
                } else {
                    indent().append(")")
                }
            }

            else -> append("/* unsupported property type ${property::class.java.simpleName}*/").appendLiteral(property.stringValue)
        }
    }

    private fun append(value: String) = apply { sw.append(value) }

    private fun append(value: Char) = apply { sw.append(value) }

    private fun appendLiteral(literal: String): DslPrinterTraverser {
        return apply {
            append('"')
            append(
                literal.replace(SPECIAL_CHARS) {
                    when (it.value) {
                        "\"" -> "\\\""
                        "\n" -> "\\n"
                        "\r" -> "\\r"
                        "\t" -> "\\t"
                        "\b" -> "\\b"
                        "\\" -> "\\\\"
                        "$" -> "\\$"
                        else -> "\\${it.value}"
                    }
                }
            )
            append('"')
        }
    }

    private fun indent(): DslPrinterTraverser = apply {
        repeat(level) {
            sw.append("    ")
        }
    }
}
