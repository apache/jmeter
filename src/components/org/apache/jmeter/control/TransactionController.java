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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.SamplePackage;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Transaction Controller to measure transaction times
 * 
 * @author  sebb AT apache DOT org
 * @version $Revision$
 */
public class TransactionController
    extends GenericController
    implements Controller, Serializable
{
    protected static final Logger log = LoggingManager.getLoggerForClass();

    transient private String threadName;
	transient private ListenerNotifier lnf;
	transient private JMeterContext threadContext;
	transient private JMeterVariables threadVars;
	transient private SampleResult res;
	
    /**
     * Creates a Transaction Controller
     */
    public TransactionController()
    {
    	threadName = Thread.currentThread().getName();
		lnf = new ListenerNotifier();
    }

    private void log_debug(String s)
    {
	    String n = this.getName();
	    log.debug(threadName + " " + n + " "+ s);
    }
    
    private int calls;
    /**
     * @see org.apache.jmeter.control.Controller#next()
     */
    public Sampler next()
    {
		Sampler returnValue = null;
    	if (isFirst()) // must be the start of the subtree
    	{
    		log_debug("+++++++++++++++++++++++++++++");
    		calls = 0;
    		res = new SampleResult();
    		res.sampleStart();
    	}
    	
    	calls++;
    	
    	returnValue = super.next();

        if (returnValue == null) // Must be the end of the controller
        {
			log_debug("-----------------------------"+calls);
        	if (res == null){
        		log_debug("already called");
        	} else {
				res.sampleEnd();
				res.setSuccessful(true);
				res.setSampleLabel(getName());
				res.setResponseCode("200");
				res.setResponseMessage("Called: "+calls);
				res.setThreadName(threadName);
        	
				//TODO could these be done earlier (or just once?)
				threadContext = JMeterContextService.getContext();
				threadVars = threadContext.getVariables();
				
				SamplePackage pack = (SamplePackage)
				              threadVars.getObject(JMeterThread.PACKAGE_OBJECT);
				if (pack == null)
				{
					log.warn("Could not fetch SamplePackage");
				}
				else 
				{
					lnf.notifyListeners(new SampleEvent(res,getName()),pack.getSampleListeners());
				}
				res=null;
        	}
        }

        return returnValue;
    }
}
