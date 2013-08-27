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
package org.apache.jmeter.report.writers;

import java.io.File;
import java.util.Calendar;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;

/**
 * The abstract report writer provides the common implementation for subclasses
 * to reuse.
 */
public abstract class AbstractReportWriter extends AbstractTestElement implements ReportWriter {

    private static final long serialVersionUID = 240L;
    public static final String TARGET_DIRECTORY = "ReportWriter.target.directory";

    /**
     *
     */
    public AbstractReportWriter() {
        super();
    }

    /**
     * Subclasses need to implement this method and provide the necessary
     * logic to produce a ReportSummary object and write the report
     */
    @Override
    public abstract ReportSummary writeReport(TestElement element);

    /**
     * The method simply returns the target directory and doesn't
     * validate it. the abstract class expects some other class will
     * validate the target directory.
     */
    @Override
    public String getTargetDirectory() {
        return getPropertyAsString(TARGET_DIRECTORY);
    }

    /**
     * Set the target directory where the report should be saved
     */
    @Override
    public void setTargetDirectory(String directory) {
        setProperty(TARGET_DIRECTORY,directory);
    }

    public void makeDirectory() {
        File output = new File(getTargetDirectory());
        // mkdir() returns false if the directory was not created; could be because it exists
        if (!output.mkdir() && !output.isDirectory()) {
            throw new IllegalStateException("Could not create directory:"+output.getAbsolutePath());
        }
    }

    /**
     * if the target output directory already exists, archive it
     */
    public void archiveDirectory() {
        File output = new File(getTargetDirectory());
        if (output.exists() && output.isDirectory()) {
            // if the directory already exists and is a directory,
            // we just renamed to "archive.date"
            if(!output.renameTo(new File("archive." + getDayString()))) {
                throw new IllegalStateException("Could not rename directory:"+output.getAbsolutePath()+
                        " to archive." + getDayString());
            }
        }
    }

    /**
     * return the day in YYYYMMDD format
     * @return the date
     */
    public String getDayString() {
        Calendar today = Calendar.getInstance();
        String year = String.valueOf(today.get(Calendar.YEAR));
        String month = String.valueOf(today.get(Calendar.MONTH));
        String day = String.valueOf(today.get(Calendar.DATE));
        return year + month + day;
    }
}
