// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
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
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.BorderLayout;
import java.awt.Component;
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

import org.apache.commons.collections.LRUMap;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.DoubleProperty;
import org.apache.jmeter.testelement.property.FloatProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * JMeter GUI element editing for TestBean elements.
 * <p>
 * The actual GUI is always a bean customizer: if the bean descriptor provides
 * one, it will be used; otherwise, a GenericTestBeanCustomizer will be
 * created for this purpose.
 * <p>
 * Those customizers deviate from the standards only in that, instead of a
 * bean, they will receive a Map in the setObject call. This will be a property
 * name to value Map. The customizer is also in charge of initializing empty
 * Maps with sensible initial values.
 * <p>
 * If the provided Customizer class implements the SharedCustomizer
 * interface, the same instance of the customizer will be reused for all
 * beans of the type: setObject(map) can then be called multiple times.
 * Otherwise, one separate instance will be used for each element.
 * For efficiency reasons, most customizers should implement
 * SharedCustomizer.
 * 
 * @version $Revision$ updated on $Date$
 */
public class TestBeanGUI
    extends AbstractJMeterGuiComponent
    implements JMeterGUIComponent
{
    private static Logger log = LoggingManager.getLoggerForClass();

    private Class testBeanClass;
    
    private BeanInfo beanInfo;

    private Class customizerClass;

    /**
     * The single customizer if the customizer class implements
     * SharedCustomizer, null otherwise.
     */
    private Customizer customizer= null;

    /**
     * TestElement to Customizer map if customizer is null. This is necessary to avoid
     * the cost of creating a new customizer on each edit. The cache size needs to be
     * limited, though, to avoid memory issues when editing very large test plans.
     */
    private Map customizers= new LRUMap(20);

    /**
     * Index of the customizer in the JPanel's child component list:
     */
    private int customizerIndexInPanel;
    
    /**
     * The property name to value map that the active customizer
     * edits:
     */
    private Map propertyMap= new HashMap();

    /**
     * Whether the GUI components have been created. 
     */
    private boolean initialized= false;

    static
    {
        List paths= new LinkedList();
        paths.add("org.apache.jmeter.testbeans.gui");
        paths.addAll(Arrays.asList(PropertyEditorManager.getEditorSearchPath()));
        String s= JMeterUtils.getPropDefault("propertyEditorSearchPath", null) ;
        if (s != null)
        {
            paths.addAll(Arrays.asList(JMeterUtils.split(s, ",", "")));
        }
        PropertyEditorManager.setEditorSearchPath((String[])paths.toArray(new String[0]));
    }

    // Dummy for JUnit test
    public TestBeanGUI()
    {
    	log.warn("Only for use in testing");
    }
    
    public TestBeanGUI(Class testBeanClass)
    {
        super();
        
        // A quick verification, just in case:
        if (! TestBean.class.isAssignableFrom(testBeanClass))
        {
            Error e= new Error();
            log.error("This should never happen!", e);
            throw e; // Programming error: bail out.
        }

        this.testBeanClass= testBeanClass;
                
        // Get the beanInfo:
        try
        {
            beanInfo= Introspector.getBeanInfo(testBeanClass);
        }
        catch (IntrospectionException e)
        {
            log.error("Can't get beanInfo for "+testBeanClass.getName(),
                e);
            throw new Error(e.toString()); // Programming error. Don't continue.
        }

        customizerClass= beanInfo.getBeanDescriptor().getCustomizerClass();

        // Creation of the customizer and GUI initialization is delayed until the first
        // configure call. We don't need all that just to find out the static label, menu
        // categories, etc!
        initialized= false;
    }

    private Customizer createCustomizer()
    {
        try
        {
            return (Customizer)customizerClass.newInstance();
        }
        catch (InstantiationException e)
        {
            log.error("Could not instantiate customizer of class "+customizerClass, e);
            throw new Error(e.toString());
        }
        catch (IllegalAccessException e)
        {
            log.error("Could not instantiate customizer of class "+customizerClass, e);
            throw new Error(e.toString());
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
     */
    public String getStaticLabel()
    {
        if (beanInfo == null) return "null";
        return beanInfo.getBeanDescriptor().getDisplayName();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement()
    {
        try
        {
            TestElement element= (TestElement)testBeanClass.newInstance();
            configure(element);
            super.clear(); // set name, enabled.
            configureTestElement(element);
            modifyTestElement(element); // put the default values back into the new element
            return element;
        }
        catch (InstantiationException e)
        {
            log.error("Can't create test element", e);
            throw new Error(e.toString()); // Programming error. Don't continue.
        }
        catch (IllegalAccessException e)
        {
            log.error("Can't create test element", e);
            throw new Error(e.toString()); // Programming error. Don't continue.
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void modifyTestElement(TestElement element)
    {
        configureTestElement(element);
        
        // Copy all property values from the map into the element:
        PropertyDescriptor[] props= beanInfo.getPropertyDescriptors();
        for (int i=0; i<props.length; i++)
        {
            String name= props[i].getName();
            Object value= propertyMap.get(name);
            log.debug("Modify "+name+" to "+value);
            if (value == null)
            {
                element.removeProperty(name);
            }
            else
            {
                JMeterProperty jprop= wrapInProperty(propertyMap.get(name));
                jprop.setName(name);
                element.setProperty(jprop);
            }
        }
    }

    /**
     * Utility method to wrap an object in a property of an appropriate type.
     * <p>
     * I plan to get rid of this sooner than later, so please don't use it much.
     * 
     * @param value Object to be wrapped.
     * @return an unnamed property holding the provided value.
     * @deprecated
     */
    private static JMeterProperty wrapInProperty(Object value)
    {
        // TODO: Awful, again...
        
        if (value instanceof JMeterProperty)
        {
            return (JMeterProperty)value;
        }
        
        JMeterProperty property;
        if (value == null)
        {
            property= new NullProperty();
        }
        else if (value instanceof Boolean)
        {
            property= new BooleanProperty();
        }
        else if (value instanceof Double)
        {
            property= new DoubleProperty();
        }
        else if (value instanceof Float)
        {
            property= new FloatProperty();
        }
        else if (value instanceof Integer)
        {
            property= new IntegerProperty();
        }
        else if (value instanceof Long)
        {
            property= new LongProperty();
        }
        else if (value instanceof String)
        {
            property= new StringProperty();
        }
        else if (value instanceof TestElement)
        {
            property= new TestElementProperty();
        }
        else throw new Error("Ouch!");

        property.setObjectValue(value);

        return property;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createPopupMenu()
     */
    public JPopupMenu createPopupMenu()
    {
        // TODO: this menu is too wide (allows, e.g. to add controllers, no matter the
        // type of the element). Change to match the actual bean's capabilities.
    	if (Timer.class.isAssignableFrom(testBeanClass))//HACK: Fix one such problem
    	{
    		return MenuFactory.getDefaultTimerMenu();
    	}
        return MenuFactory.getDefaultControllerMenu();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(org.apache.jmeter.testelement.TestElement)
     */
    public void configure(TestElement element)
    {
        if (! initialized) init();

        super.configure(element);

        // Copy all property values into the map:
        propertyMap.clear();
        for (PropertyIterator jprops= element.propertyIterator(); jprops.hasNext(); )
        {
            JMeterProperty jprop= jprops.next();
            propertyMap.put(jprop.getName(), jprop.getObjectValue());
        }

        if (customizer != null)
        {
            customizer.setObject(propertyMap);
        }
        else
        {
            if (initialized) remove(customizerIndexInPanel);
            Customizer c= (Customizer)customizers.get(element);
            if (c == null)
            {
                c= createCustomizer();
                c.setObject(propertyMap);
                customizers.put(element, c);
            }
            add((Component)c, BorderLayout.CENTER);
        }

        initialized= true;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getMenuCategories()
     */
    public Collection getMenuCategories()
    {
        List menuCategories= new LinkedList();

        // We don't want to show expert beans in the menus unless we're
        // in expert mode:
        if (beanInfo.getBeanDescriptor().isExpert()
            && ! JMeterUtils.isExpertMode())
        {
            return null;
        }
        
        // TODO: there must be a nicer way...
        if (Assertion.class.isAssignableFrom(testBeanClass))
        {
            menuCategories.add(MenuFactory.ASSERTIONS);
        }
        if (ConfigElement.class.isAssignableFrom(testBeanClass))
        {
            menuCategories.add(MenuFactory.CONFIG_ELEMENTS);
        }
        if (Controller.class.isAssignableFrom(testBeanClass))
        {
            menuCategories.add(MenuFactory.CONTROLLERS);
        }
        if (Visualizer.class.isAssignableFrom(testBeanClass))
        {
            menuCategories.add(MenuFactory.LISTENERS);
        }
        if (PostProcessor.class.isAssignableFrom(testBeanClass))
        {
            menuCategories.add(MenuFactory.POST_PROCESSORS);
        }
        if (PreProcessor.class.isAssignableFrom(testBeanClass))
        {
            menuCategories.add(MenuFactory.PRE_PROCESSORS);
        }
        if (Sampler.class.isAssignableFrom(testBeanClass))
        {
            menuCategories.add(MenuFactory.SAMPLERS);
        }
        if (Timer.class.isAssignableFrom(testBeanClass))
        {
            menuCategories.add(MenuFactory.TIMERS);
        }
        return menuCategories;
    }
    
    private void init()
    {
        setLayout(new BorderLayout(0, 5));

        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        customizerIndexInPanel= getComponentCount();

        if (customizerClass == null)
        {
            customizer= new GenericTestBeanCustomizer(beanInfo);
        }
        else if (SharedCustomizer.class.isAssignableFrom(customizerClass))
        {
            customizer= createCustomizer();
        }
        
        if (customizer != null) add((Component)customizer, BorderLayout.CENTER);
    }

	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
	 */
	public String getLabelResource() {
		// TODO Auto-generated method stub
		return null;
	}
}
