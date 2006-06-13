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
package org.htmlparser.tags;

import org.htmlparser.*;
import org.htmlparser.Node;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.visitors.NodeVisitor;

public abstract class CompositeTag extends Tag {
	protected Tag startTag, endTag;

	protected NodeList childTags;

	public CompositeTag(TagData tagData, CompositeTagData compositeTagData) {
		super(tagData);
		this.childTags = compositeTagData.getChildren();
		this.startTag = compositeTagData.getStartTag();
		this.endTag = compositeTagData.getEndTag();
	}

	public SimpleNodeIterator children() {
		return childTags.elements();
	}

	public Node getChild(int index) {
		return childTags.elementAt(index);
	}

	public Node[] getChildrenAsNodeArray() {
		return childTags.toNodeArray();
	}

	public NodeList getChildren() {
		return childTags;
	}

	/**
	 * Return the child tags as an iterator. Equivalent to calling getChildren
	 * ().elements ().
	 * 
	 * @return An iterator over the children.
	 */
	public SimpleNodeIterator elements() {
		return (getChildren().elements());
	}

	public String toPlainTextString() {
		StringBuffer stringRepresentation = new StringBuffer();
		for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
			stringRepresentation.append(e.nextNode().toPlainTextString());
		}
		return stringRepresentation.toString();
	}

	public void putStartTagInto(StringBuffer sb) {
		sb.append(startTag.toHtml());
	}

	protected void putChildrenInto(StringBuffer sb) {
		Node node, prevNode = startTag;
		for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
			node = e.nextNode();
			if (prevNode != null) {
				if (prevNode.elementEnd() > node.elementBegin()) {
					// Its a new line
					sb.append(lineSeparator);
				}
			}
			sb.append(node.toHtml());
			prevNode = node;
		}
		if (prevNode.elementEnd() > endTag.elementBegin()) {
			sb.append(lineSeparator);
		}
	}

	protected void putEndTagInto(StringBuffer sb) {
		sb.append(endTag.toHtml());
	}

	public String toHtml() {
		StringBuffer sb = new StringBuffer();
		putStartTagInto(sb);
		if (!startTag.isEmptyXmlTag()) {
			putChildrenInto(sb);
			putEndTagInto(sb);
		}
		return sb.toString();
	}

	/**
	 * Searches all children who for a name attribute. Returns first match.
	 * 
	 * @param name
	 *            Attribute to match in tag
	 * @return Tag Tag matching the name attribute
	 */
	public Tag searchByName(String name) {
		Node node;
		Tag tag = null;
		boolean found = false;
		for (SimpleNodeIterator e = children(); e.hasMoreNodes() && !found;) {
			node = e.nextNode();
			if (node instanceof Tag) {
				tag = (Tag) node;
				String nameAttribute = tag.getAttribute("NAME");
				if (nameAttribute != null && nameAttribute.equals(name))
					found = true;
			}
		}
		if (found)
			return tag;
		else
			return null;
	}

	/**
	 * Searches for any node whose text representation contains the search
	 * string. Collects all such nodes in a NodeList. e.g. if you wish to find
	 * any textareas in a form tag containing "hello world", the code would be :
	 * <code>
	 *  NodeList nodeList = formTag.searchFor("Hello World");
	 * </code>
	 * 
	 * @param searchString
	 *            search criterion
	 * @param caseSensitivie
	 *            specify whether this search should be case sensitive
	 * @return NodeList Collection of nodes whose string contents or
	 *         representation have the searchString in them
	 */

	public NodeList searchFor(String searchString, boolean caseSensitive) {
		NodeList foundList = new NodeList();
		Node node;
		if (!caseSensitive)
			searchString = searchString.toUpperCase();
		for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
			node = e.nextNode();
			String nodeTextString = node.toPlainTextString();
			if (!caseSensitive)
				nodeTextString = nodeTextString.toUpperCase();
			if (nodeTextString.indexOf(searchString) != -1) {
				foundList.add(node);
			}
		}
		return foundList;
	}

	/**
	 * Collect all objects that are of a certain type Note that this will not
	 * check for parent types, and will not recurse through child tags
	 * 
	 * @param classType
	 * @return NodeList
	 */
	public NodeList searchFor(Class classType) {
		return childTags.searchFor(classType);
	}

	/**
	 * Searches for any node whose text representation contains the search
	 * string. Collects all such nodes in a NodeList. e.g. if you wish to find
	 * any textareas in a form tag containing "hello world", the code would be :
	 * <code>
	 *  NodeList nodeList = formTag.searchFor("Hello World");
	 * </code> This
	 * search is <b>case-insensitive</b>.
	 * 
	 * @param searchString
	 *            search criterion
	 * @return NodeList Collection of nodes whose string contents or
	 *         representation have the searchString in them
	 */
	public NodeList searchFor(String searchString) {
		return searchFor(searchString, false);
	}

	/**
	 * Returns the node number of the string node containing the given text.
	 * This can be useful to index into the composite tag and get other
	 * children.
	 * 
	 * @param text
	 * @return int
	 */
	public int findPositionOf(String text) {
		Node node;
		int loc = 0;
		for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
			node = e.nextNode();
			if (node.toPlainTextString().toUpperCase().indexOf(text.toUpperCase()) != -1) {
				return loc;
			}
			loc++;
		}
		return -1;
	}

	/**
	 * Returns the node number of a child node given the node object. This would
	 * typically be used in conjuction with digUpStringNode, after which the
	 * string node's parent can be used to find the string node's position.
	 * Faster than calling findPositionOf(text) again. Note that the position is
	 * at a linear level alone - there is no recursion in this method.
	 * 
	 * @param text
	 * @return int
	 */
	public int findPositionOf(Node searchNode) {
		Node node;
		int loc = 0;
		for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
			node = e.nextNode();
			if (node == searchNode) {
				return loc;
			}
			loc++;
		}
		return -1;
	}

	/**
	 * Get child at given index
	 * 
	 * @param index
	 * @return Node
	 */
	public Node childAt(int index) {
		return childTags.elementAt(index);
	}

	public void collectInto(NodeList collectionList, String filter) {
		super.collectInto(collectionList, filter);
		Node node;
		for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
			node = e.nextNode();
			node.collectInto(collectionList, filter);
		}
	}

	public void collectInto(NodeList collectionList, Class nodeType) {
		super.collectInto(collectionList, nodeType);
		for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
			e.nextNode().collectInto(collectionList, nodeType);
		}
	}

	public String getChildrenHTML() {
		StringBuffer buff = new StringBuffer();
		for (SimpleNodeIterator e = children(); e.hasMoreNodes();) {
			Node node = e.nextNode();
			buff.append(node.toHtml());
		}
		return buff.toString();
	}

	public void accept(NodeVisitor visitor) {
		if (visitor.shouldRecurseChildren()) {
			startTag.accept(visitor);
			SimpleNodeIterator children = children();
			while (children.hasMoreNodes()) {
				Node child = children.nextNode();
				child.accept(visitor);
			}
			endTag.accept(visitor);
		}
		if (visitor.shouldRecurseSelf())
			visitor.visitTag(this);
	}

	public int getChildCount() {
		return childTags.size();
	}

	public Tag getStartTag() {
		return startTag;
	}

	public Tag getEndTag() {
		return endTag;
	}

	/**
	 * Finds a string node, however embedded it might be, and returns it. The
	 * string node will retain links to its parents, so further navigation is
	 * possible.
	 * 
	 * @param searchText
	 * @return The list of string nodes (recursively) found.
	 */
	public StringNode[] digupStringNode(String searchText) {
		NodeList nodeList = searchFor(searchText);
		NodeList stringNodes = new NodeList();
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.elementAt(i);
			if (node instanceof StringNode) {
				stringNodes.add(node);
			} else {
				if (node instanceof CompositeTag) {
					CompositeTag ctag = (CompositeTag) node;
					StringNode[] nodes = ctag.digupStringNode(searchText);
					for (int j = 0; j < nodes.length; j++)
						stringNodes.add(nodes[j]);
				}
			}
		}
		StringNode[] stringNode = new StringNode[stringNodes.size()];
		for (int i = 0; i < stringNode.length; i++) {
			stringNode[i] = (StringNode) stringNodes.elementAt(i);
		}
		return stringNode;
	}

}
