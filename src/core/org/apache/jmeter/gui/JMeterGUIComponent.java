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
import java.util.Collection;

import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
/**
 * Implementing this interface indicates that the class is a JMeter GUI
 * Component.  A JMeter GUI Component is essentially the GUI display code
 * associated with a JMeter Test Element.  The writer of the component must
 * take care to make the component be consistent with the rest of JMeter's
 * GUI look and feel and behavior.  Use of the provided abstract classes is
 * highly recommended to make this task easier.
 * 
 * @see AbstractJMeterGuiComponent
 * @see org.apache.jmeter.config.gui.AbstractConfigGui
 * @see org.apache.jmeter.assertions.gui.AbstractAssertionGui
 * @see org.apache.jmeter.control.gui.AbstractControllerGui
 * @see org.apache.jmeter.timers.gui.AbstractTimerGui
 * @see org.apache.jmeter.visualizers.gui.AbstractVisualizer
 * @see org.apache.jmeter.samplers.gui.AbstractSamplerGui
 *
 * @author    Michael Stover
 * @version   $Revision$
 * 
 */

public interface JMeterGUIComponent
{

    /**
     * Sets the name of the JMeter GUI Component.  The name
     * of the component is used in the Test Tree as the name of the
     * tree node.
     *
     * @param name  the name of the component
     */
    void setName(String name);

    /**
     * Gets the name of the JMeter GUI component.  The name
     * of the component is used in the Test Tree as the name of the tree node.
     *
     * @return   the name of the component
     */
    String getName();

    /**
     * Get the component's label.  This label is used in drop down
     * lists that give the user the option of choosing one type of
     * component in a list of many.  It should therefore be a descriptive
     * name for the end user to see.  It must be unique to the class.
     *
     * @return   GUI label for the component.
     */
    String getStaticLabel();

    /**
     * JMeter test components are separated into a model and a GUI representation.  The model holds the data and the GUI displays it.  The GUI class is
     * responsible for knowing how to create and initialize with data the model class that it knows how to display, and this method is called when new test elements are
     * created.  
     *
     * @return  the Test Element object that the GUI component represents.
     */
    TestElement createTestElement();
    
    /**
     * GUI components are responsible for populating TestElements they create with the data currently held in the GUI components.  This method should 
     * overwrite whatever data is currently in the TestElement as it is called after a user has filled out the form elements in the gui with new information.
     * 
     * @param element the TestElement to modify
     */
    void modifyTestElement(TestElement element);

    /**
     * Test GUI elements can be disabled, in which case
     * they do not become part of the test when run.
     * 
     * @return true if the element should be part of the test run, false
     * otherwise
     */
    boolean isEnabled();

    /**
     * Set whether this component is enabled.
     * 
     * @param enabled true for enabled, false for disabled.
     */
    void setEnabled(boolean enabled);

    /**
     * When a user right-clicks on the component in the test tree, or
     * selects the edit menu when the component is selected, the 
     * component will be asked to return a JPopupMenu that provides
     * all the options available to the user from this component.
     *
     * @return   a JPopupMenu appropriate for the component.
     */
    JPopupMenu createPopupMenu();

    /**
     * The GUI must be able to extract the data from the TestElement and update all GUI fields to represent those data.
     * This method is called to allow JMeter to show the user the GUI that represents the test element's data.
     *
     * @param element the TestElement to configure 
     */
    void configure(TestElement element);

    /**
     * This is the list of add menu categories this gui component will be available
     * under. For instance, if this represents a Controller, then the
     * MenuFactory.CONTROLLERS category should be in the returned collection.  When a user right-clicks on a tree element and looks through
     * the "add" menu, which category your GUI component shows up in is determined by which categories are returned by this method.  Most GUI's belong to 
     * only one category, but it is possible for a component to exist in multiple categories.
     *
     * @return   a Collection of Strings, where each element is one of the
     *           constants defined in MenuFactory
     * 
     * @see org.apache.jmeter.gui.util.MenuFactory
     */
    Collection getMenuCategories();

    /**
     * Sets the tree node which this component is associated with.
     * 
     * @param node the tree node corresponding to this component
     */
    void setNode(JMeterTreeNode node);
    
    /**
     * Clear the gui and return it to initial default values.  This is necessary because most gui classes are instantiated just once and re-used for multiple
     * test element objects and thus they need to be cleared between use.
     * TODO: implement this in all gui classes.
     */
    public void clear();
}
