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

package org.apache.jmeter.visualizers.backend;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.reflect.LogAndIgnoreServiceLoadExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BackendListenerGui} class provides the user interface for the
 * {@link BackendListener} object.
 * @since 2.13
 */
@GUIMenuSortOrder(4)
@TestElementMetadata(labelResource = "backend_listener")
public class BackendListenerGui extends AbstractListenerGui implements ActionListener {

    private static final long serialVersionUID = 1L;

    /** Logging */
    private static final Logger log = LoggerFactory.getLogger(BackendListenerGui.class);

    /** A combo box allowing the user to choose a backend class. */
    private JComboBox<String> classnameCombo;

    /**
     * A field allowing the user to specify the size of Queue
     */
    private JTextField queueSize;

    /** A panel allowing the user to set arguments for this test. */
    private ArgumentsPanel argsPanel;

    /** The current className of the Backend listener **/
    private String className;

    /**
     * Create a new BackendListenerGui as a standalone component.
     */
    public BackendListenerGui() {
        super();
        init();
    }


    /** {@inheritDoc} */
    @Override
    public String getLabelResource() {
        return "backend_listener"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(0, 5));

        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel classnameRequestPanel = new JPanel(new BorderLayout(0, 5));
        classnameRequestPanel.add(createClassnamePanel(), BorderLayout.NORTH);
        classnameRequestPanel.add(createParameterPanel(), BorderLayout.CENTER);

        add(classnameRequestPanel, BorderLayout.CENTER);
        className = ((String) classnameCombo.getSelectedItem()).trim();
    }

    /**
     * Create a panel with GUI components allowing the user to select a test
     * class.
     *
     * @return a panel containing the relevant components
     */
    private JPanel createClassnamePanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("backend_listener_classname")); // $NON-NLS-1$

        String[] listenerClasses = JMeterUtils.loadServicesAndScanJars(
                        BackendListenerClient.class,
                        ServiceLoader.load(BackendListenerClient.class),
                        Thread.currentThread().getContextClassLoader(),
                        new LogAndIgnoreServiceLoadExceptionHandler(log)
                ).stream()
                .map(s -> s.getClass().getName())
                .sorted()
                .toArray(String[]::new);
        classnameCombo = new JComboBox<>(listenerClasses);
        classnameCombo.addActionListener(this);
        classnameCombo.setEditable(false);
        label.setLabelFor(classnameCombo);

        HorizontalPanel classNamePanel = new HorizontalPanel();
        classNamePanel.add(label);
        classNamePanel.add(classnameCombo);

        queueSize = new JTextField(BackendListener.DEFAULT_QUEUE_SIZE, 5);
        queueSize.setName("Queue Size"); //$NON-NLS-1$
        JLabel queueSizeLabel = new JLabel(JMeterUtils.getResString("backend_listener_queue_size")); // $NON-NLS-1$
        queueSizeLabel.setLabelFor(queueSize);
        HorizontalPanel queueSizePanel = new HorizontalPanel();
        queueSizePanel.add(queueSizeLabel, BorderLayout.WEST);
        queueSizePanel.add(queueSize);

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.add(classNamePanel, BorderLayout.NORTH);
        panel.add(queueSizePanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Handle action events for this component. This method currently handles
     * events for the classname combo box.
     *
     * @param event
     *            the ActionEvent to be handled
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == classnameCombo) {

            String newClassName = ((String) classnameCombo.getSelectedItem()).trim();
            try {
                BackendListenerClient client = createBackendListenerClient(newClassName);
                BackendListenerClient oldClient = createBackendListenerClient(className);

                Arguments currArgs = new Arguments();
                argsPanel.modifyTestElement(currArgs);
                Map<String, String> currArgsMap = currArgs.getArgumentsAsMap();
                Map<String, String> userArgMap = new HashMap<>();
                userArgMap.putAll(currArgsMap);
                Arguments defaultArgs = extractDefaultArguments(client, userArgMap, oldClient.getDefaultParameters());
                Arguments newArgs = copyDefaultArguments(currArgsMap, defaultArgs);
                userArgMap.forEach(newArgs::addArgument);

                className = newClassName;
                argsPanel.configure(newArgs);
            } catch (Exception e) {
                log.error("Error getting argument list for {}", newClassName, e);
            }
        }
    }


    private static Arguments copyDefaultArguments(Map<String, String> currArgsMap, Arguments defaultArgs) {
        Arguments newArgs = new Arguments();
        if (defaultArgs != null) {
            for (JMeterProperty jMeterProperty : defaultArgs.getArguments()) {
                Argument arg = (Argument) jMeterProperty.getObjectValue();
                String name = arg.getName();
                String value = arg.getValue();

                // If a user has set parameters in one test, and then
                // selects a different test which supports the same
                // parameters, those parameters should have the same
                // values that they did in the original test.
                if (currArgsMap.containsKey(name)) {
                    String newVal = currArgsMap.get(name);
                    if (StringUtils.isNotBlank(newVal)) {
                        value = newVal;
                    }
                }
                newArgs.addArgument(name, value);
            }
        }
        return newArgs;
    }


    private static Arguments extractDefaultArguments(BackendListenerClient client, Map<String, String> userArgMap,
            Arguments currentUserArguments) {
        Arguments defaultArgs = null;
        try {
            defaultArgs = client.getDefaultParameters();
            if(currentUserArguments != null) {
                userArgMap.keySet().removeAll(currentUserArguments.getArgumentsAsMap().keySet());
            }
        } catch (AbstractMethodError e) {
            log.warn("BackendListenerClient doesn't implement "
                    + "getDefaultParameters.  Default parameters won't "
                    + "be shown.  Please update your client class: {}", client.getClass().getName());
        }
        return defaultArgs;
    }


    private static BackendListenerClient createBackendListenerClient(String newClassName)
            throws ReflectiveOperationException {
        return Class.forName(newClassName, true,
                Thread.currentThread().getContextClassLoader())
                .asSubclass(BackendListenerClient.class)
                .getDeclaredConstructor().newInstance();
    }

    /**
     * Create a panel containing components allowing the user to provide
     * arguments to be passed to the test class instance.
     *
     * @return a panel containing the relevant components
     */
    private JPanel createParameterPanel() {
        argsPanel = new ArgumentsPanel(JMeterUtils.getResString("backend_listener_paramtable")); // $NON-NLS-1$
        return argsPanel;
    }

    /** {@inheritDoc} */
    @Override
    public void configure(TestElement config) {
        super.configure(config);

        argsPanel.configure((Arguments) config.getProperty(BackendListener.ARGUMENTS).getObjectValue());

        className = config.getPropertyAsString(BackendListener.CLASSNAME);
        if(checkContainsClassName(classnameCombo.getModel(), className)) {
            classnameCombo.setSelectedItem(className);
        } else {
            log.error(
                    "Error setting class: '{}' in BackendListener: {}, check for a missing jar in"
                    + " your jmeter 'search_paths' and 'plugin_dependency_paths' properties",
                    className, getName());
        }
        queueSize.setText(((BackendListener)config).getQueueSize());
    }

    /**
     * Check combo contains className
     * @param model ComboBoxModel
     * @param className String class name
     * @return boolean true if model contains className
     */
    private static boolean checkContainsClassName(
            ComboBoxModel<?> model, String className) {
        int size = model.getSize();
        Set<String> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add((String)model.getElementAt(i));
        }
        return set.contains(className);
    }

    /** {@inheritDoc} */
    @Override
    public TestElement createTestElement() {
        BackendListener config = new BackendListener();
        modifyTestElement(config);
        return config;
    }

    /** {@inheritDoc} */
    @Override
    public void modifyTestElement(TestElement config) {
        configureTestElement(config);
        BackendListener backendListener = (BackendListener) config;
        backendListener.setArguments((Arguments) argsPanel.createTestElement());
        backendListener.setClassname(String.valueOf(classnameCombo.getSelectedItem()));
        backendListener.setQueueSize(queueSize.getText());
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#clearGui()
     */
    @Override
    public void clearGui() {
        super.clearGui();
        argsPanel.clearGui();
        classnameCombo.setSelectedIndex(0);
        queueSize.setText(BackendListener.DEFAULT_QUEUE_SIZE);
    }
}
