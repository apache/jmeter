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

package org.apache.jmeter.gui.action;

import static org.junit.Assert.assertEquals;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.Before;
import org.junit.Test;

public class TestSave {
        private Save save;


        @Before
        public void setUp() {
            save = new Save();
        }

        @Test
        public void testTreeConversion() throws Exception {
            HashTree tree = new ListedHashTree();
            JMeterTreeNode root = new JMeterTreeNode(new Arguments(), null);
            tree.add(root, root);
            tree.getTree(root).add(root, root);
            save.convertSubTree(tree);
            assertEquals(tree.getArray()[0].getClass().getName(), root.getTestElement().getClass().getName());
            tree = tree.getTree(tree.getArray()[0]);
            assertEquals(tree.getArray()[0].getClass().getName(), root.getTestElement().getClass().getName());
            assertEquals(tree.getTree(tree.getArray()[0]).getArray()[0].getClass().getName(), root.getTestElement()
                    .getClass().getName());
        }
}
