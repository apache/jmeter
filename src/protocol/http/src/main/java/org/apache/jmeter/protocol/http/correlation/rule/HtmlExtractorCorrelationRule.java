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

package org.apache.jmeter.protocol.http.correlation.rule;

import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.protocol.http.gui.action.CorrelationRuleFile;

public class HtmlExtractorCorrelationRule extends CorrelationRule {

    private String attribute;
    private String expr;
    private String name;
    private String type;

    public HtmlExtractorCorrelationRule(HtmlExtractor htmlExtractor) {
        super(htmlExtractor);
        setName(htmlExtractor.getRefName());
        setType(CorrelationRuleFile.HTML_EXTRACTOR_TYPE);
        setExpr(htmlExtractor.getExpression());
        setAttribute(htmlExtractor.getAttribute());
    }

    public String getAttribute() {
        return attribute;
    }

    public String getExpr() {
        return expr;
    }

    public String getName() {
        return name;
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

    public void setName(String name) {
        this.name = name;
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
        HtmlExtractorCorrelationRule correlationRule = (HtmlExtractorCorrelationRule) rule;
        Boolean isEqual = true;
        if (attribute != null) {
            isEqual = isEqual && correlationRule.getAttribute().equals(attribute);
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
        if (expr != null) {
            result = 31 * result + expr.hashCode();
        }
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

}
