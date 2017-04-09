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

package org.apache.jmeter.protocol.system;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.exec.SystemCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler for executing a System function. 
 */
public class SystemSampler extends AbstractSampler {

    private static final int POLL_INTERVAL = JMeterUtils.getPropDefault("os_sampler.poll_for_timeout", SystemCommand.POLL_INTERVAL);

    private static final long serialVersionUID = 1;
    
    // + JMX names, do not change their values
    public static final String COMMAND = "SystemSampler.command";
    
    public static final String DIRECTORY = "SystemSampler.directory";

    public static final String ARGUMENTS = "SystemSampler.arguments";
    
    public static final String ENVIRONMENT = "SystemSampler.environment";

    public static final String CHECK_RETURN_CODE = "SystemSampler.checkReturnCode";
    
    public static final String EXPECTED_RETURN_CODE = "SystemSampler.expectedReturnCode";
    
    private static final String STDOUT = "SystemSampler.stdout";

    private static final String STDERR = "SystemSampler.stderr";

    private static final String STDIN = "SystemSampler.stdin";

    private static final String TIMEOUT = "SystemSampler.timeout";

    // - JMX names

    /**
     * Logging
     */
    private static final Logger log = LoggerFactory.getLogger(SystemSampler.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

    public static final int DEFAULT_RETURN_CODE = 0;


    /**
     * Create a SystemSampler.
     */
    public SystemSampler() {
        super();
    }
    
    /**
     * Performs a test sample.
     * 
     * @param entry
     *            the Entry for this sample
     * @return test SampleResult
     */
    @Override
    public SampleResult sample(Entry entry) {
        SampleResult results = new SampleResult();
        results.setDataType(SampleResult.TEXT);
        results.setSampleLabel(getName());
        
        String command = getCommand();
        Arguments args = getArguments();
        Arguments environment = getEnvironmentVariables();
        boolean checkReturnCode = getCheckReturnCode();
        int expectedReturnCode = getExpectedReturnCode();
        List<String> cmds = new ArrayList<>(args.getArgumentCount() + 1);
        StringBuilder cmdLine = new StringBuilder((null == command) ? "" : command);
        cmds.add(command);
        for (int i=0;i<args.getArgumentCount();i++) {
            Argument arg = args.getArgument(i);
            cmds.add(arg.getPropertyAsString(Argument.VALUE));
            cmdLine.append(" ");
            cmdLine.append(cmds.get(i+1));
        }

        Map<String,String> env = new HashMap<>();
        for (int i=0;i<environment.getArgumentCount();i++) {
            Argument arg = environment.getArgument(i);
            env.put(arg.getName(), arg.getPropertyAsString(Argument.VALUE));
        }
        
        File directory;
        if(StringUtils.isEmpty(getDirectory())) {
            directory = new File(FileServer.getDefaultBase());
            if(log.isDebugEnabled()) {
                log.debug("Using default directory:"+directory.getAbsolutePath());
            }
        } else {
            directory = new File(getDirectory());
            if(log.isDebugEnabled()) {
                log.debug("Using configured directory:"+directory.getAbsolutePath());
            }
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Will run : "+cmdLine + " using working directory:"+directory.getAbsolutePath()+
                    " with environment: "+env);
        }

        results.setSamplerData("Working Directory: "+directory.getAbsolutePath()+
                "\nEnvironment: "+env+
                "\nExecuting: " + cmdLine.toString());

        SystemCommand nativeCommand = null;
        try {
            nativeCommand = new SystemCommand(directory, getTimeout(), POLL_INTERVAL, env, getStdin(), getStdout(), getStderr());
            results.sampleStart();
            int returnCode = nativeCommand.run(cmds);
            results.sampleEnd();
            results.setResponseCode(Integer.toString(returnCode));
            if(log.isDebugEnabled()) {
                log.debug("Ran : "+cmdLine + " using working directory: "+directory.getAbsolutePath()+
                        " with execution environment: "+nativeCommand.getExecutionEnvironment()+ " => " + returnCode);
            }

            if (checkReturnCode && (returnCode != expectedReturnCode)) {
                results.setSuccessful(false);
                results.setResponseMessage("Unexpected return code.  Expected ["+expectedReturnCode+"]. Actual ["+returnCode+"].");
            } else {
                results.setSuccessful(true);
                results.setResponseMessage("OK");
            }
        } catch (IOException ioe) {
            results.sampleEnd();
            results.setSuccessful(false);
            results.setResponseCode("500"); //$NON-NLS-1$
            results.setResponseMessage("Exception occurred whilst executing system call: " + ioe);
        } catch (InterruptedException ie) {
            results.sampleEnd();
            results.setSuccessful(false);
            results.setResponseCode("500"); //$NON-NLS-1$
            results.setResponseMessage("System Sampler interrupted whilst executing system call: " + ie);
            Thread.currentThread().interrupt();
        }

        if (nativeCommand != null) {
            results.setResponseData(nativeCommand.getOutResult().getBytes()); // default charset is deliberate here
        }
            
        return results;
    }
    
    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    /**
     * @return working directory to use for system commands
     */
    public String getDirectory() {
        return getPropertyAsString(DIRECTORY, FileServer.getDefaultBase());
    }

    /**
     * Set the working directory to use for system commands
     * 
     * @param directory
     *            working directory to use for system commands
     */
    public void setDirectory(String directory) {
        setProperty(DIRECTORY, directory, FileServer.getDefaultBase());
    }

    /**
     * Sets the Command attribute of the JavaConfig object
     * 
     * @param command
     *            the new Command value
     */
    public void setCommand(String command) {
        setProperty(COMMAND, command);
    }

    /**
     * Gets the Command attribute of the JavaConfig object
     * 
     * @return the Command value
     */
    public String getCommand() {
        return getPropertyAsString(COMMAND);
    }
    
    /**
     * Set the arguments (parameters) for the JavaSamplerClient to be executed
     * with.
     * 
     * @param args
     *            the new arguments. These replace any existing arguments.
     */
    public void setArguments(Arguments args) {
        setProperty(new TestElementProperty(ARGUMENTS, args));
    }

    /**
     * Get the arguments (parameters) for the JavaSamplerClient to be executed
     * with.
     * 
     * @return the arguments
     */
    public Arguments getArguments() {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }
    
    /**
     * @param checkit boolean indicates if we check or not return code
     */
    public void setCheckReturnCode(boolean checkit) {
        setProperty(CHECK_RETURN_CODE, checkit);
    }
    
    /**
     * @return boolean indicating if we check or not return code
     */
    public boolean getCheckReturnCode() {
        return getPropertyAsBoolean(CHECK_RETURN_CODE);
    }
    
    /**
     * @param code expected return code
     */
    public void setExpectedReturnCode(int code) {
        setProperty(EXPECTED_RETURN_CODE, Integer.toString(code));
    }
    
    /**
     * @return expected return code
     */
    public int getExpectedReturnCode() {
        return getPropertyAsInt(EXPECTED_RETURN_CODE);
    }

    /**
     * @param arguments Env vars
     */
    public void setEnvironmentVariables(Arguments arguments) {
        setProperty(new TestElementProperty(ENVIRONMENT, arguments));
    }
    
    /**
     * Get the env variables
     * 
     * @return the arguments
     */
    public Arguments getEnvironmentVariables() {
        return (Arguments) getProperty(ENVIRONMENT).getObjectValue();
    }

    public String getStdout() {
        return getPropertyAsString(STDOUT, "");
    }

    public void setStdout(String filename) {
        setProperty(STDOUT, filename, "");
    }

    public String getStderr() {
        return getPropertyAsString(STDERR, "");
    }

    public void setStderr(String filename) {
        setProperty(STDERR, filename, "");
    }

    public String getStdin() {
        return getPropertyAsString(STDIN, "");
    }

    public void setStdin(String filename) {
        setProperty(STDIN, filename, "");
    }

    public long getTimeout() {
        return getPropertyAsLong(TIMEOUT, 0L);
    }

    public void setTimout(long timeoutMs) {
        setProperty(TIMEOUT, timeoutMs, 0L);
    }
}
