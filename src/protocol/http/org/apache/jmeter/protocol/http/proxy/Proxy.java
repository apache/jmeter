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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * Thread to handle one client request.  Gets the request from the client and
 * passes it on to the server, then sends the response back to the client.
 * Information about the request and response is stored so it can be used in a
 * JMeter test plan.
 *
 * @author     mike
 * @version    $Revision$
 * @created    June 8, 2001
 */
public class Proxy extends Thread
{
    /** Logging. */
    private static transient Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.http");

    /** Socket to client. */
    private Socket clientSocket = null;

    /** Target to receive the generated sampler. */
    private ProxyControl target;


    /**
     * Default constructor.
     */
    public Proxy()
    {
    }

    /**
     * Create and configure a new Proxy object.
     *
     * @param clientSocket the socket connection to the client
     * @param target       the ProxyControl which will receive the generated
     *                     sampler
     */
    Proxy(Socket clientSocket, ProxyControl target)
    {
        configure(clientSocket, target);
    }

    /**
     * Configure the Proxy.
     *
     * @param clientSocket the socket connection to the client
     * @param target       the ProxyControl which will receive the generated
     *                     sampler
     */
    void configure(
        Socket clientSocket,
        ProxyControl target)
    {
        this.target = target;
        this.clientSocket = clientSocket;
    }

    /**
     * Main processing method for the Proxy object
     */
    public void run()
    {
        HttpRequestHdr request = new HttpRequestHdr();
        byte[] serverResponse = new byte[0];
        HeaderManager headers = new HeaderManager();
        HTTPSampler sampler = new HTTPSampler();
        try
        {
            byte[] clientRequest =
                request.parse(
                    new BufferedInputStream(clientSocket.getInputStream()));
            headers = request.getHeaderManager();

            sampler = request.getSampler();
            sampler.setHeaderManager(headers);

            serverResponse = sampler.sample().getResponseData();
            writeToClient(
                serverResponse,
                new BufferedOutputStream(clientSocket.getOutputStream()));
            headers.removeHeaderNamed("cookie");

           
        }
        catch (UnknownHostException uhe)
        {
            log.warn("Server Not Found.", uhe);
            writeErrorToClient(HttpReplyHdr.formServerNotFound());
        }
        catch (Exception e)
        {
            log.error("",e);
            writeErrorToClient(HttpReplyHdr.formTimeout());
        }
        finally
        {
            target.deliverSampler(
                                       sampler,
                                       new TestElement[] { headers },
                                       serverResponse);
            try
            {
                clientSocket.close();
            }
            catch (Exception e)
            {
                log.error("",e);
            }
        }
    }

    /**
     * Write output to the output stream, then flush and close the stream.
     *
     * @param inBytes       the bytes to write
     * @param out           the output stream to write to
     * @throws IOException  if an IOException occurs while writing
     */
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
            throw e;
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (Exception ex)
            {
                log.warn("Error while closing socket", ex);
            }
        }
    }

    /**
     * Write an error message to the client.  The message should be the full
     * HTTP response.
     *
     * @param message the message to write
     */
    private void writeErrorToClient(String message)
    {
        try
        {
            OutputStream sockOut = clientSocket.getOutputStream();
            DataOutputStream out = new DataOutputStream(sockOut);
            out.writeBytes(message);
            out.flush();
        }
        catch (Exception e)
        {
            log.warn("Exception while writing error", e);
        }
    }
}