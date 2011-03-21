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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
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
import org.apache.jorphan.gui.JLabeledTextField;

//For unit tests, @see TestHttpTestSampleGui

/**
 * HTTP Sampler GUI
 *
 */
public class HttpTestSampleGui extends AbstractSamplerGui 
    implements ItemListener {
    private static final long serialVersionUID = 240L;

    private MultipartUrlConfigGui urlConfigGui;

    private JCheckBox getImages;
    
    private JCheckBox concurrentDwn;
    
    private JTextField concurrentPool; 

    private JCheckBox isMon;

    private JCheckBox useMD5;

    private JLabeledTextField embeddedRE; // regular expression used to match against embedded resource URLs

    private JLabeledTextField sourceIpAddr; // does not apply to Java implementation

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
        }
    }

    /**
     * {@inheritDoc}
     */
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
        }
        this.configureTestElement(sampler);
    }

    /**
     * {@inheritDoc}
     */
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

        // OPTIONAL TASKS
        add(createOptionalTasksPanel(), BorderLayout.SOUTH);
    }

    protected JPanel createOptionalTasksPanel() {
        // OPTIONAL TASKS
        final JPanel optionalTasksPanel = new VerticalPanel();
        optionalTasksPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$

        final JPanel checkBoxPanel = new HorizontalPanel();
        // RETRIEVE IMAGES
        getImages = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
        // add a listener to activate or not concurrent dwn.
        getImages.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) { enableConcurrentDwn(true); }
                else { enableConcurrentDwn(false); }
            }
        });
        // Download concurrent resources
        concurrentDwn = new JCheckBox(JMeterUtils.getResString("web_testing_concurrent_download")); // $NON-NLS-1$
        concurrentDwn.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) { concurrentPool.setEnabled(true); }
                else { concurrentPool.setEnabled(false); }
            }
        });
        concurrentPool = new JTextField(2); // 2 column size
        concurrentPool.setMaximumSize(new Dimension(30,20));
        // Is monitor
        isMon = new JCheckBox(JMeterUtils.getResString("monitor_is_title")); // $NON-NLS-1$
        // Use MD5
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$

        checkBoxPanel.add(getImages);
        checkBoxPanel.add(concurrentDwn);
        checkBoxPanel.add(concurrentPool);
        checkBoxPanel.add(isMon);
        checkBoxPanel.add(useMD5);
        optionalTasksPanel.add(checkBoxPanel);

        // Embedded URL match regex
        embeddedRE = new JLabeledTextField(JMeterUtils.getResString("web_testing_embedded_url_pattern"),30); // $NON-NLS-1$
        optionalTasksPanel.add(embeddedRE, BorderLayout.CENTER);

        if (!isAJP) {
            // Add a new field source ip address (for HC implementations only)
            sourceIpAddr = new JLabeledTextField(JMeterUtils.getResString("web_testing2_source_ip")); // $NON-NLS-1$
            optionalTasksPanel.add(sourceIpAddr, BorderLayout.EAST);
        }

        return optionalTasksPanel;
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
        }
    }
    
    private void enableConcurrentDwn(boolean enable) {
        if (enable) {
            concurrentDwn.setEnabled(true);
            if (concurrentDwn.isSelected()) {
                concurrentPool.setEnabled(true);
            }
        } else {
            concurrentDwn.setEnabled(false);
            concurrentPool.setEnabled(false);
        }
    }

    public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            enableConcurrentDwn(true);
        } else {
            enableConcurrentDwn(false);
        }
    }

}
