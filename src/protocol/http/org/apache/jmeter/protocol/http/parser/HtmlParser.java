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
package org.apache.jmeter.protocol.http.parser;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/****************************************
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author    Michael Stover
 *@created   June 14, 2001
 *@version   1.0
 ***************************************/

public class HtmlParser implements Serializable
{

	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");

	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected static String utfEncodingName;
	private int compilerOptions = Perl5Compiler.CASE_INSENSITIVE_MASK |
			Perl5Compiler.MULTILINE_MASK | Perl5Compiler.READ_ONLY_MASK;

	private static transient Perl5Compiler compiler = new Perl5Compiler();

	private static transient Perl5Matcher matcher = new Perl5Matcher();

	/****************************************
	 * Constructor for the HtmlParser object
	 ***************************************/

	public HtmlParser()
	{
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param newLink                        !ToDo (Parameter description)
	 *@param config                         !ToDo (Parameter description)
	 *@return                               !ToDo (Return description)
	 *@exception MalformedPatternException  !ToDo (Exception description)
	 ***************************************/
	public static synchronized boolean isAnchorMatched(HTTPSampler newLink, 
			HTTPSampler config) throws MalformedPatternException
	{
		boolean ok = true;

		Iterator iter = config.getArguments().iterator();

		// In JDK1.2, URLDecoder.decode has Exception in its throws clause. However, it
		// was removed in JDK1.3. Since JMeter is JDK1.2-compatible, we need to catch
		// Exception.
		String query = null;
		try
		{
			query = URLDecoder.decode(newLink.getQueryString());
		}
		catch (Exception e)
		{
			// do nothing. query will remain null.
		}

		if(query == null && config.getArguments().getArgumentCount() > 0)
			return false;

		while(iter.hasNext())
		{
			Argument item = (Argument)iter.next();
			if(query.indexOf(item.getName()+"=") == -1)
			{
				if(!(ok = ok && matcher.contains(query, compiler.compile(item.getName()))))
				{
					return false;
				}
			}
		}

		if(config.getDomain() != null && config.getDomain().length() > 0 &&
				!newLink.getDomain().equals(config.getDomain()))
		{
			if(!(ok = ok && matcher.matches(newLink.getDomain(),
					compiler.compile(config.getDomain()))))
				return false;
		}

		if(!newLink.getPath().equals(config.getPath()) && !matcher.matches(newLink.getPath(), 
				compiler.compile("[/]*" + config.getPath())))
			return false;

		if(!(ok = ok && matcher.matches(newLink.getProtocol(), compiler.compile(config.getProtocol()))))
			return false;

		return ok;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param arg                            !ToDo (Parameter description)
	 *@param patternArg                     !ToDo (Parameter description)
	 *@return                               !ToDo (Return description)
	 *@exception MalformedPatternException  !ToDo (Exception description)
	 ***************************************/
	public static synchronized boolean isArgumentMatched(Argument arg, Argument patternArg) throws MalformedPatternException
	{
		return (arg.getName().equals(patternArg.getName()) || 
				matcher.matches(arg.getName(), compiler.compile(patternArg.getName()))) &&
				(arg.getValue().equals(patternArg.getValue()) || 
				matcher.matches((String)arg.getValue(), compiler.compile((String)patternArg.getValue())));
	}

	/****************************************
	 * Returns <code>tidy</code> as HTML parser
	 *
	 *@return   a <code>tidy</code> HTML parser
	 ***************************************/
	public static Tidy getParser()
	{
		log.debug("Start : getParser1");
		Tidy tidy = new Tidy();
		tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
		tidy.setQuiet(true);
		tidy.setShowWarnings(false);

		if(log.isDebugEnabled())
			log.debug("getParser1 : tidy parser created - " + tidy);

		log.debug("End : getParser1");

		return tidy;
	}

	/****************************************
	 * Returns a node representing a whole xml given an xml document
	 *
	 *@param text              an xml document
	 *@return                  a node representing a whole xml
	 *@exception SAXException  !ToDo (Exception description)
	 ***************************************/
	public static Node getDOM(String text) throws SAXException
	{
		log.debug("Start : getDOM1");

		try
		{
			Node node = getParser().parseDOM(new
					ByteArrayInputStream(text.getBytes(getUTFEncodingName())), null);

			if(log.isDebugEnabled())
				log.debug("node : " + node);

			log.debug("End : getDOM1");

			return node;
		}
		catch(UnsupportedEncodingException e)
		{
			log.error("getDOM1 : Unsupported encoding exception - " + e);
			log.debug("End : getDOM1");
			throw new RuntimeException("UTF-8 encoding failed");
		}
	}

	/****************************************
	 * Returns the encoding type which is different for different jdks even though
	 * the mean the same thing i.e. UTF8 or UTF-8
	 *
	 *@return   either UTF8 or UTF-8 depending on the jdk version
	 ***************************************/
	public static String getUTFEncodingName()
	{
		log.debug("Start : getUTFEncodingName1");

		if(utfEncodingName == null)
		{
			String versionNum = System.getProperty("java.version");

			if(log.isDebugEnabled())
				log.debug("getUTFEncodingName1 : versionNum - " + versionNum);

			if(versionNum.startsWith("1.1"))
				utfEncodingName = "UTF8";

			else
				utfEncodingName = "UTF-8";

		}

		if(log.isDebugEnabled())
			log.debug("getUTFEncodingName1 : Returning utfEncodingName - " +
					utfEncodingName);

		log.debug("End : getUTFEncodingName1");

		return utfEncodingName;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public static Document createEmptyDoc()
	{
		return new Tidy().createEmptyDocument();
	}

	/****************************************
	 * Create a new URL based on an HREF string plus a contextual URL object. Given
	 * that an HREF string might be of three possible forms, some processing is
	 * required.
	 *
	 *@param parsedUrlString            !ToDo (Parameter description)
	 *@param context                    !ToDo (Parameter description)
	 *@return                           !ToDo (Return description)
	 *@exception MalformedURLException  !ToDo (Exception description)
	 ***************************************/
	public static HTTPSampler createUrlFromAnchor(String parsedUrlString, HTTPSampler context) throws MalformedURLException
	{
		HTTPSampler url = new HTTPSampler();
		url.setDomain(context.getDomain());
		url.setProtocol(context.getProtocol());
		url.setPort(context.getPort());

		// In JDK1.3, we can get the path using getPath(). However, in JDK1.2, we have to parse
		// the file to obtain the path. In the source for JDK1.3.1, they determine the path to
		// be from the start of the file up to the LAST question mark (if any).
		String contextPath = null;
		String contextFile = context.getPath();
		int indexContextQuery = contextFile.lastIndexOf('?');
		if(indexContextQuery != -1)
			contextPath = contextFile.substring(0, indexContextQuery);

		else
			contextPath = contextFile;

		int queryStarts = parsedUrlString.indexOf("?");

		if(queryStarts == -1)
			queryStarts = parsedUrlString.length();

		if(parsedUrlString.startsWith("/"))
			url.setPath(parsedUrlString.substring(0, queryStarts));

		else if(parsedUrlString.startsWith(".."))
			url.setPath(contextPath.substring(0, contextPath.substring(0,
					contextPath.lastIndexOf("/")).lastIndexOf("/")) +
					parsedUrlString.substring(2, queryStarts));

		else if(!parsedUrlString.toLowerCase().startsWith("http"))
			url.setPath(contextPath.substring(0, contextPath.lastIndexOf("/")) +
					"/" + parsedUrlString.substring(0, queryStarts));

		else
		{
			URL u = new URL(parsedUrlString);

			// Determine the path. (See JDK1.2/1.3 comment above.)
			String uPath = null;
			String uFile = u.getFile();
			int indexUQuery = uFile.lastIndexOf('?');
			if(indexUQuery != -1)
				uPath = uFile.substring(0, indexUQuery);

			else
				uPath = uFile;

			url.setPath(uPath);
			url.setDomain(u.getHost());
			url.setProtocol(u.getProtocol());
			url.setPort(u.getPort());
		}

		if(queryStarts < parsedUrlString.length())
			url.parseArguments(parsedUrlString.substring(queryStarts + 1));

		return url;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param context                    !ToDo (Parameter description)
	 *@param doc                        !ToDo (Parameter description)
	 *@return                           !ToDo (Return description)
	 ***************************************/

	public static List createURLFromForm(Node doc, HTTPSampler context)
	{
		String selectName = null;
		LinkedList urlConfigs = new LinkedList();
		recurseForm(doc, urlConfigs, context, selectName, false);
		/*
		 * NamedNodeMap atts = formNode.getAttributes();
		 * if(atts.getNamedItem("action") == null)
		 * {
		 * throw new MalformedURLException();
		 * }
		 * String action = atts.getNamedItem("action").getNodeValue();
		 * UrlConfig url = createUrlFromAnchor(action, context);
		 * recurseForm(doc, url, selectName,true,formStart);
		 */
		return urlConfigs;
	}

	private static boolean recurseForm(Node tempNode, LinkedList urlConfigs, HTTPSampler context,
			String selectName, boolean inForm)
	{
		NamedNodeMap nodeAtts = tempNode.getAttributes();
		String tag = tempNode.getNodeName();
		try
		{
			if(inForm)
			{
				HTTPSampler url = (HTTPSampler)urlConfigs.getLast();
				if(tag.equalsIgnoreCase("form"))
				{
					try
					{
						urlConfigs.add(createFormUrlConfig(tempNode, context));
					}
					catch(MalformedURLException e)
					{
						inForm = false;
					}
				}
				else if(tag.equalsIgnoreCase("input"))
				{
					url.addArgument(getAttributeValue(nodeAtts, "name"),
							getAttributeValue(nodeAtts, "value"));
				}

				else if(tag.equalsIgnoreCase("textarea"))
					try
					{
						url.addArgument(getAttributeValue(nodeAtts, "name"),
								tempNode.getFirstChild().getNodeValue());
					}
					catch(NullPointerException e)
					{
						url.addArgument(getAttributeValue(nodeAtts, "name"), "");
					}

				else if(tag.equalsIgnoreCase("select"))
					selectName = getAttributeValue(nodeAtts, "name");

				else if(tag.equalsIgnoreCase("option"))
				{
					String value = getAttributeValue(nodeAtts, "value");
					if(value == null)
					{
						try
						{
							value = tempNode.getFirstChild().getNodeValue();
						}
						catch(NullPointerException e)
						{
							value = "";
						}
					}
					url.addArgument(selectName, value);
				}
			}
			else if(tag.equalsIgnoreCase("form"))
			{
				try
				{
					urlConfigs.add(createFormUrlConfig(tempNode, context));
					inForm = true;
				}
				catch(MalformedURLException e)
				{
					inForm = false;
				}
				try{Thread.sleep(5000);}catch(Exception e){}
			}
		}
		catch(Exception ex)
		{
			System.out.println("Some bad HTML " + printNode(tempNode));
		}
		NodeList childNodes = tempNode.getChildNodes();
		for(int x = 0; x < childNodes.getLength(); x++)
		{
			inForm = recurseForm(childNodes.item(x), urlConfigs, context, selectName, inForm);
		}
		return inForm;
	}

	private static String getAttributeValue(NamedNodeMap att, String attName)
	{
		try
		{
			return att.getNamedItem(attName).getNodeValue();
		}
		catch(Exception ex)
		{
			return "";
		}
	}

	private static String printNode(Node node)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("<");
		buf.append(node.getNodeName());
		NamedNodeMap atts = node.getAttributes();
		for(int x = 0; x < atts.getLength(); x++)
		{
			buf.append(" ");
			buf.append(atts.item(x).getNodeName());
			buf.append("=\"");
			buf.append(atts.item(x).getNodeValue());
			buf.append("\"");
		}

		buf.append(">");

		return buf.toString();
	}

	/****************************************
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	public static class Test extends TestCase
	{
		private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.test");

		/****************************************
		 * !ToDo (Constructor description)
		 *
		 *@param name  !ToDo (Parameter description)
		 ***************************************/
		public Test(String name)
		{
			super(name);
		}

		/****************************************
		 * !ToDo
		 ***************************************/
		public void testGetUTFEncodingName()
		{
			log.debug("Start : testGetUTFEncodingName1");
			String javaVersion = System.getProperty("java.version");
			utfEncodingName = null;
			System.setProperty("java.version", "1.1");
			assertEquals("UTF8", HtmlParser.getUTFEncodingName());
			// need to clear utfEncodingName variable first 'cos
			// getUTFEncodingName checks to see if it's null
			utfEncodingName = null;
			System.setProperty("java.version", "1.2");
			assertEquals("UTF-8", HtmlParser.getUTFEncodingName());
			System.setProperty("java.version", javaVersion);
			log.debug("End : testGetUTFEncodingName1");
		}

		/****************************************
		 * !ToDo
		 ***************************************/
		protected void setUp()
		{
		}
	}

	private static HTTPSampler createFormUrlConfig(Node tempNode, HTTPSampler context) throws
			MalformedURLException
	{
		NamedNodeMap atts = tempNode.getAttributes();
		if(atts.getNamedItem("action") == null)
			throw new MalformedURLException();
		String action = atts.getNamedItem("action").getNodeValue();
		HTTPSampler url = createUrlFromAnchor(action, context);
		return url;
	}
}
