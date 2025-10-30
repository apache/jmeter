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

package org.apache.jmeter.protocol.http.sampler;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.jupiter.api.Test;

public class TestSerializedHTTPSampler extends JMeterTestCase implements JMeterSerialTest {

    @Test
    public void checkThatFilesAreReadRelativeToBaseDir() {
        String baseDirPath = FileServer.getFileServer().getBaseDir();
        File baseDir = new File(baseDirPath);
        try {
            File file = new File(getResourceFilePath("checkThatFilesAreReadRelativeToBaseDir.txt"));
            FileServer.getFileServer().setBase(file.getParentFile());
            HTTPSamplerBase sampler = new HTTPSampler3();
            sampler.setMethod("POST");
            sampler.setPath("https://httpbin.org/post");
            sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg(file.getName(), "", "")});

            SampleResult sample = sampler.sample();
            assertFalse(sample.getResponseDataAsString().contains("java.io.FileNotFoundException:"),
                    () -> sample.getResponseDataAsString() + " should not contain java.io.FileNotFoundException:");
        } finally {
            FileServer.getFileServer().setBase(baseDir);
        }
    }

}
