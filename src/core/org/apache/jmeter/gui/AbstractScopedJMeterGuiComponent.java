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

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.util.ScopePanel;


public abstract class AbstractScopedJMeterGuiComponent extends AbstractJMeterGuiComponent {

    private static final long serialVersionUID = 240L;

    private ScopePanel scopePanel;

    @Override
    public void clearGui(){
        super.clearGui();
        if (scopePanel != null) {
            scopePanel.clearGui();
        }
    }
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
     * Create the scope settings panel.
     *
     * @return the scope settings panel
     */
    protected JPanel createScopePanel() {
        return createScopePanel(false);
    }

    /**
     * Create the scope settings panel.
     * @param enableVariable set true to enable the variable panel
     * @return the scope settings panel
     */
    protected JPanel createScopePanel(boolean enableVariable) {
        scopePanel = new ScopePanel(enableVariable);
        return scopePanel;
    }

    /**
     * Save the scope settings in the test element.
     *
     * @param testElement
     */
    protected void saveScopeSettings(AbstractScopedTestElement testElement) {
        if (scopePanel.isScopeParent()){
            testElement.setScopeParent();
        } else if (scopePanel.isScopeChildren()){
            testElement.setScopeChildren();
        } else if (scopePanel.isScopeAll()) {
            testElement.setScopeAll();
        } else if (scopePanel.isScopeVariable()) {
            testElement.setScopeVariable(scopePanel.getVariable());
        } else {
            throw new IllegalArgumentException("Unexpected scope panel state");
        }
    }

    /**
     * Show the scope settings from the test element.
     *
     * @param testElement
     */
    protected void showScopeSettings(AbstractScopedTestElement testElement) {
        showScopeSettings(testElement, false);
    }
    
    /**
     * Show the scope settings from the test element with variable scope
     *
     * @param testElement
     * @param enableVariableButton
     */
    protected void showScopeSettings(AbstractScopedTestElement testElement,
            boolean enableVariableButton) {
        String scope = testElement.fetchScope();
        if (testElement.isScopeParent(scope)) {
                scopePanel.setScopeParent(enableVariableButton);
        } else if (testElement.isScopeChildren(scope)){
            scopePanel.setScopeChildren(enableVariableButton);
        } else if (testElement.isScopeAll(scope)){
            scopePanel.setScopeAll(enableVariableButton);
        } else if (testElement.isScopeVariable(scope)){
            scopePanel.setScopeVariable(testElement.getVariableName());
        } else {
            throw new IllegalArgumentException("Invalid scope: "+scope);
        }
    }


}
