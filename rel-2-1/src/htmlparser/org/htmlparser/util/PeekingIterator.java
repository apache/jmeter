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

import org.htmlparser.Node;

public interface PeekingIterator extends NodeIterator {
	/**
	 * Fetch a node without consuming it. Subsequent calls to
	 * <code>peek()</code> will return subsequent nodes. The node returned by
	 * <code>peek()</code> will never be a node already consumed by
	 * <code>nextHTMLNode()</code>.
	 * <p>
	 * For example, say there are nodes
	 * &lt;H1&gt;&lt;H2&gt;&lt;H3&gt;&lt;H4&gt;&lt;H5&gt;, this is the nodes
	 * that would be returned for the indicated calls:
	 * 
	 * <pre>
	 * 
	 *  peek()         H1
	 *  peek()         H2
	 *  nextHTMLNode() H1
	 *  peek()         H3
	 *  nextHTMLNode() H2
	 *  nextHTMLNode() H3
	 *  nextHTMLNode() H4
	 *  peek()         H5
	 *  
	 * </pre>
	 * 
	 * @return The next node that would be returned by
	 *         <code>nextHTMLNode()</code> or the node after the last node
	 *         returned by <code>peek()</code>, whichever is later in the
	 *         stream. or null if there are no more nodes available via the
	 *         above rules.
	 */
	public Node peek() throws ParserException;
}
