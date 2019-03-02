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
package org.apache.jmeter.report.dashboard;

import java.io.File;

import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.report.processor.SampleContext;

/**
 * The Interface DataExporter represents an engine to export data from samples
 * consumption.
 *
 * @since 3.0
 */
public interface DataExporter {

    /**
     * Gets the name of the exporter.
     *
     * @return the name of the exporter
     */
    String getName();

    /**
     * Sets the name of the exporter.
     *
     * @param name
     *            the new name of the exporter
     */
    void setName(String name);

    /**
     * Export data from the specified context using the given configuration.
     *
     * @param context
     *            the context (must not be {@code null})
     * @param file
     *            the file which from samples come (must not be {@code null})
     * @param configuration
     *            the configuration (must not be {@code null})
     * @throws ExportException
     *             the export exception
     */
    void export(SampleContext context, File file,
            ReportGeneratorConfiguration configuration) throws ExportException;
}
