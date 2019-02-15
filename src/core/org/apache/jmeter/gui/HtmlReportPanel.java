package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
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
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.HtmlReportAction;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.glass.events.KeyEvent;

public class HtmlReportPanel extends AbstractAction implements MenuCreator, ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportPanel.class);
    private static final Set<String> commands = new HashSet<>();
    public static final String HTML_REPORT = "html_report";
    private static final String CREATE_REQUEST = "CREATE_REQUEST";
    private static final String BROWSE_CSV = "BROWSE_CSV";
    private static final String BROWSE_USER_PROPERTIES = "BROWSE_USER_PROPERTIES";
    private static final String BROWSE_OUTPUT = "BROWSE_OUTPUT";

    static {
        commands.add(HTML_REPORT);
    }

    private JTextField cSVFilePathTextField;
    private JTextField userPropertiesFilePathTextField;
    private JTextField outputDirectoryPathTextField;
    private JButton reportLaunchButton;

    public HtmlReportPanel() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) {
        showInputDialog();
    }

    private void showInputDialog() {
        EscapeDialog messageDialog = new EscapeDialog(GuiPackage.getInstance().getMainFrame(),
                JMeterUtils.getResString("html_report_menu"), false);
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel fileChooserPanel = new JPanel(new GridLayout(3, 3));

        fileChooserPanel.add(new JLabel(JMeterUtils.getResString("csv_file")));

        cSVFilePathTextField = new JTextField();
        fileChooserPanel.add(cSVFilePathTextField);

        JButton cSVFileButton = new JButton(JMeterUtils.getResString("browse"));
        cSVFileButton.setActionCommand(BROWSE_CSV);
        cSVFileButton.addActionListener(this);
        fileChooserPanel.add(cSVFileButton);

        fileChooserPanel.add(new JLabel(JMeterUtils.getResString("user_properties_file")));

        userPropertiesFilePathTextField = new JTextField();
        fileChooserPanel.add(userPropertiesFilePathTextField);

        JButton userPropertiesFileButton = new JButton(JMeterUtils.getResString("browse"));
        userPropertiesFileButton.setActionCommand(BROWSE_USER_PROPERTIES);
        userPropertiesFileButton.addActionListener(this);
        fileChooserPanel.add(userPropertiesFileButton);

        fileChooserPanel.add(new JLabel(JMeterUtils.getResString("output_directory")));

        outputDirectoryPathTextField = new JTextField();
        fileChooserPanel.add(outputDirectoryPathTextField);

        JButton outputDirectoryButton = new JButton(JMeterUtils.getResString("browse"));
        outputDirectoryButton.setActionCommand(BROWSE_OUTPUT);
        outputDirectoryButton.addActionListener(this);
        fileChooserPanel.add(outputDirectoryButton);

        contentPane.add(fileChooserPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
        
        reportLaunchButton = new JButton(JMeterUtils.getResString("html_report_request"));
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
            reportLaunchButton.setText(JMeterUtils.getResString("html_report_processing"));
            reportLaunchButton.setForeground(Color.ORANGE);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("CSV file path : {}", cSVFilePathTextField.getText());
                LOGGER.debug("user.properties file path : {}", userPropertiesFilePathTextField.getText());
                LOGGER.debug("Output directory file path : {}", outputDirectoryPathTextField.getText());
            }
            HtmlReportAction htmlReportAction = new HtmlReportAction(cSVFilePathTextField.getText(),
                    userPropertiesFilePathTextField.getText(), outputDirectoryPathTextField.getText());
            reportToUser(htmlReportAction.run());
            reportLaunchButton.setText(JMeterUtils.getResString("html_report_request"));
            reportLaunchButton.setForeground(null);
            break;
        case BROWSE_USER_PROPERTIES:
            userPropertiesFilePathTextField.setText(showFileChooser(userPropertiesFilePathTextField, false));
            break;
        case BROWSE_CSV:
            cSVFilePathTextField.setText(showFileChooser(cSVFilePathTextField, false));
            break;
        case BROWSE_OUTPUT:
            outputDirectoryPathTextField.setText(showFileChooser(outputDirectoryPathTextField, true));
        default:
            break;
        }
    }

    private void reportToUser(List<String> runResults) {
        if (runResults.isEmpty()) {
            LOGGER.error("No result after HTML Report Generation command");
        } else {
            switch (runResults.get(0)) {
            case HtmlReportAction.ERROR_GENERATING:
                JMeterUtils.reportErrorToUser(JMeterUtils.getResString("html_report_unknown_error"));
                break;
            case HtmlReportAction.HTML_REPORT_SUCCESS:
                JMeterUtils.reportInfoToUser(JMeterUtils.getResString("html_report_success"),
                        JMeterUtils.getResString("html_report_menu"));
                break;
            default:
                // It means that it's a List of file selection error
                sendFileSelectionError(runResults);
                break;
            }
        }
    }

    /**
     * Show a file chooser to the user
     * 
     * @param locationTextField
     *            the textField that will receive the path
     * @param onlyDirectory
     *            whether or not the file chooser will only display directories
     * @return the path the user selected or, if the user cancelled the file
     *         chooser, the previous path
     */
    private String showFileChooser(JTextField locationTextField, boolean onlyDirectory) {
        JFileChooser fileChooser = FileDialoger.promptToOpenFile(System.getProperty("user.home"), onlyDirectory);
        if (fileChooser == null) {
            return locationTextField.getText();
        }
        return fileChooser.getSelectedFile().getPath();
    }

    /**
     * Show file selection errors to the users
     * 
     * @param errors
     *            A list of all selection error messages to send
     */
    private void sendFileSelectionError(List<String> errors) {
        String errorMessage = new String();
        for (String error : errors) {
            errorMessage += error + "\n";
        }
        JMeterUtils.reportErrorToUser(errorMessage);
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
