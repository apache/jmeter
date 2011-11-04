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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Regexp search implementation
 */
public class RegexpSearcher implements Searcher {
	private boolean caseSensitive;
	private Pattern pattern;
	

	/**
	 * @param caseSensitive
	 * @param regexp
	 */
	public RegexpSearcher(boolean caseSensitive, String regexp) {
		super();
		this.caseSensitive = caseSensitive;
		String newRegexp = ".*"+regexp+".*";
		if(caseSensitive) {
			pattern = Pattern.compile(newRegexp);
		} else {
			pattern = Pattern.compile(newRegexp.toLowerCase());
		}
	}


	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.action.ISearcher#search(java.util.List)
	 */
	@Override
	public boolean search(List<String> textTokens) {
		for (String searchableToken : textTokens) {
			if(!StringUtils.isEmpty(searchableToken)) {
				Matcher matcher = null; 
				if(caseSensitive) {
					matcher = pattern.matcher(searchableToken);
				} else {
					matcher = pattern.matcher(searchableToken.toLowerCase());
				}
				if(matcher.find()) {
					return true;
				}
			}
		}
		return false;
	}
}