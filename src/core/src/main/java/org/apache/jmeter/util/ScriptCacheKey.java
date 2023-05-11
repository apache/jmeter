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

package org.apache.jmeter.util;

import java.util.Objects;

interface ScriptCacheKey {
    /**
     * Creates a script key from a string. Note: do not use concatenation to build a key. Prefer creating a subclass.
     * @param key cache key
     * @return cache key
     */
    static ScriptCacheKey ofString(String key) {
        return new StringScriptCacheKey(key);
    }

    /**
     * Creates a cache key for a file contents assuming its last modified date is up to date.
     * @param language script language
     * @param absolutePath absolute path of the file
     * @param lastModified file last modification date
     * @return cache key
     */
    static ScriptCacheKey ofFile(String language, String absolutePath, long lastModified) {
        return new FileScriptCacheKey(language, absolutePath, lastModified);
    }

    final class StringScriptCacheKey implements ScriptCacheKey {
        final String contents;

        StringScriptCacheKey(String contents) {
            this.contents = contents;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StringScriptCacheKey that = (StringScriptCacheKey) o;
            return Objects.equals(contents, that.contents);
        }

        @Override
        public int hashCode() {
            return contents.hashCode();
        }

        @Override
        public String toString() {
            return "StringScriptCacheKey{" +
                    "contents='" + contents + '\'' +
                    '}';
        }
    }

    final class FileScriptCacheKey implements ScriptCacheKey {
        final String language;
        final String absolutePath;
        final long lastModified;

        FileScriptCacheKey(String language, String absolutePath, long lastModified) {
            this.language = language;
            this.absolutePath = absolutePath;
            this.lastModified = lastModified;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            FileScriptCacheKey that = (FileScriptCacheKey) o;
            return Objects.equals(language, that.language) && Objects.equals(absolutePath, that.absolutePath) && lastModified == that.lastModified;
        }

        @Override
        public int hashCode() {
            int hash = 1;
            hash = 31 * hash + language.hashCode();
            hash = 31 * hash + absolutePath.hashCode();
            hash = 31 * hash + Long.hashCode(lastModified);
            return hash;
        }

        @Override
        public String toString() {
            return "ScriptCacheKey{" +
                    "language='" + language + '\'' +
                    ", absolutePath='" + absolutePath + '\'' +
                    ", lastModified=" + lastModified +
                    '}';
        }
    }
}
