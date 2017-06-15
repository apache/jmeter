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

package org.apache.jmeter.control;

import java.io.Serializable;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * This is a Conditional Controller; it will execute the set of statements
 * (samplers/controllers, etc) while the 'condition' is true.
 * <p>
 * In a programming world - this is equivalent of :
 * <pre>
 * if (condition) {
 *          statements ....
 *          }
 * </pre>
 * In JMeter you may have :
 * <pre> 
 * Thread-Group (set to loop a number of times or indefinitely,
 *    ... Samplers ... (e.g. Counter )
 *    ... Other Controllers ....
 *    ... IfController ( condition set to something like - ${counter} &lt; 10)
 *       ... statements to perform if condition is true
 *       ...
 *    ... Other Controllers /Samplers }
 * </pre>
 */

// for unit test code @see TestIfController

public class IfController extends GenericController implements Serializable, ThreadListener {

    private static final Logger log = LoggerFactory.getLogger(IfController.class);

    private static final long serialVersionUID = 242L;

    private static final String NASHORN_ENGINE_NAME = "nashorn"; //$NON-NLS-1$

    private static final String CONDITION = "IfController.condition"; //$NON-NLS-1$

    private static final String EVALUATE_ALL = "IfController.evaluateAll"; //$NON-NLS-1$

    private static final String USE_EXPRESSION = "IfController.useExpression"; //$NON-NLS-1$
    
    private static final String USE_RHINO_ENGINE_PROPERTY = "javascript.use_rhino"; //$NON-NLS-1$

    private static final boolean USE_RHINO_ENGINE = 
            JMeterUtils.getPropDefault(USE_RHINO_ENGINE_PROPERTY, false) ||
            getInstance().getEngineByName(NASHORN_ENGINE_NAME) == null;

    
    private static final ThreadLocal<ScriptEngine> NASHORN_ENGINE = new ThreadLocal<ScriptEngine>() {

        @Override
        protected ScriptEngine initialValue() {
            return getInstance().getEngineByName("nashorn");//$NON-NLS-N$
        }
    
    };

    private interface JsEvaluator {
        boolean evaluate(String testElementName, String condition);
    }
    
    private static class RhinoJsEngine implements JsEvaluator {
        @Override
        public boolean evaluate(String testElementName, String condition) {
            boolean result = false;
            // now evaluate the condition using JavaScript
            Context cx = Context.enter();
            try {
                Scriptable scope = cx.initStandardObjects(null);
                Object cxResultObject = cx.evaluateString(scope, condition
                /** * conditionString ** */
                , "<cmd>", 1, null);
                result = computeResultFromString(condition, Context.toString(cxResultObject));
            } catch (Exception e) {
                log.error("{}: error while processing "+ "[{}]", testElementName, condition, e);
            } finally {
                Context.exit();
            }
            return result;
        }
    }
    
    private static class NashornJsEngine implements JsEvaluator {
        @Override
        public boolean evaluate(String testElementName, String condition) {
            try {
                ScriptContext newContext = new SimpleScriptContext();
                newContext.setBindings(NASHORN_ENGINE.get().createBindings(), ScriptContext.ENGINE_SCOPE);
                Object o = NASHORN_ENGINE.get().eval(condition, newContext);
                return computeResultFromString(condition, o.toString());
            } catch (Exception ex) {
                log.error("{}: error while processing [{}]", testElementName, condition, ex);
            }
            return false;
        }
    }
        
    private static JsEvaluator JAVASCRIPT_EVALUATOR = USE_RHINO_ENGINE ? new RhinoJsEngine() : new NashornJsEngine();
    
    /**
     * Initialization On Demand Holder pattern
     */
    private static class LazyHolder {
        public static final ScriptEngineManager INSTANCE = new ScriptEngineManager();
    }
 
    /**
     * @return ScriptEngineManager singleton
     */
    private static ScriptEngineManager getInstance() {
            return LazyHolder.INSTANCE;
    }
    /**
     * constructor
     */
    public IfController() {
        super();
    }

    /**
     * constructor
     * @param condition The condition for this controller
     */
    public IfController(String condition) {
        super();
        this.setCondition(condition);
    }

    /**
     * Condition Accessor - this is gonna be like <code>${count} &lt; 10</code>
     * @param condition The condition for this controller
     */
    public void setCondition(String condition) {
        setProperty(new StringProperty(CONDITION, condition));
    }

    /**
     * Condition Accessor - this is gonna be like <code>${count} &lt; 10</code>
     * @return the condition associated with this controller
     */
    public String getCondition() {
        return getPropertyAsString(CONDITION);
    }

    /**
     * evaluate the condition clause log error if bad condition
     */
    private boolean evaluateCondition(String cond) {
        log.debug("    getCondition() : [{}]", cond);
        return JAVASCRIPT_EVALUATOR.evaluate(getName(), cond);
    }

    /**
     * @param condition
     * @param resultStr
     * @return boolean
     * @throws Exception
     */
    private static boolean computeResultFromString(
            String condition, String resultStr) throws Exception {
        boolean result;
        switch(resultStr) {
            case "false":
                result = false;
                break;
            case "true":
                result = true;
                break;
            default:
                throw new Exception(" BAD CONDITION :: " + condition + " :: expected true or false");
        }
        log.debug("    >> evaluate Condition -  [{}] results is  [{}]", condition, result);
        return result;
    }
    
    
    private static boolean evaluateExpression(String cond) {
        return cond.equalsIgnoreCase("true"); // $NON-NLS-1$
    }

    @Override
    public boolean isDone() {
        // bug 26672 : the isDone result should always be false and not based on the expression evaluation
        // if an IfController ever gets evaluated to false it gets removed from the test tree. 
        // The problem is that the condition might get evaluated to true the next iteration, 
        // which we don't get the opportunity for
        return false;
    }

    /**
     * @see org.apache.jmeter.control.Controller#next()
     */
    @Override
    public Sampler next() {
        // We should only evaluate the condition if it is the first
        // time ( first "iteration" ) we are called.
        // For subsequent calls, we are inside the IfControllerGroup,
        // so then we just pass the control to the next item inside the if control
        boolean result = true;
        if(isEvaluateAll() || isFirst()) {
            result = isUseExpression() ? 
                    evaluateExpression(getCondition())
                    :
                    evaluateCondition(getCondition());
        }

        if (result) {
            return super.next();
        }
        // If-test is false, need to re-initialize indexes
        try {
            initializeSubControllers();
            return nextIsNull();
        } catch (NextIsNullException e1) {
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerEndOfLoop() {
        super.initializeSubControllers();
        super.triggerEndOfLoop();
    }

    public boolean isEvaluateAll() {
        return getPropertyAsBoolean(EVALUATE_ALL,false);
    }

    public void setEvaluateAll(boolean b) {
        setProperty(EVALUATE_ALL,b);
    }

    public boolean isUseExpression() {
        return getPropertyAsBoolean(USE_EXPRESSION, false);
    }

    public void setUseExpression(boolean selected) {
        setProperty(USE_EXPRESSION, selected, false);
    }
    
    @Override
    public void threadStarted() {}
    
    @Override
    public void threadFinished() {
       NASHORN_ENGINE.remove();
    }
}
