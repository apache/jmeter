/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
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

package org.apache.jmeter.assertions;

import java.io.IOException;
import java.io.Serializable;

//import bsh.EvalError;
import bsh.Interpreter;
   
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * A sampler which understands BeanShell
 *
 * @author sebb AT apache DOT org
 * @version    $Revision$ Updated on: $Date$
 */
public class BeanShellAssertion extends AbstractTestElement
    implements Serializable, Assertion
{
	protected static Logger log = LoggingManager.getLoggerForClass();

    public static final String FILENAME   = "BeanShellAssertion.filename"; //$NON-NLS-1$
	public static final String SCRIPT     = "BeanShellAssertion.query"; //$NON-NLS-1$
	public static final String PARAMETERS = "BeanShellAssertion.parameters"; //$NON-NLS-1$

    private Interpreter bshInterpreter;
	
	public BeanShellAssertion()
	{
		try{
			bshInterpreter = new Interpreter();
		} catch (NoClassDefFoundError e){
			bshInterpreter=null;
		}
	}

	public String getScript()
	{
		return getPropertyAsString(SCRIPT);
	}
    
	public String getFilename()
	{
		return getPropertyAsString(FILENAME);
	}

	public String getParameters()
	{
		return getPropertyAsString(PARAMETERS);
	}

		/* (non-Javadoc)
		 * @see org.apache.jmeter.assertions.Assertion#getResult(org.apache.jmeter.samplers.SampleResult)
		 */
	public AssertionResult getResult(SampleResult response)
	{
		AssertionResult result = new AssertionResult();
		
		try
        {
        	String request=getScript();
        	String fileName=getFilename();

			bshInterpreter.set("FileName",getFilename());
			bshInterpreter.set("Parameters",getParameters());// as a single line
			bshInterpreter.set("bsh.args",JOrphanUtils.split(getParameters()," "));
			
			bshInterpreter.set("Response",response);// Raw access to the response
			bshInterpreter.set("ResponseData",response.getResponseData());
			bshInterpreter.set("ResponseCode",response.getResponseCode());
			bshInterpreter.set("ResponseMessage",response.getResponseMessage());
			bshInterpreter.set("ResponseHeaders",response.getResponseHeaders());
			bshInterpreter.set("RequestHeaders",response.getRequestHeaders());
			bshInterpreter.set("SampleLabel",response.getSampleLabel());
			bshInterpreter.set("SamplerData",response.getSamplerData());
			bshInterpreter.set("Successful",response.isSuccessful());

			bshInterpreter.set("Result",result);// Raw access to the result

			bshInterpreter.set("FailureMessage","");
			bshInterpreter.set("Failure",false);

			//Object bshOut;
			
			if (fileName.length() == 0){
				//bshOut = 
				bshInterpreter.eval(request);
			} else {
				//bshOut = 
				bshInterpreter.source(fileName);
			}
			
	        result.setFailureMessage(bshInterpreter.get("FailureMessage").toString());//$NON-NLS-1$
	        result.setFailure(Boolean.valueOf(bshInterpreter.get("Failure") //$NON-NLS-1$
	            .toString()).booleanValue());
			result.setError(false);
        }
/*
 * To avoid class loading problems when bsh,jar is missing,
 * we don't try to catch this error separately
 * 		catch (bsh.EvalError ex)
		{
			log.debug("",ex);
			result.setError(true);
			result.setFailureMessage(ex.toString());
		} 
 */
        // but we do trap this error to make tests work better
        catch(NoClassDefFoundError ex){
			result.setError(true);
			log.error("BeanShell Jar missing? "+ex.toString());
			response.setStopThread(true); // No point continuing
			result.setFailureMessage("");
        }
		catch (IOException ex)
		{
			result.setError(true);
			result.setFailureMessage(ex.toString());
			log.warn(ex.toString());
		}
		catch (Exception ex) // Mainly for bsh.EvalError
		{
			result.setError(true);
			result.setFailureMessage(ex.toString());
			log.warn(ex.toString());
		}

        return result;
    }
}