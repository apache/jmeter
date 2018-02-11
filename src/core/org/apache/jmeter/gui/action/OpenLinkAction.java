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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenLinkAction extends AbstractAction {

    private static final Logger log = LoggerFactory.getLogger(OpenLinkAction.class);

    private static final Map<String, String> LINK_MAP =
            initLinkMap();

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.LINK_BUG_TRACKER);
        commands.add(ActionNames.LINK_COMP_REF);
        commands.add(ActionNames.LINK_FUNC_REF);
        commands.add(ActionNames.LINK_NIGHTLY_BUILD);
        commands.add(ActionNames.LINK_RELEASE_NOTES);
    }

    private static final Map<String, String> initLinkMap() {
        Map<String, String> map = new HashMap<>(5);
        map.put(ActionNames.LINK_BUG_TRACKER, "https://jmeter.apache.org/issues.html");
        map.put(ActionNames.LINK_COMP_REF, "https://jmeter.apache.org/usermanual/component_reference.html");
        map.put(ActionNames.LINK_FUNC_REF, "https://jmeter.apache.org/usermanual/functions.html");
        map.put(ActionNames.LINK_NIGHTLY_BUILD, "https://jmeter.apache.org/nightly.html");
        map.put(ActionNames.LINK_RELEASE_NOTES, "https://jmeter.apache.org/changes.html");
        return map;
    }
    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        String url = LINK_MAP.get(e.getActionCommand());
        if(url == null) {
            log.warn("Action {} not handled by this class", e.getActionCommand());
            return;
        }
        try {
            if(e.getSource() instanceof String[]) {
                url += "#"+((String[])e.getSource())[1];
            }
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException err) {
            log.error(
                    "OpenLinkAction: User default browser is not found, or it fails to be launched,"
                    + " or the default handler application failed to be launched on {}",
                    url, err);
        } catch (UnsupportedOperationException err) {
            log.error("OpenLinkAction: Current platform does not support the Desktop.Action.BROWSE action on {}", url, err);
            showBrowserWarning(url);
        } catch (SecurityException err) {
            log.error("OpenLinkAction: Security problem on {}", url, err);
        } catch (Exception err) {
            log.error("OpenLinkAction on {}", url, err);
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    private void showBrowserWarning(String url) {
        String problemSolver;
        if (url.startsWith(LINK_MAP.get(ActionNames.LINK_COMP_REF))
                || url.startsWith(LINK_MAP.get(ActionNames.LINK_FUNC_REF))) {
            problemSolver = "\n\nTry to set the system property help.local to true.";
        } else {
            problemSolver = "";
        }
        JOptionPane.showMessageDialog(null, String.format(
                "Problem opening a browser to show the content of the URL%n%s%s",
                url, problemSolver), "Problem opening browser",
                JOptionPane.WARNING_MESSAGE);
    }

}
