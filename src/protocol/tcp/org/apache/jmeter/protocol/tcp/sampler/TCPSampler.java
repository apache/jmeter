/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2004 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.tcp.sampler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler which understands Tcp requests.
 *
 * @version    $Revision$ $Date$
 */
public class TCPSampler extends AbstractSampler implements TestListener
{
	protected static Logger log = LoggingManager.getLoggerForClass();

    public final static String SERVER     = "TCPSampler.server";   //$NON-NLS-1$
    public final static String PORT       = "TCPSampler.port";     //$NON-NLS-1$
	public final static String FILENAME   = "TCPSampler.filename"; //$NON-NLS-1$
	public final static String CLASSNAME  = "TCPSampler.classname";//$NON-NLS-1$
	public final static String NODELAY    = "TCPSampler.nodelay";  //$NON-NLS-1$
	public final static String TIMEOUT    = "TCPSampler.timeout";  //$NON-NLS-1$

	private final static String TCPKEY = "TCP"; //$NON-NLS-1$ key for HashMap
	private final static String ERRKEY = "ERR"; //$NON-NLS-1$ key for HashMap

	private static Set allSockets = new HashSet();// Keep track of connections to allow close

	/** the cache of TCP Connections */
	private static ThreadLocal tp = new ThreadLocal(){
		protected Object initialValue(){
			return new HashMap();
		}
	};

	public TCPSampler()
	{
		log.debug("Created "+this);
	}

	private String getError(){
		Map cp = (Map) tp.get();
		return  (String) cp.get(ERRKEY);
	}

	private Socket getSocket() {
		Map cp = (Map) tp.get();
		Socket con = (Socket) cp.get(TCPKEY);
		if (con != null) {
			log.debug(this+" Reusing connection "+con); //$NON-NLS-1$
			return (Socket) con; 
		}
	
		// Not in cache, so create new one and cache it
		try
	    {
	        con = new Socket(getServer(),getPort());
			con.setSoTimeout(getTimeout());
			con.setTcpNoDelay(getNoDelay());
	        
			log.debug(this+"  Timeout "+getTimeout()+" NoDelay "+getNoDelay()); //$NON-NLS-1$
			log.debug("Created new connection "+con); //$NON-NLS-1$
			cp.put(TCPKEY,con);
			allSockets.add(con);// Save so can be closed
	    }
	    catch (UnknownHostException e)
	    {
	    	log.warn("Unknown host for "+getLabel(),e);//$NON-NLS-1$
			cp.put(ERRKEY,e.toString());
	    }
	    catch (IOException e)
	    {
			log.warn("Could not create socket for "+getLabel(),e); //$NON-NLS-1$
			cp.put(ERRKEY,e.toString());
	    }
		return con;
	}

	public String getUsername()
	{
		return getPropertyAsString(ConfigTestElement.USERNAME);
	}

	public String getPassword()
	{
		return getPropertyAsString(ConfigTestElement.PASSWORD);
	}

    public void setServer(String newServer)
    {
        this.setProperty(SERVER, newServer);
    }
    public String getServer()
    {
        return getPropertyAsString(SERVER);
    }
    public void setPort(String newFilename)
    {
        this.setProperty(PORT, newFilename);
    }
    public int getPort()
    {
        return getPropertyAsInt(PORT);
    }
    
	public void setFilename(String newFilename)
	{
		this.setProperty(FILENAME, newFilename);
	}
	public String getFilename()
	{
		return getPropertyAsString(FILENAME);
	}


	public void setTimeout(String newTimeout)
	{
		this.setProperty(FILENAME, newTimeout);
	}
	public int getTimeout()
	{
		return getPropertyAsInt(TIMEOUT);
	}


	public void setNoDelay(String newNoDelay)
	{
		this.setProperty(NODELAY, newNoDelay);
	}
	public boolean getNoDelay()
	{
		return getPropertyAsBoolean(NODELAY);
	}



    /**
     * Returns a formatted string label describing this sampler
     * Example output:
     *      Tcp://Tcp.nowhere.com/pub/README.txt
     *
     * @return a formatted string label describing this sampler
     */
    public String getLabel()
    {
        return ("tcp://" + this.getServer() + ":" + this.getPort());//$NON-NLS-1$
    }

	private String getClassname()
	{
		String className = JMeterUtils.getPropDefault("tcp.handler","TCPClientImpl");
		return className;
	}

	private static final String protoPrefix = "org.apache.jmeter.protocol.tcp.sampler."; 
	private Class getClass(String className)
	{
		Class c = null;
		try
        {
            c = Class.forName(className
            	,false,Thread.currentThread().getContextClassLoader());
        }
        catch (ClassNotFoundException e)
        {
			try
            {
                c = Class.forName(protoPrefix+className
                	,false,Thread.currentThread().getContextClassLoader());
            }
            catch (ClassNotFoundException e1)
            {
            	log.error("Could not find protocol class "+ className);
            }
        }
		return c;
        
	}

    private Object getProtocol(){
    	Object TCPClient = null;
    	Class javaClass = getClass(getClassname());
		try
		{
			TCPClient = javaClass.newInstance();
			if (log.isDebugEnabled())
			{
				log.debug(this
						+ "Created: "
						+ getClassname()
						+ "@"
						+ Integer.toHexString(TCPClient.hashCode()));
			}
		}
		catch (Exception e)
		{
			log.error(
				this + " Exception creating: " + getClassname(),e);
		}
		return TCPClient;
    }

    public SampleResult sample(Entry e)// Entry tends to be ignored ...
    {
    	log.info(getLabel()+" "+getFilename()+" "+getUsername()+" "+getPassword());
        SampleResult res = new SampleResult();
        boolean isSuccessful = false;
        res.setSampleLabel(getLabel());
        res.sampleStart();
        try
        {
			Socket sock = getSocket();
			if (sock == null){
				res.setResponseCode("500");
				res.setResponseMessage(getError());
			} else {
				InputStream is = sock.getInputStream();
				OutputStream os = sock.getOutputStream();
				TCPClient proto = (TCPClient) getProtocol();
	        	log.debug("Found class "+ proto.toString());
				String req=proto.write(os);
				res.setSamplerData(req);
				String in = proto.read(is);
	            res.setResponseData(in.getBytes());
	            res.setDataType(SampleResult.TEXT);
	            res.setResponseCode("200");
	            res.setResponseMessage("OK");
	            isSuccessful = true;
			}
        }
        catch (Exception ex)
        {
        	log.debug("",ex);
			res.setResponseCode("500");
            res.setResponseMessage(ex.toString());
        }

        // Calculate response time
        res.sampleEnd();

        // Set if we were successful or not
        res.setSuccessful(isSuccessful);

        return res;
    }

     private void disconnectAll(){
		synchronized (allSockets)
		{
			Iterator i = allSockets.iterator();
			while (i.hasNext())
			{
				Socket socket = (Socket) i.next();
				try
                {
                    socket.close();
                }
                catch (IOException e)
                {
                    log.warn("Error closing socket ",e);
                } finally {
					i.remove();
                }
			}
		}
     }


	 /* (non-Javadoc)
	  * @see org.apache.jmeter.testelement.TestListener#testStarted()
	  */
	 public void testStarted() // Only called once per class?
	 {
		 log.debug(this+" test started");
		 // TODO Auto-generated method stub
        
	 }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testEnded()
     */
    public void testEnded() // Only called once per class?
    {
		log.debug(this+" test ended");
		disconnectAll();
        
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
     */
    public void testStarted(String host)
    {
		log.debug(this+" test started on "+host);
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
     */
    public void testEnded(String host)
    {
		log.debug(this+" test ended on "+host);
		disconnectAll();
        
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
		log.debug(this+" test iteration start on "+event.getIteration());
        // TODO Auto-generated method stub
        
    }
}
