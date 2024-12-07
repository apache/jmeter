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

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.openrewrite.ExecutionContext
import org.openrewrite.InMemoryExecutionContext
import org.openrewrite.Parser
import org.openrewrite.RecipeRun
import org.openrewrite.Result
import org.openrewrite.SourceFile
import org.openrewrite.Tree
import org.openrewrite.Validated
import org.openrewrite.config.Environment
import org.openrewrite.config.OptionDescriptor
import org.openrewrite.config.RecipeDescriptor
import org.openrewrite.config.YamlResourceLoader
import org.openrewrite.gradle.isolated.ResultsContainer
import org.openrewrite.gradle.isolated.deleted
import org.openrewrite.gradle.isolated.generated
import org.openrewrite.gradle.isolated.moved
import org.openrewrite.gradle.isolated.refactoredInPlace
import org.openrewrite.internal.InMemoryLargeSourceSet
import org.openrewrite.java.JavaParser
import org.openrewrite.java.internal.JavaTypeCache
import org.openrewrite.java.marker.JavaVersion
import org.openrewrite.java.style.Autodetect
import org.openrewrite.java.tree.J
import org.openrewrite.kotlin.KotlinParser
import org.openrewrite.kotlin.tree.K
import org.openrewrite.tree.ParsingExecutionContextView
import org.openrewrite.xml.tree.Xml
import org.slf4j.LoggerFactory
import java.io.File
import java.io.Serializable
import java.net.URL
import java.util.*
import java.util.zip.ZipFile
import javax.inject.Inject
import kotlin.system.measureTimeMillis

abstract class SourceSetConfig(
    private val name: String,
) : Named, Serializable {
    @Internal
    override fun getName(): String = name

    @get:Input
    abstract val javaRelease: Property<Int>

    @get:Nested
    abstract val srcDirSets: NamedDomainObjectContainer<SourceDirectorySetConfig>

    @get:Classpath
    abstract val compileClasspath: ConfigurableFileCollection

}

abstract class SourceDirectorySetConfig(
    private val name: String,
) : Named, Serializable {
    @Internal
    override fun getName(): String = name

    @get:Internal
    abstract val sourceDirectories: ConfigurableFileCollection

    @get:Internal
    lateinit var filter: PatternFilterable

    @get:Internal
    abstract val includes: SetProperty<String>

    @get:Inject
    abstract val providers: ProviderFactory

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val files: SetProperty<ConfigurableFileTree>
}

fun SourceSetConfig.toSnapshot() =
    SourceSetSnapshot(
        name = name,
        javaRelease = javaRelease.get(),
        srcDirSets = srcDirSets.associateBy({ it.name }) { it.toSnapshot() },
        compileClasspath = compileClasspath.elements.get()
            .mapNotNullTo(mutableSetOf()) { it.asFile.takeIf { it.exists() } },
    )

fun SourceDirectorySetConfig.toSnapshot() =
    SourceDirectorySetSnapshot(
        name = name,
        // TODO: this causes serializing the file names
        files = files.get().mapNotNull {
            SourceDirectorySetFilesSnapshot(
                baseDir = it.dir,
                files = it.files,
            ).takeIf { it.files.isNotEmpty() }
        }
    )

data class SourceSetSnapshot(
    val name: String,
    val javaRelease: Int,
    val srcDirSets: Map<String, SourceDirectorySetSnapshot>,
    val compileClasspath: Set<File>,
) : Serializable

data class SourceDirectorySetSnapshot(
    val name: String,
    val files: List<SourceDirectorySetFilesSnapshot>,
) : Serializable

data class SourceDirectorySetFilesSnapshot(
    val baseDir: File,
    val files: Set<File>,
) : Serializable

interface OpenRewriteParameters : WorkParameters {
    val outputDirectory: DirectoryProperty
    val baseDir: DirectoryProperty
    val configFile: RegularFileProperty
    val activeRecipes: SetProperty<String>
    val sourceSets: SetProperty<SourceSetSnapshot>
    val rewriteRuntimeClasspath: SetProperty<File>
    val logCompilationWarningsAndErrors: Property<Boolean>
}

abstract class OpenRewriteWork : WorkAction<OpenRewriteParameters> {
    companion object {
        val logger = LoggerFactory.getLogger(OpenRewriteWork::class.java) as org.gradle.api.logging.Logger
    }

//    @get:Inject
//    abstract val fileOperations: FileOperations


    override fun execute() {
        val view = ParsingExecutionContextView.view(
            InMemoryExecutionContext { logger.warn("Error rewriting", it) }
        )
        val results = listResults(view)
        run2(results, view)
    }

    fun run2(results: ResultsContainer, view: ParsingExecutionContextView) {
        if (!results.isNotEmpty) {
            return
        }
        results.firstException?.let {
            throw it
        }
        for (result: Result in results.generated) {
            logger.lifecycle("Generated new file " + result.after!!.sourcePath + " by:")
            logRecipesThatMadeChanges(result)
        }
        for (result: Result in results.deleted) {
            logger.lifecycle("Deleted file " + result.before!!.sourcePath + " by:")
            logRecipesThatMadeChanges(result)
        }
        for (result: Result in results.moved) {
            logger.lifecycle(
                "File has been moved from " +
                    result.before!!.sourcePath + " to " +
                    result.after!!.sourcePath + " by:"
            )
            logRecipesThatMadeChanges(result)
        }
        for (result: Result in results.refactoredInPlace) {
            logger.lifecycle("Changes have been made to " + result.before!!.sourcePath + " by:")
            logRecipesThatMadeChanges(result)
        }
    }

    private fun logRecipesThatMadeChanges(result: Result) {
        val indent = "    "
        var prefix = "    "
        for (recipeDescriptor in result.recipeDescriptorsThatMadeChanges) {
            logRecipe(recipeDescriptor, prefix)
            prefix += indent
        }
    }

    private fun logRecipe(rd: RecipeDescriptor, prefix: String) {
        val recipeString = StringBuilder(prefix + rd.name)
        if (rd.options.isNotEmpty()) {
            val opts = rd.options
                .mapNotNull { option: OptionDescriptor ->
                    if (option.value != null) {
                        option.name + "=" + option.value
                    } else {
                        null
                    }
                }
                .joinToString(", ")
            if (opts.isNotEmpty()) {
                recipeString.append(": {").append(opts).append("}")
            }
        }
        logger.warn("{}", recipeString)
        for (rChild in rd.recipeList) {
            logRecipe(rChild, "$prefix    ")
        }
    }

    private fun listResults(ctx: ExecutionContext): ResultsContainer {
        val baseDir = parameters.baseDir.get().asFile.toPath()
        val env = createEnvironment()
        val recipe = env.activateRecipes(parameters.activeRecipes.get());
        if (recipe.recipeList.isEmpty()) {
            logger.warn("No recipes were activated. Activate a recipe with rewriteTask.activeRecipes.add(\"com.fully.qualified.RecipeClassName\") in your build file")
            return ResultsContainer(baseDir, null)
        }

        val validated = recipe.validateAll(ctx, mutableListOf<Validated<Any>>())
        val failedValidations = validated.flatMap {
            it.failures().map { failure ->
                IllegalArgumentException(
                    "Recipe validation error in ${failure.property}: ${failure.message}",
                    failure.exception
                )
            }
        }
        if (failedValidations.size == 1) {
            throw failedValidations.first()
        } else if (failedValidations.size > 1) {
            throw IllegalArgumentException(
                "Multiple recipe failed validations: ${failedValidations.map { it.toString() }}"
            ).apply {
                failedValidations.forEach { addSuppressed(it) }
            }
        }

        val javaDetector = Autodetect.detector()
        val kotlinDetector = org.openrewrite.kotlin.style.Autodetect.detector()
        val xmlDetector = org.openrewrite.xml.style.Autodetect.detector()
        var sourceFiles = parse(ctx)
                .onEach { s ->
                    when (s) {
                        is K.CompilationUnit ->
                            kotlinDetector.sample(s)

                        is J.CompilationUnit ->
                            javaDetector.sample(s)

                        else ->
                            xmlDetector.sample(s)
                    }
                }
                .toList()

        val stylesByType = mapOf(
            J.CompilationUnit::class.java to javaDetector.build(),
            K.CompilationUnit::class.java to kotlinDetector.build(),
            Xml.Document::class.java to xmlDetector.build(),
        )

        sourceFiles = sourceFiles.map {
            for ((klass, format) in stylesByType) {
                if (klass.isInstance(it)) {
                    return@map it.withMarkers<SourceFile>(it.markers.add(format))
                }
            }
            it
        }

//        logger.lifecycle(
//            "All sources parsed, running active recipes: {}",
//            recipe.recipeList.joinToString(", ")
//        )
        val recipeRun = recipe.run(InMemoryLargeSourceSet(sourceFiles), ctx)
        return ResultsContainer(baseDir, recipeRun)
    }

    private fun createEnvironment() = Environment.builder()
        .apply {
            parameters.rewriteRuntimeClasspath.get().forEach { cpFile ->
                if (cpFile.isFile && cpFile.path.endsWith(".jar", ignoreCase = true)) {
                    val baseUrl by lazy { URL(cpFile.toURI().toURL(), "!/") }
                    ZipFile(cpFile).use { zip ->
                        zip.entries().asSequence().forEach { ze ->
                            if (ze.name.startsWith("META-INF/rewrite/") && (
                                    ze.name.endsWith(".yml", ignoreCase = true) ||
                                        ze.name.endsWith(".yaml", ignoreCase = true)
                                    )
                            ) {
                                zip.getInputStream(ze).use {
                                    val uri = URL(baseUrl, ze.name).toURI()
                                    load(YamlResourceLoader(it, uri, Properties(), this::class.java.classLoader))
                                }
                            }
                        }
                    }
                }
            }
            val configFile = parameters.configFile.asFile.get()
            configFile.inputStream().use { stream ->
                load(YamlResourceLoader(stream, configFile.toURI(), Properties(), this::class.java.classLoader))
            }
        }
        .build()

    private fun parse(ctx: ExecutionContext): Sequence<SourceFile> {
        var res = sequenceOf<SourceFile>()
        for (sourceSet in parameters.sourceSets.get()) {
            val javaTypeCache = JavaTypeCache()

            val javaVersion = JavaVersion(
                Tree.randomId(),
                System.getProperty("java.runtime.version"),
                System.getProperty("java.vm.vendor"),
                sourceSet.javaRelease.toString(),
                sourceSet.javaRelease.toString()
            )

            fun Parser.parse(srcDirSet: SourceDirectorySetSnapshot) {
                srcDirSet.files.forEach { root ->
                    val files = root.files.filter { it.path.endsWith(".kt") }.map { it.toPath() }
                    if (files.isEmpty()) {
                        return@forEach
                    }
                    logger.lifecycle("Parsing {} files in {}: {}", files.size, root.baseDir, files)
                    res = res.plus(
                        parse(files, root.baseDir.toPath(), ctx).iterator()
                            .asSequence()
                            .map {
                                it.withMarkers(it.markers.add(javaVersion))
                            }
                    )
                }
            }

            sourceSet.srcDirSets["java"]?.let { srcDirSet ->
                val parser = JavaParser.fromJavaVersion()
                    .classpath(sourceSet.compileClasspath.map { it.toPath() })
//                    .styles(styles)
                    .typeCache(javaTypeCache)
                    .logCompilationWarningsAndErrors(parameters.logCompilationWarningsAndErrors.get())
                    .build()

                parser.parse(srcDirSet)
            }

            sourceSet.srcDirSets["kotlin"]?.let { srcDirSet ->
                val parser = KotlinParser.builder()
                    .classpath(sourceSet.compileClasspath.map { it.toPath() })
//                    .styles(styles)
                    .typeCache(javaTypeCache)
                    .logCompilationWarningsAndErrors(parameters.logCompilationWarningsAndErrors.get())
                    .build()

                parser.parse(srcDirSet)
            }
        }
        return res
    }
}
