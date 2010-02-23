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

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;

/**
 *
 * Debug class to show details of the currently selected object
 * Currently shows TestElement and GUI class names
 *
 * Also enables/disables debug for the test element.
 *
 */
public class What implements Command {

    private static final Set<String> commandSet;

    static {
        HashSet<String> commands = new HashSet<String>();
        commands.add(ActionNames.WHAT_CLASS);
        commands.add(ActionNames.DEBUG_ON);
        commands.add(ActionNames.DEBUG_OFF);
        commandSet = Collections.unmodifiableSet(commands);
    }


    public void doAction(ActionEvent e) throws IllegalUserActionException {
        JMeterTreeNode node= GuiPackage.getInstance().getTreeListener().getCurrentNode();
        TestElement te = (TestElement)node.getUserObject();
        if (ActionNames.WHAT_CLASS.equals(e.getActionCommand())){
            String guiClassName = te.getPropertyAsString(TestElement.GUI_CLASS);
            System.out.println(te.getClass().getName());
            System.out.println(guiClassName);
        } else if (ActionNames.DEBUG_ON.equals(e.getActionCommand())){
            LoggingManager.setPriorityFullName("DEBUG",te.getClass().getName());//$NON-NLS-1$
        } else if (ActionNames.DEBUG_OFF.equals(e.getActionCommand())){
            LoggingManager.setPriorityFullName("INFO",te.getClass().getName());//$NON-NLS-1$
        }
    }

    /**
     * Provide the list of Action names that are available in this command.
     */
    public Set<String> getActionNames() {
        return commandSet;
    }
}
