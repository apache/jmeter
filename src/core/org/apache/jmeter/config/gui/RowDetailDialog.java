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

package org.apache.jmeter.config.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.ObjectTableModel;

/**
 * Show detail of a Row 
 */
public class RowDetailDialog extends JDialog implements ActionListener {

	/**
     * 
     */
    private static final long serialVersionUID = 6578889215615435475L;

    /** Command for moving a row up in the table. */
    private static final String NEXT = "next"; // $NON-NLS-1$

    /** Command for moving a row down in the table. */
    private static final String PREVIOUS = "previous"; // $NON-NLS-1$

    /** Command for CANCEL. */
    private static final String CLOSE = "close"; // $NON-NLS-1$

    private static final String UPDATE = "update"; // $NON-NLS-1$
    
	private JLabeledTextField nameTF;

    private JLabeledTextArea valueTA;

    private JButton nextButton;

    private JButton previousButton;

    private ObjectTableModel tableModel;

    private int selectedRow;

    
    public RowDetailDialog() {
        super();
    }
	
	public RowDetailDialog(ObjectTableModel tableModel, int selectedRow) {
        super((JFrame) null, JMeterUtils.getResString("detail"), true); //$NON-NLS-1$
        this.tableModel = tableModel;
        this.selectedRow = selectedRow;
        init();
    }

	@Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        // Hide Window on ESC
        Action escapeAction = new AbstractAction("ESCAPE") { 
            /**
             * 
             */
            private static final long serialVersionUID = -8699034338969407625L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) { 
                setVisible(false);
            } 
        };
        // Do update on Enter
        Action enterAction = new AbstractAction("ENTER") { 
            /**
             * 
             */
            private static final long serialVersionUID = -1529005452976176873L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                doUpdate(actionEvent);
                setVisible(false);
            }
        };
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put(escapeAction.getValue(Action.NAME), escapeAction);
        actionMap.put(enterAction.getValue(Action.NAME), enterAction);
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);  
        inputMap.put(KeyStrokes.ESC, escapeAction.getValue(Action.NAME));
        inputMap.put(KeyStrokes.ENTER, enterAction.getValue(Action.NAME));
        return rootPane;
    }
	
    private void init() {
        this.getContentPane().setLayout(new BorderLayout(10,10));

        nameTF = new JLabeledTextField(JMeterUtils.getResString("name"), 20); //$NON-NLS-1$
        valueTA = new JLabeledTextArea(JMeterUtils.getResString("value")); //$NON-NLS-1$
        valueTA.setPreferredSize(new Dimension(450, 300));
        setValues(selectedRow);
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());
        //detailPanel.setBorder(BorderFactory.createEmptyBorder(7, 3, 3, 3));
        detailPanel.add(nameTF, BorderLayout.NORTH);
        detailPanel.add(valueTA, BorderLayout.CENTER);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(7, 3, 3, 3));
        mainPanel.add(detailPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton updateButton = new JButton(JMeterUtils.getResString("update")); //$NON-NLS-1$
        updateButton.setActionCommand(UPDATE);
        updateButton.addActionListener(this);
        JButton closeButton = new JButton(JMeterUtils.getResString("close")); //$NON-NLS-1$
        closeButton.setActionCommand(CLOSE);
        closeButton.addActionListener(this);
        nextButton = new JButton(JMeterUtils.getResString("next")); //$NON-NLS-1$
        nextButton.setActionCommand(NEXT);
        nextButton.addActionListener(this);
        nextButton.setEnabled(selectedRow < tableModel.getRowCount()-1);
        previousButton = new JButton(JMeterUtils.getResString("previous")); //$NON-NLS-1$
        previousButton.setActionCommand(PREVIOUS);
        previousButton.addActionListener(this);
        previousButton.setEnabled(selectedRow > 0);

        buttonsPanel.add(updateButton);
        buttonsPanel.add(previousButton);
        buttonsPanel.add(nextButton);
        buttonsPanel.add(closeButton);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        this.getContentPane().add(mainPanel);
    	nameTF.requestFocusInWindow();

        this.pack();
        ComponentUtil.centerComponentInWindow(this);
    }

    /**
     * Do search
     * @param e {@link ActionEvent}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
    	if(action.equals(CLOSE)) {
    		this.setVisible(false);
    	} 
    	else if(action.equals(NEXT)) {
    	    selectedRow++;
            previousButton.setEnabled(true);
    	    nextButton.setEnabled(selectedRow < tableModel.getRowCount()-1);
    	    setValues(selectedRow);
    	} 
    	else if(action.equals(PREVIOUS)) {
            selectedRow--;
            nextButton.setEnabled(true);
            previousButton.setEnabled(selectedRow > 0);
            setValues(selectedRow);
        } 
    	else if(action.equals(UPDATE)) { 	   
            doUpdate(e);
    	}
    }

    /**
     * Set TextField and TA values from model
     * @param selectedRow Selected row
     */
	private void setValues(int selectedRow) {
        nameTF.setText((String)tableModel.getValueAt(selectedRow, 0));
        valueTA.setText((String)tableModel.getValueAt(selectedRow, 1));
    }
	
	/**
	 * Update model values
	 * @param actionEvent
	 */
    protected void doUpdate(ActionEvent actionEvent) {
        tableModel.setValueAt(nameTF.getText(), selectedRow, 0);
        tableModel.setValueAt(valueTA.getText(), selectedRow, 1);
    }
}