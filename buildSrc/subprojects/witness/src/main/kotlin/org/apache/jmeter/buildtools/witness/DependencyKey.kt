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

package org.apache.jmeter.buildtools.witness

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyArtifact
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

data class DependencyKey(val dependencyNotation: String) : Comparable<DependencyKey> {
    constructor(compId: ModuleComponentIdentifier, classifier: String?, ext: String) :
            this(compId.toString().with(classifier, ext))

    constructor(dependency: Dependency) : this(dependency.toStr())

    override fun compareTo(other: DependencyKey): Int =
        dependencyNotation.compareTo(other.dependencyNotation)

    override fun toString() = dependencyNotation
}

private fun String.with(classifier: String?, ext: String): String {
    if (classifier == null) {
        return this
    }
    var result = "$this:$classifier"
    if (ext != DependencyArtifact.DEFAULT_TYPE) {
        result += "@$ext"
    }
    return result

}

fun Dependency.toStr(): String {
    val result = "$group:$name:$version"
    if (this is ModuleDependency) {
        for (artifact in this.artifacts) {
            return result.with(artifact.classifier, artifact.extension)
        }
    }
    return result
}
