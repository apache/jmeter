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
import java.util.Map;

/**
 * Native Command
 * @deprecated (2.10) use {@link org.apache.jorphan.exec.SystemCommand} instead
 */
@Deprecated
public class NativeCommand extends  org.apache.jorphan.exec.SystemCommand {

    /**
     * @param env Environment variables appended to environment
     * @param directory File working directory
     */
    public NativeCommand(File directory, Map<String, String> env) {
        super(directory, env);
    }

    /**
     * 
     * @param env Environment variables appended to environment
     * @param directory File working directory
     * @param stdin File name that will contain data to be input to process
     * @param stdout File name that will contain out stream
     * @param stderr File name that will contain err stream
     * @throws IOException if any of the files are not accessible
     */
    public NativeCommand(File directory, Map<String, String> env, String stdin, String stdout, String stderr) throws IOException {
        super(directory, 0L, POLL_INTERVAL, env, stdin, stdout, stderr);
    }

}
