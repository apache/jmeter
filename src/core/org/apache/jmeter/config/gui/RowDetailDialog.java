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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

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

    private JButton updateButton;

    private JButton nextButton;

    private JButton previousButton;

    private JButton closeButton;

    private ObjectTableModel tableModel;

    private int selectedRow;

    
    public RowDetailDialog() {
        super();
    }
	/**
	 * Hide Window on ESC
	 */
	private transient ActionListener enterActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent actionEvent) {
			doUpdate(actionEvent);
			setVisible(false);
		}	
	};
	
	/**
	 * Do search on Enter
	 */
	private transient ActionListener escapeActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent actionEvent) {
			setVisible(false);
		}	
	};
	
	public RowDetailDialog(ObjectTableModel tableModel, int selectedRow) {
        super((JFrame) null, JMeterUtils.getResString("search_tree_title"), true); //$NON-NLS-1$
        this.tableModel = tableModel;
        this.selectedRow = selectedRow;
        init();
    }

    private void init() {
        this.getContentPane().setLayout(new BorderLayout(10,10));

        nameTF = new JLabeledTextField(JMeterUtils.getResString("name"), 20); //$NON-NLS-1$
        valueTA = new JLabeledTextArea(JMeterUtils.getResString("value")); //$NON-NLS-1$
        valueTA.setPreferredSize(new Dimension(150, 300));
        nameTF.setPreferredSize(new Dimension((int)Math.round(getWidth()*0.8), 40));
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
        
        updateButton = new JButton(JMeterUtils.getResString("update")); //$NON-NLS-1$
        updateButton.setActionCommand(UPDATE);
        updateButton.addActionListener(this);
        closeButton = new JButton(JMeterUtils.getResString("close")); //$NON-NLS-1$
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
        mainPanel.registerKeyboardAction(enterActionListener, KeyStrokes.ENTER, JComponent.WHEN_IN_FOCUSED_WINDOW);
        mainPanel.registerKeyboardAction(escapeActionListener, KeyStrokes.ESC, JComponent.WHEN_IN_FOCUSED_WINDOW);
    	nameTF.requestFocusInWindow();

        this.pack();
        ComponentUtil.centerComponentInWindow(this);
    }

    /**
     * Do search
     * @param e {@link ActionEvent}
     */
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