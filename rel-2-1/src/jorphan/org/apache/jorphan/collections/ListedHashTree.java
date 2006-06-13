/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * ListedHashTree is a different implementation of the {@link HashTree}
 * collection class. In the ListedHashTree, the order in which values are added
 * is preserved (not to be confused with {@link SortedHashTree}, which sorts
 * the order of the values using the compare() function). Any listing of nodes
 * or iteration through the list of nodes of a ListedHashTree will be given in
 * the order in which the nodes were added to the tree.
 * 
 * @see HashTree
 * @author mstover1 at apache.org
 * @version $Revision$
 */
public class ListedHashTree extends HashTree implements Serializable, Cloneable {
	private List order;

	public ListedHashTree() {
		data = new HashMap();
		order = new LinkedList();
	}

	public Object clone() {
		ListedHashTree newTree = new ListedHashTree();
		cloneTree(newTree);
		return newTree;
	}

	public ListedHashTree(Object key) {
		data = new HashMap();
		order = new LinkedList();
		data.put(key, new ListedHashTree());
		order.add(key);
	}

	public ListedHashTree(Collection keys) {
		data = new HashMap();
		order = new LinkedList();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			Object temp = it.next();
			data.put(temp, new ListedHashTree());
			order.add(temp);
		}
	}

	public ListedHashTree(Object[] keys) {
		data = new HashMap();
		order = new LinkedList();
		for (int x = 0; x < keys.length; x++) {
			data.put(keys[x], new ListedHashTree());
			order.add(keys[x]);
		}
	}

	public void set(Object key, Object value) {
		if (!data.containsKey(key)) {
			order.add(key);
		}
		super.set(key, value);
	}

	public void set(Object key, HashTree t) {
		if (!data.containsKey(key)) {
			order.add(key);
		}
		super.set(key, t);
	}

	public void set(Object key, Object[] values) {
		if (!data.containsKey(key)) {
			order.add(key);
		}
		super.set(key, values);
	}

	public void set(Object key, Collection values) {
		if (!data.containsKey(key)) {
			order.add(key);
		}
		super.set(key, values);
	}

	public void replace(Object currentKey, Object newKey) {
		HashTree tree = getTree(currentKey);
		data.remove(currentKey);
		data.put(newKey, tree);
		order.set(order.indexOf(currentKey), newKey);
	}

	public HashTree createNewTree() {
		return new ListedHashTree();
	}

	public HashTree createNewTree(Object key) {
		return new ListedHashTree(key);
	}

	public HashTree createNewTree(Collection values) {
		return new ListedHashTree(values);
	}

	public HashTree add(Object key) {
		if (!data.containsKey(key)) {
			HashTree newTree = createNewTree();
			data.put(key, newTree);
			order.add(key);
			return newTree;
		} else {
			return getTree(key);
		}
	}

	public Collection list() {
		return order;
	}

	public Object remove(Object key) {
		order.remove(key);
		return data.remove(key);
	}

	public Object[] getArray() {
		return order.toArray();
	}

	// Make sure the hashCode depends on the order as well
	public int hashCode() {
		int hc = 17;
		hc = hc * 37 + (order == null ? 0 : order.hashCode());
		hc = hc * 37 + super.hashCode();
		return hc;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ListedHashTree))
			return false;
		ListedHashTree lht = (ListedHashTree) o;
		return (super.equals(lht) && order.equals(lht.order));

		// boolean flag = true;
		// if (o instanceof ListedHashTree)
		// {
		// ListedHashTree oo = (ListedHashTree) o;
		// Iterator it = order.iterator();
		// Iterator it2 = oo.order.iterator();
		// if (size() != oo.size())
		// {
		// flag = false;
		// }
		// while (it.hasNext() && it2.hasNext() && flag)
		// {
		// if (!it.next().equals(it2.next()))
		// {
		// flag = false;
		// }
		// }
		// if (flag)
		// {
		// it = order.iterator();
		// while (it.hasNext() && flag)
		// {
		// Object temp = it.next();
		// flag = get(temp).equals(oo.get(temp));
		// }
		// }
		// }
		// else
		// {
		// flag = false;
		// }
		// return flag;
	}

	public Set keySet() {
		return data.keySet();
	}

	public int size() {
		return data.size();
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		super.clear();
		order.clear();
	}
}
