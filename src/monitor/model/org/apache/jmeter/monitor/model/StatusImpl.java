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
 */
package org.apache.jmeter.monitor.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @version $Revision$ on $Date$
 */
public class StatusImpl implements Status {
	private Jvm jvm = null;

	private List connectors = null;

	/**
	 * 
	 */
	public StatusImpl() {
		super();
		connectors = new LinkedList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.monitor.model.Status#getJvm()
	 */
	public Jvm getJvm() {
		return jvm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.monitor.model.Status#setJvm(org.apache.jmeter.monitor.model.Jvm)
	 */
	public void setJvm(Jvm vm) {
		this.jvm = vm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.monitor.model.Status#getConnector()
	 */
	public List getConnector() {
		return this.connectors;
	}

	public void addConnector(Connector conn) {
		this.connectors.add(conn);
	}

}
