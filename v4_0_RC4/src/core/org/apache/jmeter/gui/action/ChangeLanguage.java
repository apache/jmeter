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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JMeterError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Change language
 */
public class ChangeLanguage extends AbstractActionWithNoRunningTest {
    private static final Set<String> commands = new HashSet<>();

    private static final Logger log = LoggerFactory.getLogger(ChangeLanguage.class);

    static {
        commands.add(ActionNames.CHANGE_LANGUAGE);
    }

    /**
     * @see org.apache.jmeter.gui.action.AbstractActionWithNoRunningTest#doActionAfterCheck(ActionEvent)
     */
    @Override
    public void doActionAfterCheck(ActionEvent e) {
        String locale = ((Component) e.getSource()).getName();
        Locale loc;

        int sep = locale.indexOf('_');
        if (sep > 0) {
            loc = new Locale(locale.substring(0, sep), locale.substring(sep + 1));
        } else {
            loc = new Locale(locale, "");
        }
        log.debug("Changing locale to {}", loc);
        try {
            JMeterUtils.setLocale(loc);
        } catch (JMeterError err) {
            JMeterUtils.reportErrorToUser(err.toString());
        }
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#getActionNames()
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }
}
