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

import java.util.Collection;

import javax.swing.JPopupMenu;

import org.apache.jmeter.testelement.TestElement;

/**
 * Implementing this interface indicates that the class is a JMeter GUI
 * Component. A JMeter GUI Component is essentially the GUI display code
 * associated with a JMeter Test Element. The writer of the component must take
 * care to make the component be consistent with the rest of JMeter's GUI look
 * and feel and behavior. Use of the provided abstract classes is highly
 * recommended to make this task easier.
 *
 * @see AbstractJMeterGuiComponent
 * @see org.apache.jmeter.config.gui.AbstractConfigGui
 * @see org.apache.jmeter.assertions.gui.AbstractAssertionGui
 * @see org.apache.jmeter.control.gui.AbstractControllerGui
 * @see org.apache.jmeter.timers.gui.AbstractTimerGui
 * @see org.apache.jmeter.visualizers.gui.AbstractVisualizer
 * @see org.apache.jmeter.samplers.gui.AbstractSamplerGui
 *
 */

public interface JMeterGUIComponent extends ClearGui {

    /**
     * Sets the name of the JMeter GUI Component. The name of the component is
     * used in the Test Tree as the name of the tree node.
     *
     * @param name
     *            the name of the component
     */
    void setName(String name);

    /**
     * Gets the name of the JMeter GUI component. The name of the component is
     * used in the Test Tree as the name of the tree node.
     *
     * @return the name of the component
     */
    String getName();

    /**
     * Get the component's label. This label is used in drop down lists that
     * give the user the option of choosing one type of component in a list of
     * many. It should therefore be a descriptive name for the end user to see.
     * It must be unique to the class.
     *
     * It is also used by Help to find the appropriate location in the
     * documentation.
     *
     * Normally getLabelResource() should be overridden instead of
     * this method; the definition of this method in AbstractJMeterGuiComponent
     * is intended for general use.
     *
     * @see #getLabelResource()
     * @return GUI label for the component.
     */
    String getStaticLabel();

    /**
     * Get the component's resource name, which getStaticLabel uses to derive
     * the component's label in the local language. The resource name is fixed,
     * and does not vary with the selected language.
     *
     * Normally this method should be overridden in preference to overriding
     * getStaticLabel(). However where the resource name is not available or required,
     * getStaticLabel() may be overridden instead.
     *
     * @return the resource name
     */
    String getLabelResource();

    /**
     * Get the component's document anchor name. Used by Help to find the
     * appropriate location in the documentation
     *
     * @return Document anchor (#ref) for the component.
     */
    String getDocAnchor();

    /**
     * JMeter test components are separated into a model and a GUI
     * representation. The model holds the data and the GUI displays it. The GUI
     * class is responsible for knowing how to create and initialize with data
     * the model class that it knows how to display, and this method is called
     * when new test elements are created.
     *
     * <p>
     * The canonical implementation looks like this:
     * <pre>
     * public TestElement createTestElement() {
     *     TestElementXYZ el = new TestElementXYZ();
     *     modifyTestElement(el);
     *     return el;
     * }
     * </pre>
     *
     * @return the Test Element object that the GUI component represents.
     */
    TestElement createTestElement();

    /**
     * GUI components are responsible for populating TestElements they create
     * with the data currently held in the GUI components. This method should
     * overwrite whatever data is currently in the TestElement as it is called
     * after a user has filled out the form elements in the gui with new
     * information.
     *
     * <p>
     * The canonical implementation looks like this:
     * <pre>
     * public void modifyTestElement(TestElement element) {
     *     element.clear(); // many implementations use this
     *     configureTestElement(element);
     *     // Using the element setters (preferred):
     *     TestElementXYZ xyz = (TestElementXYZ) element;
     *     xyz.setState(guiState.getText());
     *     xyz.setCode(guiCode.getText());
     *     ... other GUI fields ...
     *     // or directly (do not use unless there is no setter for the field):
     *     element.setProperty(TestElementXYZ.STATE, guiState.getText())
     *     element.setProperty(TestElementXYZ.CODE, guiCode.getText())
     *     ... other GUI fields ...
     * }
     * </pre>
     *
     * @param element
     *            the TestElement to modify
     */
    void modifyTestElement(TestElement element);

    /**
     * Test GUI elements can be disabled, in which case they do not become part
     * of the test when run.
     *
     * @return true if the element should be part of the test run, false
     *         otherwise
     */
    boolean isEnabled();

    /**
     * Set whether this component is enabled.
     *
     * @param enabled
     *            true for enabled, false for disabled.
     */
    void setEnabled(boolean enabled);

    /**
     * When a user right-clicks on the component in the test tree, or selects
     * the edit menu when the component is selected, the component will be asked
     * to return a JPopupMenu that provides all the options available to the
     * user from this component.
     *
     * @return a JPopupMenu appropriate for the component.
     */
    JPopupMenu createPopupMenu();

    /**
     * The GUI must be able to extract the data from the TestElement and update
     * all GUI fields to represent those data. This method is called to allow
     * JMeter to show the user the GUI that represents the test element's data.
     *
     * <p>
     * The canonical implementation looks like this:
     * <pre>
     * public void configure(TestElement element) {
     *     super.configure(element);
     *     // Using the element getter (preferred):
     *     TestElementXYZ xyz = (TestElementXYZ) element;
     *     guiState.setText(xyz.getState());
     *     guiCode.setText(xyz.getCode());
     *     ... other GUI fields ...
     *     // or using the element property names directly (do not use unless there is no getter for the field)
     *     guiState.setText(element.getPropertyAsString(TestElementXYZ.STATE));
     *     guiCode.setText(element.getPropertyAsString(TestElementXYZ.CODE));
     *     ... other GUI fields ...
     * }
     * </pre>
     *
     * @param element
     *            the TestElement to configure
     */
    void configure(TestElement element);

    /**
     * This is the list of add menu categories this gui component will be
     * available under. For instance, if this represents a Controller, then the
     * MenuFactory.CONTROLLERS category should be in the returned collection.
     * When a user right-clicks on a tree element and looks through the "add"
     * menu, which category your GUI component shows up in is determined by
     * which categories are returned by this method. Most GUI's belong to only
     * one category, but it is possible for a component to exist in multiple
     * categories.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     *
     * @see org.apache.jmeter.gui.util.MenuFactory
     */
    Collection<String> getMenuCategories();
}
