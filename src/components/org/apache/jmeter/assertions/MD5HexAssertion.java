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

/**
 * MD5HexAssertion class creates an MD5 checksum from the response <br/>
 * and matches it with the MD5 hex provided.
 * The assertion will fail when the expected hex is different from the <br/>
 * one calculated from the response OR when the expected hex is left empty.
 * 
 * @author	<a href="mailto:jh@domek.be">Jorg Heymans</a>
 */
package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import junit.framework.TestCase;

//import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;

public class MD5HexAssertion
    extends AbstractTestElement
    implements Serializable, Assertion {

    /** Key for storing assertion-informations in the jmx-file. */
    private static final String MD5HEX_KEY = "MD5HexAssertion.size";

    /* 
     * @param response
     * @return
     */
    public AssertionResult getResult(SampleResult response) {

        AssertionResult result = new AssertionResult();
        result.setFailure(false);
        byte[] resultData = response.getResponseData();

        if (resultData == null) {
            result.setError(false);
            result.setFailure(true);
            result.setFailureMessage("Response was null");
            return result;
        }

        //no point in checking if we don't have anything to compare against
        if (getAllowedMD5Hex().equals("")) {
            result.setError(false);
            result.setFailure(true);
            result.setFailureMessage("MD5Hex to test against is empty");
            return result;
        }
        
		String md5Result=baMD5Hex(resultData);

        //String md5Result = DigestUtils.md5Hex(resultData);

        if (!md5Result.equalsIgnoreCase(getAllowedMD5Hex())) {
            result.setFailure(true);

            Object[] arguments = { md5Result, getAllowedMD5Hex()};
            String message =
                MessageFormat.format(
                    JMeterUtils.getResString("md5hex_assertion_failure"),
                    arguments);
            result.setFailureMessage(message);

        }

        return result;
    }

    public void setAllowedMD5Hex(String hex) {
        setProperty(new StringProperty(MD5HexAssertion.MD5HEX_KEY, hex));
    }

    public String getAllowedMD5Hex() {
        return getPropertyAsString(MD5HexAssertion.MD5HEX_KEY);
    }
    
    private static String baToHex(byte ba [])
    {
    	StringBuffer sb = new StringBuffer(32);
		for (int i = 0; i<ba.length; i++ )
			{
				int j = ba[i]&0xff;
				if (j < 16) sb.append("0");
				sb.append(Integer.toHexString(j));
			}
	   	return sb.toString();
    }
    
    private static String baMD5Hex(byte ba[])
    {
		byte [] md5Result={};
		
		try
		{
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			md5Result = md.digest(ba);
		}
		catch (NoSuchAlgorithmException e)
		{
			log.error("",e);
		}
    	return baToHex(md5Result);
    }
    
    public static class Test extends TestCase
    {
    	public void testHex() throws Exception
    	{
			assertEquals("00010203",baToHex(new byte[]{0,1,2,3}));
			assertEquals("03020100",baToHex(new byte[]{3,2,1,0}));
			assertEquals("0f807fff",baToHex(new byte[]{0xF,-128,127,-1}));
    	}
    	public void testMD5() throws Exception
    	{
    		assertEquals("D41D8CD98F00B204E9800998ECF8427E",baMD5Hex(new byte[]{}).toUpperCase());
    	}
    }
}
