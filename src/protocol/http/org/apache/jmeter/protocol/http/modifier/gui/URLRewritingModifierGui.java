// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.http.modifier.gui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.protocol.http.modifier.URLRewritingModifier;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @version $Revision$ last updated $Date$
 */
public class URLRewritingModifierGui extends AbstractPreProcessorGui
{
    JLabeledTextField argumentName;
    JCheckBox pathExt;
    JCheckBox pathExtNoEquals;
    JCheckBox pathExtNoQuestionmark;

    public String getLabelResource()
    {
        return "http_url_rewriting_modifier_title";
    }

    public URLRewritingModifierGui()
    {
        init();
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();

        argumentName =
            new JLabeledTextField(
                JMeterUtils.getResString("session_argument_name"),
                10);
        mainPanel.add(argumentName);

        pathExt =
            new JCheckBox(JMeterUtils.getResString("Path_Extension_choice"));
        mainPanel.add(pathExt);

        pathExtNoEquals =
            new JCheckBox(
                JMeterUtils.getResString("path_extension_dont_use_equals"));
        mainPanel.add(pathExtNoEquals);

        pathExtNoQuestionmark =
            new JCheckBox(
                JMeterUtils.getResString("path_extension_dont_use_questionmark"));
        mainPanel.add(pathExtNoQuestionmark);

        add(mainPanel, BorderLayout.CENTER);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement()
    {
        URLRewritingModifier modifier = new URLRewritingModifier();
        modifyTestElement(modifier);
        return modifier;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement modifier)
    {
        this.configureTestElement(modifier);
        ((URLRewritingModifier) modifier).setArgumentName(
            argumentName.getText());
        ((URLRewritingModifier) modifier).setPathExtension(
            pathExt.isSelected());
        ((URLRewritingModifier) modifier).setPathExtensionNoEquals(
            pathExtNoEquals.isSelected());
        ((URLRewritingModifier) modifier).setPathExtensionNoQuestionmark(
                pathExtNoQuestionmark.isSelected());
    }

    public void configure(TestElement el)
    {
        argumentName.setText(((URLRewritingModifier) el).getArgumentName());
        pathExt.setSelected(((URLRewritingModifier) el).isPathExtension());
        pathExtNoEquals.setSelected(
            ((URLRewritingModifier) el).isPathExtensionNoEquals());
        pathExtNoQuestionmark.setSelected(
                ((URLRewritingModifier) el).isPathExtensionNoQuestionmark());

        super.configure(el);
    }
}
