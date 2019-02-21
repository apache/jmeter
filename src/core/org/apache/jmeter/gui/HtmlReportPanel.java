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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.HtmlReportGenerator;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.VisibleForTesting;

public class HtmlReportPanel implements ActionListener {
    private static Set<String> commands = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportPanel.class);

    private static final String CREATE_REQUEST = "CREATE_REQUEST";
    private static final String BROWSE_CSV = "BROWSE_CSV";
    private static final String BROWSE_USER_PROPERTIES = "BROWSE_USER_PROPERTIES";
    private static final String BROWSE_OUTPUT = "BROWSE_OUTPUT";

    private EscapeDialog messageDialog;

    private JTextField cSVFilePathTextField;
    private JTextField userPropertiesFilePathTextField;
    private JTextField outputDirectoryPathTextField;
    private JButton reportLaunchButton;

    static {
        commands.add(ActionNames.HTML_REPORT);
    }

    public HtmlReportPanel() {

    }

    public void showInputDialog() {
        setupInputDialog();
        launchInputDialog();
    }
    
    private void launchInputDialog() {
        messageDialog.pack();
        ComponentUtil.centerComponentInComponent(GuiPackage.getInstance().getMainFrame(), messageDialog);
        SwingUtilities.invokeLater(() -> messageDialog.setVisible(true));
    }
    
    @VisibleForTesting
    public void setupInputDialog() {
        messageDialog = new EscapeDialog(GuiPackage.getInstance().getMainFrame(),
                JMeterUtils.getResString("html_report_menu"), false);

        setupContentPane();

    }

    private void setupContentPane() {
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(setupFileChooserPanel(), BorderLayout.CENTER);

        contentPane.add(setupButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel setupFileChooserPanel() {
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
        return fileChooserPanel;
    }

    private JPanel setupButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 1));

        reportLaunchButton = new JButton(JMeterUtils.getResString("html_report_request"));
        reportLaunchButton.setActionCommand(CREATE_REQUEST);
        reportLaunchButton.addActionListener(this);
        buttonPanel.add(reportLaunchButton);
        return buttonPanel;
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
            HtmlReportGenerator htmlReportAction = new HtmlReportGenerator(cSVFilePathTextField.getText(),
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
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("No result after HTML Report Generation command");
            }
        } else {
            switch (runResults.get(0)) {
            case HtmlReportGenerator.ERROR_GENERATING:
                JMeterUtils.reportErrorToUser(JMeterUtils.getResString("html_report_unknown_error"));
                break;
            case HtmlReportGenerator.HTML_REPORT_SUCCESS:
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

    public JTextField getcSVFilePathTextField() {
        return cSVFilePathTextField;
    }

    public void setcSVFilePathTextField(JTextField cSVFilePathTextField) {
        this.cSVFilePathTextField = cSVFilePathTextField;
    }

    public JTextField getUserPropertiesFilePathTextField() {
        return userPropertiesFilePathTextField;
    }

    public JTextField getOutputDirectoryPathTextField() {
        return outputDirectoryPathTextField;
    }

    public JButton getReportLaunchButton() {
        return reportLaunchButton;
    }

    public EscapeDialog getMessageDialog() {
        return messageDialog;
    }

    @VisibleForTesting
    public void setMessageDialog(EscapeDialog messageDialog) {
        this.messageDialog = messageDialog;
    }

}
