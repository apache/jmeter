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
package org.apache.jmeter.engine;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;

import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


/**
 * @version $Revision$
 */
public class RemoteJMeterEngineImpl
    extends java.rmi.server.UnicastRemoteObject
    implements RemoteJMeterEngine
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    JMeterEngine backingEngine;

    public RemoteJMeterEngineImpl() throws RemoteException
    {
        try
        {
            backingEngine =
                new StandardJMeterEngine(
                    InetAddress.getLocalHost().getHostName());
            Naming.rebind("JMeterEngine", this);
        }
        catch (Exception ex)
        {
            log.error(
                "rmiregistry needs to be running to start JMeter in server " +
                "mode",
                ex);
        }
    }

    public void setHost(String host)
    {
        log.warn("received host");
        backingEngine.setHost(host);
    }

    /**
     * Adds a feature to the ThreadGroup attribute of the RemoteJMeterEngineImpl
     * object.
     *
     * @param  tGroup the feature to be added to the ThreadGroup attribute
     */
    public void configure(HashTree testTree) throws RemoteException
    {
        log.warn("received test tree");
        backingEngine.configure(testTree);
    }

    public void runTest() throws RemoteException, JMeterEngineException
    {
        log.warn("running test");
        backingEngine.runTest();
    }

    public void reset() throws RemoteException
    {
        backingEngine.reset();
    }

    public void stopTest() throws RemoteException
    {
        backingEngine.stopTest();
    }

    /**
     * The main program for the RemoteJMeterEngineImpl class.
     *
     * @param  args  the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            RemoteJMeterEngine engine = new RemoteJMeterEngineImpl();
            while (true)
            {
                Thread.sleep(Long.MAX_VALUE);
            }
        }
        catch (Exception ex)
        {
            log.error("", ex);
        }

    }
}
