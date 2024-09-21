/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JMeterUIDefaults;

import com.google.auto.service.AutoService;

/**
 * Zoom IN/OUT
 * @since 3.2
 */
@AutoService(Command.class)
public class ZoomInOut extends AbstractAction {
    private static final Set<String> commands = new HashSet<>();

    private static final float ZOOM_SCALE = JMeterUtils.getPropDefault("zoom_scale", 1.1f);

    static {
        commands.add(ActionNames.ZOOM_IN);
        commands.add(ActionNames.ZOOM_OUT);
    }

    /**
     * @see org.apache.jmeter.gui.action.AbstractActionWithNoRunningTest#doActionAfterCheck(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        final String actionCommand = e.getActionCommand();
        float scale = JMeterUIDefaults.INSTANCE.getScale();
        if (actionCommand.equals(ActionNames.ZOOM_IN)) {
            scale *= ZOOM_SCALE;
        } else if (actionCommand.equals(ActionNames.ZOOM_OUT)) {
            scale /= ZOOM_SCALE;
        }
        JMeterUIDefaults.INSTANCE.setScale(scale);
        JMeterUtils.refreshUI();
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
