// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version   $Revision$
 */
public class WhileController extends GenericController implements Serializable
{
    private static Logger log = LoggingManager.getLoggerForClass();
	private final static String CONDITION = "WhileController.condition"; // $NON-NLS-1$
	
	private static boolean testMode=false; // To make testing easier
	private static boolean testModeResult=false; // dummy sample result

    public WhileController()
    {
    }


    /* (non-Javadoc)
     * @see org.apache.jmeter.control.Controller#isDone()
     */
    public boolean isDone()
    {
        return false;// Never want to remove the controller from the tree
    }

    /*
     * Evaluate the condition, which can be:
     * blank or LAST = was the last sampler OK?
     * otherwise, evaluate the condition to see if it is not "false"
     * If blank, only evaluate at the end of the loop
     * 
     * Must only be called at start and end of loop
     * 
     * @param loopEnd - are we at loop end?
     * @return true means OK to continue 
     */
    private boolean conditionTrue(boolean loopEnd)
    {
//		clear cached condition
		getProperty(CONDITION).recoverRunningVersion(null);
    	String cnd = getCondition();
    	log.debug("Condition string:"+cnd);
    	boolean res;
    	// If blank, only check previous sample when at end of loop
    	if ((loopEnd && cnd.length() == 0) 
    			|| "LAST".equalsIgnoreCase(cnd)) {// $NON-NLS-1$
			if (testMode) {
			 	res=testModeResult;
			} else {
        	    JMeterVariables threadVars = 
        		    JMeterContextService.getContext().getVariables();
        	    // Use !false rather than true, so that null is treated as true 
       	        res = !"false".equalsIgnoreCase(threadVars.get(JMeterThread.LAST_SAMPLE_OK));// $NON-NLS-1$
    	    }
    	} else {
    		// cnd may be blank if next() called us
    		res = !"false".equalsIgnoreCase(cnd);// $NON-NLS-1$
    	}
    	log.debug("Condition value: "+res);
        return res;
    }

	/* (non-Javadoc)
	 * Only called at End of Loop
     * @see org.apache.jmeter.control.GenericController#nextIsNull()
     */
    protected Sampler nextIsNull() throws NextIsNullException
    {
        reInitialize();
        if (conditionTrue(true))
        {
            return super.next();
        }
        else
        {
            setDone(true);
            return null;
        }
    }

    public Sampler next()
    {
		if (current!=0){ // in the middle of the loop
			return super.next();
		}
		// Must be start of loop
        if(conditionTrue(false)) // Still OK
        {
            return super.next(); // OK to continue
        }
        else
        {
            reInitialize(); // Don't even start the loop
            return null;
        }
    }

	/**
	 * @param string the condition to save
	 */
	public void setCondition(String string) {
		log.debug("setCondition("+ string+")");
		setProperty(new StringProperty(CONDITION, string));
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		String cnd;
		cnd=getPropertyAsString(CONDITION);
		log.debug("getCondition() => "+cnd);
		return cnd;
	}
    public static class Test extends JMeterTestCase
    {
	    static{
		    //LoggingManager.setPriority("DEBUG","jmeter");
		    //LoggingManager.setTarget(new java.io.PrintWriter(System.out));
	    }

        public Test(String name)
        {
            super(name);
        }

		// Get next sample and its name
		private String nextName(GenericController c){
			Sampler s = c.next();
			if (s==null){
				return null;
			} else {
			    return s.getPropertyAsString(TestElement.NAME);
			}
		}
		
		// While (blank), previous sample OK - should loop until false
		public void testBlankPrevOK() throws Exception
		{
			log.info("testBlankPrevOK");
			runtestPrevOK("");
		}
		
		// While (LAST), previous sample OK - should loop until false
		public void testLastPrevOK() throws Exception
		{
			log.info("testLASTPrevOK");
			runtestPrevOK("LAST");
		}
		
		private static final String OTHER = "X"; // Dummy for testing functions
		// While (LAST), previous sample OK - should loop until false
		public void testOtherPrevOK() throws Exception
		{
			log.info("testOtherPrevOK");
			runtestPrevOK(OTHER);
		}
		
        public void runtestPrevOK(String type) throws Exception
        {
			testMode=true;
			testModeResult=true;
            GenericController controller = new GenericController();
            WhileController while_cont = new WhileController();
			while_cont.setCondition(type);
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            while_cont.addTestElement(new TestSampler("three"));
            controller.addTestElement(while_cont);			
            controller.addTestElement(new TestSampler("four"));
            controller.initialize();
			assertEquals("one",nextName(controller));
			assertEquals("two",nextName(controller));
			assertEquals("three",nextName(controller));
			assertEquals("one",nextName(controller));
			assertEquals("two",nextName(controller));
			assertEquals("three",nextName(controller));
			assertEquals("one",nextName(controller));
			testModeResult=false;// one and two fail
			if (type.equals(OTHER)) while_cont.setCondition("false");
			assertEquals("two",nextName(controller));
			assertEquals("three",nextName(controller));
			testModeResult=true;// but three OK, so does not exit
			if (type.equals(OTHER)) while_cont.setCondition(OTHER);
			assertEquals("one",nextName(controller));
			assertEquals("two",nextName(controller));
			assertEquals("three",nextName(controller));
			testModeResult=false;// three fails, so exits while
			if (type.equals(OTHER)) while_cont.setCondition("false");
			assertEquals("four",nextName(controller));
			assertNull(nextName(controller));
			testModeResult=true;
			if (type.equals(OTHER)) while_cont.setCondition(OTHER);
			assertEquals("one",nextName(controller));
	    }
		
		// While (blank), previous sample failed - should run once
        public void testBlankPrevFailed() throws Exception
        {
			log.info("testBlankPrevFailed");
			testMode=true;
			testModeResult=false;
            GenericController controller = new GenericController();
            WhileController while_cont = new WhileController();
			while_cont.setCondition("");
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            controller.addTestElement(while_cont);			
            controller.addTestElement(new TestSampler("three"));
            controller.initialize();
			assertEquals("one",nextName(controller));
			assertEquals("two",nextName(controller));
			assertEquals("three",nextName(controller));
			assertNull(nextName(controller));
			assertEquals("one",nextName(controller));
			assertEquals("two",nextName(controller));
			assertEquals("three",nextName(controller));
			assertNull(nextName(controller));
        }

		// While LAST,  previous sample failed - should not run
        public void testLASTPrevFailed() throws Exception
        {
			log.info("testLastPrevFailed");
			runTestPrevFailed("LAST");
        }
		// While False,  previous sample failed - should not run
        public void testfalsePrevFailed() throws Exception
        {
			log.info("testFalsePrevFailed");
			runTestPrevFailed("False");
        }
	    public void runTestPrevFailed(String s) throws Exception
	    {
			testMode=true;
			testModeResult=false;
            GenericController controller = new GenericController();
            WhileController while_cont = new WhileController();
			while_cont.setCondition(s);
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            controller.addTestElement(while_cont);			
            controller.addTestElement(new TestSampler("three"));
            controller.initialize();
			assertEquals("three",nextName(controller));
			assertNull(nextName(controller));
			assertEquals("three",nextName(controller));
			assertNull(nextName(controller));
        }

		// Tests for Stack Overflow (bug 33954)
		public void testAlwaysFailOK() throws Exception
	    {
			runTestAlwaysFail(true); // Should be OK
	    }
		
		// TODO - re-enable when fix found
		public void disabletestAlwaysFailBAD() throws Exception
	    {
			runTestAlwaysFail(false); // Currently fails
	    }
		
		public void runTestAlwaysFail(boolean other)
		{
			testMode=true;
			testModeResult=false;
            LoopController controller = new LoopController();
			controller.setContinueForever(true);
			controller.setLoops(-1);
            WhileController while_cont = new WhileController();
			while_cont.setCondition("false");
            while_cont.addTestElement(new TestSampler("one"));
            while_cont.addTestElement(new TestSampler("two"));
            controller.addTestElement(while_cont);			
            if (other) controller.addTestElement(new TestSampler("three"));
            controller.initialize();
			try {
				if (other){
			    assertEquals("three",nextName(controller));
				} else {
					assertNull(nextName(controller));
				}
			} catch (StackOverflowError e){
				//e.printStackTrace();
				fail(e.toString());
			}
        }

   }
}