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
package org.apache.jmeter.report.processor;

import java.io.File;
import java.util.HashMap;

/**
 * Defines the context in which {@link SampleConsumer}, {@link SampleProducer}
 * will operate
 * 
 * @since 2.14
 */
public class SampleContext {

    private File workingDirectory;
    private HashMap<String, Object> data = new HashMap<String, Object>();

    /**
     * Return the root directory that consumers are authorized to use for
     * intermediate work.<br>
     * SampleConsumers are encourage to create their own work directories
     * beneath this root work directory
     * 
     * @return A file pointing to an existing directory
     */
    public final File getWorkingDirectory() {
	return workingDirectory;
    }

    /**
     * Sets the work directory.
     *
     * @param workingDirectory
     *            the new working directory
     */
    public final void setWorkingDirectory(File workingDirectory) {
//	if (workingDirectory == null)
//	    throw new ArgumentNullException("workingDirectory");

	this.workingDirectory = workingDirectory;
	// workingDirectory.mkdirs();
    }

    /**
     * Gets the data matching the specified key.
     *
     * @param key
     *            the key
     * @return the data
     */
    public final Object getData(String key) {
	return data.get(key);
    }

    /**
     * Sets the data with the specified key.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     */
    public final void setData(String key, Object value) {
	data.put(key, value);
    }
}
