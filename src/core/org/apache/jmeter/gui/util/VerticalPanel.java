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

package org.apache.jmeter.gui.util;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class VerticalPanel extends JPanel {
    private static final long serialVersionUID = 240L;

    private final Box subPanel = Box.createVerticalBox();

    private final float horizontalAlign;

    private final int vgap;

    public VerticalPanel() {
        this(5, LEFT_ALIGNMENT);
    }

    public VerticalPanel(Color bkg) {
        this();
        subPanel.setBackground(bkg);
        this.setBackground(bkg);
    }

    public VerticalPanel(int vgap, float horizontalAlign) {
        super(new BorderLayout());
        add(subPanel, BorderLayout.NORTH);
        this.vgap = vgap;
        this.horizontalAlign = horizontalAlign;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component add(Component c) {
        // This won't work right if we remove components. But we don't, so I'm
        // not going to worry about it right now.
        if (vgap > 0 && subPanel.getComponentCount() > 0) {
            subPanel.add(Box.createVerticalStrut(vgap));
        }

        if (c instanceof JComponent) {
            ((JComponent) c).setAlignmentX(horizontalAlign);
        }

        return subPanel.add(c);
    }
}
