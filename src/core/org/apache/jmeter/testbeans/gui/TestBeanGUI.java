/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPopupMenu;

import org.apache.commons.collections.map.LRUMap;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.AbstractProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.timers.gui.AbstractTimerGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * JMeter GUI element editing for TestBean elements.
 * <p>
 * The actual GUI is always a bean customizer: if the bean descriptor provides
 * one, it will be used; otherwise, a GenericTestBeanCustomizer will be created
 * for this purpose.
 * <p>
 * Those customizers deviate from the standards only in that, instead of a bean,
 * they will receive a Map in the setObject call. This will be a property name
 * to value Map. The customizer is also in charge of initializing empty Maps
 * with sensible initial values.
 * <p>
 * If the provided Customizer class implements the SharedCustomizer interface,
 * the same instance of the customizer will be reused for all beans of the type:
 * setObject(map) can then be called multiple times. Otherwise, one separate
 * instance will be used for each element. For efficiency reasons, most
 * customizers should implement SharedCustomizer.
 *
 */
public class TestBeanGUI extends AbstractJMeterGuiComponent implements JMeterGUIComponent {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final Class<?> testBeanClass;

    private transient final BeanInfo beanInfo;

    private final Class<?> customizerClass;

    /**
     * The single customizer if the customizer class implements
     * SharedCustomizer, null otherwise.
     */
    private Customizer customizer = null;

    /**
     * TestElement to Customizer map if customizer is null. This is necessary to
     * avoid the cost of creating a new customizer on each edit. The cache size
     * needs to be limited, though, to avoid memory issues when editing very
     * large test plans.
     */
    @SuppressWarnings("unchecked")
    private final Map<TestElement, Customizer> customizers = new LRUMap(20);

    /**
     * Index of the customizer in the JPanel's child component list:
     */
    private int customizerIndexInPanel;

    /**
     * The property name to value map that the active customizer edits:
     */
    private final Map<String, Object> propertyMap = new HashMap<String, Object>();

    /**
     * Whether the GUI components have been created.
     */
    private boolean initialized = false;

    static {
        List<String> paths = new LinkedList<String>();
        paths.add("org.apache.jmeter.testbeans.gui");// $NON-NLS-1$
        paths.addAll(Arrays.asList(PropertyEditorManager.getEditorSearchPath()));
        String s = JMeterUtils.getPropDefault("propertyEditorSearchPath", null);// $NON-NLS-1$
        if (s != null) {
            paths.addAll(Arrays.asList(JOrphanUtils.split(s, ",", "")));// $NON-NLS-1$ // $NON-NLS-2$
        }
        PropertyEditorManager.setEditorSearchPath(paths.toArray(new String[0]));
    }

    /**
     * @deprecated Dummy for JUnit test purposes only
     */
    @Deprecated
    public TestBeanGUI() {
        log.warn("Constructor only for use in testing");// $NON-NLS-1$
        testBeanClass = null;
        customizerClass = null;
        beanInfo = null;
    }

    public TestBeanGUI(Class<?> testBeanClass) {
        super();
        log.debug("testing class: " + testBeanClass.getName());
        // A quick verification, just in case:
        if (!TestBean.class.isAssignableFrom(testBeanClass)) {
            Error e = new Error();
            log.error("This should never happen!", e);
            throw e; // Programming error: bail out.
        }

        this.testBeanClass = testBeanClass;

        // Get the beanInfo:
        try {
            beanInfo = Introspector.getBeanInfo(testBeanClass);
        } catch (IntrospectionException e) {
            log.error("Can't get beanInfo for " + testBeanClass.getName(), e);
            throw new Error(e.toString()); // Programming error. Don't
                                            // continue.
        }

        customizerClass = beanInfo.getBeanDescriptor().getCustomizerClass();

        // Creation of the customizer and GUI initialization is delayed until
        // the
        // first
        // configure call. We don't need all that just to find out the static
        // label, menu
        // categories, etc!
        initialized = false;
    }

    private Customizer createCustomizer() {
        try {
            return (Customizer) customizerClass.newInstance();
        } catch (InstantiationException e) {
            log.error("Could not instantiate customizer of class " + customizerClass, e);
            throw new Error(e.toString());
        } catch (IllegalAccessException e) {
            log.error("Could not instantiate customizer of class " + customizerClass, e);
            throw new Error(e.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStaticLabel() {
        if (beanInfo == null){
            return "null";// $NON-NLS-1$
        }
        return beanInfo.getBeanDescriptor().getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
   public TestElement createTestElement() {
        try {
            TestElement element = (TestElement) testBeanClass.newInstance();
            // configure(element);
            // super.clear(); // set name, enabled.
            modifyTestElement(element); // put the default values back into the
            // new element
            return element;
        } catch (InstantiationException e) {
            log.error("Can't create test element", e);
            throw new Error(e.toString()); // Programming error. Don't
                                            // continue.
        } catch (IllegalAccessException e) {
            log.error("Can't create test element", e);
            throw new Error(e.toString()); // Programming error. Don't
                                            // continue.
        }
    }

   /**
    * {@inheritDoc}
    */
    public void modifyTestElement(TestElement element) {
        // Fetch data from screen fields
        if (customizer instanceof GenericTestBeanCustomizer) {
            GenericTestBeanCustomizer gtbc = (GenericTestBeanCustomizer) customizer;
            gtbc.saveGuiFields();
        }
        configureTestElement(element);

        // Copy all property values from the map into the element:
        for (PropertyDescriptor desc : beanInfo.getPropertyDescriptors()) {
            String name = desc.getName();
            Object value = propertyMap.get(name);
            log.debug("Modify " + name + " to " + value);
            if (value == null) {
                if (GenericTestBeanCustomizer.notNull(desc)) { // cannot be null
                    setPropertyInElement(element, name, desc.getValue(GenericTestBeanCustomizer.DEFAULT));
                } else {
                    element.removeProperty(name);
                }
            } else {
                setPropertyInElement(element, name, value);
            }
        }
    }

    /**
     * @param element
     * @param name
     */
    private void setPropertyInElement(TestElement element, String name, Object value) {
        JMeterProperty jprop = AbstractProperty.createProperty(value);
        jprop.setName(name);
        element.setProperty(jprop);
    }

    /**
     * {@inheritDoc}
     */
    public JPopupMenu createPopupMenu() {
        if (Timer.class.isAssignableFrom(testBeanClass))
        {
            return MenuFactory.getDefaultTimerMenu();
        }
        else if(Sampler.class.isAssignableFrom(testBeanClass))
        {
            return MenuFactory.getDefaultSamplerMenu();
        }
        else if(ConfigElement.class.isAssignableFrom(testBeanClass))
        {
            return MenuFactory.getDefaultConfigElementMenu();
        }
        else if(Assertion.class.isAssignableFrom(testBeanClass))
        {
            return MenuFactory.getDefaultAssertionMenu();
        }
        else if(PostProcessor.class.isAssignableFrom(testBeanClass) ||
                PreProcessor.class.isAssignableFrom(testBeanClass))
        {
            return MenuFactory.getDefaultExtractorMenu();
        }
        else if(Visualizer.class.isAssignableFrom(testBeanClass))
        {
            return MenuFactory.getDefaultVisualizerMenu();
        }
        else if(Controller.class.isAssignableFrom(testBeanClass))
        {
            return MenuFactory.getDefaultControllerMenu();
        }
        else {
            log.warn("Cannot determine PopupMenu for "+testBeanClass.getName());
            return MenuFactory.getDefaultMenu();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        if (!initialized){
            init();
        }
        clearGui();

        super.configure(element);

        // Copy all property values into the map:
        for (PropertyIterator jprops = element.propertyIterator(); jprops.hasNext();) {
            JMeterProperty jprop = jprops.next();
            propertyMap.put(jprop.getName(), jprop.getObjectValue());
        }

        if (customizer != null) {
            customizer.setObject(propertyMap);
        } else {
            if (initialized){
                remove(customizerIndexInPanel);
            }
            Customizer c = customizers.get(element);
            if (c == null) {
                c = createCustomizer();
                c.setObject(propertyMap);
                customizers.put(element, c);
            }
            add((Component) c, BorderLayout.CENTER);
        }

        initialized = true;
    }

    /** {@inheritDoc} */
    public Collection<String> getMenuCategories() {
        List<String> menuCategories = new LinkedList<String>();
        BeanDescriptor bd = beanInfo.getBeanDescriptor();

        // We don't want to show expert beans in the menus unless we're
        // in expert mode:
        if (bd.isExpert() && !JMeterUtils.isExpertMode()) {
            return null;
        }

        int matches = 0; // How many classes can we assign from?
        // TODO: there must be a nicer way...
        if (Assertion.class.isAssignableFrom(testBeanClass)) {
            menuCategories.add(MenuFactory.ASSERTIONS);
            bd.setValue(TestElement.GUI_CLASS, AbstractAssertionGui.class.getName());
            matches++;
        }
        if (ConfigElement.class.isAssignableFrom(testBeanClass)) {
            menuCategories.add(MenuFactory.CONFIG_ELEMENTS);
            bd.setValue(TestElement.GUI_CLASS, AbstractConfigGui.class.getName());
            matches++;
        }
        if (Controller.class.isAssignableFrom(testBeanClass)) {
            menuCategories.add(MenuFactory.CONTROLLERS);
            bd.setValue(TestElement.GUI_CLASS, AbstractControllerGui.class.getName());
            matches++;
        }
        if (Visualizer.class.isAssignableFrom(testBeanClass)) {
            menuCategories.add(MenuFactory.LISTENERS);
            bd.setValue(TestElement.GUI_CLASS, AbstractVisualizer.class.getName());
            matches++;
        }
        if (PostProcessor.class.isAssignableFrom(testBeanClass)) {
            menuCategories.add(MenuFactory.POST_PROCESSORS);
            bd.setValue(TestElement.GUI_CLASS, AbstractPostProcessorGui.class.getName());
            matches++;
        }
        if (PreProcessor.class.isAssignableFrom(testBeanClass)) {
            matches++;
            menuCategories.add(MenuFactory.PRE_PROCESSORS);
            bd.setValue(TestElement.GUI_CLASS, AbstractPreProcessorGui.class.getName());
        }
        if (Sampler.class.isAssignableFrom(testBeanClass)) {
            matches++;
            menuCategories.add(MenuFactory.SAMPLERS);
            bd.setValue(TestElement.GUI_CLASS, AbstractSamplerGui.class.getName());
        }
        if (Timer.class.isAssignableFrom(testBeanClass)) {
            matches++;
            menuCategories.add(MenuFactory.TIMERS);
            bd.setValue(TestElement.GUI_CLASS, AbstractTimerGui.class.getName());
        }
        if (matches == 0) {
            log.error("Could not assign GUI class to " + testBeanClass.getName());
        } else if (matches > 1) {// may be impossible, but no harm in
                                    // checking ...
            log.error("More than 1 GUI class found for " + testBeanClass.getName());
        }
        return menuCategories;
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));

        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        customizerIndexInPanel = getComponentCount();

        if (customizerClass == null) {
            customizer = new GenericTestBeanCustomizer(beanInfo);
        } else if (SharedCustomizer.class.isAssignableFrom(customizerClass)) {
            customizer = createCustomizer();
        }

        if (customizer != null){
            add((Component) customizer, BorderLayout.CENTER);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getLabelResource() {
        // @see getStaticLabel
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        if (customizer instanceof GenericTestBeanCustomizer) {
            GenericTestBeanCustomizer gtbc = (GenericTestBeanCustomizer) customizer;
            gtbc.clearGuiFields();
        }
    }

    public boolean isHidden() {
        return beanInfo.getBeanDescriptor().isHidden();
    }

    public boolean isExpert() {
        return beanInfo.getBeanDescriptor().isExpert();
    }
}
