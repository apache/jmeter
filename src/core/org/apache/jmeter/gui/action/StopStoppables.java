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
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.Stoppable;

/**
 * Stops stopables (Proxy, Mirror)
 * @since 2.5.1
 */
public class StopStoppables extends AbstractAction implements ActionListener {
    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.STOP_THREAD);
    }

    /**
     *
     */
    public StopStoppables() {
        super();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.action.AbstractAction#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {

    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.action.AbstractAction#doAction(java.awt.event.ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        GuiPackage instance = GuiPackage.getInstance();
        List<Stoppable> stopables = instance.getStoppables();
        for (Stoppable element : stopables) {
            instance.unregister(element);
            element.stopServer();
        }
    }
}
