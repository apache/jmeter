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

package org.apache.jmeter.processor.gui;

import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.util.ScopePanel;

/**
 * This is the base class for JMeter GUI components which manage PostProcessors.
 * 
 * PostProcessors which can be applied to different scopes (parent, children or both)
 * need to use the createScopePanel() to add the panel to the GUI, and they also
 * need to use saveScopeSettings() and showScopeSettings() to keep the test element
 * and GUI in synch.
 *
 */
public abstract class AbstractPostProcessorGui extends AbstractJMeterGuiComponent {

    private ScopePanel scopePanel;

    public JPopupMenu createPopupMenu() {
        return MenuFactory.getDefaultExtractorMenu();
    }

    public Collection<String> getMenuCategories() {
        return Arrays.asList(new String[] { MenuFactory.POST_PROCESSORS });
    }
    /**
     * Create the scope settings panel.
     * GUIs that want to add the panel need to add the following to the init method:
     * <br/>
     * box.add(createScopePanel());
     * @return the scope settings panel
     */
    protected JPanel createScopePanel(){
        scopePanel = new ScopePanel();
        return scopePanel;
    }

    @Override
    public void clearGui(){
        super.clearGui();
        if (scopePanel != null) {
            scopePanel.clearGui();
        }
    }

    /**
     * Save the scope settings in the test element.
     * Needs to be called by the GUIs modifyTestElement method.
     * @param testElement
     */
    protected void saveScopeSettings(AbstractScopedTestElement testElement) {
        if (scopePanel.isScopeParent()){
            testElement.setScopeParent();
        } else
        if (scopePanel.isScopeChildren()){
            testElement.setScopeChildren();
        } else {
            testElement.setScopeAll();
        }
        
    }

    /**
     * Show the scope settings from the test element.
     * Needs to be called by the GUIs configure method.     * 
     * @param testElement
     */
    protected void showScopeSettings(AbstractScopedTestElement testElement) {
        String scope = testElement.fetchScope();
        if (testElement.isScopeParent(scope)) {
                scopePanel.setScopeParent();                
        } else if (testElement.isScopeChildren(scope)){
            scopePanel.setScopeChildren();
        } else {
            scopePanel.setScopeAll();
        }
    }
}
