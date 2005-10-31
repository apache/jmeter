/*
 * Created on Oct 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.jorphan.gui;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.jorphan.reflect.Functor;

/**
 * @author pete
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DefaultTreeTableModel extends AbstractTreeTableModel {

    public DefaultTreeTableModel() {
        this(new DefaultMutableTreeNode());
    }
    
	/**
	 * @param root
	 */
	public DefaultTreeTableModel(TreeNode root) {
		super(root);
	}

	/**
	 * @param root
	 * @param editable
	 */
	public DefaultTreeTableModel(TreeNode root, boolean editable) {
		super(root, editable);
	}

	/**
	 * @param headers
	 * @param readFunctors
	 * @param writeFunctors
	 * @param editorClasses
	 */
	public DefaultTreeTableModel(String[] headers, Functor[] readFunctors,
			Functor[] writeFunctors, Class[] editorClasses) {
		super(headers, readFunctors, writeFunctors, editorClasses);
	}

}
