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

package org.apache.jmeter.report.processor;

/**
 * This provides a value result from samples processing.
 *
 * @since 3.0
 */
public class ValueResultData implements ResultData {

    private Object value;

    public ValueResultData() {
    }

    public ValueResultData(Object value) {
        setValue(value);
    }

    public final Object getValue() {
        return value;
    }

    public final void setValue(Object value) {
        this.value = value;
    }

    @Override
    public <T> T accept(ResultDataVisitor<T> visitor) {
        return visitor.visitValueResult(this);
    }

    @Override
    public String toString() {
        return "ValueResultData [value=" + value + "]";
    }

}
