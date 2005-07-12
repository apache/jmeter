// $Header$
/*
 * ====================================================================
 * Copyright 2002-2004 The Apache Software Foundation.
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

public class NodeList implements Serializable {
	private static final int INITIAL_CAPACITY = 10;

	// private static final int CAPACITY_INCREMENT=20;
	private Node nodeData[];

	private int size;

	private int capacity;

	private int capacityIncrement;

	private int numberOfAdjustments;

	public NodeList() {
		size = 0;
		capacity = INITIAL_CAPACITY;
		nodeData = new Node[capacity];
		capacityIncrement = capacity * 2;
		numberOfAdjustments = 0;
	}

	public void add(Node node) {
		if (size == capacity)
			adjustVectorCapacity();
		nodeData[size++] = node;
	}

	/**
	 * Insert the given node at the head of the list.
	 * 
	 * @param node
	 *            The new first element.
	 */
	public void prepend(Node node) {
		if (size == capacity)
			adjustVectorCapacity();
		System.arraycopy(nodeData, 0, nodeData, 1, size);
		size++;
		nodeData[0] = node;
	}

	private void adjustVectorCapacity() {
		capacity += capacityIncrement;
		capacityIncrement *= 2;
		Node oldData[] = nodeData;
		nodeData = new Node[capacity];
		System.arraycopy(oldData, 0, nodeData, 0, size);
		numberOfAdjustments++;
	}

	public int size() {
		return size;
	}

	public Node elementAt(int i) {
		return nodeData[i];
	}

	public int getNumberOfAdjustments() {
		return numberOfAdjustments;
	}

	public SimpleNodeIterator elements() {
		return new SimpleNodeIterator() {
			int count = 0;

			public boolean hasMoreNodes() {
				return count < size;
			}

			public Node nextNode() {
				synchronized (NodeList.this) {
					if (count < size) {
						return nodeData[count++];
					}
				}
				throw new NoSuchElementException("Vector Enumeration");
			}
		};
	}

	public Node[] toNodeArray() {
		Node[] nodeArray = new Node[size];
		System.arraycopy(nodeData, 0, nodeArray, 0, size);
		return nodeArray;
	}

	public String asString() {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < size; i++)
			buff.append(nodeData[i].toPlainTextString());
		return buff.toString();
	}

	public String asHtml() {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < size; i++)
			buff.append(nodeData[i].toHtml());
		return buff.toString();
	}

	public void remove(int index) {
		System.arraycopy(nodeData, index + 1, nodeData, index, size - index - 1);
		size--;
	}

	public void removeAll() {
		size = 0;
		capacity = INITIAL_CAPACITY;
		nodeData = new Node[capacity];
		capacityIncrement = capacity * 2;
		numberOfAdjustments = 0;
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		for (int i = 0; i < size; i++)
			text.append(nodeData[i].toPlainTextString());
		return text.toString();
	}

	public NodeList searchFor(Class classType) {
		NodeList foundList = new NodeList();
		Node node;
		for (int i = 0; i < size; i++) {
			if (nodeData[i].getClass().getName().equals(classType.getName()))
				foundList.add(nodeData[i]);
		}
		return foundList;
	}
}
