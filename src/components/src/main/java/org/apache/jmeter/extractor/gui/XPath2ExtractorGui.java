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

package org.apache.jmeter.extractor.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import net.miginfocom.swing.MigLayout;

/**
 * GUI for XPath2Extractor class.
 * @since 5.0
 */
@TestElementMetadata(labelResource = "xpath2_extractor_title")
public class XPath2ExtractorGui extends AbstractPostProcessorGui{ // NOSONAR Ignore parents warning

    private static final long serialVersionUID = 1L;

    private final JTextField defaultField = new JTextField(25);

    private final JTextField xpathQueryField = new JTextField(30);

    private final JTextField matchNumberField = new JTextField();

    private final JTextField refNameField = new JTextField(25);

    // Should we return fragment as text, rather than text of fragment?
    private JCheckBox getFragment;

    private JSyntaxTextArea namespacesTA;

    @Override
    public String getLabelResource() {
        return "xpath2_extractor_title"; //$NON-NLS-1$
    }

    public XPath2ExtractorGui() {
        super();
        init();
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        XPath2Extractor xpe = (XPath2Extractor) el;
        showScopeSettings(xpe, true);
        xpathQueryField.setText(xpe.getXPathQuery());
        defaultField.setText(xpe.getDefaultValue());
        refNameField.setText(xpe.getRefName());
        matchNumberField.setText(xpe.getMatchNumberAsString());
        namespacesTA.setText(xpe.getNamespaces());
        getFragment.setSelected(xpe.getFragment());
    }

    @Override
    public TestElement createTestElement() {
        XPath2Extractor extractor = new XPath2Extractor();
        modifyTestElement(extractor);
        return extractor;
    }

    @Override
    public void modifyTestElement(TestElement extractor) {
        super.configureTestElement(extractor);
        if (extractor instanceof XPath2Extractor) {
            XPath2Extractor xpath = (XPath2Extractor) extractor;
            saveScopeSettings(xpath);
            xpath.setDefaultValue(defaultField.getText());
            xpath.setRefName(refNameField.getText());
            xpath.setMatchNumber(matchNumberField.getText());
            xpath.setXPathQuery(xpathQueryField.getText());
            xpath.setFragment(getFragment.isSelected());
            xpath.setNamespaces(namespacesTA.getText());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        xpathQueryField.setText(""); // $NON-NLS-1$
        defaultField.setText(""); // $NON-NLS-1$
        refNameField.setText(""); // $NON-NLS-1$
        matchNumberField.setText(XPath2Extractor.DEFAULT_VALUE_AS_STRING); // $NON-NLS-1$
        namespacesTA.setText("");
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(makeParameterPanel(), BorderLayout.CENTER);
    }

    private JPanel makeParameterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createScopePanel(true, true, true), BorderLayout.NORTH);

        JPanel panel = new JPanel(new MigLayout("fillx, wrap 2, insets 0", "[][fill,grow]"));
        panel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("xpath2_extractor_properties")));

        panel.add(JMeterUtils.labelFor(refNameField, "ref_name_field"));
        panel.add(refNameField);

        panel.add(JMeterUtils.labelFor(xpathQueryField, "xpath_extractor_query"));
        panel.add(xpathQueryField);

        panel.add(JMeterUtils.labelFor(matchNumberField, "match_num_field"));
        panel.add(matchNumberField);

        panel.add(JMeterUtils.labelFor(defaultField, "default_value_field"));
        panel.add(defaultField);

        namespacesTA = JSyntaxTextArea.getInstance(5, 80);
        JTextScrollPane namespaceJSP = JTextScrollPane.getInstance(namespacesTA, true);
        panel.add(JMeterUtils.labelFor(namespaceJSP, "xpath_extractor_user_namespaces"));
        panel.add(namespaceJSP);

        getFragment = new JCheckBox(JMeterUtils.getResString("xpath_extractor_fragment"));//$NON-NLS-1$
        panel.add(getFragment, "span 2");
        mainPanel.add(panel, BorderLayout.CENTER);
        return mainPanel;
    }
}
