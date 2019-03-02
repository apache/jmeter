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
package org.apache.jmeter.gui.action;

import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * Naming policy applied to JMeter Tree nodes :
 * <ul>
 * <li>on creation through {@link TreeNodeNamingPolicy#nameOnCreation(JMeterTreeNode)}</li>
 * <li>By applying naming policy on Controller child nodes through {@link TreeNodeNamingPolicy#resetState(JMeterTreeNode)}
    and {@link TreeNodeNamingPolicy#rename(JMeterTreeNode, JMeterTreeNode, int)}</li>
 * </ul> 
 * @since 3.2
 */
public interface TreeNodeNamingPolicy {

    /**
     * Called by Apply Naming Policy popup menu on TransactionController nodes
     * Rename childNode based on custom policy 
     * @param parentNode Parent node
     * @param childNode Child node
     * @param index index of child node
     */
    void rename(JMeterTreeNode parentNode, JMeterTreeNode childNode, int index);

    /**
     * Called within Apply Naming Policy popup menu on TransactionController nodes to 
     * init the naming process. 
     * @param parentNode {@link JMeterTreeNode} Parent of nodes that will be renamed
     */
    void resetState(JMeterTreeNode parentNode);
    
    /**
     * @param node {@link JMeterTreeNode} node that has been added to JMeter Tree node
     */
    void nameOnCreation(JMeterTreeNode node);
}
