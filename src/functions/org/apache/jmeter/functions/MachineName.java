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

package org.apache.jmeter.functions;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;

public class MachineName extends AbstractFunction implements Serializable {

	private static final List desc = new LinkedList();

	private static final String KEY = "__machineName"; //$NON-NLS-1$

	// Number of parameters expected - used to reject invalid calls
	private static final int PARAMETER_COUNT = 1;
	static {
		// desc.add("Use fully qualified host name: TRUE/FALSE (Default FALSE)");
		desc.add(JMeterUtils.getResString("function_name_param")); //$NON-NLS-1$
	}

	private Object[] values;

	public MachineName() {
	}

	public Object clone() {
		return new MachineName();
	}

	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {

		JMeterVariables vars = getVariables();

		/*
		 * boolean fullHostName = false; if (((CompoundFunction) values[0])
		 * .execute() .toLowerCase() .equals("true")) { fullHostName = true; }
		 */

		String varName = ((CompoundVariable) values[values.length - 1]).execute();
		String machineName = "";

		try {

			InetAddress Address = InetAddress.getLocalHost();

			// fullHostName disabled until we move up to 1.4 as the support jre
			// if ( fullHostName ) {
			// machineName = Address.getCanonicalHostName();

			// } else {
			machineName = Address.getHostName();
			// }

		} catch (UnknownHostException e) {
		}

		vars.put(varName, machineName);
		return machineName;

	}

	public void setParameters(Collection parameters) throws InvalidVariableException {

		values = parameters.toArray();

		if (values.length != PARAMETER_COUNT) {
			throw new InvalidVariableException("Parameter Count != " + PARAMETER_COUNT);
		}

	}

	public String getReferenceKey() {
		return KEY;
	}

	public List getArgumentDesc() {
		return desc;
	}
}
