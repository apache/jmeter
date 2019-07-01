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
package org.apache.jmeter.thinktime;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.thinktime.ThinkTimeCreator;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.sampler.gui.TestActionGui;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.timers.gui.UniformRandomTimerGui;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Default implementation of {@link ThinkTimeCreator}
 * @since 3.2
 */
public class DefaultThinkTimeCreator implements ThinkTimeCreator {
    private static final String DEFAULT_TIMER_IMPLEMENTATION =
            JMeterUtils.getPropDefault(
                    "think_time_creator.default_timer_implementation",
                    UniformRandomTimerGui.class.getName());

    private static final String DEFAULT_PAUSE =
            JMeterUtils.getPropDefault(
                    "think_time_creator.default_constant_pause",
                    "1000");

    private static final String DEFAULT_RANGE =
            JMeterUtils.getPropDefault(
                    "think_time_creator.default_range",
                    "100");

    /**
     */
    public DefaultThinkTimeCreator() {
        super();
    }

    @Override
    public JMeterTreeNode[] createThinkTime(GuiPackage guiPackage, JMeterTreeNode parentNode)
            throws IllegalUserActionException {
        TestAction testAction = (TestAction) guiPackage.createTestElement(TestActionGui.class.getName());
        testAction.setAction(TestAction.PAUSE);
        testAction.setDuration("0");
        JMeterTreeNode thinkTimeNode = new JMeterTreeNode(testAction, guiPackage.getTreeModel());
        thinkTimeNode.setName("Think Time");
        RandomTimer randomTimer = (RandomTimer)
                guiPackage.createTestElement(DEFAULT_TIMER_IMPLEMENTATION);
        randomTimer.setDelay(DEFAULT_PAUSE);
        randomTimer.setRange(DEFAULT_RANGE);
        randomTimer.setName("Pause");

        JMeterTreeNode urtNode = new JMeterTreeNode(randomTimer, guiPackage.getTreeModel());
        return new JMeterTreeNode[] {
                thinkTimeNode,
                urtNode
        };
    }
}
