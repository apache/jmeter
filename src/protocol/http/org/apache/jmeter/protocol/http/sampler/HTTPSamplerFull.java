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
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.jmeter.protocol.http.parser.*;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler that downloads downloadable components such as images, applets,
 * etc.
 * <p>
 * For HTML files, this class will download binary files specified in the
 * following ways (where <b>url</b> represents the binary file to be
 * downloaded):
 * <ul>
 *  <li>&lt;img src=<b>url</b> ... &gt;
 *  <li>&lt;applet code=<b>url</b> ... &gt;
 *  <li>&lt;input type=image src=<b>url</b> ... &gt;
 *  <li>&lt;body background=<b>url</b> ... &gt;
 * </ul>
 *
 * Note that files that are duplicated within the enclosing document will
 * only be downloaded once. Also, the processing does not take account of the
 * following parameters:
 * <ul>
 *  <li>&lt;base href=<b>url</b>&gt;
 *  <li>&lt; ... codebase=<b>url</b> ... &gt;
 * </ul>
 *
 * The following parameters are not accounted for either (as the textbooks
 * say, they are left as an exercise for the interested reader):
 * <ul>
 *  <li>&lt;applet ... codebase=<b>url</b> ... &gt;
 *  <li>&lt;area href=<b>url</b> ... &gt;
 *  <li>&lt;embed src=<b>url</b> ... &gt;
 *  <li>&lt;embed codebase=<b>url</b> ... &gt;
 *  <li>&lt;object codebase=<b>url</b> ... &gt;
 *  <li>&lt;table background=<b>url</b> ... &gt;
 *  <li>&lt;td background=<b>url</b> ... &gt;
 *  <li>&lt;tr background=<b>url</b> ... &gt;
 * </ul>
 *
 * Due to the recent refactoring of this class, these might not be as difficult
 * as they once might have been.
 * <p>
 * Finally, this class does not process <b>Style Sheets</b> either.
 *
 * @author  Khor Soon Hin
 * @author  <a href="mailto:mramshaw@alumni.concordia.ca">Martin Ramshaw</a>
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
public class HTTPSamplerFull
{
    /** Used to store the Logger (used for debug and error messages). */
    transient private static Logger log= LoggingManager.getLoggerForClass();

    /**
     * Used to store the UTF encoding name (which is version dependent).
     * @see #getUTFEncodingName
     */
    protected static String utfEncodingName;

    /**
     * This is the only Constructor.
     */
    public HTTPSamplerFull()
    {
        super();
    }

    /**
     * Samples the <code>HTTPSampler</code> passed in and stores the result in
     * <code>SampleResult</code>. The original file (which is assumed to be
     * an HTML file) is parsed into a DOM tree and examined for embedded binary
     * files.
     * <p>
     * Note that files that are duplicated within the enclosing document will
     * only be downloaded once.
     *
     * @param sampler an HTTPSampler to be sampled
     * 
     * @return      results of the sampling
     */
    public SampleResult sample(HTTPSampler sampler)
    {
        // Sample the container page.
        log.debug("Start : HTTPSamplerFull sample");
        SampleResult res= sampler.sample(new Entry());
        if (log.isDebugEnabled())
        {
            log.debug("Main page loading time - " + res.getTime());
        }

        // Now parse the HTML page
        return downloadEmbeddedResources(res, sampler);
    }

    /**
     * @param res - the current sample result
     * @param sampler - the HTTP sampler
     * @return the sample result, with possible additional sub results
     */
    protected SampleResult downloadEmbeddedResources(
        SampleResult res,
        HTTPSampler sampler)
    {
        Iterator urls;
        try
        {
            urls=
                HTMLParser.getParser().getEmbeddedResourceURLs(
                    res.getResponseData(),
                    sampler.getUrl());
        }
        catch (MalformedURLException e)
        {
            // If we're downloading the resources dragged in by the HTML
            // page, I can't see how the URL could be malformed!
            log.error("Program error: can't get sampler URL", e);
            throw new Error(e);
        }
        catch (HTMLParseException e)
        {
            res.setResponseData(e.toString().getBytes());
            res.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
            res.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
            res.setSuccessful(false);
            return res;
        }

        // Iterate through the URLs and download each image:
        while (urls.hasNext())
        {
            SampleResult binRes= new SampleResult();
            Object url= urls.next();
            binRes.setSampleLabel(url.toString());
            try
            {
                HTTPSamplerFull.loadBinary((URL)url, binRes, sampler);
            }
            catch (ClassCastException e)
            {
                binRes.setResponseData(e.toString().getBytes());
                binRes.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
                binRes.setResponseMessage(
                    HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
                binRes.setSuccessful(false);
                continue;
            }
            catch (Exception ioe)
            {
                log.error("Error reading from URL - " + ioe);
                binRes.setResponseData(ioe.toString().getBytes());
                binRes.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
                binRes.setResponseMessage(
                    HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
                binRes.setSuccessful(false);
            }

            log.debug("Adding result");
            res.addSubResult(binRes);
            res.setTime(res.getTime() + binRes.getTime());
        }

        // Okay, we're all done now
        if (log.isDebugEnabled())
        {
            log.debug("Total time - " + res.getTime());
        }
        return res;
    }

    /**
     * Download the binary file from the given <code>URL</code>.
     *
     * @param url   <code>URL</code> from where binary is to be downloaded
     * @param res   <code>SampleResult</code> to store sampling results
     * @return      binary downloaded
     *
     * @throws IOException indicates a problem reading from the URL
     */
    protected static byte[] loadBinary(
        URL url,
        SampleResult res,
        HTTPSampler sampler)
        throws Exception
    {
        log.debug("Start : loadBinary");
        byte[] ret= new byte[0];
        res.setSamplerData(new HTTPSampler(url).toString());
        HttpURLConnection conn;
        try
        {
            conn= sampler.setupConnection(url, HTTPSampler.GET, res);
            sampler.connect();
        }
        catch (Exception ioe)
        {
            // don't do anything 'cos presumably the connection will return the
            // correct http response codes
            if (log.isDebugEnabled())
            {
                log.debug("loadBinary : error in setupConnection " + ioe);
            }
            throw ioe;
        }

        try
        {
            long time= System.currentTimeMillis();
            if (log.isDebugEnabled())
            {
                log.debug("loadBinary : start time - " + time);
            }
            int errorLevel= getErrorLevel(conn, res);
            if (errorLevel == 2)
            {
                ret= sampler.readResponse(conn);
                res.setContentType(conn.getHeaderField("Content-type"));
                res.setSuccessful(true);
                long endTime= System.currentTimeMillis();
                if (log.isDebugEnabled())
                {
                    log.debug("loadBinary : end   time - " + endTime);
                }
                res.setTime(endTime - time);
            }
            else
            {
                res.setSuccessful(false);
                int responseCode= ((HttpURLConnection)conn).getResponseCode();
                String responseMsg=
                    ((HttpURLConnection)conn).getResponseMessage();
                log.error("loadBinary : failed code - " + responseCode);
                log.error("loadBinary : failed message - " + responseMsg);
            }
            if (log.isDebugEnabled())
            {
                log.debug("loadBinary : binary - " + ret[0] + ret[1]);
                log.debug("loadBinary : loadTime - " + res.getTime());
            }
            log.debug("End   : loadBinary");
            res.setResponseData(ret);
            res.setDataType(SampleResult.BINARY);
            return ret;
        }
        finally
        {
            try
            {
                // the server can request that the connection be closed,
                // but if we requested that the server close the connection
                // the server should echo back our 'close' request.
                // Otherwise, let the server disconnect the connection
                // when its timeout period is reached.
                sampler.disconnect(conn);
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * Get the response code of the URL connection and divide it by 100 thus
     * returning 2 (for 2xx response codes), 3 (for 3xx reponse codes), etc.
     *
     * @param conn          <code>HttpURLConnection</code> of URL request
     * @param res           where all results of sampling will be stored
     * @return              HTTP response code divided by 100
     */
    protected static int getErrorLevel(
        HttpURLConnection conn,
        SampleResult res)
    {
        log.debug("Start : getErrorLevel");
        int errorLevel= 2;
        try
        {
            int responseCode= ((HttpURLConnection)conn).getResponseCode();
            String responseMessage=
                ((HttpURLConnection)conn).getResponseMessage();
            errorLevel= responseCode / 100;
            res.setResponseCode(String.valueOf(responseCode));
            res.setResponseMessage(responseMessage);
            if (log.isDebugEnabled())
            {
                log.debug("getErrorLevel : responseCode - " + responseCode);
                log.debug(
                    "getErrorLevel : responseMessage - " + responseMessage);
            }
        }
        catch (Exception e2)
        {
            log.error("getErrorLevel : " + conn.getHeaderField(0));
            log.error("getErrorLevel : " + conn.getHeaderFieldKey(0));
            log.error(
                "getErrorLevel : "
                    + "Error getting response code for HttpUrlConnection - ",
                e2);
            res.setResponseData(e2.toString().getBytes());
            res.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
            res.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
            res.setSuccessful(false);
        }
        log.debug("End   : getErrorLevel");
        return errorLevel;
    }

    /**
     * Returns the encoding type which is different for different jdks
     * even though they mean the same thing (for example, UTF8 or UTF-8).
     *
     * @return  either UTF8 or UTF-8 depending on the jdk version
     * @see     #utfEncodingName
     */
    protected static String getUTFEncodingName()
    {
        log.debug("Start : getUTFEncodingName");
        if (utfEncodingName == null)
        {
            String versionNum= System.getProperty("java.version");
            if (log.isDebugEnabled())
            {
                log.debug("getUTFEncodingName : version = " + versionNum);
            }
            if (versionNum.startsWith("1.1"))
            {
                utfEncodingName= "UTF8";
            }
            else
            {
                utfEncodingName= "UTF-8";
            }
        }
        if (log.isDebugEnabled())
        {
            log.debug("getUTFEncodingName : Encoding = " + utfEncodingName);
        }
        log.debug("End   : getUTFEncodingName");
        return utfEncodingName;
    }

    public static class Test extends TestCase
    {
        private HTTPSampler hsf;

        transient private static Logger log= LoggingManager.getLoggerForClass();

        public Test(String name)
        {
            super(name);
        }

        protected void setUp()
        {
            log.debug("Start : setUp1");
            hsf= new HTTPSampler();
            hsf.setMethod(HTTPSampler.GET);
            hsf.setProtocol("file");
            hsf.setPath("HTTPSamplerFullTestFile.txt");
            hsf.setImageParser(true);
            log.debug("End   : setUp1");
        }

        public void testGetUTFEncodingName()
        {
            log.debug("Start : testGetUTFEncodingName");
            String javaVersion= System.getProperty("java.version");
            System.setProperty("java.version", "1.1");
            assertEquals("UTF8", HTTPSamplerFull.getUTFEncodingName());
            // need to clear utfEncodingName variable first 'cos
            // getUTFEncodingName checks to see if it's null
            utfEncodingName= null;
            System.setProperty("java.version", "1.2");
            assertEquals("UTF-8", HTTPSamplerFull.getUTFEncodingName());
            System.setProperty("java.version", javaVersion);
            log.debug("End   : testGetUTFEncodingName");
        }

        public void testGetUrlConfig()
        {
            log.debug("Start : testGetUrlConfig");
            assertEquals(HTTPSampler.GET, hsf.getMethod());
            assertEquals("file", hsf.getProtocol());
            assertEquals("HTTPSamplerFullTestFile.txt", hsf.getPath());
            log.debug("End   : testGetUrlConfig");
        }

        // Can't think of a self-contained way to test this 'cos it requires
        // http server.  Tried to use file:// but HTTPSampler's sample
        // specifically requires http.
        public void testSampleMain()
        {
            log.debug("Start : testSampleMain");
            // !ToDo : Have to wait till the day SampleResult is extended to
            // store results of all downloaded stuff e.g. images, applets etc
            String fileInput=
                "<html>\n\n"
                    + "<title>\n"
                    + "  A simple applet\n"
                    + "</title>\n"
                    + "<body background=\"back.jpg\" vlink=\"#dd0000\" "
                    + "link=\"#0000ff\">\n"
                    + "<center>\n"
                    + "<h2>   A simple applet\n"
                    + "</h2>\n"
                    + "<br>\n"
                    + "<br>\n"
                    + "<table>\n"
                    + "<td width = 20>\n"
                    + "<td width = 500 align = left>\n"
                    + "<img src=\"/tomcat.gif\">\n"
                    + "<img src=\"/tomcat.gif\">\n"
                    + "<a href=\"NervousText.java\"> Read my code <a>\n"
                    + "<p><applet code=NervousText.class width=400 "
                    + "height=200>\n"
                    + "</applet>\n"
                    + "<p><applet code=NervousText.class width=400 "
                    + "height=200>\n"
                    + "</applet>\n"
                    + "</table>\n"
                    + "<form>\n"
                    + "  <input type=\"image\" src=\"/tomcat-power.gif\">\n"
                    + "</form>\n"
                    + "<form>\n"
                    + "  <input type=\"image\" src=\"/tomcat-power.gif\">\n"
                    + "</form>\n"
                    + "</body>\n"
                    + "</html>\n";
            byte[] bytes= fileInput.getBytes();
            try
            {
                FileOutputStream fos=
                    new FileOutputStream("HTTPSamplerFullTestFile.txt");
                fos.write(bytes);
                fos.close();
            }
            catch (IOException ioe)
            {
                fail(
                    "Cannot create HTTPSamplerFullTestFile.txt in current "
                        + "directory for testing - "
                        + ioe);
            }
            // !ToDo
            // hsf.sample(entry);
            assertNull("Cannot think of way to test sample", null);
            log.debug("End   : testSampleMain");
        }

        protected void tearDown()
        {
            log.debug("Start : tearDown");
            hsf= null;
            log.debug("End   : tearDown");
        }
    }
}
