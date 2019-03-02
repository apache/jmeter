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

import javax.swing.JOptionPane;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;

/**
 * {@link AbstractAction} implementation that check no test is running 
 * before calling {@link AbstractActionWithNoRunningTest#doActionAfterCheck(ActionEvent)}
 * @since 3.1
 */
public abstract class AbstractActionWithNoRunningTest extends AbstractAction {

    @Override
    public final void doAction(ActionEvent e) throws IllegalUserActionException {
        if (JMeterUtils.isTestRunning()) {
            JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("action_check_message"),  //$NON-NLS-1$
                    JMeterUtils.getResString("action_check_title"),  //$NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        doActionAfterCheck(e);
    }

    /**
     * Called to handle {@link ActionEvent} only if no test is running
     * @param e {@link ActionEvent}
     * @throws IllegalUserActionException when user action is invalid
     */
    protected abstract void doActionAfterCheck(ActionEvent e) throws IllegalUserActionException;
}
