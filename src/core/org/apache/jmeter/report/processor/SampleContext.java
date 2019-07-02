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
import java.util.Map;

/**
 * Defines the context in which {@link SampleConsumer}, {@link SampleProducer}
 * will operate
 *
 * @since 3.0
 */
public class SampleContext {

    private File workingDirectory;
    private Map<String, Object> data = new HashMap<>();

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
        this.workingDirectory = workingDirectory;
    }

    /**
     * Gets the data storage.
     *
     * @return the data
     */
    public final Map<String, Object> getData() {
        return data;
    }

}
