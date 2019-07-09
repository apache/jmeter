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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * The GUI for the HTTP Cache Manager {@link CacheManager}
 */
@GUIMenuSortOrder(4)
public class CacheManagerGui extends AbstractConfigGui implements ActionListener {

    private static final long serialVersionUID = 240L;
    private static final String CONTROLLED_BY_THREADGROUP = "controlled_By_ThreadGroup"; //$NON-NLS-1$

    private JCheckBox clearEachIteration;
    private JCheckBox useExpires;
    private JTextField maxCacheSize;
    private JCheckBox controlledByThreadGroup;

    public CacheManagerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "cache_manager_title"; // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final CacheManager cacheManager = (CacheManager)element;
        useExpires.setSelected(cacheManager.getUseExpires());
        maxCacheSize.setText(Integer.toString(cacheManager.getMaxSize()));
        controlledByThreadGroup.setSelected(cacheManager.getControlledByThread());
        clearEachIteration.setSelected(cacheManager.getClearEachIteration());
    }

    @Override
    public TestElement createTestElement() {
        CacheManager element = new CacheManager();
        modifyTestElement(element);
        controlledByThreadGroup.setSelected(element.getControlledByThread());
        clearEachIteration.setEnabled(!element.getControlledByThread());
        return element;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        final CacheManager cacheManager = (CacheManager)element;
        cacheManager.setClearEachIteration(clearEachIteration.isSelected());
        cacheManager.setUseExpires(useExpires.isSelected());
        cacheManager.setControlledByThread(controlledByThreadGroup.isSelected());
        try {
            cacheManager.setMaxSize(Integer.parseInt(maxCacheSize.getText()));
        } catch (NumberFormatException ignored) {
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
        useExpires.setSelected(true);
        maxCacheSize.setText(""); //$NON-NLS-1$
        controlledByThreadGroup.setSelected(false);
    }

    /**
     * Initialize the components and layout of this component.
     * WARNING: called from ctor so must not be overridden (i.e. must be private or final)
     */
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        clearEachIteration = new JCheckBox(JMeterUtils.getResString("clear_cache_per_iter"), false); // $NON-NLS-1$

        controlledByThreadGroup =
                new JCheckBox(JMeterUtils.getResString("cache_clear_controlled_by_threadgroup"), false); //$NON-NLS-1$
        controlledByThreadGroup.setActionCommand(CONTROLLED_BY_THREADGROUP);
        controlledByThreadGroup.addActionListener(this);

        useExpires = new JCheckBox(JMeterUtils.getResString("use_expires"), false); // $NON-NLS-1$

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        northPanel.add(makeTitlePanel());
        northPanel.add(clearEachIteration);
        northPanel.add(controlledByThreadGroup);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(CONTROLLED_BY_THREADGROUP)) {
            clearEachIteration.setEnabled(!controlledByThreadGroup.isSelected());
        }
    }
}
