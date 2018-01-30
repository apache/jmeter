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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CsvSampleReaderTest extends JMeterTestCase {

    private static final int NR_ROWS = 100;
    private File tempCsv;
    private SampleMetadata metadata = new SampleMetadata(',', "a","b");

    @Before
    public void setUp() throws IOException {
        tempCsv = File.createTempFile("samplecsv", ".csv");
        tempCsv.deleteOnExit();
        try (CsvSampleWriter writer = new CsvSampleWriter(tempCsv, metadata)) {
            writer.setSeparator(',');
            for (long i = 0; i < NR_ROWS; i++) {
                final Sample sample = new SampleBuilder(metadata)
                        .add(i)
                        .add("a" + i)
                        .build();
                writer.write(sample);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidFile() throws Exception {
        try (CsvSampleReader csv = new CsvSampleReader(
                new File("/not/available.csv"), metadata)) {
            Assert.fail("File should not be readable and therefor illegal");
        }
    }

    @Test
    public void testGetMetadata() {
        try (CsvSampleReader reader = new CsvSampleReader(tempCsv, metadata)) {
            Assert.assertThat(reader.getMetadata().toString(),
                    CoreMatchers.is(metadata.toString()));
        }
    }

    @Test
    public void testReadSample() {
        try (CsvSampleReader reader = new CsvSampleReader(tempCsv, metadata)) {
            for (long i = 0; i < NR_ROWS; i++) {
                Sample expected = new SampleBuilder(metadata).add(i)
                        .add("a" + i).build();
                Assert.assertThat(reader.readSample().toString(),
                        CoreMatchers.is(expected.toString()));
            }
        }
    }

    @Test
    public void testPeek() {
        try (CsvSampleReader reader = new CsvSampleReader(tempCsv, metadata)) {
            for (long i = 0; i < NR_ROWS; i++) {
                Sample expected = new SampleBuilder(metadata).add(i)
                        .add("a" + i).build();
                Assert.assertThat(reader.peek().toString(),
                        CoreMatchers.is(expected.toString()));
                reader.readSample();
            }
        }
    }

    @Test
    public void testHasNextAndReadSample() {
        try (CsvSampleReader reader = new CsvSampleReader(tempCsv, metadata)) {
            for (long i = 0; i < NR_ROWS; i++) {
                Assert.assertTrue(reader.hasNext());
                final Sample sample = reader.readSample();
                Assert.assertEquals(i, sample.getSampleRow());
            }
            Assert.assertFalse(reader.hasNext());
        }
    }

    @Test
    public void testClose() {
        CsvSampleReader reader = new CsvSampleReader(tempCsv, metadata);
        reader.close();
        try {
            reader.readSample();
            fail("Stream should be closed.");
        } catch (SampleException expected) {
            // All is well
        }
    }

}
