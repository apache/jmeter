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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.MenuFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMeterTreeTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(JMeterTreeTransferHandler.class);

    private DataFlavor nodeFlavor;
    private DataFlavor[] jMeterTreeNodeDataFlavors = new DataFlavor[1];

    // hold the nodes that should be removed on drop
    private List<JMeterTreeNode> nodesForRemoval = null;

    public JMeterTreeTransferHandler() {
        try {
            // only allow a drag&drop inside the current jvm
            String jvmLocalFlavor = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + JMeterTreeNode[].class.getName() + "\"";
            nodeFlavor = new DataFlavor(jvmLocalFlavor);
            jMeterTreeNodeDataFlavors[0] = nodeFlavor;
        }
        catch (ClassNotFoundException e) {
            log.error("Class Not Found", e);
        }
    }


    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }


    @Override
    protected Transferable createTransferable(JComponent c) {
        this.nodesForRemoval = null;
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {

            // sort the selected tree path by row
            sortTreePathByRow(paths, tree);

            // if child and a parent are selected : only keep the parent
            boolean[] toRemove = new boolean[paths.length];
            int size = paths.length;
            for (int i = 0; i < paths.length; i++) {
                for (int j = 0; j < paths.length; j++) {
                    if(i!=j && ((JMeterTreeNode)paths[i].getLastPathComponent()).isNodeAncestor((JMeterTreeNode)paths[j].getLastPathComponent())) {
                        toRemove[i] = true;
                        size--;
                        break;
                    }
                }
            }

            // remove unneeded nodes
            JMeterTreeNode[] nodes = new JMeterTreeNode[size];
            size = 0;
            for (int i = 0; i < paths.length; i++) {
                if(!toRemove[i]) {
                    JMeterTreeNode node = (JMeterTreeNode) paths[i].getLastPathComponent();
                    nodes[size++] = node;
                }
            }

            return new NodesTransferable(nodes);
        }

        return null;
    }


    private static void sortTreePathByRow(TreePath[] paths, final JTree tree) {
        Comparator<TreePath> cp = new Comparator<TreePath>() {

            @Override
            public int compare(TreePath o1, TreePath o2) {
                int row1 = tree.getRowForPath(o1);
                int row2 = tree.getRowForPath(o2);

                return row1<row2 ? -1 : (row1==row2 ? 0 : 1);
            }
        };

        Arrays.sort(paths, cp);
    }


    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

        if(this.nodesForRemoval != null
                && ((action & MOVE) == MOVE))  {
            GuiPackage guiInstance = GuiPackage.getInstance();
            for (JMeterTreeNode jMeterTreeNode : nodesForRemoval) {
                guiInstance.getTreeModel().removeNodeFromParent(jMeterTreeNode);
            }

            nodesForRemoval = null;
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }

        // the tree accepts a jmx file
        DataFlavor[] flavors = support.getDataFlavors();
        for (DataFlavor flavor : flavors) {
            // Check for file lists specifically
            if (flavor.isFlavorJavaFileListType()) {
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

        TreePath dest = dl.getPath();
        JMeterTreeNode target = (JMeterTreeNode) dest.getLastPathComponent();

        // TestPlan and WorkBench are the only children of the root
        if(target.isRoot()) {
            return false;
        }

        JMeterTreeNode[] nodes = getDraggedNodes(support.getTransferable());
        if(nodes == null || nodes.length == 0) {
            return false;
        }

        for (JMeterTreeNode node : nodes) {
            if(target == node) {
                return false;
            }

            // Do not allow a non-leaf node to be moved into one of its children
            if (node.getChildCount() > 0
                    && target.isNodeAncestor(node)) {
                return false;
            }
        }

        // re-use node association logic
        return MenuFactory.canAddTo(target, nodes);
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
        for (DataFlavor flavor : flavors) {
            // Check for file lists specifically
            if (flavor.isFlavorJavaFileListType()) {
                try {
                    return guiInstance.getMainFrame().openJmxFilesFromDragAndDrop(t);
                }
                catch (Exception e) {
                    log.error("Drop file failed", e);
                }
                return false;
            }
        }

        // Extract transfer data.
        JMeterTreeNode[] nodes = getDraggedNodes(t);

        if(nodes == null || nodes.length == 0) {
            return false;
        }

        // Get drop location and mode
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath dest = dl.getPath();
        JMeterTreeNode target = (JMeterTreeNode) dest.getLastPathComponent();

        nodesForRemoval = new ArrayList<>();
        int index = dl.getChildIndex();
        TreePath[] pathsToSelect = new TreePath[nodes.length];
        int pathPosition = 0;
        JMeterTreeModel treeModel = guiInstance.getTreeModel();
        for (JMeterTreeNode node : nodes) {

            if (index == -1) { // drop mode == DropMode.ON
                index = target.getChildCount();
            }

            // Insert a clone of the node, the original one will be removed by the exportDone method
            // the children are not cloned but moved to the cloned node
            // working on the original node would be harder as
            //    you'll have to deal with the insertion index offset if you re-order a node inside a parent
            JMeterTreeNode copy = (JMeterTreeNode) node.clone();

            // first copy the children as the call to copy.add will modify the collection we're iterating on
            Enumeration<?> enumFrom = node.children();
            List<JMeterTreeNode> tmp = new ArrayList<>();
            while (enumFrom.hasMoreElements()) {
                JMeterTreeNode child = (JMeterTreeNode) enumFrom.nextElement();
                tmp.add(child);
            }

            for (JMeterTreeNode jMeterTreeNode : tmp) {
                copy.add(jMeterTreeNode);
            }
            treeModel.insertNodeInto(copy, target, index++);
            nodesForRemoval.add(node);
            pathsToSelect[pathPosition++] = new TreePath(treeModel.getPathToRoot(copy));
        }

        TreePath treePath = new TreePath(target.getPath());
        // expand the destination node
        JTree tree = (JTree) support.getComponent();
        tree.expandPath(treePath);
        tree.setSelectionPaths(pathsToSelect);
        return true;
    }


    private JMeterTreeNode[] getDraggedNodes(Transferable t) {
        JMeterTreeNode[] nodes = null;
        try {
            nodes = (JMeterTreeNode[]) t.getTransferData(nodeFlavor);
        }
        catch (Exception e) {
            log.error("Unsupported Flavor in Transferable", e);
        }
        return nodes;
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
