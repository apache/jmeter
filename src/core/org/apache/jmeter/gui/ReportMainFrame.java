// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.GlobalMouseListener;
import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;

/**
 * The main JMeter frame, containing the menu bar, test tree, and an area for
 * JMeter component GUIs.
 * 
 * @author Michael Stover
 * @version $Revision$
 */
public class ReportMainFrame extends MainFrame {

	/** An image which is displayed when a test is running. */
	private ImageIcon runningIcon = JMeterUtils.getImage("thread.enabled.gif");

	/** An image which is displayed when a test is not currently running. */
	private ImageIcon stoppedIcon = JMeterUtils.getImage("thread.disabled.gif");

	/** The button used to display the running/stopped image. */
	private JButton runningIndicator;

	/** The set of currently running hosts. */
	private Set hosts = new HashSet();

	/** A message dialog shown while JMeter threads are stopping. */
	private JDialog stoppingMessage;

	/**
	 * Create a new JMeter frame.
	 * 
	 * @param actionHandler
	 *            this parameter is not used
	 * @param treeModel
	 *            the model for the test tree
	 * @param treeListener
	 *            the listener for the test tree
	 */
	public ReportMainFrame(ActionListener actionHandler, TreeModel treeModel,
            JMeterTreeListener treeListener) {
        super(actionHandler,treeModel,treeListener);
	}

	/**
	 * Default constructor for the JMeter frame. This constructor will not
	 * properly initialize the tree, so don't use it.
	 */
	public ReportMainFrame() {
		// TODO: Can we remove this constructor? JMeter won't behave properly
		// if it used.
	}

	// MenuBar related methods
	// TODO: Do we really need to have all these menubar methods duplicated
	// here? Perhaps we can make the menu bar accessible through GuiPackage?

	/**
	 * Specify whether or not the File|Load menu item should be enabled.
	 * 
	 * @param enabled
	 *            true if the menu item should be enabled, false otherwise
	 */
	public void setFileLoadEnabled(boolean enabled) {
        super.setFileLoadEnabled(enabled);
	}

	/**
	 * Specify whether or not the File|Save menu item should be enabled.
	 * 
	 * @param enabled
	 *            true if the menu item should be enabled, false otherwise
	 */
	public void setFileSaveEnabled(boolean enabled) {
		super.setFileSaveEnabled(enabled);
	}

	/**
	 * Set the menu that should be used for the Edit menu.
	 * 
	 * @param menu
	 *            the new Edit menu
	 */
	public void setEditMenu(JPopupMenu menu) {
		super.setEditMenu(menu);
	}

	/**
	 * Specify whether or not the Edit menu item should be enabled.
	 * 
	 * @param enabled
	 *            true if the menu item should be enabled, false otherwise
	 */
	public void setEditEnabled(boolean enabled) {
		super.setEditEnabled(enabled);
	}

	/**
	 * Set the menu that should be used for the Edit|Add menu.
	 * 
	 * @param menu
	 *            the new Edit|Add menu
	 */
	public void setEditAddMenu(JMenu menu) {
		super.setEditAddMenu(menu);
	}

	/**
	 * Specify whether or not the Edit|Add menu item should be enabled.
	 * 
	 * @param enabled
	 *            true if the menu item should be enabled, false otherwise
	 */
	public void setEditAddEnabled(boolean enabled) {
		super.setEditAddEnabled(enabled);
	}

	/**
	 * Specify whether or not the Edit|Remove menu item should be enabled.
	 * 
	 * @param enabled
	 *            true if the menu item should be enabled, false otherwise
	 */
	public void setEditRemoveEnabled(boolean enabled) {
		super.setEditRemoveEnabled(enabled);
	}

	/***************************************************************************
	 * !ToDoo (Method description)
	 * 
	 * @return !ToDo (Return description)
	 **************************************************************************/
	public JTree getTree() {
		return super.getTree();
	}

	// TestListener implementation

	/**
	 * Not sure if this should be in the ReportMainFrame, since the
     * report component doesn't really test, it generates reports. for
     * now, I will use it to trigger reporting. Later we can refactor
     * MainFrame and create an abstract base class.
	 */
	public void testStarted() {
        
        // super.testStarted();
	}

	/**
     * Not sure if this should be in the ReportMainFrame, since the
     * report component doesn't really test, it generates reports. for
     * now, I will use it to trigger reporting. Later we can refactor
     * MainFrame and create an abstract base class.
	 */
	public void testStarted(String host) {
        // super.testStarted(host);
	}

	/**
     * Not sure if this should be in the ReportMainFrame, since the
     * report component doesn't really test, it generates reports. for
     * now, I will use it to trigger reporting. Later we can refactor
     * MainFrame and create an abstract base class.
	 */
	public void testEnded() {
        // super.testEnded();
	}

	/**
     * Not sure if this should be in the ReportMainFrame, since the
     * report component doesn't really test, it generates reports. for
     * now, I will use it to trigger reporting. Later we can refactor
     * MainFrame and create an abstract base class.
	 */
	public void testEnded(String host) {
        // super.testEnded(host);
	}

	/* Implements TestListener#testIterationStart(LoopIterationEvent) */
	public void testIterationStart(LoopIterationEvent event) {
	}

	/**
	 * Create the GUI components and layout.
	 */
	protected void init() {
        super.init();
        /**
		menuBar = new JMeterMenuBar();
		setJMenuBar(menuBar);

		JPanel all = new JPanel(new BorderLayout());
		all.add(createToolBar(), BorderLayout.NORTH);

		JSplitPane treeAndMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		treePanel = createTreePanel();
		treeAndMain.setLeftComponent(treePanel);

		mainPanel = createMainPanel();
		treeAndMain.setRightComponent(mainPanel);

		treeAndMain.setResizeWeight(.2);
		treeAndMain.setContinuousLayout(true);
		all.add(treeAndMain, BorderLayout.CENTER);

		getContentPane().add(all);

		tree.setSelectionRow(1);
		addWindowListener(new WindowHappenings());
		addMouseListener(new GlobalMouseListener());
	    */
    }

	/**
	 * Create the JMeter tool bar pane containing the running indicator.
	 * 
	 * @return a panel containing the running indicator
	 */
	protected Component createToolBar() {
        return super.createToolBar();
	}

	/**
	 * Create the panel where the GUI representation of the test tree is
	 * displayed. The tree should already be created before calling this method.
	 * 
	 * @return a scroll pane containing the test tree GUI
	 */
	protected JScrollPane createTreePanel() {
		return super.createTreePanel();
	}

	/**
	 * Create the main panel where components can display their GUIs.
	 * 
	 * @return the main scroll pane
	 */
	protected JScrollPane createMainPanel() {
		return super.createMainPanel();
	}

	/**
	 * Create and initialize the GUI representation of the test tree.
	 * 
	 * @param treeModel
	 *            the test tree model
	 * @param treeListener
	 *            the test tree listener
	 * 
	 * @return the initialized test tree GUI
	 */
	private JTree makeTree(TreeModel treeModel, JMeterTreeListener treeListener) {
		JTree treevar = new JTree(treeModel);
		treevar.setCellRenderer(getCellRenderer());
		treevar.setRootVisible(false);
		treevar.setShowsRootHandles(true);

		treeListener.setJTree(treevar);
		treevar.addTreeSelectionListener(treeListener);
		treevar.addMouseListener(treeListener);
		treevar.addMouseMotionListener(treeListener);
		treevar.addKeyListener(treeListener);

		return treevar;
	}

}
