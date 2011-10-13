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

import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Clones the test tree,  skipping test elements that implement {@link Timer} by default.
 */
public class TreeClonerNoTimer extends TreeCloner{
    private Logger logger = LoggingManager.getLoggerForClass();
    
    /**
     * {@inheritDoc}
     */
    public TreeClonerNoTimer() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public TreeClonerNoTimer(boolean honourNoThreadClone) {
        super(honourNoThreadClone);
    }

    /**
     * Doesn't add Timer to tree
     * @see org.apache.jmeter.engine.TreeCloner#addNodeToTree(java.lang.Object)
     */
    @Override
    protected Object addNodeToTree(Object node) {
        if(!(node instanceof Timer)) {
            return super.addNodeToTree(node);
        } else {
            if(logger.isDebugEnabled()) {
                logger.debug("Ignoring timer node:"+ node);
            }
            return node;
        }
    }
}