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

package org.apache.jmeter.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.util.JMeterError;
import org.apache.jorphan.util.JMeterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BeanShell setup function - encapsulates all the access to the BeanShell
 * Interpreter in a single class.
 *
 * The class uses dynamic class loading to access BeanShell, which means that
 * all the source files can be built without needing access to the bsh jar.
 *
 * If the beanshell jar is not present at run-time, an error will be logged
 *
 */

public class BeanShellInterpreter {
    private static final Logger log = LoggerFactory.getLogger(BeanShellInterpreter.class);

    private static final Method bshGet;

    private static final Method bshSet;

    private static final Method bshEval;

    private static final Method bshSource;

    private static final Class<?> bshClass;

    private static final String BSH_INTERPRETER = "bsh.Interpreter"; //$NON-NLS-1$

    static {
        // Temporary copies, so can set the final ones
        Method get = null;
        Method eval = null;
        Method set = null;
        Method source = null;
        Class<?> clazz = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            clazz = loader.loadClass(BSH_INTERPRETER);
            Class<String> string = String.class;
            Class<Object> object = Object.class;

            get = clazz.getMethod("get", string); //$NON-NLS-1$
            eval = clazz.getMethod("eval", string); //$NON-NLS-1$
            set = clazz.getMethod("set", string, object); //$NON-NLS-1$
            source = clazz.getMethod("source", string); //$NON-NLS-1$
        } catch (ClassNotFoundException|SecurityException | NoSuchMethodException e) {
            log.error("Beanshell Interpreter not found", e);
        } finally {
            bshEval = eval;
            bshGet = get;
            bshSet = set;
            bshSource = source;
            bshClass = clazz;
        }
    }

    // This class is not serialised
    private Object bshInstance = null; // The interpreter instance for this class

    private final String initFile; // Script file to initialize the Interpreter with

    private final Logger logger; // Logger to use during initialization and script run

    public BeanShellInterpreter() throws ClassNotFoundException {
        this(null, null);
    }

    /**
     *
     * @param init initialisation file
     * @param log logger to pass to interpreter
     * @throws ClassNotFoundException when beanshell can not be instantiated
     */
    public BeanShellInterpreter(String init, Logger log)  throws ClassNotFoundException {
        initFile = init;
        logger = log;
        init();
    }

    // Called from ctor, so must be private (or final, but it does not seem useful elsewhere)
    private void init() throws ClassNotFoundException {
        if (bshClass == null) {
            throw new ClassNotFoundException(BSH_INTERPRETER);
        }
        try {
            bshInstance = bshClass.getDeclaredConstructor().newInstance();
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            log.error("Can't instantiate BeanShell", e);
            throw new ClassNotFoundException("Can't instantiate BeanShell", e);
        } 
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
     *
     * @throws ClassNotFoundException if interpreter cannot be instantiated
     */
    public void reset() throws ClassNotFoundException {
       init();
    }

    private Object bshInvoke(Method m, Object[] o, boolean shouldLog) throws JMeterException {
        Object r = null;
        final String errorString = "Error invoking bsh method: ";
        try {
            r = m.invoke(bshInstance, o);
        } catch (IllegalArgumentException | IllegalAccessException e) { // Programming error
            final String message = errorString + m.getName();
            log.error(message);
            throw new JMeterError(message, e);
        } catch (InvocationTargetException e) { // Can occur at run-time
            // could be caused by the bsh Exceptions:
            // EvalError, ParseException or TargetError
            String message = errorString + m.getName();
            Throwable cause = e.getCause();
            if (cause != null) {
                message += "\t" + cause.getLocalizedMessage();
            }

            if (shouldLog) {
                log.error(message);
            }
            throw new JMeterException(message, e);
        }
        return r;
    }

    public Object eval(String s) throws JMeterException {
        return bshInvoke(bshEval, new Object[] { s }, true);
    }

    public Object evalNoLog(String s) throws JMeterException {
        return bshInvoke(bshEval, new Object[] { s }, false);
    }

    public Object set(String s, Object o) throws JMeterException {
        return bshInvoke(bshSet, new Object[] { s, o }, true);
    }

    public Object set(String s, boolean b) throws JMeterException {
        return bshInvoke(bshSet, new Object[] { s, Boolean.valueOf(b) }, true);
    }

    public Object source(String s) throws JMeterException {
        return bshInvoke(bshSource, new Object[] { s }, true);
    }

    public Object get(String s) throws JMeterException {
        return bshInvoke(bshGet, new Object[] { s }, true);
    }

    // For use by Unit Tests
    public static boolean isInterpreterPresent(){
        return bshClass != null;
    }
}
