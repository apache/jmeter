package org.apache.jmeter.gui.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.exec.SystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlReportGenerator {

    public final static String HTML_REPORT_SUCCESS = "html_report_success";

    public final static String ERROR_GENERATING = "html_report_error";

    public final static String NO_FILE = "no_such_file";
    public final static String WRONG_FILE = "wrong_type";

    public final static String NO_DIRECTORY = "no_such_directory";
    public final static String NOT_EMPTY_DIRECTORY = "directory_not_empty";

    private static Logger LOGGER = LoggerFactory.getLogger(HtmlReportGenerator.class);

    private String cSVFilePath;
    private String userPropertiesFilePath;
    private String outputDirectoryPath;

    public HtmlReportGenerator(String cSVFilePath, String userPropertiesFilePath, String outputDirectoryPath) {
        this.cSVFilePath = cSVFilePath;
        this.userPropertiesFilePath = userPropertiesFilePath;
        if (outputDirectoryPath == null) {
            this.outputDirectoryPath = JMeterUtils.getJMeterBinDir() + "/report-output/";
        } else {
            this.outputDirectoryPath = outputDirectoryPath;
        }
    }

    /*
     * Prepare and Run the HTML report generation command
     */
    public List<String> run() {
        List<String> returnValue = new ArrayList<>();
        List<String> testFilesResult = testArguments();
        if (testFilesResult.isEmpty()) {
            ByteArrayOutputStream commandExecutionError = new ByteArrayOutputStream();
            int resultCode = -1;
            List<String> generationCommand = createGenerationCommand();
            try {
                SystemCommand sc = new SystemCommand(new File(JMeterUtils.getJMeterBinDir()), 60000, 100, null, null,
                        null, commandExecutionError);
                resultCode = sc.run(generationCommand);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Running ");
                }
                if (resultCode == 0) {
                    returnValue.add(HTML_REPORT_SUCCESS);

                } else {
                    returnValue.add(ERROR_GENERATING);
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("The HTML report generation failed : {}", commandExecutionError);
                    }
                }
            } catch (InterruptedException | IOException e) {
                returnValue.add(ERROR_GENERATING);
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error during HTML report generation : {}", e.getMessage(),e);
                }
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SystemCommand ran : {}", generationCommand);
                LOGGER.debug("SystemCommand returned : {}", resultCode);
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
        arguments.add(JMeterUtils.getJMeterBinDir() + "/ApacheJMeter.jar");
        arguments.add("-p");
        arguments.add(JMeterUtils.getJMeterBinDir() + "/jmeter.properties");
        arguments.add("-q");
        arguments.add(userPropertiesFilePath);
        arguments.add("-g");
        arguments.add(cSVFilePath);
        arguments.add("-j");
        arguments.add(JMeterUtils.getJMeterBinDir() + "/jmeter.log");
        arguments.add("-o");
        arguments.add(outputDirectoryPath);
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
            return JMeterUtils.getResString(NO_FILE);
        }
        if (!fileToCheck.getName().contains(extension)) {
            return JMeterUtils.getResString(WRONG_FILE);
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
            return JMeterUtils.getResString(NO_DIRECTORY);
        }
        if (directoryToCheck.list().length > 0) {
            return JMeterUtils.getResString(NOT_EMPTY_DIRECTORY);
        }
        return "";
    }

}
