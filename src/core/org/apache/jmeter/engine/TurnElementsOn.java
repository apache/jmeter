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

package org.apache.jmeter.engine;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

/**
 * @author mstover
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class TurnElementsOn implements HashTreeTraverser {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jorphan.collections.HashTreeTraverser#addNode(java.lang.Object,
	 *      org.apache.jorphan.collections.HashTree)
	 */
	public void addNode(Object node, HashTree subTree) {
		if (node instanceof TestElement && !(node instanceof TestPlan)) {
			((TestElement) node).setRunningVersion(true);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jorphan.collections.HashTreeTraverser#subtractNode()
	 */
	public void subtractNode() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jorphan.collections.HashTreeTraverser#processPath()
	 */
	public void processPath() {
	}

}
