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

package org.apache.jmeter.gui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.gui.action.template.Template;
import org.apache.jmeter.gui.action.template.TemplateManager;
import org.apache.jmeter.swing.HtmlPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * Dialog used for Templates selection
 * @since 2.10
 */
public class SelectTemplateDialog extends JDialog implements ChangeListener, ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -4436834972710248247L;

//    private static final Logger log = LoggingManager.getLoggerForClass();

    private JLabeledChoice templateList;

    private HtmlPane helpDoc = new HtmlPane();

    private JButton createFromTemplateButton;

    private JButton cancelButton;
    
    private JScrollPane scroller = new JScrollPane(helpDoc);

    public SelectTemplateDialog() {
        super((JFrame) null, JMeterUtils.getResString("template_title"), true); //$NON-NLS-1$
        init();
    }

    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        // Hide Window on ESC
        Action escapeAction = new AbstractAction("ESCAPE") { //$NON-NLS-1$
            /**
             *
             */
            private static final long serialVersionUID = -6543764044868772971L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        // Do search on Enter
        Action enterAction = new AbstractAction("ENTER") { //$NON-NLS-1$
            /**
             *
             */
            private static final long serialVersionUID = -3661361497864527363L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                doOpen(actionEvent);
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
        initializeTemplateList();
        this.getContentPane().setLayout(new BorderLayout(10, 10));
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        comboPanel.add(templateList);
        this.getContentPane().add(comboPanel, BorderLayout.NORTH);
        helpDoc.setContentType("text/html"); //$NON-NLS-1$
        helpDoc.setEditable(false);
        fillDescription();
        JPanel jPanel = new JPanel(new GridLayout(1,1));
        scroller.setPreferredSize(new Dimension(300, 400));
        jPanel.setPreferredSize(new Dimension(310, 410));
        jPanel.add(scroller);
        this.getContentPane().add(jPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        createFromTemplateButton = new JButton(JMeterUtils.getResString("template_create_from")); //$NON-NLS-1$
        createFromTemplateButton.addActionListener(this);

        cancelButton = new JButton(JMeterUtils.getResString("cancel")); //$NON-NLS-1$
        cancelButton.addActionListener(this);
        buttonsPanel.add(createFromTemplateButton);
        buttonsPanel.add(cancelButton);
        this.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        this.pack();
        ComponentUtil.centerComponentInWindow(this);
    }
    
    private void initializeTemplateList() {
        Set<String> templatesAsSet = TemplateManager.getInstance().getTemplateNames();
        String[] templateNames =  templatesAsSet.toArray(new String[templatesAsSet.size()]);
        Arrays.sort(templateNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        templateList = new JLabeledChoice(JMeterUtils.getResString("template_choose"), templateNames); //$NON-NLS-1$
        templateList.addChangeListener(this);
    }

    /**
     * Do search
     * @param e {@link ActionEvent}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==cancelButton) {
            this.setVisible(false);
            return;
        }
        doOpen(e);
    }
    
    @Override
    public void stateChanged(ChangeEvent event) {
        fillDescription();
    }

    /**
     * 
     */
    protected void fillDescription() {
        String selectedTemplate = templateList.getText();
        Template template = TemplateManager.getInstance().getTemplateByName(selectedTemplate);
        helpDoc.setText(template.getDescription());
    }

    /**
     * @param e {@link ActionEvent}
     */
    private void doOpen(ActionEvent e) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            String selectedTemplate = templateList.getText();
            Template template = TemplateManager.getInstance().getTemplateByName(selectedTemplate);   
            File fileToCopy = new File(JMeterUtils.getJMeterHome(), template.getFileName());
            File targetFile = new File( System.getProperty("user.dir"), 
                    template.getFileName().substring(template.getFileName().lastIndexOf("/")));
            inputStream = new BufferedInputStream(new FileInputStream(fileToCopy));
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();
            Load.loadProjectFile(e, targetFile, false); 
            this.setVisible(false);
        } catch (Exception e1) {
            throw new Error(e1);
        } finally {
            JOrphanUtils.closeQuietly(inputStream);
            JOrphanUtils.closeQuietly(outputStream);
        }
    }
}