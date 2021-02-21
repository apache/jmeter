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
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;

public class StringArgumentHolder implements ArgumentHolder {

    @Override
    public int hashCode() {
        return Objects.hash(metadata, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StringArgumentHolder other = (StringArgumentHolder) obj;
        return Objects.equals(metadata, other.metadata) && Objects.equals(name, other.name);
    }

    private String name;
    private Map<String, String> metadata;

    private StringArgumentHolder(String name, Map<String, String> metadata) {
        this.name = name;
        this.metadata = metadata;
    }

    public static StringArgumentHolder of(String name) {
        Pair<String, Map<String, String>> argdata = ArgumentHolder.parse(name);
        return new StringArgumentHolder(argdata.getLeft(), argdata.getRight());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String toString() {
        return "StringArgumentHolder(" + name + ", " + metadata + ")";
    }
}
