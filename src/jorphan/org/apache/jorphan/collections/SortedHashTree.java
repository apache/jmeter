// $Header$
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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

//NOTUSED import org.apache.jorphan.logging.LoggingManager;
//NOTUSED import org.apache.log.Logger;

/**
 * SortedHashTree is a different implementation of the {@link HashTree}
 * collection class.  In the SortedHashTree, the ordering of values in the tree
 * is made explicit via the compare() function of objects added to the tree.
 * This works in exactly the same fashion as it does for a SortedSet.
 *
 * @see HashTree
 * @see HashTreeTraverser
 * 
 * @author    mstover1 at apache.org
 * @version   $Revision$
 */
public class SortedHashTree extends HashTree implements Serializable
{
    //NOTUSED private static Logger log = LoggingManager.getLoggerForClass();

    public SortedHashTree()
    {
        data = new TreeMap();
    }

    public SortedHashTree(Object key)
    {
        data = new TreeMap();
        data.put(key, new SortedHashTree());
    }

    public SortedHashTree(Collection keys)
    {
        data = new TreeMap();
        Iterator it = keys.iterator();
        while (it.hasNext())
        {
            data.put(it.next(), new SortedHashTree());
        }
    }

    public SortedHashTree(Object[] keys)
    {
        data = new TreeMap();
        for (int x = 0; x < keys.length; x++)
        {
            data.put(keys[x], new SortedHashTree());
        }
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
        newTree.data = (Map) ((HashMap) data).clone();
        return newTree;
    }
}
