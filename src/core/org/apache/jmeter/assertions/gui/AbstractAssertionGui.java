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

package org.apache.jmeter.assertions.gui;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.AbstractScopedAssertion;
import org.apache.jmeter.util.ScopePanel;

/**
 * This is the base class for JMeter GUI components which manage assertions.
 * 
 * Assertions which can be applied to different scopes (parent, children or both)
 * need to use the createScopePanel() to add the panel to the GUI, and they also
 * need to use saveScopeSettings() and showScopeSettings() to keep the test element
 * and GUI in synch.
 *
 */
public abstract class AbstractAssertionGui extends AbstractJMeterGuiComponent {

    private static final long serialVersionUID = 240L;

    private ScopePanel assertionScopePanel;
    
    /**
     * When a user right-clicks on the component in the test tree, or selects
     * the edit menu when the component is selected, the component will be asked
     * to return a JPopupMenu that provides all the options available to the
     * user from this component.
     * <p>
     * This implementation returns menu items appropriate for most assertion
     * components.
     *
     * @return a JPopupMenu appropriate for the component.
     */
    public JPopupMenu createPopupMenu() {
        return MenuFactory.getDefaultAssertionMenu();
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under. This implementation returns
     * {@link org.apache.jmeter.gui.util.MenuFactory#ASSERTIONS}, which is
     * appropriate for most assertion components.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     */
    public Collection<String> getMenuCategories() {
        return Arrays.asList(new String[] { MenuFactory.ASSERTIONS });
    }
    
    /**
     * Create the scope settings panel.
     * 
     * @return the scope settings panel
     */
    protected JPanel createScopePanel(){
        assertionScopePanel = new ScopePanel();
        return assertionScopePanel;
    }

    @Override
    public void clearGui(){
        super.clearGui();
        if (assertionScopePanel != null) {
            assertionScopePanel.clearGui();
        }
    }

    /**
     * Save the scope settings in the test element.
     * 
     * @param assertion
     */
    protected void saveScopeSettings(AbstractScopedAssertion assertion) {
        if (assertionScopePanel.isScopeParent()){
            assertion.setScopeParent();
        } else
        if (assertionScopePanel.isScopeChildren()){
            assertion.setScopeChildren();
        } else {
            assertion.setScopeAll();
        }
        
    }

    /**
     * Show the scope settings from the test element.
     * 
     * @param assertion
     */
    protected void showScopeSettings(AbstractScopedAssertion assertion) {
        String scope = assertion.fetchScope();
        if (assertion.isScopeParent(scope)) {
                assertionScopePanel.setScopeParent();                
        } else if (assertion.isScopeChildren(scope)){
            assertionScopePanel.setScopeChildren();
        } else {
            assertionScopePanel.setScopeAll();
        }
    }
}