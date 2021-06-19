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

import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.protocol.http.correlation.extractordata.BoundaryExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.ExtractorData;
import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateBoundaryExtractor implements CreateExtractorInterface{

    private static final Logger log = LoggerFactory.getLogger(CreateBoundaryExtractor.class);

    CreateBoundaryExtractor() {}

    private static final String ONE = "1"; //$NON-NLS-1$


    /**
     * Create Boundary Extractor
     * @param extractorCreatorData ExtractorCreatorData object
     * @return ExtractorData object.
     */
    @Override
    public ExtractorData createExtractor(ExtractorCreatorData extractorCreatorData) {
        log.debug("Create ExtractorData data from ExtractorCreatorData "+ extractorCreatorData);
        String value = extractorCreatorData.getParameterValue();
        String responseData = extractorCreatorData.getSampleResult().getResponseDataAsString();
        int startIndex = responseData.indexOf(value);
        int endIndex = responseData.indexOf(value) + value.length();
        BoundaryExtractorData boundaryExtractor = null;
        if (startIndex == -1 || endIndex == -1 || responseData.equals(value)) {
            // return null object
            return boundaryExtractor;
        }
        String lBoundary = responseData.substring(startIndex < 4 ? 0 : startIndex - 4, startIndex);
        String rBoundary = responseData.substring(endIndex,
                responseData.length() - endIndex < 4 ? responseData.length() : endIndex + 4);
        boundaryExtractor = new BoundaryExtractorData(extractorCreatorData.getParameter(), lBoundary, rBoundary, ONE,
                extractorCreatorData.getSampleResult().getSampleLabel());
        return boundaryExtractor;
    }


    /**
     * Create Boundary extractor TestElement
     *
     * @param extractorData   ExtractorData object.
     * @param testElement empty testElement object
     * @return Boundary extractor TestElement
     */
    @Override
    public  TestElement createExtractorTestElement(ExtractorData extractorData, TestElement testElement) {
        BoundaryExtractor boundaryExtractor = (BoundaryExtractor) testElement;
        BoundaryExtractorData extractor = (BoundaryExtractorData) extractorData;
        boundaryExtractor.setName(extractor.getRefname());
        boundaryExtractor.setRefName(extractor.getRefname());
        boundaryExtractor.setLeftBoundary(extractor.getlBoundary());
        boundaryExtractor.setRightBoundary(extractor.getrBoundary());
        boundaryExtractor.setMatchNumber(extractor.getMatchNumber());
        return boundaryExtractor;
    }

}
