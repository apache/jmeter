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
 

package org.apache.jorphan.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log.Logger;

import org.apache.jorphan.logging.LoggingManager;

/**********************************************************************
 * SortedHashTree is a different implementation of the {@link HashTree} collection class. 
 * In the ListedHashTree,
 * The ordering of values in the tree is made explicit via the compare() function of objects
 * added to the tree.  This works in exactly the same fashion as it does for a SortedSet.
 *
 *@author    mstover1 at apache.org
 * @see HashTree
 * @see HashTreeTraverser
***********************************************************************/
public class SortedHashTree extends HashTree implements Serializable
{
	
	private static Logger log = LoggingManager.getLoggerFor("jorphan.collections");

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
  
  public HashTree createNewTree()
  {
  	return new SortedHashTree();
  }
  
  public HashTree createNewTree(Object key)
	{
		return new SortedHashTree(key);
	}

	public HashTree createNewTree(Collection values)
	{
		return new SortedHashTree(values);
	}
	
	public Object clone()
	{
		HashTree newTree = new SortedHashTree();
		newTree.data = (Map)((HashMap)data).clone();
		return newTree;
	}
}
