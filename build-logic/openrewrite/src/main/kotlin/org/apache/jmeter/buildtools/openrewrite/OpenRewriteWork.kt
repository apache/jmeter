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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.openrewrite.ExecutionContext
import org.openrewrite.InMemoryExecutionContext
import org.openrewrite.Parser
import org.openrewrite.Result
import org.openrewrite.SourceFile
import org.openrewrite.Tree
import org.openrewrite.config.Environment
import org.openrewrite.config.OptionDescriptor
import org.openrewrite.config.RecipeDescriptor
import org.openrewrite.config.YamlResourceLoader
import org.openrewrite.internal.InMemoryLargeSourceSet
import org.openrewrite.java.JavaParser
import org.openrewrite.java.internal.JavaTypeCache
import org.openrewrite.java.marker.JavaVersion
import org.openrewrite.kotlin.KotlinParser
import org.slf4j.LoggerFactory
import java.io.File
import java.io.Serializable
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import java.util.zip.ZipFile

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

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val files: ConfigurableFileCollection
}

fun SourceSetConfig.toSnapshot() =
    SourceSetSnapshot(
        name = name,
        javaRelease = javaRelease.get(),
        srcDirSets = srcDirSets.associateBy({ it.name }) { it.files.files },
        compileClasspath = compileClasspath.files.filterTo(mutableSetOf()) { it.exists() },
    )

data class SourceSetSnapshot(
    val name: String,
    val javaRelease: Int,
    val srcDirSets: Map<String, Set<File>>,
    val compileClasspath: Set<File>,
) : Serializable

interface OpenRewriteParameters : WorkParameters {
    /** When true, apply the changes in place; when false, only write a patch to [patchFile]. */
    val applyChanges: Property<Boolean>

    /** Directory the results' source paths are resolved against (the project directory). */
    val projectRoot: DirectoryProperty

    /** Unified diff of the proposed changes, written in dry-run mode. */
    val patchFile: RegularFileProperty

    /** When true, run each leaf recipe on its own and report which ones fail or make changes. */
    val diagnose: Property<Boolean>

    val configFile: RegularFileProperty
    val activeRecipes: SetProperty<String>

    /** Fully qualified recipe names to prune from the active recipe tree before running. */
    val disabledRecipes: SetProperty<String>

    val activeStyles: SetProperty<String>
    val sourceSets: SetProperty<SourceSetSnapshot>
    val rewriteClasspath: SetProperty<File>
    val logCompilationWarningsAndErrors: Property<Boolean>
    val processKotlin: Property<Boolean>
}

abstract class OpenRewriteWork : WorkAction<OpenRewriteParameters> {
    companion object {
        val logger = LoggerFactory.getLogger(OpenRewriteWork::class.java) as org.gradle.api.logging.Logger
    }

    override fun execute() {
        val env = createEnvironment()
        val activated = env.activateRecipes(parameters.activeRecipes.get())
        val disabled = parameters.disabledRecipes.get()
        val diagnose = parameters.diagnose.get()

        if (activated.recipeList.isEmpty()) {
            logger.warn("No recipes were activated. Add a recipe with openrewrite.activeRecipes in the build file.")
            return
        }
        val styles = env.activateStyles(parameters.activeStyles.get())

        val ctx: ExecutionContext = InMemoryExecutionContext { t -> logger.warn("Error while rewriting", t) }
        val sourceFiles = parse(ctx)
            .map { source ->
                if (styles.isEmpty()) {
                    source
                } else {
                    source.withMarkers(styles.fold(source.markers) { markers, style -> markers.add(style) })
                }
            }
            .toList()

        // In diagnose we test everything (disabled recipes are still reported, annotated) so the
        // report is a complete picture. In normal runs we prune the disabled recipes from the tree.
        if (diagnose) {
            diagnose(activated, sourceFiles, disabled)
            return
        }

        val recipe = if (disabled.isEmpty()) {
            activated
        } else {
            val present = collectNames(activated).intersect(disabled)
            logger.lifecycle("OpenRewrite disabled {} recipe(s): {}", present.size, present.sorted())
            FilteringRecipe(activated, disabled)
        }

        val results = recipe.run(InMemoryLargeSourceSet(sourceFiles), ctx).changeset.allResults
        if (parameters.applyChanges.get()) {
            applyResults(results)
        } else {
            writePatch(results)
        }
    }

    private fun collectNames(recipe: org.openrewrite.Recipe): Set<String> {
        val names = mutableSetOf<String>()
        fun visit(r: org.openrewrite.Recipe) {
            names.add(r.name)
            r.recipeList.forEach { visit(it) }
        }
        visit(recipe)
        return names
    }

    /** Runs each leaf recipe individually so a single failure does not hide the others. */
    private fun diagnose(recipe: org.openrewrite.Recipe, sourceFiles: List<SourceFile>, disabled: Set<String>) {
        fun leaves(r: org.openrewrite.Recipe): List<org.openrewrite.Recipe> =
            if (r.recipeList.isEmpty()) listOf(r) else r.recipeList.flatMap { leaves(it) }

        val leaves = leaves(recipe).distinctBy { it.name }
        val report = StringBuilder()
        var failed = 0
        var changed = 0
        var clean = 0
        for (leaf in leaves) {
            val tag = if (leaf.name in disabled) " [disabled]" else ""
            val errors = mutableListOf<Throwable>()
            val ctx: ExecutionContext = InMemoryExecutionContext { errors.add(it) }
            val line = try {
                val results = leaf.run(InMemoryLargeSourceSet(sourceFiles), ctx).changeset.allResults
                when {
                    errors.isNotEmpty() -> {
                        failed++
                        "FAILED    ${leaf.name}$tag -> ${errors.first().firstMessageLine()}"
                    }
                    results.isNotEmpty() -> {
                        changed++
                        "changes:${results.size.toString().padStart(3)} ${leaf.name}$tag"
                    }
                    else -> {
                        clean++
                        "ok        ${leaf.name}$tag"
                    }
                }
            } catch (t: Throwable) {
                failed++
                "FAILED    ${leaf.name}$tag -> ${t.firstMessageLine()}"
            }
            report.appendLine(line)
            logger.lifecycle(line)
        }
        val summary = "OpenRewrite diagnose: ${leaves.size} recipes -> " +
            "$failed failed, $changed would change, $clean no-op"
        report.appendLine().appendLine(summary)
        logger.lifecycle(summary)
        val out = parameters.patchFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(report.toString())
    }

    private fun Throwable.firstMessageLine(): String =
        (message ?: this::class.java.simpleName).lineSequence().first().take(200)

    private fun applyResults(results: List<Result>) {
        val root = parameters.projectRoot.get().asFile.toPath()
        var changes = 0
        for (result in results) {
            val before = result.before
            val after = result.after
            when {
                before == null && after != null -> {
                    logger.lifecycle("Generated new file {} by:", after.sourcePath)
                    logRecipesThatMadeChanges(result)
                    writeAfter(root, after)
                    changes++
                }

                before != null && after == null -> {
                    logger.lifecycle("Deleted file {} by:", before.sourcePath)
                    logRecipesThatMadeChanges(result)
                    Files.deleteIfExists(root.resolve(before.sourcePath))
                    changes++
                }

                before != null && after != null && before.sourcePath != after.sourcePath -> {
                    logger.lifecycle("Moved file {} to {} by:", before.sourcePath, after.sourcePath)
                    logRecipesThatMadeChanges(result)
                    Files.deleteIfExists(root.resolve(before.sourcePath))
                    writeAfter(root, after)
                    changes++
                }

                before != null && after != null -> {
                    logger.lifecycle("Changed file {} by:", after.sourcePath)
                    logRecipesThatMadeChanges(result)
                    writeAfter(root, after)
                    changes++
                }
            }
        }
        logger.lifecycle("OpenRewrite applied changes to {} file(s)", changes)
    }

    private fun writeAfter(root: Path, after: SourceFile) {
        val target = root.resolve(after.sourcePath)
        Files.createDirectories(target.parent)
        val charset = after.charset ?: StandardCharsets.UTF_8
        Files.newBufferedWriter(target, charset).use { writer ->
            writer.write(after.printAll())
        }
    }

    private fun writePatch(results: List<Result>) {
        val patch = parameters.patchFile.get().asFile
        patch.parentFile.mkdirs()
        val diffs = results.mapNotNull { result ->
            result.diff().takeIf { it.isNotBlank() }?.also {
                val path = (result.after ?: result.before)?.sourcePath
                logger.lifecycle("These recipes would make changes to {}:", path)
                logRecipesThatMadeChanges(result)
            }
        }
        patch.writeText(if (diffs.isEmpty()) "" else diffs.joinToString("\n") + "\n")
    }

    private fun logRecipesThatMadeChanges(result: Result) {
        var prefix = "    "
        for (recipeDescriptor in result.recipeDescriptorsThatMadeChanges) {
            logRecipe(recipeDescriptor, prefix)
            prefix += "    "
        }
    }

    private fun logRecipe(rd: RecipeDescriptor, prefix: String) {
        val recipeString = StringBuilder(prefix + rd.name)
        val opts = rd.options
            .mapNotNull { option: OptionDescriptor -> option.value?.let { "${option.name}=$it" } }
            .joinToString(", ")
        if (opts.isNotEmpty()) {
            recipeString.append(": {").append(opts).append("}")
        }
        logger.lifecycle("{}", recipeString)
        for (child in rd.recipeList) {
            logRecipe(child, "$prefix    ")
        }
    }

    private fun createEnvironment(): Environment =
        Environment.builder()
            .apply {
                // Load declarative recipes from META-INF/rewrite/*.yml on the recipe classpath.
                // This avoids Environment.scanClassLoader (ClassGraph), which OOMs on large
                // classpaths (see https://github.com/openrewrite/rewrite/issues/3899); compiled
                // recipes referenced by the YAML are still instantiated by name at activation.
                parameters.rewriteClasspath.get().forEach { cpFile ->
                    if (cpFile.isFile && cpFile.path.endsWith(".jar", ignoreCase = true)) {
                        val baseUrl = URL(cpFile.toURI().toURL(), "!/")
                        ZipFile(cpFile).use { zip ->
                            zip.entries().asSequence().forEach { ze ->
                                if (ze.name.startsWith("META-INF/rewrite/") &&
                                    (ze.name.endsWith(".yml", true) || ze.name.endsWith(".yaml", true))
                                ) {
                                    zip.getInputStream(ze).use { stream ->
                                        val uri = URL(baseUrl, ze.name).toURI()
                                        load(YamlResourceLoader(stream, uri, Properties(), javaClass.classLoader))
                                    }
                                }
                            }
                        }
                    }
                }
                val configFile = parameters.configFile.asFile.get()
                configFile.inputStream().use { stream ->
                    load(YamlResourceLoader(stream, configFile.toURI(), Properties(), javaClass.classLoader))
                }
            }
            .build()

    private fun parse(ctx: ExecutionContext): Sequence<SourceFile> {
        val root = parameters.projectRoot.get().asFile.toPath()
        var res = sequenceOf<SourceFile>()
        for (sourceSet in parameters.sourceSets.get()) {
            val javaTypeCache = JavaTypeCache()
            val classpath = sourceSet.compileClasspath.map { it.toPath() }

            val javaVersion = JavaVersion(
                Tree.randomId(),
                System.getProperty("java.runtime.version"),
                System.getProperty("java.vm.vendor"),
                sourceSet.javaRelease.toString(),
                sourceSet.javaRelease.toString()
            )

            fun collect(name: String, extension: String): List<Path> =
                sourceSet.srcDirSets[name].orEmpty()
                    .filter { it.path.endsWith(extension, ignoreCase = true) }
                    .map { it.toPath() }

            fun Parser.parseInto(files: List<Path>) {
                if (files.isEmpty()) {
                    return
                }
                logger.info("OpenRewrite parsing {} files under {}", files.size, root)
                res += parse(files, root, ctx).iterator().asSequence()
                    .map { it.withMarkers(it.markers.add(javaVersion)) }
            }

            val javaFiles = collect("java", ".java")
            if (javaFiles.isNotEmpty()) {
                JavaParser.fromJavaVersion()
                    .classpath(classpath)
                    .typeCache(javaTypeCache)
                    .logCompilationWarningsAndErrors(parameters.logCompilationWarningsAndErrors.get())
                    .build()
                    .parseInto(javaFiles)
            }

            val kotlinFiles = if (parameters.processKotlin.get()) collect("kotlin", ".kt") else emptyList()
            if (kotlinFiles.isNotEmpty()) {
                KotlinParser.builder()
                    .classpath(classpath)
                    .typeCache(javaTypeCache)
                    .logCompilationWarningsAndErrors(parameters.logCompilationWarningsAndErrors.get())
                    .build()
                    .parseInto(kotlinFiles)
            }
        }
        return res
    }
}

/**
 * Wraps a recipe tree and hides recipes whose name is in [disabled], so a crashing or unwanted
 * recipe can be switched off by name without editing the third-party composite that declares it.
 * The compiled composites expose an immutable recipeList, so filtering by wrapping is the reliable
 * way to remove a recipe from the run.
 */
private class FilteringRecipe(
    private val delegate: org.openrewrite.Recipe,
    private val disabled: Set<String>,
) : org.openrewrite.Recipe() {
    private val filtered: List<org.openrewrite.Recipe> by lazy {
        delegate.recipeList
            .filter { it.name !in disabled }
            .map { FilteringRecipe(it, disabled) }
    }

    override fun getName(): String = delegate.name
    override fun getDisplayName(): String = delegate.displayName
    override fun getDescription(): String =
        delegate.description?.takeIf { it.isNotBlank() } ?: delegate.displayName
    override fun getVisitor(): org.openrewrite.TreeVisitor<*, org.openrewrite.ExecutionContext> =
        delegate.visitor
    override fun getRecipeList(): List<org.openrewrite.Recipe> = filtered
}
