package org.apache.jmeter.control;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;


/**	
 * 	@author	 Thad Smith
 *
 *	This interface represents a controller that gets replaced
 *	during the compilation phase of test execution in an arbitrary
 *	way.
 *
 *	@see	org.apache.jmeter.gui.action.AbstractAction.java
 */
public interface ReplaceableController {
	
	/**
	 * Returns the TestElement that should replace the current 
	 * ReplaceableCoroller.
	 * 
	 * @return TestElement
	 * @see	org.apache.jmeter.testelement.TestElement
	 */
	public TestElement getReplacement();
	
	/**
	 * Used to replace the test execution tree (usually by adding the
	 * subelements of the TestElement that is replacing the
	 * ReplaceableController.
	 * 
	 * @param	tree - The current HashTree to be executed.
	 * @see	org.apache.jorphan.collections.HashTree
	 * @see	org.apache.jmeter.gui.action.AbstractAction#convertSubTree
	 */
	public void replace( HashTree tree );

}
