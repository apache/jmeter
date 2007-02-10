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

package org.apache.jmeter.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.tree.TreeNode;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Printable;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This abstract class takes care of the most basic functions necessary to
 * create a viable JMeter GUI component. It extends JPanel and implements
 * JMeterGUIComponent. This abstract class is, in turn, extended by several
 * other abstract classes that create different classes of GUI components for
 * JMeter (Visualizers, Timers, Samplers, Modifiers, Controllers, etc).
 * 
 * @see org.apache.jmeter.gui.JMeterGUIComponent
 * @see org.apache.jmeter.config.gui.AbstractConfigGui
 * @see org.apache.jmeter.assertions.gui.AbstractAssertionGui
 * @see org.apache.jmeter.control.gui.AbstractControllerGui
 * @see org.apache.jmeter.timers.gui.AbstractTimerGui
 * @see org.apache.jmeter.visualizers.gui.AbstractVisualizer
 * @see org.apache.jmeter.samplers.gui.AbstractSamplerGui
 * 
 * @version $Revision$ on $Date$
 */
public abstract class AbstractJMeterGuiComponent extends JPanel implements JMeterGUIComponent, Printable, Clearable {
	/** Logging */
	private static Logger log = LoggingManager.getLoggerForClass();

	/** Flag indicating whether or not this component is enabled. */
	private boolean enabled = true;

	/** The tree node which this component is associated with. */
	private TreeNode node;

	/** A GUI panel containing the name of this component. */
	protected NamePanel namePanel;

	/**
	 * When constructing a new component, this takes care of basic tasks like
	 * setting up the Name Panel and assigning the class's static label as the
	 * name to start.
	 */
	public AbstractJMeterGuiComponent() {
		namePanel = new NamePanel();
		setName(getStaticLabel());
	}

	/**
	 * Provides a default implementation for the name property. It's unlikely
	 * developers will need to override.
	 */
	public void setName(String name) {
		namePanel.setName(name);
	}

	/**
	 * Provides a default implementation for the enabled property. It's unlikely
	 * developers will need to override.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Provides a default implementation for the enabled property. It's unlikely
	 * developers will need to override.
	 */
	public void setEnabled(boolean e) {
		log.debug("Setting enabled: " + e);
		enabled = e;
	}

	/**
	 * Provides a default implementation for the name property. It's unlikely
	 * developers will need to override.
	 */
	public String getName() {
		if (getNamePanel() != null) {
			return getNamePanel().getName();
		} else
			return "";
	}

	/**
	 * Provides the Name Panel for extending classes. Extending classes are free
	 * to place it as desired within the component, or not at all. Most
	 * components place the NamePanel automatically by calling
	 * {@link #makeTitlePanel()} instead of directly calling this method.
	 * 
	 * @return a NamePanel containing the name of this component
	 */
	protected NamePanel getNamePanel() {
		return namePanel;
	}

	/**
	 * Provides a label containing the title for the component. Subclasses
	 * typically place this label at the top of their GUI. The title is set to
	 * the name returned from the component's
	 * {@link JMeterGUIComponent#getStaticLabel() getStaticLabel()} method. Most
	 * components place this label automatically by calling
	 * {@link #makeTitlePanel()} instead of directly calling this method.
	 * 
	 * @return a JLabel which subclasses can add to their GUI
	 */
	protected Component createTitleLabel() {
		JLabel titleLabel = new JLabel(getStaticLabel());
		Font curFont = titleLabel.getFont();
		titleLabel.setFont(curFont.deriveFont((float) curFont.getSize() + 4));
		return titleLabel;
	}

	/**
	 * A newly created gui component can be initialized with the contents of a
	 * Test Element object by calling this method. The component is responsible
	 * for querying the Test Element object for the relevant information to
	 * display in its GUI.
	 * <p>
	 * AbstractJMeterGuiComponent provides a partial implementation of this
	 * method, setting the name of the component and its enabled status.
	 * Subclasses should override this method, performing their own
	 * configuration as needed, but also calling this super-implementation.
	 * 
	 * @param element
	 *            the TestElement to configure
	 */
	public void configure(TestElement element) {
		setName(element.getPropertyAsString(TestElement.NAME));
		if (element.getProperty(TestElement.ENABLED) instanceof NullProperty) {
			enabled = true;
		} else {
			enabled = element.getPropertyAsBoolean(TestElement.ENABLED);
		}
	}

	/**
	 * Provides a default implementat that resets the name field to the value of
	 * getStaticLabel(), and sets enabled to true. Your GUI may need more things
	 * cleared, in which case you should override, clear the extra fields, and
	 * still call super.clear().
	 */
	public void clear() {
		setName(getStaticLabel());
		enabled = true;
	}

	/**
	 * This provides a convenience for extenders when they implement the
	 * {@link JMeterGUIComponent#modifyTestElement(TestElement)} method. This
	 * method will set the name, gui class, and test class for the created Test
	 * Element. It should be called by every extending class when
	 * creating/modifying Test Elements, as that will best assure consistent
	 * behavior.
	 * 
	 * @param mc
	 *            the TestElement being created.
	 */
	protected void configureTestElement(TestElement mc) {
		mc.setProperty(new StringProperty(TestElement.NAME, getName()));

		mc.setProperty(new StringProperty(TestElement.GUI_CLASS, this.getClass().getName()));

		mc.setProperty(new StringProperty(TestElement.TEST_CLASS, mc.getClass().getName()));

		// This stores the state of the TestElement
		log.debug("setting element to enabled: " + enabled);
		mc.setProperty(new BooleanProperty(TestElement.ENABLED, enabled));
	}

	/**
	 * Provides a default implementation for the node property. It is unlikely
	 * developers would need to override this method.
	 */
	public void setNode(TreeNode node) {
		this.node = node;
		getNamePanel().setNode(node);
	}

	/**
	 * Provides a default implementation for the node property. It is unlikely
	 * developers would need to override this method.
	 */
	protected TreeNode getNode() {
		return node;
	}

	/**
	 * Create a standard title section for JMeter components. This includes the
	 * title for the component and the Name Panel allowing the user to change
	 * the name for the component. This method is typically added to the top of
	 * the component at the beginning of the component's init method.
	 * 
	 * @return a panel containing the component title and name panel
	 */
	protected Container makeTitlePanel() {
		VerticalPanel titlePanel = new VerticalPanel();
		titlePanel.add(createTitleLabel());
		titlePanel.add(getNamePanel());
		return titlePanel;
	}

	/**
	 * Create a top-level Border which can be added to JMeter components.
	 * Components typically set this as their border in their init method. It
	 * simply provides a nice spacing between the GUI components used and the
	 * edges of the window in which they appear.
	 * 
	 * @return a Border for JMeter components
	 */
	protected Border makeBorder() {
		return BorderFactory.createEmptyBorder(10, 10, 5, 10);
	}

	/**
	 * Create a scroll panel that sets it's preferred size to it's minimum size.
	 * Explicitly for scroll panes that live inside other scroll panes, or
	 * within containers that stretch components to fill the area they exist in.
	 * Use this for any component you would put in a scroll pane (such as
	 * TextAreas, tables, JLists, etc). It is here for convenience and to avoid
	 * duplicate code. JMeter displays best if you follow this custom.
	 * 
	 * @param comp
	 *            the component which should be placed inside the scroll pane
	 * @return a JScrollPane containing the specified component
	 */
	protected JScrollPane makeScrollPane(Component comp) {
		JScrollPane pane = new JScrollPane(comp);
		pane.setPreferredSize(pane.getMinimumSize());
		return pane;
	}

	/**
	 * Create a scroll panel that sets it's preferred size to it's minimum size.
	 * Explicitly for scroll panes that live inside other scroll panes, or
	 * within containers that stretch components to fill the area they exist in.
	 * Use this for any component you would put in a scroll pane (such as
	 * TextAreas, tables, JLists, etc). It is here for convenience and to avoid
	 * duplicate code. JMeter displays best if you follow this custom.
	 * 
	 * @see javax.swing.ScrollPaneConstants
	 * 
	 * @param comp
	 *            the component which should be placed inside the scroll pane
	 * @param verticalPolicy
	 *            the vertical scroll policy
	 * @param horizontalPolicy
	 *            the horizontal scroll policy
	 * @return a JScrollPane containing the specified component
	 */
	protected JScrollPane makeScrollPane(Component comp, int verticalPolicy, int horizontalPolicy) {
		JScrollPane pane = new JScrollPane(comp, verticalPolicy, horizontalPolicy);
		pane.setPreferredSize(pane.getMinimumSize());
		return pane;
	}

	public String getStaticLabel() {
		return JMeterUtils.getResString(getLabelResource());
	}

	public String getDocAnchor() {
		return getStaticLabel().replace(' ', '_');
	}

	/**
	 * Subclasses need to over-ride this method, if they wish to return
	 * something other than the Visualizer itself.
	 * 
	 * @return
	 */
	public JComponent getPrintableComponent() {
		return this;
	}

	// /*
	// * Dummy implementation so existing code still compiles.
	// * Throws an error because it should not be invoked - and cannot provide a
	// useful value.
	// *
	// * The target class should either implement getStaticLabel(), as before,
	// or it
	// * should implement getLabelResource()
	// *
	// * DONE: remove eventually
	// */
	// public String getLabelResource()
	// {
	// throw new UnsupportedOperationException("Needs to be implemented by the
	// class: "
	// +this.getClass().getName());
	// }
}
