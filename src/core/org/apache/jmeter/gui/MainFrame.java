/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.gui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

import org.apache.jmeter.engine.event.IterationEvent;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.GlobalMouseListener;
import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class MainFrame extends JFrame implements TestListener,Remoteable
{
	transient private static Logger log =
			Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");
	JPanel all;
    private JScrollPane mainPanel;
	Box toolPanel;
	JScrollPane treePanel;
	JMeterMenuBar menuBar;
	JTree tree;
	TreeModel treeModel;
	ActionListener actionHandler;
	JMeterTreeListener treeListener;
	ImageIcon runningIcon = JMeterUtils.getImage("thread.enabled.gif");
	ImageIcon stoppedIcon = JMeterUtils.getImage("thread.disabled.gif");
	JButton runningIndicator;
	private boolean running;
	int previousDragXLocation = 0;
	int previousDragYLocation = 0;
	private Set hosts = new HashSet();
	JDialog stoppingMessage;

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param actionHandler  !ToDo (Parameter description)
	 *@param treeModel      !ToDo (Parameter description)
	 *@param treeListener   !ToDo (Parameter description)
	 ***************************************/
	public MainFrame(ActionListener actionHandler, TreeModel treeModel, JMeterTreeListener
			treeListener)
	{
		runningIndicator = new JButton(stoppedIcon);
		runningIndicator.setMargin(new Insets(0,0,0,0));
		runningIndicator.setBorder(BorderFactory.createEmptyBorder());
		this.treeListener = treeListener;
		this.actionHandler = actionHandler;
		this.treeModel = treeModel;
		GuiPackage.getInstance().setMainFrame(this);
		init();
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}

	public MainFrame()
	{
	}


	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setFileSaveEnabled(boolean enabled)
	{
		menuBar.setFileSaveEnabled(enabled);
	}

	public void showStoppingMessage(String host)
	{
		stoppingMessage = new JDialog(this,
				JMeterUtils.getResString("stopping_test_title"),true);
		JLabel stopLabel = new JLabel(JMeterUtils.getResString("stopping_test")+": "+host);
		stopLabel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		stoppingMessage.getContentPane().add(stopLabel);
		stoppingMessage.pack();
		ComponentUtil.centerComponentInComponent(this,stoppingMessage);
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					if(stoppingMessage != null)
					{
						stoppingMessage.show();
					}
				}
			});
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setFileLoadEnabled(boolean enabled)
	{
		menuBar.setFileLoadEnabled(enabled);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param menu  !ToDo (Parameter description)
	 ***************************************/
	public void setEditAddMenu(JMenu menu)
	{
		menuBar.setEditAddMenu(menu);
	}

	public void setEditMenu(JPopupMenu menu)
	{
		menuBar.setEditMenu(menu);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setEditAddEnabled(boolean enabled)
	{
		menuBar.setEditAddEnabled(enabled);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setEditRemoveEnabled(boolean enabled)
	{
		menuBar.setEditRemoveEnabled(enabled);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param enabled  !ToDo (Parameter description)
	 ***************************************/
	public void setEditEnabled(boolean enabled)
	{
		menuBar.setEditEnabled(enabled);
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void closeMenu()
	{
		if(menuBar.isSelected())
		{
			MenuElement[] menuElement = menuBar.getSubElements();
			if(menuElement != null)
			{
				for(int i = 0; i < menuElement.length; i++)
				{
					JMenu menu = (JMenu)menuElement[i];
					if(menu.isSelected())
					{
						menu.setPopupMenuVisible(false);
						menu.setSelected(false);
						break;
					}
				}
			}
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param comp  !ToDo (Parameter description)
	 ***************************************/
	public void setMainPanel(JComponent comp)
	{
        // Set the preferred size to the minimum size of the component.  The
        // scroll pane (mainPanel) will always try to put the component at its
        // preferred size (but allow it to grow larger if there is extra space).
        // We don't want the scroll bars to show up until the component has
        // shrunk as much as it can -- to its minimum size.  That way, if the
        // component contains its own scroll panes, then those will get used
        // and the mainPanel scroll bars won't show up unless the component is
        // still too large for the area.
        comp.setPreferredSize(comp.getMinimumSize());
        mainPanel.setViewportView(comp);
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public JTree getTree()
	{
		return tree;
	}

	public void testStarted(String host)
	{
		hosts.add(host);
		runningIndicator.setIcon(runningIcon);
		menuBar.setRunning(true,host);
	}

	public void testStarted()
	{
		testStarted("local");
		menuBar.setEnabled(true);
	}

	public void testEnded()
	{
		testEnded("local");
		menuBar.setEnabled(false);
		if(stoppingMessage != null)
		{
			stoppingMessage.dispose();
			stoppingMessage = null;
		}
	}

	public void testEnded(String host)
	{
		hosts.remove(host);
		if(hosts.size() == 0)
		{
			runningIndicator.setIcon(stoppedIcon);
		}
		menuBar.setRunning(false,host);
		if(stoppingMessage != null)
		{
			stoppingMessage.dispose();
			stoppingMessage = null;
		}
	}

	private void init()
	{
		menuBar = new JMeterMenuBar();
		createToolBar();
		createMainPanel();
		createTreePanel();
		addThemAll();
		addWindowListener(new WindowHappenings());
		tree.setSelectionRow(1);
		this.addMouseListener(new GlobalMouseListener());
	}

	private TreeCellRenderer getCellRenderer()
	{
		DefaultTreeCellRenderer rend = new JMeterCellRenderer();
		rend.setFont(new Font("Dialog", Font.PLAIN, 11));
		return rend;
	}

	private void addThemAll()
	{
		this.setJMenuBar(menuBar);
		all = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		{
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.weighty = 0;
			all.add(toolPanel, gbc.clone());
		}
		JSplitPane treeAndMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		treeAndMain.setLeftComponent(treePanel);
		treeAndMain.setRightComponent(mainPanel);

		// The setResizeWeight() method was added to JDK1.3. For now, JMeter should
		// remain compatible with JDK1.2.
		//treeAndMain.setResizeWeight(.2);

		treeAndMain.setContinuousLayout(true);
		{
			gbc.gridy++;
			gbc.weighty = 1;
			all.add(treeAndMain, gbc.clone());
		}
		this.getContentPane().add(all);
	}

	private void createToolBar()
	{
		toolPanel = new Box(BoxLayout.X_AXIS);
		toolPanel.add(Box.createRigidArea(new Dimension(10,15)));
		toolPanel.add(Box.createGlue());
		toolPanel.add(runningIndicator);
	}

	private void createTreePanel()
	{
		treePanel = new JScrollPane(makeTree());
		treePanel.setMinimumSize(new Dimension(100,0));
	}

	public void drawDraggedComponent(Component dragIcon,int x,int y)
	{
		Dimension size = dragIcon.getPreferredSize();
		treePanel.paintImmediately(previousDragXLocation,previousDragYLocation,size.width,size.height);
		this.getLayeredPane().setLayer(dragIcon,400);
		SwingUtilities.paintComponent(treePanel.getGraphics(),
				dragIcon,treePanel,x,y,size.width,size.height);
		previousDragXLocation = x;
		previousDragYLocation = y;
	}

	private void createMainPanel()
	{
        mainPanel = new JScrollPane();
	}

	private JTree makeTree()
	{
		tree = new JTree(this.treeModel);
		tree.setCellRenderer(getCellRenderer());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		treeListener.setJTree(tree);
		tree.addTreeSelectionListener(treeListener);
		tree.addMouseListener(treeListener);
		tree.addMouseMotionListener(treeListener);
		tree.addKeyListener(treeListener);
		return tree;
	}

	/****************************************
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	private class WindowHappenings extends WindowAdapter
	{
		/****************************************
		 * !ToDo (Method description)
		 *
		 *@param event  !ToDo (Parameter description)
		 ***************************************/
		public void windowClosing(WindowEvent event)
		{
			ActionRouter.getInstance().actionPerformed(new ActionEvent(
					this, event.getID(), "exit"));
		}
	}
	
    /**
     * @see org.apache.jmeter.testelement.TestListener#iterationStart(org.apache.jmeter.engine.event.IterationEvent)
     */
    public void testIterationStart(IterationEvent event)
    {}

}
