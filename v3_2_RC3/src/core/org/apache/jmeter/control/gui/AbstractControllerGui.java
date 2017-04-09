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

package org.apache.jmeter.control.gui;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;

/**
 * This is the base class for JMeter GUI components which manage controllers.
 *
 */
public abstract class AbstractControllerGui extends AbstractJMeterGuiComponent {
    private static final long serialVersionUID = 240L;

    /**
     * When a user right-clicks on the component in the test tree, or selects
     * the edit menu when the component is selected, the component will be asked
     * to return a JPopupMenu that provides all the options available to the
     * user from this component.
     * <p>
     * This implementation returns menu items appropriate for most controller
     * components.
     *
     * @return a JPopupMenu appropriate for the component.
     */
    @Override
    public JPopupMenu createPopupMenu() {
        return MenuFactory.getDefaultControllerMenu();
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under. This implementation returns
     * {@link org.apache.jmeter.gui.util.MenuFactory#CONTROLLERS}, which is
     * appropriate for most controller components.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     */
    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(MenuFactory.CONTROLLERS);
    }
}
