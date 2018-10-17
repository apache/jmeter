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
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * Runtime Controller that runs its children until configured Runtime(s) is exceeded
 */
public class RunTime extends GenericController implements Serializable {

    private static final long serialVersionUID = 240L;

    private static final String SECONDS = "RunTime.seconds"; //$NON-NLS-1$

    private long startTime = 0;

    private int loopCount = 0; // for getIterCount

    public RunTime() {
        super();
    }

    public void setRuntime(long seconds) {
        setProperty(new LongProperty(SECONDS, seconds));
    }

    public void setRuntime(String seconds) {
        setProperty(new StringProperty(SECONDS, seconds));
    }

    public long getRuntime() {
        try {
            return Long.parseLong(getPropertyAsString(SECONDS));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public String getRuntimeString() {
        return getPropertyAsString(SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        if (getRuntime() > 0 && !getSubControllers().isEmpty()) {
            return super.isDone();
        }
        return true; // Runtime is zero - no point staying around
    }

    private boolean endOfLoop() {
        return ((System.nanoTime() - startTime)/1000000000L) >= getRuntime();
    }

    @Override
    public Sampler next() {
        if (startTime == 0) {
            startTime = System.nanoTime();
        }
        if (endOfLoop()) {
            reInitialize();// ??
            resetLoopCount();
            return null;
        }
        return super.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Sampler nextIsNull() throws NextIsNullException {
        reInitialize();
        if (endOfLoop()) {
            resetLoopCount();
            return null;
        }
        return next();
    }

    protected void incrementLoopCount() {
        loopCount++;
    }

    protected void resetLoopCount() {
        loopCount = 0;
        startTime = 0;
    }

    /*
     * This is needed for OnceOnly to work like other Loop Controllers
     */
    @Override
    protected int getIterCount() {
        return loopCount + 1;
    }

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
}
