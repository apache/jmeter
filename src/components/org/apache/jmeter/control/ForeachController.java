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

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ForeachController that iterates over a list of variables named XXXX_NN stored in {@link JMeterVariables}
 * where NN is a number starting from 1 to number of occurrences.
 * This list of variable is usually set by PostProcessor (Regexp PostProcessor or {@link org.apache.jmeter.extractor.HtmlExtractor})
 * Iteration can take the full list or only a subset (configured through indexes)
 *
 */
@GUIMenuSortOrder(5)
public class ForeachController extends GenericController implements Serializable, IteratingController {

    private static final Logger log = LoggerFactory.getLogger(ForeachController.class);

    private static final long serialVersionUID = 241L;

    private static final String INPUTVAL = "ForeachController.inputVal";// $NON-NLS-1$

    private static final String START_INDEX = "ForeachController.startIndex";// $NON-NLS-1$

    private static final String END_INDEX = "ForeachController.endIndex";// $NON-NLS-1$

    private static final String RETURNVAL = "ForeachController.returnVal";// $NON-NLS-1$

    private static final String USE_SEPARATOR = "ForeachController.useSeparator";// $NON-NLS-1$

    private static final String INDEX_DEFAULT_VALUE = ""; // start/end index default value for string getters and setters

    private int loopCount = 0;

    private boolean breakLoop;

    private static final String DEFAULT_SEPARATOR = "_";// $NON-NLS-1$

    public ForeachController() {
    }

    /**
     * @param startIndex Start index  of loop
     */
    public void setStartIndex(String startIndex) {
        setProperty(START_INDEX, startIndex, INDEX_DEFAULT_VALUE);
    }

    /**
     * @return start index of loop
     */
    private int getStartIndex() {
        // Although the default is not the same as for the string value, it is only used internally
        return getPropertyAsInt(START_INDEX, 0);
    }

    /**
     * @return start index of loop as String
     */
    public String getStartIndexAsString() {
        return getPropertyAsString(START_INDEX, INDEX_DEFAULT_VALUE);
    }

    /**
     * @param endIndex End index  of loop
     */
    public void setEndIndex(String endIndex) {
        setProperty(END_INDEX, endIndex, INDEX_DEFAULT_VALUE);
    }

    /**
     * @return end index of loop
     */
    private int getEndIndex() {
        // Although the default is not the same as for the string value, it is only used internally
        return getPropertyAsInt(END_INDEX, Integer.MAX_VALUE);
    }

    /**
     * @return end index of loop
     */
    public String getEndIndexAsString() {
        return getPropertyAsString(END_INDEX, INDEX_DEFAULT_VALUE);
    }

    public void setInputVal(String inputValue) {
        setProperty(new StringProperty(INPUTVAL, inputValue));
    }

    private String getInputVal() {
        getProperty(INPUTVAL).recoverRunningVersion(null);
        return getInputValString();
    }

    public String getInputValString() {
        return getPropertyAsString(INPUTVAL);
    }

    public void setReturnVal(String inputValue) {
        setProperty(new StringProperty(RETURNVAL, inputValue));
    }

    private String getReturnVal() {
        getProperty(RETURNVAL).recoverRunningVersion(null);
        return getReturnValString();
    }

    public String getReturnValString() {
        return getPropertyAsString(RETURNVAL);
    }

    private String getSeparator() {
        return getUseSeparator() ? DEFAULT_SEPARATOR : "";// $NON-NLS-1$
    }

    public void setUseSeparator(boolean b) {
        setProperty(new BooleanProperty(USE_SEPARATOR, b));
    }

    public boolean getUseSeparator() {
        return getPropertyAsBoolean(USE_SEPARATOR, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        if (loopCount >= getEndIndex()) {
            return true;
        }
        JMeterContext context = getThreadContext();
        StringBuilder builder = new StringBuilder(
                getInputVal().length()+getSeparator().length()+3);
        String inputVariable = 
                builder.append(getInputVal())
                .append(getSeparator())
                .append(Integer.toString(loopCount+1)).toString();
        final JMeterVariables variables = context.getVariables();
        final Object currentVariable = variables.getObject(inputVariable);
        if (currentVariable != null) {
            variables.putObject(getReturnVal(), currentVariable);
            if (log.isDebugEnabled()) {
                log.debug("{} : Found in vars:{}, isDone:{}",
                        getName(), inputVariable, Boolean.FALSE);

            }
            return false;
        }
        return super.isDone();
    }

    /**
     * Tests that JMeterVariables contain inputVal_<count>, if not we can stop iterating
     */
    private boolean endOfArguments() {
        JMeterContext context = getThreadContext();
        String inputVariable = getInputVal() + getSeparator() + (loopCount + 1);
        if (context.getVariables().getObject(inputVariable) != null) {
            if(log.isDebugEnabled()) {
                log.debug("{} : Found in vars:{}, not end of Arguments", 
                        getName(), inputVariable);
            }
            return false;
        }
        if(log.isDebugEnabled()) {
            log.debug("{} : Did not find in vars:{}, End of Arguments reached", 
                    getName(), inputVariable);
        }
        return true;
    }

    // Prevent entry if nothing to do
    @Override
    public Sampler next() {
        try {
            if (breakLoop || emptyList()) {
                resetBreakLoop();
                reInitialize();
                resetLoopCount();
                return null;
            }
            return super.next();
        } finally {
            updateIterationIndex(getName(), loopCount);
        }
    }

    /**
     * Check if there are any matching entries
     *
     * @return whether any entries in the list
     */
    private boolean emptyList() {
        JMeterContext context = getThreadContext();

        StringBuilder builder = new StringBuilder(
                getInputVal().length()+getSeparator().length()+3);
        String inputVariable = 
                builder.append(getInputVal())
                .append(getSeparator())
                .append(Integer.toString(loopCount+1)).toString();
        if (context.getVariables().getObject(inputVariable) != null) {
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("{} No entries found - null first entry: {}", 
                    getName(), inputVariable);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Sampler nextIsNull() throws NextIsNullException {
        reInitialize();
        // Conditions to reset the loop count
        if (breakLoop
                || endOfArguments() // no more variables to iterate
                || loopCount >= getEndIndex() // we reached end index
                ) {
            resetBreakLoop();
            resetLoopCount();
            return null;
        }
        return next();
    }

    protected void incrementLoopCount() {
        loopCount++;
    }

    protected void resetLoopCount() {
        loopCount = getStartIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getIterCount() {
        return loopCount + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reInitialize() {
        setFirst(true);
        resetCurrent();
        incrementLoopCount();
        recoverRunningVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerEndOfLoop() {
        super.triggerEndOfLoop();
        resetLoopCount();
    }

    /**
     * Reset loopCount to Start index
     * @see org.apache.jmeter.control.GenericController#initialize()
     */
    @Override
    public void initialize() {
        super.initialize();
        loopCount = getStartIndex();
    }

    @Override
    public void startNextLoop() {
        reInitialize();
    }

    private void resetBreakLoop() {
        if(breakLoop) {
            breakLoop = false;
        }
    }

    @Override
    public void breakLoop() {
        breakLoop = true;
        setFirst(true);
        resetCurrent();
        resetLoopCount();
        recoverRunningVersion();
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        reInitialize();
        resetLoopCount();
    }
}
