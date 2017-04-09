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

package org.apache.jmeter.engine;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

/**
 * Invokes {@link TestElement#setRunningVersion(boolean) setRunningVersion(true)} for all matched nodes
 */
public class TurnElementsOn implements HashTreeTraverser {

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNode(Object node, HashTree subTree) {
        if (node instanceof TestElement && !(node instanceof TestPlan)) {
            ((TestElement) node).setRunningVersion(true);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subtractNode() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPath() {
    }

}
