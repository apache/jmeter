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
import java.awt.event.MouseEvent;
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
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;

/**
 * The main JMeter frame, containing the menu bar, test tree, and an area for
 * JMeter component GUIs.
 *
 */
public class MainFrame extends JFrame implements TestListener, Remoteable {

    private static final long serialVersionUID = 240L;

    // This is used to keep track of local (non-remote) tests
    // The name is chosen to be an unlikely host-name
    private static final String LOCAL = "*local*"; // $NON-NLS-1$

    // The default title for the Menu bar
    private static final String DEFAULT_TITLE =
        "Apache JMeter ("+JMeterUtils.getJMeterVersion()+")"; // $NON-NLS-1$ $NON-NLS-2$
    
    // Allow display/hide toolbar
    private static final boolean DISPLAY_TOOLBAR =
            JMeterUtils.getPropDefault("jmeter.toolbar.display", true); // $NON-NLS-1$

    /** The menu bar. */
    private JMeterMenuBar menuBar;

    /** The main panel where components display their GUIs. */
    private JScrollPane mainPanel;

    /** The panel where the test tree is shown. */
    private JScrollPane treePanel;

    /** The test tree. */
    private JTree tree;

    /** An image which is displayed when a test is running. */
    private ImageIcon runningIcon = JMeterUtils.getImage("thread.enabled.gif");// $NON-NLS-1$

    /** An image which is displayed when a test is not currently running. */
    private ImageIcon stoppedIcon = JMeterUtils.getImage("thread.disabled.gif");// $NON-NLS-1$

    /** The button used to display the running/stopped image. */
    private JButton runningIndicator;

    /** The x coordinate of the last location where a component was dragged. */
    private int previousDragXLocation = 0;

    /** The y coordinate of the last location where a component was dragged. */
    private int previousDragYLocation = 0;

    /** The set of currently running hosts. */
    private Set<String> hosts = new HashSet<String>();

    /** A message dialog shown while JMeter threads are stopping. */
    private JDialog stoppingMessage;

    private JLabel totalThreads;
    private JLabel activeThreads;

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
    public MainFrame(ActionListener actionHandler, TreeModel treeModel, JMeterTreeListener treeListener) {
        // TODO: actionHandler isn't used -- remove it from the parameter list
        // this.actionHandler = actionHandler;

        // TODO: Make the running indicator its own class instead of a JButton
        runningIndicator = new JButton(stoppedIcon);
        runningIndicator.setMargin(new Insets(0, 0, 0, 0));
        runningIndicator.setBorder(BorderFactory.createEmptyBorder());

        totalThreads = new JLabel("0"); // $NON-NLS-1$
        activeThreads = new JLabel("0"); // $NON-NLS-1$

        tree = makeTree(treeModel, treeListener);

        GuiPackage.getInstance().setMainFrame(this);
        init();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Default constructor for the JMeter frame. This constructor will not
     * properly initialize the tree, so don't use it.
     *
     * @deprecated Do not use - only needed for JUnit tests
     */
    @Deprecated
    public MainFrame() {
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
     * Specify whether or not the File|Revert item should be enabled.
     *
     * @param enabled
     *            true if the menu item should be enabled, false otherwise
     */
    public void setFileRevertEnabled(boolean enabled) {
        menuBar.setFileRevertEnabled(enabled);
    }

    /**
     * Specify the project file that was just loaded
     *
     * @param file - the full path to the file that was loaded
     */
    public void setProjectFileLoaded(String file) {
        menuBar.setProjectFileLoaded(file);
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
        if (stoppingMessage != null){
            stoppingMessage.dispose();
        }
        stoppingMessage = new JDialog(this, JMeterUtils.getResString("stopping_test_title"), true); //$NON-NLS-1$
        JLabel stopLabel = new JLabel(JMeterUtils.getResString("stopping_test") + ": " + host); //$NON-NLS-1$$NON-NLS-2$
        stopLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        stoppingMessage.getContentPane().add(stopLabel);
        stoppingMessage.pack();
        ComponentUtil.centerComponentInComponent(this, stoppingMessage);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (stoppingMessage != null) {// TODO - how can this be null?
                    stoppingMessage.setVisible(true);
                }
            }
        });
    }

    public void updateCounts() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                activeThreads.setText(Integer.toString(JMeterContextService.getNumberOfThreads()));
                totalThreads.setText(Integer.toString(JMeterContextService.getTotalThreads()));
            }
        });
    }

    public void setMainPanel(JComponent comp) {
        mainPanel.setViewportView(comp);
    }

    public JTree getTree() {
        return tree;
    }

    // TestListener implementation

    /**
     * Called when a test is started on the local system. This implementation
     * sets the running indicator and ensures that the menubar is enabled and in
     * the running state.
     */
    public void testStarted() {
        testStarted(LOCAL);
        menuBar.setEnabled(true);
    }

    /**
     * Called when a test is started on a specific host. This implementation
     * sets the running indicator and ensures that the menubar is in the running
     * state.
     *
     * @param host
     *            the host where the test is starting
     */
    public void testStarted(String host) {
        hosts.add(host);
        runningIndicator.setIcon(runningIcon);
        activeThreads.setText("0"); // $NON-NLS-1$
        totalThreads.setText("0"); // $NON-NLS-1$
        menuBar.setRunning(true, host);
    }

    /**
     * Called when a test is ended on the local system. This implementation
     * disables the menubar, stops the running indicator, and closes the
     * stopping message dialog.
     */
    public void testEnded() {
        testEnded(LOCAL);
        menuBar.setEnabled(false);
    }

    /**
     * Called when a test is ended on the remote system. This implementation
     * stops the running indicator and closes the stopping message dialog.
     *
     * @param host
     *            the host where the test is ending
     */
    public void testEnded(String host) {
        hosts.remove(host);
        if (hosts.size() == 0) {
            runningIndicator.setIcon(stoppedIcon);
            JMeterContextService.endTest();
        }
        menuBar.setRunning(false, host);
        if (stoppingMessage != null) {
            stoppingMessage.dispose();
            stoppingMessage = null;
        }
    }

    /* Implements TestListener#testIterationStart(LoopIterationEvent) */
    public void testIterationStart(LoopIterationEvent event) {
    }

    /**
     * Create the GUI components and layout.
     */
    private void init() {
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
    private Component createToolBar() {
        Box toolPanel = new Box(BoxLayout.X_AXIS);
        // add the toolbar
        JToolBar toolbar = JMeterToolBar.createToolbar(DISPLAY_TOOLBAR);
        GuiPackage guiInstance = GuiPackage.getInstance();
        guiInstance.setMainToolbar(toolbar);
        guiInstance.getMenuItemToolbar().getModel().setSelected(DISPLAY_TOOLBAR);
        toolPanel.add(toolbar);

        toolPanel.add(Box.createRigidArea(new Dimension(10, 15)));
        toolPanel.add(Box.createGlue());
        toolPanel.add(activeThreads);
        toolPanel.add(new JLabel(" / "));
        toolPanel.add(totalThreads);
        toolPanel.add(Box.createRigidArea(new Dimension(10, 15)));
        toolPanel.add(runningIndicator);
        return toolPanel;
    }

    /**
     * Create the panel where the GUI representation of the test tree is
     * displayed. The tree should already be created before calling this method.
     *
     * @return a scroll pane containing the test tree GUI
     */
    private JScrollPane createTreePanel() {
        JScrollPane treeP = new JScrollPane(tree);
        treeP.setMinimumSize(new Dimension(100, 0));
        return treeP;
    }

    /**
     * Create the main panel where components can display their GUIs.
     *
     * @return the main scroll pane
     */
    private JScrollPane createMainPanel() {
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
    private JTree makeTree(TreeModel treeModel, JMeterTreeListener treeListener) {
        JTree treevar = new JTree(treeModel) {
            private static final long serialVersionUID = 240L;

            @Override
            public String getToolTipText(MouseEvent event) {
                TreePath path = this.getPathForLocation(event.getX(), event.getY());
                if (path != null) {
                    Object treeNode = path.getLastPathComponent();
                    if (treeNode instanceof DefaultMutableTreeNode) {
                        Object testElement = ((DefaultMutableTreeNode) treeNode).getUserObject();
                        if (testElement instanceof TestElement) {
                            String comment = ((TestElement) testElement).getComment();
                            if (comment != null && comment.length() > 0) {
                                return comment;
                                }
                            }
                        }
                    }
                return null;
                }
            };
           treevar.setToolTipText("");
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
    private TreeCellRenderer getCellRenderer() {
        DefaultTreeCellRenderer rend = new JMeterCellRenderer();
        rend.setFont(new Font("Dialog", Font.PLAIN, 11));
        return rend;
    }

    /**
     * Repaint pieces of the GUI as needed while dragging. This method should
     * only be called from the Swing event thread.
     *
     * @param dragIcon
     *            the component being dragged
     * @param x
     *            the current mouse x coordinate
     * @param y
     *            the current mouse y coordinate
     */
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
    private static class WindowHappenings extends WindowAdapter {
        /**
         * Called when the main JMeter frame is being closed. Sends a
         * notification so that JMeter can react appropriately.
         *
         * @param event
         *            the WindowEvent to handle
         */
        @Override
        public void windowClosing(WindowEvent event) {
            ActionRouter.getInstance().actionPerformed(new ActionEvent(this, event.getID(), ActionNames.EXIT));
        }
    }
}
