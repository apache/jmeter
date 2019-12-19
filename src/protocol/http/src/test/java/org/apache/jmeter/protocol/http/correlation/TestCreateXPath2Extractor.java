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

package org.apache.jmeter.protocol.http.correlation;

import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestCreateXPath2Extractor {

    @Test
    public void testCreateXPath2ExtractorThrowsException() throws TransformerException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CreateXPath2Extractor.createXPath2Extractor(null, null, "_csrf", "2 /login", "application/xml");
        });
    }

    @Test
    public void testCheckIfXSLTransformResourceExists() {
        InputStream in = CreateXPath2Extractor.class.getResourceAsStream("CreateXPath2ExtractorXSLTransform.xml");
        Assertions.assertNotEquals(null, in);
    }

}
