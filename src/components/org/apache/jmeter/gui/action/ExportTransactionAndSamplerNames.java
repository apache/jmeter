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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export transactions names for web report
 * @since 3.3
 */
public class ExportTransactionAndSamplerNames extends AbstractAction implements MenuCreator {
    private static final Logger log = LoggerFactory.getLogger(ExportTransactionAndSamplerNames.class);
    
    private static final String TRANSACTIONS_REGEX_PATTERN = 
            JMeterUtils.getPropDefault("jmeter.reportgenerator.exported_transactions_pattern", 
                    "[a-zA-Z0-9_\\-{}\\$\\.]*[-_][0-9]*");
    
    private static final Pattern TRANSACTIONS_REGEX = 
            Pattern.compile(TRANSACTIONS_REGEX_PATTERN);
    
    private static final Set<String> commands = new HashSet<>();

    private static final String EXPORT_NAMES = "export_transactions_names_action";
    

    static {
        commands.add(EXPORT_NAMES);
    }

    /**
     * Visitor to collect nodes matching the name
     */
    private static class SamplerAndTransactionNameVisitor implements HashTreeTraverser {
        private Set<String> listOfTransactions = new TreeSet<>();
        public SamplerAndTransactionNameVisitor() {
            super();
        }
        @Override
        public void addNode(Object object, HashTree subTree) {
            JMeterTreeNode treeNode = (JMeterTreeNode) object;
            Object userObject = treeNode.getUserObject();
            
            if (userObject instanceof TransactionController
                    || (userObject instanceof Sampler && !(userObject instanceof TestAction) 
                            && !(userObject instanceof DebugSampler))) {
                Matcher matcher = TRANSACTIONS_REGEX.matcher(((TestElement)userObject).getName());
                if(!matcher.matches()) {
                    listOfTransactions.add(((TestElement)userObject).getName());
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
         * @return the listOfTransactions
         */
        public Set<String> getListOfTransactions() {
            return listOfTransactions;
        }
    }


    public ExportTransactionAndSamplerNames() {
        super();
    }

    /**
     * @see Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        HashTree wholeTree = GuiPackage.getInstance().getTreeModel().getTestPlan();
        SamplerAndTransactionNameVisitor visitor = new SamplerAndTransactionNameVisitor();
        wholeTree.traverse(visitor);
        Set<String> sampleNames = visitor.getListOfTransactions();
        if(sampleNames.isEmpty()) {
            log.warn("No transaction exported using regexp '{}', modify property '{}' to fix this problem",
                    TRANSACTIONS_REGEX_PATTERN, "report_transactions_pattern");
            showResult("No transaction exported using regexp '"
                    +TRANSACTIONS_REGEX_PATTERN
                    +"', modify property 'report_transactions_pattern' to fix this problem");
        } else {
            StringBuilder builder = new StringBuilder();
            for (String sampleName : sampleNames) {
                builder.append(sampleName).append('|');
            }
            builder.setLength(builder.length()-1);
            String result = builder.toString();
            log.info("Exported transactions: jmeter.reportgenerator.exporter.html.series_filter=^({})(-success|-failure)?$", 
                    result);

            showResult("jmeter.reportgenerator.exporter.html.series_filter=^("
                    +result
                    +")(-success|-failure)?$");
            
        }
    }

    /**
     * Display result in popup
     * @param result String 
     */
    private static final void showResult(String result) {
        EscapeDialog messageDialog = new EscapeDialog(GuiPackage.getInstance().getMainFrame(),
                JMeterUtils.getResString("export_transactions_title"), true); //$NON-NLS-1$
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(new JLabel(
                JMeterUtils.getResString("export_transactions_exported_property"), 
                SwingConstants.CENTER), BorderLayout.NORTH);//$NON-NLS-1$
        JSyntaxTextArea syntaxTextArea = JSyntaxTextArea.getInstance(10, 80, true);
        syntaxTextArea.setText(result);
        syntaxTextArea.setCaretPosition(0);
        contentPane.add(JTextScrollPane.getInstance(syntaxTextArea), BorderLayout.CENTER);
        messageDialog.pack();
        ComponentUtil.centerComponentInComponent(GuiPackage.getInstance().getMainFrame(), messageDialog);
        SwingUtilities.invokeLater(() -> messageDialog.setVisible(true));
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
        if(location == MENU_LOCATION.HELP) {
            
            JMenuItem menuItemIC = new JMenuItem(
                    JMeterUtils.getResString("export_transactions_menu"), KeyEvent.VK_UNDEFINED);
            menuItemIC.setName(ExportTransactionAndSamplerNames.EXPORT_NAMES);
            menuItemIC.setActionCommand(ExportTransactionAndSamplerNames.EXPORT_NAMES);
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
