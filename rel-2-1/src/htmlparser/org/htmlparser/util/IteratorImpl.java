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

import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.NodeReader;

public class IteratorImpl implements PeekingIterator {
	NodeReader reader;

	Vector preRead;

	String resourceLocn;

	ParserFeedback feedback;

	public IteratorImpl(NodeReader rd, String resource, ParserFeedback fb) {
		reader = rd;
		preRead = new Vector(25);
		resourceLocn = resource;
		feedback = fb;
	}

	public Node peek() throws ParserException {
		Node ret;

		if (null == reader)
			ret = null;
		else
			try {
				ret = reader.readElement();
				if (null != ret)
					preRead.addElement(ret);
			} catch (Exception e) {
				StringBuffer msgBuffer = new StringBuffer();
				msgBuffer.append("Unexpected Exception occurred while reading ");
				msgBuffer.append(resourceLocn);
				msgBuffer.append(", in nextHTMLNode");
				reader.appendLineDetails(msgBuffer);
				ParserException ex = new ParserException(msgBuffer.toString(), e);
				feedback.error(msgBuffer.toString(), ex);
				throw ex;
			}

		return (ret);
	}

	/**
	 * Check if more nodes are available.
	 * 
	 * @return <code>true</code> if a call to <code>nextHTMLNode()</code>
	 *         will succeed.
	 */
	public boolean hasMoreNodes() throws ParserException {
		Node node;
		boolean ret;

		if (null == reader)
			ret = false;
		else if (0 != preRead.size())
			ret = true;
		else
			ret = !(null == peek());

		return (ret);
	}

	/**
	 * Get the next node.
	 * 
	 * @return The next node in the HTML stream, or null if there are no more
	 *         nodes.
	 */
	public Node nextNode() throws ParserException {
		Node ret;

		if (hasMoreNodes())
			ret = (Node) preRead.remove(0);
		else
			// should perhaps throw an exception?
			ret = null;

		return (ret);
	}
}
