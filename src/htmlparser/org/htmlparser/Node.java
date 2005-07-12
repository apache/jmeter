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
package org.htmlparser;

import java.io.*;

import org.htmlparser.tags.*;
import org.htmlparser.util.*;
import org.htmlparser.visitors.*;

/**
 * A Node interface is implemented by all types of nodes (tags, string elements,
 * etc)
 */
public abstract class Node implements Serializable {
	/**
	 * The beginning position of the tag in the line
	 */
	protected int nodeBegin;

	/**
	 * The ending position of the tag in the line
	 */
	protected int nodeEnd;

	/**
	 * If parent of this tag
	 */
	protected CompositeTag parent;

	/**
	 * Variable to store lineSeparator. This is setup to read
	 * <code>line.separator</code> from the System property. However it can
	 * also be changed using the mutator methods. This will be used in the
	 * toHTML() methods in all the sub-classes of Node.
	 */
	protected static String lineSeparator = System.getProperty("line.separator", "\n");

	public Node(int nodeBegin, int nodeEnd) {
		this.nodeBegin = nodeBegin;
		this.nodeEnd = nodeEnd;
		this.parent = null;
	}

	public Node(int nodeBegin, int nodeEnd, CompositeTag parent) {
		this.nodeBegin = nodeBegin;
		this.nodeEnd = nodeEnd;
		this.parent = parent;
	}

	/**
	 * @param lineSeparator
	 *            New Line separator to be used
	 */
	public static void setLineSeparator(String lineSeparator) {
		Node.lineSeparator = lineSeparator;
	}

	/**
	 * @return String lineSeparator that will be used in toHTML()
	 */
	public static String getLineSeparator() {
		return Node.lineSeparator;
	}

	/**
	 * Returns a string representation of the node. This is an important method,
	 * it allows a simple string transformation of a web page, regardless of a
	 * node.<br>
	 * Typical application code (for extracting only the text from a web page)
	 * would then be simplified to :<br>
	 * 
	 * <pre>
	 * Node node;
	 * for (Enumeration e = parser.elements(); e.hasMoreElements();) {
	 * 	node = (Node) e.nextElement();
	 * 	System.out.println(node.toPlainTextString()); // Or do whatever processing you wish with the plain text string
	 * }
	 * </pre>
	 */
	public abstract String toPlainTextString();

	/**
	 * This method will make it easier when using html parser to reproduce html
	 * pages (with or without modifications) Applications reproducing html can
	 * use this method on nodes which are to be used or transferred as they were
	 * recieved, with the original html
	 */
	public abstract String toHtml();

	/**
	 * Return the string representation of the node. Subclasses must define this
	 * method, and this is typically to be used in the manner<br>
	 * 
	 * <pre>
	 * System.out.println(node)
	 * </pre>
	 * 
	 * @return java.lang.String
	 */
	public abstract String toString();

	/**
	 * Collect this node and its child nodes (if-applicable) into the collection
	 * parameter, provided the node satisfies the filtering criteria. <P/>
	 * 
	 * This mechanism allows powerful filtering code to be written very easily,
	 * without bothering about collection of embedded tags separately. e.g. when
	 * we try to get all the links on a page, it is not possible to get it at
	 * the top-level, as many tags (like form tags), can contain links embedded
	 * in them. We could get the links out by checking if the current node is a
	 * form tag, and going through its contents. However, this ties us down to
	 * specific tags, and is not a very clean approach. <P/>
	 * 
	 * Using collectInto(), programs get a lot shorter. Now, the code to extract
	 * all links from a page would look like :
	 * 
	 * <pre>
	 * NodeList collectionList = new NodeList();
	 * Node node;
	 * String filter = LinkTag.LINK_TAG_FILTER;
	 * for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
	 * 	node = e.nextNode();
	 * 	node.collectInto(collectionVector, filter);
	 * }
	 * </pre>
	 * 
	 * Thus, collectionList will hold all the link nodes, irrespective of how
	 * deep the links are embedded. This of course implies that tags must
	 * fulfill their responsibilities toward honouring certain filters.
	 * 
	 * <B>Important:</B> In order to keep performance optimal, <B>do not create</B>
	 * you own filter strings, as the internal matching occurs with the
	 * pre-existing filter string object (in the relevant class). i.e. do not
	 * make calls like : <I>collectInto(collectionList,"-l")</I>, instead, make
	 * calls only like : <I>collectInto(collectionList,LinkTag.LINK_TAG_FILTER)</I>.<P/>
	 * 
	 * To find out if your desired tag has filtering support, check the API of
	 * the tag.
	 */
	public abstract void collectInto(NodeList collectionList, String filter);

	/**
	 * Collect this node and its child nodes (if-applicable) into the collection
	 * parameter, provided the node satisfies the filtering criteria. <P/>
	 * 
	 * This mechanism allows powerful filtering code to be written very easily,
	 * without bothering about collection of embedded tags separately. e.g. when
	 * we try to get all the links on a page, it is not possible to get it at
	 * the top-level, as many tags (like form tags), can contain links embedded
	 * in them. We could get the links out by checking if the current node is a
	 * form tag, and going through its contents. However, this ties us down to
	 * specific tags, and is not a very clean approach. <P/>
	 * 
	 * Using collectInto(), programs get a lot shorter. Now, the code to extract
	 * all links from a page would look like :
	 * 
	 * <pre>
	 * NodeList collectionList = new NodeList();
	 * Node node;
	 * for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
	 * 	node = e.nextNode();
	 * 	node.collectInto(collectionVector, LinkTag.class);
	 * }
	 * </pre>
	 * 
	 * Thus, collectionList will hold all the link nodes, irrespective of how
	 * deep the links are embedded.
	 */
	public void collectInto(NodeList collectionList, Class nodeType) {
		if (nodeType.getName().equals(this.getClass().getName())) {
			collectionList.add(this);
		}
	}

	/**
	 * Returns the beginning position of the tag.
	 */
	public int elementBegin() {
		return nodeBegin;
	}

	/**
	 * Returns the ending position fo the tag
	 */
	public int elementEnd() {
		return nodeEnd;
	}

	public abstract void accept(NodeVisitor visitor);

	/**
	 * @deprecated - use toHtml() instead
	 */
	public final String toHTML() {
		return toHtml();
	}

	/**
	 * Get the parent of this tag
	 * 
	 * @return The parent of this node, if it's been set, <code>null</code>
	 *         otherwise.
	 */
	public CompositeTag getParent() {
		return parent;
	}

	/**
	 * Sets the parent of this tag
	 * 
	 * @param tag
	 */
	public void setParent(CompositeTag tag) {
		parent = tag;
	}

}
