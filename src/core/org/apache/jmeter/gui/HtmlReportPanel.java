package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.HtmlReportGenerator;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlReportPanel implements ActionListener {
    private static Set<String> commands = new HashSet<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportPanel.class);

    private static final String CREATE_REQUEST = "CREATE_REQUEST";
    private static final String BROWSE_CSV = "BROWSE_CSV";
    private static final String BROWSE_USER_PROPERTIES = "BROWSE_USER_PROPERTIES";
    private static final String BROWSE_OUTPUT = "BROWSE_OUTPUT";

    private EscapeDialog messageDialog;

    private JTextField csvFilePathTextField;
    private JTextField userPropertiesFilePathTextField;
    private JTextField outputDirectoryPathTextField;
    private JButton reportLaunchButton;
    private JSyntaxTextArea reportingArea;

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
        messageDialog.setVisible(true);
    }

    public void setupInputDialog() {
        messageDialog = new EscapeDialog(GuiPackage.getInstance().getMainFrame(),
                JMeterUtils.getResString("html_report_menu"), false);

        setupContentPane();
    }

    private void setupContentPane() {
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(setupFileChooserPanel(), BorderLayout.NORTH);

        reportingArea = JSyntaxTextArea.getInstance(10, 30, true);
        reportingArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        contentPane.add(reportingArea, BorderLayout.CENTER);

        contentPane.add(setupButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel setupFileChooserPanel() {
        JPanel fileChooserPanel = new JPanel(new GridLayout(3, 3));
        fileChooserPanel.add(new JLabel(JMeterUtils.getResString("csv_file")));

        csvFilePathTextField = new JTextField();
        fileChooserPanel.add(csvFilePathTextField);

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

    private class ReportGenerationWorker extends SwingWorker<List<String>, String> {

        @Override
        protected List<String> doInBackground() throws Exception {
            HtmlReportGenerator htmlReportAction = new HtmlReportGenerator(csvFilePathTextField.getText(),
                    userPropertiesFilePathTextField.getText(), outputDirectoryPathTextField.getText());

            return htmlReportAction.run();
        }

        @Override
        protected void done() {
            try {
                reportToUser(get());
            } catch (InterruptedException | ExecutionException exception) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error during html report generation : {}", exception.getMessage(), exception);
                }
                List<String> reportException = new ArrayList<>();
                reportException.add(exception.getMessage());
                reportToUser(reportException);
            }
        }
    }

    private void reportingAddText(String resString) {
        reportingArea.setText(reportingArea.getText() + resString + "\n");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case CREATE_REQUEST:
            try {
                reportingArea.setText(JMeterUtils.getResString("html_report_processing") + "\n");
                reportLaunchButton.setForeground(Color.orange);
                new ReportGenerationWorker().execute();
            } catch (Exception exception) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error during html report generation : {}", exception.getMessage(), exception);
                }
            }
            if (LOGGER.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("CSV file path {} \n").append("user.properties file path : {} \n")
                        .append("Output directory file path : {}");
                LOGGER.debug(sb.toString(), csvFilePathTextField.getText(), userPropertiesFilePathTextField.getText(),
                        outputDirectoryPathTextField.getText());
            }
            break;
        case BROWSE_USER_PROPERTIES:
            userPropertiesFilePathTextField.setText(showFileChooser(userPropertiesFilePathTextField, false));
            break;
        case BROWSE_CSV:
            csvFilePathTextField.setText(showFileChooser(csvFilePathTextField, false));
            break;
        case BROWSE_OUTPUT:
            outputDirectoryPathTextField.setText(showFileChooser(outputDirectoryPathTextField, true));
        default:
            break;
        }
    }

    private void reportToUser(List<String> runResults) {
        if (runResults.isEmpty()) {
            reportingAddText(JMeterUtils.getResString(HtmlReportGenerator.HTML_REPORT_SUCCESS));
            reportLaunchButton.setForeground(Color.green);
        } else {
            StringBuilder sb = new StringBuilder();
            for (String errorString : runResults) {
                sb.append(errorString + "\n");
            }
            reportingAddText(sb.toString());
            reportLaunchButton.setForeground(Color.red);
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

    public JTextField getCsvFilePathTextField() {
        return csvFilePathTextField;
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

    public JSyntaxTextArea getReportingArea() {
        return reportingArea;
    }

    public EscapeDialog getMessageDialog() {
        return messageDialog;
    }
}
