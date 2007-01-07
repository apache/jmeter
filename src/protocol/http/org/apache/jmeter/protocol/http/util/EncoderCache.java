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

package org.apache.jmeter.protocol.http.util;

import java.io.UnsupportedEncodingException;

import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.util.Cache;
import org.apache.oro.util.CacheLRU;

/**
 * @author Administrator
 * 
 * @version $Revision$ last updated $Date$
 */
public class EncoderCache {
	Cache cache;

	public EncoderCache(int cacheSize) {
		cache = new CacheLRU(cacheSize);
	}

	public String getEncoded(String k) {
		Object encodedValue = cache.getElement(k);
		if (encodedValue != null) {
			return (String) encodedValue;
		}
		try {
			encodedValue = JOrphanUtils.encode(k, "utf8");
		} catch (UnsupportedEncodingException e) {
			// This can't happen (how should utf8 not be supported!?!),
			// so just throw an Error:
			throw new Error("Should not happen: " + e.toString());
		}
		cache.addElement(k, encodedValue);
		return (String) encodedValue;
	}
}
