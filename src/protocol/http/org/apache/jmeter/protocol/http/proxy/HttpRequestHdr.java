// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.http.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.TestCase;

import org.apache.jmeter.protocol.http.config.MultipartUrlConfig;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The headers of the client HTTP request.
 *
 * @version   $Revision$
 */
public class HttpRequestHdr
{
    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Http Request method. Such as get or post.
     */
    public String method = "";

    /**
     * The requested url. The universal resource locator that hopefully uniquely
     * describes the object or service the client is requesting.
     */
    public String url = "";

    /**
     * Version of http being used. Such as HTTP/1.0.
     */
    public String version = "";

    public String postData = "";
    static String CR = "\r\n";
    private Map headers = new HashMap();

    /*
     * Optionally number the requests
     */    
    private static boolean numberRequests
	    = JMeterUtils.getPropDefault("proxy.number.requests",false);
    private static int requestNumber = 0 ;// running number
 
    /**
     * Parses a http header from a stream.
     *
     * @param in  the stream to parse.
     * @return    array of bytes from client.
     */
    public byte[] parse(InputStream in) throws IOException
    {
        boolean inHeaders = true;
        int readLength = 0;
        int dataLength = 0;
        boolean first = true;
        ByteArrayOutputStream clientRequest = new ByteArrayOutputStream();
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int x;
        while ((inHeaders || readLength < dataLength)
            && ((x = in.read()) != -1))
        {
            line.write(x);
            clientRequest.write(x);
            if (inHeaders && (byte) x == (byte) '\n')
            {
                if (line.size() < 3)
                {
                    inHeaders = false;
                }
                if (first)
                {
                    parseFirstLine(line.toString());
                    first = false;
                }
                else
                {
                    dataLength =
                        Math.max(parseLine(line.toString()), dataLength);
                }
                log.debug("Client Request Line: " + line.toString());
                line.reset();
            }
            else if (!inHeaders)
            {
                readLength++;
            }
        }
        postData = line.toString().trim();
		log.debug("postData: " + postData);
		log.debug("Request: "+clientRequest.toString());
        return clientRequest.toByteArray();
    }
    
    public void parseFirstLine(String firstLine)
    {
        log.debug("browser request: " + firstLine);
        StringTokenizer tz = new StringTokenizer(firstLine);
        method = getToken(tz).toUpperCase();
        url = getToken(tz);
        log.debug("parsed url: " + url);
        version = getToken(tz);
    }
    
    public int parseLine(String nextLine)
    {
        StringTokenizer tz;
        tz = new StringTokenizer(nextLine);
        String token = getToken(tz);
        // look for termination of HTTP command
        if (0 == token.length())
        {
            return 0;
        }
        else
        {
            String name = token.trim().substring(0, token.trim().length() - 1);
            String value = getRemainder(tz);
            headers.put(name.toLowerCase(), new Header(name, value));
            if (name.equalsIgnoreCase("content-length"))
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
        while (keys.hasNext())
        {
            String key = (String) keys.next();
            if (!key.equals("proxy-connection")
                && !key.equals("content-length"))
            {
                manager.add((Header) headers.get(key));
            }
        }
        manager.setName("Browser-derived headers");
        manager.setProperty(
            TestElement.TEST_CLASS,
            HeaderManager.class.getName());
        manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        return manager;
    }
    
    public HTTPSampler getSampler()
        throws MalformedURLException, IOException, ProtocolException
    {
    	// Damn! A whole new GUI just to instantiate a test element?
    	// Isn't there a beter way? 
        HttpTestSampleGui tempGui = new HttpTestSampleGui();
        HTTPSampler result = createSampler();
        tempGui.configure(result);
        tempGui.modifyTestElement(result);
        result.setFollowRedirects(false);
        result.setUseKeepAlive(true);
        return result;
    }
    
    public String getContentType()
    {
        Header contentTypeHeader = (Header) headers.get("content-type");
        if (contentTypeHeader != null)
        {
            return contentTypeHeader.getValue();
        }
        return "";
    }
    
    public static MultipartUrlConfig isMultipart(String contentType)
    {
        if (contentType != null
            && contentType.startsWith(MultipartUrlConfig.MULTIPART_FORM))
        {
            return new MultipartUrlConfig(
                contentType.substring(contentType.indexOf("oundary=") + 8));
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
        log.debug("Proxy: setting server: " + sampler.getDomain());
        sampler.setMethod(method);
        log.debug("Proxy: method server: " + sampler.getMethod());
        sampler.setPath(serverUrl());
        log.debug("Proxy: setting path: " + sampler.getPath());
        if (numberRequests){
        	requestNumber++;
			sampler.setName(requestNumber + " " + sampler.getPath());
        } else {
			sampler.setName(sampler.getPath());
        }
        sampler.setPort(serverPort());
        log.debug("Proxy: setting port: " + sampler.getPort());
        if (url.indexOf("//") > -1)
        {
            String protocol = url.substring(0, url.indexOf(":"));
            log.debug("Proxy: setting protocol to : " + protocol);
            sampler.setProtocol(protocol);
        }
        else if (sampler.getPort() == 443)
        {
            sampler.setProtocol("https");
            log.debug("Proxy: setting protocol to https");
        }
        else
        {
            log.debug("Proxy setting default protocol to: http");
            sampler.setProtocol("http");
        }
        if ((urlConfig = isMultipart(getContentType())) != null)
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

    /**
     * Find the //server.name from an url.
     *
     * @return   server's internet name
     */
    public String serverName()
    {
        // chop to "server.name:x/thing"
        String str = url;
        int i = str.indexOf("//");
        if (i > 0)
        {
            str = str.substring(i + 2);
        }
        // chop to  server.name:xx
        i = str.indexOf("/");
        if (0 < i)
        {
            str = str.substring(0, i);
        }
        // chop to server.name
        i = str.indexOf(":");
        if (0 < i)
        {
            str = str.substring(0, i);
        }
        return str;
    }

    /**
     * Find the :PORT form http://server.ect:PORT/some/file.xxx
     *
     * @return   server's port
     */
    public int serverPort()
    {
        String str = url;
        // chop to "server.name:x/thing"
        int i = str.indexOf("//");
        if (i > 0)
        {
            str = str.substring(i + 2);
        }
        // chop to  server.name:xx
        i = str.indexOf("/");
        if (0 < i)
        {
            str = str.substring(0, i);
        }
        // chop XX
        i = str.indexOf(":");
        if (0 < i)
        {
            return Integer.parseInt(str.substring(i + 1).trim());
        }
        return 80;
    }
    
    /**
     * Find the /some/file.xxxx form http://server.ect:PORT/some/file.xxx
     *
     * @return   the deproxied url
     */
    public String serverUrl()
    {
        String str = url;
        int i = str.indexOf("//");
        if (i > 0)
        {
            str = str.substring(i + 2);
        }
        i = str.indexOf("/");
        if (i < 0)
        {
            return "";
        }
        return str.substring(i);
    }
    
    /**
     * Returns the next token in a string.
     *
     * @param tk  String that is partially tokenized.
     * @return   The remainder
     */
    String getToken(StringTokenizer tk)
    {
        String str = "";
        if (tk.hasMoreTokens())
        {
            str = tk.nextToken();
        }
        return str;
    }
    
    /**
     * Returns the remainder of a tokenized string.
     *
     * @param tk  String that is partially tokenized.
     * @return   The remainder
     */
    String getRemainder(StringTokenizer tk)
    {
        String str = "";
        if (tk.hasMoreTokens())
        {
            str = tk.nextToken();
        }
        while (tk.hasMoreTokens())
        {
            str += " " + tk.nextToken();
        }
        return str;
    }

    public static class Test extends TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void setUp()
        {
        }

        public void testRepeatedArguments() throws Exception
        {
            String TEST_REQ =
                "GET http://localhost/matrix.html?"
                    + "update=yes&d=1&d=2&d=&d=&d=&d=&d=&d=1&d=2&d=1&d=" +
                        "&d= HTTP/1.0\n\n";
            HttpRequestHdr req = new HttpRequestHdr();
            req.parse(new java.io.ByteArrayInputStream(TEST_REQ.getBytes()));
            HTTPSampler s = req.getSampler();
            assertEquals(s.getArguments().getArguments().size(), 13);
        }
    }
}
