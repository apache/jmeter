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

package org.apache.jmeter.buildtools.openrewrite

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

abstract class OpenRewriteBaseTask @Inject constructor(
    objects: ObjectFactory,
    private val executor: WorkerExecutor,
) : DefaultTask() {
    @get:Input
    abstract val activeRecipes: SetProperty<String>

    @get:Input
    abstract val disabledRecipes: SetProperty<String>

    @get:Input
    abstract val activeStyles: SetProperty<String>

    @get:Classpath
    abstract val rewriteClasspath: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val configFile: RegularFileProperty

    @get:Internal
    abstract val projectRootDir: DirectoryProperty

    @get:Input
    abstract val logCompilationWarningsAndErrors: Property<Boolean>

    @get:Input
    abstract val processKotlin: Property<Boolean>

    @get:Nested
    val sourceSets: NamedDomainObjectContainer<SourceSetConfig> =
        objects.domainObjectContainer(SourceSetConfig::class.java)

    protected fun runRewrite(applyChanges: Boolean, patch: Provider<RegularFile>?, diagnose: Boolean = false) {
        val snapshots = sourceSets.map { it.toSnapshot() }
        val rewriteCp = rewriteClasspath.files
        val queue = executor.processIsolation {
            classpath.from(rewriteClasspath)
        }
        queue.submit(OpenRewriteWork::class) {
            this.applyChanges.set(applyChanges)
            this.diagnose.set(diagnose)
            this.projectRoot.set(projectRootDir)
            patch?.let { this.patchFile.set(it) }
            this.configFile.set(this@OpenRewriteBaseTask.configFile)
            this.activeRecipes.set(this@OpenRewriteBaseTask.activeRecipes)
            this.disabledRecipes.set(this@OpenRewriteBaseTask.disabledRecipes)
            this.activeStyles.set(this@OpenRewriteBaseTask.activeStyles)
            this.sourceSets.set(snapshots)
            this.rewriteClasspath.set(rewriteCp)
            this.logCompilationWarningsAndErrors.set(this@OpenRewriteBaseTask.logCompilationWarningsAndErrors)
            this.processKotlin.set(this@OpenRewriteBaseTask.processKotlin)
        }
        queue.await()
    }
}

/** Applies the active recipes in place, creating, deleting, moving and updating source files. */
abstract class OpenRewriteRunTask @Inject constructor(
    objects: ObjectFactory,
    executor: WorkerExecutor,
) : OpenRewriteBaseTask(objects, executor) {
    @TaskAction
    fun run() {
        runRewrite(applyChanges = true, patch = null)
    }
}

/** Reports the changes the active recipes would make as a unified diff, without touching sources. */
@CacheableTask
abstract class OpenRewriteDryRunTask @Inject constructor(
    objects: ObjectFactory,
    executor: WorkerExecutor,
) : OpenRewriteBaseTask(objects, executor) {
    @get:OutputDirectory
    abstract val reportDir: DirectoryProperty

    @get:Input
    abstract val failOnDryRunResults: Property<Boolean>

    @TaskAction
    fun run() {
        val patch = reportDir.file(PATCH_NAME)
        runRewrite(applyChanges = false, patch = patch)
        val patchFile = patch.get().asFile
        if (failOnDryRunResults.get() && patchFile.length() > 0) {
            throw GradleException(
                "OpenRewrite found code that violates the active recipes. " +
                    "Run ./gradlew ${path.replace("DryRun", "Run")} to apply the changes:\n" +
                    patchFile.readText()
            )
        }
    }

    companion object {
        const val PATCH_NAME = "rewrite.patch"
    }
}

/** Runs each active leaf recipe on its own and reports which fail, change, or do nothing. */
abstract class OpenRewriteDiagnoseTask @Inject constructor(
    objects: ObjectFactory,
    executor: WorkerExecutor,
) : OpenRewriteBaseTask(objects, executor) {
    @get:OutputDirectory
    abstract val reportDir: DirectoryProperty

    @TaskAction
    fun run() {
        runRewrite(applyChanges = false, patch = reportDir.file("diagnose.txt"), diagnose = true)
        logger.lifecycle("OpenRewrite diagnose report: {}", reportDir.file("diagnose.txt").get().asFile)
    }
}
