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
public class SubDummyTest2 extends DummyTestCase {

	/**
	 * 
	 */
	private SubDummyTest2() {
		super();
        System.out.println("private empty constructor");
	}

	/**
	 * @param arg0
	 */
	public SubDummyTest2(String arg0) {
		super(arg0);
        System.out.println("public string constructor");
	}

    public void oneTimeSetUp() {
        System.out.println("oneTimeSetUp called -- ");
    }
    
    public void oneTimeTearDown() {
        System.out.println("oneTimeTearDown called -- ");
    }
}
