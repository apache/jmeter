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

package org.apache.jmeter.assertions;

import java.io.Serializable;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class AssertionResult implements Serializable
{
    /** True if the assertion failed. */
    private boolean failure;
    
    /** True if there was an error checking the assertion. */
    private boolean error;
    
    /** A message describing the failure. */
    private String failureMessage;

    /**
     * Create a new Assertion Result.  The result will indicate no failure or
     * error.
     */
    public AssertionResult()
    {
    }

    /**
     * Check if the assertion failed.  If it failed, the failure message may
     * give more details about the failure.
     * 
     * @return true if the assertion failed, false if the sample met the
     *         assertion criteria
     */
    public boolean isFailure()
    {
        return failure;
    }

    /**
     * Check if an error occurred while checking the assertion.  If an error
     * occurred, the failure message may give more details about the error.
     * 
     * @return true if an error occurred while checking the assertion, false
     *         otherwise.
     */
    public boolean isError()
    {
        return error;
    }

    /**
     * Get the message associated with any failure or error.  This method may
     * return null if no message was set.
     * 
     * @return a failure or error message, or null if no message has been set
     */
    public String getFailureMessage()
    {
        return failureMessage;
    }

    /**
     * Set the flag indicating whether or not an error occurred.
     * 
     * @param e true if an error occurred, false otherwise
     */
    public void setError(boolean e)
    {
        error = e;
    }

    /**
     * Set the flag indicating whether or not a failure occurred.
     * 
     * @param f true if a failure occurred, false otherwise
     */
    public void setFailure(boolean f)
    {
        failure = f;
    }

    /**
     * Set the failure message giving more details about a failure or error.
     * 
     * @param message the message to set
     */
    public void setFailureMessage(String message)
    {
        failureMessage = message;
    }
}
