package org.apache.jmeter.protocol.http.gui.action;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.exec.SystemCommand;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.glass.events.KeyEvent;

public class HtmlReportCommandAction extends AbstractAction implements MenuCreator, ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportCommandAction.class);
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

    public HtmlReportCommandAction() {
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("CSV file path : {}", cSVFilePathTextField.getText());
                LOGGER.debug("user.properties file path : {}", userPropertiesFilePathTextField.getText());
                LOGGER.debug("Output directory file path : {}", outputDirectoryPathTextField.getText());
            }
            generateReport(cSVFilePathTextField.getText(), userPropertiesFilePathTextField.getText(),
                    outputDirectoryPathTextField.getText());
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
     * Generate the HTML Report with ReportGenerator class
     * 
     * @param cSVFilePath
     *            A string that contains the path to the CSV test result file
     * @param userPropertiesFilePath
     *            A string that contains the path to user.properties file
     * @param outputDirectoryPath
     *            A string that contains the path to the directory where the
     *            report should be made
     */
    private void generateReport(String cSVFilePath, String userPropertiesFilePath, String outputDirectoryPath) {
        if (testArguments(cSVFilePath, userPropertiesFilePath, outputDirectoryPath)) {
            List<String> arguments = createGenerationCommand(cSVFilePath, userPropertiesFilePath, outputDirectoryPath);
            try {
                SystemCommand sc = new SystemCommand(new File(JMeterUtils.getJMeterHome() + "/bin"), null);
                int resultCode = sc.run(arguments);
                LOGGER.debug("SystemCommand run returned : {}", resultCode);
                if (sc.run(arguments) == 1) {
                    JMeterUtils.reportInfoToUser(JMeterUtils.getResString("html_report_success"),
                            JMeterUtils.getResString("html_report_menu"));
                    
                } else {
                    JMeterUtils.reportErrorToUser(JMeterUtils.getResString("html_report_error"),
                            JMeterUtils.getResString("html_report_menu"));
                }
            } catch (InterruptedException | IOException e) {
                LOGGER.error("Error during report generation : {}", e.getMessage(), e);
            }
        }
    }

    /**
     * create the command for html report generation with all the directories /
     * file
     * 
     * @param cSVFilePath
     *            the path to the CSV file the user selected
     * @param userPropertiesFilePath
     *            the path to the user.properties file the user selected
     * @param outputDirectoryPath
     *            the path to the directory where the user wants to generate the
     *            report
     * @return the list of arguments for SystemCommand execution
     */
    private List<String> createGenerationCommand(String cSVFilePath, String userPropertiesFilePath,
            String outputDirectoryPath) {
        List<String> arguments = new ArrayList<>();
        String java = System.getProperty("java.home") + "/bin/java";
        arguments.add(java);
        arguments.add("-jar");
        arguments.add(JMeterUtils.getJMeterHome() + "/bin/ApacheJMeter.jar");
        arguments.add("-q");
        arguments.add(userPropertiesFilePath);
        arguments.add("-g");
        arguments.add(cSVFilePath);
        arguments.add("-o");
        if (!StringUtils.isEmpty(outputDirectoryPath)) {
            arguments.add(outputDirectoryPath);
        } else {
            arguments.add(JMeterUtils.getJMeterHome() + "bin/report-output/");
        }
        LOGGER.debug("Command line for HTML Report generation : {}", arguments.toString());
        return arguments;
    }

    /**
     * test that all arguments are correct and send a message to the user if not
     * 
     * @param cSVFilePath
     *            the path to the CSV file the user selected
     * @param userPropertiesFilePath
     *            the path to the user.properties file the user selected
     * @param outputDirectoryPath
     *            the path to the directory where the user wants to generate the
     *            report
     * @return whether or not the files are correct
     */
    private boolean testArguments(String cSVFilePath, String userPropertiesFilePath, String outputDirectoryPath) {
        List<String> errors = new ArrayList<>();

        String cSVError = checkFile(new File(cSVFilePath), ".csv");
        if (!cSVError.isEmpty()) {
            errors.add(JMeterUtils.getResString("csv_file") + cSVError);
        }

        String userPropertiesError = checkFile(new File(userPropertiesFilePath), ".properties");
        if (!userPropertiesError.isEmpty()) {
            errors.add(JMeterUtils.getResString("user_properties_file") + userPropertiesError);
        }

        String outputError = checkDirectory(new File(outputDirectoryPath));
        if (!outputError.isEmpty()) {
            errors.add(JMeterUtils.getResString("output_directory") + outputError);
        }

        if (errors.isEmpty()) {
            return true;
        }
        sendFileSelectionError(errors);
        return false;
    }

    /**
     * Check if a file is accurate for report generation
     * 
     * @param fileToCheck
     *            the directory to check
     * @param extension
     *            the needed file extension for the file
     * @return the error message or an empty string if the file is accurate
     */
    private String checkFile(File fileToCheck, String extension) {
        if (!fileToCheck.exists() || !fileToCheck.isFile()) {
            return JMeterUtils.getResString("no_such_file");
        }
        if (!fileToCheck.getName().contains(extension)) {
            return JMeterUtils.getResString("wrong_type");
        }
        return "";
    }

    /**
     * Check if a directory is fine for report generation
     * 
     * @param directoryToCheck
     *            the directory to check
     * @return the error message or an empty string if the directory is fine
     */
    private String checkDirectory(File directoryToCheck) {
        if (!directoryToCheck.exists() || !directoryToCheck.isDirectory()) {
            return JMeterUtils.getResString("no_such_directory");
        }
        if (directoryToCheck.list().length > 0) {
            return JMeterUtils.getResString("directory_not_empty");
        }
        return "";
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
