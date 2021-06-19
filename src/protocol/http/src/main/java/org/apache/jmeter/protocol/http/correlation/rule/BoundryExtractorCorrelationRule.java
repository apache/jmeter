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

import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.protocol.http.gui.action.CorrelationRuleFile;

public class BoundryExtractorCorrelationRule extends CorrelationRule {

    private String lBoundary;
    private String name;
    private String rBoundary;
    private String type;

    public BoundryExtractorCorrelationRule(BoundaryExtractor boundaryExtractor) {
        super(boundaryExtractor);
        setName(boundaryExtractor.getRefName());
        setType(CorrelationRuleFile.BOUNDARY_EXTRACTOR_TYPE);
        setlBoundary(boundaryExtractor.getLeftBoundary());
        setrBoundary(boundaryExtractor.getRightBoundary());

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
        BoundryExtractorCorrelationRule correlationRule = (BoundryExtractorCorrelationRule) rule;
        Boolean isEqual = true;
        if (rBoundary != null) {
            isEqual = isEqual && correlationRule.getrBoundary().equals(rBoundary);
        }
        if (lBoundary != null) {
            isEqual = isEqual && correlationRule.getlBoundary().equals(lBoundary);
        }
        return isEqual && correlationRule.getName().equals(name) && correlationRule.getType().equals(type);
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (rBoundary != null) {
            result = 31 * result + rBoundary.hashCode();
        }
        if (lBoundary != null) {
            result = 31 * result + lBoundary.hashCode();
        }
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

}
