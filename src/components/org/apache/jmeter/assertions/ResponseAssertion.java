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

import java.util.*;
import java.io.Serializable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testelement.AbstractTestElement;

import org.apache.oro.text.regex.*;

/************************************************************
 *  Title: Jakarta-JMeter Description: Copyright: Copyright (c) 2001 Company:
 *  Apache
 *
 *@author     Michael Stover
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class ResponseAssertion extends AbstractTestElement implements Serializable, Assertion
{

	public final static String TEST_FIELD = "Assertion.test_field";
	public final static String TEST_TYPE = "Assertion.test_type";
	public final static String TEST_STRINGS = "Asserion.test_strings";
	public final static String SAMPLE_LABEL = "Assertion.sample_label";
	public final static String RESPONSE_DATA = "Assertion.response_data";


	private String notMessage = "";
	private String failMessage = "to contain: ";
	public final static int MATCH = 1 << 0;
	public final static int CONTAINS = 1 << 1;
	public final static int NOT = 1 << 2;
	private transient static Perl5Compiler compiler = new Perl5Compiler();
	private transient static Perl5Matcher matcher = new Perl5Matcher();

	/************************************************************
	 *  !ToDo (Constructor description)
	 ***********************************************************/
	public ResponseAssertion()
	{
		setProperty(TEST_STRINGS,new ArrayList());
	}

	/************************************************************
	 *  !ToDo (Constructor description)
	 *
	 *@param  field   !ToDo (Parameter description)
	 *@param  type    !ToDo (Parameter description)
	 *@param  string  !ToDo (Parameter description)
	 ***********************************************************/
	public ResponseAssertion(String field, int type, String string)
	{
		this();
		setTestField(field);
		setTestType(type);
		getTestStrings().add(string);
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  testField  !ToDo (Parameter description)
	 ***********************************************************/
	public void setTestField(String testField)
	{
		setProperty(TEST_FIELD,testField);
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  testType  !ToDo (Parameter description)
	 ***********************************************************/
	public void setTestType(int testType)
	{
		setProperty(TEST_TYPE,new Integer(testType));
		if ((testType & NOT) > 0)
		{
			notMessage = "not ";
		}
		else
		{
			notMessage = "";
		}
		if ((testType & CONTAINS) > 0)
		{
			failMessage = "to contain: ";
		}
		else
		{
			failMessage = "to match: ";
		}
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  testString  !ToDo (Parameter description)
	 ***********************************************************/
	public void addTestString(String testString)
	{
		getTestStrings().add(testString);
	}

	public void setTestString(String testString,int index)
	{
		getTestStrings().set(index,testString);
	}

	public void removeTestString(String testString)
	{
		getTestStrings().remove(testString);
	}

	public void removeTestString(int index)
	{
		getTestStrings().remove(index);
	}

	public void clearTestStrings()
	{
		getTestStrings().clear();
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@param  response  !ToDo (Parameter description)
	 *@return           !ToDo (Return description)
	 ***********************************************************/
	public AssertionResult getResult(SampleResult response)
	{
		AssertionResult result;
		if (!response.isSuccessful())
		{
			result = new AssertionResult();
			result.setError(true);
			result.setFailureMessage(new String((byte[])response.getResponseData()));
			return result;
		}
		result = evaluateResponse(response);
		return result;
	}


	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public String getTestField()
	{
		return (String)getProperty(TEST_FIELD);
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public int getTestType()
	{
		Object type = getProperty(TEST_TYPE);
		if(type == null)
		{
			return CONTAINS;
		}
		else if(type instanceof Integer)
		{
			return ((Integer)type).intValue();
		}
		else
		{
			return Integer.parseInt((String)type);
		}
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public List getTestStrings()
	{
		return (List)getProperty(TEST_STRINGS);
	}

	public boolean isContainsType()
	{
		return (getTestType() & CONTAINS) > 0;
	}

	public boolean isMatchType()
	{
		return (getTestType() & MATCH) > 0;
	}

	public boolean isNotType()
	{
		return (getTestType() & NOT) > 0;
	}

	public void setToContainsType()
	{
		setTestType((getTestType() | CONTAINS) & (MATCH ^ (CONTAINS | MATCH | NOT)));
		failMessage = "to contain: ";
	}

	public void setToMatchType()
	{
		setTestType((getTestType() | MATCH) & (CONTAINS ^ (CONTAINS | MATCH | NOT)));
		failMessage = "to match: ";
	}

	public void setToNotType()
	{
		setTestType((getTestType() | NOT));
	}

	public void unsetNotType()
	{
		setTestType(getTestType() & (NOT ^ (CONTAINS | MATCH | NOT)));
	}

	private AssertionResult evaluateResponse(SampleResult response)
	{
		boolean pass = true;
		boolean not = (NOT & getTestType()) > 0;
		AssertionResult result = new AssertionResult();
		try
		{
			Iterator iter = getTestStrings().iterator();
			while (iter.hasNext())
			{
				String pattern = (String)iter.next();
				if ((CONTAINS & getTestType()) > 0)
				{
					pass = pass && (not ? !matcher.contains(new String(response.getResponseData()),
							compiler.compile(pattern)) :
							matcher.contains(new String(response.getResponseData()),
							compiler.compile(pattern)));
				}
				else
				{
					pass = pass && (not ? !matcher.matches(new String(response.getResponseData()),
							compiler.compile(pattern)) :
							matcher.matches(new String(response.getResponseData()),
							compiler.compile(pattern)));
				}
				if (!pass)
				{
					result.setFailure(true);
					result.setFailureMessage("Test Failed, expected " + notMessage + failMessage + pattern);
					break;
				}
			}
			if(pass)
			{
				result.setFailure(false);
			}
			result.setError(false);
		}
		catch(MalformedPatternException e)
		{
			result.setError(true);
			result.setFailure(false);
			result.setFailureMessage("Bad test configuration"+e);
		}
		return result;
	}
}
