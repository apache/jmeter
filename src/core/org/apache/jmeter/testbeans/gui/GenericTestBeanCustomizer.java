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
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The GenericTestBeanCustomizer is designed to provide developers with a
 * mechanism to quickly implement GUIs for new components.
 * <p>
 * It allows editing each of the public exposed properties of the edited type 'a
 * la JavaBeans': as far as the types of those properties have an associated
 * editor, there's no GUI development required.
 * <p>
 * This class understands the following PropertyDescriptor attributes:
 * <dl>
 * <dt>group: String</dt>
 * <dd>Group under which the property should be shown in the GUI. The string is
 * also used as a group title (but see comment on resourceBundle below). The
 * default group is "".</dd>
 * <dt>order: Integer</dt>
 * <dd>Order in which the property will be shown in its group. A smaller
 * integer means higher up in the GUI. The default order is 0. Properties of
 * equal order are sorted alphabetically.</dd>
 * <dt>tags: String[]</dt>
 * <dd>List of values to be offered for the property in addition to those
 * offered by its property editor.</dd>
 * <dt>notUndefined: Boolean</dt>
 * <dd>If true, the property should not be left undefined. A <b>default</b>
 * attribute must be provided if this is set.</dd>
 * <dd>notExpression: Boolean</dd>
 * <dd>If true, the property content should always be constant: JMeter
 * 'expressions' (strings using ${var}, etc...) can't be used.</dt>
 * <dd>notOther: Boolean</dd>
 * <dd>If true, the property content must always be one of the tags values or
 * null.</dt>
 * <dt>default: Object</dt>
 * <dd>Initial value for the property's GUI. Must be provided and be non-null
 * if <b>notUndefined</b> is set. Must be one of the provided tags (or null) if
 * <b>notOther</b> is set.
 * </dl>
 * <p>
 * The following BeanDescriptor attributes are also understood:
 * <dl>
 * <dt>group.<i>group</i>.order: Integer</dt>
 * <dd>where <b><i>group</i></b> is a group name used in a <b>group</b>
 * attribute in one or more PropertyDescriptors. Defines the order in which the
 * group will be shown in the GUI. A smaller integer means higher up in the GUI.
 * The default order is 0. Groups of equal order are sorted alphabetically.</dd>
 * <dt>resourceBundle: ResourceBundle</dt>
 * <dd>A resource bundle to be used for GUI localization: group display names
 * will be obtained from property "<b><i>group</i>.displayName</b>" if
 * available (where <b><i>group</i></b> is the group name).
 * </dl>
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 */
public class GenericTestBeanCustomizer extends JPanel implements SharedCustomizer {
	private static final Logger log = LoggingManager.getLoggerForClass();

	public static final String GROUP = "group"; //$NON-NLS-1$

	public static final String ORDER = "order"; //$NON-NLS-1$

	public static final String TAGS = "tags"; //$NON-NLS-1$

	public static final String NOT_UNDEFINED = "notUndefined"; //$NON-NLS-1$

	public static final String NOT_EXPRESSION = "notExpression"; //$NON-NLS-1$

	public static final String NOT_OTHER = "notOther"; //$NON-NLS-1$

	public static final String DEFAULT = "default"; //$NON-NLS-1$

	public static final String RESOURCE_BUNDLE = "resourceBundle"; //$NON-NLS-1$

	public static final String ORDER(String group) {
		return "group." + group + ".order";
	}

	public static final String DEFAULT_GROUP = "";

	private int scrollerCount = 0;

	/**
	 * BeanInfo object for the class of the objects being edited.
	 */
	private transient BeanInfo beanInfo;

	/**
	 * Property descriptors from the beanInfo.
	 */
	private transient PropertyDescriptor[] descriptors;

	/**
	 * Property editors -- or null if the property can't be edited. Unused if
	 * customizerClass==null.
	 */
	private transient PropertyEditor[] editors;

	/**
	 * Message format for property field labels:
	 */
	private MessageFormat propertyFieldLabelMessage;

	/**
	 * Message format for property tooltips:
	 */
	private MessageFormat propertyToolTipMessage;

	/**
	 * The Map we're currently customizing. Set by setObject().
	 */
	private Map propertyMap;

    public GenericTestBeanCustomizer(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }
	/**
	 * Create a customizer for a given test bean type.
	 * 
	 * @param testBeanClass
	 *            a subclass of TestBean
	 * @see org.apache.jmeter.testbeans.TestBean
	 */
	GenericTestBeanCustomizer(BeanInfo beanInfo) {
		super();

		this.beanInfo = beanInfo;

		// Get and sort the property descriptors:
		descriptors = beanInfo.getPropertyDescriptors();
		Arrays.sort(descriptors, new PropertyComparator());

		// Obtain the propertyEditors:
		editors = new PropertyEditor[descriptors.length];
		for (int i = 0; i < descriptors.length; i++) {
			String name = descriptors[i].getName();

			// Don't get editors for hidden or non-read-write properties:
			if (descriptors[i].isHidden() || (descriptors[i].isExpert() && !JMeterUtils.isExpertMode())
					|| descriptors[i].getReadMethod() == null || descriptors[i].getWriteMethod() == null) {
				log.debug("No editor for property " + name);
				editors[i] = null;
				continue;
			}

			PropertyEditor propertyEditor;
			Class editorClass = descriptors[i].getPropertyEditorClass();

			if (log.isDebugEnabled()) {
				log.debug("Property " + name + " has editor class " + editorClass);
			}

			if (editorClass != null) {
				try {
					propertyEditor = (PropertyEditor) editorClass.newInstance();
				} catch (InstantiationException e) {
					log.error("Can't create property editor.", e);
					throw new Error(e.toString());
				} catch (IllegalAccessException e) {
					log.error("Can't create property editor.", e);
					throw new Error(e.toString());
				}
			} else {
				Class c = descriptors[i].getPropertyType();
				propertyEditor = PropertyEditorManager.findEditor(c);
			}

			if (log.isDebugEnabled()) {
				log.debug("Property " + name + " has property editor " + propertyEditor);
			}

			if (propertyEditor == null) {
				log.debug("No editor for property " + name);
				editors[i] = null;
				continue;
			}

			if (!propertyEditor.supportsCustomEditor()) {
				propertyEditor = createWrapperEditor(propertyEditor, descriptors[i]);

				if (log.isDebugEnabled()) {
					log.debug("Editor for property " + name + " is wrapped in " + propertyEditor);
				}
			}
			if (propertyEditor.getCustomEditor() instanceof JScrollPane) {
				scrollerCount++;
			}

			editors[i] = propertyEditor;

			// Initialize the editor with the provided default value or null:
			setEditorValue(i, descriptors[i].getValue(DEFAULT));

		}

		// Obtain message formats:
		propertyFieldLabelMessage = new MessageFormat(JMeterUtils.getResString("property_as_field_label")); //$NON-NLS-1$
		propertyToolTipMessage = new MessageFormat(JMeterUtils.getResString("property_tool_tip")); //$NON-NLS-1$

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
	private WrapperEditor createWrapperEditor(PropertyEditor typeEditor, PropertyDescriptor descriptor) {
		String[] editorTags = typeEditor.getTags();
		String[] additionalTags = (String[]) descriptor.getValue(TAGS);
		String[] tags = null;
		if (editorTags == null)
			tags = additionalTags;
		else if (additionalTags == null)
			tags = editorTags;
		else {
			tags = new String[editorTags.length + additionalTags.length];
			int j = 0;
			for (int i = 0; i < editorTags.length; i++)
				tags[j++] = editorTags[i];
			for (int i = 0; i < additionalTags.length; i++)
				tags[j++] = additionalTags[i];
		}

		boolean notNull = Boolean.TRUE.equals(descriptor.getValue(NOT_UNDEFINED));
		boolean notExpression = Boolean.TRUE.equals(descriptor.getValue(NOT_EXPRESSION));
		boolean notOther = Boolean.TRUE.equals(descriptor.getValue(NOT_OTHER));

		PropertyEditor guiEditor;
		if (notNull && tags == null) {
			guiEditor = new FieldStringEditor();
		} else {
			ComboStringEditor e = new ComboStringEditor();
			e.setNoUndefined(notNull);
			e.setNoEdit(notExpression && notOther);
			e.setTags(tags);

			guiEditor = e;
		}

		WrapperEditor wrapper = new WrapperEditor(typeEditor, guiEditor, !notNull, // acceptsNull
				!notExpression, // acceptsExpressions
				!notOther, // acceptsOther
				descriptor.getValue(DEFAULT));

		return wrapper;
	}

	/**
	 * Set the value of the i-th property, properly reporting a possible
	 * failure.
	 * 
	 * @param i
	 *            the index of the property in the descriptors and editors
	 *            arrays
	 * @param value
	 *            the value to be stored in the editor
	 * 
	 * @throws IllegalArgumentException
	 *             if the editor refuses the value
	 */
	private void setEditorValue(int i, Object value) throws IllegalArgumentException {
		editors[i].setValue(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(org.apache.jmeter.testelement.TestElement)
	 */
	public void setObject(Object map) {
		propertyMap = (Map) map;

		if (propertyMap.size() == 0) {
			// Uninitialized -- set it to the defaults:
			for (int i = 0; i < editors.length; i++) {
				Object value = descriptors[i].getValue(DEFAULT);
				String name = descriptors[i].getName();
				if (value != null) {
					propertyMap.put(name, value);
					log.debug("Set " + name + "= " + value);
				}
				firePropertyChange(name, null, value);
			}
		}

		// Now set the editors to the element's values:
		for (int i = 0; i < editors.length; i++) {
			if (editors[i] == null)
				continue;
			try {
				setEditorValue(i, propertyMap.get(descriptors[i].getName()));
			} catch (IllegalArgumentException e) {
				// I guess this can happen as a result of a bad
				// file read? In this case, it would be better to replace the
				// incorrect value with anything valid, e.g. the default value
				// for the property.
				// But for the time being, I just prefer to be aware of any
				// problems occuring here, most likely programming errors,
				// so I'll bail out.
				// (MS Note) Can't bail out - newly create elements have blank
				// values and must get the defaults.
				// Also, when loading previous versions of jmeter test scripts,
				// some values
				// may not be right, and should get default values - MS
				// TODO: review this and possibly change to:
				setEditorValue(i, descriptors[i].getValue(DEFAULT));
			}
		}
	}

//	/**
//	 * Find the index of the property of the given name.
//	 * 
//	 * @param name
//	 *            the name of the property
//	 * @return the index of that property in the descriptors array, or -1 if
//	 *         there's no property of this name.
//	 */
//	private int descriptorIndex(String name) // NOTUSED
//	{
//		for (int i = 0; i < descriptors.length; i++) {
//			if (descriptors[i].getName().equals(name)) {
//				return i;
//			}
//		}
//		return -1;
//	}

	/**
	 * Initialize the GUI.
	 */
	private void init() {
		setLayout(new GridBagLayout());

		GridBagConstraints cl = new GridBagConstraints(); // for labels
		cl.gridx = 0;
		cl.anchor = GridBagConstraints.EAST;
		cl.insets = new Insets(0, 1, 0, 1);

		GridBagConstraints ce = new GridBagConstraints(); // for editors
		ce.fill = GridBagConstraints.BOTH;
		ce.gridx = 1;
		ce.weightx = 1.0;
		ce.insets = new Insets(0, 1, 0, 1);

		GridBagConstraints cp = new GridBagConstraints(); // for panels
		cp.fill = GridBagConstraints.BOTH;
		cp.gridx = 1;
		cp.gridy = GridBagConstraints.RELATIVE;
		cp.gridwidth = 2;
		cp.weightx = 1.0;

		JPanel currentPanel = this;
		String currentGroup = DEFAULT_GROUP;
		int y = 0;

		for (int i = 0; i < editors.length; i++) {
			if (editors[i] == null)
				continue;

			if (log.isDebugEnabled()) {
				log.debug("Laying property " + descriptors[i].getName());
			}

			String g = group(descriptors[i]);
			if (!currentGroup.equals(g)) {
				if (currentPanel != this) {
					add(currentPanel, cp);
				}
				currentGroup = g;
				currentPanel = new JPanel(new GridBagLayout());
				currentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
						groupDisplayName(g)));
				cp.weighty = 0.0;
				y = 0;
			}

			Component customEditor = editors[i].getCustomEditor();

			boolean multiLineEditor = false;
			if (customEditor.getPreferredSize().height > 50 || customEditor instanceof JScrollPane) {
				// TODO: the above works in the current situation, but it's
				// just a hack. How to get each editor to report whether it
				// wants to grow bigger? Whether the property label should
				// be at the left or at the top of the editor? ...?
				multiLineEditor = true;
			}

			JLabel label = createLabel(descriptors[i]);
			label.setLabelFor(customEditor);

			cl.gridy = y;
			cl.gridwidth = multiLineEditor ? 2 : 1;
			cl.anchor = multiLineEditor ? GridBagConstraints.CENTER : GridBagConstraints.EAST;
			currentPanel.add(label, cl);

			ce.gridx = multiLineEditor ? 0 : 1;
			ce.gridy = multiLineEditor ? ++y : y;
			ce.gridwidth = multiLineEditor ? 2 : 1;
			ce.weighty = multiLineEditor ? 1.0 : 0.0;

			cp.weighty += ce.weighty;

			currentPanel.add(customEditor, ce);

			y++;
		}
		if (currentPanel != this) {
			add(currentPanel, cp);
		}

		// Add a 0-sized invisible component that will take all the vertical
		// space that nobody wants:
		cp.weighty = 0.0001;
		add(Box.createHorizontalStrut(0), cp);
	}

	private JLabel createLabel(PropertyDescriptor desc) {
		String text = desc.getDisplayName();
		if (!"".equals(text)) {
			text = propertyFieldLabelMessage.format(new Object[] { desc.getDisplayName() });
		}
		// if the displayName is the empty string, leave it like that.
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(JLabel.TRAILING);
		text = propertyToolTipMessage.format(new Object[] { desc.getName(), desc.getShortDescription() });
		label.setToolTipText(text);

		return label;
	}

	/**
	 * Obtain a property descriptor's group.
	 * 
	 * @param descriptor
	 * @return the group String.
	 */
	private String group(PropertyDescriptor d) {
		String group = (String) d.getValue(GROUP);
		if (group == null)
			group = DEFAULT_GROUP;
		return group;
	}

	/**
	 * Obtain a group's display name
	 */
	private String groupDisplayName(String group) {
		try {
			ResourceBundle b = (ResourceBundle) beanInfo.getBeanDescriptor().getValue(RESOURCE_BUNDLE);
			if (b == null)
				return group;
			else
				return b.getString(group + ".displayName");
		} catch (MissingResourceException e) {
			return group;
		}
	}

	/**
	 * Comparator used to sort properties for presentation in the GUI.
	 */
	private class PropertyComparator implements Comparator, Serializable {
		public int compare(Object o1, Object o2) {
			return compare((PropertyDescriptor) o1, (PropertyDescriptor) o2);
		}

		private int compare(PropertyDescriptor d1, PropertyDescriptor d2) {
			int result;

			String g1 = group(d1), g2 = group(d2);
			Integer go1 = groupOrder(g1), go2 = groupOrder(g2);

			result = go1.compareTo(go2);
			if (result != 0)
				return result;

			result = g1.compareTo(g2);
			if (result != 0)
				return result;

			Integer po1 = propertyOrder(d1), po2 = propertyOrder(d2);
			result = po1.compareTo(po2);
			if (result != 0)
				return result;

			return d1.getName().compareTo(d2.getName());
		}

		/**
		 * Obtain a group's order.
		 * 
		 * @param group
		 *            group name
		 * @return the group's order (zero by default)
		 */
		private Integer groupOrder(String group) {
			Integer order = (Integer) beanInfo.getBeanDescriptor().getValue(ORDER(group));
			if (order == null)
				order = new Integer(0);
			return order;
		}

		/**
		 * Obtain a property's order.
		 * 
		 * @param d
		 * @return the property's order attribute (zero by default)
		 */
		private Integer propertyOrder(PropertyDescriptor d) {
			Integer order = (Integer) d.getValue(ORDER);
			if (order == null)
				order = new Integer(0);
			return order;
		}
	}

	/**
	 * Save values from the GUI fields into the property map
	 */
	void saveGuiFields() {
		for (int i = 0; i < editors.length; i++) {
			PropertyEditor propertyEditor=editors[i]; // might be null (e.g. in testing)
			if (propertyEditor != null) {
				Object value = propertyEditor.getValue();
				String name = descriptors[i].getName();
				if (value == null) {
					propertyMap.remove(name);
					if (log.isDebugEnabled()) {
						log.debug("Unset " + name);
					}
				} else {
					propertyMap.put(name, value);
					if (log.isDebugEnabled()) {
						log.debug("Set " + name + "= " + value);
					}
				}
			}
		}
	}

	void clearGuiFields() {
		for (int i = 0; i < editors.length; i++) {
			PropertyEditor propertyEditor=editors[i]; // might be null (e.g. in testing)
			if (propertyEditor != null) {
				try {
				if (propertyEditor instanceof WrapperEditor){
					WrapperEditor we = (WrapperEditor) propertyEditor;
					String tags[]=we.getTags();
					if (tags != null && tags.length > 0) {
						we.setAsText(tags[0]);
					} else {
						we.setValue("");
					}
				} else if (propertyEditor instanceof ComboStringEditor) {
					ComboStringEditor cse = (ComboStringEditor) propertyEditor;
					cse.setAsText(cse.getInitialEditValue());
				} else {
					propertyEditor.setAsText("");
				}
				} catch (IllegalArgumentException ex){
					log.error("Failed to set field "+descriptors[i].getName(),ex);
				}
			}
		}
	}

}
