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

/*
 * Created on Apr 25, 2003
 *
 */
package org.apache.jmeter.gui.util;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class HorizontalPanel extends JPanel {
    private static final long serialVersionUID = 240L;

    private final Box subPanel = Box.createHorizontalBox();

    private final float verticalAlign;

    private final int hgap;

    public HorizontalPanel() {
        this(5, CENTER_ALIGNMENT);
    }

    public HorizontalPanel(Color bk) {
        this();
        subPanel.setBackground(bk);
        this.setBackground(bk);
    }

    public HorizontalPanel(int hgap, float verticalAlign) {
        super(new BorderLayout());
        add(subPanel, BorderLayout.CENTER);
        this.hgap = hgap;
        this.verticalAlign = verticalAlign;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component add(Component c) {
        // This won't work right if we remove components. But we don't, so I'm
        // not going to worry about it right now.
        if (hgap > 0 && subPanel.getComponentCount() > 0) {
            subPanel.add(Box.createHorizontalStrut(hgap));
        }

        if (c instanceof JComponent) {
            ((JComponent) c).setAlignmentY(verticalAlign);
        }
        return subPanel.add(c);
    }
}
