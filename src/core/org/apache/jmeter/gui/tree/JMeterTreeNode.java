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
package org.apache.jmeter.gui.tree;

import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.jmeter.gui.GUIFactory;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
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

    JMeterTreeNode(){// Allow guiTest and serializable test to work
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
            return GUIFactory.getIcon(
                Class.forName(
                    createTestElement().getPropertyAsString(
                        TestElement.GUI_CLASS)));
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
