/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
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
 */
package org.apache.jmeter.gui;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
/**
 * This abstract class takes care of the most basic functions necessary to create a viable
 * JMeter GUI component.  It extends JPanel and implements JMeterGUIComponent.  This
 * abstract is, in turn, extended by several other abstract classes that create different
 * classes of GUI components for JMeter (ie Visualizers, Timers, Samplers, Modifiers, Controllers, etc).
 * 
 * @author mstover
 *
 * @see JMeterGUIComponent
 * @see org.apache.jmeter.config.gui.AbstractConfigGui
 * @see org.apache.jmeter.config.gui.AbstractModifierGui
 * @see org.apache.jmeter.config.gui.AbstractResponseBasedModifierGui
 * @see org.apache.jmeter.assertions.gui.AbstractAssertionGui
 * @see org.apache.jmeter.control.gui.AbstractControllerGui
 * @see org.apache.jmeter.timers.gui.AbstractTimerGui
 * @see org.apache.jmeter.visualizers.gui.AbstractVisualizer
 * @see org.apache.jmeter.samplers.gui.AbstractSamplerGui
 */
public abstract class AbstractJMeterGuiComponent
	extends JPanel
	implements JMeterGUIComponent
{
    private static Logger log = LoggingManager.getLoggerFor(JMeterUtils.GUI);
	private boolean enabled = true;
	private JMeterTreeNode node;
	
	
	/**
	 * When constructing a new component, this takes care of basic tasks like
	 * setting up the Name Panel and assigning the class's static label as
	 * the name to start.
	 * @see java.lang.Object#Object()
	 */
	public AbstractJMeterGuiComponent()
	{
		namePanel = new NamePanel();
		setName(getStaticLabel());
	}
	
    /* (non-Javadoc)
	 * @see JMeterGUIComponent#setName(String)
	 */
	public void setName(String name)
	{
		namePanel.setName(name);
	}
	
    /* (non-Javadoc)
	 * @see java.awt.Component#isEnabled()
	 */
	public boolean isEnabled()
	{
		return enabled;
	}
	
    /* (non-Javadoc)
	 * @see java.awt.Component#setEnabled(boolean)
	 */
	public void setEnabled(boolean e)
	{
        log.debug("Setting enabled: " + e);
		enabled = e;
	}
	
    /* (non-Javadoc)
	 * @see JMeterGUIComponent#getName()
	 */
	public String getName()
	{
		return getNamePanel().getName();
	}
	
	/**
	 * Provides the Name Panel for extending classes.  Extending classes are free to
	 * place it as desired within the component, or not at all.
	 * 
	 * @return NamePanel
	 */
	protected NamePanel getNamePanel()
	{
		return namePanel;
	}
	
	protected NamePanel namePanel;
    
    /**
     * Provides a label containing the title for the component.  Subclasses
     * typically place this label at the top of their GUI.  The title is set
     * to the name returned from the component's
     * {@link JMeterGUIComponent#getStaticLabel() getStaticLabel()} method.
     * 
     * @return a JLabel which subclasses can add to their GUI
     */
    protected Component createTitleLabel() {
        JLabel titleLabel = new JLabel(getStaticLabel());
        Font curFont = titleLabel.getFont();
        titleLabel.setFont(curFont.deriveFont((float)curFont.getSize() + 4));
        return titleLabel;
    }

	/**
	 * This method should be overriden, but the extending class should also still call it, as
	 * it does the work necessary to configure the name of the component from the
	 * given Test Element.  Otherwise, the component can do this itself.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
	 */
	public void configure(TestElement element)
	{
		setName(element.getPropertyAsString(TestElement.NAME));
        if(element.getProperty(TestElement.ENABLED) instanceof NullProperty)
        {
            enabled = true;
        }
        else
        {
            enabled = element.getPropertyAsBoolean(TestElement.ENABLED);
        }
	}
	
	/**
	 * This provides a convenience for extenders when they implement the createTestElement()
	 * method.  This method will set the name, gui class, and test class for the created
	 * Test Element.  It should be called by every extending class when creating Test
	 * Elements, as that will best assure consistent behavior.
	 * @param The Test Element being created.
	 */
	protected void configureTestElement(TestElement mc)
	{
		mc.setProperty(new StringProperty(TestElement.NAME, getName()));
		mc.setProperty(new StringProperty(TestElement.GUI_CLASS, this.getClass().getName()));
		mc.setProperty(new StringProperty(TestElement.TEST_CLASS, mc.getClass().getName()));
                //This  stores the state of the TestElement 
                log.debug("setting element to enabled: " + enabled);
                mc.setProperty(new BooleanProperty(TestElement.ENABLED,enabled));
	}
	
    /* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#setNode(JMeterTreeNode)
	 */
	public void setNode(JMeterTreeNode node)
	{
		this.node = node;
		getNamePanel().setNode(node);
	}
	/**
	 * Method getNode.
	 * @return JMeterTreeNode
	 */
	protected JMeterTreeNode getNode()
	{
		return node;
	}
    
    /**
     * Create a standard title section for JMeter components.  This includes
     * the title for the component and the Name Panel allowing the user to
     * change the name for the component.  This method is typically added to
     * the top of the component at the beginning of the component's init method.
     * 
     * @return a Box containing the component title and name panel
     */
    protected Container makeTitlePanel() {
        VerticalPanel titlePanel = new VerticalPanel();
        titlePanel.add(createTitleLabel());
        titlePanel.add(getNamePanel());
        return titlePanel;
    }
    
    /**
     * Create a Border which can be added to JMeter components.  Components
     * typically set this as their border in their init method.
     * 
     * @return a Border for JMeter components
     */
    protected Border makeBorder() {
        return BorderFactory.createEmptyBorder(10, 10, 5, 10);
    }

    /**
     * Create a scroll panel that sets it's preferred size to it's minimum
     * size.  Explicitly for scroll panes that live inside other scroll panes,
     * or within containers that stretch components to fill the area they exist
     * in.
     * @param comp
     * @return a JScrollPane containing the specified component
     */
    protected JScrollPane makeScrollPane(Component comp)
    {
        JScrollPane pane =  new JScrollPane(comp);
        pane.setPreferredSize(pane.getMinimumSize());
        return pane;
    }

    /**
     * Create a scroll panel that sets it's preferred size to it's minimum
     * size.  Explicitly for scroll panes that live inside other scroll panes,
     * or within containers that stretch components to fill the area they exist
     * in.
     * @param comp
     * @param verticalPolicy
     * @param horizontalPolicy
     * @return a JScrollPane containing the specified component
     */
    protected JScrollPane makeScrollPane(
        Component comp,
        int verticalPolicy,
        int horizontalPolicy)
    {
        JScrollPane pane =  new JScrollPane(comp,verticalPolicy,horizontalPolicy);
        pane.setPreferredSize(pane.getMinimumSize());
        return pane;
    }
}
