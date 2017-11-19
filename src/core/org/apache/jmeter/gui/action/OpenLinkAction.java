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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenLinkAction extends AbstractAction {

    private static final Logger log = LoggerFactory.getLogger(OpenLinkAction.class);

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.LINK_BUG_TRACKER);
        commands.add(ActionNames.LINK_NIGHTLY_BUILD);
        commands.add(ActionNames.LINK_RELEASE_NOTES);
    }

    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        String url = null;
        if (e.getActionCommand().equals(ActionNames.LINK_BUG_TRACKER)) {
            url = "https://jmeter.apache.org/issues.html";
        } else if (e.getActionCommand().equals(ActionNames.LINK_NIGHTLY_BUILD)) {
            url = "https://jmeter.apache.org/nightly.html";
        } else if (e.getActionCommand().equals(ActionNames.LINK_RELEASE_NOTES)) {
            url = "https://jmeter.apache.org/changes.html";
        } else {
            log.warn("Action {} not handled by this class", e.getActionCommand());
            return;
        }
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException err) {
            log.error("OpenLinkAction: User default browser is not found, or it fails to be launched, or the default handler application failed to be launched on {}", err);
        } catch (UnsupportedOperationException err) {
            log.error("OpenLinkAction: Current platform does not support the Desktop.Action.BROWSE actionon {}", err);
        } catch (SecurityException err) {
            log.error("OpenLinkAction: Security problem on {}", err);
        } catch (Exception err) {
            log.error("OpenLinkAction on {}", err);
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

}
