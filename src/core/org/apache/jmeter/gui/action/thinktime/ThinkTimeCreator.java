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

package org.apache.jmeter.gui.action.thinktime;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;

/**
 * Interface for a ThinkTime creator
 * @since 3.2
 */
public interface ThinkTimeCreator {

    /**
     * Create think time
     * @param guiPackage {@link GuiPackage}
     * @param parentNode {@link JMeterTreeNode}
     * @return array of 2 nodes 
     * @throws IllegalUserActionException when timer can't be created for this node
     */
    JMeterTreeNode[] createThinkTime(GuiPackage guiPackage, JMeterTreeNode parentNode) throws IllegalUserActionException;
}
