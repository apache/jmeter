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

package org.apache.jmeter.control.gui;


import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.ModuleController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;



/****************************************
 * Title: ModuleController Gui
 *
 *@author    Thad Smith
 *@created   $Date$
 *@version   1.0
 ****************************************/
public class ModuleControllerGui extends AbstractControllerGui /*implements UnsharedComponent*/ {

	private JMeterTreeNode selected = null;

	private JComboBox nodes;
	private DefaultComboBoxModel nodesModel;

	public static String STATIC_LABEL = "ModuleController";
	public static String CONTROLLER = "Module To Run";
	

	/**
	 * Initializes the gui panel for the ModuleController instance.
	 */
	public ModuleControllerGui() {
		initialize();
		setName(getStaticLabel());		
	}


	/**
	 * @see	String
	 */
	public void setName(String name) {
		namePanel.setName(name);
	}

	/**
	 * @see	String
	 */
	public String getName() {
		return namePanel.getName();
	}

	/** 
	 * @see	org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
	 * @see	String
	 */
	public String getStaticLabel()	{
		return STATIC_LABEL;
	}

	
	/**
	 * @see	org.apache.jmeter.AbstractControllerGui#configure()
	 * @see	org.apache.jmeter.testelement.TestElement
	 */
	public void configure(TestElement el) {
		super.configure(el);
		this.selected = ((ModuleController)el).getSelectedNode();
		reinitialize();
	}

	/**
	 * @see	org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 * @see	org.apache.jmeter.testelement.TestElement
	 */
	public TestElement createTestElement() {
		ModuleController mc = new ModuleController();
		configureTestElement(mc);
		
		if ( selected != null ) {
			mc.setSelectedNode( selected );
		}

		return mc;
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		configureTestElement(element);
		selected = ((TreeNodeWrapper)nodesModel.getSelectedItem()).getTreeNode();
		if ( selected != null ) {
			((ModuleController)element).setSelectedNode( selected );
		}
	}

	public JPopupMenu createPopupMenu()
	{
		JPopupMenu menu = new JPopupMenu();
		JMenu addMenu = MenuFactory.makeMenus(
				new String[] 	{	MenuFactory.CONFIG_ELEMENTS, 
									MenuFactory.ASSERTIONS,
									MenuFactory.TIMERS, 
									MenuFactory.LISTENERS, 
								}, 
				JMeterUtils.getResString("Add"),
				"Add" );
		menu.add(addMenu);
		MenuFactory.addEditMenu(menu, true);
		MenuFactory.addFileMenu(menu);
		return menu;
	}


	private void initialize() {
		
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		// TITLE
		JLabel panelTitleLabel = new JLabel(getStaticLabel());
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel);

		// NAME
		namePanel = new NamePanel();
		mainPanel.add(namePanel);

		this.add(mainPanel);
			
		// DROP-DOWN MENU
		JPanel containersPanel = new JPanel();
		containersPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		containersPanel.add( new JLabel( CONTROLLER ) );
		
		nodesModel = new DefaultComboBoxModel();

		nodes = new JComboBox(nodesModel);
		
		reinitialize();
		
		try {
			
			Class addToTree = Class.forName("org.apache.jmeter.gui.action.AddToTree");
			Class remove = Class.forName("org.apache.jmeter.gui.action.Remove");
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					reinitialize();
				}
			};

			ActionRouter ar = ActionRouter.getInstance();
			ar.addPostActionListener( addToTree, listener );
			ar.addPostActionListener( remove, listener );
			
		} catch ( ClassNotFoundException e ) {
		}
		 
		containersPanel.add( nodes );
		mainPanel.add( containersPanel );

	}
	
	private void reinitialize() {

		TreeNodeWrapper current;
		nodesModel.removeAllElements();
		JMeterTreeNode wb = getWorkBench();
		buildNodesModel( wb, "" );
		
		if ( selected != null ) {
			for ( int i=0; i< nodesModel.getSize(); i++ ) {
				current = (TreeNodeWrapper)nodesModel.getElementAt(i);
				if ( current.getTreeNode().equals(selected) ) {
					nodesModel.setSelectedItem(current);
					break;
				}
			}
		}
	}

	private JMeterTreeNode getWorkBench() {

		GuiPackage gp = GuiPackage.getInstance();
		JMeterTreeModel tm = null;
		JMeterTreeNode root = null;
		JMeterTreeNode wb = null;

		if ( gp != null ) {
			
			tm = gp.getTreeModel();
			root = (JMeterTreeNode)tm.getRoot();
			wb = null;
		
			int cc = root.getChildCount();
			
			for ( int i=0; i<cc; i++ ) {
				
				JMeterTreeNode cur = (JMeterTreeNode)root.getChildAt(i);
		
				if ( cur.getUserObject() instanceof WorkBench ) {
					wb = cur;
				}
			
			}

		}

		return wb;
		
	}
	
	private void buildNodesModel( JMeterTreeNode node, String parent_name ) {

		if ( node != null ) {

			int cc = node.getChildCount();
	
			for ( int i=0; i<cc; i++ ) {
				
				JMeterTreeNode cur = (JMeterTreeNode)node.getChildAt(i);
				TestElement te = cur.createTestElement();
				if ( te instanceof Controller && !( te instanceof ModuleController ) ) {

					TreeNodeWrapper tnw = new TreeNodeWrapper( cur, parent_name + cur.getName() );
					nodesModel.addElement(tnw);
					
					String name;
					name = cur.getName() + " > ";
					buildNodesModel( cur, name );

				}

			}

		}
		
	}

}

class TreeNodeWrapper {
	
	private JMeterTreeNode tn;
	private String label;

	private TreeNodeWrapper() {};
	
	public TreeNodeWrapper( JMeterTreeNode tn, String label ) {
		this.tn = tn;
		this.label = label; 
	}	

	public JMeterTreeNode getTreeNode() {
		return tn;
	}
	
	public String toString() {
		return label;
	}

}
