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

package org.apache.jmeter.gui.action;

import com.google.auto.service.AutoService;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.ChangeElement;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows to change Sampler implementation
 */
@AutoService(Command.class)
public class ChangeSampler extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(ChangeSampler.class);
    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.CHANGE_SAMPLER);
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        String name = ((Component) e.getSource()).getName();
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode currentNode = guiPackage.getTreeListener().getCurrentNode();
        if (!(currentNode.getUserObject() instanceof Sampler)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            guiPackage.updateCurrentNode();
            Sampler sampler = (Sampler) guiPackage.createTestElement(name);
            ChangeElement.sampler(sampler, guiPackage, currentNode);
        } catch (Exception err) {
            Toolkit.getDefaultToolkit().beep();
            log.error("Failed to change sampler", err);
        }
    }
}
