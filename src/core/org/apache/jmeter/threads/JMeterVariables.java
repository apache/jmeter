// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.threads;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Revision$
 */
public class JMeterVariables {
	private Map variables = new HashMap();

	private int iteration = 0;

	public JMeterVariables() {
	}

	public String getThreadName() {
		return Thread.currentThread().getName();
	}

	public int getIteration() {
		return iteration;
	}

	public void incIteration() {
		iteration++;
	}

	public void initialize() {
		variables.clear();
	}

	public Object remove(String key) {
		return variables.remove(key);
	}

	public void put(String key, String value) {
		variables.put(key, value);
	}

	public void putObject(String key, Object value) {
		variables.put(key, value);
	}

	public void putAll(Map vars) {
		variables.putAll(vars);
	}

	public void putAll(JMeterVariables vars) {
		putAll(vars.variables);
	}

	/**
	 * Returns null values if variable doesn't exist. Users of this must check
	 * for null.
	 */
	public String get(String key) {
		return (String) variables.get(key);
	}

	public Object getObject(String key) {
		return variables.get(key);
	}
	
	public Iterator getIterator(){
		return Collections.unmodifiableMap(variables).entrySet().iterator() ;
	}
}