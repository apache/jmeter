/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.http.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
//
// Class:     Proxy
// Abstract:  Thread to handle one client request. get the requested
//            object from the web server or from the cache, and delivers
//            the bits to client.
//
/**
 *  Description of the Class
 *
 *@author     mike
 *@created    June 8, 2001
 */
public class Proxy extends Thread
{
    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
            "jmeter.protocol.http");
    //
    // Member variables
    //
    Socket ClientSocket = null;
    // Socket to client
    Socket SrvrSocket = null;
    // Socket to web server
    Cache cache = null;
    // Static cache manager object
    String localHostName = null;
    // Local machine name
    String localHostIP = null;
    // Local machine IP address
    String adminPath = null;
    // Path of admin applet
    Config config = null;
    // UrlConfig object for saving test cases
    ProxyControl target;
    CookieManager cookieManager;

    /**
     * Constructor.  Configures Proxy at same time.
     * @param clientSocket
     * @param CacheManager
     * @param configObject
     * @param target
     * @param cookieManager
     */
    Proxy(Socket clientSocket,Cache CacheManager,Config configObject,
        ProxyControl target,CookieManager cookieManager) 
    {
        configure(clientSocket,CacheManager,configObject,target,cookieManager);
    }

    public Proxy()
    {
    }

    public void configure(
        Socket clientSocket,
        Cache CacheManager,
        Config configObject,
        ProxyControl target,
        CookieManager cookieManager)
    {
        this.cookieManager = cookieManager;
        this.target = target;
        config = configObject;
        ClientSocket = clientSocket;
        cache = CacheManager;
        localHostName = config.getLocalHost();
        localHostIP = config.getLocalIP();
        adminPath = config.getAdminPath();
    }

    //
    // run - Main work is done here:
    //
    /**
     *  Main processing method for the Proxy object
     */
    public void run()
    {
        String serverName = "";
        HttpURLConnection url;
        byte line[];
        HttpRequestHdr request = new HttpRequestHdr();
        HttpReplyHdr reply = new HttpReplyHdr();
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean TakenFromCache = false;
        boolean isCachable = false;
        try
        {
            byte[] clientRequest = request.parse(new BufferedInputStream(
                ClientSocket.getInputStream()));
            HTTPSampler sampler = request.getSampler();
            HeaderManager headers = request.getHeaderManager();
            sampler.setHeaderManager(headers);
            byte[] serverResponse = sampler.sample().getResponseData();
            writeToClient(serverResponse,
                new BufferedOutputStream(ClientSocket.getOutputStream()));
            headers.removeHeaderNamed("cookie");
            target.deliverSampler(sampler,new TestElement[]{headers},serverResponse);
        }
        catch (UnknownHostException uhe)
        {
            log.warn("Server Not Found.",uhe);
            try
            {
                DataOutputStream out = new DataOutputStream(ClientSocket.getOutputStream());
                out.writeBytes(reply.formServerNotFound());
                out.flush();
            }
            catch (Exception uhe2)
            {
            }
        }
        catch (Exception e)
        {
            log.error("",e);
            try
            {
                if (TakenFromCache)
                {
                    fileInputStream.close();
                }
                else if (isCachable)
                {
                    fileOutputStream.close();
                }
                DataOutputStream out = new DataOutputStream(ClientSocket.getOutputStream());
                out.writeBytes(reply.formTimeout());
                out.flush();
            }
            catch (Exception uhe2)
            {
            }
        }
        finally
        {
            try
            {
                ClientSocket.close();
            }
            catch (Exception e)
            {
                log.error("",e);
            }
        }
    }
    
    public byte[] sampleServer(HTTPSampler sampler)
        throws IllegalAccessException, InstantiationException
    {
        SampleResult result = sampler.sample();
        return result.getResponseData();
    }
    //
    // Private methods
    //
    //
    // Send to administrator web page containing reference to applet
    //
    private void sendAppletWebPage()
    {
        log.info("Sending the applet...");
        String page = "";
        try
        {
            File appletHtmlPage =
                new File(config.getAdminPath() + File.separator + "Admin.html");
            BufferedReader in =
                new BufferedReader(new InputStreamReader(new FileInputStream(appletHtmlPage)));
            String s = null;
            while ((s = in.readLine()) != null)
            {
                page += s;
            }
            page =
                page.substring(0, page.indexOf("PORT"))
                    + config.getAdminPort()
                    + page.substring(page.indexOf("PORT") + 4);
            in.close();
            DataOutputStream out = new DataOutputStream(ClientSocket.getOutputStream());
            out.writeBytes(page);
            out.flush();
            out.close();
        }
        catch (Exception e)
        {
            log.error("can't open applet html page",e);
        }
    }
    
    //
    // Send the applet to administrator
    //
    private void sendAppletClass(String className)
    {
        try
        {
            byte data[] = new byte[2000];
            int count;
            HttpReplyHdr reply = new HttpReplyHdr();
            File appletFile = new File(adminPath + File.separatorChar + className);
            long length = appletFile.length();
            FileInputStream in = new FileInputStream(appletFile);
            DataOutputStream out = new DataOutputStream(ClientSocket.getOutputStream());
            out.writeBytes(reply.formOk("application/octet-stream", length));
            while (-1 < (count = in.read(data)))
            {
                out.write(data, 0, count);
            }
            out.flush();
            in.close();
            out.close();
        } 
        catch (Exception e)
        {
        }
    }
    
    private void writeToClient(
        byte[] inBytes,
        OutputStream out)
        throws IOException
    {
        try
        {
            out.write(inBytes);
            out.flush();
            log.info("Done writing to client");
        }
        catch (IOException e)
        {
            log.error("",e);
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (Exception ex)
            {
            }
        }
    }
}