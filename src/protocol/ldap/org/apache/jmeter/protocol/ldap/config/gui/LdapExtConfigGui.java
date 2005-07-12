//$Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.ldap.config.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.ldap.sampler.LDAPExtSampler;
import org.apache.jmeter.protocol.ldap.config.gui.LDAPArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;

/*******************************************************************************
 * author Dolf Smits(Dolf.Smits@Siemens.com) created Aug 09 2003 11:00 AM
 * company Siemens Netherlands N.V..
 * 
 * Based on the work of: author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 * created Apr 29 2003 11:00 AM company Sip Technologies and Exports Ltd.
 ******************************************************************************/

/*******************************************************************************
 * This class LdapConfigGui is user interface gui for getting all the
 * configuration value from the user
 ******************************************************************************/

public class LdapExtConfigGui extends AbstractConfigGui implements ItemListener {
	// private final static String ROOTDN = "rootDn";
	// private final static String TEST = "tesT";
	// private static String testValue="NNNN";

	private JTextField rootdn = new JTextField(20);

	private JTextField searchbase = new JTextField(20);

	private JTextField searchfilter = new JTextField(20);

	private JTextField delete = new JTextField(20);

	private JTextField add = new JTextField(20);

	private JTextField modify = new JTextField(20);

	private JTextField servername = new JTextField(20);

	private JTextField port = new JTextField(20);

	private JTextField scope = new JTextField(20);

	private JTextField countlim = new JTextField(20);

	private JTextField timelim = new JTextField(20);

	private JTextField attribs = new JTextField(20);

	private JTextField retobj = new JTextField(20);

	private JTextField deref = new JTextField(20);

	private JTextField userdn = new JTextField(20);

	private JTextField userpw = new JPasswordField(20);

	private JTextField comparedn = new JTextField(20);

	private JTextField comparefilt = new JTextField(20);

	private JTextField suserdn = new JTextField(20);

	private JTextField suserpw = new JPasswordField(20);

	private JTextField modddn = new JTextField(20);

	private JTextField newdn = new JTextField(20);

	private JRadioButton addTest = new JRadioButton(JMeterUtils.getResString("addTest"));

	private JRadioButton modifyTest = new JRadioButton(JMeterUtils.getResString("modTest"));

	private JRadioButton deleteTest = new JRadioButton(JMeterUtils.getResString("delTest"));

	private JRadioButton searchTest = new JRadioButton(JMeterUtils.getResString("searchTest"));

	private JRadioButton bind = new JRadioButton(JMeterUtils.getResString("bind"));

	private JRadioButton rename = new JRadioButton(JMeterUtils.getResString("rename"));

	private JRadioButton unbind = new JRadioButton(JMeterUtils.getResString("unbind"));

	private JRadioButton sbind = new JRadioButton(JMeterUtils.getResString("sbind"));

	private JRadioButton compare = new JRadioButton(JMeterUtils.getResString("compare"));

	private ButtonGroup bGroup = new ButtonGroup();

	private boolean displayName = true;

	ArgumentsPanel tableAddPanel = new ArgumentsPanel(JMeterUtils.getResString("addTest"));

	LDAPArgumentsPanel tableModifyPanel = new LDAPArgumentsPanel(JMeterUtils.getResString("modTest"));

	private JPanel cards;

	/***************************************************************************
	 * Default constructor for LdapConfigGui
	 **************************************************************************/
	public LdapExtConfigGui() {
		this(true);
	}

	/***************************************************************************
	 * !ToDo (Constructor description)
	 * 
	 * @param displayName
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public LdapExtConfigGui(boolean displayName) {
		this.displayName = displayName;
		init();
	}

	public String getLabelResource() {
		return "ldapext_sample_title";
	}

	// Remove this when status changes (and update component_reference)
	public String getStaticLabel() {
		return super.getStaticLabel() + " (ALPHA)";
	}

	/**
	 * A newly created component can be initialized with the contents of a Test
	 * Element object by calling this method. The component is responsible for
	 * querying the Test Element object for the relevant information to display
	 * in its GUI.
	 * 
	 * @param element
	 *            the TestElement to configure
	 */
	public void configure(TestElement element) {
		super.configure(element);
		servername.setText(element.getPropertyAsString(LDAPExtSampler.SERVERNAME));
		port.setText(element.getPropertyAsString(LDAPExtSampler.PORT));
		rootdn.setText(element.getPropertyAsString(LDAPExtSampler.ROOTDN));
		scope.setText(element.getPropertyAsString(LDAPExtSampler.SCOPE));
		countlim.setText(element.getPropertyAsString(LDAPExtSampler.COUNTLIM));
		timelim.setText(element.getPropertyAsString(LDAPExtSampler.TIMELIM));
		attribs.setText(element.getPropertyAsString(LDAPExtSampler.ATTRIBS));
		retobj.setText(element.getPropertyAsString(LDAPExtSampler.RETOBJ));
		deref.setText(element.getPropertyAsString(LDAPExtSampler.DEREF));
		userpw.setText(element.getPropertyAsString(LDAPExtSampler.USERPW));
		userdn.setText(element.getPropertyAsString(LDAPExtSampler.USERDN));
		comparedn.setText(element.getPropertyAsString(LDAPExtSampler.COMPAREDN));
		comparefilt.setText(element.getPropertyAsString(LDAPExtSampler.COMPAREFILT));
		suserpw.setText(element.getPropertyAsString(LDAPExtSampler.SUSERPW));
		suserdn.setText(element.getPropertyAsString(LDAPExtSampler.SUSERDN));
		modddn.setText(element.getPropertyAsString(LDAPExtSampler.MODDDN));
		newdn.setText(element.getPropertyAsString(LDAPExtSampler.NEWDN));
		CardLayout cl = (CardLayout) (cards.getLayout());
		if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("unbind")) {
			unbind.setSelected(true);
			cl.show(cards, "unbind");
		} else if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("bind")) {
			bind.setSelected(true);
			cl.show(cards, "bind");
		} else if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("sbind")) {
			sbind.setSelected(true);
			cl.show(cards, "sbind");
		} else if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("compare")) {
			compare.setSelected(true);
			cl.show(cards, "compare");
		} else if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("add")) {
			addTest.setSelected(true);
			add.setText(element.getPropertyAsString(LDAPExtSampler.BASE_ENTRY_DN));
			tableAddPanel.configure((TestElement) element.getProperty(LDAPExtSampler.ARGUMENTS).getObjectValue());
			cl.show(cards, "Add");
		} else if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("modify")) {
			modifyTest.setSelected(true);
			modify.setText(element.getPropertyAsString(LDAPExtSampler.BASE_ENTRY_DN));
			tableModifyPanel
					.configure((TestElement) element.getProperty(LDAPExtSampler.LDAPARGUMENTS).getObjectValue());
			cl.show(cards, "Modify");
		} else if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("delete")) {
			deleteTest.setSelected(true);
			delete.setText(element.getPropertyAsString(LDAPExtSampler.DELETE));
			cl.show(cards, "Delete");
		} else if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("rename")) {
			rename.setSelected(true);
			cl.show(cards, "rename");
		} else if (element.getPropertyAsString(LDAPExtSampler.TEST).equals("search")) {
			searchTest.setSelected(true);
			searchbase.setText(element.getPropertyAsString(LDAPExtSampler.SEARCHBASE));
			searchfilter.setText(element.getPropertyAsString(LDAPExtSampler.SEARCHFILTER));
			cl.show(cards, "Search");
		}
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		ConfigTestElement element = new ConfigTestElement();
		modifyTestElement(element);
		return element;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		element.clear();
		configureTestElement(element);
		element.setProperty(LDAPExtSampler.SERVERNAME, servername.getText());
		element.setProperty(LDAPExtSampler.PORT, port.getText());
		element.setProperty(LDAPExtSampler.ROOTDN, rootdn.getText());
		element.setProperty(LDAPExtSampler.SCOPE, scope.getText());
		element.setProperty(LDAPExtSampler.COUNTLIM, countlim.getText());
		element.setProperty(LDAPExtSampler.TIMELIM, timelim.getText());
		element.setProperty(LDAPExtSampler.ATTRIBS, attribs.getText());
		element.setProperty(LDAPExtSampler.RETOBJ, retobj.getText());
		element.setProperty(LDAPExtSampler.DEREF, deref.getText());
		element.setProperty(LDAPExtSampler.USERDN, userdn.getText());
		element.setProperty(LDAPExtSampler.USERPW, userpw.getText());
		element.setProperty(LDAPExtSampler.COMPAREDN, comparedn.getText());
		element.setProperty(LDAPExtSampler.COMPAREFILT, comparefilt.getText());
		element.setProperty(LDAPExtSampler.SUSERDN, suserdn.getText());
		element.setProperty(LDAPExtSampler.SUSERPW, suserpw.getText());
		element.setProperty(LDAPExtSampler.MODDDN, modddn.getText());
		element.setProperty(LDAPExtSampler.NEWDN, newdn.getText());
		if (addTest.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.ADD));
			element.setProperty(new StringProperty(LDAPExtSampler.BASE_ENTRY_DN, add.getText()));
			element.setProperty(new TestElementProperty(LDAPExtSampler.ARGUMENTS, tableAddPanel.createTestElement()));
		}
		if (modifyTest.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.MODIFY));
			element.setProperty(new StringProperty(LDAPExtSampler.BASE_ENTRY_DN, modify.getText()));
			element.setProperty(new TestElementProperty(LDAPExtSampler.LDAPARGUMENTS, tableModifyPanel
					.createTestElement()));
		}
		if (deleteTest.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.DELETE));
			element.setProperty(new StringProperty(LDAPExtSampler.DELETE, delete.getText()));
		}
		if (searchTest.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.SEARCHBASE));
			element.setProperty(new StringProperty(LDAPExtSampler.SEARCHBASE, searchbase.getText()));
			element.setProperty(new StringProperty(LDAPExtSampler.SEARCHFILTER, searchfilter.getText()));
		}
		if (bind.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.BIND));
		}
		if (sbind.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.SBIND));
		}
		if (compare.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.COMPARE));
		}
		if (rename.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.RENAME));
		}
		if (unbind.isSelected()) {
			element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.UNBIND));
		}
	}

	/***************************************************************************
	 * This itemStateChanged listener for changing the card layout for based on
	 * the test selected in the User defined test case.
	 **************************************************************************/
	public void itemStateChanged(ItemEvent ie) {
		CardLayout cl = (CardLayout) (cards.getLayout());
		if (addTest.isSelected()) {
			cl.show(cards, "Add");
		} else if (deleteTest.isSelected()) {
			cl.show(cards, "Delete");
		} else if (bind.isSelected()) {
			cl.show(cards, "Bind");
		} else if (sbind.isSelected()) {
			cl.show(cards, "Sbind");
		} else if (rename.isSelected()) {
			cl.show(cards, "Rename");
		} else if (compare.isSelected()) {
			cl.show(cards, "Compare");
		} else if (searchTest.isSelected()) {
			cl.show(cards, "Search");
		} else if (modifyTest.isSelected()) {
			cl.show(cards, "Modify");
		} else {
			cl.show(cards, "");
		}
	}

	/***************************************************************************
	 * This will create the servername panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createServernamePanel() {
		JPanel serverPanel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("servername"));
		label.setLabelFor(servername);
		serverPanel.add(label, BorderLayout.WEST);
		serverPanel.add(servername, BorderLayout.CENTER);
		return serverPanel;
	}

	/***************************************************************************
	 * This will create the port panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createPortPanel() {
		JPanel portPanel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("port"));
		label.setLabelFor(port);
		portPanel.add(label, BorderLayout.WEST);
		portPanel.add(port, BorderLayout.CENTER);
		return portPanel;
	}

	/***************************************************************************
	 * This will create the Root distinguised name panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createRootdnPanel() {
		JPanel rootdnPanel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("ddn"));
		label.setLabelFor(rootdn);
		rootdnPanel.add(label, BorderLayout.WEST);
		rootdnPanel.add(rootdn, BorderLayout.CENTER);
		return rootdnPanel;
	}

	/***************************************************************************
	 * This will create the bind panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createSbindPanel() {
		VerticalPanel sbindPanel = new VerticalPanel();
		JPanel sBPanel = new JPanel(new BorderLayout(5, 0));
		JLabel sBlabel0 = new JLabel(JMeterUtils.getResString("userdn"));
		sBlabel0.setLabelFor(suserdn);
		sBPanel.add(sBlabel0, BorderLayout.WEST);
		sBPanel.add(suserdn, BorderLayout.CENTER);
		sbindPanel.add(sBPanel);

		JPanel sB1Panel = new JPanel(new BorderLayout(5, 0));
		JLabel sBlabel1 = new JLabel(JMeterUtils.getResString("userpw"));
		sBlabel1.setLabelFor(suserpw);
		sB1Panel.add(sBlabel1, BorderLayout.WEST);
		sB1Panel.add(suserpw, BorderLayout.CENTER);
		sbindPanel.add(sB1Panel);
		return sbindPanel;
	}

	/***************************************************************************
	 * This will create the bind panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createBindPanel() {
		VerticalPanel bindPanel = new VerticalPanel();
		bindPanel.add(createServernamePanel());
		bindPanel.add(createPortPanel());
		bindPanel.add(createRootdnPanel());

		JPanel BPanel = new JPanel(new BorderLayout(5, 0));
		JLabel Blabel0 = new JLabel(JMeterUtils.getResString("userdn"));
		Blabel0.setLabelFor(scope);
		BPanel.add(Blabel0, BorderLayout.WEST);
		BPanel.add(userdn, BorderLayout.CENTER);
		bindPanel.add(BPanel);

		JPanel B1Panel = new JPanel(new BorderLayout(5, 0));
		JLabel Blabel1 = new JLabel(JMeterUtils.getResString("userpw"));
		Blabel1.setLabelFor(countlim);
		B1Panel.add(Blabel1, BorderLayout.WEST);
		B1Panel.add(userpw, BorderLayout.CENTER);
		bindPanel.add(B1Panel);
		return bindPanel;
	}

	/***************************************************************************
	 * This will create the bind panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createComparePanel() {
		VerticalPanel cbindPanel = new VerticalPanel();
		JPanel cBPanel = new JPanel(new BorderLayout(5, 0));
		JLabel cBlabel0 = new JLabel(JMeterUtils.getResString("entrydn"));
		cBlabel0.setLabelFor(comparedn);
		cBPanel.add(cBlabel0, BorderLayout.WEST);
		cBPanel.add(comparedn, BorderLayout.CENTER);
		cbindPanel.add(cBPanel);

		JPanel cBPanel1 = new JPanel(new BorderLayout(5, 0));
		JLabel cBlabel1 = new JLabel(JMeterUtils.getResString("comparefilt"));
		cBlabel1.setLabelFor(comparefilt);
		cBPanel1.add(cBlabel1, BorderLayout.WEST);
		cBPanel1.add(comparefilt, BorderLayout.CENTER);
		cbindPanel.add(cBPanel1);

		return cbindPanel;
	}

	/***************************************************************************
	 * This will create the Search controls panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createSCPanel() {
		VerticalPanel SCPanel = new VerticalPanel();

		JPanel SC0Panel = new JPanel(new BorderLayout(5, 0));
		JLabel label0 = new JLabel(JMeterUtils.getResString("scope"));
		label0.setLabelFor(scope);
		SC0Panel.add(label0, BorderLayout.WEST);
		SC0Panel.add(scope, BorderLayout.CENTER);
		SCPanel.add(SC0Panel);

		JPanel SC1Panel = new JPanel(new BorderLayout(5, 0));
		JLabel label1 = new JLabel(JMeterUtils.getResString("countlim"));
		label1.setLabelFor(countlim);
		SC1Panel.add(label1, BorderLayout.WEST);
		SC1Panel.add(countlim, BorderLayout.CENTER);
		SCPanel.add(SC1Panel);

		JPanel SC2Panel = new JPanel(new BorderLayout(5, 0));
		JLabel label2 = new JLabel(JMeterUtils.getResString("timelim"));
		label2.setLabelFor(timelim);
		SC2Panel.add(label2, BorderLayout.WEST);
		SC2Panel.add(timelim, BorderLayout.CENTER);
		SCPanel.add(SC2Panel);

		JPanel SC3Panel = new JPanel(new BorderLayout(5, 0));
		JLabel label3 = new JLabel(JMeterUtils.getResString("attrs"));
		label3.setLabelFor(attribs);
		SC3Panel.add(label3, BorderLayout.WEST);
		SC3Panel.add(attribs, BorderLayout.CENTER);
		SCPanel.add(SC3Panel);

		JPanel SC4Panel = new JPanel(new BorderLayout(5, 0));
		JLabel label4 = new JLabel(JMeterUtils.getResString("retobj"));
		label4.setLabelFor(retobj);
		SC4Panel.add(label4, BorderLayout.WEST);
		SC4Panel.add(retobj, BorderLayout.CENTER);
		SCPanel.add(SC4Panel);

		JPanel SC5Panel = new JPanel(new BorderLayout(5, 0));
		JLabel label5 = new JLabel(JMeterUtils.getResString("deref"));
		label5.setLabelFor(deref);
		SC5Panel.add(label5, BorderLayout.WEST);
		SC5Panel.add(deref, BorderLayout.CENTER);
		SCPanel.add(SC5Panel);

		return SCPanel;
	}

	/***************************************************************************
	 * This will create the Search panel in the LdapConfigGui
	 **************************************************************************/

	private JPanel createSearchPanel() {
		VerticalPanel searchPanel = new VerticalPanel();

		JPanel searchBPanel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("searchbase"));
		label.setLabelFor(searchbase);
		searchBPanel.add(label, BorderLayout.WEST);
		searchBPanel.add(searchbase, BorderLayout.CENTER);

		JPanel searchFPanel = new JPanel(new BorderLayout(5, 0));
		JLabel label20 = new JLabel(JMeterUtils.getResString("searchfilter"));
		label20.setLabelFor(searchfilter);
		searchFPanel.add(label20, BorderLayout.WEST);
		searchFPanel.add(searchfilter, BorderLayout.CENTER);

		searchPanel.add(searchBPanel);
		searchPanel.add(searchFPanel);
		searchPanel.add(createSCPanel());
		return searchPanel;
	}

	/***************************************************************************
	 * This will create the Moddn panel in the LdapConfigGui
	 **************************************************************************/

	private JPanel createModdnPanel() {
		VerticalPanel modPanel = new VerticalPanel();

		JPanel renamePanel = new JPanel(new BorderLayout(5, 0));
		JLabel labelmod = new JLabel(JMeterUtils.getResString("modddn"));
		labelmod.setLabelFor(modddn);
		renamePanel.add(labelmod, BorderLayout.WEST);
		renamePanel.add(modddn, BorderLayout.CENTER);

		JPanel rename2Panel = new JPanel(new BorderLayout(5, 0));
		JLabel labelmod2 = new JLabel(JMeterUtils.getResString("newdn"));
		labelmod2.setLabelFor(newdn);
		rename2Panel.add(labelmod2, BorderLayout.WEST);
		rename2Panel.add(newdn, BorderLayout.CENTER);

		modPanel.add(renamePanel);
		modPanel.add(rename2Panel);
		return modPanel;
	}

	/***************************************************************************
	 * This will create the Delete panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createDeletePanel() {
		VerticalPanel panel = new VerticalPanel();
		JPanel deletePanel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("delete"));
		label.setLabelFor(delete);
		deletePanel.add(label, BorderLayout.WEST);
		deletePanel.add(delete, BorderLayout.CENTER);
		panel.add(deletePanel);
		return panel;
	}

	/***************************************************************************
	 * This will create the Add test panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createAddPanel() {
		JPanel addPanel = new JPanel(new BorderLayout(5, 0));
		JPanel addInnerPanel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("entrydn"));
		label.setLabelFor(add);
		addInnerPanel.add(label, BorderLayout.WEST);
		addInnerPanel.add(add, BorderLayout.CENTER);
		addPanel.add(addInnerPanel, BorderLayout.NORTH);
		addPanel.add(tableAddPanel, BorderLayout.CENTER);
		return addPanel;
	}

	/***************************************************************************
	 * This will create the Modify panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createModifyPanel() {
		JPanel modifyPanel = new JPanel(new BorderLayout(5, 0));
		JPanel modifyInnerPanel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("entrydn"));
		label.setLabelFor(modify);
		modifyInnerPanel.add(label, BorderLayout.WEST);
		modifyInnerPanel.add(modify, BorderLayout.CENTER);
		modifyPanel.add(modifyInnerPanel, BorderLayout.NORTH);
		modifyPanel.add(tableModifyPanel, BorderLayout.CENTER);
		return modifyPanel;
	}

	/***************************************************************************
	 * This will create the user defined test panel for create or modify or
	 * delete or search based on the panel selected in the itemevent in the
	 * LdapConfigGui
	 **************************************************************************/
	private JPanel testPanel() {
		cards = new JPanel(new CardLayout());
		cards.add(new JPanel(), "");
		cards.add(createAddPanel(), "Add");
		cards.add(createModifyPanel(), "Modify");
		cards.add(createModdnPanel(), "Rename");
		cards.add(createDeletePanel(), "Delete");
		cards.add(createSearchPanel(), "Search");
		cards.add(createBindPanel(), "Bind");
		cards.add(createComparePanel(), "Compare");
		cards.add(createSbindPanel(), "Sbind");
		return cards;
	}

	/***************************************************************************
	 * This will create the test panel in the LdapConfigGui
	 **************************************************************************/
	private JPanel createTestPanel() {
		JPanel testPanel = new JPanel(new BorderLayout());
		testPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("test_configuration")));

		testPanel.add(new JLabel(JMeterUtils.getResString("testt")));
		JPanel rowPanel = new JPanel();
		JPanel row2Panel = new JPanel();

		rowPanel.add(bind);
		bGroup.add(bind);
		rowPanel.add(unbind);
		bGroup.add(unbind);
		rowPanel.add(sbind);
		bGroup.add(sbind);
		rowPanel.add(rename);
		bGroup.add(rename);
		row2Panel.add(addTest);
		bGroup.add(addTest);
		row2Panel.add(deleteTest);
		bGroup.add(deleteTest);
		row2Panel.add(searchTest);
		bGroup.add(searchTest);
		row2Panel.add(compare);
		bGroup.add(compare);
		row2Panel.add(modifyTest);
		bGroup.add(modifyTest);
		testPanel.add(rowPanel, BorderLayout.NORTH);
		testPanel.add(row2Panel, BorderLayout.SOUTH);
		return testPanel;
	}

	/***************************************************************************
	 * This will initalise all the panel in the LdapConfigGui
	 **************************************************************************/
	private void init() {
		setLayout(new BorderLayout(0, 5));
		if (displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
		}
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(createTestPanel());
		mainPanel.add(testPanel());
		add(mainPanel, BorderLayout.CENTER);
		bind.addItemListener(this);
		sbind.addItemListener(this);
		unbind.addItemListener(this);
		compare.addItemListener(this);
		addTest.addItemListener(this);
		modifyTest.addItemListener(this);
		rename.addItemListener(this);
		deleteTest.addItemListener(this);
		searchTest.addItemListener(this);
	}
}
