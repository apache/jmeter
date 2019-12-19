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

public class HtmlExtractorData extends ExtractorData {
    String attribute;
    String expr;

    public HtmlExtractorData(String refName, String cssSelectorExpression, String attribute, String matchNr,
            String contentType, String testName) {
        super(contentType, matchNr, refName, testName);
        this.expr = cssSelectorExpression;
        this.attribute = attribute;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getExpr() {
        return expr;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof HtmlExtractorData)) {
            return false;
        }
        HtmlExtractorData hExData = (HtmlExtractorData) o;
        return hExData.getAttribute().equals(attribute) && hExData.getExpr().equals(expr);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + attribute.hashCode();
        result = 31 * result + expr.hashCode();
        return result;
    }
}
