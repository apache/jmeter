package org.apache.jmeter.protocol.http.sampler;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Sampler to handle SOAP Requests.
 *
 * @author Jordi Salvat i Alabart
 * @version $Id$
 */
public class SoapSampler extends HTTPSampler
{
    private static Logger log = LoggingManager.getLoggerForClass();
    public static final String XML_DATA = "HTTPSamper.xml_data";
    public static final String URL_DATA = "SoapSampler.URL_DATA";

    public void setXmlData(String data)
    {
        setProperty(XML_DATA, data);
    }

    public String getXmlData()
    {
        return getPropertyAsString(XML_DATA);
    }

    public String getURLData()
    {
        return getPropertyAsString(URL_DATA);
    }

    public void setURLData(String url)
    {
        setProperty(URL_DATA, url);
    }

    /**
     * Set the HTTP request headers in preparation to open the connection
     * and sending the POST data.
     *
     * @param connection       <code>URLConnection</code> to set headers on
     * @exception IOException  if an I/O exception occurs
     */
    public void setPostHeaders(URLConnection connection) throws IOException
    {
        ((HttpURLConnection) connection).setRequestMethod("POST");
        connection.setRequestProperty(
            "Content-length",
            "" + getXmlData().length());
        // if the user provides a different content type, we use it
        // added 1-15-04 peter lin
        if (getHeaderManager().getPropertyAsString(
        	HTTPSampler.CONTENT_TYPE).length() > 0){
        		// headerManager was set, so let's set the connection
        		// to use it.
        		connection.setRequestProperty("Content-type",
        		getHeaderManager().getPropertyAsString(
        			HTTPSampler.CONTENT_TYPE));
        } else {
        	// otherwise we use "text/xml" as the default
			connection.setRequestProperty("Content-type", "text/xml");
        }
        connection.setDoOutput(true);
    }

    /**
     * Send POST data from <code>Entry</code> to the open connection.
     *
     * @param connection      <code>URLConnection</code> of where POST data
     *                        should be sent
     * @exception IOException if an I/O exception occurs
     */
    public void sendPostData(URLConnection connection) throws IOException
    {
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print(getXmlData());
        out.close();
    }
    
    /* (non-Javadoc)
     * @see Sampler#sample(Entry)
     */
    public SampleResult sample(Entry e)
    {
        try
        {
            URL url = new URL(getURLData());
            setDomain(url.getHost());
            setPort(url.getPort());
            setProtocol(url.getProtocol());
            setMethod(POST);
            if (url.getQuery() != null && url.getQuery().compareTo("") != 0)
            {
                setPath(url.getPath() + "?" + url.getQuery());
            }
            else
            {
                setPath(url.getPath());
            }
        }
        catch (MalformedURLException e1)
        {
            log.error("Bad url: " + getURLData(), e1);
        }
        return super.sample(e);
    }

    public String toString()
    {
        try
        {
            String xml = getXmlData();
            if (xml.length() > 100)
            {
                xml = xml.substring(0, 100);
            }
            return this.getUrl().toString() + "\nXML Data: " + xml;
        }
        catch (MalformedURLException e)
        {
            return "";
        }
    }
}
