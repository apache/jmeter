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

import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.protocol.http.gui.action.CorrelationRuleFile;

import net.minidev.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public final class CorrelationRule {

    private String attribute;
    private String expr;
    private String lBoundary;
    private String name;
    private String rBoundary;
    private String type;

    public CorrelationRule(Object jsonData) {
        JSONObject object = (JSONObject) jsonData;
        setName(object.getAsString("name"));
        setType(object.getAsString("type"));
        setExpr(object.getAsString("expr"));
        setAttribute(object.getAsString("attribute"));
        setlBoundary(object.getAsString("lBoundary"));
        setrBoundary(object.getAsString("rBoundary"));
    }

    public CorrelationRule(HtmlExtractor htmlExtractor) {
        setName(htmlExtractor.getRefName());
        setType(CorrelationRuleFile.HTML_EXTRACTOR_TYPE);
        setExpr(htmlExtractor.getExpression());
        setAttribute(htmlExtractor.getAttribute());
    }

    public CorrelationRule(XPath2Extractor xPath2Extractor) {
        setName(xPath2Extractor.getRefName());
        setType(CorrelationRuleFile.XPATH2_EXTRACTOR_TYPE);
        setExpr(xPath2Extractor.getXPathQuery());
    }

    public CorrelationRule(JSONPostProcessor jsonPathExtractor) {
        setName(jsonPathExtractor.getRefNames());
        setType(CorrelationRuleFile.JSON_EXTRACTOR_TYPE);
        setExpr(jsonPathExtractor.getJsonPathExpressions());
    }

    public CorrelationRule(RegexExtractor regexExtractor) {
        setName(regexExtractor.getRefName());
        setType(regexExtractor.useHeaders() ? CorrelationRuleFile.REGEX_HEADER_TYPE
                : CorrelationRuleFile.REGEX_EXTRACTOR_TYPE);
        setExpr(regexExtractor.getRegex());
    }

    public CorrelationRule(BoundaryExtractor boundaryExtractor) {
        setName(boundaryExtractor.getRefName());
        setType(CorrelationRuleFile.BOUNDARY_EXTRACTOR_TYPE);
        setlBoundary(boundaryExtractor.getLeftBoundary());
        setrBoundary(boundaryExtractor.getRightBoundary());
    }

    public String getAttribute() {
        return attribute;
    }

    public String getExpr() {
        return expr;
    }

    public String getlBoundary() {
        return lBoundary;
    }

    public String getName() {
        return name;
    }

    public String getrBoundary() {
        return rBoundary;
    }

    public String getType() {
        return type;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public void setlBoundary(String lBoundary) {
        this.lBoundary = lBoundary;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setrBoundary(String rBoundary) {
        this.rBoundary = rBoundary;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object rule) {
        if (rule == this) {
            return true;
        }
        if (!(rule instanceof CorrelationRule)) {
            return false;
        }
        CorrelationRule correlationRule = (CorrelationRule) rule;
        Boolean isEqual = true;
        if (attribute != null) {
            isEqual = isEqual && correlationRule.getAttribute().equals(attribute);
        }
        if (rBoundary != null) {
            isEqual = isEqual && correlationRule.getrBoundary().equals(rBoundary);
        }
        if (lBoundary != null) {
            isEqual = isEqual && correlationRule.getlBoundary().equals(lBoundary);
        }
        if (expr != null) {
            isEqual = isEqual && correlationRule.getExpr().equals(expr);
        }
        return isEqual && correlationRule.getName().equals(name) && correlationRule.getType().equals(type);
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (attribute != null) {
            result = 31 * result + attribute.hashCode();
        }
        if (rBoundary != null) {
            result = 31 * result + rBoundary.hashCode();
        }
        if (lBoundary != null) {
            result = 31 * result + lBoundary.hashCode();
        }
        if (expr != null) {
            result = 31 * result + expr.hashCode();
        }
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
