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

package org.apache.jorphan.exec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.util.JOrphanUtils;

/**
 * Utility class for invoking native system applications
 */
public class SystemCommand {
    protected static final int POLL_INTERVAL = 100;
    private StreamGobbler outputGobbler;
    private final File directory;
    private final Map<String, String> env;
    private Map<String, String> executionEnvironment;
    private final String stdin;
    private final String stdout;
    private final String stderr;
    private final long timeoutMillis;
    private final int pollInterval;

    /**
     * @param env Environment variables appended to environment (may be null)
     * @param directory File working directory (may be null)
     */
    public SystemCommand(File directory, Map<String, String> env) {
        this(directory, 0L, POLL_INTERVAL, env, null, null, null);
    }

    /**
     * 
     * @param env Environment variables appended to environment (may be null)
     * @param directory File working directory (may be null)
     * @param timeoutMillis timeout in Milliseconds
     * @param pollInterval Value used to poll for Process execution end
     * @param stdin File name that will contain data to be input to process (may be null)
     * @param stdout File name that will contain out stream (may be null)
     * @param stderr File name that will contain err stream (may be null)
     */
    public SystemCommand(File directory, long timeoutMillis, int pollInterval, Map<String, String> env, String stdin, String stdout, String stderr) {
        super();
        this.timeoutMillis = timeoutMillis;
        this.directory = directory;
        this.env = env;
        this.pollInterval = pollInterval;
        this.stdin = JOrphanUtils.nullifyIfEmptyTrimmed(stdin);
        this.stdout = JOrphanUtils.nullifyIfEmptyTrimmed(stdout);
        this.stderr = JOrphanUtils.nullifyIfEmptyTrimmed(stderr);
    }

    /**
     * @param arguments List<String>, not null
     * @return return code
     * @throws InterruptedException
     * @throws IOException
     */
    public int run(List<String> arguments) throws InterruptedException, IOException {
        Process proc = null;
        try
        {
            ProcessBuilder procBuild = new ProcessBuilder(arguments);
            if (env != null) {
                procBuild.environment().putAll(env);
            }
            this.executionEnvironment = Collections.unmodifiableMap(procBuild.environment());
            procBuild.directory(directory);
            if (stderr == null || stderr.equals(stdout)) { // we're not redirecting stderr separately
                procBuild.redirectErrorStream(true);
            }
            proc = procBuild.start();
            StreamCopier swerr = null;
            if (!procBuild.redirectErrorStream()) { // stderr has separate output file
                swerr = new StreamCopier(proc.getErrorStream(), new FileOutputStream(stderr));
                swerr.start();
            }
            
            StreamCopier swout = null;
            if (stdout != null) {
                swout = new StreamCopier(proc.getInputStream(), new FileOutputStream(stdout));
                swout.start();
            } else {
                outputGobbler = new StreamGobbler(proc.getInputStream());
                outputGobbler.start();
            }
            
            StreamCopier swin = null;
            if (stdin != null) {
                swin = new StreamCopier(new FileInputStream(stdin), proc.getOutputStream());
                swin.start();
            } else {
                proc.getOutputStream().close(); // ensure the application does not hang if it requests input
            }
            int exitVal = waitForEndWithTimeout(proc, timeoutMillis);

            if (outputGobbler != null) {
                outputGobbler.join();
            }
            if (swout != null) {
                swout.join();
            }
            if (swerr != null) {
                swerr.join();
            }
            if (swin != null) {
                swin.interrupt(); // the copying thread won't generally detect EOF
                swin.join();
            }
            return exitVal;
        }
        finally
        {
            if(proc != null)
            {
                try {
                    proc.destroy();
                } catch (Exception ignored) {
                    // Ignored
                }
            }
        }
    }

    /**
     * Wait for end of proc execution or timeout if timeoutInMillis is greater than 0
     * @param proc Process
     * @param timeoutInMillis long timeout in ms
     * @return proc exit value
     * @throws InterruptedException
     */
    private int waitForEndWithTimeout(Process proc, long timeoutInMillis) throws InterruptedException {
        if (timeoutInMillis <= 0L) {
            return proc.waitFor();
        } else {
            long now = System.currentTimeMillis();
            long finish = now + timeoutInMillis;
            while(System.currentTimeMillis() < finish) {
                try {
                    return proc.exitValue();
                } catch (IllegalThreadStateException e) { // not yet terminated
                    Thread.sleep(pollInterval);
                }
            }
            try {
                return proc.exitValue();
            } catch (IllegalThreadStateException e) { // not yet terminated
                // N.B. proc.destroy() is called by the finally clause in the run() method
                throw new InterruptedException( "Process timeout out after " + timeoutInMillis + " milliseconds" );
            }
        }
    }

    /**
     * @return Out/Err stream contents
     */
    public String getOutResult() {
        if(outputGobbler != null) {    
            return outputGobbler.getResult();
        } else {
            return "";
        }
    }

    /**
     * @return the executionEnvironment
     */
    public Map<String, String> getExecutionEnvironment() {
        return executionEnvironment;
    }
}
