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
package org.apache.jmeter.protocol.http.proxy.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Title: Jakarta-JMeter Description: Copyright: Copyright (c) 2001 Company:
 * Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version $Revision$
 ***************************************/

public class ProxyControlGui extends AbstractJMeterGuiComponent implements JMeterGUIComponent, ActionListener, KeyListener, FocusListener, UnsharedComponent
{
    transient private static Logger log =
            Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.gui");
    private JTextField portField;

    private ProxyControl model;

    private JTable excludeTable;
    private PowerTableModel excludeModel;
    private JTable includeTable;
    private PowerTableModel includeModel;

    private JButton stop, start, restart;
    private final static String STOP = "stop";
    private final static String START = "start";
    private final static String RESTART = "restart";
    private final static String ADD_INCLUDE = "add_include";
    private final static String ADD_EXCLUDE = "add_exclude";
    private final static String DELETE_INCLUDE = "delete_include";
    private final static String DELETE_EXCLUDE = "delete_exclude";

    private final static String INCLUDE_COL = JMeterUtils.getResString("patterns_to_include");
    private final static String EXCLUDE_COL = JMeterUtils.getResString("patterns_to_exclude");

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public ProxyControlGui()
    {
        super();
        init();
    }

    public JPopupMenu createPopupMenu()
    {
        return MenuFactory.getDefaultTimerMenu();
    }

    public TestElement createTestElement()
    {
        if (model == null)
        {
            model = makeProxyControl();
        }
        log.debug("creating/configuring model = " + model);
        modifyTestElement(model);
        return model;
    }
    
    protected ProxyControl makeProxyControl()
        {
            ProxyControl local = new ProxyControl();
            return local;
        }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement el)
    {        
        configureTestElement(el);
        if(el instanceof ProxyControl)
        {            
            ((ProxyControl)el).setPort(portField.getText());
            setIncludeListInProxyControl((ProxyControl)el);
            setExcludeListInProxyControl((ProxyControl)el);
            model = (ProxyControl)el;
        }
    }

    protected void setIncludeListInProxyControl(ProxyControl element)
    {
        List includeList = getDataList(includeModel, INCLUDE_COL);
        element.setIncludeList(includeList);
    }

    protected void setExcludeListInProxyControl(ProxyControl element)
    {
        List excludeList = getDataList(excludeModel, EXCLUDE_COL);
        element.setExcludeList(excludeList);
    }

    private List getDataList(PowerTableModel model, String colName)
    {
        String[] dataArray = model.getData().getColumn(colName);
        List list = new LinkedList();
        for (int i = 0; i < dataArray.length; i++)
        {
            list.add(dataArray[i]);
        }
        return list;
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("proxy_title");
    }

    public Collection getMenuCategories()
    {
        return Arrays.asList(new String[] { MenuFactory.NON_TEST_ELEMENTS });
    }

    public void configure(TestElement element)
    {
        log.debug("Configuring gui with " + element);
        super.configure(element);
        model = (ProxyControl)element;
        portField.setText(model.getPropertyAsString(ProxyControl.PORT));
        populateTable(includeModel, model.getIncludePatterns().iterator());
        populateTable(excludeModel, model.getExcludePatterns().iterator());
        repaint();
    }

    private void populateTable(PowerTableModel model, PropertyIterator iter)
    {
        model.clearData();
        while (iter.hasNext())
        {
            model.addRow(new Object[] { iter.next().getStringValue()});
        }
        model.fireTableDataChanged();
    }

    public void focusLost(FocusEvent e)
    {
        try
        {
            ((JTable) e.getSource()).getCellEditor().stopCellEditing();
        }
        catch (Exception err)
        {}
    }

    public void focusGained(FocusEvent e)
    {}

    /****************************************
     * !ToDo (Method description)
     *
     *@param action  !ToDo (Parameter description)
     ***************************************/
    public void actionPerformed(ActionEvent action)
    {
        String command = action.getActionCommand();

        if (command.equals(STOP))
        {
            model.stopProxy();
            stop.setEnabled(false);
            start.setEnabled(true);
        }
        else if (command.equals(START))
        {
            model = (ProxyControl) createTestElement();
            startProxy();
        }
        else if (command.equals(RESTART))
        {
            model.stopProxy();
            model = (ProxyControl) createTestElement();
            startProxy();
        }
        else if (command.equals(ADD_EXCLUDE))
        {
            excludeModel.addNewRow();
            excludeModel.fireTableDataChanged();
            if (stop.isEnabled())
                enableRestart();
        }
        else if (command.equals(ADD_INCLUDE))
        {
            includeModel.addNewRow();
            includeModel.fireTableDataChanged();
            enableRestart();
        }
        else if (command.equals(DELETE_EXCLUDE))
        {
            excludeModel.removeRow(excludeTable.getSelectedRow());
            excludeModel.fireTableDataChanged();
            enableRestart();
        }
        else if (command.equals(DELETE_INCLUDE))
        {
            includeModel.removeRow(includeTable.getSelectedRow());
            includeModel.fireTableDataChanged();
            enableRestart();
        }
    }

    private void startProxy()
    {
        ValueReplacer replacer = GuiPackage.getInstance().getReplacer();
        try
        {
            replacer.replaceValues(model);
            model.startProxy();
            start.setEnabled(false);
            stop.setEnabled(true);
            restart.setEnabled(false);
        }
        catch (InvalidVariableException e)
        {
            JOptionPane.showMessageDialog(this, JMeterUtils.getResString("invalid_variables"), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enableRestart()
    {
        if (stop.isEnabled())
        {
            restart.setEnabled(true);
        }
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param e  !ToDo (Parameter description)
     ***************************************/
    public void keyPressed(KeyEvent e)
    {}

    /****************************************
     * !ToDo (Method description)
     *
     *@param e  !ToDo (Parameter description)
     ***************************************/
    public void keyTyped(KeyEvent e)
    {}

    /****************************************
     * !ToDo (Method description)
     *
     *@param e  !ToDo (Parameter description)
     ***************************************/
    public void keyReleased(KeyEvent e)
    {
        String fieldName = e.getComponent().getName();

        if (fieldName.equals(ProxyControl.PORT))
        {
            try
            {
                Integer.parseInt(portField.getText());
            }
            catch (NumberFormatException nfe)
            {
                if (portField.getText().length() > 0)
                {
                    JOptionPane.showMessageDialog(this, "You must enter a valid number", "Invalid data", JOptionPane.WARNING_MESSAGE);

                    // Right now, the cleanest thing to do is simply clear the
                    // entire text field. We do not want to set the text to
                    // the default because that would be confusing to the user.
                    // For example, the user typed "5t" instead of "56". After
                    // the user closes the error dialog, the text would change
                    // from "5t" to "1".  A litle confusing. If anything, it
                    // should display just "5". Future enhancement...
                    portField.setText("");
                }
            }
        }
    }

    private void init()
    {
        setBorder (BorderFactory.createEmptyBorder(10, 10, 5, 10));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        add(createTitleLabel());

        add(namePanel);
        add(createPortPanel());
        add(createIncludePanel());
        add(createExcludePanel());
        add(createControls());        
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel(JMeterUtils.getResString("proxy_title"));
        Font curFont = titleLabel.getFont();
        titleLabel.setFont(curFont.deriveFont((float)curFont.getSize() + 4));
        titleLabel.setAlignmentX(0.5f);
        return titleLabel;
    }
    
    private JPanel createControls()
    {
        JPanel panel = new JPanel();

        start = new JButton(JMeterUtils.getResString("start"));
        start.addActionListener(this);
        start.setActionCommand(START);

        stop = new JButton(JMeterUtils.getResString("stop"));
        stop.addActionListener(this);
        stop.setActionCommand(STOP);

        restart = new JButton(JMeterUtils.getResString("restart"));
        restart.addActionListener(this);
        restart.setActionCommand(RESTART);

        panel.add(start);
        panel.add(stop);
        panel.add(restart);
        start.setEnabled(true);
        stop.setEnabled(false);
        restart.setEnabled(false);

        return panel;
    }

    private JPanel createPortPanel()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel(JMeterUtils.getResString("port")));

        portField = new JTextField(8);
        portField.setName(ProxyControl.PORT);
        portField.addKeyListener(this);
        portField.setText("8080");
        panel.add(portField);
        return panel;
    }

    private JPanel createIncludePanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("patterns_to_include")));

        includeModel = new PowerTableModel(new String[] { INCLUDE_COL }, new Class[] { String.class });
        includeTable = new JTable(includeModel);
        includeTable.setPreferredScrollableViewportSize(new Dimension(100, 70));
        
        JScrollPane scroller = new JScrollPane(includeTable);
        scroller.setBackground(panel.getBackground());
        panel.add(scroller, BorderLayout.CENTER);
        
        JPanel buttonPanel = createTableButtonPanel(ADD_INCLUDE, DELETE_INCLUDE);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        includeTable.addFocusListener(this);
        return panel;
    }
    
    private JPanel createExcludePanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("patterns_to_exclude")));

        excludeModel = new PowerTableModel(new String[] { EXCLUDE_COL }, new Class[] { String.class });
        excludeTable = new JTable(excludeModel);
        excludeTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

        JScrollPane scroller = new JScrollPane(excludeTable);
        scroller.setBackground(panel.getBackground());
        panel.add(scroller, BorderLayout.CENTER);
        
        JPanel buttonPanel = createTableButtonPanel(ADD_EXCLUDE, DELETE_EXCLUDE);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        excludeTable.addFocusListener(this);
        return panel;
    }

    private JPanel createTableButtonPanel(String addCommand, String deleteCommand) {
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton(JMeterUtils.getResString("add"));
        addButton.setActionCommand(ADD_INCLUDE);
        addButton.addActionListener(this);
        buttonPanel.add(addButton);

        JButton deleteButton = new JButton(JMeterUtils.getResString("delete"));
        deleteButton.setActionCommand(deleteCommand);
        deleteButton.addActionListener(this);
        buttonPanel.add(deleteButton);
        
        return buttonPanel;        
    }

    public void setNode(JMeterTreeNode node)
    {
        namePanel.setNode(node);
    }

    /**
     * Returns the portField.
     * @return JTextField
     */
    protected JTextField getPortField()
    {
        return portField;
    }

}
