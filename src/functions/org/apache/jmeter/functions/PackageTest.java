// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
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

/*
 * Package to test functions
 * 
 * Functions are created and parameters set up in one thread.
 * 
 * They are then tested in another thread, or two threads running in parallel
 * 
 */
package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.LinkedList;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author sebb AT apache DOT org
 * @version $Revision$ $Date$
 */
public class PackageTest extends JMeterTestCase
{

	transient private static final Logger log = LoggingManager.getLoggerForClass();

    static{
	    //LoggingManager.setPriority("DEBUG","jmeter");
	    //LoggingManager.setTarget(new java.io.PrintWriter(System.out));
    }

    public PackageTest(String arg0)
    {
        super(arg0);
    }


    // Create the CSVRead function and set its parameters.
	private static CSVRead setParams(String p1, String p2) throws Exception
	{
		CSVRead cr = new CSVRead();
		Collection parms = new LinkedList();
		if (p1 != null) parms.add(new CompoundVariable(p1));
		if (p2 != null) parms.add(new CompoundVariable(p2));
		cr.setParameters(parms);
		return cr;
	}

	// Create the StringFromFile function and set its parameters.
	private static StringFromFile SFFParams(String p1, String p2, String p3, String p4)
	throws Exception
	{
		StringFromFile sff = new StringFromFile();
		Collection parms = new LinkedList();
		if (p1 != null) parms.add(new CompoundVariable(p1));
		if (p2 != null) parms.add(new CompoundVariable(p2));
		if (p3 != null) parms.add(new CompoundVariable(p3));
		if (p4 != null) parms.add(new CompoundVariable(p4));
		sff.setParameters(parms);
		return sff;
	}

	public static Test suite() throws Exception
	{
		   TestSuite suite = new TestSuite("SingleThreaded");
  		   suite.addTest(new PackageTest("CSVParams"));
		   suite.addTest(new PackageTest("CSVNoFile"));
		   suite.addTest(new PackageTest("CSVSetup"));
		   suite.addTest(new PackageTest("CSVRun"));

           suite.addTest(new PackageTest("CSValias"));
           suite.addTest(new PackageTest("CSVBlankLine"));

           //Reset files
           suite.addTest(new PackageTest("CSVSetup"));
		   TestSuite par = new ActiveTestSuite("Parallel");
		   par.addTest(new PackageTest("CSVThread1"));
		   par.addTest(new PackageTest("CSVThread2"));
		   suite.addTest(par);
		   
		   TestSuite sff = new TestSuite("StringFromFile");
		   sff.addTest(new PackageTest("SFFTest1"));
		   suite.addTest(sff);
		   return suite;
    }
    
    public void SFFTest1() throws Exception
    {
		StringFromFile sff1 = SFFParams("testfiles/SFFTest#.txt","","1","3");
		assertEquals("uno",sff1.execute());
		assertEquals("dos",sff1.execute());
		assertEquals("tres",sff1.execute());
		assertEquals("cuatro",sff1.execute());
		assertEquals("cinco",sff1.execute());
		assertEquals("one",sff1.execute());
		assertEquals("two",sff1.execute());
		sff1.execute();
		sff1.execute();
		assertEquals("five",sff1.execute());
		assertEquals("eins",sff1.execute());
		sff1.execute();
		sff1.execute();
		sff1.execute();
		assertEquals("fuenf",sff1.execute());
		assertEquals("**ERR**",sff1.execute());
    }
    
    // Function objects to be tested
    private static CSVRead cr1, cr2, cr3, cr4, cr5, cr6;
    
    // Helper class used to implement co-routine between two threads
    private static class Baton{
    	void pass(){
    		done();
    		try
            {
				//System.out.println(">wait:"+Thread.currentThread().getName());
                wait(1000);
            }
            catch (InterruptedException e)
            {
            	System.out.println(e);
            }
			//System.out.println("<wait:"+Thread.currentThread().getName());

    	}
    	
    	void done(){
			//System.out.println(">done:"+Thread.currentThread().getName());
    		notifyAll();
    	}

    }
    
    private static Baton baton = new Baton();

	public void CSVThread1() throws Exception
	{
		Thread.currentThread().setName("One");
		synchronized(baton){
			
			assertEquals("b1",cr1.execute(null,null));

			assertEquals("",cr4.execute(null,null));
	
			assertEquals("b2",cr1.execute(null,null));
           
			baton.pass();

			assertEquals("",cr4.execute(null,null));
	
			assertEquals("b4",cr1.execute(null,null));

			assertEquals("",cr4.execute(null,null));

			baton.pass();

			assertEquals("b3",cr1.execute(null,null));

			assertEquals("",cr4.execute(null,null));

			baton.done();
		}
	}

	public void CSVThread2() throws Exception
	{
		Thread.currentThread().setName("Two");
		Thread.sleep(500);// Allow other thread to start
		synchronized(baton){

			assertEquals("b3",cr1.execute(null,null));
			
            assertEquals("",cr4.execute(null,null));
			
			baton.pass();
			
			assertEquals("b1",cr1.execute(null,null));

			assertEquals("",cr4.execute(null,null));

			assertEquals("b2",cr1.execute(null,null));
			
			baton.pass();

			assertEquals("",cr4.execute(null,null));

			assertEquals("b4",cr1.execute(null,null));

			baton.done();
		}
	}

    
    public void CSVRun() throws Exception
    {
    	assertEquals("b1",cr1.execute(null,null));
		assertEquals("c1",cr2.execute(null,null));
		assertEquals("d1",cr3.execute(null,null));

		assertEquals("",cr4.execute(null,null));
		assertEquals("b2",cr1.execute(null,null));
		assertEquals("c2",cr2.execute(null,null));
		assertEquals("d2",cr3.execute(null,null));

		assertEquals("",cr4.execute(null,null));
		assertEquals("b3",cr1.execute(null,null));
		assertEquals("c3",cr2.execute(null,null));
		assertEquals("d3",cr3.execute(null,null));

		assertEquals("",cr4.execute(null,null));
		assertEquals("b4",cr1.execute(null,null));
		assertEquals("c4",cr2.execute(null,null));
		assertEquals("d4",cr3.execute(null,null));

		assertEquals("",cr4.execute(null,null));
		assertEquals("b1",cr1.execute(null,null));
		assertEquals("c1",cr2.execute(null,null));
		assertEquals("d1",cr3.execute(null,null));
		
		assertEquals("a1",cr5.execute(null,null));
		assertEquals("",cr6.execute(null,null));
		assertEquals("a2",cr5.execute(null,null));
		
    }
    
    public void CSVParams() throws Exception
    {
		try {
			setParams(null,null);
			fail("Should have failed");
		}
		catch (InvalidVariableException e)
		{
		}
		try {
			setParams(null,"");
			fail("Should have failed");
		}
		catch (InvalidVariableException e)
		{
		}
		try {
			setParams("",null);
			fail("Should have failed");
		}
		catch (InvalidVariableException e)
		{
		}
    }

    public void CSVSetup() throws Exception
    {
    	cr1=setParams("testfiles/test.csv","1");
		cr2=setParams("testfiles/test.csv","2");
		cr3=setParams("testfiles/test.csv","3");
		cr4=setParams("testfiles/test.csv","next");
		cr5=setParams("","0");
		cr6=setParams("","next");
    }
    
    public void CSValias() throws Exception
    {
    	cr1 = setParams("testfiles/test.csv","*A");
    	cr2 = setParams("*A","1");
		cr3 = setParams("*A","next");

		cr4 = setParams("testfiles/test.csv","*B");
		cr5 = setParams("*B","2");
		cr6 = setParams("*B","next");

		String s;
		
		s = cr1.execute(null,null); // open as *A
		assertEquals("",s);
		s = cr2.execute(null,null); // col 1, line 1, *A
		assertEquals("b1",s);
		

		s = cr4.execute(null,null);// open as *B
		assertEquals("",s);
		s = cr5.execute(null,null);// col2 line 1
		assertEquals("c1",s);
		
		s = cr3.execute(null,null);// *A next
		assertEquals("",s);
		s = cr2.execute(null,null);// col 1, line 2, *A
		assertEquals("b2",s);
		
		s = cr5.execute(null,null);// col2, line 1, *B
		assertEquals("c1",s);

		s = cr6.execute(null,null);// *B next
		assertEquals("",s);

		s = cr5.execute(null,null);// col2, line 2, *B
		assertEquals("c2",s);

    }

    public void CSVNoFile() throws Exception
    {
    	String s;

		cr1 = setParams("xtestfiles/test.csv","1");
		log.info("Expecting file not found");
		s = cr1.execute(null,null);
		assertEquals("",s);

		cr2 = setParams("xtestfiles/test.csv","next");
		log.info("Expecting no entry for file");
		s = cr2.execute(null,null);
		assertEquals("",s);

		cr3 = setParams("xtestfiles/test.csv","*ABC");
		log.info("Expecting file not found");
		s = cr3.execute(null,null);
		assertEquals("",s);

		cr4 = setParams("*ABC","1");
		log.info("Expecting cannot open file");
		s = cr4.execute(null,null);
		assertEquals("",s);
    }
    
    // Check blank lines are treated as EOF
    public void CSVBlankLine() throws Exception
    {
    	CSVRead csv1 = setParams("testfiles/testblank.csv","1");
		CSVRead csv2 = setParams("testfiles/testblank.csv","next");
    	
    	String s;
    	
    	for (int i = 1; i<=2; i++)
    	{
	    	s= csv1.execute(null,null);
	    	assertEquals("b1",s);
	    	
			s= csv2.execute(null,null);
			assertEquals("",s);
	    	
			s= csv1.execute(null,null);
			assertEquals("b2",s);
	    	
			s= csv2.execute(null,null);
			assertEquals("",s);
		}
    	
    }
}
