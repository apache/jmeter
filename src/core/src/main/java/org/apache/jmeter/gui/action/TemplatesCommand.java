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

/**
 * Open Templates 
 * @since 2.10
 */
public class TemplatesCommand extends AbstractActionWithNoRunningTest {

    private static final Set<String> commands = new HashSet<>();

    // Ensure the dialog is only created when it is first needed
    // In turn this avoids scanning the templates until first needed
    static class IODH {
        private static final SelectTemplatesDialog dialog = new SelectTemplatesDialog();        
    }

    static {
        commands.add(ActionNames.TEMPLATES);
    }

    /**
     * @see org.apache.jmeter.gui.action.AbstractActionWithNoRunningTest#doActionAfterCheck(ActionEvent)
     */
    @Override
    public void doActionAfterCheck(ActionEvent e) {
        IODH.dialog.setVisible(true);
    }

    /**
     * @see Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
