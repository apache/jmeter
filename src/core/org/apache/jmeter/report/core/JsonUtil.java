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
package org.apache.jmeter.report.core;

import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonWriter;

/**
 * The class JsonUtil provides helper functions to generate Json.
 * 
 * @since 2.14
 */
public final class JsonUtil {

    /**
     * Converts a json object to a json-like string.
     *
     * @param json
     *            the json object to convert
     * @return the json-like string representing the specified object
     */
    public static String convertJsonToString(JsonObject json) {
	StringWriter stWriter = new StringWriter();
	JsonWriter jsonWriter = Json.createWriter(stWriter);
	try {
	    jsonWriter.writeObject(json);
	} finally {
	    jsonWriter.close();
	}
	return stWriter.toString();
    }

    /**
     * Convert a json array to a json-like string.
     *
     * @param jsonArray
     *            the json array
     * @return the string
     */
    public static String convertJsonToString(JsonArray jsonArray) {
	StringWriter stWriter = new StringWriter();
	JsonWriter jsonWriter = Json.createWriter(stWriter);
	try {
	    jsonWriter.writeArray(jsonArray);
	} finally {
	    jsonWriter.close();
	}
	return stWriter.toString();
    }
}
