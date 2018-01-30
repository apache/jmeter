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

package org.apache.jmeter.visualizers;

import java.awt.Color;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Tree cell renderer used by ComparisonVisualizer.
 */
public class TreeNodeRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 240L;

    private static final String ICON_SIZE = JMeterUtils.getPropDefault(JMeter.TREE_ICON_SIZE, JMeter.DEFAULT_TREE_ICON_SIZE);

    // Same ViewResultsTree
    private static final ImageIcon imageSuccess = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.success",  //$NON-NLS-1$
                    "vrt/" + ICON_SIZE + "/security-high-2.png")); //$NON-NLS-1$ $NON-NLS-2$

    private static final ImageIcon imageFailure = JMeterUtils.getImage(
            JMeterUtils.getPropDefault("viewResultsTree.failure",  //$NON-NLS-1$
                    "vrt/" + ICON_SIZE + "/security-low-2.png")); //$NON-NLS-1$ $NON-NLS-2$

    public TreeNodeRenderer() {
        super();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean focus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
        Object obj = ((DefaultMutableTreeNode) value).getUserObject();
        if(obj instanceof SampleResult)
        {
            if (!((SampleResult) obj).isSuccessful()) {
                this.setForeground(Color.red);
                this.setIcon(imageFailure);
            } else {
                this.setIcon(imageSuccess);
            }
        }
        return this;
    }

}
