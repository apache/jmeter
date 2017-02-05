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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jorphan.util.JMeterError;

/**
 * ListedHashTree is a different implementation of the {@link HashTree}
 * collection class. In the ListedHashTree, the order in which values are added
 * is preserved . Any listing of nodes
 * or iteration through the list of nodes of a ListedHashTree will be given in
 * the order in which the nodes were added to the tree.
 *
 * @see HashTree
 */
public class ListedHashTree extends HashTree implements Serializable, Cloneable {
    private static final long serialVersionUID = 240L;

    private final List<Object> order;

    public ListedHashTree() {
        super();
        order = new LinkedList<>();
    }

    public ListedHashTree(Object key) {
        this();
        data.put(key, new ListedHashTree());
        order.add(key);
    }

    public ListedHashTree(Collection<?> keys) {
        this();
        for (Object temp : keys) {
            data.put(temp, new ListedHashTree());
            order.add(temp);
        }
    }

    public ListedHashTree(Object[] keys) {
        this();
        for (Object key : keys) {
            data.put(key, new ListedHashTree());
            order.add(key);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object clone() {
        ListedHashTree newTree = new ListedHashTree();
        cloneTree(newTree);
        return newTree;
    }

    /** {@inheritDoc} */
    @Override
    public void set(Object key, Object value) {
        if (!data.containsKey(key)) {
            order.add(key);
        }
        super.set(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public void set(Object key, HashTree t) {
        if (!data.containsKey(key)) {
            order.add(key);
        }
        super.set(key, t);
    }

    /** {@inheritDoc} */
    @Override
    public void set(Object key, Object[] values) {
        if (!data.containsKey(key)) {
            order.add(key);
        }
        super.set(key, values);
    }

    /** {@inheritDoc} */
    @Override
    public void set(Object key, Collection<?> values) {
        if (!data.containsKey(key)) {
            order.add(key);
        }
        super.set(key, values);
    }

    /** {@inheritDoc} */
    @Override
    public void replaceKey(Object currentKey, Object newKey) {
        HashTree tree = getTree(currentKey);
        data.remove(currentKey);
        data.put(newKey, tree);
        // find order.indexOf(currentKey) using == rather than equals()
        // there may be multiple entries which compare equals (Bug 50898)
        // This will be slightly slower than the built-in method,
        // but replace() is not used frequently.
        int entry=-1;
        for (int i=0; i < order.size(); i++) {
            Object ent = order.get(i);
            if (ent == currentKey) {
                entry = i;
                break;
            }
        }
        if (entry == -1) {
            throw new JMeterError("Impossible state, data key not present in order: "+currentKey.getClass());
        }
        order.set(entry, newKey);
    }

    /** {@inheritDoc} */
    @Override
    public HashTree createNewTree() {
        return new ListedHashTree();
    }

    /** {@inheritDoc} */
    @Override
    public HashTree createNewTree(Object key) {
        return new ListedHashTree(key);
    }

    /** {@inheritDoc} */
    @Override
    public HashTree createNewTree(Collection<?> values) {
        return new ListedHashTree(values);
    }

    /** {@inheritDoc} */
    @Override
    public HashTree add(Object key) {
        if (!data.containsKey(key)) {
            HashTree newTree = createNewTree();
            data.put(key, newTree);
            order.add(key);
            return newTree;
        }
        return getTree(key);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Object> list() {
        return order;
    }

    /** {@inheritDoc} */
    @Override
    public HashTree remove(Object key) {
        order.remove(key);
        return data.remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] getArray() {
        return order.toArray();
    }

    /** {@inheritDoc} */
    // Make sure the hashCode depends on the order as well
    @Override
    public int hashCode() {
        int hc = 17;
        hc = hc * 37 + (order == null ? 0 : order.hashCode());
        hc = hc * 37 + super.hashCode();
        return hc;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ListedHashTree)) {
            return false;
        }
        ListedHashTree lht = (ListedHashTree) o;
        return super.equals(lht) && order.equals(lht.order);
    }


    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        super.clear();
        order.clear();
    }
}
