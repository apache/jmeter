/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.Test;

/**
 * Tests for the merge behaviour of {@link JMeterTreeModel#addSubTree(HashTree, JMeterTreeNode, boolean)}.
 *
 * <p>Covers GitHub issue #6633: when {@code merging=true} the existing Test Plan name must be
 * preserved; when {@code merging=false} (normal load) the name from the loaded file is adopted.
 */
class JMeterTreeModelMergeTest {

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    /** Build a minimal HashTree whose root is a TestPlan with the given name. */
    private static HashTree buildPlanTree(String planName) {
        TestPlan plan = new TestPlan(planName);
        HashTree tree = new HashTree();
        tree.add(plan);
        return tree;
    }

    /** Return the TestPlan held at the first child of the model root. */
    private static TestPlan rootPlan(JMeterTreeModel model) {
        JMeterTreeNode root = (JMeterTreeNode) model.getRoot();
        JMeterTreeNode planNode = (JMeterTreeNode) root.getChildAt(0);
        return (TestPlan) planNode.getUserObject();
    }

    // -----------------------------------------------------------------------
    // tests
    // -----------------------------------------------------------------------

    /**
     * When merging=true the existing Test Plan name must NOT be overwritten,
     * regardless of the name carried by the incoming file.
     *
     * <p>This is the primary regression test for issue #6633 (inconsistent
     * Test Plan naming after merge).
     */
    @SuppressWarnings("deprecation") // JMeterTreeModel(Object) is the intended non-GUI constructor
    @Test
    void merging_preservesExistingTestPlanName() throws Exception {
        // Arrange – model initialised with "Test Plan A"
        JMeterTreeModel model = new JMeterTreeModel(new Object()); // non-GUI constructor
        rootPlan(model).setName("Test Plan A");

        // Act – merge a file whose root plan is named "Test Plan B"
        JMeterTreeNode currentNode = (JMeterTreeNode) ((JMeterTreeNode) model.getRoot()).getChildAt(0);
        model.addSubTree(buildPlanTree("Test Plan B"), currentNode, /* merging= */ true);

        // Assert – the model still shows "Test Plan A"
        assertEquals("Test Plan A", rootPlan(model).getName(),
                "Merge must not overwrite the existing Test Plan name");
    }

    /**
     * When merging=false (normal open/load) the Test Plan name IS replaced by
     * the name from the loaded file.
     */
    @SuppressWarnings("deprecation") // JMeterTreeModel(Object) is the intended non-GUI constructor
    @Test
    void loading_replacesTestPlanName() throws Exception {
        // Arrange
        JMeterTreeModel model = new JMeterTreeModel(new Object());
        rootPlan(model).setName("Test Plan A");

        // Act – load (not merge) a file whose root plan is named "Test Plan B"
        JMeterTreeNode currentNode = (JMeterTreeNode) ((JMeterTreeNode) model.getRoot()).getChildAt(0);
        model.addSubTree(buildPlanTree("Test Plan B"), currentNode, /* merging= */ false);

        // Assert – the model now shows "Test Plan B"
        assertEquals("Test Plan B", rootPlan(model).getName(),
                "Load (non-merge) must adopt the name from the loaded file");
    }

    /**
     * The zero-argument convenience overload {@link JMeterTreeModel#addSubTree(HashTree, JMeterTreeNode)}
     * must behave identically to calling the three-argument form with {@code merging=false}.
     */
    @SuppressWarnings("deprecation") // JMeterTreeModel(Object) is the intended non-GUI constructor
    @Test
    void defaultOverload_behavesLikeNonMerge() throws Exception {
        JMeterTreeModel model = new JMeterTreeModel(new Object());
        rootPlan(model).setName("Original");

        JMeterTreeNode currentNode = (JMeterTreeNode) ((JMeterTreeNode) model.getRoot()).getChildAt(0);
        model.addSubTree(buildPlanTree("Loaded"), currentNode); // two-arg form

        assertEquals("Loaded", rootPlan(model).getName(),
                "Two-argument addSubTree must replace the plan name (non-merge semantics)");
    }
}
