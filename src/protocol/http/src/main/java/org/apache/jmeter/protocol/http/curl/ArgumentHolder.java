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

package org.apache.jmeter.protocol.http.curl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public interface ArgumentHolder {

    String getName();

    Map<String, String> getMetadata();

    default String getContentType() {
        return getMetadata().get("type");
    }

    default boolean hasContenType() {
        return getMetadata().containsKey("type");
    }

    static Pair<String, Map<String, String>> parse(String name) {
        if (name.contains(";")) {
            String[] parts = name.split(";");
            String realName = parts[0];
            Map<String, String> metadata = new HashMap<>();
            for (int i = 1; i< parts.length; i++) {
                String[] typeParts = parts[i].split("\\s*=\\s*", 2);
                metadata.put(typeParts[0].toLowerCase(Locale.US), typeParts[1]);
            }
            return Pair.of(realName, metadata);
        } else {
            return Pair.of(name, Collections.emptyMap());
        }
    }
}
