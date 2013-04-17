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

package org.apache.jmeter.gui.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class JMeterTreeTransferHandler extends TransferHandler {
    
    private static final long serialVersionUID = 8560957372186260765L;

    private static final Logger LOG = LoggingManager.getLoggerForClass();
  
    private DataFlavor nodeFlavor;
    private DataFlavor[] jMeterTreeNodeDataFlavors = new DataFlavor[1];

    public JMeterTreeTransferHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + JMeterTreeNode[].class.getName() + "\"";
            nodeFlavor = new DataFlavor(mimeType);
            jMeterTreeNodeDataFlavors[0] = nodeFlavor;
        }
        catch (ClassNotFoundException e) {
            LOG.error("Class Not Found", e);
        }
    }

    
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }
    

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
           
            //TODO : deal with all the selected nodes
            JMeterTreeNode node = (JMeterTreeNode) paths[0].getLastPathComponent();
            
            return new NodesTransferable(new JMeterTreeNode[] {node});
        }
        
        return null;
    }
   
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }
        
        // the tree accepts a jmx file 
        DataFlavor[] flavors = support.getDataFlavors();
        for (int i = 0; i < flavors.length; i++) {
            // Check for file lists specifically
            if (flavors[i].isFlavorJavaFileListType()) {
                return true;
            }
        }

        // or a treenode from the same tree
        if (!support.isDataFlavorSupported(nodeFlavor)) {
            return false;
        }
        
        // the copy is disabled
        int action = support.getDropAction();
        if(action != MOVE) {
            return false;
        }
        
        support.setShowDropLocation(true);

        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        JTree tree = (JTree) support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for (int i = 0; i < selRows.length; i++) {
            if (selRows[i] == dropRow) {
                return false;
            }
        }
        
        TreePath dest = dl.getPath();
        JMeterTreeNode target = (JMeterTreeNode) dest.getLastPathComponent();
        
        // TestPlan and WorkBench are the only children of the root
        if(target.isRoot()) {
            return false;
        }
        
        TreePath path = tree.getPathForRow(selRows[0]);
        JMeterTreeNode draggedNode = (JMeterTreeNode) path.getLastPathComponent();
        
        // Do not allow a non-leaf node to be moved into one of its children
        if (draggedNode.getChildCount() > 0
                && target.isNodeAncestor(draggedNode)) {
            return false;
        }
        
        // re-use node association logic
        return MenuFactory.canAddTo(target, new JMeterTreeNode[] { draggedNode });
    }


    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        
        // deal with the jmx files
        GuiPackage guiInstance = GuiPackage.getInstance();
        DataFlavor[] flavors = support.getDataFlavors();
        Transferable t = support.getTransferable();
        for (int i = 0; i < flavors.length; i++) {
            // Check for file lists specifically
            if (flavors[i].isFlavorJavaFileListType()) {
                try {
                    return guiInstance.getMainFrame().openJmxFilesFromDragAndDrop(t);
                }
                catch (Exception e) {
                    LOG.error("Drop file failed", e);
                }
                return false;
            }
        }
        
        // Extract transfer data.
        JMeterTreeNode[] nodes = null;
        try {
            nodes = (JMeterTreeNode[]) t.getTransferData(nodeFlavor);
        }
        catch (Exception e) {
            LOG.error("Unsupported Flavor in Transferable", e);
        }

        if(nodes == null || nodes.length == 0) {
            return false;
        }
        
        // Get drop location and mode
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath dest = dl.getPath();
        JMeterTreeNode target = (JMeterTreeNode) dest.getLastPathComponent();
       
        //TODO : deal with all the selected nodes
        JMeterTreeNode draggedNode = nodes[0];
        
        int index = dl.getChildIndex();
        if (index == -1) { // drop mode is ON
            index = target.getChildCount();
            if(draggedNode.getParent() == target) {
                //when the target is the current parent of the node being dragged
                // re-add it as the last child
                index--;
            }
        }
        else if(draggedNode.getParent() == target) { // insert mode
            if(guiInstance.getTreeModel().getIndexOfChild(target, draggedNode) < index) {
                index--;
            }
        }
        
        // remove - add the nodes
        for (int i = 0; i < nodes.length; i++) {
            guiInstance.getTreeModel().removeNodeFromParent(nodes[i]);
            guiInstance.getTreeModel().insertNodeInto(nodes[i], target, index);
        }
        
        // expand the destination node
        JTree tree = (JTree) support.getComponent();
        tree.expandPath(new TreePath(target.getPath()));
        
        return true;
    }

    private class NodesTransferable implements Transferable {
        JMeterTreeNode[] nodes;

        public NodesTransferable(JMeterTreeNode[] nodes) {
            this.nodes = nodes;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return jMeterTreeNodeDataFlavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodeFlavor.equals(flavor);
        }
    }
}