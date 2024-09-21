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

package org.apache.jmeter.modifiers;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.BSFTestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BSFPreProcessor extends BSFTestElement implements Cloneable, PreProcessor, TestBean
{
    private static final Logger log = LoggerFactory.getLogger(BSFPreProcessor.class);

    private static final long serialVersionUID = 233L;

    @Override
    public void process(){
        BSFManager mgr =null;
        try {
            mgr = getManager();
            if (mgr == null) {
                return;
            }
            processFileOrScript(mgr);
        } catch (BSFException e) {
            if (log.isWarnEnabled()) {
                log.warn("Problem in BSF script. {}", e.toString());
            }
        } finally {
            if (mgr != null) {
                mgr.terminate();
            }
        }
    }

    @Override
    @SuppressWarnings("RedundantOverride")
    public Object clone() {
        return super.clone();
    }
}
