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
 
package org.apache.jmeter.control;

import java.util.Enumeration;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;


/**
 * Title: Module Controller
 * The goal of ModuleController is to add modularity to JMeter. The general idea
 * is that web applications consist of small units of functionality (i.e.
 * Logon, Create Account, Logoff...) which consist of requests that implement
 * the functionality. These small units of functionality can be stored in
 * SimpleControllers as modules that can be linked together quickly to form
 * tests. ModuleController facilitates this by acting as a pointer to any
 * controller that sits under the WorkBench. The controller and it's subelements
 * will be substituted in place of the ModuleController at runtime. Config
 * elements can be attached to the ModuleController to alter the functionality
 * (which user logs in, which account is created, etc.) of the module.
 *
 *@author    Thad Smith
 *@created   $Date$
 *@version   1.0
 */
public class ModuleController extends GenericController implements ReplaceableController {

	private JMeterTreeNode selectedNode = null;


	/**
	 * No-arg constructor
	 * 
	 * @see java.lang.Object#Object()
	 */
	public ModuleController() {
	}
	

	/**
	 * Get the controller which this object is "pointing" to
	 * 
	 * @return	TestElement
	 * @see	org.apache.jmeter.testelement.TestElement
	 * @see	org.apache.jmeter.control.ReplaceableController#getReplacement()
	 */
	public TestElement getReplacement() {
		if ( selectedNode != null ) {
			return selectedNode.createTestElement();
		} else {
			return this;
		}
	}

	
	/**
	 * Sets the (@link JMeterTreeNode) which represents the controller which
	 * this object is pointing to. Used for building the test case upon
	 * execution.
	 * 
	 * @param	tn - JMeterTreeNode
	 * @see	org.apache.jmeter.gui.tree.JMeterTreeNode
	 */
	public void setSelectedNode( JMeterTreeNode tn ) {
		selectedNode = tn;
	}
	
	/**
	 * Gets the (@link JMeterTreeNode) for the Controller
	 * 
	 * @return JMeterTreeNode
	 */
	public JMeterTreeNode getSelectedNode() {
		return selectedNode;
	}

	/**
	 * Copies the controller's subelements into the execution tree
	 * 
	 * @param tree - The current tree under which the nodes will be added
	 */
	public void replace( HashTree tree ) {
		createSubTree( tree, selectedNode );
	}
	
	private void createSubTree( HashTree tree, JMeterTreeNode node ) {
		Enumeration e = node.children();
		while ( e.hasMoreElements() ) {
			JMeterTreeNode subNode = (JMeterTreeNode)e.nextElement();
			tree.add(subNode);
			createSubTree( tree.getTree(subNode), subNode );
		}
	}

}

