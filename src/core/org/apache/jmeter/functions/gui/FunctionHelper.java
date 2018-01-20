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

package org.apache.jmeter.functions.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Help;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionHelper extends JDialog implements ActionListener, ChangeListener, LocaleChangeListener {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggerFactory.getLogger(ClientJMeterEngine.class);

    private JLabeledChoice functionList;

    private ArgumentsPanel parameterPanel;

    private JLabeledTextField cutPasteFunction;
    
    private JSyntaxTextArea resultTextArea;

    public FunctionHelper() {
        super((JFrame) null, JMeterUtils.getResString("function_helper_title"), false); //$NON-NLS-1$
        init();
        JMeterUtils.addLocaleChangeListener(this);
    }

    /**
     * Allow Dialog to be closed by ESC key
     */
    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        javax.swing.Action escapeAction = new AbstractAction("ESCAPE") { 

            private static final long serialVersionUID = -4036804004190858925L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            } 
        };
        rootPane.getActionMap().put(escapeAction.getValue(Action.NAME), escapeAction);
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStrokes.ESC, escapeAction.getValue(Action.NAME));
        return rootPane;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        parameterPanel = new ArgumentsPanel(true,JMeterUtils.getResString("function_params")); //$NON-NLS-1$
        initializeFunctionList();
        this.getContentPane().setLayout(new BorderLayout(10, 10));
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        comboPanel.add(functionList);
        JButton helpButton = new JButton(JMeterUtils.getResString("help")); //$NON-NLS-1$
        helpButton.addActionListener(new HelpListener());
        comboPanel.add(helpButton);
        this.getContentPane().add(comboPanel, BorderLayout.NORTH);
        this.getContentPane().add(parameterPanel, BorderLayout.CENTER);
        JPanel resultsPanel = new VerticalPanel();
        JPanel generatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel displayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cutPasteFunction = new JLabeledTextField(JMeterUtils.getResString("cut_paste_function"), 35, null, false); //$NON-NLS-1$
        generatePanel.add(cutPasteFunction);
        JButton generateButton = new JButton(JMeterUtils.getResString("generate")); //$NON-NLS-1$
        generateButton.addActionListener(this);
        generatePanel.add(generateButton);
        resultTextArea = JSyntaxTextArea.getInstance(5,60);
        resultTextArea.setToolTipText(JMeterUtils.getResString("function_helper_dialog_result_warn"));
        displayPanel.add(new JLabel(JMeterUtils.getResString("result_function")));
        displayPanel.add(JTextScrollPane.getInstance(resultTextArea));
        
        resultsPanel.add(generatePanel);
        resultsPanel.add(displayPanel);
        
        this.getContentPane().add(resultsPanel, BorderLayout.SOUTH);
        this.pack();
        ComponentUtil.centerComponentInWindow(this);
    }

    private void initializeFunctionList() {
        String[] functionNames = CompoundVariable.getFunctionNames();
        Arrays.sort(functionNames, String::compareToIgnoreCase);
        functionList = new JLabeledChoice(JMeterUtils.getResString("choose_function"), functionNames); //$NON-NLS-1$
        functionList.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        try {
            initParameterPanel();
            getContentPane().remove(parameterPanel);
            this.pack();
            getContentPane().add(parameterPanel, BorderLayout.CENTER);
            this.pack();
            this.validate();
            resultTextArea.setText("");
            this.repaint();
        } catch (InstantiationException | IllegalAccessException ex) {
            log.info("Exception during stateChanged", ex);
        }
    }

    /**
     * @throws InstantiationException if function instantiation fails
     * @throws IllegalAccessException if function instantiation fails
     */
    protected void initParameterPanel() throws InstantiationException, IllegalAccessException {
        Arguments args = new Arguments();
        Function function = CompoundVariable.getFunctionClass(functionList.getText()).newInstance();
        List<String> argumentDesc = function.getArgumentDesc();
        for (String help : argumentDesc) {
            args.addArgument(help, ""); //$NON-NLS-1$
        }
        parameterPanel.configure(args);
        parameterPanel.revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        StringBuilder functionCall = new StringBuilder("${");
        functionCall.append(functionList.getText());
        Arguments args = (Arguments) parameterPanel.createTestElement();
        if (args.getArguments().size() > 0) {
            functionCall.append("(");
            PropertyIterator iter = args.iterator();
            boolean first = true;
            while (iter.hasNext()) {
                Argument arg = (Argument) iter.next().getObjectValue();
                if (!first) {
                    functionCall.append(",");
                }
                functionCall.append(arg.getValue());
                first = false;
            }
            functionCall.append(")");
        }
        functionCall.append("}");
        cutPasteFunction.setText(functionCall.toString());
        GuiUtils.copyTextToClipboard(cutPasteFunction.getText());
        CompoundVariable function = new CompoundVariable(functionCall.toString());
        try {
            resultTextArea.setText(function.execute().trim());
        } catch(Exception ex) {
            log.error("Error calling function {}", functionCall.toString(), ex);
            resultTextArea.setText(ex.getMessage() + ", \nstacktrace:\n "+
                    ExceptionUtils.getStackTrace(ex));
            resultTextArea.setCaretPosition(0);
        }
    }

    private class HelpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] source = new String[] { Help.HELP_FUNCTIONS, functionList.getText() };
            ActionRouter.getInstance().doActionNow(
                    new ActionEvent(source, e.getID(), ActionNames.HELP));

        }
    }

    @Override
    public void localeChanged(LocaleChangeEvent event) {
        setTitle(JMeterUtils.getResString("function_helper_title")); //$NON-NLS-1$
        this.getContentPane().removeAll(); // so we can add them again in init
        init();
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            try {
                initParameterPanel();
            } catch (InstantiationException | IllegalAccessException ex) {
                log.error("Error initializing parameter panel", ex);
            }
        }
    }
}
