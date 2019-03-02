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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordController extends LogicControllerGui implements ActionListener {
    private static final long serialVersionUID = 241L;
    
    private static final Logger log = LoggerFactory.getLogger(RecordController.class);

    /**
     * Clear the recorded samples
     */
    private JButton clearButton;
    
    public RecordController() {
        super();
        init();
    }
    
    @Override
    public String getLabelResource() {
        return "record_controller_title"; // $NON-NLS-1$
    }

    @Override
    public TestElement createTestElement() {
        RecordingController con = new RecordingController();
        super.configureTestElement(con);
        return con;
    }
    
    private void init() {
       
        JPanel panel = new JPanel();
        
        clearButton = new JButton(JMeterUtils.getResString("record_controller_clear_samples")); //$NON-NLS-1$
        clearButton.addActionListener(this);
        panel.add(clearButton);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        add(panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == clearButton) {
            GuiPackage guiPackage = GuiPackage.getInstance();
            JMeterTreeNode currentNode = guiPackage.getTreeListener().getCurrentNode();
            if (!(currentNode.getUserObject() instanceof org.apache.jmeter.protocol.http.control.RecordingController)) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            
            try {
                guiPackage.updateCurrentNode();
                JMeterTreeModel treeModel = guiPackage.getTreeModel();
                int childCount = currentNode.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    JMeterTreeNode node = (JMeterTreeNode) currentNode.getChildAt(0);
                    treeModel.removeNodeFromParent(node);
                }
            } catch (Exception err) {
                Toolkit.getDefaultToolkit().beep();
                log.error("Error while removing recorded samples", err);
            }
        }
    }
}
