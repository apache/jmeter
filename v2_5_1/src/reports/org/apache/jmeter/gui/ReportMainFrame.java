/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.gui.util.ReportMenuBar;
import org.apache.jmeter.report.gui.action.ReportActionRouter;
import org.apache.jmeter.report.gui.tree.ReportCellRenderer;
import org.apache.jmeter.report.gui.tree.ReportTreeListener;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * ReportMainFrame is based on MainFrame. it uses the same basic structure,
 * but with changes for the report gui.
 *
 */
public class ReportMainFrame extends JFrame implements TestListener, Remoteable {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    // The default title for the Menu bar
    private static final String DEFAULT_TITLE =
        "Apache JMeter ("+JMeterUtils.getJMeterVersion()+")"; // $NON-NLS-1$ $NON-NLS-2$

    /** The menu bar. */
    protected ReportMenuBar menuBar;

    /** The main panel where components display their GUIs. */
    protected JScrollPane mainPanel;

    /** The panel where the test tree is shown. */
    protected JScrollPane treePanel;

    /** The test tree. */
    protected JTree tree;

    /** An image which is displayed when a test is running. */
    //private ImageIcon runningIcon = JMeterUtils.getImage("thread.enabled.gif");

    /** An image which is displayed when a test is not currently running. */
    private ImageIcon stoppedIcon = JMeterUtils.getImage("thread.disabled.gif");// $NON-NLS-1$

    /** The x coordinate of the last location where a component was dragged. */
    private int previousDragXLocation = 0;

    /** The y coordinate of the last location where a component was dragged. */
    private int previousDragYLocation = 0;

    /** The button used to display the running/stopped image. */
    private JButton runningIndicator;

    /** The set of currently running hosts. */
    //private Set hosts = new HashSet();

    /** A message dialog shown while JMeter threads are stopping. */
    private JDialog stoppingMessage;

    /**
     * @deprecated only for use by test code
     */
    @Deprecated
    public ReportMainFrame(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }
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
            ReportTreeListener treeListener) {
        runningIndicator = new JButton(stoppedIcon);
        runningIndicator.setMargin(new Insets(0, 0, 0, 0));
        runningIndicator.setBorder(BorderFactory.createEmptyBorder());

        this.tree = this.makeTree(treeModel,treeListener);

        ReportGuiPackage.getInstance().setMainFrame(this);
        init();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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
        menuBar.setFileLoadEnabled(enabled);
    }

    /**
     * Specify whether or not the File|Save menu item should be enabled.
     *
     * @param enabled
     *            true if the menu item should be enabled, false otherwise
     */
    public void setFileSaveEnabled(boolean enabled) {
        menuBar.setFileSaveEnabled(enabled);
    }

    /**
     * Set the menu that should be used for the Edit menu.
     *
     * @param menu
     *            the new Edit menu
     */
    public void setEditMenu(JPopupMenu menu) {
        menuBar.setEditMenu(menu);
    }

    /**
     * Specify whether or not the Edit menu item should be enabled.
     *
     * @param enabled
     *            true if the menu item should be enabled, false otherwise
     */
    public void setEditEnabled(boolean enabled) {
        menuBar.setEditEnabled(enabled);
    }

    /**
     * Set the menu that should be used for the Edit|Add menu.
     *
     * @param menu
     *            the new Edit|Add menu
     */
    public void setEditAddMenu(JMenu menu) {
        menuBar.setEditAddMenu(menu);
    }

    /**
     * Specify whether or not the Edit|Add menu item should be enabled.
     *
     * @param enabled
     *            true if the menu item should be enabled, false otherwise
     */
    public void setEditAddEnabled(boolean enabled) {
        menuBar.setEditAddEnabled(enabled);
    }

    /**
     * Specify whether or not the Edit|Remove menu item should be enabled.
     *
     * @param enabled
     *            true if the menu item should be enabled, false otherwise
     */
    public void setEditRemoveEnabled(boolean enabled) {
        menuBar.setEditRemoveEnabled(enabled);
    }

    /**
     * Close the currently selected menu.
     */
    public void closeMenu() {
        if (menuBar.isSelected()) {
            MenuElement[] menuElement = menuBar.getSubElements();
            if (menuElement != null) {
                for (int i = 0; i < menuElement.length; i++) {
                    JMenu menu = (JMenu) menuElement[i];
                    if (menu.isSelected()) {
                        menu.setPopupMenuVisible(false);
                        menu.setSelected(false);
                        break;
                    }
                }
            }
        }
    }
    /**
     * Show a dialog indicating that JMeter threads are stopping on a particular
     * host.
     *
     * @param host
     *            the host where JMeter threads are stopping
     */
    public void showStoppingMessage(String host) {
        stoppingMessage = new JDialog(this, JMeterUtils.getResString("stopping_test_title"), true);// $NON-NLS-1$
        JLabel stopLabel = new JLabel(JMeterUtils.getResString("stopping_test") + ": " + host);// $NON-NLS-1$
        stopLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        stoppingMessage.getContentPane().add(stopLabel);
        stoppingMessage.pack();
        ComponentUtil.centerComponentInComponent(this, stoppingMessage);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (stoppingMessage != null) {
                    stoppingMessage.setVisible(true);
                }
            }
        });
    }

    public void setMainPanel(JComponent comp) {
        mainPanel.setViewportView(comp);
    }

    public JTree getTree() {
        return this.tree;
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
    private void init() {// called from ctor, so must not be overridable
        menuBar = new ReportMenuBar();
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

        setTitle(DEFAULT_TITLE);
        setIconImage(JMeterUtils.getImage("jmeter.jpg").getImage());// $NON-NLS-1$
    }

    public void setExtendedFrameTitle(String fname) {
        // file New operation may set to null, so just return app name
        if (fname == null) {
            setTitle(DEFAULT_TITLE);
            return;
        }

        // allow for windows / chars in filename
        String temp = fname.replace('\\', '/'); // $NON-NLS-1$ // $NON-NLS-2$
        String simpleName = temp.substring(temp.lastIndexOf("/") + 1);// $NON-NLS-1$
        setTitle(simpleName + " (" + fname + ") - " + DEFAULT_TITLE); // $NON-NLS-1$ // $NON-NLS-2$
    }

    /**
     * Create the JMeter tool bar pane containing the running indicator.
     *
     * @return a panel containing the running indicator
     */
    protected Component createToolBar() {
        Box toolPanel = new Box(BoxLayout.X_AXIS);
        toolPanel.add(Box.createRigidArea(new Dimension(10, 15)));
        toolPanel.add(Box.createGlue());
        toolPanel.add(runningIndicator);
        return toolPanel;
    }

    /**
     * Create the panel where the GUI representation of the test tree is
     * displayed. The tree should already be created before calling this method.
     *
     * @return a scroll pane containing the test tree GUI
     */
    protected JScrollPane createTreePanel() {
        JScrollPane treeP = new JScrollPane(tree);
        treeP.setMinimumSize(new Dimension(100, 0));
        return treeP;
    }

    /**
     * Create the main panel where components can display their GUIs.
     *
     * @return the main scroll pane
     */
    protected JScrollPane createMainPanel() {
        return new JScrollPane();
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
    private JTree makeTree(TreeModel treeModel, ReportTreeListener treeListener) {
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

    /**
     * Create the tree cell renderer used to draw the nodes in the test tree.
     *
     * @return a renderer to draw the test tree nodes
     */
    protected TreeCellRenderer getCellRenderer() {
        DefaultTreeCellRenderer rend = new ReportCellRenderer();
        rend.setFont(new Font("Dialog", Font.PLAIN, 11));
        return rend;
    }

    public void drawDraggedComponent(Component dragIcon, int x, int y) {
        Dimension size = dragIcon.getPreferredSize();
        treePanel.paintImmediately(previousDragXLocation, previousDragYLocation, size.width, size.height);
        this.getLayeredPane().setLayer(dragIcon, 400);
        SwingUtilities.paintComponent(treePanel.getGraphics(), dragIcon, treePanel, x, y, size.width, size.height);
        previousDragXLocation = x;
        previousDragYLocation = y;
    }

    /**
     * A window adapter used to detect when the main JMeter frame is being
     * closed.
     */
    protected static class WindowHappenings extends WindowAdapter {
        /**
         * Called when the main JMeter frame is being closed. Sends a
         * notification so that JMeter can react appropriately.
         *
         * @param event
         *            the WindowEvent to handle
         */
        @Override
        public void windowClosing(WindowEvent event) {
            ReportActionRouter.getInstance().actionPerformed(new ActionEvent(this, event.getID(), "exit"));
        }
    }
}
