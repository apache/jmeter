/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.report.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringWriter;
import java.io.Writer;

import org.apache.jmeter.junit.JMeterTestUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestCsvSampleWriter {

    private static final String LINE_SEP = System.getProperty("line.separator"); // $NON-NLS-1$

    private SampleMetadata metadata = new SampleMetadata(',', "a", "b");

    @BeforeEach
    public void setUp() throws Exception {
        // We have to initialize JMeterUtils
        JMeterTestUtils.setupJMeterHome();
        JMeterUtils.loadJMeterProperties(JMeterUtils.getJMeterBinDir() + "/jmeter.properties");
    }

    @Test
    public void testCsvSampleWriterConstructorWithNull() throws Exception {
        Assertions.assertThrows(
                NullPointerException.class,
                () -> new CsvSampleWriter(null)
        );
    }

    @Test
    public void testCsvSampleWriterConstructorWithWriter() throws Exception {
        try (Writer writer = new StringWriter();
             CsvSampleWriter csvWriter = new CsvSampleWriter(writer, metadata)) {
            csvWriter.writeHeader();
            csvWriter.flush();
            assertEquals("a,b" + LINE_SEP, writer.toString());
        }
    }

    @Test
    public void testWriteWithoutWriter() throws Exception {
        try (CsvSampleWriter csvWriter = new CsvSampleWriter(metadata)) {
            Sample sample = new SampleBuilder(metadata).add("a1").add("b1").build();
            try {
                csvWriter.write(sample);
                fail("ISE expected");
            } catch (IllegalStateException e) {
                // OK, we should land here
            }
        }
    }

    @Test
    public void testWriteWithoutSample() throws Exception {
        try (Writer writer = new StringWriter();
             CsvSampleWriter csvWriter = new CsvSampleWriter(writer, metadata)) {
            try {
                csvWriter.write(null);
                fail("NPE expected");
            } catch (NullPointerException e) {
                // OK. Expected to land here
            }
        }
    }

    @Test
    public void testWrite() throws Exception {
        try (Writer writer = new StringWriter();
             CsvSampleWriter csvWriter = new CsvSampleWriter(writer, metadata)) {
            Sample sample = new SampleBuilder(metadata).add("a1").add("b1").build();
            csvWriter.write(sample);
            csvWriter.flush();
            assertEquals("a1,b1" + LINE_SEP, writer.toString());
        }
    }

}
