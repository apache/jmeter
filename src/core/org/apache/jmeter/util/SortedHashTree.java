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

/**********************************************************************
This class is used to create a tree structure of objects.  Each element in the tree
is also a key to the next node down in the tree.
***********************************************************************/
public class SortedHashTree implements Serializable
{

  public SortedHashTree()
  {
	 data = new TreeMap();
  }

  public SortedHashTree(Object key)
  {
	 data = new TreeMap();
	 data.put(key,new SortedHashTree());
  }

  public SortedHashTree(Collection keys)
  {
	 data = new TreeMap();
	 Iterator it = keys.iterator();
	 while(it.hasNext())
		data.put(it.next(),new SortedHashTree());
  }

  public SortedHashTree(Object[] keys)
  {
	 data = new TreeMap();
	 for(int x = 0;x < keys.length;x++)
		data.put(keys[x],new SortedHashTree());
  }

/************************************************************
  If the SortedHashTree contains the given object as a key at the top level, then a
  true result is returned, otherwise false.
@param obj Object to be tested as a key.
@return True if the SortedHashTree contains the key, false otherwise.
***************************************************************/
  public boolean containsKey(Object o)
  {
	 return data.containsKey(o);
  }

/*****************************************************************
  If the SortedHashTree is empty, true is returned, false otherwise.
@return True if SortedHashTree is empty, false otherwise.
***************************************************************/
  public boolean isEmpty()
  {
	 return data.isEmpty();
  }

/************************************************************************
  Sets a key and it's value in the SortedHashTree.  It actually sets up a key, and then
  creates a node for the key and adds the value to the new node, as a key.
@param key Key to be set up.
@param value Value to be set up as a key in the secondary node.
*************************************************************************/
  public void set(Object key,Object value)
  {
	 data.put(key,new SortedHashTree(value));
  }

/************************************************************************
Sets a key and it's values in the SortedHashTree.  It sets up a key in the current node, and
then creates a node for that key, and adds all the values in the array as keys in
 the new node.
@param key Key to be set up.
@param values Array of objects to be added as keys in the secondary node.
********************************************************************/
  public void set(Object key,Object[] values)
  {
	 data.put(key,new SortedHashTree(values));
  }

/********************************************************************
 Sets a key and it's values in the SortedHashTree.  It sets up a key in the current node, and
 then creates a node for that key, and adds all the values in the array as keys in
 the new node.
@param key Key to be set up.
@param values Collection of objects to be added as keys in the secondary node.
**************************************************************************/
  public void set(Object key,Collection values)
  {
	 data.put(key,new SortedHashTree(values));
  }

/********************************************************************
 Sets a key and it's values in the SortedHashTree.  It sets up a key in the current node, and
 then creates a node for that key, and adds all the values in the array as keys in
 the new node.
@param key Key to be set up.
@param tree SortedHashTree that the key maps to.
**************************************************************************/
  public void set(Object key,SortedHashTree t)
  {
	 data.put(key,t);
  }

/********************************************************************
  Sets a series of keys into the SortedHashTree.  It sets up the first object in the
  key array as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the array.  Continues recursing in this
  manner until the end of the first array is reached, at which point all the
  values of the second array are added as keys to the bottom-most node.
@param key Array of keys to put into SortedHashTree.
@param values Array of values to be added as keys to bottom-most node.
********************************************************************/
  public void set(Object[] key,Object[] values)
  {
	 List keys = new LinkedList();
	 for(int x = 0;x < key.length;keys.add(key[x++]));
	 set(keys,values);
  }

/********************************************************************
  Sets a series of keys into the SortedHashTree.  It sets up the first object in the
  key array as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the array.  Continues recursing in this
  manner until the end of the first array is reached, at which point all the
  values of the Collection of values are added as keys to the bottom-most node.
@param key Array of keys to put into SortedHashTree.
@param values Collection of values to be added as keys to bottom-most node.
********************************************************************/
  public void set(Object[] key,Collection values)
  {
	 List keys = new LinkedList();
	 for(int x = 0;x < key.length;keys.add(key[x++]));
	 set(keys,values);
  }

/********************************************************************
  Sets a series of keys into the SortedHashTree.  It sets up the first object in the
  key list as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the list.  Continues recursing in this
  manner until the end of the first list is reached, at which point all the
  values of the array of values are added as keys to the bottom-most node.
@param key List of keys to put into SortedHashTree.
@param values Array of values to be added as keys to bottom-most node.
********************************************************************/
  public void set(List key,Object[] values)
  {
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		set(key.get(0),values);
	 else
	 {
		Object temp = key.remove(0);
		add(temp,key.get(0));
		get(temp).set(key,values);
	 }
  }

/********************************************************************
  Sets a series of keys into the SortedHashTree.  It sets up the first object in the
  key list as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the list.  Continues recursing in this
  manner until the end of the first list is reached, at which point all the
  values of the Collection of values are added as keys to the bottom-most node.
@param key List of keys to put into SortedHashTree.
@param values Collection of values to be added as keys to bottom-most node.
********************************************************************/
  public void set(List key,Collection values)
  {
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		set(key.get(0),values);
	 else
	 {
		Object temp = key.remove(0);
		add(temp,key.get(0));
		get(temp).set(key,values);
	 }
  }

/********************************************************************
  Sets a series of keys into the SortedHashTree.  It sets up the first object in the
  key SortedSet as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the SortedSet.  Continues recursing in this
  manner until the end of the first SortedSet is reached, at which point all the
  values of the array of values are added as keys to the bottom-most node.
@param key SortedSet of keys to put into SortedHashTree.
@param values Array of values to be added as keys to bottom-most node.
********************************************************************/
  public void set(SortedSet key,Object[] values)
  {
	 Object temp;
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		set(key.first(),values);
	 else
	 {
		temp = key.first();
		key.remove(temp);
		add(temp,key.first());
		get(temp).set(key,values);
	 }
  }

/********************************************************************
  Sets a series of keys into the SortedHashTree.  It sets up the first object in the
  key SortedSet as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the SortedSet.  Continues recursing in this
  manner until the end of the first SortedSet is reached, at which point all the
  values of the Collection of values are added as keys to the bottom-most node.
@param key SortedSet of keys to put into SortedHashTree.
@param values Collection of values to be added as keys to bottom-most node.
********************************************************************/
  public void set(SortedSet key,Collection values)
  {
	 Object temp;
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		set(key.first(),values);
	 else
	 {
		temp = key.first();
		key.remove(temp);
		add(temp,key.first());
		get(temp).set(key,values);
	 }
  }

/*********************************************************************
Adds an key into the SortedHashTree at the current level.
@param key Key to be added to SortedHashTree.
*****************************************************************/
  public void add(Object key)
  {
	 if(!data.containsKey(key))
		data.put(key,new SortedHashTree());
  }

/*******************************************************************
Adds a bunch of keys into the SortedHashTree at the current level.
@param keys Array of Keys to be added to SortedHashTree.
*******************************************************************/
  public void add(Object[] keys)
  {
	 for(int x = 0;x < keys.length;x++)
		add(keys[x]);
  }

/*******************************************************************
Adds a bunch of keys into the SortedHashTree at the current level.
@param keys Collection of Keys to be added to SortedHashTree.
*******************************************************************/
  public void add(Collection keys)
  {
	 Iterator it = keys.iterator();
	 while(it.hasNext())
		add(it.next());
  }

/************************************************************************
  Adds a key and it's value in the SortedHashTree.  It actually adds  a key, and then
  creates a node for the key and adds the value to the new node, as a key.
@param key Key to be added.
@param value Value to be added as a key in the secondary node.
*************************************************************************/
  public void add(Object key,Object value)
  {
	 if(!data.containsKey(key))
		set(key,value);
	 else
		get(key).add(value);
  }

/************************************************************************
Adds a key and it's values in the SortedHashTree.  It Adds a key in the current node, and
then creates a node for that key, and adds all the values in the array as keys in
 the new node.
@param key Key to be added.
@param values Array of objects to be added as keys in the secondary node.
********************************************************************/
  public void add(Object key,Object[] values)
  {
	 if(!data.containsKey(key))
		set(key,values);
	 else
		get(key).add(values);
  }

/************************************************************************
Adds a key and it's values in the SortedHashTree.  It adds a key in the current node, and
then creates a node for that key, and adds all the values in the Collection as keys in
 the new node.
@param key Key to be added.
@param values Collection of objects to be added as keys in the secondary node.
********************************************************************/
  public void add(Object key,Collection values)
  {
	 if(!data.containsKey(key))
		set(key,values);
	 else
		get(key).add(values);
  }

/********************************************************************
  Adds a series of keys into the SortedHashTree.  It sets up the first object in the
  key array as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the array.  Continues recursing in this
  manner until the end of the first array is reached, at which point all the
  values of the second array are added as keys to the bottom-most node.
@param key Array of keys to put into SortedHashTree.
@param values Array of values to be added as keys to bottom-most node.
********************************************************************/
  public void add(Object[] key,Object[] values)
  {
	 List list = new LinkedList();
	 for(int x = 0;x < key.length;list.add(key[x++]));
	 add(list,values);
  }

/********************************************************************
  Adds a series of keys into the SortedHashTree.  It sets up the first object in the
  key array as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the array.  Continues recursing in this
  manner until the end of the first array is reached, at which point all the
  values of the second Collection are added as keys to the bottom-most node.
@param key Array of keys to put into SortedHashTree.
@param values Collection of values to be added as keys to bottom-most node.
********************************************************************/
  public void add(Object[] key,Collection values)
  {
	 List list = new LinkedList();
	 for(int x = 0;x < key.length;list.add(key[x++]));
	 add(list,values);
  }

/********************************************************************
  Adds a series of keys into the SortedHashTree.  It sets up the first object in the
  key List as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the List.  Continues recursing in this
  manner until the end of the first List is reached, at which point all the
  values of the second Array are added as keys to the bottom-most node.
@param key List of keys to put into SortedHashTree.
@param values Array of values to be added as keys to bottom-most node.
********************************************************************/
  public void add(List key,Object[] values)
  {
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		add(key.get(0),values);
	 else
	 {
		Object temp = key.remove(0);
		add(temp,key.get(0));
		get(temp).add(key,values);
	 }
  }

/********************************************************************
  Adds a series of keys into the SortedHashTree.  It sets up the first object in the
  key List as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the List.  Continues recursing in this
  manner until the end of the first List is reached, at which point all the
  values of the second Collection are added as keys to the bottom-most node.
@param key List of keys to put into SortedHashTree.
@param values Collection of values to be added as keys to bottom-most node.
********************************************************************/
  public void add(List key,Collection values)
  {
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		add(key.get(0),values);
	 else
	 {
		Object temp = key.remove(0);
		add(temp,key.get(0));
		get(temp).add(key,values);
	 }
  }

  /********************************************************************
  Adds a series of keys into the SortedHashTree.  It sets up the first object in the
  key List as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the List.  Continues recursing in this
  manner until the end of the first List is reached, at which point all the
  values of the second Collection are added as keys to the bottom-most node.
@param key List of keys to put into SortedHashTree.
@param values Collection of values to be added as keys to bottom-most node.
********************************************************************/
  public void add(List key,Object value)
  {
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		  add(key.get(0),value);
	 else
	 {
		Object temp = key.remove(0);
		add(temp,key.get(0));
		get(temp).add(key,value);
	 }
  }

/********************************************************************
  Adds a series of keys into the SortedHashTree.  It sets up the first object in the
  key SortedSet as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the SortedSet.  Continues recursing in this
  manner until the end of the first SortedSet is reached, at which point all the
  values of the second Array are added as keys to the bottom-most node.
@param key SortedSet of keys to put into SortedHashTree.
@param values Array of values to be added as keys to bottom-most node.
********************************************************************/
  public void add(SortedSet key,Object[] values)
  {
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		add(key.first(),values);
	 else
	 {
		Object temp = key.first();
		key.remove(temp);
		add(temp,key.first());
		get(temp).add(key,values);
	 }
  }

/********************************************************************
  Adds a series of keys into the SortedHashTree.  It sets up the first object in the
  key SortedSet as a key in the current node, recurses into the next SortedHashTree node through
  that key and adds the second object in the SortedSet.  Continues recursing in this
  manner until the end of the first SortedSet is reached, at which point all the
  values of the second Collection are added as keys to the bottom-most node.
@param key SortedSet of keys to put into SortedHashTree.
@param values Collection of values to be added as keys to bottom-most node.
********************************************************************/
  public void add(SortedSet key,Collection values)
  {
	 if(key.size() == 0)
		return;
	 else if(key.size() == 1)
		add(key.first(),values);
	 else
	 {
		Object temp = key.first();
		key.remove(temp);
		add(temp,key.first());
		get(temp).add(key,values);
	 }
  }

/*********************************************************************
Gets the SortedHashTree object mapped to the given key.
@param key Key used to find appropriate SortedHashTree()
********************************************************************/
  public SortedHashTree get(Object key)
  {
	 return (SortedHashTree)data.get(key);
  }

/*************************************************************
Gets the SortedHashTree object mapped to the last key in the array by recursing through
the SortedHashTree structure one key at a time.
@param keys Array of keys.
@return SortedHashTree at the end of the recursion.
**************************************************************/
  public SortedHashTree get(Object[] keys)
  {
	 List list = new LinkedList();
	 for(int x = 0;x < keys.length;list.add(keys[x++]));
	 return get(list);
  }

/*************************************************************
Gets the SortedHashTree object mapped to the last key in the List by recursing through
the SortedHashTree structure one key at a time.
@param keys List of keys.
@return SortedHashTree at the end of the recursion.
**************************************************************/
  public SortedHashTree get(List keys)
  {
	 if(keys.size() == 1)
		return get(keys.get(0));
	 else if(keys.size() > 1)
	 {
		Object temp = keys.remove(0);
		return get(temp).get(keys);
	 }
	 return new SortedHashTree();
  }

/*************************************************************
Gets the SortedHashTree object mapped to the last key in the SortedSet by recursing through
the SortedHashTree structure one key at a time.
@param keys SortedSet of keys.
@return SortedHashTree at the end of the recursion.
**************************************************************/
  public SortedHashTree get(SortedSet keys)
  {
	 if(keys.size() == 1)
		return get(keys.first());
	 else if(keys.size() > 1)
	 {
		Object temp = keys.first();
		keys.remove(temp);
		return get(temp).get(keys);
	 }
	 return new SortedHashTree();
  }

/****************************************************************
Gets a Set of all keys in the current SortedHashTree node.
@return Set of all keys in this SortedHashTree.
*****************************************************************/
  public Set list()
  {
	 return data.keySet();
  }

/***************************************************************
  Gets a Set of all keys in the SortedHashTree mapped to the given key of the
  current SortedHashTree object (in other words, one level down.
@param key Key used to find SortedHashTree to get list of.
@return Set of all keys in found SortedHashTree.
***************************************************************/
  public Set list(Object key)
  {
	 SortedHashTree temp = (SortedHashTree)data.get(key);
	 if(temp != null)
		return temp.list();
	 else
		return null;
  }

  public void remove(Object key)
  {
	 data.remove(key);
  }

/*************************************************************
Recurses down into the SortedHashTree stucture using each subsequent key in the
array of keys, and returns the Set of keys of the SortedHashTree object at the
end of the recursion.
@param keys Array of keys used to recurse into SortedHashTree structure.
@return Set of all keys found in end SortedHashTree.
**************************************************************/
  public Set list(Object[] keys)
  {
	 List list = new LinkedList();
	 for(int x = 0;x < keys.length;list.add(keys[x++]));
	 return list(list);
  }

/*************************************************************
Recurses down into the SortedHashTree stucture using each subsequent key in the
List of keys, and returns the Set of keys of the SortedHashTree object at the
end of the recursion.
@param keys List of keys used to recurse into SortedHashTree structure.
@return Set of all keys found in end SortedHashTree.
**************************************************************/
  public Set list(List keys)
  {
	 if(keys.size() == 0)
		return list();
	 else if(keys.size() == 1)
		return get(keys.get(0)).list();
	 else
	 {
		Object temp = keys.remove(0);
		return get(temp).list(keys);
	 }
  }

/*************************************************************
Recurses down into the SortedHashTree stucture using each subsequent key in the
SortedSet of keys, and returns the Set of keys of the SortedHashTree object at the
end of the recursion.
@param keys SortedSet of keys used to recurse into SortedHashTree structure.
@return Set of all keys found in end SortedHashTree.
**************************************************************/
  public Set list(SortedSet keys)
  {
	 if(keys.size() == 0)
		return list();
	 else if(keys.size() == 1)
		return get(keys.first()).list();
	 else
	 {
		Object temp = keys.first();
		keys.remove(temp);
		return get(temp).list(keys);
	 }
  }

/****************************************************************
Gets an array of all keys in the current SortedHashTree node.
@return Array of all keys in this SortedHashTree.
*****************************************************************/
  public Object[] getArray()
  {
	 return data.keySet().toArray();
  }

/***************************************************************
  Gets an array of all keys in the SortedHashTree mapped to the given key of the
  current SortedHashTree object (in other words, one level down.
@param key Key used to find SortedHashTree to get list of.
@return Array of all keys in found SortedHashTree.
***************************************************************/
  public Object[] getArray(Object key)
  {
	 return get(key).getArray();
  }

/*************************************************************
Recurses down into the SortedHashTree stucture using each subsequent key in the
array of keys, and returns an array of keys of the SortedHashTree object at the
end of the recursion.
@param keys Array of keys used to recurse into SortedHashTree structure.
@return Array of all keys found in end SortedHashTree.
**************************************************************/
  public Object[] getArray(Object[] keys)
  {
	 List list = new LinkedList();
	 for(int x = 0;x < keys.length;list.add(keys[x++]));
	 return getArray(list);
  }

/*************************************************************
Recurses down into the SortedHashTree stucture using each subsequent key in the
List of keys, and returns an array of keys of the SortedHashTree object at the
end of the recursion.
@param keys List of keys used to recurse into SortedHashTree structure.
@return Array of all keys found in end SortedHashTree.
**************************************************************/
  public Object[] getArray(List keys)
  {
	 if(keys.size() == 0)
		return getArray();
	 if(keys.size() == 1)
		return getArray(keys.get(0));
	 else
	 {
		Object temp = keys.remove(0);
		return get(temp).getArray(keys);
	 }
  }

/*************************************************************
Recurses down into the SortedHashTree stucture using each subsequent key in the
SortedSet of keys, and returns an array of keys of the SortedHashTree object at the
end of the recursion.
@param keys SortedSet of keys used to recurse into SortedHashTree structure.
@return Array of all keys found in end SortedHashTree.
**************************************************************/
  public Object[] getArray(SortedSet keys)
  {
	 if(keys.size() == 0)
		return getArray();
	 if(keys.size() == 1)
		return getArray(keys.first());
	 else
	 {
		Object temp = keys.first();
		keys.remove(temp);
		return get(temp).getArray(keys);
	 }
  }

  public int hashCode()
  {
	 return data.hashCode()*7;
  }

  public boolean equals(Object o)
  {
	 boolean flag = true;
	 if(o instanceof SortedHashTree)
	 {
		SortedHashTree oo = (SortedHashTree)o;
		Iterator it = data.keySet().iterator();
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
		  it = data.keySet().iterator();
		  while(it.hasNext())
		  {
			 Object temp = it.next();
			 flag = get(temp).equals(oo.get(temp));
			 if(!flag)
				break;
		  }
		}
	 }
	 else
		flag = false;
	 return flag;
  }

  public Set keySet()
  {
	 return data.keySet();
  }

/*****************************************************
Searches the SortedHashTree structure for the given key.  If it finds the key,
it returns the SortedHashTree mapped to the key.  If it finds nothing, it returns
null.
@param key Key to search for.
@return SortedHashTree mapped to key, if found, otherwise <B>null</B>.
**********************************************************/
  public SortedHashTree search(Object key)
  {
	 SortedHashTree temp = null;
	 if(data.containsKey(key))
		temp = (SortedHashTree)data.get(key);
	 else
	 {
		Iterator it = list().iterator();
		while(it.hasNext())
		{
		  if(temp == null)
		  {
			 temp = ((SortedHashTree)it.next()).search(key);
			 break;
		  }
		  else
			 break;
		}
	 }
	 return temp;
  }




  void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
  {
	 ois.defaultReadObject();
  }

  void writeObject(ObjectOutputStream oos) throws IOException
  {
	 oos.defaultWriteObject();
  }

  public int size()
  {
	 return data.size();
  }

  private java.util.SortedMap data;
}
