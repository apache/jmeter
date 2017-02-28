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

import javax.swing.JOptionPane;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.util.HeapDumper;
import org.apache.jorphan.util.ThreadDumper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Debug class to show details of the currently selected object
 * Currently shows TestElement and GUI class names
 *
 * Also enables/disables debug for the test element.
 *
 */
public class What extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(What.class);

    private static final Set<String> commandSet;

    static {
        Set<String> commands = new HashSet<>();
        commands.add(ActionNames.WHAT_CLASS);
        commands.add(ActionNames.DEBUG_ON);
        commands.add(ActionNames.DEBUG_OFF);
        commands.add(ActionNames.HEAP_DUMP);
        commands.add(ActionNames.THREAD_DUMP);
        commandSet = Collections.unmodifiableSet(commands);
    }


    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        JMeterTreeNode node= GuiPackage.getInstance().getTreeListener().getCurrentNode();
        TestElement te = (TestElement)node.getUserObject();
        if (ActionNames.WHAT_CLASS.equals(e.getActionCommand())){
            String guiClassName = te.getPropertyAsString(TestElement.GUI_CLASS);
            System.out.println(te.getClass().getName());
            System.out.println(guiClassName);
            if (log.isInfoEnabled()) {
                log.info("TestElement: {}, guiClassName: {}", te.getClass(), guiClassName);
            }
        } else if (ActionNames.DEBUG_ON.equals(e.getActionCommand())) {
            final String loggerName = te.getClass().getName();
            Configurator.setAllLevels(loggerName, Level.DEBUG);
            log.info("Log level set to DEBUG for {}", loggerName);
        } else if (ActionNames.DEBUG_OFF.equals(e.getActionCommand())){
            final String loggerName = te.getClass().getName();
            Configurator.setAllLevels(loggerName, Level.INFO);
            log.info("Log level set to INFO for {}", loggerName);
        } else if (ActionNames.HEAP_DUMP.equals(e.getActionCommand())){
            try {
                String s = HeapDumper.dumpHeap();
                JOptionPane.showMessageDialog(null, "Created "+s, "HeapDump", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) { // NOSONAR We show cause in message
                JOptionPane.showMessageDialog(null, ex.toString(), "HeapDump", JOptionPane.ERROR_MESSAGE);
            }
        } else if (ActionNames.THREAD_DUMP.equals(e.getActionCommand())){
            try {
                String s = ThreadDumper.threadDump();
                JOptionPane.showMessageDialog(null, "Created "+s, "ThreadDump", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) { // NOSONAR We show cause in message
                JOptionPane.showMessageDialog(null, ex.toString(), "ThreadDump", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Provide the list of Action names that are available in this command.
     */
    @Override
    public Set<String> getActionNames() {
        return commandSet;
    }
}
