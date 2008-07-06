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

package org.apache.jmeter.extractor;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.BSFTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class BSFPostProcessor extends BSFTestElement implements Cloneable, PostProcessor, TestBean
{
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 232L;

    public void process(){
        try {
            BSFManager mgr = getManager();
            if (mgr == null) { return; }
            processFileOrScript(mgr);
            mgr.terminate();
        } catch (BSFException e) {
            log.warn("Problem in BSF script "+e);
        }
    }
}
