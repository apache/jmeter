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

package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * The GUI for the HTTP Cache Manager
 *
 */
public class CacheManagerGui extends AbstractConfigGui {

    private static final long serialVersionUID = 240L;

    private JCheckBox clearEachIteration;

    private JCheckBox useExpires;

    private JTextField maxCacheSize;

    /**
     * Create a new LoginConfigGui as a standalone component.
     */
    public CacheManagerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "cache_manager_title"; // $NON-NLS-1$
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param element
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final CacheManager cacheManager = (CacheManager)element;
        clearEachIteration.setSelected(cacheManager.getClearEachIteration());
        useExpires.setSelected(cacheManager.getUseExpires());
        maxCacheSize.setText(Integer.toString(cacheManager.getMaxSize()));
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        CacheManager element = new CacheManager();
        modifyTestElement(element);
        return element;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        final CacheManager cacheManager = (CacheManager)element;
        cacheManager.setClearEachIteration(clearEachIteration.isSelected());
        cacheManager.setUseExpires(useExpires.isSelected());
        try {
            cacheManager.setMaxSize(Integer.parseInt(maxCacheSize.getText()));
        } catch (NumberFormatException e) {
            // NOOP
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        clearEachIteration.setSelected(false);
        useExpires.setSelected(false);
        maxCacheSize.setText(""); //$NON-NLS-1$
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        clearEachIteration = new JCheckBox(JMeterUtils.getResString("clear_cache_per_iter"), false); // $NON-NLS-1$
        useExpires = new JCheckBox(JMeterUtils.getResString("use_expires"), false); // $NON-NLS-1$

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        northPanel.add(makeTitlePanel());
        northPanel.add(clearEachIteration);
        northPanel.add(useExpires);
        
        JLabel label = new JLabel(JMeterUtils.getResString("cache_manager_size")); //$NON-NLS-1$

        maxCacheSize = new JTextField(20);
        maxCacheSize.setName(CacheManager.MAX_SIZE);
        label.setLabelFor(maxCacheSize);
        JPanel maxCacheSizePanel = new JPanel(new BorderLayout(5, 0));
        maxCacheSizePanel.add(label, BorderLayout.WEST);
        maxCacheSizePanel.add(maxCacheSize, BorderLayout.CENTER);
        northPanel.add(maxCacheSizePanel);
        add(northPanel, BorderLayout.NORTH);
    }

}
