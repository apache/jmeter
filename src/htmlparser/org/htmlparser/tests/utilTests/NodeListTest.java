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

package org.htmlparser.tests.utilTests;

import org.htmlparser.Node;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.visitors.NodeVisitor;

public class NodeListTest extends ParserTestCase
{
    private NodeList nodeList;
    private Node[] testNodes;

    public NodeListTest(String name)
    {
        super(name);
    }

    protected void setUp()
    {
        nodeList = new NodeList();
    }

    public void testAddOneItem()
    {
        Node node = createHTMLNodeObject();
        nodeList.add(node);
        assertEquals("Vector Size", 1, nodeList.size());
        assertTrue("First Element", node == nodeList.elementAt(0));
    }

    public void testAddTwoItems()
    {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        assertEquals("Vector Size", 2, nodeList.size());
        assertTrue("First Element", node1 == nodeList.elementAt(0));
        assertTrue("Second Element", node2 == nodeList.elementAt(1));
    }

    public void testAddTenItems()
    {
        createTestDataAndPutInVector(10);
        assertTestDataCouldBeExtractedFromVector(10);
    }

    public void testAddElevenItems()
    {
        createTestDataAndPutInVector(11);
        assertTestDataCouldBeExtractedFromVector(11);
    }

    public void testAddThirtyItems()
    {
        createTestDataAndPutInVector(30);
        assertTestDataCouldBeExtractedFromVector(30);
        assertEquals(
            "Number of Adjustments",
            1,
            nodeList.getNumberOfAdjustments());
    }

    public void testAddThirtyOneItems()
    {
        createTestDataAndPutInVector(31);
        assertTestDataCouldBeExtractedFromVector(31);
        assertEquals(
            "Number of Adjustments",
            2,
            nodeList.getNumberOfAdjustments());
    }

    public void testAddFiftyItems()
    {
        createTestDataAndPutInVector(50);
        assertTestDataCouldBeExtractedFromVector(50);
        assertEquals(
            "Number of Adjustments",
            2,
            nodeList.getNumberOfAdjustments());
    }

    public void testAddFiftyOneItems()
    {
        createTestDataAndPutInVector(51);
        assertTestDataCouldBeExtractedFromVector(51);
        assertEquals(
            "Number of Adjustments",
            2,
            nodeList.getNumberOfAdjustments());
    }

    public void testAddTwoHundredItems()
    {
        createTestDataAndPutInVector(200);
        assertEquals(
            "Number of Adjustments",
            4,
            nodeList.getNumberOfAdjustments());
    }

    public void testElements() throws Exception
    {
        createTestDataAndPutInVector(11);
        Node[] resultNodes = new Node[11];
        int i = 0;
        for (SimpleNodeIterator e = nodeList.elements(); e.hasMoreNodes();)
        {
            resultNodes[i] = e.nextNode();
            assertTrue(
                "Node " + i + " did not match",
                testNodes[i] == resultNodes[i]);
            i++;
        }

    }

    private Node createHTMLNodeObject()
    {
        Node node = new Node(10, 20)
        {
            public void accept(NodeVisitor visitor)
            {
            }

            public void collectInto(NodeList collectionList, String filter)
            {
            }

            public String toHtml()
            {
                return null;
            }

            public String toPlainTextString()
            {
                return null;
            }

            public String toString()
            {
                return "";
            }
        };
        return node;
    }

    private void createTestDataAndPutInVector(int nodeCount)
    {
        testNodes = new Node[nodeCount];
        for (int i = 0; i < nodeCount; i++)
        {
            testNodes[i] = createHTMLNodeObject();
            nodeList.add(testNodes[i]);
        }
    }

    private void assertTestDataCouldBeExtractedFromVector(int nodeCount)
    {
        for (int i = 0; i < nodeCount; i++)
        {
            assertTrue(
                "Element " + i + " did not match",
                testNodes[i] == nodeList.elementAt(i));
        }
    }

    public void testToNodeArray()
    {
        createTestDataAndPutInVector(387);
        Node nodes[] = nodeList.toNodeArray();
        assertEquals("Length of array", 387, nodes.length);
        for (int i = 0; i < nodes.length; i++)
            assertNotNull("node " + i + " should not be null", nodes[i]);
    }

    public void testRemove()
    {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        assertEquals("Vector Size", 2, nodeList.size());
        assertTrue("First Element", node1 == nodeList.elementAt(0));
        assertTrue("Second Element", node2 == nodeList.elementAt(1));
        nodeList.remove(1);
        assertEquals("List Size", 1, nodeList.size());
        assertTrue("First Element", node1 == nodeList.elementAt(0));
    }

    public void testRemoveAll()
    {
        Node node1 = createHTMLNodeObject();
        Node node2 = createHTMLNodeObject();
        nodeList.add(node1);
        nodeList.add(node2);
        assertEquals("Vector Size", 2, nodeList.size());
        assertTrue("First Element", node1 == nodeList.elementAt(0));
        assertTrue("Second Element", node2 == nodeList.elementAt(1));
        nodeList.removeAll();
        assertEquals("List Size", 0, nodeList.size());
        assertTrue("First Element", null == nodeList.elementAt(0));
        assertTrue("Second Element", null == nodeList.elementAt(1));
    }

    public static void main(String[] args)
    {
        new junit.awtui.TestRunner().start(
            new String[] { NodeListTest.class.getName()});
    }

}
