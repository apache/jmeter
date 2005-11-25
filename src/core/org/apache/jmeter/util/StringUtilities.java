/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.util;

/**
 * @version $Revision$
 */
public final class StringUtilities {
	public static String substitute(String input, String pattern, String sub) {
		StringBuffer ret = new StringBuffer();
		int start = 0;
		int index = -1;
		while ((index = input.indexOf(pattern, start)) >= start) {
			ret.append(input.substring(start, index));
			ret.append(sub);
			start = index + pattern.length();
		}
		ret.append(input.substring(start));
		return ret.toString();
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private StringUtilities() {
	}

}
