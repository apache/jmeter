/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 
 */

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.

package org.htmlparser.util;

import java.io.Serializable;
import java.util.NoSuchElementException;
import org.htmlparser.Node;

public class NodeList implements Serializable
{
    private static final int INITIAL_CAPACITY = 10;
    //private static final int CAPACITY_INCREMENT=20;
    private Node nodeData[];
    private int size;
    private int capacity;
    private int capacityIncrement;
    private int numberOfAdjustments;

    public NodeList()
    {
        size = 0;
        capacity = INITIAL_CAPACITY;
        nodeData = new Node[capacity];
        capacityIncrement = capacity * 2;
        numberOfAdjustments = 0;
    }

    public void add(Node node)
    {
        if (size == capacity)
            adjustVectorCapacity();
        nodeData[size++] = node;
    }

    /**
     * Insert the given node at the head of the list.
     * @param node The new first element.
     */
    public void prepend(Node node)
    {
        if (size == capacity)
            adjustVectorCapacity();
        System.arraycopy(nodeData, 0, nodeData, 1, size);
        size++;
        nodeData[0] = node;
    }

    private void adjustVectorCapacity()
    {
        capacity += capacityIncrement;
        capacityIncrement *= 2;
        Node oldData[] = nodeData;
        nodeData = new Node[capacity];
        System.arraycopy(oldData, 0, nodeData, 0, size);
        numberOfAdjustments++;
    }

    public int size()
    {
        return size;
    }

    public Node elementAt(int i)
    {
        return nodeData[i];
    }

    public int getNumberOfAdjustments()
    {
        return numberOfAdjustments;
    }

    public SimpleNodeIterator elements()
    {
        return new SimpleNodeIterator()
        {
            int count = 0;

            public boolean hasMoreNodes()
            {
                return count < size;
            }

            public Node nextNode()
            {
                synchronized (NodeList.this)
                {
                    if (count < size)
                    {
                        return nodeData[count++];
                    }
                }
                throw new NoSuchElementException("Vector Enumeration");
            }
        };
    }

    public Node[] toNodeArray()
    {
        Node[] nodeArray = new Node[size];
        System.arraycopy(nodeData, 0, nodeArray, 0, size);
        return nodeArray;
    }

    public String asString()
    {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < size; i++)
            buff.append(nodeData[i].toPlainTextString());
        return buff.toString();
    }

    public String asHtml()
    {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < size; i++)
            buff.append(nodeData[i].toHtml());
        return buff.toString();
    }

    public void remove(int index)
    {
        System.arraycopy(
            nodeData,
            index + 1,
            nodeData,
            index,
            size - index - 1);
        size--;
    }

    public void removeAll()
    {
        size = 0;
        capacity = INITIAL_CAPACITY;
        nodeData = new Node[capacity];
        capacityIncrement = capacity * 2;
        numberOfAdjustments = 0;
    }

    public String toString()
    {
        StringBuffer text = new StringBuffer();
        for (int i = 0; i < size; i++)
            text.append(nodeData[i].toPlainTextString());
        return text.toString();
    }

    public NodeList searchFor(Class classType)
    {
        NodeList foundList = new NodeList();
        Node node;
        for (int i = 0; i < size; i++)
        {
            if (nodeData[i].getClass().getName().equals(classType.getName()))
                foundList.add(nodeData[i]);
        }
        return foundList;
    }
}
