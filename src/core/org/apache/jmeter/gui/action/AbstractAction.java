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

import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AbstractAction implements Command {
    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * @see Command#doAction(ActionEvent)
     */
    public void doAction(ActionEvent e) {
    }

    /**
     * @see Command#getActionNames()
     */
    abstract public Set<String> getActionNames();

    /**
     * @param e
     */
    protected void popupShouldSave(ActionEvent e) {
        log.debug("popupShouldSave");
        if (GuiPackage.getInstance().getTestPlanFile() == null) {
            if (JOptionPane.showConfirmDialog(GuiPackage.getInstance().getMainFrame(),
                    JMeterUtils.getResString("should_save"),  //$NON-NLS-1$
                    JMeterUtils.getResString("warning"),  //$NON-NLS-1$
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(),ActionNames.SAVE));
            }
        }
    }
}
