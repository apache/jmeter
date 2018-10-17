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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.util.JOrphanUtils;

/**
 * Utility class for invoking native system applications
 */
public class SystemCommand {
    public static final int POLL_INTERVAL = 100;
    private final File directory;
    private final Map<String, String> env;
    private Map<String, String> executionEnvironment;
    private final InputStream stdin;
    private final OutputStream stdout;
    private final boolean stdoutWasNull;
    private final OutputStream stderr;
    private final long timeoutMillis;
    private final int pollInterval;

    /**
     * @param env Environment variables appended to environment (may be null)
     * @param directory File working directory (may be null)
     */
    public SystemCommand(File directory, Map<String, String> env) {
        this(directory, 0L, POLL_INTERVAL, env, (InputStream) null, (OutputStream) null, (OutputStream) null);
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
     * @throws IOException if the input file is not found or output cannot be written
     */
    public SystemCommand(File directory, long timeoutMillis, int pollInterval, Map<String, String> env,
            String stdin, String stdout, String stderr) throws IOException {
        this(directory, timeoutMillis, pollInterval, env, checkIn(stdin), checkOut(stdout), checkOut(stderr));
    }

    private static InputStream checkIn(String stdin) throws FileNotFoundException {
        String in = JOrphanUtils.nullifyIfEmptyTrimmed(stdin);
        if (in == null) {
            return null;
        } else {
            return new FileInputStream(in);
        }
    }

    private static OutputStream checkOut(String path) throws IOException {
        String in = JOrphanUtils.nullifyIfEmptyTrimmed(path);
        if (in == null) {
            return null;
        } else {
            return new FileOutputStream(path);
        }
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
    public SystemCommand(File directory, long timeoutMillis, int pollInterval, Map<String, String> env,
            InputStream stdin, OutputStream stdout, OutputStream stderr) {
        super();
        this.timeoutMillis = timeoutMillis;
        this.directory = directory;
        this.env = env;
        this.pollInterval = pollInterval;
        this.stdin = stdin;
        this.stdoutWasNull = stdout == null;
        if (stdout == null) {
            this.stdout = new ByteArrayOutputStream(); // capture the output
        } else {
            this.stdout = stdout;
        }
        this.stderr = stderr;
    }

    /**
     * @param arguments List of strings, not null
     * @return return code
     * @throws InterruptedException when execution was interrupted
     * @throws IOException when I/O error occurs while execution
     */
    public int run(List<String> arguments) throws InterruptedException, IOException {
        return run(arguments, stdin, stdout, stderr);
    }

    // helper method to allow input and output to be changed for chaining
    private int run(List<String> arguments, InputStream in,
            OutputStream out,OutputStream err) throws InterruptedException, IOException {
        Process proc = null;
        final ProcessBuilder procBuild = new ProcessBuilder(arguments);
        if (env != null) {
            procBuild.environment().putAll(env);
        }
        this.executionEnvironment = Collections.unmodifiableMap(procBuild.environment());
        procBuild.directory(directory);
        if (err == null) {
            procBuild.redirectErrorStream(true);
        }
        try
        {
            proc = procBuild.start();

            final OutputStream procOut = proc.getOutputStream();
            final InputStream procErr = proc.getErrorStream();
            final InputStream procIn = proc.getInputStream();

            final StreamCopier swerr;
            if (err != null){
                swerr = new StreamCopier(procErr, err);
                swerr.start();
            } else {
                swerr = null;
            }

            final StreamCopier swout = new StreamCopier(procIn, out);
            swout.start();
            
            final StreamCopier swin;
            if (in != null) {
                swin = new StreamCopier(in, procOut);
                swin.start();
            } else {
                swin = null;
                procOut.close(); // ensure the application does not hang if it requests input
            }
            int exitVal = waitForEndWithTimeout(proc, timeoutMillis);

            swout.join();
            if (swerr != null) {
                swerr.join();
            }
            if (swin != null) {
                swin.interrupt(); // the copying thread won't generally detect EOF
                swin.join();
            }
            procErr.close();
            procIn.close();
            procOut.close();
            return exitVal;
        } finally {
            if(proc != null) {
                try {
                    proc.destroy();
                } catch (Exception ignored) {
                    // Ignored
                }
            }
        }
    }

    /**
     * Pipe the output of one command into another
     * 
     * @param arguments1 first command to run
     * @param arguments2 second command to run
     * @return exit status
     * @throws InterruptedException when execution gets interrupted
     * @throws IOException when I/O error occurs while execution
     */
    public int run(List<String> arguments1, List<String> arguments2) throws InterruptedException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); // capture the intermediate output
        int exitCode=run(arguments1,stdin,out, stderr);
        if (exitCode == 0) {
            exitCode = run(arguments2,new ByteArrayInputStream(out.toByteArray()),stdout,stderr);
        }
        return exitCode;
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
        if (stdoutWasNull) { // we are capturing output
            return stdout.toString(); // Default charset is probably appropriate here.
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
