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

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.util.JMeterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * BeanShell setup function - encapsulates all the access to the BeanShell
 * Interpreter in a single class.
 *
 * This class wraps a BeanShell instance.
 *
 * Note that reflection-based dynamic class loading has been removed, so the
 * bsh jar must be available at compile-time and runtime.
 */
public class BeanShellInterpreter {
    private static final Logger log = LoggerFactory.getLogger(BeanShellInterpreter.class);

    // This class is not serialised
    private Interpreter bshInstance = null; // The interpreter instance for this class

    private final String initFile; // Script file to initialize the Interpreter with

    private final Logger logger; // Logger to use during initialization and script run

    private static final String BSH_ERROR_TEMPLATE = "Error invoking bsh method: %s";

    public BeanShellInterpreter() {
        this(null, null);
    }

    /**
     *
     * @param init initialisation file
     * @param log logger to pass to interpreter
     */
    public BeanShellInterpreter(String init, Logger log) {
        initFile = init;
        logger = log;
        init();
    }

    // Called from ctor, so must be private (or final, but it does not seem useful elsewhere)
    private void init() {
        bshInstance = new Interpreter();
         if (logger != null) {// Do this before starting the script
            try {
                set("log", logger);//$NON-NLS-1$
            } catch (JMeterException e) {
                log.warn("Can't set logger variable", e);
            }
        }
        if (StringUtils.isNotBlank(initFile)) {
            String fileToUse=initFile;
            // Check file so we can distinguish file error from script error
            File in = new File(fileToUse);
            if (!in.exists()){// Cannot find the file locally, so try the bin directory
                fileToUse=JMeterUtils.getJMeterHome()
                        +File.separator+"bin" // $NON-NLS-1$
                        +File.separator+initFile;
                in = new File(fileToUse);
                if (!in.exists()) {
                    log.warn("Cannot find init file: {}", initFile);
                }
            }
            if (!in.canRead()) {
                log.warn("Cannot read init file: {}", fileToUse);
            }
            try {
                source(fileToUse);
            } catch (JMeterException e) {
                log.warn("Cannot source init file: {}", fileToUse,e);
            }
        }
    }

    /**
     * Resets the BeanShell interpreter.
     */
    public void reset() {
       init();
    }

    public Object eval(String s) throws JMeterException {
        Object r = null;
        try {
            r = bshInstance.eval(s);
        } catch (EvalError e) {
            String message = String.format(BSH_ERROR_TEMPLATE, "eval");
            Throwable cause = e.getCause();
            if (cause != null) {
                message += "\t" + cause.getLocalizedMessage();
            }
            log.error(message);
            throw new JMeterException(message, e);
        }
        return r;
    }

    public Object evalNoLog(String s) throws JMeterException {
        Object r = null;
        try {
            r = bshInstance.eval(s);
        } catch (EvalError e) {
            String message = String.format(BSH_ERROR_TEMPLATE, "eval");
            Throwable cause = e.getCause();
            if (cause != null) {
                message += "\t" + cause.getLocalizedMessage();
            }
            throw new JMeterException(message, e);
        }
        return r;
    }

    public void set(String s, Object o) throws JMeterException {
        try {
            bshInstance.set(s, o);
        } catch (EvalError e) {
            String message = String.format(BSH_ERROR_TEMPLATE, "set");
            Throwable cause = e.getCause();
            if (cause != null) {
                message += "\t" + cause.getLocalizedMessage();
            }
            log.error(message);
            throw new JMeterException(message, e);
        }
    }

    public void set(String s, boolean b) throws JMeterException {
        try {
            bshInstance.set(s, b);
        } catch (EvalError e) {
            String message = String.format(BSH_ERROR_TEMPLATE, "set");
            Throwable cause = e.getCause();
            if (cause != null) {
                message += "\t" + cause.getLocalizedMessage();
            }
            log.error(message);
            throw new JMeterException(message, e);
        }
    }

    public Object source(String s) throws JMeterException {
        Object r = null;
        try {
            r = bshInstance.source(s);
        } catch (EvalError | IOException e) {
            String message = String.format(BSH_ERROR_TEMPLATE, "source");
            Throwable cause = e.getCause();
            if (cause != null) {
                message += "\t" + cause.getLocalizedMessage();
            }
            log.error(message);
            throw new JMeterException(message, e);
        }
        return r;
    }

    public Object get(String s) throws JMeterException {
        Object r = null;
        try {
            r = bshInstance.get(s);
        } catch (EvalError e) {
            String message = String.format(BSH_ERROR_TEMPLATE, "get");
            Throwable cause = e.getCause();
            if (cause != null) {
                message += "\t" + cause.getLocalizedMessage();
            }
            log.error(message);
            throw new JMeterException(message, e);
        }
        return r;
    }

    // For use by Unit Tests
    public static boolean isInterpreterPresent() {
        Class<?> bshClass = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            bshClass = loader.loadClass("bsh.Interpreter");
        } catch (ClassNotFoundException e) {
            log.error("Beanshell Interpreter not found", e);
        }
        return bshClass != null;
    }
}
