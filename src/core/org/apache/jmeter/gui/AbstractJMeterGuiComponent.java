/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
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
 * @see JMeterGuiComponent
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
	
	/**
	 * @see JMeterGUIComponent#setName(String)
	 */
	public void setName(String name)
	{
		namePanel.setName(name);
	}
	
	/**
	 * @see java.awt.Component#isEnabled()
	 */
	public boolean isEnabled()
	{
		return enabled;
	}
	
	/**
	 * @see java.awt.Component#setEnabled(boolean)
	 */
	public void setEnabled(boolean e)
	{
        log.debug("Setting enabled: " + e);
		enabled = e;
	}
	
	/**
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
	 * This method should be overriden, but the extending class should also still call it, as
	 * it does the work necessary to configure the name of the component from the
	 * given Test Element.  Otherwise, the component can do this itself.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(org.apache.jmeter.testelement.TestElement)
	 */
	public void configure(TestElement element)
	{
		setName((String) element.getProperty(TestElement.NAME));
        if(element.getProperty(TestElement.ENABLED) == null)
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
		mc.setProperty(TestElement.NAME, getName());
		mc.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
		mc.setProperty(TestElement.TEST_CLASS, mc.getClass().getName());
                //This  stores the state of the TestElement 
                log.debug("setting element to enabled: " + enabled);
                mc.setProperty(TestElement.ENABLED,new Boolean(enabled).toString());
	}
	
	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#setNode(org.apache.jmeter.gui.tree.JMeterTreeNode)
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
    
    protected JPanel makeTitlePanel() {
            JLabel title = new JLabel(getStaticLabel());
            Font font = title.getFont();
            title.setFont(new Font(font.getFontName(),font.getStyle(),font.getSize()+4));
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.add(title, BorderLayout.NORTH);
            titlePanel.add(getNamePanel(), BorderLayout.SOUTH);
            return titlePanel;
        }
}
