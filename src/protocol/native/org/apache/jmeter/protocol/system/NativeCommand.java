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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Native Command 
 */
public class NativeCommand {

	private StreamGobbler outputGobbler;
    private final File directory;
    private final Map<String, String> env;
    private Map<String, String> executionEnvironment;

	/**
	 * @param env Environment variables appended to environment
	 * @param directory File working directory
	 */
	public NativeCommand(File directory, Map<String, String> env) {
		super();
		this.directory = directory;
		this.env = env;
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
            procBuild.redirectErrorStream(true);
            proc = procBuild.start();
            this.outputGobbler = new 
                     StreamGobbler(proc.getInputStream());
            outputGobbler.start();
	                            
			int exitVal = proc.waitFor();

			outputGobbler.join();
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
