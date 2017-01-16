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

package org.apache.jmeter.gui.action;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Searcher implementation that searches text as is
 */
public class RawTextSearcher implements Searcher {
    private boolean caseSensitive;
    private String textToSearch;


    /**
     * Constructor
     * @param caseSensitive is search case sensitive
     * @param textToSearch Text to search
     */
    public RawTextSearcher(boolean caseSensitive, String textToSearch) {
        super();
        this.caseSensitive = caseSensitive;
        if(caseSensitive) {
            this.textToSearch = textToSearch;
        } else {
            this.textToSearch = textToSearch.toLowerCase();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean search(List<String> textTokens) {
        boolean result;
        for (String searchableToken : textTokens) {
            if(!StringUtils.isEmpty(searchableToken)) {
                if(caseSensitive) {
                    result = searchableToken.contains(textToSearch);
                } else {
                    result = searchableToken.toLowerCase().contains(textToSearch);
                }
                if (result) {
                    return result;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if searchedTextLowerCase is in value
     * @param value string in which the search will be done
     * @param searchedTextLowerCase string which will be searched for
     * @return true if searchedTextLowerCase is in value
     */
    protected boolean testField(String value, String searchedTextLowerCase) {
        if(!StringUtils.isEmpty(value)) {
            return value.toLowerCase().contains(searchedTextLowerCase);
        }
        return false;
    }
}
