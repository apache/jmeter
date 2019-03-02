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
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JSR223TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compile JSR223 Test Element that use Compilable script language
 * @since 5.1
 */
public class CompileJSR223TestElements extends AbstractAction implements MenuCreator {
    private static final Logger log = LoggerFactory.getLogger(CompileJSR223TestElements.class);

    private static final MessageFormat MESSAGE_FORMAT =
            new MessageFormat(JMeterUtils.getResString("compilation_errors")); // //$NON-NLS-1$
    /**
     *
     */
    private static class JSR223TestElementCompilerVisitor implements HashTreeTraverser {
        private int elementsWithCompilationErrors = 0;
        public JSR223TestElementCompilerVisitor() {
            super();
        }
        @Override
        public void addNode(Object object, HashTree subTree) {
            JMeterTreeNode treeNode = (JMeterTreeNode) object;
            Object userObject = treeNode.getUserObject();
            treeNode.setMarkedBySearch(false);
            if (treeNode.isEnabled() && (userObject instanceof JSR223TestElement)) {
                JSR223TestElement element = (JSR223TestElement) userObject;
                TestBeanHelper.prepare(element);
                try {
                    log.info("Compiling {}", element.getName());
                    if(!element.compile()) {
                        elementsWithCompilationErrors++;
                        treeNode.setMarkedBySearch(true);
                    } else {
                        log.info("Compilation succeeded for {}", element.getName());
                    }
                } catch (Exception e) {
                    treeNode.setMarkedBySearch(true);
                    log.error("Error compiling test element {}", element.getName(), e);
                }
            }
        }

        @Override
        public void subtractNode() {
            // NOOP
        }
        @Override
        public void processPath() {
            // NOOP
        }
        /**
         * @return the elementsWithCompilationErrors
         */
        public int getElementsWithCompilationErrors() {
            return elementsWithCompilationErrors;
        }
    }

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.COMPILE_JSR223);
    }

    public CompileJSR223TestElements() {
        super();
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        HashTree wholeTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
        JSR223TestElementCompilerVisitor visitor = new JSR223TestElementCompilerVisitor();
        wholeTree.traverse(visitor);
        GuiPackage.getInstance().getMainFrame().repaint();
        if (visitor.getElementsWithCompilationErrors()>0) {
            JMeterUtils.reportErrorToUser(MESSAGE_FORMAT.format(new Object[]{Integer.valueOf(visitor.getElementsWithCompilationErrors())}));
        }
    }


    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if(location == MENU_LOCATION.TOOLS) {

            JMenuItem menuItemIC = new JMenuItem(
                    JMeterUtils.getResString("compile_menu"), KeyEvent.VK_UNDEFINED);
            menuItemIC.setName(ActionNames.COMPILE_JSR223);
            menuItemIC.setActionCommand(ActionNames.COMPILE_JSR223);
            menuItemIC.setAccelerator(null);
            menuItemIC.addActionListener(ActionRouter.getInstance());

            return new JMenuItem[]{menuItemIC};
        }
        return new JMenuItem[0];
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
        // NOOP
    }
}
