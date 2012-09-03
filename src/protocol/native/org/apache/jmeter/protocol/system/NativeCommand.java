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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.util.JOrphanUtils;

/**
 * Native Command 
 */
public class NativeCommand {

	private StreamGobbler outputGobbler;
    private final File directory;
    private final Map<String, String> env;
    private Map<String, String> executionEnvironment;
    private final String stdin;
    private final String stdout;
    private final String stderr;

	/**
	 * @param env Environment variables appended to environment
	 * @param directory File working directory
	 */
	public NativeCommand(File directory, Map<String, String> env) {
	    this(directory, env, null, null, null);
	}

	/**
	 * 
     * @param env Environment variables appended to environment
     * @param directory File working directory
	 * @param stdin File name that will contain data to be input to process
	 * @param stdout File name that will contain out stream
	 * @param stderr File name that will contain err stream
	 */
    public NativeCommand(File directory, Map<String, String> env, String stdin, String stdout, String stderr) {
        super();
        this.directory = directory;
        this.env = env;
        this.stdin = JOrphanUtils.nullifyIfEmptyTrimmed(stdin);
        this.stdout = JOrphanUtils.nullifyIfEmptyTrimmed(stdout);
        this.stderr = JOrphanUtils.nullifyIfEmptyTrimmed(stderr);
    }

	/**
	 * @param arguments List<String>
	 * @return return code
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public int run(List<String> arguments) throws InterruptedException, IOException {
		Process proc = null;
		try
		{
		    ProcessBuilder procBuild = new ProcessBuilder(arguments);
		    procBuild.environment().putAll(env);
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
	        }
			int exitVal = proc.waitFor();

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