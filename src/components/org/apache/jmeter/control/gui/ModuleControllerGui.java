/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.ModuleController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.threads.ThreadGroup;
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
	public ModuleControllerGui() 
	{
		init();
	}


    /* (non-Javadoc)
	 * @see	org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
	 */
	public String getStaticLabel()	
	{
		return STATIC_LABEL;
	}

	
	/* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
	 */
	public void configure(TestElement el) 
	{
		super.configure(el);
		this.selected = ((ModuleController)el).getSelectedNode();
		reinitialize();
	}

    /* (non-Javadoc)
	 * @see	org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() 
	{
		ModuleController mc = new ModuleController();
		configureTestElement(mc);
		if ( selected != null ) 
		{
			mc.setSelectedNode( selected );
		}
		return mc;
	}

    /* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement element) 
	{
		configureTestElement(element);
		TreeNodeWrapper tnw = (TreeNodeWrapper)nodesModel.getSelectedItem();
		if ( tnw != null ) 
		{
			selected = tnw.getTreeNode();
			if ( selected != null ) 
			{
				((ModuleController)element).setSelectedNode( selected );
			}
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


	private void init() 
	{
		setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
        setBorder(makeBorder());
		add(makeTitlePanel());
			
		// DROP-DOWN MENU
		JPanel modulesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modulesPanel.add( new JLabel( CONTROLLER ) );
		nodesModel = new DefaultComboBoxModel();
		nodes = new JComboBox(nodesModel);
		reinitialize();
		
		try 
		{
			Class addToTree = Class.forName("org.apache.jmeter.gui.action.AddToTree");
			Class remove = Class.forName("org.apache.jmeter.gui.action.Remove");
			ActionListener listener = new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					reinitialize();
				}
			};
			ActionRouter ar = ActionRouter.getInstance();
			ar.addPostActionListener( addToTree, listener );
			ar.addPostActionListener( remove, listener );
		} 
		catch ( ClassNotFoundException e ) 
		{
		}
		modulesPanel.add( nodes );
		add( modulesPanel );
	}
	
	private void reinitialize() 
	{
		TreeNodeWrapper current;
		nodesModel.removeAllElements();
		GuiPackage gp = GuiPackage.getInstance();
		JMeterTreeNode root;
		if (gp != null)
		{
			root = (JMeterTreeNode)GuiPackage.getInstance().getTreeModel().getRoot();
			buildNodesModel( root, "", 0 );
		}
		if ( selected != null )
		{
			for ( int i=0; i< nodesModel.getSize(); i++ ) 
			{
				current = (TreeNodeWrapper)nodesModel.getElementAt(i);
				if ( current.getTreeNode().equals(selected) ) 
				{
					nodesModel.setSelectedItem(current);
					break;
				}
			}
		}
	}

	private void buildNodesModel( JMeterTreeNode node, String parent_name, int level ) 
	{
		String seperator = " > ";
		if ( node != null ) 
		{
			for ( int i=0; i<node.getChildCount(); i++ ) 
			{
				StringBuffer name = new StringBuffer();
				JMeterTreeNode cur = (JMeterTreeNode)node.getChildAt(i);
				TestElement te = cur.createTestElement();
				if ( te instanceof ThreadGroup )
				{
					name.append(parent_name);
					name.append(cur.getName());
					name.append(seperator);
					buildNodesModel( cur, name.toString(), level );
				}
				else if ( te instanceof Controller && !( te instanceof ModuleController ) ) 
				{
					name.append(spaces(level));
					name.append(parent_name);
					name.append(cur.getName());
					TreeNodeWrapper tnw = new TreeNodeWrapper( cur, name.toString() );
					nodesModel.addElement(tnw);
					name = new StringBuffer();
					name.append(cur.getName());
					name.append(seperator);
					buildNodesModel( cur, name.toString(), level+1 );
				}
				else if ( te instanceof TestPlan || te instanceof WorkBench )
				{
					name.append(cur.getName());
					name.append(seperator);
					buildNodesModel( cur, name.toString(), 0 );
				}
			}
		}
	}
	
	private String spaces(int level)
	{
		int multi = 4;
		StringBuffer spaces = new StringBuffer(level*multi);
		for (int i=0; i<level*multi; i++)
		{
			spaces.append(" ");
		}
		return spaces.toString();
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
