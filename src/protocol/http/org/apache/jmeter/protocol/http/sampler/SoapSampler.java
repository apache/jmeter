package org.apache.jmeter.protocol.http.sampler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;


/**
 * Sampler to handle SOAP Requests
 *
 *
 * @author Jordi Salvat i Alabart
 * @version $Id$
 */
public class SoapSampler extends HTTPSampler
{
	public static final String XML_DATA = "HTTPSamper.xml_data";

	public void setXmlData(String data)
	{
		setProperty(XML_DATA,data);
	}

	public String getXmlData()
	{
		return getPropertyAsString(XML_DATA);
	}

	/****************************************
	 * Set the HTTP request headers in preparation to open the connection
	 * and sending the POST data:
	 *
	 *@param connection       <code>URLConnection</code> to set headers on
	 *@exception IOException  if an I/O exception occurs
	 ***************************************/
	public void setPostHeaders(URLConnection connection)
	  		throws IOException
	{
		((HttpURLConnection)connection).setRequestMethod("POST");
		connection.setRequestProperty("Content-length", "" + getXmlData().length());
		connection.setRequestProperty("Content-type", "text/xml");
		connection.setDoOutput(true);
	}

	/****************************************
	 * Send POST data from <code>Entry</code> to the open connection.
	 *
	 *@param connection       <code>URLConnection</code> of where POST data should
	 *      be sent
	 *@param url              contains the query string for POST
	 *@exception IOException  if an I/O exception occurs
	 ***************************************/
	public void sendPostData(URLConnection connection)
			 throws IOException
	{
		PrintWriter out = new PrintWriter(connection.getOutputStream());
		out.print(getXmlData());
		out.close();
	}
}

