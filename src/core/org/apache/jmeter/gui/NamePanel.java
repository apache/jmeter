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
import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.LocaleChangeEvent;
/****************************************
 * Title: JMeter
 *
 *@author    Michael Stover
 *@version   $Revision$  Last updated: $Date$
 ***************************************/

public class NamePanel extends JPanel implements JMeterGUIComponent
{
    /** A text field containing the name. */
    private JTextField nameField = new JTextField(15);
    
    /** The label for the text field. */
    private JLabel nameLabel;
    
    /** The node which this component is providing the name for. */
    private JMeterTreeNode node;

    /**
     * Create a new NamePanel with the default name.
     */
    public NamePanel()
    {
        setName(getStaticLabel());
        init();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init()
    {
        setLayout(new BorderLayout(5, 0));

        nameLabel = new JLabel(JMeterUtils.getResString("name"));
        nameLabel.setName("name");
        nameLabel.setLabelFor(nameField);

        nameField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                updateName(nameField.getText());
            }

            public void removeUpdate(DocumentEvent e)
            {
                updateName(nameField.getText());
            }

            public void changedUpdate(DocumentEvent e)
            {
                // not for text fields
            }
        });

        add(nameLabel, BorderLayout.WEST);
        add(nameField, BorderLayout.CENTER);
    }
    
    public void clear()
    {
        setName(getStaticLabel());        
    }


    /**
     * Get the currently displayed name.
     * 
     * @return the current name
     */
    public String getName()
    {
        return nameField.getText();
    }

    /**
     * Set the name displayed in this component.
     * 
     * @param name the name to display
     */
    public void setName(String name)
    {
        super.setName(name);
        nameField.setText(name);
    }

    /**
     * Get the tree node which this component provides the name for.
     * 
     * @return the tree node corresponding to this component
     */
    protected JMeterTreeNode getNode()
    {
        return node;
    }

    /**
     * Set the tree node which this component provides the name for.
     * 
     * @param node the tree node corresponding to this component
     */
    public void setNode(JMeterTreeNode node)
    {
        this.node = node;
    }

    /* Implements JMeterGUIComponent.configure(TestElement) */
    public void configure(TestElement testElement)
    {
        setName(testElement.getPropertyAsString(TestElement.NAME));
    }

    /* Implements JMeterGUIComponent.createPopupMenu() */
    public JPopupMenu createPopupMenu()
    {
        return null;
    }

    /* Implements JMeterGUIComponent.getStaticLabel() */
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("root");
    }

    /* Implements JMeterGUIComponent.getMenuCategories() */
    public Collection getMenuCategories()
    {
        return null;
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    public TestElement createTestElement()
    {
        WorkBench wb = new WorkBench();
        modifyTestElement(wb);
        return wb;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    public void modifyTestElement(TestElement wb)
    {
        wb.setProperty(new StringProperty(TestElement.NAME, getName()));
        wb.setProperty(
            new StringProperty(
                TestElement.GUI_CLASS,
                this.getClass().getName()));
        wb.setProperty(
            new StringProperty(
                TestElement.TEST_CLASS,
                WorkBench.class.getName()));
    }

    /**
     * Called when the name changes.  The tree node which this component names
     * will be notified of the change.
     * 
     * @param newValue the new name
     */
    private void updateName(String newValue)
    {
        if (getNode() != null)
        {
            getNode().nameChanged();
        }
    }

    /**
     * Called when the locale is changed so that the label can be updated.
     * This method is not currently used.
     * 
     * @param event the event to be handled
     */
    public void localeChanged(LocaleChangeEvent event)
    {
        nameLabel.setText(JMeterUtils.getResString(nameLabel.getName()));
    }
}
