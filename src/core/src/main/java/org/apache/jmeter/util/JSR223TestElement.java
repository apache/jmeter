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

package org.apache.jmeter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Properties;
import java.util.function.Function;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jorphan.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

/**
 * Base class for JSR223 Test elements
 */
public abstract class JSR223TestElement extends ScriptingTestElement
        implements Serializable, TestStateListener
{
    private static final long serialVersionUID = 233L;

    private static final Logger logger = LoggerFactory.getLogger(JSR223TestElement.class);
    /**
     * Cache of compiled scripts
     */
    private static Cache<ScriptCacheKey, CompiledScript> COMPILED_SCRIPT_CACHE;

    /**
     * Used for locking cache initialization
     */
    private static final Object lock = new Object();

    /**
     * Lambdas can't throw checked exceptions, so we wrap cache loading failure with a runtime one.
     */
    static class ScriptCompilationInvocationTargetException extends RuntimeException {
        public ScriptCompilationInvocationTargetException(Throwable cause) {
            super(cause);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    /** If JSR223 element has checkbox 'Cache compile' checked then script in ScriptText will be compiled and cached */
    private String cacheChecked = "";

    /** Used as an unique key for the cache */
    private ScriptCacheKey scriptCacheKey;

    /**
     * Initialization On Demand Holder pattern
     */
    private static class LazyHolder {
        private LazyHolder() {
            super();
        }
        public static final ScriptEngineManager INSTANCE = new ScriptEngineManager();
    }

    /**
     * @return ScriptEngineManager singleton
     */
    public static ScriptEngineManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    protected JSR223TestElement() {
        super();
    }

    /**
     * @return {@link ScriptEngine} for language defaulting to groovy if language is not set
     * @throws ScriptException when no {@link ScriptEngine} could be found
     */
    protected ScriptEngine getScriptEngine() throws ScriptException {
        String lang = getScriptLanguageWithDefault();
        ScriptEngine scriptEngine = getInstance().getEngineByName(lang);
        if (scriptEngine == null) {
            throw new ScriptException("Cannot find engine named: '"+lang+"', ensure you set language field in JSR223 element named: " + getName());
        }

        return scriptEngine;
    }

    /**
     * @return script language or DEFAULT_SCRIPT_LANGUAGE if none is set
     */
    private String getScriptLanguageWithDefault() {
        String lang = getScriptLanguage();
        if (StringUtilities.isNotEmpty(lang)) {
            return lang;
        }
        return DEFAULT_SCRIPT_LANGUAGE;
    }

    /**
     * Populate variables to be passed to scripts
     * @param bindings Bindings
     */
    protected void populateBindings(Bindings bindings) {
        final String label = getName();
        final String fileName = getFilename();
        final String scriptParameters = getParameters();
        // Use actual class name for log
        final Logger elementLogger = LoggerFactory.getLogger(getClass().getName()+"."+getName());
        bindings.put("log", elementLogger); // $NON-NLS-1$ (this name is fixed)
        bindings.put("Label", label); // $NON-NLS-1$ (this name is fixed)
        bindings.put("FileName", fileName); // $NON-NLS-1$ (this name is fixed)
        bindings.put("Parameters", scriptParameters); // $NON-NLS-1$ (this name is fixed)
        String[] args=JOrphanUtils.split(scriptParameters, " ");//$NON-NLS-1$
        bindings.put("args", args); // $NON-NLS-1$ (this name is fixed)
        // Add variables for access to context and variables
        JMeterContext jmctx = JMeterContextService.getContext();
        bindings.put("ctx", jmctx); // $NON-NLS-1$ (this name is fixed)
        JMeterVariables vars = jmctx.getVariables();
        bindings.put("vars", vars); // $NON-NLS-1$ (this name is fixed)
        Properties props = JMeterUtils.getJMeterProperties();
        bindings.put("props", props); // $NON-NLS-1$ (this name is fixed)
        // For use in debugging:
        bindings.put("OUT", System.out); // NOSONAR $NON-NLS-1$ (this name is fixed)

        // Most subclasses will need these:
        Sampler sampler = jmctx.getCurrentSampler();
        bindings.put("sampler", sampler); // $NON-NLS-1$ (this name is fixed)
        SampleResult prev = jmctx.getPreviousResult();
        bindings.put("prev", prev); // $NON-NLS-1$ (this name is fixed)
    }


    /**
     * This method will run inline script or file script with special behaviour for file script:
     * - If ScriptEngine implements Compilable script will be compiled and cached
     * - If not if will be run
     * @param scriptEngine ScriptEngine
     * @param pBindings {@link Bindings} might be null
     * @return Object returned by script
     * @throws IOException when reading the script fails
     * @throws ScriptException when compiling or evaluation of the script fails
     */
    protected Object processFileOrScript(ScriptEngine scriptEngine, final Bindings pBindings)
            throws IOException, ScriptException {
        Bindings bindings = pBindings;
        if (bindings == null) {
            bindings = scriptEngine.createBindings();
        }
        populateBindings(bindings);
        String filename = getFilename();
        File scriptFile = new File(filename);
        // Hack: bsh-2.0b5.jar BshScriptEngine implements Compilable but throws
        // "java.lang.Error: unimplemented"
        boolean supportsCompilable = scriptEngine instanceof Compilable
                && !"bsh.engine.BshScriptEngine".equals(scriptEngine.getClass().getName()); // NOSONAR // $NON-NLS-1$
        try {
            if (StringUtilities.isNotEmpty(filename)) {
                if (!scriptFile.isFile()) {
                    throw new ScriptException("Script file '" + scriptFile.getAbsolutePath()
                            + "' is not a file for JSR223 element named: " + getName());
                }
                if (!scriptFile.canRead()) {
                    throw new ScriptException("Script file '" + scriptFile.getAbsolutePath()
                            + "' is not readable for JSR223 element named: " + getName());
                }
                if (!supportsCompilable) {
                    try (BufferedReader fileReader = Files.newBufferedReader(scriptFile.toPath())) {
                        return scriptEngine.eval(fileReader, bindings);
                    }
                }
                computeScriptCacheKey(scriptFile);
                CompiledScript compiledScript = getCompiledScript(scriptCacheKey, key -> {
                    try (BufferedReader fileReader = Files.newBufferedReader(scriptFile.toPath())) {
                        return ((Compilable) scriptEngine).compile(fileReader);
                    } catch (IOException | ScriptException e) {
                        if (logger.isDebugEnabled()) {
                            logger.warn("Cache missed access: for file script: '{}' for element named: '{}'", scriptFile.getAbsolutePath(), getName());
                        }
                        throw new ScriptCompilationInvocationTargetException(e);
                    }
                });
                return compiledScript.eval(bindings);
            }
            String script = getScript();
            if (StringUtilities.isNotEmpty(script)) {
                if (supportsCompilable) {
                    if (!ScriptingBeanInfoSupport.FALSE_AS_STRING.equals(cacheChecked)) {
                        computeScriptCacheKey(script);
                        CompiledScript compiledScript = getCompiledScript(scriptCacheKey, key -> {
                            try {
                                return ((Compilable) scriptEngine).compile(script);
                            } catch (ScriptException e) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Cache missed access: failed compile of JSR223 element named: '{}'", getName());
                                }
                                throw new ScriptCompilationInvocationTargetException(e);
                            }
                        });
                        return compiledScript.eval(bindings);
                    } else {
                        computeScriptCacheKey(script.hashCode());
                        //simulate a cache miss when JSR223 'Cache compiled script if available' is unchecked to have better view of cache usage
                        var unused = COMPILED_SCRIPT_CACHE.get(scriptCacheKey, k -> {
                            return null;
                        });
                        if (logger.isDebugEnabled()) {
                            logger.debug("Cache missed access: 'Cache compile' is unchecked for JSR223 element named: '{}'", getName());
                        }
                        return scriptEngine.eval(script, bindings);
                    }
                } else {
                    return scriptEngine.eval(script, bindings);
                }
            } else {
                throw new ScriptException("Both script file and script text are empty for JSR223 element named: " + getName());
            }
        } catch (ScriptException ex) {
            Throwable rootCause = ex.getCause();
            if (isStopCondition(rootCause)) {
                throw (RuntimeException) ex.getCause();
            } else {
                throw ex;
            }
        }
    }

    private static <T extends ScriptCacheKey> CompiledScript getCompiledScript(
            T newCacheKey,
            Function<? super ScriptCacheKey, ? extends CompiledScript> compiler
    ) throws IOException, ScriptException {
        try {
            CompiledScript compiledScript = COMPILED_SCRIPT_CACHE.get(newCacheKey, compiler);
            if (compiledScript == null) {
                throw new ScriptException("Script compilation returned null: " + newCacheKey);
            }
            return compiledScript;
        } catch (ScriptCompilationInvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                cause.addSuppressed(new IllegalStateException("Unable to compile JSR223 script: " + newCacheKey));
                throw (IOException) cause;
            }
            if (cause instanceof ScriptException) {
                cause.addSuppressed(new IllegalStateException("Unable to compile JSR223 script: " + newCacheKey));
                throw (ScriptException) cause;
            }
            throw e;
        }
    }

    /**
     * @return boolean true if element is not compilable or if compilation succeeds
     * @throws IOException if script is missing
     * @throws ScriptException if compilation fails
     */
    public boolean compile()
            throws ScriptException, IOException {
        String lang = getScriptLanguageWithDefault();
        ScriptEngine scriptEngine = getInstance().getEngineByName(lang);
        boolean supportsCompilable = scriptEngine instanceof Compilable
                && !"bsh.engine.BshScriptEngine".equals(scriptEngine.getClass().getName()); // NOSONAR // $NON-NLS-1$
        if(!supportsCompilable) {
            return true;
        }
        if (!(getScript() == null || getScript().isEmpty())) {
            try {
                ((Compilable) scriptEngine).compile(getScript());
                return true;
            } catch (ScriptException e) { // NOSONAR
                logger.error("Error compiling script for JSR223 element named: '{}', error: {}", getName(), e.getMessage());
                return false;
            }
        } else {
            File scriptFile = new File(getFilename());
            try (BufferedReader fileReader = Files.newBufferedReader(scriptFile.toPath())) {
                try {
                    ((Compilable) scriptEngine).compile(fileReader);
                    return true;
                } catch (ScriptException e) { // NOSONAR
                    logger.error("Error compiling script for JSR223 element named: '{}', error: {}", getName(), e.getMessage());
                    return false;
                }
            }
        }
    }

    /**
     * compute MD5 of a script if null
     */
    private void computeScriptCacheKey(String script) {
        // compute the md5 of the script if needed
        if (scriptCacheKey == null) {
            scriptCacheKey = ScriptCacheKey.ofString(DigestUtils.md5Hex(script));
        }
    }

    /**
     * compute cache key for a file based script if null
     */
    private void computeScriptCacheKey(File scriptFile) {
        if (scriptCacheKey == null) {
            scriptCacheKey = ScriptCacheKey.ofFile(getScriptLanguage(), scriptFile.getAbsolutePath(), scriptFile.lastModified());
        }
    }

    /**
     * compute cache key of a long value if null
     */
    private void computeScriptCacheKey(int reference) {
        if (scriptCacheKey == null) {
            scriptCacheKey = ScriptCacheKey.ofString(Integer.toString(reference));
        }
    }


    /**
     * @return the cacheChecked
     */
    public String getCacheKey() {
        return cacheChecked;
    }

    /**
     * @param cacheChecked the cacheChecked to set
     */
    public void setCacheKey(String cacheChecked) {
        this.cacheChecked = cacheChecked;
    }

    /**
     * @see org.apache.jmeter.testelement.TestStateListener#testStarted()
     */
    @Override
    public void testStarted() {
        testStarted("");
    }

    /**
     * @see org.apache.jmeter.testelement.TestStateListener#testStarted(java.lang.String)
     */
    @Override
    public void testStarted(String host) {
        synchronized (lock) {
            if (COMPILED_SCRIPT_CACHE == null) {
                COMPILED_SCRIPT_CACHE =
                        Caffeine.from(JMeterUtils.getPropDefault("jsr223.compiled_scripts_cache_spec", "maximumSize=" +
                                JMeterUtils.getPropDefault("jsr223.compiled_scripts_cache_size", 100) + ",recordStats")).build();
            }
        }
    }

    /**
     * @see org.apache.jmeter.testelement.TestStateListener#testEnded()
     */
    @Override
    public void testEnded() {
        testEnded("");
    }

    /**
     * @see org.apache.jmeter.testelement.TestStateListener#testEnded(java.lang.String)
     */
    @Override
    public void testEnded(String host) {
        synchronized (lock) {
            if (COMPILED_SCRIPT_CACHE != null) {
                CacheStats stats = COMPILED_SCRIPT_CACHE.stats();
                logger.info("JSR223 cached scripts: {}, requestsCount: {} (hitCount: {} + missedCount: {}), (hitRate: {}, missRate: {}), " +
                                "loadCount: {} (loadSuccessCount: {} + loadFailureCount: {}), " +
                                "evictionCount: {}, evictionWeight: {}, " +
                                "totalLoadTime: {} ms, averageLoadPenalty: {} ms",
                        COMPILED_SCRIPT_CACHE.estimatedSize(),
                        stats.requestCount(), stats.hitCount(), stats.missCount(),
                        String.format("%.02f", stats.hitRate()), String.format("%.02f", stats.missRate()),
                        stats.loadCount(), stats.loadSuccessCount(), stats.loadFailureCount(),
                        stats.evictionCount(), stats.evictionWeight(),
                        String.format("%.02f", (stats.totalLoadTime() / 100000f)), String.format("%.02f", (stats.averageLoadPenalty() / 100000f)));
                COMPILED_SCRIPT_CACHE.invalidateAll();
                COMPILED_SCRIPT_CACHE.cleanUp();
                COMPILED_SCRIPT_CACHE = null;
            }
        }
        scriptCacheKey = null;
    }

    public String getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(String s) {
        scriptLanguage = s;
    }
}
