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
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

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
    private static final Cache<ScriptCacheKey, CompiledScript> COMPILED_SCRIPT_CACHE =
            Caffeine
                    .newBuilder()
                    .maximumSize(JMeterUtils.getPropDefault("jsr223.compiled_scripts_cache_size", 100))
                    .build();

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

    /** If not empty then script in ScriptText will be compiled and cached */
    private String cacheKey = "";

    /** md5 of the script, used as an unique key for the cache */
    private ScriptCacheKey scriptMd5;

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
            throw new ScriptException("Cannot find engine named: '"+lang+"', ensure you set language field in JSR223 Test Element: "+getName());
        }

        return scriptEngine;
    }

    /**
     * @return script language or DEFAULT_SCRIPT_LANGUAGE if none is set
     */
    private String getScriptLanguageWithDefault() {
        String lang = getScriptLanguage();
        if (StringUtils.isNotEmpty(lang)) {
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
            if (!StringUtils.isEmpty(filename)) {
                if (!scriptFile.isFile()) {
                    throw new ScriptException("Script file '" + scriptFile.getAbsolutePath()
                            + "' is not a file for element: " + getName());
                }
                if (!scriptFile.canRead()) {
                    throw new ScriptException("Script file '" + scriptFile.getAbsolutePath()
                            + "' is not readable for element:" + getName());
                }
                if (!supportsCompilable) {
                    try (BufferedReader fileReader = Files.newBufferedReader(scriptFile.toPath())) {
                        return scriptEngine.eval(fileReader, bindings);
                    }
                }
                CompiledScript compiledScript;
                ScriptCacheKey newCacheKey =
                        ScriptCacheKey.ofFile(getScriptLanguage(), scriptFile.getAbsolutePath(), scriptFile.lastModified());
                compiledScript = getCompiledScript(newCacheKey, key -> {
                    try (BufferedReader fileReader = Files.newBufferedReader(scriptFile.toPath())) {
                        return ((Compilable) scriptEngine).compile(fileReader);
                    } catch (IOException | ScriptException e) {
                        throw new ScriptCompilationInvocationTargetException(e);
                    }
                });
                return compiledScript.eval(bindings);
            }
            String script = getScript();
            if (!StringUtils.isEmpty(script)) {
                if (supportsCompilable &&
                        !ScriptingBeanInfoSupport.FALSE_AS_STRING.equals(cacheKey)) {
                    computeScriptMD5(script);
                    CompiledScript compiledScript = getCompiledScript(scriptMd5, key -> {
                        try {
                            return ((Compilable) scriptEngine).compile(script);
                        } catch (ScriptException e) {
                            throw new ScriptCompilationInvocationTargetException(e);
                        }
                    });
                    return compiledScript.eval(bindings);
                } else {
                    return scriptEngine.eval(script, bindings);
                }
            } else {
                throw new ScriptException("Both script file and script text are empty for element:" + getName());
            }
        } catch (ScriptException ex) {
            Throwable rootCause = ex.getCause();
            if(isStopCondition(rootCause)) {
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
                cause.addSuppressed(new IllegalStateException("Unable to compile script " + newCacheKey));
                throw (IOException) cause;
            }
            if (cause instanceof ScriptException) {
                cause.addSuppressed(new IllegalStateException("Unable to compile script " + newCacheKey));
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
        if (!StringUtils.isEmpty(getScript())) {
            try {
                ((Compilable) scriptEngine).compile(getScript());
                return true;
            } catch (ScriptException e) { // NOSONAR
                logger.error("Error compiling script for test element {}, error:{}", getName(), e.getMessage());
                return false;
            }
        } else {
            File scriptFile = new File(getFilename());
            try (BufferedReader fileReader = Files.newBufferedReader(scriptFile.toPath())) {
                try {
                    ((Compilable) scriptEngine).compile(fileReader);
                    return true;
                } catch (ScriptException e) { // NOSONAR
                    logger.error("Error compiling script for test element {}, error:{}", getName(), e.getMessage());
                    return false;
                }
            }
        }
    }

    /**
     * compute MD5 if it is null
     */
    private void computeScriptMD5(String script) {
        // compute the md5 of the script if needed
        if(scriptMd5 == null) {
            scriptMd5 = ScriptCacheKey.ofString(DigestUtils.md5Hex(script));
        }
    }

    /**
     * @return the cacheKey
     */
    public String getCacheKey() {
        return cacheKey;
    }

    /**
     * @param cacheKey the cacheKey to set
     */
    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    /**
     * @see org.apache.jmeter.testelement.TestStateListener#testStarted()
     */
    @Override
    public void testStarted() {
        // NOOP
    }

    /**
     * @see org.apache.jmeter.testelement.TestStateListener#testStarted(java.lang.String)
     */
    @Override
    public void testStarted(String host) {
        // NOOP
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
        COMPILED_SCRIPT_CACHE.invalidateAll();
        scriptMd5 = null;
    }

    public String getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(String s) {
        scriptLanguage = s;
    }
}
