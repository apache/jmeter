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
package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.io.StringReader;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.jdom.input.SAXBuilder;

/**
 * Checks if the result is a well-formed XML content.
 *
 * Copyright: Copyright (c) 2001
 *
 * @author <a href="mailto:gottfried@szing.at">Gottfried Szing</a>
 * @version $Revision$, $Date$
 */
public class XMLAssertion extends AbstractTestElement implements Serializable, Assertion
{
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
        result.setFailure(false);

        // the result data
        String resultData = new String(getResultBody(response.getResponseData()));

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

    /**
     * Return the body of the http return.
     */
    private byte[] getResultBody(byte[] resultData)
    {
        for (int i = 0; i < (resultData.length - 1); i++)
        {
            if (resultData[i] == '\n' && resultData[i + 1] == '\n')
            {
                return getByteArraySlice(resultData, (i + 2), resultData.length - 1);
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
