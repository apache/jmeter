// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public class EnableComponent implements Command
{
    private static Logger log = LoggingManager.getLoggerForClass();

    public static final String ENABLE = "enable";
    public static final String DISABLE = "disable";

    private static Set commands = new HashSet();
    static
    {
        commands.add(ENABLE);
        commands.add(DISABLE);
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    public void doAction(ActionEvent e)
    {
        if (e.getActionCommand().equals(ENABLE))
        {
            log.debug("enabling current gui object");
            GuiPackage.getInstance().getCurrentNode().setEnabled(true);
            GuiPackage.getInstance().getCurrentGui().setEnabled(true);
        }
        else if (e.getActionCommand().equals(DISABLE))
        {
            log.debug("disabling current gui object");
            GuiPackage.getInstance().getCurrentNode().setEnabled(false);
            GuiPackage.getInstance().getCurrentGui().setEnabled(false);
        }
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    public Set getActionNames()
    {
        return commands;
    }
}
