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

import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Implements log level setting menu item.
 * @since 3.2
 */
public class LogLevelCommand extends AbstractAction {

    private static final Logger log = LoggerFactory.getLogger(LogLevelCommand.class);

    private static final Set<String> commands = new HashSet<>();

    static {
        for (Level level : Level.values()) {
            commands.add(ActionNames.LOG_LEVEL_PREFIX + level.toString());
        }
    }

    public LogLevelCommand() {
        super();
    }

    @Override
    public void doAction(ActionEvent ev) {
        String levelString = ev.getActionCommand().substring(ActionNames.LOG_LEVEL_PREFIX.length());
        log.warn("Setting root log level: {}", levelString);
        Configurator.setRootLevel(org.apache.logging.log4j.Level.toLevel(levelString));
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
