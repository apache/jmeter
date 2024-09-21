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

package org.apache.jmeter.gui.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Searcher implementation that searches text as is
 */
public class RawTextSearcher implements Searcher {
    private final boolean caseSensitive;
    private final String textToSearch;

    /**
     * Constructor
     * @param caseSensitive is search case-sensitive
     * @param textToSearch Text to search
     */
    public RawTextSearcher(boolean caseSensitive, String textToSearch) {
        this.caseSensitive = caseSensitive;
        this.textToSearch = textToSearch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean search(List<String> textTokens) {
        return textTokens.stream()
                .filter(StringUtils::isNotEmpty)
                .anyMatch(token -> caseSensitive ? token.contains(textToSearch) : StringUtils.containsAnyIgnoreCase(token, textToSearch));
    }

    @Override
    public String toString() {
        return "RawTextSearcher [caseSensitive=" + caseSensitive + ", textToSearch=" + textToSearch + "]";
    }
}
