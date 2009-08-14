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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

//For unit tests, @see TestHttpTestSampleGui

/**
 * HTTP Sampler GUI
 *
 */
public class HttpTestSampleGui extends AbstractSamplerGui {
    private MultipartUrlConfigGui urlConfigGui;

    private JCheckBox getImages;

    private JCheckBox isMon;

    private JCheckBox useMD5;

    private JLabeledTextField embeddedRE; // regular expression used to match against embedded resource URLs

    public HttpTestSampleGui() {
        init();
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) element;
        urlConfigGui.configure(element);
        getImages.setSelected(samplerBase.isImageParser());
        isMon.setSelected(samplerBase.isMonitor());
        useMD5.setSelected(samplerBase.useMD5());
        embeddedRE.setText(samplerBase.getEmbeddedUrlRE());
    }

    public TestElement createTestElement() {
        HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance();// create default sampler
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        urlConfigGui.modifyTestElement(sampler);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) sampler;
        if (getImages.isSelected()) {
            samplerBase.setImageParser(true);
        } else {
            // The default is false, so we can remove the property to simplify JMX files
            // This also allows HTTPDefaults to work for this checkbox
            sampler.removeProperty(HTTPSamplerBase.IMAGE_PARSER);
        }
        samplerBase.setMonitor(isMon.isSelected());
        samplerBase.setMD5(useMD5.isSelected());
        samplerBase.setEmbeddedUrlRE(embeddedRE.getText());
        this.configureTestElement(sampler);
    }

    public String getLabelResource() {
        return "web_testing_title"; // $NON-NLS-1$
    }

    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        // URL CONFIG
        urlConfigGui = new MultipartUrlConfigGui();
        add(urlConfigGui, BorderLayout.CENTER);

        // OPTIONAL TASKS
        add(createOptionalTasksPanel(), BorderLayout.SOUTH);
    }

    private JPanel createOptionalTasksPanel() {
        // OPTIONAL TASKS
        JPanel optionalTasksPanel = new VerticalPanel();
        optionalTasksPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$

        JPanel checkBoxPanel = new HorizontalPanel();
        // RETRIEVE IMAGES
        getImages = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
        // Is monitor
        isMon = new JCheckBox(JMeterUtils.getResString("monitor_is_title")); // $NON-NLS-1$
        // Use MD5
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$

        checkBoxPanel.add(getImages);
        checkBoxPanel.add(isMon);
        checkBoxPanel.add(useMD5);
        optionalTasksPanel.add(checkBoxPanel);

        // Embedded URL match regex
        embeddedRE = new JLabeledTextField(JMeterUtils.getResString("web_testing_embedded_url_pattern"),30); // $NON-NLS-1$
        optionalTasksPanel.add(embeddedRE);
        return optionalTasksPanel;
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#clearGui()
     */
    @Override
    public void clearGui() {
        super.clearGui();
        getImages.setSelected(false);
        isMon.setSelected(false);
        useMD5.setSelected(false);
        urlConfigGui.clear();
        embeddedRE.setText(""); // $NON-NLS-1$
    }
}
