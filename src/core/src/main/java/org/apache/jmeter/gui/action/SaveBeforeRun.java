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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;

/**
 * Save Before Run Action To save test plan before GUI execution
 *
 * @since 4.0
 */
public class SaveBeforeRun extends AbstractAction {
    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.SAVE_BEFORE_RUN);
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent e) {
        if (ActionNames.SAVE_BEFORE_RUN.equals(e.getActionCommand())) {
            // toggle boolean preference value
            GuiPackage guiInstance = GuiPackage.getInstance();
            boolean togglePreferenceValue = !guiInstance.shouldSaveBeforeRunByPreference();
            guiInstance.setSaveBeforeRunByPreference(togglePreferenceValue);
            // toggle check box
            guiInstance.getMenuItemSaveBeforeRunPanel().getModel().setSelected(togglePreferenceValue);
        }
    }
}
