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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.exec.SystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlReportGenerator {

    public static final String HTML_REPORT_SUCCESS = "generate_report_ui.html_report_success";
    public static final String ERROR_GENERATING = "generate_report_ui.html_report_error";
    public static final String NO_FILE = "generate_report_ui.no_such_file";
    public static final String NO_DIRECTORY = "generate_report_ui.no_such_directory";
    public static final String NOT_EMPTY_DIRECTORY = "generate_report_ui.directory_not_empty";
    public static final String CANNOT_CREATE_DIRECTORY = "generate_report_ui.cannot_create_directory";

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportGenerator.class);
    private static final long COMMAND_TIMEOUT = JMeterUtils.getPropDefault("generate_report_ui.generation_timeout", 120000L);

    private String csvFilePath;
    private String userPropertiesFilePath;
    private String outputDirectoryPath;

    public HtmlReportGenerator(String csvFilePath, String userPropertiesFilePath, String outputDirectoryPath) {
        this.csvFilePath = csvFilePath;
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
        List<String> errorMessageList = new ArrayList<>();
        errorMessageList.addAll(checkArguments());
        if (!errorMessageList.isEmpty()) {
            return errorMessageList;
        }
        
        ByteArrayOutputStream commandExecutionOutput = new ByteArrayOutputStream();
        int resultCode = -1;
        List<String> generationCommand = createGenerationCommand();
        try {
            SystemCommand sc = new SystemCommand(new File(JMeterUtils.getJMeterBinDir()), COMMAND_TIMEOUT, 100, null, null,
                    commandExecutionOutput, null);
            LOGGER.debug("Running report generation");
            resultCode = sc.run(generationCommand);
            if (resultCode != 0) {
                errorMessageList.add(commandExecutionOutput.toString());
                LOGGER.info("The HTML report generation failed and returned: {}", commandExecutionOutput);
                return errorMessageList;
            }
        } catch (InterruptedException | IOException e) {
            errorMessageList.add(commandExecutionOutput.toString());
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error during HTML report generation: {}", e.getMessage(), e);
            }
        }
        LOGGER.debug("SystemCommand ran: {}  returned: {}", generationCommand, resultCode);
        return errorMessageList;
    }

    /**
     * create the command for html report generation with all the directories /
     * file
     * 
     * @return the list of arguments for SystemCommand execution
     */
    private List<String> createGenerationCommand() {
        String jmeterBinDir = JMeterUtils.getJMeterBinDir();
        List<String> arguments = new ArrayList<>();
        String java = System.getProperty("java.home") + "/bin/java";
        arguments.add(java);
        arguments.add("-jar");
        arguments.add(jmeterBinDir + "/ApacheJMeter.jar");
        arguments.add("-p");
        arguments.add(jmeterBinDir + "/jmeter.properties");
        arguments.add("-q");
        arguments.add(userPropertiesFilePath);
        arguments.add("-g");
        arguments.add(csvFilePath);
        arguments.add("-j");
        arguments.add(jmeterBinDir + "/jmeter_html_report.log");
        arguments.add("-o");
        arguments.add(outputDirectoryPath);
        return arguments;
    }

    /**
     * test that all arguments are correct and send a message to the user if not
     * 
     * @return whether or not the files are correct
     */
    private List<String> checkArguments() {
        List<String> errors = new ArrayList<>();

        String csvError = checkFile(new File(csvFilePath));
        if (csvError != null) {
            errors.add(JMeterUtils.getResString("generate_report_ui.csv_file") + csvError);
        }

        String userPropertiesError = checkFile(new File(userPropertiesFilePath));
        if (userPropertiesError != null) {
            errors.add(JMeterUtils.getResString("generate_report_ui.user_properties_file") + userPropertiesError);
        }

        String outputError = checkDirectory(new File(outputDirectoryPath));
        if (outputError != null) {
            errors.add(JMeterUtils.getResString("generate_report_ui.output_directory") + outputError);
        }
        return errors;
    }

    /**
     * Check if a file is correct for report generation
     * 
     * @param fileToCheck
     *            the directory to check
     * @return the error message or null if the file is ok
     */
    private String checkFile(File fileToCheck) {
        if (fileToCheck.exists() && fileToCheck.canRead() && fileToCheck.isFile()) {
            return null;
        } else {
            return MessageFormat.format(JMeterUtils.getResString(NO_FILE), fileToCheck);
        }
    }

    /**
     * Check if a directory is fine for report generation
     * 
     * @param directoryToCheck
     *            the directory to check
     * @return the error message or an empty string if the directory is fine
     */
    private String checkDirectory(File directoryToCheck) {
        if (directoryToCheck.exists()) {
            String[] files = directoryToCheck.list();
            if (files != null && files.length > 0) {
                return MessageFormat.format(JMeterUtils.getResString(NOT_EMPTY_DIRECTORY), directoryToCheck);
            } else {
                return null;
            }
        } else {
            File parentDirectory = directoryToCheck.getParentFile();
            if(parentDirectory != null && parentDirectory.exists() && parentDirectory.canWrite()) {
                if(directoryToCheck.mkdir()) {
                    return null;
                } else {
                    return MessageFormat.format(JMeterUtils.getResString(CANNOT_CREATE_DIRECTORY), directoryToCheck);
                }
            } else {
                return MessageFormat.format(JMeterUtils.getResString(CANNOT_CREATE_DIRECTORY), directoryToCheck);
            }
        }
    }
}
