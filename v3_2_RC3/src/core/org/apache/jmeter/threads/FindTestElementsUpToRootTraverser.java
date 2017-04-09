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

package org.apache.jmeter.threads;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HashTreeTraverser implementation that stores in a Stack all 
 * the Test Elements on the path to a particular node.
 */
public class FindTestElementsUpToRootTraverser implements HashTreeTraverser {
    private static final Logger log = LoggerFactory.getLogger(FindTestElementsUpToRootTraverser.class);

    private final LinkedList<TestElement> stack = new LinkedList<>();

    /**
     * Node to find in TestTree
     */
    private final Object nodeToFind;
    /**
     * Once we find the node in the Tree we stop recording nodes
     */
    private boolean stopRecording = false;

    /**
     * @param nodeToFind Node to find
     */
    public FindTestElementsUpToRootTraverser(Object nodeToFind) {
        this.nodeToFind = nodeToFind;
    }

    /** {@inheritDoc} */
    @Override
    public void addNode(Object node, HashTree subTree) {
        if(stopRecording) {
            return;
        }
        if(node == nodeToFind) {
            this.stopRecording = true;
        }
        stack.addLast((TestElement) node);        
    }

    /** {@inheritDoc} */
    @Override
    public void subtractNode() {
        if(stopRecording) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Subtracting node, stack size = {}", stack.size());
        }
        stack.removeLast();        
    }

    /** {@inheritDoc} */
    @Override
    public void processPath() {
        //NOOP
    }

    /**
     * Returns all controllers that where in Tree down to nodeToFind in reverse order (from leaf to root)
     * @return List of {@link Controller}
     */
    public List<Controller> getControllersToRoot() {
        List<Controller> result = new ArrayList<>(stack.size());
        LinkedList<TestElement> stackLocalCopy = new LinkedList<>(stack);
        while(!stackLocalCopy.isEmpty()) {
            TestElement te = stackLocalCopy.getLast();
            if(te instanceof Controller) {
                result.add((Controller)te);
            }
            stackLocalCopy.removeLast();
        }
        return result;
    }
}
