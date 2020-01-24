/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.jmeter.gui.CorrelationTableModel;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.protocol.http.correlation.CorrelationRule;
import org.apache.jmeter.util.JMeterUtils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CorrelationExportRuleGui {

    private static final String[] exts = new String[] { ".json" };

    public static void createCorrelationRuleFileGui(Set<CorrelationRule> ruleSet) {
        // create the j-frame
        JFrame frame = new JFrame(JMeterUtils.getResString("correlation_export_rule")); //$NON-NLS-1$
        frame.setSize(800, 600);
        frame.setLocation(300, 100);
        // create the j-panel
        JPanel panel = new JPanel();
        panel.setBounds(200, 200, 100, 100);
        // create action buttons
        JButton ok = new JButton("OK"); //$NON-NLS-1$
        JButton cancel = new JButton(JMeterUtils.getResString("cancel")); //$NON-NLS-1$
        // add buttons into JPanel
        panel.add(ok);
        panel.add(cancel);
        // add panel into JFrame
        frame.add(panel, BorderLayout.SOUTH);
        // create the correlation table
        CorrelationTableModel.setColumnNames("Select extractors to export", "Name", "Type");
        TableModel model = new CorrelationTableModel();
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        JTable jTable = new JTable(model);
        jTable.setDefaultRenderer(String.class, centerRenderer);
        jTable.getSelectionModel();
        jTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane sp = new JScrollPane(jTable);
        frame.add(sp, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // OK event of JFrame
        ok.addActionListener(event -> {
            int rowCount = jTable.getRowCount();
            Map<String, String> parameterMap = new LinkedHashMap<>();
            // prepare the list parameters which the user wants to correlate
            for (int i = 0; i < rowCount; i++) {
                if ((Boolean) jTable.getValueAt(i, 0) && jTable.getValueAt(i, 1) != null) {
                    parameterMap.put(jTable.getValueAt(i, 1).toString(), jTable.getValueAt(i, 2).toString());
                }
            }
            if (parameterMap.isEmpty()) {
                JMeterUtils.reportErrorToUser("No extractors selected. Please select the extractors and try again.");
                return;
            }
            // filter the rules which are selected by the user to be exported
            List<CorrelationRule> rulesToPrepareJson = ruleSet.stream()
                    .filter(rule -> parameterMap.containsKey(rule.getName())).collect(Collectors.toList());
            // prepare internal JSON data objects
            ObjectMapper obj = new ObjectMapper();
            JSONArray jsonArray = new JSONArray();
            // Prepare JSONObject list from rule which will be written to JSON file
            List<JSONObject> jsonObjectList = rulesToPrepareJson.stream()
                    .map(rule -> new JSONObject(obj.convertValue(rule, new TypeReference<Map<String, String>>() {
                    }))).collect(Collectors.toList());
            jsonArray.addAll(jsonObjectList);
            // prepare JSON root
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("rule", jsonArray);
            // dispose the JTable frame
            frame.dispose();
            // Get user input for saving file
            JFileChooser chooser = FileDialoger.promptToSaveFile("rule.json", exts);
            if (chooser == null) {
                return;
            }
            String updateFile = chooser.getSelectedFile().getAbsolutePath();
            File f = new File(updateFile);
            if (f.exists()) {
                int response = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                        JMeterUtils.getResString("save_overwrite_existing_file"), // $NON-NLS-1$
                        JMeterUtils.getResString("save?"), // $NON-NLS-1$
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CLOSED_OPTION || response == JOptionPane.NO_OPTION) {
                    return; // Do not save, user does not want to overwrite
                }
            }
            // Write the file
            try (FileWriter file = new FileWriter(updateFile)) {
                file.write(jsonObject.toJSONString());
            } catch (IOException e) {
                JMeterUtils.reportErrorToUser("Some error occured while exporting file. " + e.getMessage(),
                        "Export Failed");
                return;
            }
            JMeterUtils.reportInfoToUser("Export successful.", "Export Successful");
        });
        // Cancel event of JFrame
        cancel.addActionListener(actionListener -> frame.dispose());
    }
}
