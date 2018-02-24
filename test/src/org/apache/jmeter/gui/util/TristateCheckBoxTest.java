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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

//derived from: http://www.javaspecialists.eu/archive/Issue145.html

public class TristateCheckBoxTest {
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("TristateCheckBoxTest");
        frame.setLayout(new GridLayout(0, 1, 15, 15));
        UIManager.LookAndFeelInfo[] lfs =
                UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lf : lfs) {
            System.out.println("Look&Feel " + lf.getName());
            UIManager.setLookAndFeel(lf.getClassName());
            frame.add(makePanel(lf.getName()));
        }
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel makePanel(String name) {
        final TristateCheckBox tristateBox = new TristateCheckBox("Tristate checkbox (icon)", false);
        createTristate(tristateBox);
        final TristateCheckBox tristateBoxorig = new TristateCheckBox("Tristate checkbox (original)", true);
        createTristate(tristateBoxorig);
        final JCheckBox normalBox = new JCheckBox("Normal checkbox");
        normalBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e);
            }
        });

        final JCheckBox enabledBox = new JCheckBox("Enable", true);
        enabledBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                tristateBox.setEnabled(enabledBox.isSelected());
                normalBox.setEnabled(enabledBox.isSelected());
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel(name));
        panel.add(tristateBox);
        panel.add(tristateBoxorig);
        panel.add(normalBox);
        panel.add(enabledBox);
        return panel;
    }

    private static void createTristate(final TristateCheckBox tristateBox) {
        tristateBox.setIndeterminate(); // start in new state
        tristateBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                System.out.println(e);
                switchOnAction(tristateBox);
            }
        });
        tristateBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e);
                switchOnAction(tristateBox);
            }
        });
    }

    private static void switchOnAction(TristateCheckBox tristateBox) {
        switch(tristateBox.getState()) {
        case SELECTED:
            System.out.println("Selected");
            break;
        case DESELECTED:
            System.out.println("Not Selected");
            break;
        case INDETERMINATE:
            System.out.println("Tristate Selected");
            break;
        default:
            System.err.println("Unexpected state: " + tristateBox.getState());
            break;
        }
    }
}
