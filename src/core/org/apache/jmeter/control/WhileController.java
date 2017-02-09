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

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @see TestWhileController for unit tests

public class WhileController extends GenericController implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(WhileController.class);

    private static final long serialVersionUID = 233L;

    private static final String CONDITION = "WhileController.condition"; // $NON-NLS-1$

    public WhileController() {
    }

    /*
     * Evaluate the condition, which can be:
     * blank or LAST = was the last sampler OK?
     * otherwise, evaluate the condition to see if it is not "false"
     * If blank, only evaluate at the end of the loop
     *
     * Must only be called at start and end of loop
     *
     * @param loopEnd - are we at loop end?
     * @return true means OK to continue
     */
    private boolean endOfLoop(boolean loopEnd) {
        String cnd = getCondition().trim();
        log.debug("Condition string: '{}'", cnd);
        boolean res;
        // If blank, only check previous sample when at end of loop
        if ((loopEnd && cnd.length() == 0) || "LAST".equalsIgnoreCase(cnd)) {// $NON-NLS-1$
            JMeterVariables threadVars = JMeterContextService.getContext().getVariables();
            res = "false".equalsIgnoreCase(threadVars.get(JMeterThread.LAST_SAMPLE_OK));// $NON-NLS-1$
        } else {
            // cnd may be null if next() called us
            res = "false".equalsIgnoreCase(cnd);// $NON-NLS-1$
        }
        log.debug("Condition value: '{}'", res);
        return res;
    }

    /**
     * Only called at End of Loop
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected Sampler nextIsNull() throws NextIsNullException {
        reInitialize();
        if (endOfLoop(true)){
            return null;
        }
        return next();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerEndOfLoop() {
        super.triggerEndOfLoop();
        endOfLoop(true);
    }

    /**
     * This skips controller entirely if the condition is false on first entry.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Sampler next(){
        if (isFirst()){
            if (endOfLoop(false)){
                return null;
            }
        }
        return super.next();
    }

    /**
     * @param string
     *            the condition to save
     */
    public void setCondition(String string) {
        log.debug("setCondition({})", string);
        setProperty(new StringProperty(CONDITION, string));
    }

    /**
     * @return the condition
     */
    public String getCondition() {
        JMeterProperty prop=getProperty(CONDITION);
        prop.recoverRunningVersion(this);
        return prop.getStringValue();
    }
}
