package org.apache.jmeter.protocol.http.gui.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.exec.SystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlReportAction {

    public final static String HTML_REPORT_SUCCESS = "html_report_success";

    public final static String ERROR_GENERATING = "html_report_error";

    public final static String NO_FILE_CSV = "no_such_file_csv";
    public final static String WRONG_FILE_CSV = "wrong_type_csv";

    public final static String NO_FILE_USER_PROPERTIES = "no_such_file_user_properties";
    public final static String WRONG_FILE_USER_PROPERTIES = "wrong_type_user_properties";

    public final static String NO_DIRECTORY_OUTPUT = "no_such_directory";
    public final static String NOT_EMPTY_DIRECTORY_OUTPUT = "directory_not_empty";

    private static Logger LOGGER = LoggerFactory.getLogger(HtmlReportAction.class);

    private String cSVFilePath;
    private String userPropertiesFilePath;
    private String outputDirectoryPath;

    public HtmlReportAction(String cSVFilePath, String userPropertiesFilePath, String outputDirectoryPath) {
        this.cSVFilePath = cSVFilePath;
        this.userPropertiesFilePath = userPropertiesFilePath;
        if (outputDirectoryPath == null) {
            this.outputDirectoryPath = JMeterUtils.getJMeterHome() + "bin/report-output/";
        } else {
            this.outputDirectoryPath = outputDirectoryPath;
        }
    }

    /*
     * Preapre and Run the HTML report generation command
     */
    public List<String> run() {
        List<String> returnValue = new ArrayList<>();
        List<String> testFilesResult = testArguments();
        if (testFilesResult.isEmpty()) {
            SystemCommand sc = new SystemCommand(new File(JMeterUtils.getJMeterHome() + "/bin"), null);
            int resultCode = -1;
            try {
                resultCode = sc.run(createGenerationCommand());
            } catch (InterruptedException | IOException e) {
                returnValue.add(ERROR_GENERATING);
            }
            LOGGER.debug("SystemCommand run returned : {}", resultCode);
            if (resultCode == 0) {
                returnValue.add(HTML_REPORT_SUCCESS);

            } else {
                returnValue.add(ERROR_GENERATING);
            }

        } else {
            returnValue.addAll(testFilesResult);
        }
        return returnValue;
    }

    /**
     * create the command for html report generation with all the directories /
     * file
     * 
     * @return the list of arguments for SystemCommand execution
     */
    private List<String> createGenerationCommand() {
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
        arguments.add(outputDirectoryPath);
        LOGGER.debug("Command line for HTML Report generation : {}", arguments.toString());
        return arguments;
    }

    /**
     * test that all arguments are correct and send a message to the user if not
     * 
     * @return whether or not the files are correct
     */
    private List<String> testArguments() {
        List<String> errors = new ArrayList<>();

        String cSVError = checkFile(new File(cSVFilePath), ".csv");
        if (!cSVError.isEmpty()) {
            errors.add(JMeterUtils.getResString("csv_file")+cSVError);
        }

        String userPropertiesError = checkFile(new File(userPropertiesFilePath), ".properties");
        if (!userPropertiesError.isEmpty()) {
            errors.add(JMeterUtils.getResString("user_properties_file")+userPropertiesError);
        }

        String outputError = checkDirectory(new File(outputDirectoryPath));
        if (!outputError.isEmpty()) {
            errors.add(JMeterUtils.getResString("output_directory")+outputError);
        }
        return errors;
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

}
