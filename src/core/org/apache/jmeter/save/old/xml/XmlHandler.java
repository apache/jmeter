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
package org.apache.jmeter.save.old.xml;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.util.ListedHashTree;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/************************************************************
 *  Title: Description: Copyright: Copyright (c) 2000 Company:
 *
 *@author
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class XmlHandler extends DefaultHandler
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.util");
	protected LinkedList objectStack;

	NameSpaceHandler informer;
	List finalHandlers;
	ListedHashTree objectHierarchy = new ListedHashTree();
	boolean replaced = false;

	/************************************************************
	 *  !ToDo (Constructor description)
	 *
	 *@param  informer  !ToDo (Parameter description)
	 ***********************************************************/
	public XmlHandler(NameSpaceHandler informer)
	{
		this.informer = informer;
		objectStack = new LinkedList();
		finalHandlers = new LinkedList();
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public ListedHashTree getDataTree()
	{
		if (!replaced)
		{
			replaceWithModels(objectHierarchy);
		}
		replaced = true;
		return objectHierarchy;
	}

	/************************************************************
	 *  HandlerBase implementation This reports an error that has occured. This
	 *  indicates that a rule was broken in validation, but that parsing can
	 *  continue.
	 *
	 *@param  e                 !ToDo (Parameter description)
	 *@exception  SAXException  !ToDo (Exception description)
	 ***********************************************************/
	public void fatalError(SAXParseException e) throws SAXException
	{
		log.error("***Parsing Fatal Error**\n" +
				" Line:   " + e.getLineNumber() + "\n" +
				" URI:    " + e.getSystemId() + "\n" +
				" Message: " + e.getMessage());
		throw new SAXException("Fatal Error encountered");
	}

	/************************************************************
	 *  HandlerBase implementation This reports a fatal error that has occured.
	 *  This indicates that a rule was broken that makes continued parsing
	 *  impossible.
	 *
	 *@param  errs              parse error message
	 *@exception  SAXException  !ToDo (Exception description)
	 ***********************************************************/
	public void error(SAXParseException errs) throws SAXException
	{
		log.error("***Parsing Error**\n" +
				" Line:   " + errs.getLineNumber() + "\n" +
				" URI:    " + errs.getSystemId() + "\n" +
				" Message: " + errs.getMessage());
		throw new SAXException("Error encountered");
	}

	/************************************************************
	 *  HandlerBase implementation This reports a warning that has occured; this
	 *  indicates that while no XML rules were broken, something appears to be
	 *  incorrect or missing.
	 *
	 *@param  err               parse warning message
	 *@exception  SAXException  !ToDo (Exception description)
	 ***********************************************************/
	public void warning(SAXParseException err) throws SAXException
	{
		log.warn("***Parsing Warning**\n" +
				" Line:   " + err.getLineNumber() + "\n" +
				" URI:    " + err.getSystemId() + "\n" +
				" Message: " + err.getMessage());
		throw new SAXException("Warning encountered");
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  chardata          !ToDo (Parameter description)
	 *@param  start             !ToDo (Parameter description)
	 *@param  end               !ToDo (Parameter description)
	 *@exception  SAXException  !ToDo (Exception description)
	 ***********************************************************/
	public void characters(char[] chardata, int start, int end) throws SAXException
	{
		try
		{
			if(start >= 0 && start < chardata.length && (end + start) <= chardata.length)
			{
				getCurrentHandler().handleData(new String(chardata, start, end));
			}
		}
		catch (Exception e)
		{
			log.error("",e);
		}
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  uri        !ToDo (Parameter description)
	 *@param  localName  !ToDo (Parameter description)
	 *@param  qName      !ToDo (Parameter description)
	 *@param  atts       !ToDo (Parameter description)
	 ***********************************************************/
	public void startElement(String uri, String localName, String qName, Attributes atts)
	{
		documentElement(localName+":"+qName, atts);
		TagHandler newXmlObject = informer.getXmlObject(qName, atts);
		if (newXmlObject == null)
		{
			try
			{
				getCurrentHandler().passToHandler(qName, atts);
			}
			catch (Exception ex)
			{
				log.debug("No current Handler for " + qName);
			}
		}
		else
		{
			newXmlObject.setXmlParent(this);
			objectHierarchy.add(objectStack, newXmlObject);
			objectStack.addLast(newXmlObject);
			try
			{
				getCurrentHandler().passToHandler(qName, atts);
			}
			catch (Exception ex)
			{
				log.debug("(2)No current Handler for " + qName);
			}
		}
	}

	/************************************************************
	 *  HandlerBase implementation
	 *
	 *@param  uri               !ToDo (Parameter description)
	 *@param  localName         !ToDo (Parameter description)
	 *@param  qName             !ToDo (Parameter description)
	 *@exception  SAXException  !ToDo (Exception description)
	 ***********************************************************/
	public void endElement(java.lang.String uri, java.lang.String localName,
			java.lang.String qName) throws SAXException
	{
		try
		{
			getCurrentHandler().passToHandler(qName);
		}
		catch (Exception ex)
		{
			//log.error("",ex);
		}
		if (currentHandlerIsDone())
		{
			TagHandler currentHandler = (TagHandler)objectStack.removeLast();
			try
			{
				TagHandler previousHandler = (TagHandler)objectStack.getLast();
				previousHandler.notifySubElementEnded(currentHandler.getModel());
			}
			catch(Exception e)
			{
			}
		}
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  handler  !ToDo (Parameter description)
	 *@return          !ToDo (Return description)
	 ***********************************************************/
	public List takeChildObjects(TagHandler handler)
	{
		List stack = new LinkedList();
		Iterator iter = objectStack.iterator();
		while (iter.hasNext())
		{
			Object item = iter.next();
			stack.add(item);
			if (item.equals(handler))
			{
				break;
			}
		}
		ListedHashTree subTree = objectHierarchy.get(stack);
		if (subTree == null)
		{
			return new LinkedList();
		}
		List items = new LinkedList(subTree.list());
		iter = items.iterator();
		while (iter.hasNext())
		{
			Object item = iter.next();
			replaceSubItems(subTree, item);
			subTree.remove(item);
		}
		return items;
	}


	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@param  tagName  !ToDo (Parameter description)
	 *@param  atts     !ToDo (Parameter description)
	 *@return          !ToDo (Return description)
	 ***********************************************************/
	protected TagHandler getXmlObject(String tagName, Attributes atts)
	{
		return informer.getXmlObject(tagName, atts);
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return                                !ToDo (Return description)
	 *@exception  NoCurrentHandlerException  !ToDo (Exception description)
	 ***********************************************************/
	protected TagHandler getCurrentHandler() throws NoCurrentHandlerException
	{
		if (objectStack.size() == 0)
		{
			throw new NoCurrentHandlerException();
		}
		else
		{
			return (TagHandler)objectStack.getLast();
		}
	}

	private void replaceWithModels(ListedHashTree tree)
	{
		Iterator iter = new LinkedList(tree.list()).iterator();
		while (iter.hasNext())
		{
			TagHandler item = (TagHandler)iter.next();
			tree.replace(item,item.getModel());
			replaceWithModels(tree.get(item.getModel()));
		}
	}

	private boolean currentHandlerIsDone()
	{
		try
		{
			return getCurrentHandler().isDone();
		}
		catch (NoCurrentHandlerException e)
		{
			return false;
		}
	}

	private void documentElement(String name, Attributes atts)
	{
		if(log.isDebugEnabled())
		{
			log.debug("startElement= " + name);
			for (int i = 0; i < atts.getLength(); i++)
			{
				log.debug(" Attribute= " + atts.getQName(i) + "=" + atts.getValue(i));
			}
		}
	}

	private void replaceSubItems(ListedHashTree subTree, Object item)
	{
		ListedHashTree subSubTree = subTree.get(item);
		List subItems = subSubTree.list();
		Iterator iter2 = subItems.iterator();
		while (iter2.hasNext())
		{
			Object subItem = iter2.next();
			subTree.set(subItem, subSubTree.get(subItem));
		}
	}
}
