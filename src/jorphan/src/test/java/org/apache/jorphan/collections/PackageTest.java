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

package org.apache.jorphan.collections;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageTest {

    private static Logger log = LoggerFactory.getLogger(PackageTest.class);

    @Test
    public void testAdd1() throws Exception {
        Collection<String> treePath = Arrays.asList(new String[]{"1", "2", "3", "4"});
        HashTree tree = new HashTree();
        log.debug("treePath = {}", treePath);
        tree.add(treePath, "value");
        log.debug("Now treePath = {}, tree = {}", treePath, tree);
        assertEquals(1, tree.list(treePath).size());
        assertEquals("value", tree.getArray(treePath)[0]);
    }

    @Test
    public void testEqualsAndHashCode1() throws Exception {
        HashTree tree1 = new HashTree("abcd");
        HashTree tree2 = new HashTree("abcd");
        HashTree tree3 = new HashTree("abcde");
        HashTree tree4 = new HashTree("abcde");

        assertEquals(tree1, tree2);
        assertEquals(tree2, tree1);
        assertEquals(tree1.hashCode(), tree2.hashCode());

        assertEquals(tree3, tree4);
        assertEquals(tree4, tree3);
        assertEquals(tree3.hashCode(), tree4.hashCode());

        assertNotSame(tree1, tree2);
        assertNotSame(tree1, tree3);
        assertNotSame(tree1, tree4);
        assertNotSame(tree2, tree3);
        assertNotSame(tree2, tree4);

        assertNotEquals(tree1, tree3);
        assertNotEquals(tree1, tree4);
        assertNotEquals(tree2, tree3);
        assertNotEquals(tree2, tree4);

        assertNotNull(tree1);
        assertNotNull(tree2);

        tree1.add("abcd", tree3);
        assertNotEquals(tree1, tree2);
        assertNotEquals(tree2, tree1);// Check reflexive
        if (tree1.hashCode() == tree2.hashCode()) {
            // This is not a requirement
            System.out.println("WARN: unequal HashTrees should not have equal hashCodes");
        }
        tree2.add("abcd", tree4);
        assertEquals(tree1, tree2);
        assertEquals(tree2, tree1);
        assertEquals(tree1.hashCode(), tree2.hashCode());
    }


    @Test
    public void testAddObjectAndTree() throws Exception {
        ListedHashTree tree = new ListedHashTree("key");
        ListedHashTree newTree = new ListedHashTree("value");
        tree.add("key", newTree);
        assertEquals(1, tree.list().size());
        assertEquals("key", tree.getArray()[0]);
        assertEquals(1, tree.getTree("key").list().size());
        assertEquals(0, tree.getTree("key").getTree("value").size());
        assertEquals("value", tree.getTree("key").getArray()[0]);
        assertNotNull(tree.getTree("key").get("value"));
    }

    @Test
    public void testEqualsAndHashCode2() throws Exception {
        ListedHashTree tree1 = new ListedHashTree("abcd");
        ListedHashTree tree2 = new ListedHashTree("abcd");
        ListedHashTree tree3 = new ListedHashTree("abcde");
        ListedHashTree tree4 = new ListedHashTree("abcde");

        assertEquals(tree1, tree2);
        assertEquals(tree2, tree1);
        assertEquals(tree1.hashCode(), tree2.hashCode());

        assertEquals(tree3, tree4);
        assertEquals(tree4, tree3);
        assertEquals(tree3.hashCode(), tree4.hashCode());

        assertNotSame(tree1, tree2);
        assertNotSame(tree1, tree3);
        assertNotEquals(tree1, tree3);
        assertNotEquals(tree3, tree1);
        assertNotEquals(tree1, tree4);
        assertNotEquals(tree4, tree1);

        assertNotEquals(tree2, tree3);
        assertNotEquals(tree3, tree2);
        assertNotEquals(tree2, tree4);
        assertNotEquals(tree4, tree2);

        tree1.add("abcd", tree3);
        assertNotEquals(tree1, tree2);
        assertNotEquals(tree2, tree1);

        tree2.add("abcd", tree4);
        assertEquals(tree1, tree2);
        assertEquals(tree2, tree1);
        assertEquals(tree1.hashCode(), tree2.hashCode());

        tree1.add("a1");
        tree1.add("a2");
        tree2.add("a2");
        tree2.add("a1");

        assertNotEquals(tree1, tree2);
        assertNotEquals(tree2, tree1);
        if (tree1.hashCode() == tree2.hashCode()) {
            // This is not a requirement
            System.out.println("WARN: unequal ListedHashTrees should not have equal hashcodes");
        }

        tree4.add("abcdef");
        assertNotEquals(tree3, tree4);
        assertNotEquals(tree4, tree3);
    }

    @Test
    public void testSearch() throws Exception {
        ListedHashTree tree = new ListedHashTree();
        SearchByClass<Integer> searcher = new SearchByClass<>(Integer.class);
        String one = "one";
        String two = "two";
        Integer o = 1;
        tree.add(one, o);
        tree.getTree(one).add(o, two);
        tree.traverse(searcher);
        assertEquals(1, searcher.getSearchResults().size());
    }
}
