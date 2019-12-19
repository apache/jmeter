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

package org.apache.jmeter.protocol.http.correlation.extractordata;

public class RegexExtractorData extends ExtractorData {
    String expr;
    String template;

    Boolean useHeaders;

    public RegexExtractorData(String expr, String refName, String testName, String matchNr, String groupNumber,
            Boolean useHeaders) {
        super("", matchNr, refName, testName);
        this.expr = expr;
        this.template = groupNumber;
        this.useHeaders = useHeaders;
    }

    public String getExpr() {
        return expr;
    }

    public String getTemplate() {
        return template;
    }

    public Boolean getUseHeaders() {
        return useHeaders;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setUseHeaders(Boolean useHeaders) {
        this.useHeaders = useHeaders;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RegexExtractorData)) {
            return false;
        }
        RegexExtractorData rExData = (RegexExtractorData) o;
        return rExData.getExpr().equals(expr) && rExData.getTemplate().equals(template)
                && rExData.getUseHeaders().equals(useHeaders);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + expr.hashCode();
        result = 31 * result + template.hashCode();
        result = 31 * result + useHeaders.hashCode();
        return result;
    }

}
