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

package org.apache.jmeter.validation;

import org.apache.jmeter.control.ThroughputController;
import org.apache.jmeter.gui.action.validation.TreeClonerForValidation;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Clones the test tree, modifying throughput controller percentage
 * @since 3.1
 */
public class ComponentTreeClonerForValidation extends TreeClonerForValidation {
    
    /**
     * For 100% on ThroughputController
     */
    protected static final boolean VALIDATION_TPC_FORCE_100_PERCENT = JMeterUtils.getPropDefault("testplan_validation.tpc_force_100_pct", false); //$NON-NLS-1$

    public ComponentTreeClonerForValidation() {
        this(false);
    }

    public ComponentTreeClonerForValidation(boolean honourNoThreadClone) {
        super(honourNoThreadClone);
    }

    /**
     * @see org.apache.jmeter.engine.TreeCloner#addNodeToTree(java.lang.Object)
     */
    @Override
    protected Object addNodeToTree(Object node) {
        Object clonedNode = super.addNodeToTree(node);
        if (VALIDATION_TPC_FORCE_100_PERCENT && clonedNode instanceof ThroughputController) {
            ThroughputController tc = (ThroughputController) clonedNode;
            if(tc.getStyle() == ThroughputController.BYPERCENT) {
                tc.setPercentThroughput(100);
            }
        }
        return clonedNode;
    }
}
