// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
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
 */
package org.apache.jmeter.monitor.model;

/**
 * 
 * @version $Revision$ on $Date$
 */
public class JvmImpl implements Jvm {
	private Memory memory = null;

	/**
	 * 
	 */
	public JvmImpl() {
		super();
	}

	public Memory getMemory() {
		return this.memory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.monitor.model.Jvm#setMemory(org.apache.jmeter.monitor.model.Memory)
	 */
	public void setMemory(Memory mem) {
		this.memory = mem;
	}

}
