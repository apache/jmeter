package org.apache.jmeter.extractor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class RegexExtractorGui extends AbstractPostProcessorGui
{
    JLabeledTextField regexField;
    JLabeledTextField templateField;
    JLabeledTextField defaultField;
    JLabeledTextField matchNumberField;
    JLabeledTextField refNameField;
    
    public RegexExtractorGui()
    {
        super();
        init();
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
     */
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("regex_extractor_title");
    }
    
    public void configure(TestElement el)
    {
        super.configure(el);
        regexField.setText(el.getPropertyAsString(RegexExtractor.REGEX));
        templateField.setText(el.getPropertyAsString(RegexExtractor.TEMPLATE));
        defaultField.setText(el.getPropertyAsString(RegexExtractor.DEFAULT));
        matchNumberField.setText(el.getPropertyAsString(RegexExtractor.MATCH_NUMBER));
        refNameField.setText(el.getPropertyAsString(RegexExtractor.REFNAME));
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement()
    {
        RegexExtractor extractor = new RegexExtractor();
        modifyTestElement(extractor);
        return extractor;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement extractor)
    {
        super.configureTestElement(extractor);
        extractor.setProperty(RegexExtractor.MATCH_NUMBER,matchNumberField.getText());
        if(extractor instanceof RegexExtractor)
        {
            RegexExtractor regex = (RegexExtractor)extractor;
            regex.setRefName(refNameField.getText());
            regex.setRegex(regexField.getText());
            regex.setTemplate(templateField.getText());
            regex.setDefaultValue(defaultField.getText());
        }
    }
    
    private void init()
    {
        regexField = new JLabeledTextField(JMeterUtils.getResString("regex_field"));
        templateField = new JLabeledTextField(JMeterUtils.getResString("template_field"));
        defaultField = new JLabeledTextField(JMeterUtils.getResString("default_value_field"));
        refNameField = new JLabeledTextField(JMeterUtils.getResString("ref_name_field"));
        matchNumberField = new JLabeledTextField(JMeterUtils.getResString("match_num_field"));
        setLayout(new BorderLayout());
        add(makeTitlePanel(),BorderLayout.NORTH);
        add(makeParameterPanel(),BorderLayout.CENTER);
    }
    
    private JPanel makeParameterPanel()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        addField(panel,refNameField,gbc);
        resetContraints(gbc);
        gbc.gridy++;
        addField(panel,regexField,gbc);
        resetContraints(gbc);
        gbc.gridy++;
        addField(panel,templateField,gbc);
        resetContraints(gbc);
        gbc.gridy++;
        addField(panel,matchNumberField,gbc);
        resetContraints(gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        addField(panel,defaultField,gbc);
        return panel;
    }
    
    private void addField(JPanel panel,JLabeledTextField field,GridBagConstraints gbc)
    {
        List item = field.getComponentList();
        panel.add((Component)item.get(0),gbc.clone());
        gbc.gridx++;
        gbc.weightx = 1;;
        panel.add((Component)item.get(1),gbc.clone());
    }
    
    private void resetContraints(GridBagConstraints gbc)
    {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
    }
    
    private void initConstraints(GridBagConstraints gbc)
    {
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
    }

}
