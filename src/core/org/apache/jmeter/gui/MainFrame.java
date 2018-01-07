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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
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
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.action.LoadDraggedFile;
import org.apache.jmeter.gui.logging.GuiLogEventListener;
import org.apache.jmeter.gui.logging.LogEventObject;
import org.apache.jmeter.gui.tree.JMeterCellRenderer;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.tree.JMeterTreeTransferHandler;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main JMeter frame, containing the menu bar, test tree, and an area for
 * JMeter component GUIs.
 */
public class MainFrame extends JFrame implements TestStateListener, Remoteable, DropTargetListener, Clearable, ActionListener {

    private static final long serialVersionUID = 241L;

    // This is used to keep track of local (non-remote) tests
    // The name is chosen to be an unlikely host-name
    public static final String LOCAL = "*local*"; // $NON-NLS-1$

    // The application name
    private static final String DEFAULT_APP_NAME = "Apache JMeter"; // $NON-NLS-1$

    // The default title for the Menu bar
    private static final String DEFAULT_TITLE = DEFAULT_APP_NAME +
            " (" + JMeterUtils.getJMeterVersion() + ")"; // $NON-NLS-1$ $NON-NLS-2$

    // Allow display/hide LoggerPanel
    private static final boolean DISPLAY_LOGGER_PANEL =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.display", false); // $NON-NLS-1$

    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

    /** The menu bar. */
    private JMeterMenuBar menuBar;

    /** The main panel where components display their GUIs. */
    private JScrollPane mainPanel;

    /** The panel where the test tree is shown. */
    private JScrollPane treePanel;

    /** The LOG panel. */
    private LoggerPanel logPanel;

    /** The test tree. */
    private JTree tree;

    private final String iconSize = JMeterUtils.getPropDefault(JMeterToolBar.TOOLBAR_ICON_SIZE, JMeterToolBar.DEFAULT_TOOLBAR_ICON_SIZE); 

    /** An image which is displayed when a test is running. */
    private final ImageIcon runningIcon = JMeterUtils.getImage("status/" + iconSize +"/user-online-2.png");// $NON-NLS-1$

    /** An image which is displayed when a test is not currently running. */
    private final ImageIcon stoppedIcon = JMeterUtils.getImage("status/" + iconSize +"/user-offline-2.png");// $NON-NLS-1$
    
    /** An image which is displayed to indicate FATAL, ERROR or WARNING. */
    private final ImageIcon warningIcon = JMeterUtils.getImage("status/" + iconSize +"/pictogram-din-w000-general.png");// $NON-NLS-1$

    /** The button used to display the running/stopped image. */
    private JButton runningIndicator;

    /** The set of currently running hosts. */
    private final Set<String> hosts = new HashSet<>();

    /** A message dialog shown while JMeter threads are stopping. */
    private JDialog stoppingMessage;

    private JLabel activeAndTotalThreads;

    private JMeterToolBar toolbar;

    /** Label at top right showing test duration */
    private JLabel testTimeDuration;

    /** Indicator for Log errors and Fatals */
    private JButton warnIndicator;

    /** LogTarget that receives ERROR or FATAL */
    private transient ErrorsAndFatalsCounterLogTarget errorsAndFatalsCounterLogTarget;
    
    private javax.swing.Timer computeTestDurationTimer = new javax.swing.Timer(1000, 
            this::computeTestDuration);
    
    public AtomicInteger errorOrFatal = new AtomicInteger(0);

    private javax.swing.Timer refreshErrorsTimer = new javax.swing.Timer(1000, 
            this::refreshErrors);

    /**
     * Create a new JMeter frame.
     *
     * @param treeModel
     *            the model for the test tree
     * @param treeListener
     *            the listener for the test tree
     */
    public MainFrame(TreeModel treeModel, JMeterTreeListener treeListener) {
        runningIndicator = new JButton(stoppedIcon);
        runningIndicator.setFocusable(false);
        runningIndicator.setBorderPainted(false);
        runningIndicator.setContentAreaFilled(false);
        runningIndicator.setMargin(new Insets(0, 0, 0, 0));

        testTimeDuration = new JLabel("00:00:00"); //$NON-NLS-1$
        testTimeDuration.setToolTipText(JMeterUtils.getResString("duration_tooltip")); //$NON-NLS-1$
        testTimeDuration.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        activeAndTotalThreads = new JLabel("0/0"); // $NON-NLS-1$
        activeAndTotalThreads.setToolTipText(JMeterUtils.getResString("active_total_threads_tooltip")); // $NON-NLS-1$

        warnIndicator = new JButton(warningIcon);
        warnIndicator.setMargin(new Insets(0, 0, 0, 0));
        // Transparent JButton with no border
        warnIndicator.setOpaque(false);
        warnIndicator.setContentAreaFilled(false);
        warnIndicator.setBorderPainted(false);
        warnIndicator.setCursor(new Cursor(Cursor.HAND_CURSOR));
        warnIndicator.setToolTipText(JMeterUtils.getResString("error_indicator_tooltip")); // $NON-NLS-1$
        warnIndicator.addActionListener(this);

        tree = makeTree(treeModel, treeListener);

        GuiPackage.getInstance().setMainFrame(this);
        init();
        initTopLevelDndHandler();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                final float scale = 1.1f;
                int rotation = e.getWheelRotation();
                if (rotation > 0) { // DOWN
                    JMeterUtils.applyScaleOnFonts(1.0f / scale);
                } else if (rotation < 0) { // UP
                    JMeterUtils.applyScaleOnFonts(scale);
                }
                e.consume();
            }
        });
    }

    /**
     * Refresh errors label
     * @param evt {@link ActionEvent}
     */
    private void refreshErrors(ActionEvent evt) {
        if (errorOrFatal.get() > 0) {
            warnIndicator.setForeground(Color.RED);
            warnIndicator.setText(Integer.toString(errorOrFatal.get()));
        }
    }
    
    protected void computeTestDuration(ActionEvent evt) {
        long startTime = JMeterContextService.getTestStartTime();
        if (startTime > 0) {
            long elapsedSec = (System.currentTimeMillis() - startTime + 500) / 1000; // rounded seconds
            testTimeDuration.setText(JOrphanUtils.formatDuration(elapsedSec));
        }
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
     * Close the currently selected menu.
     */
    public void closeMenu() {
        if (!menuBar.isSelected()) {
            return;
        }
        MenuElement[] menuElement = menuBar.getSubElements();
        if (menuElement != null) {
            for (MenuElement element : menuElement) {
                JMenu menu = (JMenu) element;
                if (menu.isSelected()) {
                    menu.setPopupMenuVisible(false);
                    menu.setSelected(false);
                    break;
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
        stoppingMessage = new EscapeDialog(this, JMeterUtils.getResString("stopping_test_title"), true); //$NON-NLS-1$
        String label = JMeterUtils.getResString("stopping_test"); //$NON-NLS-1
        if (!StringUtils.isEmpty(host)) {
            label = label + JMeterUtils.getResString("stopping_test_host")+ ": " + host;
        }
        JLabel stopLabel = new JLabel(label); //$NON-NLS-1$$NON-NLS-2$
        stopLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        stoppingMessage.getContentPane().add(stopLabel);
        stoppingMessage.pack();
        ComponentUtil.centerComponentInComponent(this, stoppingMessage);
        SwingUtilities.invokeLater(() -> {
                if (stoppingMessage != null) {
                    stoppingMessage.setVisible(true);
                }
        });
    }

    public void updateCounts() {
        SwingUtilities.invokeLater(() ->
                activeAndTotalThreads.setText(
                        String.format("%d/%d",
                                JMeterContextService.getNumberOfThreads(),
                                JMeterContextService.getTotalThreads())));
    }

    public void setMainPanel(JComponent comp) {
        mainPanel.setViewportView(comp);
    }

    public JTree getTree() {
        return tree;
    }

    // TestStateListener implementation

    /**
     * Called when a test is started on the local system. This implementation
     * sets the running indicator and ensures that the menubar is enabled and in
     * the running state.
     */
    @Override
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
    @Override
    public void testStarted(String host) {
        hosts.add(host);
        computeTestDurationTimer.start();
        runningIndicator.setIcon(runningIcon);
        activeAndTotalThreads.setText("0/0"); // $NON-NLS-1$
        menuBar.setRunning(true, host);
        if (LOCAL.equals(host)) {
            toolbar.setLocalTestStarted(true);
        } else {
            toolbar.setRemoteTestStarted(true);
        }
    }

    /**
     * Called when a test is ended on the local system. This implementation
     * disables the menubar, stops the running indicator, and closes the
     * stopping message dialog.
     */
    @Override
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
    @Override
    public void testEnded(String host) {
        hosts.remove(host);
        if (hosts.size() == 0) {
            runningIndicator.setIcon(stoppedIcon);
            JMeterContextService.endTest();
            computeTestDurationTimer.stop();
        }
        menuBar.setRunning(false, host);
        if (LOCAL.equals(host)) {
            toolbar.setLocalTestStarted(false);
        } else {
            toolbar.setRemoteTestStarted(false);
        }
        if (stoppingMessage != null) {
            stoppingMessage.dispose();
            stoppingMessage = null;
        }
    }

    /**
     * Create the GUI components and layout.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        menuBar = new JMeterMenuBar();
        setJMenuBar(menuBar);
        JPanel all = new JPanel(new BorderLayout());
        all.add(createToolBar(), BorderLayout.NORTH);

        JSplitPane treeAndMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        treePanel = createTreePanel();
        treeAndMain.setLeftComponent(treePanel);

        JSplitPane topAndDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        topAndDown.setOneTouchExpandable(true);
        topAndDown.setDividerLocation(0.8);
        topAndDown.setResizeWeight(.8);
        topAndDown.setContinuousLayout(true);
        topAndDown.setBorder(null); // see bug jdk 4131528
        if (!DISPLAY_LOGGER_PANEL) {
            topAndDown.setDividerSize(0);
        }
        mainPanel = createMainPanel();

        logPanel = createLoggerPanel();
        errorsAndFatalsCounterLogTarget = new ErrorsAndFatalsCounterLogTarget();
        GuiPackage.getInstance().getLogEventBus().registerEventListener(logPanel);
        GuiPackage.getInstance().getLogEventBus().registerEventListener(errorsAndFatalsCounterLogTarget);

        topAndDown.setTopComponent(mainPanel);
        topAndDown.setBottomComponent(logPanel);

        treeAndMain.setRightComponent(topAndDown);

        treeAndMain.setResizeWeight(.2);
        treeAndMain.setContinuousLayout(true);
        all.add(treeAndMain, BorderLayout.CENTER);

        getContentPane().add(all);

        tree.setSelectionRow(1);
        addWindowListener(new WindowHappenings());
        // Building is complete, register as listener
        GuiPackage.getInstance().registerAsListener();
        setTitle(DEFAULT_TITLE);
        setIconImage(JMeterUtils.getImage("icon-apache.png").getImage());// $NON-NLS-1$
        setWindowTitle(); // define AWT WM_CLASS string
        refreshErrorsTimer.start();
    }

    /**
     * Support for Test Plan Dnd
     * see BUG 52281 (when JDK6 will be minimum JDK target)
     */
    public void initTopLevelDndHandler() {
        new DropTarget(this, this);
    }

    public void setExtendedFrameTitle(String fname) {
        // file New operation may set to null, so just return app name
        if (fname == null) {
            setTitle(DEFAULT_TITLE);
            return;
        }

        // allow for windows / chars in filename
        String temp = fname.replace('\\', '/'); // $NON-NLS-1$ // $NON-NLS-2$
        String simpleName = temp.substring(temp.lastIndexOf('/') + 1);// $NON-NLS-1$
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
        this.toolbar = JMeterToolBar.createToolbar(true);
        GuiPackage guiInstance = GuiPackage.getInstance();
        guiInstance.setMainToolbar(toolbar);
        toolPanel.add(toolbar);

        toolPanel.add(Box.createRigidArea(new Dimension(5, 15)));
        toolPanel.add(Box.createGlue());

        toolPanel.add(testTimeDuration);
        toolPanel.add(Box.createRigidArea(new Dimension(5, 15)));

        toolPanel.add(warnIndicator);
        warnIndicator.setText("0");
        toolPanel.add(Box.createRigidArea(new Dimension(5, 15)));

        toolPanel.add(activeAndTotalThreads);
        toolPanel.add(Box.createRigidArea(new Dimension(5, 15)));
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
     * Create at the down of the left a Console for Log events
     * @return {@link LoggerPanel}
     */
    private LoggerPanel createLoggerPanel() {
        LoggerPanel loggerPanel = new LoggerPanel();
        loggerPanel.setMinimumSize(new Dimension(0, 100));
        loggerPanel.setPreferredSize(new Dimension(0, 150));
        GuiPackage guiInstance = GuiPackage.getInstance();
        guiInstance.setLoggerPanel(loggerPanel);
        guiInstance.getMenuItemLoggerPanel().getModel().setSelected(DISPLAY_LOGGER_PANEL);
        loggerPanel.setVisible(DISPLAY_LOGGER_PANEL);
        return loggerPanel;
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
        treevar.addKeyListener(treeListener);

        // enable drag&drop, install a custom transfer handler
        treevar.setDragEnabled(true);
        treevar.setDropMode(DropMode.ON_OR_INSERT);
        treevar.setTransferHandler(new JMeterTreeTransferHandler());

        addQuickComponentHotkeys(treevar);

        return treevar;
    }

    private void addQuickComponentHotkeys(JTree treevar) {
        Action quickComponent = new AbstractAction("Quick Component") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String propname = "gui.quick_" + actionEvent.getActionCommand();
                String comp = JMeterUtils.getProperty(propname);
                log.debug("Event {}: {}", propname, comp);

                if (comp == null) {
                    log.warn("No component set through property: {}", propname);
                    return;
                }

                GuiPackage guiPackage = GuiPackage.getInstance();
                try {
                    guiPackage.updateCurrentNode();
                    TestElement testElement = guiPackage.createTestElement(SaveService.aliasToClass(comp));
                    JMeterTreeNode parentNode = guiPackage.getCurrentNode();
                    while (!MenuFactory.canAddTo(parentNode, testElement)) {
                        parentNode = (JMeterTreeNode) parentNode.getParent();
                    }
                    if (parentNode.getParent() == null) {
                        log.debug("Cannot add element on very top level");
                    } else {
                        JMeterTreeNode node = guiPackage.getTreeModel().addComponent(testElement, parentNode);
                        guiPackage.getMainFrame().getTree().setSelectionPath(new TreePath(node.getPath()));
                    }
                } catch (Exception err) {
                    log.warn("Failed to perform quick component add: {}", comp, err); // $NON-NLS-1$
                }
            }
        };

        InputMap inputMap = treevar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke[] keyStrokes = new KeyStroke[]{KeyStrokes.CTRL_0,
                KeyStrokes.CTRL_1, KeyStrokes.CTRL_2, KeyStrokes.CTRL_3,
                KeyStrokes.CTRL_4, KeyStrokes.CTRL_5, KeyStrokes.CTRL_6,
                KeyStrokes.CTRL_7, KeyStrokes.CTRL_8, KeyStrokes.CTRL_9,};
        for (int n = 0; n < keyStrokes.length; n++) {
            treevar.getActionMap().put(ActionNames.QUICK_COMPONENT + String.valueOf(n), quickComponent);
            inputMap.put(keyStrokes[n], ActionNames.QUICK_COMPONENT + String.valueOf(n));
        }
    }

    /**
     * Create the tree cell renderer used to draw the nodes in the test tree.
     *
     * @return a renderer to draw the test tree nodes
     */
    private TreeCellRenderer getCellRenderer() {
        return new JMeterCellRenderer();
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

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        // NOOP
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // NOOP
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        // NOOP
    }

    /**
     * Handler of Top level Dnd
     */
    @Override
    public void drop(DropTargetDropEvent dtde) {
        Transferable tr = dtde.getTransferable();
        boolean anyFlavourIsJavaFileList =
                Arrays.stream(tr.getTransferDataFlavors())
                        .anyMatch(DataFlavor::isFlavorJavaFileListType);
        if (anyFlavourIsJavaFileList) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            try {
                openJmxFilesFromDragAndDrop(tr);
            } catch (UnsupportedFlavorException | IOException e) {
                log.warn("Dnd failed", e);
            } finally {
                dtde.dropComplete(true);
            }
        }
    }

    public boolean openJmxFilesFromDragAndDrop(Transferable tr) throws UnsupportedFlavorException, IOException {
        @SuppressWarnings("unchecked")
        List<File> files = (List<File>)
                tr.getTransferData(DataFlavor.javaFileListFlavor);
        if (files.isEmpty()) {
            return false;
        }
        File file = files.get(0);
        if (!file.getName().endsWith(".jmx")) {
            if (log.isWarnEnabled()) {
                log.warn("Importing file, {}, from DnD failed because file extension does not end with .jmx", file.getName());
            }
            return false;
        }

        ActionEvent fakeEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ActionNames.OPEN);
        LoadDraggedFile.loadProject(fakeEvent, file);

        return true;
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        // NOOP
    }

    /**
     * ErrorsAndFatalsCounterLogTarget.
     */
    public final class ErrorsAndFatalsCounterLogTarget implements GuiLogEventListener, Clearable {

        @Override
        public void processLogEvent(LogEventObject logEventObject) {
            if (logEventObject.isMoreSpecificThanError()) {
                errorOrFatal.incrementAndGet();
            }
        }

        @Override
        public void clearData() {
            errorOrFatal.set(0);
            SwingUtilities.invokeLater(() -> {
                warnIndicator.setForeground(null);
                warnIndicator.setText(Integer.toString(errorOrFatal.get()));
            });
        }
    }

    @Override
    public void clearData() {
        logPanel.clear();
        errorsAndFatalsCounterLogTarget.clearData();
    }

    /**
     * Handles click on warnIndicator
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == warnIndicator) {
            ActionRouter.getInstance().doActionNow(
                    new ActionEvent(event.getSource(), event.getID(), ActionNames.LOGGER_PANEL_ENABLE_DISABLE));
        }
    }

    /**
     * Define AWT window title (WM_CLASS string) (useful on Gnome 3 / Linux)
     */
    private void setWindowTitle() {
        Class<?> xtoolkit = Toolkit.getDefaultToolkit().getClass();
        if (xtoolkit.getName().equals("sun.awt.X11.XToolkit")) { // NOSONAR (we don't want to depend on native LAF) $NON-NLS-1$
            try {
                final Field awtAppClassName = xtoolkit.getDeclaredField("awtAppClassName"); // $NON-NLS-1$
                awtAppClassName.setAccessible(true);
                awtAppClassName.set(null, DEFAULT_APP_NAME);
            } catch (NoSuchFieldException | IllegalAccessException nsfe) {
                if (log.isWarnEnabled()) {
                    log.warn("Error awt title: {}", nsfe.toString()); // $NON-NLS-1$
                }
            }
        }
    }

    /**
     * Update Undo/Redo icons state
     *
     * @param canUndo Flag whether the undo button should be enabled
     * @param canRedo Flag whether the redo button should be enabled
     */
    public void updateUndoRedoIcons(boolean canUndo, boolean canRedo) {
        toolbar.updateUndoRedoIcons(canUndo, canRedo);
    }
}
