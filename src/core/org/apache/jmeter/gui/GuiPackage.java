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
package org.apache.jmeter.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPopupMenu;

import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
import org.apache.jmeter.util.LocaleChangeListener;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * GuiPackage is a static class that provides convenient access to information
 * about the current state of JMeter's GUI.  Any GUI class can grab a handle to
 * GuiPackage by calling the static method {@link #getInstance()} and then use
 * it to query the GUI about it's state.  When actions, for instance, need to
 * affect the GUI, they typically use GuiPackage to get access to different
 * parts of the GUI.
 * 
 * @author Michael Stover
 * @version $Revision$
 */
public final class GuiPackage implements LocaleChangeListener
{
    /** Logging. */
    private static transient Logger log =
        LoggingManager.getLoggerForClass();

    /** Singleton instance. */
    private static GuiPackage guiPack;
    
    /**
     * Flag indicating whether or not parts of the tree have changed since
     * they were last saved.
     */
    private boolean dirty = false;
    
    /** 
     * Map from TestElement to JMeterGUIComponent, mapping the nodes in the
     * tree to their corresponding GUI components.
     */
    private Map nodesToGui = new HashMap();
    
    /**
     * Map from Class to JMeterGUIComponent, mapping the Class of a GUI
     * component to an instance of that component.
     */
    private Map guis = new HashMap();
    
    /**
     * Map from Class to TestBeanGUI, mapping the Class of a TestBean to an
     * instance of TestBeanGUI to be used to edit such components.
     */
    private Map testBeanGUIs= new HashMap();

    /** The currently selected node in the tree. */
    private JMeterTreeNode currentNode = null;
    
    /** The model for JMeter's test tree. */
    private JMeterTreeModel treeModel;
    
    /** The listener for JMeter's test tree. */
    private JMeterTreeListener treeListener;

    /** The main JMeter frame. */
    private MainFrame mainFrame;

    
    /**
     * Private constructor to permit instantiation only from within this class.
     * Use {@link #getInstance()} to retrieve a singleton instance.
     */
    private GuiPackage()
    {
    	JMeterUtils.addLocaleChangeListener(this);
    }
    
    /**
     * Retrieve the singleton GuiPackage instance.
     * 
     * @return the GuiPackage instance
     */
    public static GuiPackage getInstance()
    {
        return guiPack;
    }
    
    /**
     * When GuiPackage is requested for the first time, it should be given
     * handles to JMeter's Tree Listener and TreeModel.
     *   
     * @param listener the TreeListener for JMeter's test tree
     * @param treeModel the model for JMeter's test tree
     * 
     * @return GuiPackage
     */
    public static GuiPackage getInstance(
        JMeterTreeListener listener,
        JMeterTreeModel treeModel)
    {
        if (guiPack == null)
        {
            guiPack = new GuiPackage();
            guiPack.setTreeListener(listener);
            guiPack.setTreeModel(treeModel);
        }
        return guiPack;
    }

    /**
     * Get a JMeterGUIComponent for the specified test element.  If the GUI has
     * already been created, that instance will be returned.  Otherwise, if a
     * GUI component of the same type has been created, and the component is
     * not marked as an {@link UnsharedComponent}, that shared component will
     * be returned.  Otherwise, a new instance of the component will be created.
     * The TestElement's GUI_CLASS property will be used to determine the
     * appropriate type of GUI component to use.
     * 
     * @param node     the test element which this GUI is being created for
     * 
     * @return         the GUI component corresponding to the specified test
     *                 element
     */
    public JMeterGUIComponent getGui(TestElement node)
    {
        try
        {
            return getGui(
                node, 
                Class.forName(node.getPropertyAsString(TestElement.GUI_CLASS)),
                Class.forName(node.getPropertyAsString(TestElement.TEST_CLASS)));
        }
        catch (ClassNotFoundException e)
        {
            log.error("Could not get GUI for "
                +node.getPropertyAsString(TestElement.GUI_CLASS), e);
            return null;
        }
    }

    /**
     * Get a JMeterGUIComponent for the specified test element.  If the GUI has
     * already been created, that instance will be returned.  Otherwise, if a
     * GUI component of the same type has been created, and the component is
     * not marked as an {@link UnsharedComponent}, that shared component will
     * be returned.  Otherwise, a new instance of the component will be created.
     * 
     * @param node     the test element which this GUI is being created for
     * @param guiClass the fully qualifed class name of the GUI component which
     *                 will be created if it doesn't already exist
     * @param testClass the fully qualifed class name of the test elements which
     *                 have to be edited by the returned GUI component
     * 
     * @return         the GUI component corresponding to the specified test
     *                 element
     */
    public JMeterGUIComponent getGui(
        TestElement node, Class guiClass, Class testClass)
    {
        try
        {
            JMeterGUIComponent comp = (JMeterGUIComponent) nodesToGui.get(node);
            log.debug("Gui retrieved = " + comp);
            if (comp == null)
            {
                comp = getGuiFromCache(guiClass, testClass);
                nodesToGui.put(node, comp);
            }
            return comp;
        }
        catch (Exception e)
        {
            log.error("Problem retrieving gui", e);
            return null;
        }
    }

    /**
     * Remove a test element from the tree.  This removes the reference to any
     * associated GUI component.
     * 
     * @param node the test element being removed
     */
    public void removeNode(TestElement node)
    {
        nodesToGui.remove(node);
    }
    
    /**
     * Convenience method for grabbing the gui for the current node.
     * 
     * @return the GUI component associated with the currently selected node
     */
    public JMeterGUIComponent getCurrentGui()
    {
        try
        {
            TestElement currentNode =
                treeListener.getCurrentNode().createTestElement();
            JMeterGUIComponent comp = getGui(currentNode);
            if(!(comp instanceof AbstractVisualizer))  // TODO: a hack that needs to be fixed for 2.0
            {
                comp.clear();
            }
            comp.configure(currentNode);
            return comp;
        }
        catch (Exception e)
        {
            log.error("Problem retrieving gui", e);
            return null;
        }
    }
    
    /**
     * Find the JMeterTreeNode for a certain TestElement object.
     * @param userObject the test element to search for
     * @return           the tree node associated with the test element
     */
    public JMeterTreeNode getNodeOf(TestElement userObject)
    {
        return treeModel.getNodeOf(userObject);
    }
    
    /**
     * Create a TestElement corresponding to the specified GUI class. 
     * 
     * @param guiClass the fully qualified class name of the GUI component
     *                  or a TestBean class for TestBeanGUIs.  
     * @param testClass the fully qualified class name of the test elements
     *                 edited by this GUI component.
     * @return the test element corresponding to the specified GUI class.
     */
    public TestElement createTestElement(Class guiClass, Class testClass)
    {
        try
        {
            JMeterGUIComponent comp = getGuiFromCache(guiClass, testClass);
            comp.clear();
            TestElement node = comp.createTestElement();
            nodesToGui.put(node, comp);
            return node;
        }
        catch (Exception e)
        {
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
     * @param objClass the fully qualified class name of the GUI component or of
     *                  the TestBean subclass for which a TestBeanGUI is wanted.  
     * @return the test element corresponding to the specified GUI class.
     */
    public TestElement createTestElement(String objClass)
    {
        JMeterGUIComponent comp;
        Class c;
        try
        {
            c= Class.forName(objClass);
            if (TestBean.class.isAssignableFrom(c))
            {
                comp= getGuiFromCache(TestBeanGUI.class, c);
            }
            else
            {
                comp= getGuiFromCache(c, null);
            }
            comp.clear();
            TestElement node = comp.createTestElement();
            nodesToGui.put(node, comp);
            return node;
        }
        catch (ClassNotFoundException e)
        {
            log.error("Problem retrieving gui for "+objClass, e);
            throw new Error(e.toString()); // Programming error: bail out.
        } catch (InstantiationException e)
        {
            log.error("Problem retrieving gui for "+objClass, e);
            throw new Error(e.toString()); // Programming error: bail out.
        } catch (IllegalAccessException e)
        {
            log.error("Problem retrieving gui for "+objClass, e);
            throw new Error(e.toString()); // Programming error: bail out.
        }
    }
    /**
     * Get an instance of the specified JMeterGUIComponent class.  If an
     * instance of the GUI class has previously been created and it is not
     * marked as an {@link UnsharedComponent}, that shared instance will be
     * returned.  Otherwise, a new instance of the component will be created,
     * and shared components will be cached for future retrieval.
     * 
     * @param guiClass the fully qualified class name of the GUI component.
     *                 This class must implement JMeterGUIComponent.
     * @param testClass the fully qualified class name of the test elements
     *                 edited by this GUI component. This class must 
     *                 implement TestElement.
     * @return         an instance of the specified class
     * 
     * @throws InstantiationException if an instance of the object cannot be
     *                                created
     * @throws IllegalAccessException if access rights do not allow the default
     *                                constructor to be called
     * @throws ClassNotFoundException if the specified GUI class cannot be
     *                                found
     */
    private JMeterGUIComponent getGuiFromCache(Class guiClass, Class testClass)
        throws InstantiationException,
               IllegalAccessException,
               ClassNotFoundException
    {
        JMeterGUIComponent comp ;
		if (guiClass == TestBeanGUI.class)
		{
			comp= (TestBeanGUI) testBeanGUIs.get(testClass); 
			if (comp == null)
			{
				comp= new TestBeanGUI(testClass);
				testBeanGUIs.put(testClass, comp);
			} 
		}
		else
		{
			comp= (JMeterGUIComponent) guis.get(guiClass);
			if (comp == null) 
			{
				comp = (JMeterGUIComponent) guiClass.newInstance();
				if (!(comp instanceof UnsharedComponent))
				{
					guis.put(guiClass, comp);
				}
			} 
		}
        return comp;
    }

    /**
     * Update the GUI for the currently selected node.  The GUI component is
     * configured to reflect the settings in the current tree node.
     *
     */
    public void updateCurrentGui()
    {
        currentNode= treeListener.getCurrentNode();
		TestElement element = currentNode.createTestElement();
		JMeterGUIComponent comp = getGui(element);
		comp.configure(element);
    }

    /**
     * This method should be called in order for GuiPackage to change the
     * current node.  This will save any changes made to the earlier node
     * before choosing the new node. 
     */
    public void updateCurrentNode()
    {
        try
        {
            if (currentNode != null)
            {
                log.debug(
                    "Updating current node " + currentNode.getName());
                JMeterGUIComponent comp =
                    getGui(currentNode.createTestElement());
                TestElement el = currentNode.createTestElement();
                comp.modifyTestElement(el);
            }
            currentNode = treeListener.getCurrentNode();
        }
        catch (Exception e)
        {
            log.error("Problem retrieving gui", e);
        }
    }

    /**
     * The dirty property is a flag that indicates whether there are parts of
     * JMeter's test tree that the user has not saved since last modification.
     * Various (@link Command actions) set this property when components are
     * modified/created/saved.
     * 
     * @param dirty the new value of the dirty flag
     */
    public void setDirty(boolean dirty)
    {
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
    public boolean isDirty()
    {
        return dirty;
    }
    
    /**
     * Add a subtree to the currently selected node.
     * 
     * @param subTree the subtree to add.
     * 
     * @return the resulting subtree starting with the currently selected node
     * 
     * @throws IllegalUserActionException if a subtree cannot be added to the
     *         currently selected node
     */
    public HashTree addSubTree(HashTree subTree)
        throws IllegalUserActionException
    {
        return treeModel.addSubTree(subTree, treeListener.getCurrentNode());
    }
    
    /**
     * Get the currently selected subtree.
     * 
     * @return the subtree of the currently selected node
     */
    public HashTree getCurrentSubTree()
    {
        return treeModel.getCurrentSubTree(treeListener.getCurrentNode());
    }
    
    /**
     * Get the model for JMeter's test tree.
     * 
     * @return the JMeter tree model
     */
    public JMeterTreeModel getTreeModel()
    {
        return treeModel;
    }

    /**
     * Set the model for JMeter's test tree.
     * 
     * @param newTreeModel the new JMeter tree model
     */
    public void setTreeModel(JMeterTreeModel newTreeModel)
    {
        treeModel = newTreeModel;
    }
    
    /**
     * Get a ValueReplacer for the test tree.
     * 
     * @return a ValueReplacer configured for the test tree
     */
    public ValueReplacer getReplacer()
    {
        return new ValueReplacer(
            (TestPlan) ((JMeterGUIComponent) getTreeModel()
                .getTestPlan()
                .getArray()[0])
                .createTestElement());
    }

    /**
     * Set the main JMeter frame.
     * 
     * @param newMainFrame the new JMeter main frame
     */
    public void setMainFrame(MainFrame newMainFrame)
    {
        mainFrame = newMainFrame;
    }
    
    /**
     * Get the main JMeter frame.
     * 
     * @return the main JMeter frame
     */
    public MainFrame getMainFrame()
    {
        return mainFrame;
    }
    
    /**
     * Set the listener for JMeter's test tree.
     * 
     * @param newTreeListener the new JMeter test tree listener
     */
    public void setTreeListener(JMeterTreeListener newTreeListener)
    {
        treeListener = newTreeListener;
    }
    
    /**
     * Get the listener for JMeter's test tree.
     * 
     * @return the JMeter test tree listener
     */
    public JMeterTreeListener getTreeListener()
    {
        return treeListener;
    }
    
    /**
     * Display the specified popup menu with the source component and location
     * from the specified mouse event.
     * 
     * @param e     the mouse event causing this popup to be displayed
     * @param popup the popup menu to display
     */
    public void displayPopUp(MouseEvent e, JPopupMenu popup)
    {
        displayPopUp((Component) e.getSource(), e, popup);
    }

    /**
     * Display the specified popup menu at the location specified by a mouse
     * event with the specified source component.
     * 
     * @param invoker the source component
     * @param e       the mouse event causing this popup to be displayed
     * @param popup   the popup menu to display
     */
    public void displayPopUp(Component invoker, MouseEvent e, JPopupMenu popup)
    {
        if (popup != null)
        {
            log.debug(
                "Showing pop up for " + invoker
                + " at x,y = " + e.getX() + "," + e.getY());
                
            popup.pack();
            popup.show(invoker, e.getX(), e.getY());
            popup.setVisible(true);
            popup.requestFocus();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.util.LocaleChangeListener#localeChanged(org.apache.jmeter.util.LocaleChangeEvent)
     */
    public void localeChanged(LocaleChangeEvent event)
    {
        // Forget about all GUIs we've created so far: we'll need to re-created them all!
        guis= new HashMap();
        nodesToGui= new HashMap();
        testBeanGUIs= new HashMap();
		ActionRouter.getInstance().actionPerformed(new ActionEvent(this, 3333, "edit"));
    }
}