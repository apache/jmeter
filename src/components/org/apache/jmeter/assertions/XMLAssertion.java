// $Header$
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

import java.io.Serializable;
import java.io.StringReader;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.jdom.input.SAXBuilder;

/**
 * Checks if the result is a well-formed XML content.
 *
 * @author <a href="mailto:gottfried@szing.at">Gottfried Szing</a>
 * @version $Revision$, $Date$
 */
public class XMLAssertion
    extends AbstractTestElement
    implements Serializable, Assertion
{
	private static final Logger log = LoggingManager.getLoggerForClass();
	
    // one builder for all requests
    private static SAXBuilder  builder = null;

    /**
     * Returns the result of the Assertion. Here it checks wether the
     * Sample took to long to be considered successful. If so an AssertionResult
     * containing a FailureMessage will be returned. Otherwise the returned
     * AssertionResult will reflect the success of the Sample.
     */
    public AssertionResult getResult(SampleResult response)
    {
        // no error as default
        AssertionResult result = new AssertionResult();
        if(response.getResponseData() == null)
             {
                 return setResultForNull(result);
             }
        result.setFailure(false);

        // the result data
        String resultData =
            new String(getResultBody(response.getResponseData()));

        // create parser like (!) a singleton
        if (builder  == null)
        {
            try
            {
                // This builds a document of whatever's in the given resource
                builder = new SAXBuilder();
            }
            catch (Exception e)
            {
                log.error("Unable to instantiate DOM Builder", e);

                result.setFailure(true);
                result.setFailureMessage("Unable to instantiate DOM Builder");

                // return with an error
                return result;
            }
        }

        try
        {
            builder.build(new StringReader(resultData));
        }
        catch (Exception e)
        {
            log.debug("Cannot parse result content", e);

            result.setFailure(true);
            result.setFailureMessage(e.getMessage());
        }

        return result;
    }
    
    protected AssertionResult setResultForNull(AssertionResult result)
        {
            result.setError(false);
            result.setFailure(true);
            result.setFailureMessage("Response was null");
            return result;
        }

    /**
     * Return the body of the http return.
     */
    private byte[] getResultBody(byte[] resultData)
    {
        for (int i = 0; i < (resultData.length - 1); i++)
        {
            if (resultData[i] == '\n' && resultData[i + 1] == '\n')
            {
                return getByteArraySlice(
                    resultData,
                    (i + 2),
                    resultData.length - 1);
            }
        }
        return resultData;
    }

    /**
     * Return a slice of a byte array
     */
    private byte[] getByteArraySlice(byte[] array, int begin, int end)
    {
        byte[] slice = new byte[(end - begin + 1)];
        int count = 0;
        for (int i = begin; i <= end; i++)
        {
            slice[count] = array[i];
            count++;
        }

        return slice;
    }
}
