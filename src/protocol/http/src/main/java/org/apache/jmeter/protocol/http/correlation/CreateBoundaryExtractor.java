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

package org.apache.jmeter.extractor;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.testelement.TestElement;

public class CreateBoundaryExtractor {

    private CreateBoundaryExtractor() {}

    public static final String BOUNDARY_EXTRACTOR_VARIABLE_NAME = "BoundaryExtractor.refname"; //$NON-NLS-1$
    public static final String BOUNDARY_EXTRACTOR_LBOUNDARY = "BoundaryExtractor.lboundary"; //$NON-NLS-1$
    public static final String BOUNDARY_EXTRACTOR_RBOUNDARY = "BoundaryExtractor.rboundary"; //$NON-NLS-1$
    public static final String BOUNDARY_EXTRACTOR_MATCH_NO = "BoundaryExtractor.match_number"; //$NON-NLS-1$
    public static final String BOUNDARY_EXTRACTOR_TEST_NAME = "testname"; //$NON-NLS-1$

    private static final String ONE = "1"; //$NON-NLS-1$

    /**
     * Create Boundary Extractor
     *
     * @param responseData as string
     * @param value        Value of the parameter required to correlate
     * @param parameter    parameter alias for correlation
     * @param testname     TestName of the request whose response yields the
     *                     parameter required to correlate
     * @return Boundary Extractor values in a map
     */
    public static Map<String, String> createBoundaryExtractor(String responseData, String value, String parameter,
            String testname) {
        int startIndex = responseData.indexOf(value);
        int endIndex = responseData.indexOf(value) + value.length();
        if (startIndex == -1 || endIndex == -1 || responseData.equals(value)) {
            // return empty map
            return new HashMap<>();
        }
        String lBoundary = responseData.substring(startIndex < 4 ? 0 : startIndex - 4, startIndex);
        String rBoundary = responseData.substring(endIndex,
                responseData.length() - endIndex < 4 ? responseData.length() : endIndex + 4);
        Map<String, String> boundaryExtractor = new HashMap<>();
        boundaryExtractor.put(BOUNDARY_EXTRACTOR_VARIABLE_NAME, parameter);
        boundaryExtractor.put(BOUNDARY_EXTRACTOR_LBOUNDARY, lBoundary);
        boundaryExtractor.put(BOUNDARY_EXTRACTOR_RBOUNDARY, rBoundary);
        boundaryExtractor.put(BOUNDARY_EXTRACTOR_MATCH_NO, ONE);
        boundaryExtractor.put(BOUNDARY_EXTRACTOR_TEST_NAME, testname);
        return boundaryExtractor;
    }

    /**
     * Create Boundary extractor TestElement
     *
     * @param extractor   Map containing extractor data
     * @param testElement empty testElement object
     * @return Boundary extractor TestElement
     */
    public static TestElement createBoundaryExtractorTestElement(Map<String, String> extractor,
            TestElement testElement) {
        BoundaryExtractor boundaryExtractor = (BoundaryExtractor) testElement;
        boundaryExtractor.setName(extractor.get(BOUNDARY_EXTRACTOR_VARIABLE_NAME));
        boundaryExtractor.setRefName(extractor.get(BOUNDARY_EXTRACTOR_VARIABLE_NAME));
        boundaryExtractor.setLeftBoundary(extractor.get(BOUNDARY_EXTRACTOR_LBOUNDARY));
        boundaryExtractor.setRightBoundary(extractor.get(BOUNDARY_EXTRACTOR_RBOUNDARY));
        boundaryExtractor.setMatchNumber(extractor.get(BOUNDARY_EXTRACTOR_MATCH_NO));
        return boundaryExtractor;
    }

}
