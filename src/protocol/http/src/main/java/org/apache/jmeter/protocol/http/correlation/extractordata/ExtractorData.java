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

package org.apache.jmeter.protocol.http.correlation.extractordata;

public class ExtractorData{

    // Content type of the extractor
    String contentType;
    // Match number for the extractor
    String matchNumber;
    // Correlation variable name
    String refname;
    // name of the test element in which the extractor
    // will be added
    String testName;

    public ExtractorData(String contentType, String matchNr, String refName, String testName) {
        this.contentType = contentType;
        this.matchNumber = matchNr;
        this.refname = refName;
        this.testName = testName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getMatchNumber() {
        return matchNumber;
    }

    public String getRefname() {
        return refname;
    }

    public String getTestName() {
        return testName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ExtractorData)) {
            return false;
        }
        ExtractorData extractorData = (ExtractorData) o;
        return extractorData.getContentType().equals(contentType) && extractorData.getMatchNumber().equals(matchNumber)
                && extractorData.getRefname().equals(refname) && extractorData.getTestName().equals(testName);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + contentType.hashCode();
        result = 31 * result + matchNumber.hashCode();
        result = 31 * result + refname.hashCode();
        result = 31 * result + testName.hashCode();
        return result;
    }
}
