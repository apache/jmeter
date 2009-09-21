/*
 * Created on Jul 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package woolfel;

/**
 * @author pete
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SubDummyTest extends DummyTestCase {

	/**
	 * 
	 */
	public SubDummyTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 */
	public SubDummyTest(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

    public void oneTimeSetUp() {
        System.out.println("oneTimeSetUp called -- ");
    }
    
    public void oneTimeTearDown() {
        System.out.println("oneTimeTearDown called -- ");
    }
}
