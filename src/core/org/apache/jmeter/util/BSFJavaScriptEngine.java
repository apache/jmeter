/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.util;

import java.util.Vector;

import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.Wrapper;

/**
 * This is the interface to Netscape's Rhino (JavaScript) from the
 * Bean Scripting Framework.
 * <p>
 * The original version of this code was first written by Adam Peller
 * for use in LotusXSL. Sanjiva took his code and adapted it for BSF.
 *
 * Modified for JMeter to fix bug BSF-22.
 */
public class BSFJavaScriptEngine extends BSFEngineImpl {
    /**
     * The global script object, where all embedded functions are defined,
     * as well as the standard ECMA "core" objects.
     */
    private Scriptable global;

    /**
     * Return an object from an extension.
     * @param object Object on which to make the call (ignored).
     * @param method The name of the method to call.
     * @param args an array of arguments to be
     * passed to the extension, which may be either
     * Vectors of Nodes, or Strings.
     */
    @Override
    public Object call(Object object, String method, Object[] args)
        throws BSFException {

        Object retval = null;
        Context cx;

        try {
            cx = Context.enter();

            // REMIND: convert arg list Vectors here?

            Object fun = global.get(method, global);
            // NOTE: Source and line arguments are nonsense in a call().
            //       Any way to make these arguments *sensible?
            if (fun == Scriptable.NOT_FOUND) {
                throw new EvaluatorException("function " + method +
                                             " not found.", "none", 0);
            }

            cx.setOptimizationLevel(-1);
            cx.setGeneratingDebug(false);
            cx.setGeneratingSource(false);
            cx.setOptimizationLevel(0);
            cx.setDebugger(null, null);

            retval =
                ((Function) fun).call(cx, global, global, args);
            if (retval instanceof Wrapper) {
                retval = ((Wrapper) retval).unwrap();
            }
        }
        catch (Throwable t) { //NOSONAR We handle correctly Error case in function
            handleError(t);
        }
        finally {
            Context.exit();
        }
        return retval;
    }

    @Override
    public void declareBean(BSFDeclaredBean bean) throws BSFException {
        if ((bean.bean instanceof Number) ||
            (bean.bean == null) ||
            (bean.bean instanceof String) ||
            (bean.bean instanceof Boolean)) {
            global.put(bean.name, global, bean.bean);
        }
        else {
            // Must wrap non-scriptable objects before presenting to Rhino
            Scriptable wrapped = Context.toObject(bean.bean, global);
            global.put(bean.name, global, wrapped);
        }
    }

    /**
     * This is used by an application to evaluate a string containing
     * some expression.
     */
    @Override
    public Object eval(String source, int lineNo, int columnNo, Object oscript)
        throws BSFException {

        String scriptText = oscript.toString();
        Object retval = null;
        Context cx;

        try {
            cx = Context.enter();

            cx.setOptimizationLevel(-1);
            cx.setGeneratingDebug(false);
            cx.setGeneratingSource(false);
            cx.setOptimizationLevel(0);
            cx.setDebugger(null, null);

            retval = cx.evaluateString(global, scriptText,
                                       source, lineNo,
                                       null);

            if (retval instanceof NativeJavaObject) {
                retval = ((NativeJavaObject) retval).unwrap();
            }

        }
        catch (Throwable t) { // NOSONAR We handle correctly Error case in function, includes JavaScriptException, rethrows Errors
            handleError(t);
        }
        finally {
            Context.exit();
        }
        return retval;
    }

    /**
     * @param t {@link Throwable}
     * @throws BSFException
     */
    private void handleError(Throwable t) throws BSFException {
        Throwable target = t;
        if (t instanceof WrappedException) {
            target = ((WrappedException) t).getWrappedException();
        }

        String message = null;
        if (target instanceof JavaScriptException) {
            message = target.getLocalizedMessage();

            // Is it an exception wrapped in a JavaScriptException?
            Object value = ((JavaScriptException) target).getValue();
            if (value instanceof Throwable) {
                // likely a wrapped exception from a LiveConnect call.
                // Display its stack trace as a diagnostic
                target = (Throwable) value;
            }
        }
        else if (target instanceof EvaluatorException ||
                target instanceof SecurityException) {
            message = target.getLocalizedMessage();
        }
        else if (target instanceof RuntimeException) {
            message = "Internal Error: " + target.toString();
        }
        else if (target instanceof StackOverflowError) {
            message = "Stack Overflow";
        }

        if (message == null) {
            message = target.toString();
        }

        if (target instanceof Error && !(target instanceof StackOverflowError)) {
            // Re-throw Errors because we're supposed to let the JVM see it
            // Don't re-throw StackOverflows, because we know we've
            // corrected the situation by aborting the loop and
            // a long stacktrace would end up on the user's console
            throw (Error) target;
        }
        else {
            throw new BSFException(BSFException.REASON_OTHER_ERROR,
                                   "JavaScript Error: " + message,
                                   target);
        }
    }

    /**
     * Initialize the engine.
     * Put the manager into the context-manager
     * map hashtable too.
     */
    @Override
    public void initialize(BSFManager mgr, String lang,
            @SuppressWarnings("rawtypes") // superclass does not support types
            Vector declaredBeans)
        throws BSFException {

        super.initialize(mgr, lang, declaredBeans);

        // Initialize context and global scope object
        try {
            Context cx = Context.enter();
            global = new ImporterTopLevel(cx);
            Scriptable bsf = Context.toObject(new BSFFunctions(mgr, this), global);
            global.put("bsf", global, bsf);
            @SuppressWarnings("unchecked") // superclass does not support types
            final Vector<BSFDeclaredBean> beans = declaredBeans;
            for (BSFDeclaredBean declaredBean : beans) {
                declareBean(declaredBean);
            }
        }
        catch (Throwable t) { // NOSONAR We handle correctly Error case in function
            handleError(t);
        }
        finally {
            Context.exit();
        }
    }

    @Override
    public void undeclareBean(BSFDeclaredBean bean) throws BSFException {
        global.delete(bean.name);
    }
}
