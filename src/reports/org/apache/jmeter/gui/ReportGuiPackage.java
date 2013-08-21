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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPopupMenu;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.report.engine.ValueReplacer;
import org.apache.jmeter.report.gui.tree.ReportTreeListener;
import org.apache.jmeter.report.gui.tree.ReportTreeModel;
import org.apache.jmeter.report.gui.tree.ReportTreeNode;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.ReportPlan;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * ReportGuiPackage is based on GuiPackage, but with changes for
 * the reporting tool. Because of how the gui components work, it
 * was safer to just make a new class, rather than braking existing
 * JMeter gui code.
 *
 */
public final class ReportGuiPackage implements LocaleChangeListener {
    /** Logging. */
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Object LOCK = new Object();

    /** Singleton instance. */
    private static volatile ReportGuiPackage guiPack;

    /**
     * Flag indicating whether or not parts of the tree have changed since they
     * were last saved.
     */
    private boolean dirty = false;

    /**
     * Map from TestElement to JMeterGUIComponent, mapping the nodes in the tree
     * to their corresponding GUI components.
     */
    private Map<TestElement, JMeterGUIComponent> nodesToGui = new HashMap<TestElement, JMeterGUIComponent>();

    /**
     * Map from Class to JMeterGUIComponent, mapping the Class of a GUI
     * component to an instance of that component.
     */
    private Map<Class<?>, JMeterGUIComponent> guis = new HashMap<Class<?>, JMeterGUIComponent>();

    /**
     * Map from Class to TestBeanGUI, mapping the Class of a TestBean to an
     * instance of TestBeanGUI to be used to edit such components.
     */
    private Map<Class<?>, JMeterGUIComponent> testBeanGUIs = new HashMap<Class<?>, JMeterGUIComponent>();

    /** The currently selected node in the tree. */
    private ReportTreeNode currentNode = null;

    private boolean currentNodeUpdated = false;

    /** The model for JMeter's test tree. */
    private ReportTreeModel treeModel;

    /** The listener for JMeter's test tree. */
    private ReportTreeListener treeListener;

    /** The main JMeter frame. */
    private ReportMainFrame mainFrame;

    /**
     * Private constructor to permit instantiation only from within this class.
     * Use {@link #getInstance()} to retrieve a singleton instance.
     */
    private ReportGuiPackage() {
        JMeterUtils.addLocaleChangeListener(this);
    }

    /**
     * Retrieve the singleton GuiPackage instance.
     *
     * @return the GuiPackage instance
     */
    public static ReportGuiPackage getInstance() {
        if (guiPack == null){
            log.error("ReportGuiPackage is null");
        }
        return guiPack;
    }

    /**
     * When GuiPackage is requested for the first time, it should be given
     * handles to JMeter's Tree Listener and TreeModel.
     *
     * @param listener
     *            the TreeListener for JMeter's test tree
     * @param treeModel
     *            the model for JMeter's test tree
     *
     * @return GuiPackage
     */
    public static ReportGuiPackage getInstance(ReportTreeListener listener, ReportTreeModel treeModel) {
        if (guiPack == null) {
            synchronized (LOCK) {
                if(guiPack== null) {
                    guiPack = new ReportGuiPackage();
                    guiPack.setTreeListener(listener);
                    guiPack.setTreeModel(treeModel);
                }
            }
        }
        return guiPack;
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
            if (testClassName.equals("")) {
                testClass = node.getClass();
            } else {
                testClass = Class.forName(testClassName);
            }
            Class<?> guiClass = null;
            if (!guiClassName.equals("")) {
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
     *            the fully qualifed class name of the GUI component which will
     *            be created if it doesn't already exist
     * @param testClass
     *            the fully qualifed class name of the test elements which have
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
            log.debug("Gui retrieved = " + comp);
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
            comp.clearGui();
            log.debug("Updating gui to new node");
            comp.configure(curNode);
            currentNodeUpdated = false;
            return comp;
        } catch (Exception e) {
            log.error("Problem retrieving gui", e);
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
    public ReportTreeNode getNodeOf(TestElement userObject) {
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
            throw new RuntimeException(e.toString(), e); // Probably a missing
                                                        // jar
        } catch (ClassNotFoundException e) {
            log.error("Problem retrieving gui for " + objClass, e);
            throw new RuntimeException(e.toString(), e); // Programming error:
                                                        // bail out.
        } catch (InstantiationException e) {
            log.error("Problem retrieving gui for " + objClass, e);
            throw new RuntimeException(e.toString(), e); // Programming error:
                                                        // bail out.
        } catch (IllegalAccessException e) {
            log.error("Problem retrieving gui for " + objClass, e);
            throw new RuntimeException(e.toString(), e); // Programming error:
                                                        // bail out.
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
     * @throws ClassNotFoundException
     *             if the specified GUI class cannot be found
     */
    private JMeterGUIComponent getGuiFromCache(Class<?> guiClass, Class<?> testClass) throws InstantiationException,
            IllegalAccessException {
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
                comp = (JMeterGUIComponent) guiClass.newInstance();
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
     *
     */
    public void updateCurrentGui() {
        updateCurrentNode();
        currentNode = treeListener.getCurrentNode();
        TestElement element = currentNode.getTestElement();
        JMeterGUIComponent comp = getGui(element);
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
                log.debug("Updating current node " + currentNode.getName());
                JMeterGUIComponent comp = getGui(currentNode.getTestElement());
                TestElement el = currentNode.getTestElement();
                comp.modifyTestElement(el);
            }
            if (currentNode != treeListener.getCurrentNode()) {
                currentNodeUpdated = true;
            }
            currentNode = treeListener.getCurrentNode();
        } catch (Exception e) {
            log.error("Problem retrieving gui", e);
        }
    }

    public ReportTreeNode getCurrentNode() {
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
        return treeModel.addSubTree(subTree, treeListener.getCurrentNode());
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
    public ReportTreeModel getTreeModel() {
        return treeModel;
    }

    /**
     * Set the model for JMeter's test tree.
     *
     * @param newTreeModel
     *            the new JMeter tree model
     */
    public void setTreeModel(ReportTreeModel newTreeModel) {
        treeModel = newTreeModel;
    }

    /**
     * Get a ValueReplacer for the test tree.
     *
     * @return a ValueReplacer configured for the test tree
     */
    public ValueReplacer getReplacer() {
        return new ValueReplacer((ReportPlan) ((ReportTreeNode) getTreeModel().getReportPlan().getArray()[0])
                .getTestElement());
    }

    /**
     * Set the main JMeter frame.
     *
     * @param newMainFrame
     *            the new JMeter main frame
     */
    public void setMainFrame(ReportMainFrame newMainFrame) {
        this.mainFrame = newMainFrame;
    }

    /**
     * Get the main JMeter frame.
     *
     * @return the main JMeter frame
     */
    public ReportMainFrame getMainFrame() {
        return this.mainFrame;
    }

    /**
     * Set the listener for JMeter's test tree.
     *
     * @param newTreeListener
     *            the new JMeter test tree listener
     */
    public void setTreeListener(ReportTreeListener newTreeListener) {
        treeListener = newTreeListener;
    }

    /**
     * Get the listener for JMeter's test tree.
     *
     * @return the JMeter test tree listener
     */
    public ReportTreeListener getTreeListener() {
        return treeListener;
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
            log.debug("Showing pop up for " + invoker + " at x,y = " + e.getX() + "," + e.getY());

            popup.pack();
            popup.show(invoker, e.getX(), e.getY());
            popup.setVisible(true);
            popup.requestFocusInWindow();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jmeter.util.LocaleChangeListener#localeChanged(org.apache.jmeter.util.LocaleChangeEvent)
     */
    @Override
    public void localeChanged(LocaleChangeEvent event) {
        // FIrst make sure we save the content of the current GUI (since we
        // will flush it away):
        updateCurrentNode();

        // Forget about all GUIs we've created so far: we'll need to re-created
        // them all!
        guis = new HashMap<Class<?>, JMeterGUIComponent>();
        nodesToGui = new HashMap<TestElement, JMeterGUIComponent>();
        testBeanGUIs = new HashMap<Class<?>, JMeterGUIComponent>();

        // BeanInfo objects also contain locale-sensitive data -- flush them
        // away:
        Introspector.flushCaches();

        // Now put the current GUI in place. [This code was copied from the
        // EditCommand action -- we can't just trigger the action because that
        // would populate the current node with the contents of the new GUI --
        // which is empty.]
        ReportMainFrame mf = getMainFrame(); // Fetch once
        if (mf == null) // Probably caused by unit testing on headless system
        {
            log.warn("Mainframe is null");
        } else {
            mf.setMainPanel((javax.swing.JComponent) getCurrentGui());
            mf.setEditMenu(getTreeListener().getCurrentNode().createPopupMenu());
        }
    }

    private String reportPlanFile;

    /**
     * Sets the filepath of the current test plan. It's shown in the main frame
     * title and used on saving.
     *
     * @param f
     */
    public void setReportPlanFile(String f) {
        reportPlanFile = f;
        ReportGuiPackage.getInstance().getMainFrame().setExtendedFrameTitle(reportPlanFile);
        try {
            FileServer.getFileServer().setBasedir(reportPlanFile);
        } catch (IllegalStateException e1) {
            log.error("Failure setting file server's base dir", e1);
        }
    }

    public String getReportPlanFile() {
        return reportPlanFile;
    }
}