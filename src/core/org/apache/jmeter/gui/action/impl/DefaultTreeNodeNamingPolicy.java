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

package org.apache.jmeter.gui.action.impl;

import java.text.DecimalFormat;

import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.gui.action.TreeNodeNamingPolicy;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Default implementation of {@link TreeNodeNamingPolicy}
 * @since 3.2
 */
public class DefaultTreeNodeNamingPolicy implements TreeNodeNamingPolicy {
    private static final String PREFIX = JMeterUtils.getPropDefault("naming_policy.prefix", ""); 
    private static final String SUFFIX = JMeterUtils.getPropDefault("naming_policy.suffix", ""); 
    private int numberOfChildren;
    private int index;
    private DecimalFormat formatter;


    /**
     * @see org.apache.jmeter.gui.action.TreeNodeNamingPolicy#rename(org.apache.jmeter.gui.tree.JMeterTreeNode, org.apache.jmeter.gui.tree.JMeterTreeNode, int)
     */
    @Override
    public void rename(JMeterTreeNode parentNode, JMeterTreeNode childNode, int iterationIndex) {
        if(childNode.getUserObject() instanceof TransactionController ||
                childNode.getUserObject() instanceof Sampler) {
            childNode.setName(parentNode.getName()+"-"+formatter.format(index));
            index++;
        }
    }

    /**
     * @see org.apache.jmeter.gui.action.TreeNodeNamingPolicy#resetState(org.apache.jmeter.gui.tree.JMeterTreeNode)
     */
    @Override
    public void resetState(JMeterTreeNode rootNode) {
        this.numberOfChildren = rootNode.getChildCount();
        this.index = 0;
        int numberOfDigits = String.valueOf(numberOfChildren).length();
        StringBuilder formatSB = new StringBuilder(numberOfDigits);
        for (int i=0; i<numberOfDigits;i++) {
            formatSB.append("0");
        }
        this.formatter = new DecimalFormat(formatSB.toString());
    }

    @Override
    public void nameOnCreation(JMeterTreeNode node) {
        if(node.getName().isEmpty()) {
            node.setName(((TestElement)node.getUserObject()).getClass().getSimpleName());
        }
        node.setName(PREFIX+node.getName()+SUFFIX);
    }
}
