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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.UndoHistory.HistoryListener;
import org.apache.jmeter.gui.action.TreeNodeNamingPolicy;
import org.apache.jmeter.gui.action.impl.DefaultTreeNodeNamingPolicy;
import org.apache.jmeter.gui.logging.GuiLogEventBus;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GuiPackage is a static class that provides convenient access to information
 * about the current state of JMeter's GUI. Any GUI class can grab a handle to
 * GuiPackage by calling the static method {@link #getInstance()} and then use
 * it to query the GUI about it's state. When actions, for instance, need to
 * affect the GUI, they typically use GuiPackage to get access to different
 * parts of the GUI.
 *
 */
public final class GuiPackage implements LocaleChangeListener, HistoryListener {

    private static final Logger log = LoggerFactory.getLogger(GuiPackage.class);

    private static final String SBR_PREFS_KEY = "save_before_run";

    private static final String SAVE_BEFORE_RUN_PROPERTY = "save_automatically_before_run"; // $NON-NLS-1$
    
    private static final boolean SAVE_BEFORE_RUN_PROPERTY_DEFAULT_VALUE = true;

    private static final Preferences PREFS = Preferences.userNodeForPackage(GuiPackage.class);

    /** Singleton instance. */
    private static GuiPackage guiPack;

    /**
     * Flag indicating whether or not parts of the tree have changed since they
     * were last saved.
     */
    private boolean dirty = false;

    /**
     * Map from TestElement to JMeterGUIComponent, mapping the nodes in the tree
     * to their corresponding GUI components.
     */
    private Map<TestElement, JMeterGUIComponent> nodesToGui = new HashMap<>();

    /**
     * Map from Class to JMeterGUIComponent, mapping the Class of a GUI
     * component to an instance of that component.
     */
    private Map<Class<?>, JMeterGUIComponent> guis = new HashMap<>();

    /**
     * Map from Class to TestBeanGUI, mapping the Class of a TestBean to an
     * instance of TestBeanGUI to be used to edit such components.
     */
    private Map<Class<?>, JMeterGUIComponent> testBeanGUIs = new HashMap<>();

    /** The currently selected node in the tree. */
    private JMeterTreeNode currentNode = null;

    private boolean currentNodeUpdated = false;

    /** The model for JMeter's test tree. */
    private final JMeterTreeModel treeModel;

    /** The listener for JMeter's test tree. */
    private final JMeterTreeListener treeListener;

    /** The main JMeter frame. */
    private MainFrame mainFrame;

    /** The main JMeter toolbar. */
    private JToolBar toolbar;
    
    /** The LoggerPanel menu item */
    private JCheckBoxMenuItem menuItemLoggerPanel;

    /** The LoggerPanel menu item */
    private JCheckBoxMenuItem menuItemSaveBeforeRunPanel;
    
    /** Logger Panel reference */
    private LoggerPanel loggerPanel;

    /** History for tree states */
    private UndoHistory undoHistory = new UndoHistory();

    /** GUI Logging Event Bus. */
    private GuiLogEventBus logEventBus = new GuiLogEventBus();

    /** Listeners for events on test plan */
    private List<TestPlanListener> testPlanListeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * Private constructor to permit instantiation only from within this class.
     * Use {@link #getInstance()} to retrieve a singleton instance.
     */
    private GuiPackage(JMeterTreeModel treeModel, JMeterTreeListener treeListener) {
        this.treeModel = treeModel;
        if (UndoHistory.isEnabled()) {
            this.treeModel.addTreeModelListener(undoHistory);
        }
        this.treeListener = treeListener;
    }

    /**
     * Retrieve the singleton GuiPackage instance.
     *
     * @return the GuiPackage instance (may be null, e.g in non-Gui mode)
     */
    public static GuiPackage getInstance() {
        return guiPack;
    }
    
    /**
     * Register as listener of:
     * - UndoHistory
     * - Locale Changes
     */
    public void registerAsListener() {
        if(UndoHistory.isEnabled()) {
            this.undoHistory.registerHistoryListener(this);
        }
        JMeterUtils.addLocaleChangeListener(this);
    }

    /**
     * When GuiPackage is requested for the first time, it should be given
     * handles to JMeter's Tree Listener and TreeModel.
     *
     * @param listener
     *            the TreeListener for JMeter's test tree
     * @param treeModel
     *            the model for JMeter's test tree
     */
    public static void initInstance(JMeterTreeListener listener, JMeterTreeModel treeModel) {
        GuiPackage guiPack = new GuiPackage(treeModel, listener);
        guiPack.undoHistory.add(treeModel, "Created");
        GuiPackage.guiPack = guiPack;
    }
   

    /**
     * Get a JMeterGUIComponent for the specified test element. If the GUI has
     * already been created, that instance will be returned. Otherwise, if a GUI
     * component of the same type has been created, and the component is not
     * marked as an {@link UnsharedComponent}, that shared component will be
     * returned. Otherwise, a new instance of the component will be created. The
     * TestElement's GUI_CLASS property will be used to determine the
     * appropriate type of GUI component to use.
     *
     * @param node
     *            the test element which this GUI is being created for
     *
     * @return the GUI component corresponding to the specified test element
     */
    public JMeterGUIComponent getGui(TestElement node) {
        String testClassName = node.getPropertyAsString(TestElement.TEST_CLASS);
        String guiClassName = node.getPropertyAsString(TestElement.GUI_CLASS);
        try {
            Class<?> testClass;
            if (testClassName.isEmpty()) {
                testClass = node.getClass();
            } else {
                testClass = Class.forName(testClassName);
            }
            Class<?> guiClass = null;
            if (!guiClassName.isEmpty()) {
                guiClass = Class.forName(guiClassName);
            }
            return getGui(node, guiClass, testClass);
        } catch (ClassNotFoundException e) {
            log.error("Could not get GUI for " + node, e);
            return null;
        }
    }

    /**
     * Get a JMeterGUIComponent for the specified test element. If the GUI has
     * already been created, that instance will be returned. Otherwise, if a GUI
     * component of the same type has been created, and the component is not
     * marked as an {@link UnsharedComponent}, that shared component will be
     * returned. Otherwise, a new instance of the component will be created.
     *
     * @param node
     *            the test element which this GUI is being created for
     * @param guiClass
     *            the fully qualified class name of the GUI component which will
     *            be created if it doesn't already exist
     * @param testClass
     *            the fully qualified class name of the test elements which have
     *            to be edited by the returned GUI component
     *
     * @return the GUI component corresponding to the specified test element
     */
    public JMeterGUIComponent getGui(TestElement node, Class<?> guiClass, Class<?> testClass) {
        try {
            JMeterGUIComponent comp = nodesToGui.get(node);
            if (comp == null) {
                comp = getGuiFromCache(guiClass, testClass);
                nodesToGui.put(node, comp);
            }
            log.debug("Gui retrieved = {}", comp);
            return comp;
        } catch (Exception e) {
            log.error("Problem retrieving gui", e);
            return null;
        }
    }

    /**
     * Remove a test element from the tree. This removes the reference to any
     * associated GUI component.
     *
     * @param node
     *            the test element being removed
     */
    public void removeNode(TestElement node) {
        nodesToGui.remove(node);
    }

    /**
     * Convenience method for grabbing the gui for the current node.
     *
     * @return the GUI component associated with the currently selected node
     */
    public JMeterGUIComponent getCurrentGui() {
        try {
            updateCurrentNode();
            TestElement curNode = treeListener.getCurrentNode().getTestElement();
            JMeterGUIComponent comp = getGui(curNode);
            if (comp == null) {
                log.debug("No Component found for {}.", treeListener.getCurrentNode().getName());
                return null;
            }
            comp.clearGui();
            log.debug("Updating gui to new node");
            comp.configure(curNode);
            currentNodeUpdated = false;
            return comp;
        } catch (Exception e) {
            log.error("Problem retrieving current gui", e);
            return null;
        }
    }

    /**
     * Find the JMeterTreeNode for a certain TestElement object.
     *
     * @param userObject
     *            the test element to search for
     * @return the tree node associated with the test element
     */
    public JMeterTreeNode getNodeOf(TestElement userObject) {
        return treeModel.getNodeOf(userObject);
    }

    /**
     * Create a TestElement corresponding to the specified GUI class.
     *
     * @param guiClass
     *            the fully qualified class name of the GUI component or a
     *            TestBean class for TestBeanGUIs.
     * @param testClass
     *            the fully qualified class name of the test elements edited by
     *            this GUI component.
     * @return the test element corresponding to the specified GUI class.
     */
    public TestElement createTestElement(Class<?> guiClass, Class<?> testClass) {
        try {
            JMeterGUIComponent comp = getGuiFromCache(guiClass, testClass);
            comp.clearGui();
            TestElement node = comp.createTestElement();
            nodesToGui.put(node, comp);
            return node;
        } catch (Exception e) {
            log.error("Problem retrieving gui", e);
            return null;
        }
    }

    /**
     * Create a TestElement for a GUI or TestBean class.
     * <p>
     * This is a utility method to help actions do with one single String
     * parameter.
     *
     * @param objClass
     *            the fully qualified class name of the GUI component or of the
     *            TestBean subclass for which a TestBeanGUI is wanted.
     * @return the test element corresponding to the specified GUI class.
     */
    public TestElement createTestElement(String objClass) {
        JMeterGUIComponent comp;
        Class<?> c;
        try {
            c = Class.forName(objClass);
            if (TestBean.class.isAssignableFrom(c)) {
                comp = getGuiFromCache(TestBeanGUI.class, c);
            } else {
                comp = getGuiFromCache(c, null);
            }
            comp.clearGui();
            TestElement node = comp.createTestElement();
            nodesToGui.put(node, comp);
            return node;
        } catch (NoClassDefFoundError e) {
            log.error("Problem retrieving gui for " + objClass, e);
            String msg="Cannot find class: "+e.getMessage();
            JOptionPane.showMessageDialog(null,
                    msg,
                    "Missing jar? See log file." ,
                    JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(e.toString(), e); // Probably a missing jar
        } catch ( ReflectiveOperationException e) {
            log.error("Problem retrieving gui for " + objClass, e);
            throw new RuntimeException(e.toString(), e); // Programming error: bail out.
        }
    }

    /**
     * Get an instance of the specified JMeterGUIComponent class. If an instance
     * of the GUI class has previously been created and it is not marked as an
     * {@link UnsharedComponent}, that shared instance will be returned.
     * Otherwise, a new instance of the component will be created, and shared
     * components will be cached for future retrieval.
     *
     * @param guiClass
     *            the fully qualified class name of the GUI component. This
     *            class must implement JMeterGUIComponent.
     * @param testClass
     *            the fully qualified class name of the test elements edited by
     *            this GUI component. This class must implement TestElement.
     * @return an instance of the specified class
     *
     * @throws InstantiationException
     *             if an instance of the object cannot be created
     * @throws IllegalAccessException
     *             if access rights do not allow the default constructor to be
     *             called
     * @throws ReflectiveOperationException when construction of guiClass fails
     */
    private JMeterGUIComponent getGuiFromCache(Class<?> guiClass, Class<?> testClass) throws ReflectiveOperationException {
        JMeterGUIComponent comp;
        if (guiClass == TestBeanGUI.class) {
            comp = testBeanGUIs.get(testClass);
            if (comp == null) {
                comp = new TestBeanGUI(testClass);
                testBeanGUIs.put(testClass, comp);
            }
        } else {
            comp = guis.get(guiClass);
            if (comp == null) {
                comp = (JMeterGUIComponent) guiClass.getDeclaredConstructor().newInstance();
                if (!(comp instanceof UnsharedComponent)) {
                    guis.put(guiClass, comp);
                }
            }
        }
        return comp;
    }

    /**
     * Update the GUI for the currently selected node. The GUI component is
     * configured to reflect the settings in the current tree node.
     */
    public void updateCurrentGui() {
        updateCurrentNode();
        refreshCurrentGui();
    }

    /**
     * Refresh GUI from node state. 
     * This method does not update the current node from GUI at the 
     * difference of {@link GuiPackage#updateCurrentGui()}
     */
    public void refreshCurrentGui() {
        currentNode = treeListener.getCurrentNode();
        TestElement element = currentNode.getTestElement();
        JMeterGUIComponent comp = getGui(element);
        if (comp == null) {
            log.debug("No component found for {}", currentNode.getName());
            return;
        }
        comp.configure(element);
        currentNodeUpdated = false;
    }

    /**
     * This method should be called in order for GuiPackage to change the
     * current node. This will save any changes made to the earlier node before
     * choosing the new node.
     */
    public void updateCurrentNode() {
        try {
            if (currentNode != null && !currentNodeUpdated) {
                log.debug("Updating current node {}", currentNode.getName());
                JMeterGUIComponent comp = getGui(currentNode.getTestElement());
                if (comp == null) {
                    log.debug("No component found for {}", currentNode.getName());
                    return;
                }
                TestElement el = currentNode.getTestElement();
                int before = 0;
                int after = 0;
                final boolean historyEnabled = UndoHistory.isEnabled();
                if (historyEnabled) {
                    before = getTestElementCheckSum(el);
                }
                comp.modifyTestElement(el);
                if (historyEnabled) {
                    after = getTestElementCheckSum(el);
                }
                if (!historyEnabled || (before != after)) {
                    currentNode.nameChanged(); // Bug 50221 - ensure label is updated
                }
            }
            // The current node is now updated
            currentNodeUpdated = true;
            currentNode = treeListener.getCurrentNode();
        } catch (Exception e) {
            log.error("Problem retrieving gui", e);
        }
    }

    public JMeterTreeNode getCurrentNode() {
        return treeListener.getCurrentNode();
    }

    public TestElement getCurrentElement() {
        return getCurrentNode().getTestElement();
    }

    /**
     * The dirty property is a flag that indicates whether there are parts of
     * JMeter's test tree that the user has not saved since last modification.
     * Various (@link Command actions) set this property when components are
     * modified/created/saved.
     *
     * @param dirty
     *            the new value of the dirty flag
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Retrieves the state of the 'dirty' property, a flag that indicates if
     * there are test tree components that have been modified since they were
     * last saved.
     *
     * @return true if some tree components have been modified since they were
     *         last saved, false otherwise
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Add a subtree to the currently selected node.
     *
     * @param subTree
     *            the subtree to add.
     *
     * @return the resulting subtree starting with the currently selected node
     *
     * @throws IllegalUserActionException
     *             if a subtree cannot be added to the currently selected node
     */
    public HashTree addSubTree(HashTree subTree) throws IllegalUserActionException {
        HashTree hashTree = treeModel.addSubTree(subTree, treeListener.getCurrentNode());
        undoHistory.clear();
        undoHistory.add(this.treeModel, "Loaded tree");
        return hashTree;
    }

    /**
     * Get the currently selected subtree.
     *
     * @return the subtree of the currently selected node
     */
    public HashTree getCurrentSubTree() {
        return treeModel.getCurrentSubTree(treeListener.getCurrentNode());
    }

    /**
     * Get the model for JMeter's test tree.
     *
     * @return the JMeter tree model
     */
    /*
     * TODO consider removing this method, and providing method wrappers instead.
     * This would allow the Gui package to do any additional clearups if required,
     * as has been done with clearTestPlan()
    */
    public JMeterTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * Get a ValueReplacer for the test tree.
     *
     * @return a ValueReplacer configured for the test tree
     */
    public ValueReplacer getReplacer() {
        return new ValueReplacer((TestPlan) ((JMeterTreeNode) getTreeModel().getTestPlan().getArray()[0])
                .getTestElement());
    }

    /**
     * Set the main JMeter frame.
     *
     * @param newMainFrame
     *            the new JMeter main frame
     */
    public void setMainFrame(MainFrame newMainFrame) {
        mainFrame = newMainFrame;
    }

    /**
     * Get the main JMeter frame.
     *
     * @return the main JMeter frame
     */
    public MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Get the listener for JMeter's test tree.
     *
     * @return the JMeter test tree listener
     */
    public JMeterTreeListener getTreeListener() {
        return treeListener;
    }

    /**
     * Set the main JMeter toolbar.
     *
     * @param newToolbar
     *            the new JMeter main toolbar
     */
    public void setMainToolbar(JToolBar newToolbar) {
        toolbar = newToolbar;
    }

    /**
     * Get the main JMeter toolbar.
     *
     * @return the main JMeter toolbar
     */
    public JToolBar getMainToolbar() {
        return toolbar;
    }


    /**
     * Display the specified popup menu with the source component and location
     * from the specified mouse event.
     *
     * @param e
     *            the mouse event causing this popup to be displayed
     * @param popup
     *            the popup menu to display
     */
    public void displayPopUp(MouseEvent e, JPopupMenu popup) {
        displayPopUp((Component) e.getSource(), e, popup);
    }

    /**
     * Display the specified popup menu at the location specified by a mouse
     * event with the specified source component.
     *
     * @param invoker
     *            the source component
     * @param e
     *            the mouse event causing this popup to be displayed
     * @param popup
     *            the popup menu to display
     */
    public void displayPopUp(Component invoker, MouseEvent e, JPopupMenu popup) {
        if (popup != null) {
            log.debug("Showing pop up for {} at x,y = {},{}", invoker, e.getX(), e.getY());

            popup.pack();
            popup.show(invoker, e.getX(), e.getY());
            popup.setVisible(true);
            popup.requestFocusInWindow();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void localeChanged(LocaleChangeEvent event) {
        // First make sure we save the content of the current GUI (since we
        // will flush it away):
        updateCurrentNode();

        // Forget about all GUIs we've created so far: we'll need to re-created
        // them all!
        guis = new HashMap<>();
        nodesToGui = new HashMap<>();
        testBeanGUIs = new HashMap<>();

        // BeanInfo objects also contain locale-sensitive data -- flush them
        // away:
        Introspector.flushCaches();

        // Now put the current GUI in place. [This code was copied from the
        // EditCommand action -- we can't just trigger the action because that
        // would populate the current node with the contents of the new GUI --
        // which is empty.]
        MainFrame mf = getMainFrame(); // Fetch once
        if (mf == null) { // Probably caused by unit testing on headless system
            log.warn("Mainframe is null");
        } else {
            mf.setMainPanel((javax.swing.JComponent) getCurrentGui());
            mf.setEditMenu(getTreeListener().getCurrentNode().createPopupMenu());
        }
    }

    private String testPlanFile;

    private final List<Stoppable> stoppables = Collections.synchronizedList(new ArrayList<Stoppable>());

    private TreeNodeNamingPolicy namingPolicy;

    /**
     * Sets the filepath of the current test plan. It's shown in the main frame
     * title and used on saving.
     *
     * @param f
     *            The filepath of the current test plan
     */
    public void setTestPlanFile(String f) {
        testPlanFile = f;
        getMainFrame().setExtendedFrameTitle(testPlanFile);
        // Enable file revert action if a file is used
        getMainFrame().setFileRevertEnabled(f != null);
        getMainFrame().setProjectFileLoaded(f);

        try {
            FileServer.getFileServer().setBasedir(testPlanFile);
        } catch (IllegalStateException e1) {
            log.error("Failure setting file server's base dir", e1);
        }
        testPlanListeners.stream().forEach(TestPlanListener::testPlanLoaded);
    }

    public String getTestPlanFile() {
        return testPlanFile;
    }

    /**
     * Clears the test plan and associated objects.
     * Clears the test plan file name.
     */
    public void clearTestPlan() {
        testPlanListeners.stream().forEach(TestPlanListener::beforeTestPlanCleared);
        getTreeModel().clearTestPlan();
        nodesToGui.clear();
        setTestPlanFile(null);
        testPlanListeners.stream().forEach(TestPlanListener::afterTestPlanCleared);
        undoHistory.clear();
        undoHistory.add(this.treeModel, "Initial Tree");
    }

    /**
     * Clears the test plan element and associated object
     *
     * @param element to clear
     */
    public void clearTestPlan(TestElement element) {
        getTreeModel().clearTestPlan(element);
        removeNode(element);
        undoHistory.clear();
        undoHistory.add(this.treeModel, "Initial Tree");
    }

    public static void showErrorMessage(final String message, final String title){
        showMessage(message,title,JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfoMessage(final String message, final String title){
        showMessage(message,title,JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarningMessage(final String message, final String title){
        showMessage(message,title,JOptionPane.WARNING_MESSAGE);
    }

    public static void showMessage(final String message, final String title, final int type){
        if (guiPack == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,message,title,type));

    }

    /**
     * Unregister stoppable
     *
     * @param stoppableToUnregister Stoppable to unregister
     */
    public void unregister(Stoppable stoppableToUnregister) {
        stoppables.removeIf(stoppable -> stoppable == stoppableToUnregister);
    }

    /**
     * Register process to stop on reload
     *
     * @param stoppable The {@link Stoppable} to be registered
     */
    public void register(Stoppable stoppable) {
        stoppables.add(stoppable);
    }

    /**
     * @return copy of list of {@link Stoppable}s
     */
    public List<Stoppable> getStoppables() {
        List<Stoppable> list = new ArrayList<>();
        list.addAll(stoppables);
        return list;
    }

    /**
     * Set the menu item LoggerPanel.
     *
     * @param menuItemLoggerPanel The menu item LoggerPanel
     */
    public void setMenuItemLoggerPanel(JCheckBoxMenuItem menuItemLoggerPanel) {
        this.menuItemLoggerPanel = menuItemLoggerPanel;
    }
        
    /**
     * Get the menu item LoggerPanel.
     *
     * @return the menu item LoggerPanel
     */
    public JCheckBoxMenuItem getMenuItemLoggerPanel() {
        return menuItemLoggerPanel;
    }
    
    /**
     * Set the menu item SaveBeforeRunPanel.
     * 
     * @param menuItemSaveBeforeRunPanel
     *            The menu item SaveBeforeRunPanel
     */
    public void setMenuItemSaveBeforeRunPanel(JCheckBoxMenuItem menuItemSaveBeforeRunPanel) {
        this.menuItemSaveBeforeRunPanel = menuItemSaveBeforeRunPanel;
    }

    /**
     * Get the menu item SaveBeforeRunPanel.
     *
     * @return the menu item SaveBeforeRunPanel
     */
    public JCheckBoxMenuItem getMenuItemSaveBeforeRunPanel() {
        return menuItemSaveBeforeRunPanel;
    }

    /**
     * @param loggerPanel LoggerPanel
     */
    public void setLoggerPanel(LoggerPanel loggerPanel) {
        this.loggerPanel = loggerPanel;
    }

    /**
     * @return the loggerPanel
     */
    public LoggerPanel getLoggerPanel() {
        return loggerPanel;
    }

    /**
     * Navigate back through undo history
     */
    public void undo() {
        undoHistory.undo();
    }

    /**
     * Navigate forward through undo history
     */
    public void redo() {
        undoHistory.redo();
    }

    /**
     * @return true if history contains redo item
     */
    public boolean canRedo() {
        return undoHistory.canRedo();
    }

    /**
     * @return true if history contains undo item
     */
    public boolean canUndo() {
        return undoHistory.canUndo();
    }

    /**
     * Compute checksum of TestElement to detect changes
     * the method calculates properties checksum to detect testelement
     * modifications
     * TODO would be better to override hashCode for TestElement, but I decided not to touch it
     *
     * @param el {@link TestElement}
     * @return int checksum
     */
    private int getTestElementCheckSum(TestElement el) {
        int ret = el.getClass().hashCode();
        PropertyIterator it = el.propertyIterator();
        while (it.hasNext()) {
            JMeterProperty obj = it.next();
            if (obj instanceof TestElementProperty) {
                ret ^= getTestElementCheckSum(
                        ((TestElementProperty) obj).getElement());
            } else {
                ret ^= obj.getName().hashCode();
                String stringValue = obj.getStringValue();
                if (stringValue != null) {
                    ret ^= stringValue.hashCode();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("obj.getStringValue() returned null for test element:"
                                + el.getName() + " at property:" + obj.getName());
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Called when history changes, it updates toolbar
     */
    @Override
    public void notifyChangeInHistory(UndoHistory history) {
        ((JMeterToolBar)toolbar).updateUndoRedoIcons(history.canUndo(), history.canRedo());
    }

    /**
     * @return {@link TreeNodeNamingPolicy}
     */
    public TreeNodeNamingPolicy getNamingPolicy() {
        if (namingPolicy == null) {
            final String namingPolicyImplementation =
                    JMeterUtils.getPropDefault("naming_policy.impl", //$NON-NLS-1$ 
                            DefaultTreeNodeNamingPolicy.class.getName());

            try {
                Class<?> implementationClass = Class.forName(namingPolicyImplementation);
                this.namingPolicy = (TreeNodeNamingPolicy) implementationClass.getDeclaredConstructor().newInstance();

            } catch (Exception ex) {
                log.error("Failed to create configured naming policy:" + namingPolicyImplementation + ", will use default one", ex);
                this.namingPolicy = new DefaultTreeNodeNamingPolicy();
            }
        }
        return namingPolicy;
    }

    /**
     * Return {@link GuiLogEventBus}.
     * @return {@link GuiLogEventBus}
     */
    public GuiLogEventBus getLogEventBus() {
        return logEventBus;
    }

    /**
     * Begin a group of actions modeled as 1 undo
     */
    public void beginUndoTransaction() {
        undoHistory.beginUndoTransaction();
    }

    /**
     * End a group of actions modeled as 1 undo
     */
    public void endUndoTransaction() {
        undoHistory.endUndoTransaction();
    }
    
    
    /**
     * Should Save Before Run by Preference Only
     * @return boolean
     */
    public boolean shouldSaveBeforeRunByPreference() {
        return Boolean.TRUE.toString().
                equalsIgnoreCase(PREFS.get(SBR_PREFS_KEY, null));
    }
    
    /**
     * Should Save Before Run by Preference Only
     * @param saveBeforeRun boolean
     */
    public void setSaveBeforeRunByPreference(boolean saveBeforeRun) {
        PREFS.put(SBR_PREFS_KEY, Boolean.toString(saveBeforeRun)); // $NON-NLS-1$ // $NON-NLS-2$
    }
    
    /**
     * Should Save Before Run 
     * Decide by Preference and if not exists by Property
     * 
     * @return boolean Should Save Before Run
     */
    public boolean shouldSaveBeforeRun() {
        String sbr = PREFS.get(SBR_PREFS_KEY, null);
        if (sbr == null) {
            // use property if no preference
            return JMeterUtils.getPropDefault(SAVE_BEFORE_RUN_PROPERTY, 
                    SAVE_BEFORE_RUN_PROPERTY_DEFAULT_VALUE);         
        } else {
            return shouldSaveBeforeRunByPreference();
        }
    }

    /**
     * Adds a test plan listener.
     * @param listener to add
     */
    public void addTestPlanListener(TestPlanListener listener) {
        testPlanListeners.add(listener);
    }

    /**
     * Removes a test plan listener.
     * @param listener to remove
     */
    public void removeTestPlanListener(TestPlanListener listener) {
        testPlanListeners.remove(listener);
    }

}
