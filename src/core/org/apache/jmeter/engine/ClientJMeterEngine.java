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
package org.apache.jmeter.engine;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


/************************************************************
 *  !ToDo (Class description)
 *
 *@author     $Author$
 *@created    $Date$
 *@version    $Revision$
 ***********************************************************/
public class ClientJMeterEngine implements JMeterEngine,Runnable
{
	transient private static Logger log = LoggingManager.getLoggerForClass();
	RemoteJMeterEngine remote;
	HashTree test;
	SearchByClass testListeners;
	ConvertListeners sampleListeners;
	private String host;

	/************************************************************
	 *  !ToDo (Constructor description)
	 *
	 *@param  host                       !ToDo (Parameter description)
	 *@exception  MalformedURLException  !ToDo (Exception description)
	 *@exception  NotBoundException      !ToDo (Exception description)
	 *@exception  RemoteException        !ToDo (Exception description)
	 ***********************************************************/
	public ClientJMeterEngine(String host)
			 throws MalformedURLException, NotBoundException, RemoteException
	{
		this((RemoteJMeterEngine)Naming.lookup("//" + host + "/JMeterEngine"));
		this.host = host;
	}

	/************************************************************
	 *  !ToDo (Constructor description)
	 *
	 *@param  remote  !ToDo (Parameter description)
	 ***********************************************************/
	public ClientJMeterEngine(RemoteJMeterEngine remote)
	{
		this.remote = remote;
	}
	
	protected HashTree getTestTree()
	{
		return test;
	}

	public void configure(HashTree testTree)
	{
		test = testTree;
	}
	
	public void setHost(String host)
	{
		this.host = host;
	}

	/************************************************************
	 *  !ToDo (Method description)
	 ***********************************************************/
	public void runTest()
	{
        log.warn("about to run remote test");
		new Thread(this).start();
        log.warn("done initiating run command");
	}

	/************************************************************
	 *  !ToDo (Method description)
	 ***********************************************************/
	public void stopTest()
	{
		try
		{
			remote.stopTest();
		}
		catch(Exception ex)
		{
			log.error("",ex);
		}
	}

	/************************************************************
	 *  !ToDo (Method description)
	 ***********************************************************/
	public void reset()
	{
		try
		{
			remote.reset();
		}
		catch(Exception ex)
		{
			log.error("",ex);
		}
	}

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        log.warn("running clientengine run method");
        testListeners = new SearchByClass(TestListener.class);
        getTestTree().traverse(testListeners);
        sampleListeners = new ConvertListeners();
        getTestTree().traverse(sampleListeners);
        try
        {
            remote.setHost(host);
            log.warn("sent host info");
            remote.configure(test);
            log.warn("sent test");
            remote.runTest();
            log.warn("sent run command");
        }
        catch(Exception ex)
        {
            log.error("",ex);
        }

    }

}
