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

package org.apache.jmeter.report.dashboard;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.report.core.JsonUtil;
import org.apache.jmeter.report.processor.ListResultData;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.ResultData;
import org.apache.jmeter.report.processor.ResultDataVisitor;
import org.apache.jmeter.report.processor.ValueResultData;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
/**
 * The class JsonizerVisitor provides a visitor that can get json-like string
 * from ResultData.
 *
 * @since 3.0
 */
public class JsonizerVisitor implements ResultDataVisitor<String> {

    /**
     * Instantiates a new jsonizer visitor.
     */
    public JsonizerVisitor() {
    }

    @Override
    public String visitListResult(ListResultData listResult) {
        String result = "";
        if (listResult != null) {
            int count = listResult.getSize();
            String[] items = new String[count];
            for (int i = 0; i < count; i++) {
                items[i] = listResult.get(i).accept(this);
            }
            result = JsonUtil.toJsonArray(items);
        }
        return result;
    }

    @Override
    public String visitMapResult(MapResultData mapResult) {
        String result = "";
        if (mapResult != null) {
            HashMap<String, String> map = new HashMap<>();
            for (Map.Entry<String, ResultData> entry : mapResult.entrySet()) {
                map.put(entry.getKey(), entry.getValue().accept(this));
            }
            result = JsonUtil.toJsonObject(map);
        }
        return result;
    }

    @Override
    public String visitValueResult(ValueResultData valueResult) {
        String result = "";
        if (valueResult != null) {
            Object value = valueResult.getValue();
            result = String.valueOf(value);
            if (value instanceof String) {
                result = '"' + new String(JsonStringEncoder.getInstance().quoteAsString(result)) + '"';
            }
        }
        return result;
    }
}
