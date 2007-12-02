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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.StringProperty;

public class SwitchController extends GenericController implements Serializable {
	private final static String SWITCH_VALUE = "SwitchController.value";

	public SwitchController() {
		super();
	}

	public Sampler next() {
		if (isFirst()) { // Set the selection once per iteration
			current = getSelectionAsInt();
		}
		return super.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * incrementCurrent is called when the current child (whether sampler or controller)
	 * has been processed.
	 * 
	 * Setting it to int.max marks the controller as having processed all its
	 * children. Thus the controller processes one child per iteration.
	 * 
	 * @see org.apache.jmeter.control.GenericController#incrementCurrent()
	 */
	protected void incrementCurrent() {
		current=Integer.MAX_VALUE;
	}

	public void setSelection(String inputValue) {
		setProperty(new StringProperty(SWITCH_VALUE, inputValue));
	}

	/*
	 * Returns the selection value as a int,
	 * with the value set to zero if it is out of range.
	 */
	private int getSelectionAsInt() {
		int ret;
		getProperty(SWITCH_VALUE).recoverRunningVersion(null);
		String sel = getSelection();
		try {
			ret = Integer.parseInt(sel);
		} catch (NumberFormatException e) {
			ret = 0;
		}
		if (ret < 0 || ret >= getSubControllers().size()) {
			ret = 0;
		}
		return ret;
	}

	public String getSelection() {
		return getPropertyAsString(SWITCH_VALUE);
	}
}
