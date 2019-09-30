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

package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.jmeter.gui.CorrelationTableModel;
import org.apache.jmeter.protocol.http.gui.action.CorrelationExtractor;

public class CorrelationGui {

    private static JTable jTable = null;

    private CorrelationGui() {
    }

    public static void createCoRelationGui(JFileChooser chooser) {

        // create the j-frame
        JFrame frame = new JFrame("Correlation");
        frame.setSize(800, 600);
        frame.setLocation(300, 100);

        // create the j-panel
        JPanel panel = new JPanel();
        panel.setBounds(200, 200, 100, 100);

        // create action buttons
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        // add buttons into JPanel
        panel.add(ok);
        panel.add(cancel);

        // add panel into JFrame
        frame.add(panel, BorderLayout.SOUTH);

        // create the correlation table
        TableModel model = new CorrelationTableModel();
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        jTable = new JTable(model);
        jTable.setDefaultRenderer(String.class, centerRenderer);

        jTable.getSelectionModel();

        JScrollPane sp = new JScrollPane(jTable);
        frame.add(sp, BorderLayout.CENTER);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /**
         * OK event of JFrame
         */
        ok.addActionListener(event -> {

            // Initialize local variables
            int rowCount = jTable.getRowCount();
            List<String> argumentsList = new ArrayList<>();
            Map<String, String> bodyParameterMap = new LinkedHashMap<>();

            // prepare the list of user selected parameters
            for (int i = 0; i < rowCount; i++) {

                if ((Boolean) jTable.getValueAt(i, 0) && jTable.getValueAt(i, 1) != null) {
                    argumentsList.add(jTable.getValueAt(i, 1).toString());
                    bodyParameterMap.put(jTable.getValueAt(i, 1).toString(), jTable.getValueAt(i, 3).toString());

                }
            }
            CorrelationExtractor.readResponse(argumentsList, bodyParameterMap);
            frame.dispose();
        }

        );

        /**
         * Cancel event of JFrame
         */
        cancel.addActionListener(actionListener -> {
            frame.dispose();
        });

    }
}
