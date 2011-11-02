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

package org.apache.jorphan.collections;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class PackageTest extends TestCase {
        public PackageTest(String name) {
            super(name);
        }

        public void testAdd1() throws Exception {
            Logger log = LoggingManager.getLoggerForClass();
            Collection<String> treePath = Arrays.asList(new String[] { "1", "2", "3", "4" });
            HashTree tree = new HashTree();
            log.debug("treePath = " + treePath);
            tree.add(treePath, "value");
            log.debug("Now treePath = " + treePath);
            log.debug(tree.toString());
            assertEquals(1, tree.list(treePath).size());
            assertEquals("value", tree.getArray(treePath)[0]);
        }

        public void testEqualsAndHashCode1() throws Exception {
            HashTree tree1 = new HashTree("abcd");
            HashTree tree2 = new HashTree("abcd");
            HashTree tree3 = new HashTree("abcde");
            HashTree tree4 = new HashTree("abcde");

            assertTrue(tree1.equals(tree1));
            assertTrue(tree1.equals(tree2));
            assertTrue(tree2.equals(tree1));
            assertTrue(tree2.equals(tree2));
            assertEquals(tree1.hashCode(), tree2.hashCode());

            assertTrue(tree3.equals(tree3));
            assertTrue(tree3.equals(tree4));
            assertTrue(tree4.equals(tree3));
            assertTrue(tree4.equals(tree4));
            assertEquals(tree3.hashCode(), tree4.hashCode());

            assertNotSame(tree1, tree2);
            assertNotSame(tree1, tree3);
            assertNotSame(tree1, tree4);
            assertNotSame(tree2, tree3);
            assertNotSame(tree2, tree4);

            assertFalse(tree1.equals(tree3));
            assertFalse(tree1.equals(tree4));
            assertFalse(tree2.equals(tree3));
            assertFalse(tree2.equals(tree4));

            assertNotNull(tree1);
            assertNotNull(tree2);

            tree1.add("abcd", tree3);
            assertFalse(tree1.equals(tree2));
            assertFalse(tree2.equals(tree1));// Check reflexive
            if (tree1.hashCode() == tree2.hashCode()) {
                // This is not a requirement
                System.out.println("WARN: unequal HashTrees should not have equal hashCodes");
            }
            tree2.add("abcd", tree4);
            assertTrue(tree1.equals(tree2));
            assertTrue(tree2.equals(tree1));
            assertEquals(tree1.hashCode(), tree2.hashCode());
        }


        public void testAddObjectAndTree() throws Exception {
            ListedHashTree tree = new ListedHashTree("key");
            ListedHashTree newTree = new ListedHashTree("value");
            tree.add("key", newTree);
            assertEquals(tree.list().size(), 1);
            assertEquals("key", tree.getArray()[0]);
            assertEquals(1, tree.getTree("key").list().size());
            assertEquals(0, tree.getTree("key").getTree("value").size());
            assertEquals(tree.getTree("key").getArray()[0], "value");
            assertNotNull(tree.getTree("key").get("value"));
        }

        public void testEqualsAndHashCode2() throws Exception {
            ListedHashTree tree1 = new ListedHashTree("abcd");
            ListedHashTree tree2 = new ListedHashTree("abcd");
            ListedHashTree tree3 = new ListedHashTree("abcde");
            ListedHashTree tree4 = new ListedHashTree("abcde");

            assertTrue(tree1.equals(tree1));
            assertTrue(tree1.equals(tree2));
            assertTrue(tree2.equals(tree1));
            assertTrue(tree2.equals(tree2));
            assertEquals(tree1.hashCode(), tree2.hashCode());

            assertTrue(tree3.equals(tree3));
            assertTrue(tree3.equals(tree4));
            assertTrue(tree4.equals(tree3));
            assertTrue(tree4.equals(tree4));
            assertEquals(tree3.hashCode(), tree4.hashCode());

            assertNotSame(tree1, tree2);
            assertNotSame(tree1, tree3);
            assertFalse(tree1.equals(tree3));
            assertFalse(tree3.equals(tree1));
            assertFalse(tree1.equals(tree4));
            assertFalse(tree4.equals(tree1));

            assertFalse(tree2.equals(tree3));
            assertFalse(tree3.equals(tree2));
            assertFalse(tree2.equals(tree4));
            assertFalse(tree4.equals(tree2));

            tree1.add("abcd", tree3);
            assertFalse(tree1.equals(tree2));
            assertFalse(tree2.equals(tree1));

            tree2.add("abcd", tree4);
            assertTrue(tree1.equals(tree2));
            assertTrue(tree2.equals(tree1));
            assertEquals(tree1.hashCode(), tree2.hashCode());

            tree1.add("a1");
            tree1.add("a2");
            // tree1.add("a3");
            tree2.add("a2");
            tree2.add("a1");

            assertFalse(tree1.equals(tree2));
            assertFalse(tree2.equals(tree1));
            if (tree1.hashCode() == tree2.hashCode()) {
                // This is not a requirement
                System.out.println("WARN: unequal ListedHashTrees should not have equal hashcodes");

            }

            tree4.add("abcdef");
            assertFalse(tree3.equals(tree4));
            assertFalse(tree4.equals(tree3));
        }


        public void testSearch() throws Exception {
            ListedHashTree tree = new ListedHashTree();
            SearchByClass<Integer> searcher = new SearchByClass<Integer>(Integer.class);
            String one = "one";
            String two = "two";
            Integer o = Integer.valueOf(1);
            tree.add(one, o);
            tree.getTree(one).add(o, two);
            tree.traverse(searcher);
            assertEquals(1, searcher.getSearchResults().size());
        }

}
