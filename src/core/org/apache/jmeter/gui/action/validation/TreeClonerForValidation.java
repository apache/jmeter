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

package org.apache.jmeter.gui.action.validation;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.TreeCloner;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clones the test tree,  skipping test elements that implement {@link Timer} by default.
 * @since 3.0
 */
public class TreeClonerForValidation extends TreeCloner {
    
    private static final Logger log = LoggerFactory.getLogger(TreeClonerForValidation.class);

    /**
     * Number of Threads to configure when running a Thread Group during a validation
     */
    protected static final int VALIDATION_NUMBER_OF_THREADS = JMeterUtils.getPropDefault("testplan_validation.nb_threads_per_thread_group", 1); //$NON-NLS-1$

    /**
     * Ignore or not timers during a Thread Group validation
     */
    protected static final boolean VALIDATION_IGNORE_TIMERS = JMeterUtils.getPropDefault("testplan_validation.ignore_timers", true); //$NON-NLS-1$

    /**
     * Ignore or not Backend during a Thread Group validation
     */
    protected static final boolean VALIDATION_IGNORE_BACKENDS = JMeterUtils.getPropDefault("testplan_validation.ignore_backends", true); //$NON-NLS-1$

    /**
     * Number of iterations to run during a Thread Group validation
     */
    protected static final int VALIDATION_ITERATIONS = JMeterUtils.getPropDefault("testplan_validation.number_iterations", 1); //$NON-NLS-1$
    
    static {
        log.info("Running validation with number of threads:{}, ignoreTimers:{}, number of iterations:{}",
                VALIDATION_NUMBER_OF_THREADS, VALIDATION_IGNORE_TIMERS, VALIDATION_ITERATIONS);
    }

    public TreeClonerForValidation() {
        this(false);
    }

    public TreeClonerForValidation(boolean honourNoThreadClone) {
        super(honourNoThreadClone);
    }

    /**
     * @see org.apache.jmeter.engine.TreeCloner#addNodeToTree(java.lang.Object)
     */
    @Override
    protected Object addNodeToTree(Object node) {
        if((VALIDATION_IGNORE_TIMERS && node instanceof Timer) || 
                (VALIDATION_IGNORE_BACKENDS && node instanceof Backend)) {
            return node; // don't add timer or backend
        } else {
            Object clonedNode = super.addNodeToTree(node);
            if(clonedNode instanceof org.apache.jmeter.threads.ThreadGroup) {
                ThreadGroup tg = (ThreadGroup)clonedNode;
                tg.setNumThreads(VALIDATION_NUMBER_OF_THREADS);
                tg.setScheduler(false);
                tg.setProperty(ThreadGroup.DELAY, 0);
                if(((AbstractThreadGroup)clonedNode).getSamplerController() instanceof LoopController) {
                    ((LoopController)((AbstractThreadGroup)clonedNode).getSamplerController()).setLoops(VALIDATION_ITERATIONS);
                }
            }
            return clonedNode;
        }
    }
}
