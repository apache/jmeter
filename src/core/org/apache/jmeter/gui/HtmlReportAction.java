package org.apache.jmeter.gui;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import java.awt.event.KeyEvent;

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
    public static Set<String> commands = new HashSet<>();
    private HtmlReportPanel htmlReportPanel;

    static {
        commands.add(ActionNames.HTML_REPORT);
    }

    public HtmlReportAction() {
    }

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {

        htmlReportPanel = new HtmlReportPanel();
        htmlReportPanel.showInputDialog();
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if (location == MENU_LOCATION.TOOLS) {
            JMenuItem menuItemIC = new JMenuItem(JMeterUtils.getResString("html_report_menu"), KeyEvent.VK_UNDEFINED);
            menuItemIC.setName(ActionNames.HTML_REPORT);
            menuItemIC.setActionCommand(ActionNames.HTML_REPORT);
            menuItemIC.setAccelerator(null);
            menuItemIC.addActionListener(ActionRouter.getInstance());
            return new JMenuItem[] { menuItemIC };
        }
        return new JMenuItem[0];
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

    public HtmlReportPanel getHtmlReportPanel() {
        return htmlReportPanel;
    }

}
