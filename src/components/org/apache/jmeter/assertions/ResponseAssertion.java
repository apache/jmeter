/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
import java.util.ArrayList;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
/************************************************************
 *  Title: Jakarta-JMeter Description: Copyright: Copyright (c) 2001 Company:
 *  Apache
 *
 * @author     Michael Stover
 * @author     <a href="mailto:jacarlco@katun.com">Jonathan Carlson</a>
 * @created    $Date$
 * @version    $Revision$
 ***********************************************************/
public class ResponseAssertion
   extends AbstractTestElement
   implements Serializable, Assertion
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
   private static ThreadLocal matcher = new ThreadLocal()
   {
      protected Object initialValue()
      {
         return new Perl5Matcher();
      }
   };
   private static PatternCacheLRU patternCache =
      new PatternCacheLRU(1000, new Perl5Compiler());
   /************************************************************
    *  !ToDo (Constructor description)
    ***********************************************************/
   public ResponseAssertion()
   {
      setProperty(new CollectionProperty(TEST_STRINGS, new ArrayList()));
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
      getTestStrings().addProperty(new StringProperty(string,string));
   }
   
   public void clear()
   {
       super.clear();
       setProperty(new CollectionProperty(TEST_STRINGS, new ArrayList()));
   }
   
   /************************************************************
    *  !ToDo (Method description)
    *
    *@param  testField  !ToDo (Parameter description)
    ***********************************************************/
   public void setTestField(String testField)
   {
      setProperty(TEST_FIELD, testField);
   }
   /************************************************************
    *  !ToDo (Method description)
    *
    *@param  testType  !ToDo (Parameter description)
    ***********************************************************/
   public void setTestType(int testType)
   {
      setProperty(new IntegerProperty(TEST_TYPE, testType));
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
      getTestStrings().addProperty(new StringProperty(testString,testString));
   }
   public void setTestString(String testString, int index)
   {
      getTestStrings().set(index, testString);
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
         result.setFailureMessage(response.responseDatatoString());
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
      return getPropertyAsString(TEST_FIELD);
   }
   /************************************************************
    *  !ToDoo (Method description)
    *
    *@return    !ToDo (Return description)
    ***********************************************************/
   public int getTestType()
   {
      JMeterProperty type = getProperty(TEST_TYPE);
      if (type instanceof NullProperty)
      {
         return CONTAINS;
      }
      else
      {
         return type.getIntValue();
      }
   }
   /************************************************************
    *  !ToDoo (Method description)
    *
    *@return    !ToDo (Return description)
    ***********************************************************/
   public CollectionProperty getTestStrings()
   {
      return (CollectionProperty) getProperty(TEST_STRINGS);
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
      setTestType(
         (getTestType() | CONTAINS) & (MATCH ^ (CONTAINS | MATCH | NOT)));
      failMessage = "to contain: ";
   }
   public void setToMatchType()
   {
      setTestType(
         (getTestType() | MATCH) & (CONTAINS ^ (CONTAINS | MATCH | NOT)));
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
   /**
    * Make sure the response satisfies the specified assertion requirements.
    * 
    * @param response an instance of SampleResult
    * @return an instance of AssertionResult
    */
   private AssertionResult evaluateResponse(SampleResult response)
   {
      boolean pass = true;
      boolean not = (NOT & getTestType()) > 0;
      AssertionResult result = new AssertionResult();
      if(response.getResponseData() == null)
      {
          return setResultForNull(result);
      }
      String responseString = new String(response.getResponseData());
      try
      {
         // Get the Matcher for this thread
         Perl5Matcher localMatcher = (Perl5Matcher) matcher.get();
         PropertyIterator iter = getTestStrings().iterator();
         while (iter.hasNext())
         {
            String stringPattern = iter.next().getStringValue();
            Pattern pattern =
               patternCache.getPattern(
                  stringPattern,
                  Perl5Compiler.READ_ONLY_MASK);
            boolean found;
            if ((CONTAINS & getTestType()) > 0)
            {
               found = localMatcher.contains(responseString, pattern);
            }
            else
            {
               found = localMatcher.matches(responseString, pattern);
            }
            pass = not ? !found : found;
            if (!pass)
            {
               result.setFailure(true);
               result.setFailureMessage(
                  "Test Failed, expected "
                     + notMessage
                     + failMessage
                     + stringPattern);
               break;
            }
         }
         if (pass)
         {
            result.setFailure(false);
         }
         result.setError(false);
      }
      catch (MalformedCachePatternException e)
      {
         result.setError(true);
         result.setFailure(false);
         result.setFailureMessage("Bad test configuration" + e);
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
   public static class Test extends junit.framework.TestCase
   {
      int threadsRunning;
      int failed;
      public Test(String name)
      {
         super(name);
      }
      public void testThreadSafety() throws Exception
      {
         Thread[] threads = new Thread[100];
         for (int i = 0; i < threads.length; i++)
         {
            threads[i] = new TestThread();
         }
         failed = 0;
         for (int i = 0; i < threads.length; i++)
         {
            threads[i].start();
            threadsRunning++;
         }
         synchronized (this)
         {
            while (threadsRunning > 0)
            {
               wait();
            }
         }
         assertEquals(failed, 0);
      }
      class TestThread extends Thread
      {
         static final String TEST_STRING = "DAbale arroz a la zorra el abad.";
           // Used to be 'dábale', but caused trouble on Gump. Reasons unknown.
         static final String TEST_PATTERN = ".*A.*\\.";
         public void run()
         {
            ResponseAssertion assertion =
               new ResponseAssertion(RESPONSE_DATA, CONTAINS, TEST_PATTERN);
            SampleResult response = new SampleResult();
            response.setResponseData(TEST_STRING.getBytes());
            for (int i = 0; i < 100; i++)
            {
               AssertionResult result;
               result = assertion.evaluateResponse(response);
               if (result.isFailure() || result.isError())
               {
                  failed++;
               }
            }
            synchronized (Test.this)
            {
               threadsRunning--;
               Test.this.notify();
            }
         }
      }
   }
}
