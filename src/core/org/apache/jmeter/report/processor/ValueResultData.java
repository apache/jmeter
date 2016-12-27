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
package org.apache.jmeter.report.processor;

/**
 * The class ValueResultData provides a value result from samples processing.
 * 
 * @since 3.0
 */
public class ValueResultData implements ResultData {

    private Object value;

    /**
     * Gets the value of the result.
     *
     * @return the value of the result
     */
    public final Object getValue() {
        return value;
    }

    /**
     * Sets the value of the result.
     *
     * @param value
     *            the new value of the result
     */
    public final void setValue(Object value) {
        this.value = value;
    }

    /**
     * Instantiates a new value result data.
     */
    public ValueResultData() {
    }

    /**
     * Instantiates a new value result data.
     *
     * @param value
     *            the value of the result
     */
    public ValueResultData(Object value) {
        setValue(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.report.processor.ResultData#accept(org.apache.jmeter
     * .report.processor.ResultDataVisitor)
     */
    @Override
    public <T> T accept(ResultDataVisitor<T> visitor) {
        return visitor.visitValueResult(this);
    }

}
