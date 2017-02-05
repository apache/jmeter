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

package org.apache.jmeter.protocol.http.modifier.gui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.protocol.http.modifier.URLRewritingModifier;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

public class URLRewritingModifierGui extends AbstractPreProcessorGui {
    private static final long serialVersionUID = 240L;

    private JLabeledTextField argumentName;

    private JCheckBox pathExt;

    private JCheckBox pathExtNoEquals;

    private JCheckBox pathExtNoQuestionmark;

    private JCheckBox shouldCache;

    private JCheckBox encode;

    @Override
    public String getLabelResource() {
        return "http_url_rewriting_modifier_title"; // $NON-NLS-1$
    }

    public URLRewritingModifierGui() {
        init();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel mainPanel = new VerticalPanel();

        argumentName = new JLabeledTextField(JMeterUtils.getResString("session_argument_name"), 10); // $NON-NLS-1$
        mainPanel.add(argumentName);

        pathExt = new JCheckBox(JMeterUtils.getResString("path_extension_choice")); // $NON-NLS-1$
        mainPanel.add(pathExt);

        pathExtNoEquals = new JCheckBox(JMeterUtils.getResString("path_extension_dont_use_equals")); // $NON-NLS-1$
        mainPanel.add(pathExtNoEquals);

        pathExtNoQuestionmark = new JCheckBox(JMeterUtils.getResString("path_extension_dont_use_questionmark")); // $NON-NLS-1$
        mainPanel.add(pathExtNoQuestionmark);

        shouldCache = new JCheckBox(JMeterUtils.getResString("cache_session_id")); // $NON-NLS-1$
        shouldCache.setSelected(true);
        mainPanel.add(shouldCache);

        encode = new JCheckBox(JMeterUtils.getResString("encode")); // $NON-NLS-1$
        encode.setSelected(false);
        mainPanel.add(encode);

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        URLRewritingModifier modifier = new URLRewritingModifier();
        modifyTestElement(modifier);
        return modifier;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement modifier) {
        super.configureTestElement(modifier);
        URLRewritingModifier rewritingModifier = (URLRewritingModifier) modifier;
        rewritingModifier.setArgumentName(argumentName.getText());
        rewritingModifier.setPathExtension(pathExt.isSelected());
        rewritingModifier.setPathExtensionNoEquals(pathExtNoEquals.isSelected());
        rewritingModifier.setPathExtensionNoQuestionmark(pathExtNoQuestionmark.isSelected());
        rewritingModifier.setShouldCache(shouldCache.isSelected());
        rewritingModifier.setEncode(encode.isSelected());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();

        argumentName.setText(""); //$NON-NLS-1$
        pathExt.setSelected(false);
        pathExtNoEquals.setSelected(false);
        pathExtNoQuestionmark.setSelected(false);
        shouldCache.setSelected(false);
        encode.setSelected(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement el) {
        URLRewritingModifier rewritingModifier = (URLRewritingModifier) el;
        argumentName.setText(rewritingModifier.getArgumentName());
        pathExt.setSelected(rewritingModifier.isPathExtension());
        pathExtNoEquals.setSelected(rewritingModifier.isPathExtensionNoEquals());
        pathExtNoQuestionmark.setSelected(rewritingModifier.isPathExtensionNoQuestionmark());
        shouldCache.setSelected(rewritingModifier.shouldCache());
        encode.setSelected(rewritingModifier.encode());
        super.configure(el);
    }
}
