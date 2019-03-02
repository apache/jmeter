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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterTreeNodeTransferable;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Copy menu command
 */
public class Copy extends AbstractAction {
    private static final Logger log = LoggerFactory.getLogger(Copy.class);

    private static final HashSet<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.COPY);
    }

    /*
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {
        JMeterTreeListener treeListener = GuiPackage.getInstance().getTreeListener();
        JMeterTreeNode[] nodes = treeListener.getSelectedNodes();
        nodes = keepOnlyAncestors(nodes);
        nodes = cloneTreeNodes(nodes);
        setCopiedNodes(nodes);
    }

    public static JMeterTreeNode[] getCopiedNodes() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard.isDataFlavorAvailable(JMeterTreeNodeTransferable.JMETER_TREE_NODE_ARRAY_DATA_FLAVOR)) {
            try {
                return (JMeterTreeNode[]) clipboard.getData(JMeterTreeNodeTransferable.JMETER_TREE_NODE_ARRAY_DATA_FLAVOR);
            } catch (Exception ex) {
                log.error("Clipboard node read error: {}", ex.getMessage(), ex);
                JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(),
                        JMeterUtils.getResString("clipboard_node_read_error")+":\n" + ex.getLocalizedMessage(),  //$NON-NLS-1$  //$NON-NLS-2$
                        JMeterUtils.getResString("error_title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            }
        }
        return null;
    }

    public static JMeterTreeNode cloneTreeNode(JMeterTreeNode node) {
        JMeterTreeNode treeNode = (JMeterTreeNode) node.clone();
        treeNode.setUserObject(((TestElement) node.getUserObject()).clone());
        cloneChildren(treeNode, node);
        return treeNode;
    }

    /**
     * If a child and one of its ancestors are selected : only keep the ancestor
     * @param currentNodes JMeterTreeNode[]
     * @return JMeterTreeNode[]
     */
    static JMeterTreeNode[] keepOnlyAncestors(JMeterTreeNode[] currentNodes) {
        List<JMeterTreeNode> nodes = new ArrayList<>();
        for (int i = 0; i < currentNodes.length; i++) {
            boolean exclude = false;
            for (int j = 0; j < currentNodes.length; j++) {
                if(i!=j && currentNodes[i].isNodeAncestor(currentNodes[j])) {
                    exclude = true;
                    break;
                }
            }

            if(!exclude) {
                nodes.add(currentNodes[i]);
            }
        }

        return nodes.toArray(new JMeterTreeNode[nodes.size()]);
    }

    public static void setCopiedNodes(JMeterTreeNode[] nodes) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            JMeterTreeNodeTransferable transferable = new JMeterTreeNodeTransferable();
            transferable.setTransferData(nodes);
            clipboard.setContents(transferable, null);
        } catch (Exception ex) {
            log.error("Clipboard node read error: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("clipboard_node_read_error")+":\n" + ex.getLocalizedMessage(), //$NON-NLS-1$ //$NON-NLS-2$
                    JMeterUtils.getResString("error_title"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
        }
    }

    public static JMeterTreeNode[] cloneTreeNodes(JMeterTreeNode[] nodes) {
        JMeterTreeNode[] treeNodes = new JMeterTreeNode[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            treeNodes[i] = cloneTreeNode(nodes[i]);
        }
        return treeNodes;
    }

    private static void cloneChildren(JMeterTreeNode to, JMeterTreeNode from) {
        Enumeration<?> enumFrom = from.children();
        while (enumFrom.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) enumFrom.nextElement();
            JMeterTreeNode childClone = (JMeterTreeNode) child.clone();
            childClone.setUserObject(((TestElement) child.getUserObject()).clone());
            to.add(childClone);
            cloneChildren((JMeterTreeNode) to.getLastChild(), child);
        }
    }
}
