/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
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
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The GenericGUI is designed to provide developers with a mechanism to
 * quickly implement GUIs for new components.
 * <p>
 * It allows editing each of the public exposed properties of the
 * edited type 'a la JavaBeans': as far as the types of those properties
 * have an associated editor, there's no GUI development required. 
 * <p>
 * TestBeanGUI understands the following PropertyDescriptor attributes:
 * <dl>
 * <dt>group: String</dt>
 * <dd>Group under which the property should be shown in the GUI. The string is
 * also used as a group title. The default group is "".</dd>
 * <dt>order: Integer</dt>
 * <dd>Order in which the property will be shown in its group. A smaller
 * integer means higher up in the GUI. The default order is 0. Properties
 * of equal order are sorted alphabetically.</dd>
 * <dt>tags: String[]</dt>
 * <dd>List of values to be offered for the property in addition to those
 * offered by its property editor.</dd>
 * <dt>noUndefined: Boolean</dt>
 * <dd>If true, the property should not be left undefined. A <b>default</b>
 * attribute must be provided if this is set.</dd>
 * <dd>noEdit: Boolean</dd>
 * <dd>If true, the property content should not be edited manually, that is: it
 * should always be one of the tags values.</dt>
 * <dt>default: Object</dt>
 * <dd>Initial value for the property's GUI. Must be provided and be non-null
 * if <b>noUndefined</b> is set. Must be one of the provided tags (or null) if
 * <b>noEdit</b> is set.
 * </dl>
 * <p>
 * The following BeanDescriptor attributes are also understood:
 * <dl>
 * <dt>group.<i>group</i>.order: Integer</dt>
 * <dd>where <b><i>group</i></b> is a group name used in a <b>group</b>
 * attribute in one or more PropertyDescriptors. Defines the order in which
 * the group will be shown in the GUI. A smaller integer means higher up
 * in the GUI. The default order is 0. Groups of equal order are sorted
 * alphabetically.</dd>
 * <dt>resourceBundle: ResourceBundle</dt>
 * <dd>A resource bundle to be used for GUI localization. Group display names,
 * for example, will be obtained from property "<i>group</i>.displayName" if
 * available (where <b><i>group</i></b> is the group name).
 * </dl>
 */
public class TestBeanGUI extends AbstractJMeterGuiComponent
{
    private static Logger log = LoggingManager.getLoggerForClass();

    /**
     * Class of the objects being edited.
     */
    private Class testBeanClass;
    
    /**
     * BeanInfo object for the class of the objects being edited.
     */
    private BeanInfo beanInfo;

    /**
     * Property descriptors from the beanInfo.
     */
    private PropertyDescriptor[] descriptors;

    /**
     * Property editors -- or null if the property can't be edited.
     */
    private PropertyEditor[] editors;

	/**
	 * Message format for property field labels:
	 */
	private MessageFormat propertyFieldLabelMessage;
	
	/**
	 * Message format for property tooltips:
	 */
	private MessageFormat propertyToolTipMessage;
	
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

    /**
     * Create a GUI for a given test bean type.
     * 
     * @param testBeanClass a subclass of TestBean
     * @see org.apache.jmeter.testbeans.TestBean 
     */
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
            beanInfo= Introspector.getBeanInfo(testBeanClass, TestBean.class);
            descriptors= beanInfo.getPropertyDescriptors();
        }
        catch (IntrospectionException e)
        {
            log.error("Can't get beanInfo for "+testBeanClass.getName(),
                e);
            throw new Error(e); // Programming error. Don't continue.
        }

		// Sort the property descriptors:
		Arrays.sort(descriptors, new PropertyComparator());

        // Obtain the propertyEditors:
        editors= new PropertyEditor[descriptors.length];
        for (int i=0; i<descriptors.length; i++)
        {
            String name= descriptors[i].getName();

            // Don't get editors for hidden or non-read-write properties:
            if (descriptors[i].isHidden()
                || descriptors[i].getReadMethod() == null
                || descriptors[i].getWriteMethod() == null)
            {
                log.debug("No editor for property "+name);
                editors[i]= null;
                continue;
            }

            PropertyEditor propertyEditor;
            Class editorClass= descriptors[i].getPropertyEditorClass();
            
            if (log.isDebugEnabled())
            {
                log.debug("Property "+name
                        +" has editor class "+editorClass);
            }
            
            if (editorClass != null)
            {
                try
                {
                    propertyEditor= (PropertyEditor)editorClass.newInstance();
                }
                catch (InstantiationException e)
                {
                    log.error("Can't create property editor.", e);
                    throw new Error(e);
                }
                catch (IllegalAccessException e)
                {
                    log.error("Can't create property editor.", e);
                    throw new Error(e);
                }
            }
            else
            {
                Class c= descriptors[i].getPropertyType();
                propertyEditor= PropertyEditorManager.findEditor(c);
            }

            if (log.isDebugEnabled())
            {
                log.debug("Property "+name
                        +" has property editor "+propertyEditor);
            }
            
            if (propertyEditor == null)
            {
                log.debug("No editor for property "+name);
                editors[i]= null;
                continue;
            }
            
            if (! propertyEditor.supportsCustomEditor())
            {
				propertyEditor= createWrapperEditor(
					propertyEditor, descriptors[i]);

				if (log.isDebugEnabled())
				{
					log.debug("Editor for property "+name
							+" is wrapped in "+propertyEditor);
				}
            }
            
			editors[i]= propertyEditor;

			// Initialize the editor with the provided default value or null:
            setEditorValue(i, descriptors[i].getValue("default"));
        }

		// Obtain message formats:
		propertyFieldLabelMessage= new MessageFormat(	
			JMeterUtils.getResString("property_as_field_label"));
		propertyToolTipMessage= new MessageFormat(	
			JMeterUtils.getResString("property_tool_tip"));

        // Initialize the GUI:
        init();
    }

	/**
	 * Find the default typeEditor and a suitable guiEditor for the given
	 * property descriptor, and combine them in a WrapperEditor.
	 * 
	 * @param typeEditor
	 * @param descriptor
	 * @return
	 */
	private WrapperEditor createWrapperEditor(
			PropertyEditor typeEditor, PropertyDescriptor descriptor)
	{
		String[] editorTags= typeEditor.getTags();
		String[] additionalTags= (String[])descriptor.getValue("tags");
		String[] tags= null;
		if (editorTags == null) tags= additionalTags;
		else if (additionalTags == null) tags= editorTags;
		else {
			tags= new String[editorTags.length+additionalTags.length];
			int j= 0;
			for (int i=0; i<editorTags.length; i++) tags[j++]= editorTags[i];
			for (int i=0; i<additionalTags.length; i++) tags[j++]= additionalTags[i];
		}
		
		boolean noUndefined=
			Boolean.TRUE.equals(descriptor.getValue("noUndefined"));
		boolean noEdit=
			Boolean.TRUE.equals(descriptor.getValue("noEdit"));

		PropertyEditor guiEditor;
		if (noUndefined && tags==null)
		{
			guiEditor= new FieldStringEditor();
		}
		else
		{
			ComboStringEditor e= new ComboStringEditor();
			e.setNoUndefined(noUndefined);
			e.setNoEdit(noEdit);
			e.setTags(tags);
			
			guiEditor= e;
		}

		WrapperEditor wrapper= new WrapperEditor(
			typeEditor, guiEditor,
			!noUndefined, // acceptsNull
			!noEdit // acceptsExpressions TODO: can be finer
			);

		Object defaultValue= descriptor.getValue("default");
		
		if (guiEditor instanceof ComboStringEditor && !noEdit)
		{
			// Provide an initial edit value:
			if (tags != null)
			{
				((ComboStringEditor)guiEditor).setInitialEditValue(tags[0]);
			}
			else
			{
				// 'expressions' are currently always valid on
				// editable fields: TODO: could be finer
				((ComboStringEditor)guiEditor).setInitialEditValue("${}");
			}
			// TODO: I don't like this solution. We could make a
			// more convenient approach if we knew whether the property
			// accepts expressions or not, whether it accepts any 
			// values beyond the provided tags or not, etc... 
			// ... plus we could define a "initialEditValue" property descriptor
			// attribute to explicitly specify which value should be used. 
		}

		wrapper.setValue(defaultValue);

		return wrapper;
	}

	/**
	 * Set the value of the i-th property, properly reporting a possible failure.
	 * 
	 * @param i the index of the property in the descriptors and editors arrays
	 * @param value the value to be stored in the editor
	 * 
	 * @throws IllegalArgumentException if the editor refuses the value
	 */
	private void setEditorValue(int i, Object value)
		throws IllegalArgumentException
	{
		try
		{
			editors[i].setValue(value);
		}
		catch (IllegalArgumentException e)
		{
			log.error("Could not set value "
				+ ( value == null ? "NULL" : value.getClass().getName() )
				+ ":" + value
				+" for property "+descriptors[i].getName());
			throw e;
		}
	}

    public String getStaticLabel() {
        if (beanInfo == null) return "null";
        return beanInfo.getBeanDescriptor().getDisplayName();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(org.apache.jmeter.testelement.TestElement)
     */
    public void configure(TestElement element)
    {
        super.configure(element);

		for (int i=0; i<editors.length; i++)
		{
			if (editors[i] == null) continue;
			JMeterProperty jprop= element.getProperty(descriptors[i].getName());
			try
			{
				setEditorValue(i, jprop.getObjectValue());
			}
			catch (IllegalArgumentException e)
			{
				// I guess this can happen as a result of a bad
				// file read? In this case, it would be better to replace the
				// incorrect value with anything valid, e.g. the default value
				// for the property.
				// But for the time being, I just prefer to be aware of any
				// problems occuring here, most likely programming errors,
				// so I'll bail out.
				throw new Error("Bad property value.", e);
				// TODO: review this and possibly change to:
				// setEditorValue(i, descriptors[i].getValue("default"); 
			}
		}
    }

	/**
	 * Find the index of the property of the given name.
	 * 
	 * @param name the name of the property
	 * @return the index of that property in the descriptors array, or -1 if 
	 * 			there's no property of this name.
	 */
	private int descriptorIndex(String name)
	{
		for (int i=0; i<descriptors.length; i++)
		{
			if (descriptors[i].getName().equals(name))
			{
				return i;
			}
		}
		return -1;
	}
	
    public TestElement createTestElement()
    {
        try
        {
            TestElement element= (TestElement)testBeanClass.newInstance();
            modifyTestElement(element);
            return element;
        }
        catch (InstantiationException e)
        {
            log.error("Can't create test element", e);
            throw new Error(e); // Programming error. Don't continue.
        }
        catch (IllegalAccessException e)
        {
            log.error("Can't create test element", e);
            throw new Error(e); // Programming error. Don't continue.
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void modifyTestElement(TestElement element)
    {
        configureTestElement(element);
        for (int i=0; i<editors.length; i++)
        {
            if (editors[i] == null) continue;
            Object value= editors[i].getValue();
            
			if (value == null)
			{
				element.removeProperty(descriptors[i].getName());
			}
			else {
				JMeterProperty jprop= wrapInProperty(value);
				jprop.setName(descriptors[i].getName());
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
        return MenuFactory.getDefaultControllerMenu();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getMenuCategories()
     */
    public Collection getMenuCategories()
    {
        List menuCategories= new LinkedList();

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

    /**
     * Initialize the GUI.
     */
    private void init()
    {
		// TODO: add support for Bean Customizers

        setLayout(new BorderLayout(0, 5));

        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints cl= new GridBagConstraints(); // for labels
		cl.gridx= 0;
		cl.anchor= GridBagConstraints.LINE_END;
		cl.insets= new Insets(0, 1, 0, 1);

		GridBagConstraints ce= new GridBagConstraints(); // for editors
		ce.fill= GridBagConstraints.BOTH;
		ce.gridx= 1;
		ce.weightx= 1.0;
		ce.insets= new Insets(0, 1, 0, 1);
		
		GridBagConstraints cp= new GridBagConstraints(); // for panels
		cp.fill= GridBagConstraints.BOTH;
		cp.gridx= 1;
		cp.gridy= GridBagConstraints.RELATIVE;
		cp.gridwidth= 2;
		cp.weightx= 1.0;

		JPanel currentPanel= mainPanel;
		String currentGroup= "";
		int y=0;
		
        for (int i=0; i<editors.length; i++)
        {
            if (editors[i] == null) continue;

			if (log.isDebugEnabled())
			{
				log.debug("Laying property "+descriptors[i].getName());
			}
			
			String g= group(descriptors[i]);
			if (! currentGroup.equals(g))
			{
				if (currentPanel != mainPanel)
				{
					mainPanel.add(currentPanel, cp);
				}
				currentGroup= g;
				currentPanel= new JPanel(new GridBagLayout()); 
				currentPanel.setBorder(
					BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(),
						groupDisplayName(g)));
				cp.weighty= 0.0;
				y= 0;
			}

			Component customEditor= editors[i].getCustomEditor();

			boolean multiLineEditor= false;
			if (customEditor.getPreferredSize().height > 50)
			{
				// TODO: the above works in the current situation, but it's
				// just a hack. How to get each editor to report whether it
				// wants to grow bigger? Whether the property label should
				// be at the left or at the top of the editor? ...?
				multiLineEditor= true;
			}
			
			JLabel label= createLabel(descriptors[i]);
			label.setLabelFor(customEditor);

			cl.gridy= y;
			cl.gridwidth= multiLineEditor ? 2 : 1;
			cl.anchor= multiLineEditor 
				? GridBagConstraints.CENTER
				: GridBagConstraints.LINE_END;
            currentPanel.add(label, cl);

			ce.gridx= multiLineEditor ? 0 : 1;
			ce.gridy= multiLineEditor ? ++y : y;
			ce.gridwidth= multiLineEditor ? 2 : 1;
			ce.weighty= multiLineEditor ? 1.0 : 0.0;

			cp.weighty+= ce.weighty;

            currentPanel.add(customEditor, ce);

            y++;
        }
		if (currentPanel != mainPanel)
		{
			mainPanel.add(currentPanel, cp);
		}
		
		// Add a 0-sized invisible component that will take all the vertical
		// space that nobody wants:
		GridBagConstraints cs= new GridBagConstraints(); // for strut
		cs.gridx= 1;
		cs.gridy= y++;
		cs.gridwidth= 2;
		cs.weighty= 0.0001;

		mainPanel.add(Box.createHorizontalStrut(0), cs);
		
		// Done: add the panel to the GUI:
        add(mainPanel, BorderLayout.CENTER);
    }

	private JLabel createLabel(PropertyDescriptor desc)
	{
		String text= desc.getDisplayName();
		if (! "".equals(text))
		{
			text= propertyFieldLabelMessage.format(
				new Object[] { desc.getDisplayName() } );
		}
		// if the displayName is the empty string, leave it like that.
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.TRAILING);
		text= propertyToolTipMessage.format(
			new Object[] { desc.getName(), desc.getShortDescription() } );
		label.setToolTipText(text);

		return label;
	}


	/**
	 * Obtain a property descriptor's group.
	 * 
	 * @param descriptor
	 * @return the group String.
	 */
	private String group(PropertyDescriptor d)
	{
		String group= (String)d.getValue("group");
		if (group == null) group= "";
		return group;
	}

	/**
	 *  Obtain a group's display name
	 */
	private String groupDisplayName(String group)
	{
		try {
			ResourceBundle b= (ResourceBundle)
				beanInfo.getBeanDescriptor().getValue("resourceBundle");
			return b.getString(group+".displayName");
		}
		catch (MissingResourceException e)
		{
			return group;
		}
	}

    /**
     * Comparator used to sort properties for presentation in the GUI.
     */
    private class PropertyComparator implements Comparator
    {
		public int compare(Object o1, Object o2)
		{
			return compare((PropertyDescriptor)o1, (PropertyDescriptor)o2);
		}
		
		private int compare(PropertyDescriptor d1, PropertyDescriptor d2)
		{
			int result;
		
			String g1= group(d1), g2= group(d2);
			Integer go1= groupOrder(g1), go2= groupOrder(g2);
		
			result= go1.compareTo(go2);
			if (result != 0) return result;
		
			result= g1.compareTo(g2);
			if (result != 0) return result;
		
			Integer po1= propertyOrder(d1), po2= propertyOrder(d2);
			result= po1.compareTo(po2);
			if (result != 0) return result;
		
			return d1.getName().compareTo(d2.getName());
		}
	
		/**
		 * Obtain a group's order.
		 * 
		 * @param group group name
		 * @return the group's order (zero by default)
		 */
		private Integer groupOrder(String group)
		{
			Integer order= (Integer)beanInfo.getBeanDescriptor()
					.getValue("group."+group+".order");
			if (order == null) order= new Integer(0);
			return order;
		}

		/**
		 * Obtain a property's order.
		 * 
		 * @param d
		 * @return the property's order attribute (zero by default)
		 */
		private Integer propertyOrder(PropertyDescriptor d)
		{
			Integer order= (Integer)d.getValue("order");
			if (order == null) order= new Integer(0);
			return order;
		}
    }
}
