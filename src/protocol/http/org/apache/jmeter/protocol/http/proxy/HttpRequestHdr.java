package org.apache.jmeter.protocol.http.proxy;
/****************************************
 * File HttpRequestHdr.java
 ***************************************/
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.jmeter.protocol.http.config.MultipartUrlConfig;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;

//
// Class:     HttpRequestHdr
// Abstract:  The headers of the client HTTP request.
//

/****************************************
 * !ToDo (Class description)
 *
 *@author    $Author$
 *@created   $Date$
 *@version   $Revision$
 ***************************************/
public class HttpRequestHdr
{

	/****************************************
	 * Http Request method. Such as get or post.
	 ***************************************/
	public String method = new String();

	/****************************************
	 * The requested url. The universal resource locator that hopefully uniquely
	 * describes the object or service the client is requesting.
	 ***************************************/
	public String url = new String();

	/****************************************
	 * Version of http being used. Such as HTTP/1.0
	 ***************************************/
	public String version = new String();



	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public String postData = "";

	static String CR = "\r\n";

	private Map headers = new HashMap();


	/****************************************
	 * Parses a http header from a stream.
	 *
	 *@param in  The stream to parse.
	 *@return    Array of bytes from client.
	 ***************************************/
	public byte[] parse(InputStream in) throws IOException
	{
		boolean inHeaders = true;
		int readLength = 0;
		int dataLength = 0;
		String CR = "\r\n";
		boolean first = true;
		ByteArrayOutputStream clientRequest = new ByteArrayOutputStream();
		ByteArrayOutputStream line = new ByteArrayOutputStream();
		int x;
		while((inHeaders || readLength < dataLength) && ((x = in.read()) != -1))
		{
			line.write(x);
			clientRequest.write(x);
			if(inHeaders && (byte)x == (byte)'\n')
			{
				if(line.size() < 3)
				{
					inHeaders = false;
				}
				if(first)
				{
					parseFirstLine(line.toString());
					first = false;
				}
				else
				{
					dataLength = Math.max(parseLine(line.toString()),dataLength);
				}
				line.reset();
			}
			else if(!inHeaders)
			{
				readLength++;
			}
		}
		postData = line.toString().trim();
		return clientRequest.toByteArray();
	}

	public void parseFirstLine(String firstLine)
	{
		StringTokenizer tz = new StringTokenizer(firstLine);
		method = getToken(tz).toUpperCase();
		url = getToken(tz);
		version = getToken(tz);
	}

	public int parseLine(String nextLine)
	{
		StringTokenizer tz;
		tz = new StringTokenizer(nextLine);
		String token = getToken(tz);
		// look for termination of HTTP command
		if(0 == token.length())
		{
			return 0;
		}
		else
		{
			String name = token.trim().substring(0,token.trim().length()-1);
			String value = getRemainder(tz);
			headers.put(name,value);
			if(name.equalsIgnoreCase("content-length"))
			{
				return Integer.parseInt(value);
			}
		}
		return 0;
	}
	
	public HeaderManager getHeaderManager()
	{
		HeaderManager manager = new HeaderManager();
		Iterator keys = headers.keySet().iterator();
		while(keys.hasNext())
		{
			String key = (String)keys.next();
			if(!key.equalsIgnoreCase("proxy-connection") && !key.equalsIgnoreCase("content-length"))
			{
				Header h = new Header(key,(String)headers.get(key));
				manager.add(h);
			}
		}
		manager.setName("Browser-derived headers");
		manager.setProperty(TestElement.TEST_CLASS,HeaderManager.class.getName());
		manager.setProperty(TestElement.GUI_CLASS,HeaderPanel.class.getName());
		return  manager;
	}

	public HTTPSampler getSampler() throws MalformedURLException,IOException,ProtocolException
	{
		HttpTestSampleGui tempGui = new HttpTestSampleGui();
		tempGui.configure(createSampler());
		HTTPSampler result = (HTTPSampler)tempGui.createTestElement();
		result.setFollowRedirects(false);
		result.setUseKeepAlive(true);
		return result;
	}

	public String getContentType()
	{
		String contentType = (String)headers.get("Content-Type");
		if(contentType == null)
		{
			contentType = (String)headers.get("Content-type");
		}
		if(contentType == null)
		{
			contentType = (String)headers.get("content-type");
		}
		return contentType;
	}
	
	public static MultipartUrlConfig isMultipart(String contentType)
	{
		if(contentType != null && contentType.startsWith(MultipartUrlConfig.MULTIPART_FORM))
		{
			return new MultipartUrlConfig(contentType.substring(contentType.indexOf("oundary=")+8));
		}
		else
		{
			return null;
		}
	}

	private HTTPSampler createSampler()
	{
		MultipartUrlConfig urlConfig = null;
		HTTPSampler sampler = new HTTPSampler();
		sampler.setDomain(serverName());
		sampler.setMethod(method);
		sampler.setPath(serverUrl());
		sampler.setName(sampler.getPath());
		if(url.indexOf(":") > -1)
		{
			sampler.setProtocol(url.substring(0, url.indexOf(":")));
		}
		else
		{
			sampler.setProtocol("http");
		}
		sampler.setPort(serverPort());
		if((urlConfig = isMultipart(getContentType())) != null)
		{
			urlConfig.parseArguments(postData);
			sampler.setArguments(urlConfig.getArguments());
			sampler.setFileField(urlConfig.getFileFieldName());
			sampler.setFilename(urlConfig.getFilename());
			sampler.setMimetype(urlConfig.getMimeType());
		}
		else
		{
			sampler.parseArguments(postData);
		}
		return sampler;
	 }

	//
	// Parsing Methods
	//

	/****************************************
	 * Find the //server.name from an url.
	 *
	 *@return   Servers internet name
	 ***************************************/
	public String serverName()
	{
		// chop to "server.name:x/thing"
		String str = url;
		int i = str.indexOf("//");
		if(i < 0)
		{
			return "";
		}
		str = str.substring(i + 2);

		// chop to  server.name:xx
		i = str.indexOf("/");
		if(0 < i)
		{
			str = str.substring(0, i);
		}

		// chop to server.name
		i = str.indexOf(":");
		if(0 < i)
		{
			str = str.substring(0, i);
		}

		return str;
	}

	/****************************************
	 * Find the :PORT form http://server.ect:PORT/some/file.xxx
	 *
	 *@return   Servers internet name
	 ***************************************/
	public int serverPort()
	{
		String str = url;
		// chop to "server.name:x/thing"
		int i = str.indexOf("//");
		if(i < 0)
		{
			return 80;
		}
		str = str.substring(i + 2);

		// chop to  server.name:xx
		i = str.indexOf("/");
		if(0 < i)
		{
			str = str.substring(0, i);
		}

		// chop XX
		i = str.indexOf(":");
		if(0 < i)
		{
			return Integer.parseInt(str.substring(i + 1).trim());
		}

		return 80;
	}

	/****************************************
	 * Find the /some/file.xxxx form http://server.ect:PORT/some/file.xxx
	 *
	 *@return   the deproxied url
	 ***************************************/
	public String serverUrl()
	{
		String str = url;
		int i = str.indexOf("//");
		if(i < 0)
		{
			return str;
		}

		str = str.substring(i + 2);
		i = str.indexOf("/");
		if(i < 0)
		{
			return str;
		}

		return str.substring(i);
	}

	/****************************************
	 * Returns the next token in a string
	 *
	 *@param tk  String that is partially tokenized.
	 *@return    !ToDo (Return description)
	 *@returns   The remainder
	 ***************************************/
	String getToken(StringTokenizer tk)
	{
		String str = "";
		if(tk.hasMoreTokens())
		{
			str = tk.nextToken();
		}
		return str;
	}

	/****************************************
	 * Returns the remainder of a tokenized string
	 *
	 *@param tk  String that is partially tokenized.
	 *@return    !ToDo (Return description)
	 *@returns   The remainder
	 ***************************************/
	String getRemainder(StringTokenizer tk)
	{
		String str = "";
		if(tk.hasMoreTokens())
		{
			str = tk.nextToken();
		}
		while(tk.hasMoreTokens())
		{
			str += " " + tk.nextToken();
		}

		return str;
	}

}
