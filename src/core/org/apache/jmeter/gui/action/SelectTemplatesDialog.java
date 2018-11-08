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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.template.Template;
import org.apache.jmeter.gui.action.template.TemplateManager;
import org.apache.jmeter.swing.HtmlPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.TemplateUtil;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * Dialog used for Templates selection
 * @since 2.10
 */
public class SelectTemplatesDialog extends JDialog implements ChangeListener, ActionListener, HyperlinkListener {

    private static final long serialVersionUID = 1;
    
    // Minimal dimensions for dialog box
    private static final int MINIMAL_BOX_WIDTH = 500;
    private static final int MINIMAL_BOX_HEIGHT = 300;
    
    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font"); //$NON-NLS-1$

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8)); //$NON-NLS-1$

    private static final Logger log = LoggerFactory.getLogger(SelectTemplatesDialog.class);

    private final JLabeledChoice templateList = new JLabeledChoice(JMeterUtils.getResString("template_choose"), false); //$NON-NLS-1$

    private final HtmlPane helpDoc = new HtmlPane();

    private final JButton reloadTemplateButton = new JButton(JMeterUtils.getResString("template_reload")); //$NON-NLS-1$

    private final JButton applyTemplateButton = new JButton();

    private final JButton cancelButton = new JButton(JMeterUtils.getResString("cancel")); //$NON-NLS-1$
    
    // #########################################################################
    private final JButton goBack = new JButton("goBack"); //$NON-NLS-1$
    private final JButton validateButton = new JButton("validate"); //$NON-NLS-1$
    private final JButton cancelButton2 = new JButton(JMeterUtils.getResString("cancel")); //$NON-NLS-1$
    private JScrollPane scroller2;
    private JPanel actionBtnBar2 = new JPanel(new FlowLayout());
 // #########################################################################
    private JScrollPane scroller;
    
    private Map<String, JLabeledTextField> buttonsParameters = new LinkedHashMap<>();
    
    private JPanel gridbagpanel = new JPanel(new GridBagLayout());
    
    JPanel actionBtnBar = new JPanel(new FlowLayout());

    public SelectTemplatesDialog() {
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

            private static final long serialVersionUID = -3661361497864527363L;

            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                checkDirtyAndLoad(actionEvent);
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
    
    /**
     * Check if existing Test Plan has been modified and ask user 
     * what he wants to do if test plan is dirty
     * @param actionEvent {@link ActionEvent}
     */
    private void checkDirtyAndLoad(final ActionEvent actionEvent)
            throws HeadlessException {
        String separator = File.separator;
        final String selectedTemplate = templateList.getText();
        final Template template = TemplateManager.getInstance().getTemplateByName(selectedTemplate);
        if (template == null) {
            return;
        }
        
        String jmeterTemplateDirectory = JMeterUtils.getJMeterBinDir()+separator+"templates"; // $NON-NLS-1$
        String jmxFolderPath = jmeterTemplateDirectory+separator+template.getName();
        if(buttonsParameters != null && !buttonsParameters.isEmpty()) { // handle customized templates (the .jmx.fmkr files)
            File fmkrFile = new File(template.getFileName());
            String fmkrFileName = fmkrFile.getName();
            String jmxFileName = fmkrFileName.substring(0, fmkrFileName.length()-5);
            
            Map<String, String> userParameters = getUserParameters();
            
            Configuration templateCfg = TemplateUtil.getTemplateConfig();
            try {
                TemplateUtil.processTemplate(new File(jmeterTemplateDirectory+separator+fmkrFileName), jmxFileName,
                        jmxFolderPath, templateCfg, userParameters);
                String generatedJmxRelativePath = separator+"bin"+separator+"templates"+separator+ //$NON-NLS-1$ //$NON-NLS-2$ 
                        template.getName()+separator+jmxFileName;
                template.setFileName(generatedJmxRelativePath); // put the generated jmx in the template fileName
            } catch (IOException e) {
                log.error("IOException while processing template", e);
                return;
            } catch (TemplateException e) {
                log.error("couldn't replace elements in {}", jmeterTemplateDirectory+File.separator+fmkrFileName, e);
                return;
            }
        }
        
        final boolean isTestPlan = template.isTestPlan();
        // Check if the user wants to drop any changes
        if (isTestPlan) {
            ActionRouter.getInstance().doActionNow(new ActionEvent(actionEvent.getSource(), actionEvent.getID(), ActionNames.CHECK_DIRTY));
            GuiPackage guiPackage = GuiPackage.getInstance();
            if (guiPackage.isDirty()) {
                // Check if the user wants to create from template
                int response = JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                        JMeterUtils.getResString("cancel_new_from_template"), // $NON-NLS-1$
                        JMeterUtils.getResString("template_load?"),  // $NON-NLS-1$
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if(response == JOptionPane.YES_OPTION) {
                    ActionRouter.getInstance().doActionNow(new ActionEvent(actionEvent.getSource(), actionEvent.getID(), ActionNames.SAVE));
                }
                if (response == JOptionPane.CLOSED_OPTION || response == JOptionPane.CANCEL_OPTION) {
                    return; // Don't clear the plan
                }
            }
        }
        ActionRouter.getInstance().doActionNow(new ActionEvent(actionEvent.getSource(), actionEvent.getID(), ActionNames.STOP_THREAD));
        final File parent = template.getParent();
        final File fileToCopy = parent != null 
              ? new File(parent, template.getFileName())
              : new File(JMeterUtils.getJMeterHome(), template.getFileName());       
        Load.loadProjectFile(actionEvent, fileToCopy, !isTestPlan, false);
        
        // suppress the previously created jmx in case of customized templates
        if(template.getParameters() != null && !template.getParameters().isEmpty()) {
            try {
                FileUtils.deleteDirectory(new File(jmxFolderPath));
            } catch (IOException e) {
                log.error("could not suppress created template directory",e);
            } catch(IllegalArgumentException e) {
                log.error("could not find directory {}",jmxFolderPath,e);
            }
        }
        this.setVisible(false);
    }
    
    private Map<String, String> getUserParameters(){
        Map<String, String> userParameters = new LinkedHashMap<>();
        for(Entry<String, JLabeledTextField> entry : buttonsParameters.entrySet()) {
            userParameters.put(entry.getKey(), entry.getValue().getText());
        }
        return userParameters;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        templateList.setValues(TemplateManager.getInstance().getTemplateNames());            
        templateList.addChangeListener(this);
        reloadTemplateButton.addActionListener(this);
        reloadTemplateButton.setFont(FONT_SMALL);
        helpDoc.setContentType("text/html"); //$NON-NLS-1$
        helpDoc.setEditable(false);
        helpDoc.addHyperlinkListener(this);
        applyTemplateButton.addActionListener(this);
        cancelButton.addActionListener(this);
        goBack.addActionListener(this);
        validateButton.addActionListener(this);
        
        this.setContentPane(templateSelectionPanel());

        this.pack();
        this.setMinimumSize(new Dimension(MINIMAL_BOX_WIDTH, MINIMAL_BOX_HEIGHT));
        ComponentUtil.centerComponentInWindow(this, 50); // center position and 50% of screen size
        populateTemplatePage();
    }
    
    private JPanel templateSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        scroller = new JScrollPane();
        scroller.setViewportView(helpDoc);
        JPanel templateBar = new JPanel(new BorderLayout());
        templateBar.add(templateList, BorderLayout.CENTER);
        JPanel reloadBtnBar = new JPanel();
        reloadBtnBar.add(reloadTemplateButton);
        templateBar.add(reloadBtnBar, BorderLayout.EAST);

        // Bottom buttons bar
        actionBtnBar.add(applyTemplateButton);
        actionBtnBar.add(cancelButton);
        
        panel.add(templateBar, BorderLayout.NORTH);
        panel.add(scroller, BorderLayout.CENTER);
        panel.add(actionBtnBar, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Do search
     * @param e {@link ActionEvent}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        final Object source = e.getSource();
        if (source == cancelButton) {
            this.setVisible(false);
            return;
        } else if (source == applyTemplateButton) {
            final String selectedTemplate = templateList.getText();
            final Template template = TemplateManager.getInstance().getTemplateByName(selectedTemplate);
            if(template.getParameters() != null && !template.getParameters().isEmpty()) {
                this.setContentPane(choseParametersPanel(template.getParameters()));
                this.revalidate();
            }else {
                checkDirtyAndLoad(e);
            }
        } else if (source == reloadTemplateButton) {
            templateList.setValues(TemplateManager.getInstance().reset().getTemplateNames());
        } else if (source == goBack) {
            this.setContentPane(templateSelectionPanel());
            buttonsParameters.clear();
            this.revalidate();
        } else if(source == validateButton) {
            checkDirtyAndLoad(e);
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent event) {
        populateTemplatePage();
    }

    private void populateTemplatePage() {
        String selectedTemplate = templateList.getText();
        Template template = TemplateManager.getInstance().getTemplateByName(selectedTemplate);
        helpDoc.setText(template.getDescription());
        applyTemplateButton.setText(template.isTestPlan() 
                ? JMeterUtils.getResString("template_create_from")
                : JMeterUtils.getResString("template_merge_from") );
    }
    

    
    private JPanel choseParametersPanel(Map<String, String> parameters) {
        JPanel panel = new JPanel(new BorderLayout());
        buttonsParameters.clear();
        
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        int parameterCount = 0;

        for(Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            JLabeledTextField paramLabel = new JLabeledTextField(key + " : ");
            paramLabel.setText(value);
            buttonsParameters.put(key, paramLabel);

            gbc.gridy = parameterCount;
            List<JComponent> listedParamLabel = paramLabel.getComponentList();
            gridbagpanel.add(listedParamLabel.get(0), gbc.clone());
            gbc.gridx = 1;
            gridbagpanel.add(listedParamLabel.get(1), gbc.clone());
            gbc.gridx = 0;

            parameterCount++;
        }
        
        actionBtnBar2.add(validateButton);
        actionBtnBar2.add(cancelButton2);
        actionBtnBar2.add(goBack);
        
        scroller2 = new JScrollPane(gridbagpanel);
        panel.add(scroller2, BorderLayout.CENTER);
        panel.add(actionBtnBar2, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void initConstraints(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0,0,5,0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (Exception ex) {
                log.error("Error opening URL in browser: {}", e.getURL());
            } 
        }
    }

}
