/*
 * Created on Jul 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package woolfel;

import junit.framework.TestCase;

/**
 * @author pete
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DummyTestCase extends TestCase {

	/**
	 * 
	 */
	public DummyTestCase() {
		super();
	}

	/**
	 * @param arg0
	 */
	protected DummyTestCase(String arg0) {
		super(arg0);
	}

    public void setUp(){
        System.out.println("setup called");
    }
    
    public void tearDown(){
        System.out.println("tearDown called");
    }
    
    public void testMethodPass() {
        try {
            Thread.sleep(100);
            assertEquals(10,10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void testMethodPass2() {
        try {
            Thread.sleep(100);
            assertEquals("one","one");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testMethodFail() {
        try {
            Thread.sleep(100);
            assertEquals(20,10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void testMethodFail2() {
        try {
            Thread.sleep(100);
            assertEquals("one","two");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
