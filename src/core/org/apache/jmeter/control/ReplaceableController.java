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

import org.apache.jorphan.collections.HashTree;

/**
 * This interface represents a controller that gets replaced during the
 * compilation phase of test execution in an arbitrary way.
 * 
 * @see org.apache.jmeter.gui.action.AbstractAction
 * 
 * @author Thad Smith
 * @version $Revision$
 */
public interface ReplaceableController {

	/**
	 * Used to replace the test execution tree (usually by adding the
	 * subelements of the TestElement that is replacing the
	 * ReplaceableController.
	 * 
	 * @param tree -
	 *            The current HashTree to be executed.
	 * @see org.apache.jorphan.collections.HashTree
	 * @see org.apache.jmeter.gui.action.AbstractAction#convertSubTree
	 */
	public HashTree getReplacementSubTree();
}
