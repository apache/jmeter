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

package org.apache.jmeter.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.util.JMeterUtils;

public class HtmlReportAction extends AbstractAction implements MenuCreator {
    private static Set<String> commands = new HashSet<>();
    private HtmlReportUI htmlReportPanel;

    static {
        commands.add(ActionNames.HTML_REPORT);
    }

    public HtmlReportAction() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        htmlReportPanel = new HtmlReportUI();
        htmlReportPanel.showInputDialog(getParentFrame(e));
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if (location != MENU_LOCATION.TOOLS) {
            return new JMenuItem[0];
        }

        JMenuItem menuItem = new JMenuItem(JMeterUtils.getResString("generate_report_ui.html_report_menu"), KeyEvent.VK_UNDEFINED);
        menuItem.setName(ActionNames.HTML_REPORT);
        menuItem.setActionCommand(ActionNames.HTML_REPORT);
        menuItem.setAccelerator(null);
        menuItem.addActionListener(ActionRouter.getInstance());
        return new JMenuItem[] { menuItem };
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
        // NOOP
    }

    public HtmlReportUI getHtmlReportPanel() {
        return htmlReportPanel;
    }
}
