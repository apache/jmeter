/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jmeter.util;
import java.io.*;
import java.util.*;

/****************************************
 * This class is used to create a tree structure of objects. Each element in the
 * tree is also a key to the next node down in the tree. In the ListedHashTree,
 * the order in which values are added is preserved (not to be confused with
 * SortedHashTree, which sorts the order of the values using the compare()
 * function).
 *
 *@author    $Author$
 *@created   $Date$
 *@version   $Revision$
 ***************************************/
public class ListedHashTree implements Serializable,Cloneable
{

	private java.util.Map data;
	private List order;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public ListedHashTree()
	{
		data = new HashMap();
		order = new LinkedList();
	}
	
	public Object clone()
	{
		ListedHashTree newTree = new ListedHashTree();
		newTree.data = (Map)((HashMap)data).clone();
		newTree.order = (List)((LinkedList)order).clone();
		return newTree;
	}

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param key  !ToDo (Parameter description)
	 ***************************************/
	public ListedHashTree(Object key)
	{
		data = new HashMap();
		order = new LinkedList();
		data.put(key, new ListedHashTree());
		order.add(key);
	}

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param keys  !ToDo (Parameter description)
	 ***************************************/
	public ListedHashTree(Collection keys)
	{
		data = new HashMap();
		order = new LinkedList();
		Iterator it = keys.iterator();
		while(it.hasNext())
		{
			Object temp = it.next();
			data.put(temp, new ListedHashTree());
			order.add(temp);
		}
	}

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param keys  !ToDo (Parameter description)
	 ***************************************/
	public ListedHashTree(Object[] keys)
	{
		data = new HashMap();
		order = new LinkedList();
		for(int x = 0; x < keys.length; x++)
		{
			data.put(keys[x], new ListedHashTree());
			order.add(keys[x]);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param visitor  !ToDo (Parameter description)
	 ***************************************/
	public void traverse(ListedHashTreeVisitor visitor)
	{
		Iterator iter = list().iterator();
		while(iter.hasNext())
		{
			Object item = iter.next();
			visitor.addNode(item, get(item));
			get(item).traverseInto(visitor);
		}
	}

	private void traverseInto(ListedHashTreeVisitor visitor)
	{
		Iterator iter = list().iterator();
		while(iter.hasNext())
		{
			Object item = iter.next();
			visitor.addNode(item, get(item));
			get(item).traverseInto(visitor);
		}
		if(list().size() == 0)
		{
			visitor.processPath();
		}
		visitor.subtractNode();
	}

	/****************************************
	 * If the ListedHashTree contains the given object as a key at the top level,
	 * then a true result is returned, otherwise false.
	 *
	 *@param o    !ToDo (Parameter description)
	 *@return     True if the ListedHashTree contains the key, false otherwise.
	 ***************************************/
	public boolean containsKey(Object o)
	{
		return data.containsKey(o);
	}

	/****************************************
	 * If the ListedHashTree is empty, true is returned, false otherwise.
	 *
	 *@return   True if ListedHashTree is empty, false otherwise.
	 ***************************************/
	public boolean isEmpty()
	{
		return data.isEmpty();
	}

	/****************************************
	 * Sets a key and it's value in the ListedHashTree. It actually sets up a key,
	 * and then creates a node for the key and adds the value to the new node, as a
	 * key.
	 *
	 *@param key    Key to be set up.
	 *@param value  Value to be set up as a key in the secondary node.
	 ***************************************/
	public void set(Object key, Object value)
	{
		data.put(key, new ListedHashTree(value));
		order.add(key);
	}

	/****************************************
	 * Sets a key and it's values in the ListedHashTree. It sets up a key in the
	 * current node, and then creates a node for that key, and adds all the values
	 * in the array as keys in the new node.
	 *
	 *@param key   Key to be set up.
	 *@param t     !ToDo (Parameter description)
	 ***************************************/
	public void set(Object key, ListedHashTree t)
	{
		if(!data.containsKey(key))
		{
			order.add(key);
		}
		data.put(key, t);
	}

	/****************************************
	 * Sets a key and it's values in the ListedHashTree. It sets up a key in the
	 * current node, and then creates a node for that key, and adds all the values
	 * in the array as keys in the new node.
	 *
	 *@param key     Key to be set up.
	 *@param values  Array of objects to be added as keys in the secondary node.
	 ***************************************/
	public void set(Object key, Object[] values)
	{
		data.put(key, new ListedHashTree(values));
		order.add(key);
	}

	/****************************************
	 * Sets a key and it's values in the ListedHashTree. It sets up a key in the
	 * current node, and then creates a node for that key, and adds all the values
	 * in the array as keys in the new node.
	 *
	 *@param key     Key to be set up.
	 *@param values  Collection of objects to be added as keys in the secondary
	 *      node.
	 ***************************************/
	public void set(Object key, Collection values)
	{
		data.put(key, new ListedHashTree(values));
		order.add(key);
	}

	/****************************************
	 * Sets a series of keys into the ListedHashTree. It sets up the first object
	 * in the key array as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the
	 * array. Continues recursing in this manner until the end of the first array
	 * is reached, at which point all the values of the second array are added as
	 * keys to the bottom-most node.
	 *
	 *@param key     Array of keys to put into ListedHashTree.
	 *@param values  Array of values to be added as keys to bottom-most node.
	 ***************************************/
	public void set(Object[] key, Object[] values)
	{
		List keys = new LinkedList();
		for(int x = 0; x < key.length; keys.add(key[x++]))
		{
			;
		}
		set(keys, values);
	}

	/****************************************
	 * Sets a series of keys into the ListedHashTree. It sets up the first object
	 * in the key array as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the
	 * array. Continues recursing in this manner until the end of the first array
	 * is reached, at which point all the values of the Collection of values are
	 * added as keys to the bottom-most node.
	 *
	 *@param key     Array of keys to put into ListedHashTree.
	 *@param values  Collection of values to be added as keys to bottom-most node.
	 ***************************************/
	public void set(Object[] key, Collection values)
	{
		List keys = new LinkedList();
		for(int x = 0; x < key.length; keys.add(key[x++]))
		{
			;
		}
		set(keys, values);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param values  !ToDo (Parameter description)
	 ***************************************/
	public void set(Collection values)
	{
		Iterator iter = this.list().iterator();
		while(iter.hasNext())
		{
			this.remove(iter.next());
		}
		this.add(values);
	}

	/****************************************
	 * Sets a series of keys into the ListedHashTree. It sets up the first object
	 * in the key list as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the list.
	 * Continues recursing in this manner until the end of the first list is
	 * reached, at which point all the values of the array of values are added as
	 * keys to the bottom-most node.
	 *
	 *@param key     List of keys to put into ListedHashTree.
	 *@param values  Array of values to be added as keys to bottom-most node.
	 ***************************************/
	public void set(List key, Object[] values)
	{
		if(key.size() == 0)
		{
			return;
		}
		else if(key.size() == 1)
		{
			set(key.get(0), values);
		}
		else
		{
			Object temp = key.get(0);
			add(temp, key.get(1));
			get(temp).set(key.subList(1, key.size()), values);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param currentKey  !ToDo (Parameter description)
	 *@param newKey      !ToDo (Parameter description)
	 ***************************************/
	public void replace(Object currentKey, Object newKey)
	{
		ListedHashTree tree = this.get(currentKey);
		data.remove(currentKey);
		data.put(newKey, tree);
		order.set(order.indexOf(currentKey), newKey);
	}

	/****************************************
	 * Sets a series of keys into the ListedHashTree. It sets up the first object
	 * in the key list as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the list.
	 * Continues recursing in this manner until the end of the first list is
	 * reached, at which point all the values of the Collection of values are added
	 * as keys to the bottom-most node.
	 *
	 *@param key     List of keys to put into ListedHashTree.
	 *@param values  Collection of values to be added as keys to bottom-most node.
	 ***************************************/
	public void set(List key, Collection values)
	{
		if(key.size() == 0)
		{
			return;
		}
		else if(key.size() == 1)
		{
			set(key.get(0), values);
		}
		else
		{
			Object temp = key.get(0);
			add(temp, key.get(1));
			get(temp).set(key.subList(1, key.size()), values);
		}
	}

	/****************************************
	 * Adds an key into the ListedHashTree at the current level.
	 *
	 *@param key  Key to be added to ListedHashTree.
	 ***************************************/
	public void add(Object key)
	{
		if(!data.containsKey(key))
		{
			data.put(key, new ListedHashTree());
			order.add(key);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param key      !ToDo (Parameter description)
	 *@param subTree  !ToDo (Parameter description)
	 ***************************************/
	public void add(Object key, ListedHashTree subTree)
	{
		Iterator iter = subTree.list().iterator();
		while(iter.hasNext())
		{
			Object item = iter.next();
			add(key, item);
			get(key).set(item, subTree.get(item));
		}
	}

	/****************************************
	 * Adds a bunch of keys into the ListedHashTree at the current level.
	 *
	 *@param keys  Array of Keys to be added to ListedHashTree.
	 ***************************************/
	public void add(Object[] keys)
	{
		for(int x = 0; x < keys.length; x++)
		{
			add(keys[x]);
		}
	}

	/****************************************
	 * Adds a bunch of keys into the ListedHashTree at the current level.
	 *
	 *@param keys  Collection of Keys to be added to ListedHashTree.
	 ***************************************/
	public void add(Collection keys)
	{
		Iterator it = keys.iterator();
		while(it.hasNext())
		{
			add(it.next());
		}
	}

	/****************************************
	 * Adds a key and it's value in the ListedHashTree. It actually adds a key, and
	 * then creates a node for the key and adds the value to the new node, as a
	 * key.
	 *
	 *@param key    Key to be added.
	 *@param value  Value to be added as a key in the secondary node.
	 ***************************************/
	public void add(Object key, Object value)
	{
		if(!data.containsKey(key))
		{
			set(key, value);
		}
		else
		{
			get(key).add(value);
		}
	}

	/****************************************
	 * Adds a key and it's values in the ListedHashTree. It Adds a key in the
	 * current node, and then creates a node for that key, and adds all the values
	 * in the array as keys in the new node.
	 *
	 *@param key     Key to be added.
	 *@param values  Array of objects to be added as keys in the secondary node.
	 ***************************************/
	public void add(Object key, Object[] values)
	{
		if(!data.containsKey(key))
		{
			set(key, values);
		}
		else
		{
			get(key).add(values);
		}
	}

	/****************************************
	 * Adds a key and it's values in the ListedHashTree. It adds a key in the
	 * current node, and then creates a node for that key, and adds all the values
	 * in the Collection as keys in the new node.
	 *
	 *@param key     Key to be added.
	 *@param values  Collection of objects to be added as keys in the secondary
	 *      node.
	 ***************************************/
	public void add(Object key, Collection values)
	{
		if(!data.containsKey(key))
		{
			set(key, values);
		}
		else
		{
			get(key).add(values);
		}
	}

	/****************************************
	 * Adds a series of keys into the ListedHashTree. It sets up the first object
	 * in the key array as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the
	 * array. Continues recursing in this manner until the end of the first array
	 * is reached, at which point all the values of the second array are added as
	 * keys to the bottom-most node.
	 *
	 *@param key     Array of keys to put into ListedHashTree.
	 *@param values  Array of values to be added as keys to bottom-most node.
	 ***************************************/
	public void add(Object[] key, Object[] values)
	{
		List list = new LinkedList();
		for(int x = 0; x < key.length; list.add(key[x++]))
		{
			;
		}
		add(list, values);
	}

	/****************************************
	 * Adds a series of keys into the ListedHashTree. It sets up the first object
	 * in the key array as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the
	 * array. Continues recursing in this manner until the end of the first array
	 * is reached, at which point all the values of the second Collection are added
	 * as keys to the bottom-most node.
	 *
	 *@param key     Array of keys to put into ListedHashTree.
	 *@param values  Collection of values to be added as keys to bottom-most node.
	 ***************************************/
	public void add(Object[] key, Collection values)
	{
		List list = new LinkedList();
		for(int x = 0; x < key.length; list.add(key[x++]))
		{
			;
		}
		add(list, values);
	}

	/****************************************
	 * Adds a series of keys into the ListedHashTree. It sets up the first object
	 * in the key List as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the List.
	 * Continues recursing in this manner until the end of the first List is
	 * reached, at which point all the values of the second Array are added as keys
	 * to the bottom-most node.
	 *
	 *@param key     List of keys to put into ListedHashTree.
	 *@param values  Array of values to be added as keys to bottom-most node.
	 ***************************************/
	public void add(List key, Object[] values)
	{
		if(key == null || key.size() == 0)
		{
			add(values);
			return;
		}
		else if(key.size() == 1)
		{
			add(key.get(0), values);
		}
		else
		{
			Object temp = key.get(0);
			add(temp, key.get(1));
			get(temp).add(key.subList(1, key.size()), values);
		}
	}

	/****************************************
	 * Adds a series of keys into the SortedListedHashTree. It sets up the first
	 * object in the key List as a key in the current node, recurses into the next
	 * SortedListedHashTree node through that key and adds the second object in the
	 * List. Continues recursing in this manner until the end of the first List is
	 * reached, at which point all the values of the second Collection are added as
	 * keys to the bottom-most node.
	 *
	 *@param key     List of keys to put into SortedListedHashTree.
	 *@param value   !ToDo (Parameter description)
	 ***************************************/
	public void add(List key, Object value)
	{
		if(key == null || key.size() == 0)
		{
			add(value);
			return;
		}
		else if(key.size() == 1)
		{
			add(key.get(0), value);
		}
		else
		{
			Object temp = key.get(0);
			add(temp, key.get(1));
			get(temp).add(key.subList(1, key.size()), value);
		}
	}

	/****************************************
	 * Adds a series of keys into the ListedHashTree. It sets up the first object
	 * in the key List as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the List.
	 * Continues recursing in this manner until the end of the first List is
	 * reached, at which point all the values of the second Collection are added as
	 * keys to the bottom-most node.
	 *
	 *@param key     List of keys to put into ListedHashTree.
	 *@param values  Collection of values to be added as keys to bottom-most node.
	 ***************************************/
	public void add(List key, Collection values)
	{
		if(key == null || key.size() == 0)
		{
			add(values);
			return;
		}
		else if(key.size() == 1)
		{
			add(key.get(0), values);
		}
		else
		{
			Object temp = key.get(0);
			add(temp, key.get(1));
			get(temp).add(key.subList(1, key.size()), values);
		}
	}

	/****************************************
	 * Adds a series of keys into the ListedHashTree. It sets up the first object
	 * in the key SortedSet as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the
	 * SortedSet. Continues recursing in this manner until the end of the first
	 * SortedSet is reached, at which point all the values of the second Array are
	 * added as keys to the bottom-most node.
	 *
	 *@param key     SortedSet of keys to put into ListedHashTree.
	 *@param values  Array of values to be added as keys to bottom-most node.
	 ***************************************/
	public void add(SortedSet key, Object[] values)
	{
		if(key.size() == 0)
		{
			return;
		}
		else if(key.size() == 1)
		{
			add(key.first(), values);
		}
		else
		{
			Iterator iter = key.iterator();
			Object previous = null;
			ListedHashTree tree = this;
			while(iter.hasNext())
			{
				Object item = iter.next();
				if(previous == null)
				{
					previous = item;
				}
				else
				{
					tree.add(previous, item);
					tree = tree.get(item);
				}
			}
			tree.add(values);
		}
	}

	/****************************************
	 * Adds a series of keys into the ListedHashTree. It sets up the first object
	 * in the key SortedSet as a key in the current node, recurses into the next
	 * ListedHashTree node through that key and adds the second object in the
	 * SortedSet. Continues recursing in this manner until the end of the first
	 * SortedSet is reached, at which point all the values of the second Collection
	 * are added as keys to the bottom-most node.
	 *
	 *@param key     SortedSet of keys to put into ListedHashTree.
	 *@param values  Collection of values to be added as keys to bottom-most node.
	 ***************************************/
	public void add(SortedSet key, Collection values)
	{
		if(key.size() == 0)
		{
			return;
		}
		else if(key.size() == 1)
		{
			add(key.first(), values);
		}
		else
		{
			Iterator iter = key.iterator();
			Object previous = null;
			ListedHashTree tree = this;
			while(iter.hasNext())
			{
				Object item = iter.next();
				if(previous == null)
				{
					previous = item;
				}
				else
				{
					tree.add(previous, item);
					tree = tree.get(item);
				}
			}
			tree.add(values);
		}
	}

	/****************************************
	 * Gets the ListedHashTree object mapped to the given key.
	 *
	 *@param key  Key used to find appropriate ListedHashTree()
	 *@return     !ToDo (Return description)
	 ***************************************/
	public ListedHashTree get(Object key)
	{
		return (ListedHashTree)data.get(key);
	}

	/****************************************
	 * Gets the ListedHashTree object mapped to the last key in the array by
	 * recursing through the ListedHashTree structure one key at a time.
	 *
	 *@param keys  Array of keys.
	 *@return      ListedHashTree at the end of the recursion.
	 ***************************************/
	public ListedHashTree get(Object[] keys)
	{
		List list = new LinkedList();
		for(int x = 0; x < keys.length; list.add(keys[x++]))
		{
			;
		}
		return get(list);
	}

	/****************************************
	 * Gets the ListedHashTree object mapped to the last key in the List by
	 * recursing through the ListedHashTree structure one key at a time.
	 *
	 *@param keys  List of keys.
	 *@return      ListedHashTree at the end of the recursion.
	 ***************************************/
	public ListedHashTree get(List keys)
	{
		if(keys.size() == 1)
		{
			return get(keys.get(0));
		}
		else if(keys.size() > 1)
		{
			return get(keys.get(0)).get(keys.subList(1, keys.size()));
		}
		return new ListedHashTree();
	}

	/****************************************
	 * Gets the ListedHashTree object mapped to the last key in the SortedSet by
	 * recursing through the ListedHashTree structure one key at a time.
	 *
	 *@param keys  SortedSet of keys.
	 *@return      ListedHashTree at the end of the recursion.
	 ***************************************/
	public ListedHashTree get(SortedSet keys)
	{
		if(keys.size() == 1)
		{
			return get(keys.first());
		}
		else if(keys.size() > 1)
		{
			Iterator iter = keys.iterator();
			ListedHashTree tree = this;
			while(iter.hasNext())
			{
				tree = tree.get(iter.next());
			}
			return tree;
		}
		return new ListedHashTree();
	}

	/****************************************
	 * Gets a Set of all keys in the current ListedHashTree node.
	 *
	 *@return   Set of all keys in this ListedHashTree.
	 ***************************************/
	public List list()
	{
		return order;
	}

	/****************************************
	 * Gets a Set of all keys in the ListedHashTree mapped to the given key of the
	 * current ListedHashTree object (in other words, one level down.
	 *
	 *@param key  Key used to find ListedHashTree to get list of.
	 *@return     Set of all keys in found ListedHashTree.
	 ***************************************/
	public List list(Object key)
	{
		ListedHashTree temp = (ListedHashTree)data.get(key);
		if(temp != null)
		{
			return temp.list();
		}
		else
		{
			return null;
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param key  !ToDo (Parameter description)
	 ***************************************/
	public void remove(Object key)
	{
		data.remove(key);
		order.remove(key);
	}

	/****************************************
	 * Recurses down into the ListedHashTree stucture using each subsequent key in
	 * the array of keys, and returns the Set of keys of the ListedHashTree object
	 * at the end of the recursion.
	 *
	 *@param keys  Array of keys used to recurse into ListedHashTree structure.
	 *@return      Set of all keys found in end ListedHashTree.
	 ***************************************/
	public List list(Object[] keys)
	{
		List list = new LinkedList();
		for(int x = 0; x < keys.length; list.add(keys[x++]))
		{
			;
		}
		return list(list);
	}

	/****************************************
	 * Recurses down into the ListedHashTree stucture using each subsequent key in
	 * the List of keys, and returns the Set of keys of the ListedHashTree object
	 * at the end of the recursion.
	 *
	 *@param keys  List of keys used to recurse into ListedHashTree structure.
	 *@return      Set of all keys found in end ListedHashTree.
	 ***************************************/
	public List list(List keys)
	{
		if(keys.size() == 0)
		{
			return list();
		}
		else if(keys.size() == 1)
		{
			return get(keys.get(0)).list();
		}
		else
		{
			return get(keys.get(0)).list(keys.subList(1, keys.size()));
		}
	}

	/****************************************
	 * Recurses down into the ListedHashTree stucture using each subsequent key in
	 * the SortedSet of keys, and returns the Set of keys of the ListedHashTree
	 * object at the end of the recursion.
	 *
	 *@param keys  SortedSet of keys used to recurse into ListedHashTree structure.
	 *@return      Set of all keys found in end ListedHashTree.
	 ***************************************/
	public List list(SortedSet keys)
	{
		if(keys.size() == 0)
		{
			return list();
		}
		else if(keys.size() == 1)
		{
			return get(keys.first()).list();
		}
		else
		{
			Iterator iter = keys.iterator();
			ListedHashTree tree = this;
			while(iter.hasNext())
			{
				tree = tree.get(iter.next());
			}
			return tree.list();
		}
	}

	/****************************************
	 * Gets an array of all keys in the current ListedHashTree node.
	 *
	 *@return   Array of all keys in this ListedHashTree.
	 ***************************************/
	public Object[] getArray()
	{
		return order.toArray();
	}

	/****************************************
	 * Gets an array of all keys in the ListedHashTree mapped to the given key of
	 * the current ListedHashTree object (in other words, one level down.
	 *
	 *@param key  Key used to find ListedHashTree to get list of.
	 *@return     Array of all keys in found ListedHashTree.
	 ***************************************/
	public Object[] getArray(Object key)
	{
		return get(key).getArray();
	}

	/****************************************
	 * Recurses down into the ListedHashTree stucture using each subsequent key in
	 * the array of keys, and returns an array of keys of the ListedHashTree object
	 * at the end of the recursion.
	 *
	 *@param keys  Array of keys used to recurse into ListedHashTree structure.
	 *@return      Array of all keys found in end ListedHashTree.
	 ***************************************/
	public Object[] getArray(Object[] keys)
	{
		List list = new LinkedList();
		for(int x = 0; x < keys.length; list.add(keys[x++]))
		{
			;
		}
		return getArray(list);
	}

	/****************************************
	 * Recurses down into the ListedHashTree stucture using each subsequent key in
	 * the List of keys, and returns an array of keys of the ListedHashTree object
	 * at the end of the recursion.
	 *
	 *@param keys  List of keys used to recurse into ListedHashTree structure.
	 *@return      Array of all keys found in end ListedHashTree.
	 ***************************************/
	public Object[] getArray(List keys)
	{
		if(keys.size() == 0)
		{
			return getArray();
		}
		if(keys.size() == 1)
		{
			return getArray(keys.get(0));
		}
		else
		{
			return get(keys.get(0)).getArray(keys.subList(1, keys.size()));
		}
	}

	/****************************************
	 * Recurses down into the ListedHashTree stucture using each subsequent key in
	 * the SortedSet of keys, and returns an array of keys of the ListedHashTree
	 * object at the end of the recursion.
	 *
	 *@param keys  SortedSet of keys used to recurse into ListedHashTree structure.
	 *@return      Array of all keys found in end ListedHashTree.
	 ***************************************/
	public Object[] getArray(SortedSet keys)
	{
		if(keys.size() == 0)
		{
			return getArray();
		}
		if(keys.size() == 1)
		{
			return getArray(keys.first());
		}
		else
		{
			Iterator iter = keys.iterator();
			ListedHashTree tree = this;
			while(iter.hasNext())
			{
				tree = tree.get(iter.next());
			}
			return tree.getArray();
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int hashCode()
	{
		return data.hashCode() * 7 + 3;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param o  !ToDo (Parameter description)
	 *@return   !ToDo (Return description)
	 ***************************************/
	public boolean equals(Object o)
	{
		boolean flag = true;
		if(o instanceof ListedHashTree)
		{
			ListedHashTree oo = (ListedHashTree)o;
			Iterator it = order.iterator();
			while(it.hasNext())
			{
				if(!oo.containsKey(it.next()))
				{
					flag = false;
					break;
				}
			}
			if(flag)
			{
				it = order.iterator();
				while(it.hasNext())
				{
					Object temp = it.next();
					flag = get(temp).equals(oo.get(temp));
					if(!flag)
					{
						break;
					}
				}
			}
		}
		else
		{
			flag = false;
		}
		return flag;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Set keySet()
	{
		return data.keySet();
	}

	/****************************************
	 * Searches the ListedHashTree structure for the given key. If it finds the
	 * key, it returns the ListedHashTree mapped to the key. If it finds nothing,
	 * it returns null.
	 *
	 *@param key  Key to search for.
	 *@return     ListedHashTree mapped to key, if found, otherwise <B>null</B> .
	 ***************************************/
	public ListedHashTree search(Object key)
	{
		ListedHashTree temp = null;
		if(data.containsKey(key))
		{
			temp = (ListedHashTree)data.get(key);
		}
		else
		{
			Iterator it = list().iterator();
			while(it.hasNext())
			{
				if(temp == null)
				{
					temp = ((ListedHashTree)it.next()).search(key);
					break;
				}
				else
				{
					break;
				}
			}
		}
		return temp;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int size()
	{
		return data.size();
	}

	/****************************************
	 * The method debugs the entire tree all the way down (recursively)adding a tab
	 * at each level. The debugging is only supported on the log4j output files.
	 *
	 *@param numTabs  The number of tabs (preferably make the first call with a
	 *      value 0)
	 **************************************
	public void debugDeep(int numTabs)
	{
		if(!log.isDebugEnabled())
		{
			return;
		}
		if(numTabs == 0 && order.size() > 0)
		{
			log.debug("==============Starting the debugDeep:" + this + "===============");
		}

		StringBuffer tabBuffer = new StringBuffer(numTabs);
		for(int i = 0; i < numTabs; i++)
		{
			tabBuffer.append("\t");
		}
		String tabs = tabBuffer.toString();

		for(Iterator iter = order.iterator(); iter.hasNext(); )
		{
			Object obj = iter.next();
			log.debug(tabs + obj);
			ListedHashTree tree = get(obj);
			tree.debugDeep(numTabs + 1);
		}
		if(numTabs == 0 && order.size() > 0)
		{
			log.debug("==============Ending the debugDeep===============");
		}
	}

	/****************************************
	 * Debugs all the elements in the current ListedHashTree
	 ***************************************
	public void debugShallow()
	{
		if(!log.isDebugEnabled())
		{
			return;
		}
		for(Iterator iter = order.iterator(); iter.hasNext(); )
		{
			log.debug(iter.next());
		}
	}*/



	void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		ois.defaultReadObject();
	}

	void writeObject(ObjectOutputStream oos) throws IOException
	{
		oos.defaultWriteObject();
	}

	/****************************************
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	public static class Test extends junit.framework.TestCase
	{
		/****************************************
		 * !ToDo (Constructor description)
		 *
		 *@param name  !ToDo (Parameter description)
		 ***************************************/
		public Test(String name)
		{
			super(name);
		}

		/****************************************
		 * !ToDo
		 *
		 *@exception Exception  !ToDo (Exception description)
		 ***************************************/
		public void testAddObjectAndTree() throws Exception
		{
			ListedHashTree tree = new ListedHashTree("key");
			ListedHashTree newTree = new ListedHashTree("value");
			tree.add("key", newTree);
			assertEquals(tree.list().size(), 1);
			assertEquals(tree.getArray()[0], "key");
			assertEquals(tree.get("key").list().size(), 1);
			assertEquals(tree.get("key").get("value").size(), 0);
			assertEquals(tree.get("key").getArray()[0], "value");
			this.assertNotNull(tree.get("key").get("value"));
		}
	}

}
