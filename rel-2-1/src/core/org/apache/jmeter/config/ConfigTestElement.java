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

package org.apache.jmeter.config;

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class ConfigTestElement extends AbstractTestElement implements Serializable, ConfigElement {
	public final static String USERNAME = "ConfigTestElement.username";

	public final static String PASSWORD = "ConfigTestElement.password";

	public ConfigTestElement() {
	}

	public void addTestElement(TestElement parm1) {
		if (parm1 instanceof ConfigTestElement) {
			mergeIn(parm1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.config.ConfigElement#addConfigElement(org.apache.jmeter.config.ConfigElement)
	 */
	public void addConfigElement(ConfigElement config) {
		mergeIn((TestElement) config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.config.ConfigElement#expectsModification()
	 */
	public boolean expectsModification() {
		return false;
	}
}
