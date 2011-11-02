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
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Help;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.reflect.ClassFinder;

public class FunctionHelper extends JDialog implements ActionListener, ChangeListener {
    private static final long serialVersionUID = 240L;

    private JLabeledChoice functionList;

    private ArgumentsPanel parameterPanel;

    private JLabeledTextField cutPasteFunction;

    // Not modified after initial setup
    private final Map<String, Class<?>> functionMap = new HashMap<String, Class<?>>();

    private JButton generateButton;

    public FunctionHelper() {
        super((JFrame) null, JMeterUtils.getResString("function_helper_title"), false); //$NON-NLS-1$
        init();
    }

    private void init() {
        parameterPanel = new ArgumentsPanel(JMeterUtils.getResString("function_params")); //$NON-NLS-1$
        initializeFunctionList();
        this.getContentPane().setLayout(new BorderLayout(10, 10));
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        comboPanel.add(functionList);
        JButton helpButton = new JButton(JMeterUtils.getResString("help")); //$NON-NLS-1$
        helpButton.addActionListener(new HelpListener());
        comboPanel.add(helpButton);
        this.getContentPane().add(comboPanel, BorderLayout.NORTH);
        this.getContentPane().add(parameterPanel, BorderLayout.CENTER);
        JPanel resultsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cutPasteFunction = new JLabeledTextField(JMeterUtils.getResString("cut_paste_function"), 35); //$NON-NLS-1$
        resultsPanel.add(cutPasteFunction);
        generateButton = new JButton(JMeterUtils.getResString("generate")); //$NON-NLS-1$
        generateButton.addActionListener(this);
        resultsPanel.add(generateButton);
        this.getContentPane().add(resultsPanel, BorderLayout.SOUTH);
        this.pack();
        ComponentUtil.centerComponentInWindow(this);
    }

    private void initializeFunctionList() {
        try {
            List<String> functionClasses = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(),
                    new Class[] { Function.class }, true);
            Iterator<String> iter = functionClasses.iterator();
            String[] functionNames = new String[functionClasses.size()];
            int count = 0;
            while (iter.hasNext()) {
                Class<?> cl = Class.forName(iter.next());
                functionNames[count] = ((Function) cl.newInstance()).getReferenceKey();
                functionMap.put(functionNames[count], cl);
                count++;
            }
            functionList = new JLabeledChoice(JMeterUtils.getResString("choose_function"), functionNames); //$NON-NLS-1$
            functionList.addChangeListener(this);
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
    }

    public void stateChanged(ChangeEvent event) {
        try {
            Arguments args = new Arguments();
            Function function = (Function) ((Class<?>) functionMap.get(functionList.getText())).newInstance();
            List<String> argumentDesc = function.getArgumentDesc();
            Iterator<String> iter = argumentDesc.iterator();
            while (iter.hasNext()) {
                String help = iter.next();
                args.addArgument(help, ""); //$NON-NLS-1$
            }
            parameterPanel.configure(args);
            parameterPanel.revalidate();
            getContentPane().remove(parameterPanel);
            this.pack();
            getContentPane().add(parameterPanel, BorderLayout.CENTER);
            this.pack();
            this.validate();
            this.repaint();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
    }

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
    }

    private class HelpListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String[] source = new String[] { Help.HELP_FUNCTIONS, functionList.getText() };
            ActionEvent helpEvent = new ActionEvent(source, e.getID(), "help"); //$NON-NLS-1$
            ActionRouter.getInstance().actionPerformed(helpEvent);
        }
    }
}
