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
 *
 */

package org.apache.jmeter.buildtools

import org.apache.tools.ant.filters.FixCrLfFilter
import org.gradle.api.file.ContentFilterable
import org.gradle.api.file.CopySpec
import org.gradle.kotlin.dsl.filter

enum class LineEndings(val value: String) {
    CRLF("dos"), LF("unix");

    companion object {
        fun current() = when (val sep = System.lineSeparator()) {
            "\n" -> LF
            "\r\n" -> CRLF
            else -> throw IllegalStateException("Unexpected line separator: ${sep.toByteArray()}")
        }
    }
}

fun ContentFilterable.filter(eol: LineEndings) =
    filter(
        FixCrLfFilter::class, mapOf(
            "eol" to FixCrLfFilter.CrLf.newInstance(eol.value),
            "fixlast" to false,
            "ctrlz" to FixCrLfFilter.AddAsisRemove.newInstance("asis")
        )
    )

fun CopySpec.includeShell(
    src: Any,
    vararg scriptName: String,
    action: (CopySpec.() -> Unit)? = null
) {
    if (scriptName.isEmpty()) {
        return
    }
    from(src) {
        filter(LineEndings.LF)
        fileMode = "755".toInt(8)
        include(*scriptName)
        include(scriptName.map { "$it.sh" })
        action?.invoke(this)
    }
    from(src) {
        filter(LineEndings.CRLF)
        include(scriptName.map { "$it.bat" })
        include(scriptName.map { "$it.cmd" })
        action?.invoke(this)
    }
}

fun CopySpec.excludeShell(vararg scriptName: String) {
    scriptName.forEach {
        exclude(it, "$it.sh", "$it.bat", "$it.cmd")
    }
}

private fun MutableList<String>.exceptStar() = filter { !it.endsWith("/*") && !it.endsWith("/*.*") }

fun CopySpec.from(sourcePath: Any, textEol: LineEndings, action: AutoClassifySpec.() -> Unit) {
    val spec = AutoClassifySpec()
    spec.action()
    val shellScripts = spec.shell.toTypedArray()
    includeShell(sourcePath, *shellScripts) {
        exclude(spec.exclude)
    }
    if (!spec.text.isEmpty()) {
        from(sourcePath) {
            filter(textEol)
            include(spec.text)
            exclude(spec.binary.exceptStar())
            exclude(spec.exclude)
            excludeShell(*shellScripts)
        }
    }
    if (!spec.binary.isEmpty()) {
        from(sourcePath) {
            include(spec.binary)
            exclude(spec.text.exceptStar())
            exclude(spec.exclude)
            excludeShell(*shellScripts)
        }
    }
}

class CrLfSpec(val textEol: LineEndings = LineEndings.current()) {
    fun CopySpec.textFrom(o: Any, eol: LineEndings = textEol) =
        from(o) {
            filter(eol)
        }

    fun CopySpec.textFrom(o: Any, action: AutoClassifySpec.() -> Unit) =
        from(o, textEol) {
            action()
        }
}
