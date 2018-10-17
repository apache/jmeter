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

package org.apache.jmeter.timers;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.BeanShellTestElement;
import org.apache.jorphan.util.JMeterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GUIMenuSortOrder(Integer.MAX_VALUE)
public class BeanShellTimer extends BeanShellTestElement implements Cloneable, Timer, TestBean {
    private static final Logger log = LoggerFactory.getLogger(BeanShellTimer.class);

    private static final long serialVersionUID = 5;

    // can be specified in jmeter.properties
    private static final String INIT_FILE = "beanshell.timer.init"; //$NON-NLS-1$

    @Override
    protected String getInitFileProperty() {
        return INIT_FILE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long delay() {
        String ret="0";
        final BeanShellInterpreter bshInterpreter = getBeanShellInterpreter();
        if (bshInterpreter == null) {
            log.error("BeanShell not found");
            return 0;
        }
        try {
            Object o = processFileOrScript(bshInterpreter);
            if (o != null) { 
                ret=o.toString(); 
            }
        } catch (JMeterException e) {
            if (log.isWarnEnabled()) {
                log.warn("Problem in BeanShell script. {}", e.toString());
            }
        }
        try {
            return Long.decode(ret).longValue();
        } catch (NumberFormatException e){
            log.warn("Number format exception while decoding number: '{}'", ret);
            return 0;
        }
    }
    
    @Override
    public Object clone() {
        return super.clone();
    }
}
