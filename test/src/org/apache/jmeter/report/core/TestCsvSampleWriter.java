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
package org.apache.jmeter.report.core;

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.apache.jmeter.util.JMeterUtils;

public class TestCsvSampleWriter extends TestCase {

    @Override
    protected void setUp() throws Exception {
        // We have to initialize JMeterUtils
        JMeterUtils.loadJMeterProperties("jmeter.properties");
    };

    SampleMetadata metadata = new SampleMetadata(',', "a", "b");

    public void testCsvSampleWriterConstructorWithNull() throws Exception {
        try {
            CsvSampleWriter dummy = new CsvSampleWriter(null);
            dummy.close(); // We should never get here, but it would be a
                           // writer, so close it
            fail("NPE expected");
        } catch (NullPointerException e) {
            // OK, we should land here
        }
    }

    public void testCsvSampleWriterConstructorWithWriter() throws Exception {
        try (Writer writer = new StringWriter();
                CsvSampleWriter csvWriter = new CsvSampleWriter(writer,
                        metadata)) {
            csvWriter.writeHeader();
            csvWriter.flush();
            assertEquals("a,b\n", writer.toString());
        }
    }

    public void testWriteWithoutWriter() throws Exception {
        try (CsvSampleWriter csvWriter = new CsvSampleWriter(metadata)) {
            Sample sample = new SampleBuilder(metadata).add("a1").add("b1")
                    .build();
            try {
                csvWriter.write(sample);
                fail("ISE expected");
            } catch (IllegalStateException e) {
                // OK, we should land here
            }
        }
    }

    public void testWriteWithoutSample() throws Exception {
        try (Writer writer = new StringWriter();
                CsvSampleWriter csvWriter = new CsvSampleWriter(writer,
                        metadata)) {
            try {
                csvWriter.write(null);
                fail("NPE expected");
            } catch (NullPointerException e) {
                assertEquals("sample must not be null", e.getMessage());
            }
        }
    }

    public void testWrite() throws Exception {
        try (Writer writer = new StringWriter();
                CsvSampleWriter csvWriter = new CsvSampleWriter(writer,
                        metadata)) {
            Sample sample = new SampleBuilder(metadata).add("a1").add("b1")
                    .build();
            csvWriter.write(sample);
            csvWriter.flush();
            assertEquals("a1,b1\n", writer.toString());
        }
    }

}
