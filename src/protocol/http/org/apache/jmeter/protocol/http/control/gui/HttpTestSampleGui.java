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
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

//For unit tests, @see TestHttpTestSampleGui

/**
 * HTTP Sampler GUI
 *
 */
public class HttpTestSampleGui extends AbstractSamplerGui 
    implements ItemListener {
    private static final long serialVersionUID = 240L;
    
    private static final Font FONT_VERY_SMALL = new Font("SansSerif", Font.PLAIN, 9);
    
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    private MultipartUrlConfigGui urlConfigGui;

    private JCheckBox getImages;
    
    private JCheckBox concurrentDwn;
    
    private JTextField concurrentPool; 

    private JCheckBox isMon;

    private JCheckBox useMD5;

    private JLabel labelEmbeddedRE = new JLabel(JMeterUtils.getResString("web_testing_embedded_url_pattern")); // $NON-NLS-1$

    private JTextField embeddedRE; // regular expression used to match against embedded resource URLs

    private JTextField sourceIpAddr; // does not apply to Java implementation
    
    private JComboBox sourceIpType = new JComboBox(HTTPSamplerBase.getSourceTypeList());

    private final boolean isAJP;
    
    public HttpTestSampleGui() {
        isAJP = false;
        init();
    }

    // For use by AJP
    protected HttpTestSampleGui(boolean ajp) {
        isAJP = ajp;
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) element;
        urlConfigGui.configure(element);
        getImages.setSelected(samplerBase.isImageParser());
        concurrentDwn.setSelected(samplerBase.isConcurrentDwn());
        concurrentPool.setText(samplerBase.getConcurrentPool());
        isMon.setSelected(samplerBase.isMonitor());
        useMD5.setSelected(samplerBase.useMD5());
        embeddedRE.setText(samplerBase.getEmbeddedUrlRE());
        if (!isAJP) {
            sourceIpAddr.setText(samplerBase.getIpSource());
            sourceIpType.setSelectedIndex(samplerBase.getIpSourceType());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        HTTPSamplerBase sampler = new HTTPSamplerProxy();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        urlConfigGui.modifyTestElement(sampler);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) sampler;
        samplerBase.setImageParser(getImages.isSelected());
        enableConcurrentDwn(getImages.isSelected());
        samplerBase.setConcurrentDwn(concurrentDwn.isSelected());
        samplerBase.setConcurrentPool(concurrentPool.getText());
        samplerBase.setMonitor(isMon.isSelected());
        samplerBase.setMD5(useMD5.isSelected());
        samplerBase.setEmbeddedUrlRE(embeddedRE.getText());
        if (!isAJP) {
            samplerBase.setIpSource(sourceIpAddr.getText());
            samplerBase.setIpSourceType(sourceIpType.getSelectedIndex());
        }
        this.configureTestElement(sampler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return "web_testing_title"; // $NON-NLS-1$
    }

    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        // URL CONFIG
        urlConfigGui = new MultipartUrlConfigGui(true, !isAJP);
        add(urlConfigGui, BorderLayout.CENTER);

        // Bottom (embedded resources, source address and optional tasks)
        JPanel bottomPane = new VerticalPanel();
        bottomPane.add(createEmbeddedRsrcPanel());
        JPanel optionAndSourcePane = new HorizontalPanel();
        optionAndSourcePane.add(createSourceAddrPanel());
        optionAndSourcePane.add(createOptionalTasksPanel());
        bottomPane.add(optionAndSourcePane);
        add(bottomPane, BorderLayout.SOUTH);
    }

    protected JPanel createEmbeddedRsrcPanel() {
        final JPanel embeddedRsrcPanel = new VerticalPanel();
        embeddedRsrcPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_retrieve_title"))); // $NON-NLS-1$

        final JPanel checkBoxPanel = new HorizontalPanel();
        // RETRIEVE IMAGES
        getImages = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
        getImages.setFont(FONT_SMALL);
        // add a listener to activate or not concurrent dwn.
        getImages.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) { enableConcurrentDwn(true); }
                else { enableConcurrentDwn(false); }
            }
        });
        // Download concurrent resources
        concurrentDwn = new JCheckBox(JMeterUtils.getResString("web_testing_concurrent_download")); // $NON-NLS-1$
        concurrentDwn.setFont(FONT_SMALL);
        concurrentDwn.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (getImages.isSelected() && e.getStateChange() == ItemEvent.SELECTED) { concurrentPool.setEnabled(true); }
                else { concurrentPool.setEnabled(false); }
            }
        });
        concurrentPool = new JTextField(2); // 2 column size
        concurrentPool.setFont(FONT_SMALL);
        concurrentPool.setMaximumSize(new Dimension(30,20));

        checkBoxPanel.add(getImages);
        checkBoxPanel.add(concurrentDwn);
        checkBoxPanel.add(concurrentPool);
        embeddedRsrcPanel.add(checkBoxPanel);

        // Embedded URL match regex
        labelEmbeddedRE.setFont(FONT_SMALL);
        checkBoxPanel.add(labelEmbeddedRE);
        embeddedRE = new JTextField(10);
        checkBoxPanel.add(embeddedRE);
        embeddedRsrcPanel.add(checkBoxPanel);

        return embeddedRsrcPanel;
    }

    protected JPanel createOptionalTasksPanel() {
        // OPTIONAL TASKS
        final JPanel checkBoxPanel = new HorizontalPanel();
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$

        // Is monitor
        isMon = new JCheckBox(JMeterUtils.getResString("monitor_is_title")); // $NON-NLS-1$
        isMon.setFont(FONT_SMALL);
        // Use MD5
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$
        useMD5.setFont(FONT_SMALL);

        checkBoxPanel.add(isMon);
        checkBoxPanel.add(useMD5);

        return checkBoxPanel;
    }
    
    protected JPanel createSourceAddrPanel() {
        final JPanel sourceAddrPanel = new HorizontalPanel();
        sourceAddrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_source_ip"))); // $NON-NLS-1$

        if (!isAJP) {
            // Add a new field source ip address (for HC implementations only)
            sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
            sourceIpType.setFont(FONT_VERY_SMALL);
            sourceAddrPanel.add(sourceIpType);

            sourceIpAddr = new JTextField();
            sourceAddrPanel.add(sourceIpAddr);
        }
        return sourceAddrPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        getImages.setSelected(false);
        concurrentDwn.setSelected(false);
        concurrentPool.setText(String.valueOf(HTTPSamplerBase.CONCURRENT_POOL_SIZE));
        enableConcurrentDwn(false);
        isMon.setSelected(false);
        useMD5.setSelected(false);
        urlConfigGui.clear();
        embeddedRE.setText(""); // $NON-NLS-1$
        if (!isAJP) {
            sourceIpAddr.setText(""); // $NON-NLS-1$
            sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        }
    }
    
    private void enableConcurrentDwn(boolean enable) {
        if (enable) {
            concurrentDwn.setEnabled(true);
            labelEmbeddedRE.setEnabled(true);
            embeddedRE.setEnabled(true);
            if (concurrentDwn.isSelected()) {
                concurrentPool.setEnabled(true);
            }
        } else {
            concurrentDwn.setEnabled(false);
            concurrentPool.setEnabled(false);
            labelEmbeddedRE.setEnabled(false);
            embeddedRE.setEnabled(false);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            enableConcurrentDwn(true);
        } else {
            enableConcurrentDwn(false);
        }
    }

}
