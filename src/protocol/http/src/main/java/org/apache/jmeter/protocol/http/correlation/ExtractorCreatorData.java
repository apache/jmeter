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

import org.apache.jmeter.samplers.SampleResult;

public class ExtractorCreatorData {

    private String parameter;

    private String parameterValue;

    private String contentType;

    private SampleResult sampleResult;

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public SampleResult getSampleResult() {
        return sampleResult;
    }

    public void setSampleResult(SampleResult sampleResult) {
        this.sampleResult = sampleResult;
    }

    @Override
    public String toString() {
        return "ExtractorCreatorData [parameter=" + parameter + ", parameterValue=" + parameterValue + ", contentType="
                + contentType + ", sampleResult=" + sampleResult + "]";
    }

}
