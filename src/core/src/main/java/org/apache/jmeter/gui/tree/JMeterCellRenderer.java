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

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * Class to render the test tree - sets the enabled/disabled versions of the icons
 */
public class JMeterCellRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 241L;

    private static final int DEFAULT_LENGTH = 15;

    private static final String BLANK = StringUtils.repeat(' ', DEFAULT_LENGTH);

    private static final Border RED_BORDER = BorderFactory.createLineBorder(Color.red);
    private static final Border BLUE_BORDER = BorderFactory.createLineBorder(Color.blue);
    public JMeterCellRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean p_hasFocus) {
        JMeterTreeNode node = (JMeterTreeNode) value;
        super.getTreeCellRendererComponent(tree,
                JOrphanUtils.isBlank(node.getName()) ? BLANK : node.getName(),
                        sel, expanded, leaf, row, p_hasFocus);
        boolean enabled = node.isEnabled();
        ImageIcon ic = node.getIcon(enabled);
        if (ic != null) {
            if (enabled) {
                setIcon(ic);
            } else {
                setDisabledIcon(ic);
            }
        } else {
            if (!enabled)// i.e. no disabled icon found
            {
                // Must therefore set the enabled icon so there is at least some
                // icon
                ic = node.getIcon();
                if (ic != null) {
                    setDisabledIcon(ic);
                }
            }
        }
        this.setEnabled(enabled);
        if(node.isMarkedBySearch()) {
            setBorder(RED_BORDER);
        } else if (node.isChildrenMarkedBySearch()) {
            setBorder(BLUE_BORDER);
        } else {
            setBorder(null);
        }
        return this;
    }
}
