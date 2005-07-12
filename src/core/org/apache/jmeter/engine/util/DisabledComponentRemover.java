// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.engine.util;

import java.util.LinkedList;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

/**
 * @version $Revision$
 */
public class DisabledComponentRemover implements HashTreeTraverser {
	HashTree tree;

	LinkedList stack = new LinkedList();

	public DisabledComponentRemover(HashTree tree) {
		this.tree = tree;
	}

	public void addNode(Object node, HashTree subTree) {
		stack.addLast(node);
	}

	public void subtractNode() {
		TestElement lastNode = (TestElement) stack.removeLast();
		if (!lastNode.getPropertyAsBoolean(TestElement.ENABLED)) {
			tree.getTree(stack).remove(lastNode);
		}
	}

	public void processPath() {
	}
}
