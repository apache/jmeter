package org.apache.jmeter.protocol.http.gui.action;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.plugin.MenuCreator.MENU_LOCATION;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.TextBoxDialoger;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.GenerationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.glass.events.KeyEvent;

public class HtmlReportCommandAction extends AbstractAction implements MenuCreator, ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportCommandAction.class);
    private static final Set<String> commands = new HashSet<>();
    public static final String HTML_REPORT = "html_report";
    private static final String CREATE_REQUEST = "CRATE_REQUEST";
    private static final String BROWSE_CSV = "BROWSE_CSV";

    static {
        commands.add(HTML_REPORT);
    }

    private JTextField cSVFilePathTextField;

    public HtmlReportCommandAction() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) {
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CLOSE));
        showInputDialog();
    }

    private void showInputDialog() {
        EscapeDialog messageDialog = new EscapeDialog(GuiPackage.getInstance().getMainFrame(),
                JMeterUtils.getResString("html_report_menu"), false);
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel fileChooserPanel = new JPanel(new GridLayout(1, 3));

        fileChooserPanel.add(new JLabel(JMeterUtils.getResString("csv_file")));

        cSVFilePathTextField = new JTextField();
        fileChooserPanel.add(cSVFilePathTextField);

        JButton cSVFileButton = new JButton(JMeterUtils.getResString("browse"));
        cSVFileButton.setActionCommand(BROWSE_CSV);
        cSVFileButton.addActionListener(this);
        fileChooserPanel.add(cSVFileButton);

        contentPane.add(fileChooserPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
        JButton reportLaunchButton = new JButton(JMeterUtils.getResString("html_report_request"));
        reportLaunchButton.setActionCommand(CREATE_REQUEST);
        reportLaunchButton.addActionListener(this);
        buttonPanel.add(reportLaunchButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        messageDialog.pack();
        ComponentUtil.centerComponentInComponent(GuiPackage.getInstance().getMainFrame(), messageDialog);
        SwingUtilities.invokeLater(() -> messageDialog.setVisible(true));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case CREATE_REQUEST:
            LOGGER.info("CSV file path : {}", cSVFilePathTextField.getText());
            generateReport(cSVFilePathTextField.getText());
            break;
        case BROWSE_CSV:
            cSVFilePathTextField.setText(
                    FileDialoger.promptToOpenFile(System.getProperty("user.home"), false).getSelectedFile().getPath());
            break;
        default:
            break;
        }
    }

    /**
     * Generate the HTML Report with ReportGenerator class
     * 
     * @param cSVFilePath
     */
    private void generateReport(String cSVFilePath) {
        if ("".equals(cSVFilePath)) {
            JMeterUtils.reportErrorToUser(JMeterUtils.getResString("html_report_file_not_found"));
        } else {
            try {
                ReportGenerator rg = new ReportGenerator(cSVFilePath, null);
                rg.generate();
            } catch (GenerationException | ConfigurationException | IllegalArgumentException e) {
                Object[] arguments = {e.getMessage()};
                String message = MessageFormat.format(
                        JMeterUtils.getResString("html_report_unknown_error") // $NON-NLS-1$
                        , arguments);
                
                JMeterUtils.reportErrorToUser(message);
            }
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if (location == MENU_LOCATION.TOOLS) {
            JMenuItem menuItemIC = new JMenuItem(JMeterUtils.getResString("html_report_menu"), KeyEvent.VK_UNDEFINED);
            menuItemIC.setName(HTML_REPORT);
            menuItemIC.setActionCommand(HTML_REPORT);
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
}
