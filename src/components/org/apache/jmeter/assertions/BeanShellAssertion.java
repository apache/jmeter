/*
 * Copyright 2003-2004 The Apache Software Foundation.
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
 */
public class BeanShellAssertion extends AbstractTestElement
    implements Serializable, Assertion
{
	protected static final Logger log = LoggingManager.getLoggerForClass();

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

			// The following are used to set the Result details on return from the script:
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
 * To avoid class loading problems when the BSH jar is missing,
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
			log.error("BeanShell Jar missing? "+ex.toString());
			result.setError(true);
			result.setFailureMessage("BeanShell Jar missing? "+ex.toString());
			response.setStopThread(true); // No point continuing
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