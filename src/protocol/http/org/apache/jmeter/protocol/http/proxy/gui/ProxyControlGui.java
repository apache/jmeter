/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.functions.ValueReplacer;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: Jakarta-JMeter Description: Copyright: Copyright (c) 2001 Company:
 * Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class ProxyControlGui extends JPanel implements JMeterGUIComponent, ActionListener,
		KeyListener,FocusListener
{

	NamePanel namePanel;
	JTextField portField;

	ProxyControl model;

	JTable excludeTable;
	PowerTableModel excludeModel;
	JTable includeTable;
	PowerTableModel includeModel;
	JButton addExclude,deleteExclude,addInclude,deleteInclude;

	JButton stop, start,restart;
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
		namePanel = new NamePanel();
		setName(getStaticLabel());
		init();
	}

	public JPopupMenu createPopupMenu()
	{
		return MenuFactory.getDefaultTimerMenu();
	}

	public TestElement createTestElement()
	{
		ProxyControl element = new ProxyControl();
		element.setProperty(TestElement.NAME,getName());
		element.setPort(Integer.parseInt(portField.getText()));
		setIncludeListInProxyControl(element);
		setExcludeListInProxyControl(element);
		element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
		element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());
		return element;
	}

	protected void setIncludeListInProxyControl(ProxyControl element) {
		List includeList = getDataList(includeModel,INCLUDE_COL);
		element.setIncludeList(includeList);
	}

	protected void setExcludeListInProxyControl(ProxyControl element)
	{
		List excludeList = getDataList(excludeModel,EXCLUDE_COL);
		element.setExcludeList(excludeList);
	}

	private List getDataList(PowerTableModel model,String colName) {
		String[] dataArray = model.getData().getColumn(colName);
		List list = new LinkedList();
		for(int i = 0;i < dataArray.length;i++)
		{
			list.add(dataArray[i]);
		}
		return list;
	}



	public void setName(String name)
	{
		super.setName(name);
		namePanel.setName(name);
	}

	public String getName()
	{
		return namePanel.getName();
	}

	public String getStaticLabel()
	{
		return JMeterUtils.getResString("proxy_title");
	}

	public Collection getMenuCategories()
	{
		return Arrays.asList(new String[]{MenuFactory.NON_TEST_ELEMENTS});
	}

	public void configure(TestElement element)
	{
		ProxyControl el = (ProxyControl)element;
		setName(element.getProperty(TestElement.NAME).toString());
		portField.setText(element.getProperty(ProxyControl.PORT).toString());
		populateTable(includeModel,el.getIncludePatterns().iterator());
		populateTable(excludeModel,el.getExcludePatterns().iterator());
	}

	private void populateTable(PowerTableModel model,Iterator iter) {
		while(iter.hasNext())
		{
			model.addRow(new Object[]{iter.next()});
		}
	}

	public void focusLost(FocusEvent e)
	{
		try
		{
			((JTable)e.getSource()).getCellEditor().stopCellEditing();
		}
		catch(Exception err)
		{
		}
	}

	public void focusGained(FocusEvent e)
	{
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param action  !ToDo (Parameter description)
	 ***************************************/
	public void actionPerformed(ActionEvent action)
	{
		String command = action.getActionCommand();

		if(command.equals(STOP))
		{
			model.stopProxy();
			stop.setEnabled(false);
			start.setEnabled(true);
			model = null;
		}
		else if(command.equals(START))
		{
			model = (ProxyControl)createTestElement();
			startProxy();
		}
		else if(command.equals(RESTART))
		{
			model.stopProxy();
			model = (ProxyControl)createTestElement();
			startProxy();
		}
		else if(command.equals(this.ADD_EXCLUDE))
		{
			excludeModel.addNewRow();
			excludeModel.fireTableDataChanged();
			if(model != null)
			enableRestart();
		}
		else if(command.equals(this.ADD_INCLUDE))
		{
			includeModel.addNewRow();
			includeModel.fireTableDataChanged();
			enableRestart();
		}
		else if(command.equals(this.DELETE_EXCLUDE))
		{
			excludeModel.removeRow(excludeTable.getSelectedRow());
			excludeModel.fireTableDataChanged();
			enableRestart();
		}
		else if(command.equals(this.DELETE_INCLUDE))
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
			JOptionPane.showMessageDialog(this,JMeterUtils.getResString(
					"invalid_variables"),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}

	private void enableRestart()
	{
		if(model != null)
		{
			restart.setEnabled(true);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void keyPressed(KeyEvent e) { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void keyTyped(KeyEvent e) { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void keyReleased(KeyEvent e)
	{
		String fieldName = e.getComponent().getName();

		if(fieldName.equals(ProxyControl.PORT))
		{
			try
			{
				Integer.parseInt(portField.getText());
			}
			catch(NumberFormatException nfe)
			{
				if(portField.getText().length() > 0)
				{
					JOptionPane.showMessageDialog(this, "You must enter a valid number",
							"Invalid data", JOptionPane.WARNING_MESSAGE);

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
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = gbc.WEST;
		gbc.fill = gbc.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;

		// MAIN PANEL
		JPanel mainPanel = new JPanel();
		add(mainPanel,gbc.clone());
		Border margin = new EmptyBorder(10, 10, 5, 10);
		mainPanel.setBorder(margin);
		mainPanel.setLayout(new GridBagLayout());
		//gbc.fill = gbc.NONE;
		gbc.weighty = 0;

		// TITLE
		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("proxy_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		mainPanel.add(panelTitleLabel,gbc.clone());
		gbc.gridy++;

		// NAME
		mainPanel.add(namePanel,gbc.clone());
		gbc.gridy++;

		mainPanel.add(createPortPanel(),gbc.clone());
		gbc.gridy++;
		gbc.fill = gbc.BOTH;
		gbc.weighty = .5;
		mainPanel.add(createIncludePanel(),gbc.clone());
		gbc.gridy++;
		mainPanel.add(createExcludePanel(),gbc.clone());
		gbc.gridy++;
		gbc.fill = gbc.NONE;
		gbc.weighty = 0;
		mainPanel.add(createControls(),gbc.clone());

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
		panel.revalidate();
		return panel;
	}

	private JPanel createIncludePanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				JMeterUtils.getResString("patterns_to_include")));
		includeTable = new JTable();
		includeModel = new PowerTableModel(new String[]{INCLUDE_COL},
					new Class[]{String.class});
		JScrollPane scroller = new JScrollPane(includeTable);
		scroller.setBackground(panel.getBackground());
		includeTable.setModel(includeModel);
		addInclude = new JButton(JMeterUtils.getResString("add"));
		deleteInclude = new JButton(JMeterUtils.getResString("delete"));
		addInclude.setActionCommand(ADD_INCLUDE);
		deleteInclude.setActionCommand(DELETE_INCLUDE);
		addInclude.addActionListener(this);
		deleteInclude.addActionListener(this);
		panel.add(scroller,BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addInclude);
		buttonPanel.add(deleteInclude);
		panel.add(buttonPanel,BorderLayout.SOUTH);
		includeTable.addFocusListener(this);
		return panel;
	}

	private JPanel createExcludePanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				JMeterUtils.getResString("patterns_to_exclude")));
		excludeTable = new JTable();
		excludeModel = new PowerTableModel(new String[]{EXCLUDE_COL},
					new Class[]{String.class});
		JScrollPane scroller = new JScrollPane(excludeTable);
		scroller.setBackground(panel.getBackground());
		excludeTable.setModel(excludeModel);
		addExclude = new JButton(JMeterUtils.getResString("add"));
		deleteExclude = new JButton(JMeterUtils.getResString("delete"));
		addExclude.setActionCommand(ADD_EXCLUDE);
		deleteExclude.setActionCommand(DELETE_EXCLUDE);
		addExclude.addActionListener(this);
		deleteExclude.addActionListener(this);
		panel.add(scroller,BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addExclude);
		buttonPanel.add(deleteExclude);
		panel.add(buttonPanel,BorderLayout.SOUTH);
		excludeTable.addFocusListener(this);
		return panel;
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
