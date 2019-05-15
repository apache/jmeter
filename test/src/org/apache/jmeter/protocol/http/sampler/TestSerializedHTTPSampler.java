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

package org.apache.jmeter.protocol.http.sampler;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Paths;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.Test;

public class TestSerializedHTTPSampler extends JMeterTestCase implements JMeterSerialTest {

    @Test
    public void checkThatFilesAreReadRelativeToBaseDir() {
        String baseDirPath = FileServer.getFileServer().getBaseDir();
        File baseDir = new File(baseDirPath);
        try {
            FileServer.getFileServer().setBase(Paths.get(JMeterUtils.getJMeterHome(), "test", "resources").toFile());
            HTTPSamplerBase sampler = new HTTPSampler3();
            sampler.setMethod("POST");
            sampler.setPath("https://httpbin.org/post");
            sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("resourcefile.txt", "", "")});

            SampleResult sample = sampler.sample();
            assertThat(sample.getResponseDataAsString(), not(containsString("java.io.FileNotFoundException:")));
        } finally {
            FileServer.getFileServer().setBase(baseDir);
        }
    }

}
