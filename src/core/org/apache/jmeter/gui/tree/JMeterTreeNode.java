// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package org.apache.jmeter.gui.tree;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.jmeter.gui.GUIFactory;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author     Michael Stover
 * @version    $Revision$
 */
public class JMeterTreeNode
    extends DefaultMutableTreeNode
    implements JMeterGUIComponent
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    private JMeterTreeModel treeModel;
    //boolean enabled = true;

    public JMeterTreeNode(){// Allow guiTest and serializable test to work
    	this(null,null);
    }
    
    public JMeterTreeNode(TestElement userObj, JMeterTreeModel treeModel)
    {
        super(userObj);
        this.treeModel = treeModel;
    }

    public boolean isEnabled()
    {
        return (
            (AbstractTestElement) createTestElement()).getPropertyAsBoolean(
            TestElement.ENABLED);
    }

    public void setEnabled(boolean enabled)
    {
        createTestElement().setProperty(
            new BooleanProperty(TestElement.ENABLED, enabled));
    }

    public void clear()
    {
    }

    public ImageIcon getIcon()
    {
        try
        {
            if (createTestElement() instanceof TestBean)
            {
                try
                {
                    Image img= Introspector.getBeanInfo(
                        createTestElement().getClass())
                            .getIcon(BeanInfo.ICON_COLOR_16x16);
                    if (img == null) return null;
                    return new ImageIcon(img);
                }
                catch (IntrospectionException e1)
                {
                    log.error("Can't obtain icon", e1);
                    throw new org.apache.jorphan.util.JMeterError(e1);
                }
            }
            else
            {
                return GUIFactory.getIcon(
                    Class.forName(
                        createTestElement().getPropertyAsString(
                            TestElement.GUI_CLASS)));
            }
        }
        catch (ClassNotFoundException e)
        {
            log.warn("Can't get icon for class " + createTestElement(), e);
            return null;
        }
    }

    public Collection getMenuCategories()
    {
        try
        {
            return GuiPackage
                .getInstance()
                .getGui(createTestElement())
                .getMenuCategories();
        }
        catch (Exception e)
        {
            log.error("Can't get popup menu for gui", e);
            return null;
        }
    }

    public JPopupMenu createPopupMenu()
    {
        try
        {
            return GuiPackage
                .getInstance()
                .getGui(createTestElement())
                .createPopupMenu();
        }
        catch (Exception e)
        {
            log.error("Can't get popup menu for gui", e);
            return null;
        }
    }

    public void configure(TestElement element)
    {

    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     */
    public void modifyTestElement(TestElement el)
    {

    }

    public TestElement createTestElement()
    {
        return (TestElement) getUserObject();
    }

    public String getStaticLabel()
    {
        return GuiPackage
            .getInstance()
            .getGui((TestElement) getUserObject())
            .getStaticLabel();
    }

    public void setName(String name)
    {
        ((TestElement) getUserObject()).setProperty(
            new StringProperty(TestElement.NAME, name));
    }

    public String getName()
    {
        return ((TestElement) getUserObject()).getPropertyAsString(
            TestElement.NAME);
    }

    public void setNode(JMeterTreeNode node)
    {

    }

    public void nameChanged()
    {
        treeModel.nodeChanged(this);
    }
}
