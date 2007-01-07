/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */

/*
 * Created on May 21, 2004
 */
package org.apache.jmeter.protocol.http.util.accesslog;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestCloneable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * @author mstover
 * 
 */
public class SessionFilter implements Filter, Serializable, TestCloneable,ThreadListener {
    private static final long serialVersionUID = 1;
	static Logger log = LoggingManager.getLoggerForClass();

	/**
	 * These objects are static across multiple threads in a test, via clone()
	 * method.
	 */
	protected Map cookieManagers;
    protected Set managersInUse;
    
    protected CookieManager lastUsed;

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.LogFilter#excPattern(java.lang.String)
	 */
	protected boolean hasExcPattern(String text) {
        return false;
	}

	protected String getIpAddress(String logLine) {
		Pattern incIp = JMeterUtils.getPatternCache().getPattern("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
				Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
		Perl5Matcher matcher = JMeterUtils.getMatcher();
		matcher.contains(logLine, incIp);
		return matcher.getMatch().group(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#reset()
	 */
	public void reset() {
		cookieManagers.clear();
	}

	public Object clone() {
        if(cookieManagers == null)
        {
            cookieManagers = Collections.synchronizedMap(new HashMap());
        }
        if(managersInUse == null)
        {
            managersInUse = Collections.synchronizedSet(new HashSet());
        }
		SessionFilter f = new SessionFilter();
        f.cookieManagers = cookieManagers;
        f.managersInUse = managersInUse;
		return f;
	}

	/**
	 * 
	 */
	public SessionFilter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#excludeFiles(java.lang.String[])
	 */
	public void excludeFiles(String[] filenames) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#excludePattern(java.lang.String[])
	 */
	public void excludePattern(String[] regexp) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#filter(java.lang.String)
	 */
	public String filter(String text) {
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#includeFiles(java.lang.String[])
	 */
	public void includeFiles(String[] filenames) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#includePattern(java.lang.String[])
	 */
	public void includePattern(String[] regexp) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#isFiltered(java.lang.String)
	 */
	public boolean isFiltered(String path,TestElement sampler) {
		String ipAddr = getIpAddress(path);
        CookieManager cm = getCookieManager(ipAddr);
        ((HTTPSampler)sampler).setCookieManager(cm);   
        return false;
	}
    
    protected CookieManager getCookieManager(String ipAddr)
    {
        CookieManager cm = null;
        // First have to release the cookie we were using so other
        // threads stuck in wait can move on
        synchronized(managersInUse)
        {
            if(lastUsed != null)
            {
                managersInUse.remove(lastUsed);
                managersInUse.notify(); 
            }
        }
        // let notified threads move on and get lock on managersInUse
        if(lastUsed != null)
        {
            Thread.yield();
        }
        // here is the core routine to find appropriate cookie manager and
        // check it's not being used.  If used, wait until whoever's using it gives
        // it up
        synchronized(managersInUse)
        {
            cm = (CookieManager)cookieManagers.get(ipAddr);
            if(cm == null)
            {
                cm = new CookieManager();
                cookieManagers.put(ipAddr,cm);
            } 
            while(managersInUse.contains(cm))
            {
                try {
                    managersInUse.wait();
                } catch (InterruptedException e) {
                    log.info("SessionFilter wait interrupted");
                }
            }
            managersInUse.add(cm);
            lastUsed = cm;
        }
        return cm;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.util.accesslog.Filter#setReplaceExtension(java.lang.String,
	 *      java.lang.String)
	 */
	public void setReplaceExtension(String oldextension, String newextension) {
	}

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.ThreadListener#threadFinished()
     */
    public void threadFinished() {
        synchronized(managersInUse)
        {
            managersInUse.remove(lastUsed);
            managersInUse.notify(); 
        }        
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.ThreadListener#threadStarted()
     */
    public void threadStarted() {
        // TODO Auto-generated method stub
        
    }
}
