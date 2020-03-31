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

package org.apache.jmeter.protocol.http.correlation;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.TransformerException;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestCreateXPath2Extractor {

    CreateExtractorInterface createExtractorInterface = null;
    ExtractorCreatorData extractorCreatorData = null;
    SampleResult sampleResult;

    @BeforeEach
    public void setup() {
        sampleResult = new SampleResult();
        sampleResult.setSampleLabel("2 /login");
        sampleResult.setResponseHeaders("HTTP/1.1 200 OK");
        sampleResult.setResponseData(
                "<?xml version=\"1.0\" encoding=\"UTF8\"?>"
                        + "<token><_csrf>7d1de48134af4342a9b4b8288c451f7c</_csrf></token>",
                StandardCharsets.UTF_8.name());
        sampleResult.setContentType("text/html;charset=UTF8");
        createExtractorInterface = new CreateXPath2Extractor();
        extractorCreatorData = new ExtractorCreatorData();
        extractorCreatorData.setContentType("application/xml");
        extractorCreatorData.setParameter("_csrf");
        extractorCreatorData.setParameterValue(null);
        SampleResult sampleResult = new SampleResult();
        extractorCreatorData.setSampleResult(sampleResult);

    }

    @Test
    public void testCreateXPath2ExtractorThrowsException() throws TransformerException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            createExtractorInterface.createExtractor(extractorCreatorData);
        });
    }

    @Test
    public void testCheckIfXSLTransformResourceExists() {
        InputStream in = CreateXPath2Extractor.class.getResourceAsStream("CreateXPath2ExtractorXSLTransform.xml");
        Assertions.assertNotEquals(null, in);
    }

}
