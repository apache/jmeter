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

public class FileArgumentHolder implements ArgumentHolder {
    private String name;
    private Map<String, String> metadata;

    private FileArgumentHolder(String name, Map<String, String> metadata) {
        this.name = name;
        this.metadata = metadata;
    }

    public static FileArgumentHolder of(String name) {
        if (name == null) {
            return new FileArgumentHolder("", Collections.emptyMap());
        }
        Pair<String, Map<String, String>> parsed = ArgumentHolder.parse(name);
        return new FileArgumentHolder(parsed.getLeft(), parsed.getRight());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
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
        FileArgumentHolder other = (FileArgumentHolder) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "FileArgumentHolder(" + name + ", " + metadata + ")";
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
}
