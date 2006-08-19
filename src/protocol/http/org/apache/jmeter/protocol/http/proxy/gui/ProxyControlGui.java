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

package org.apache.jmeter.protocol.http.proxy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$ updated on $Date$
 */
public class ProxyControlGui extends LogicControllerGui implements JMeterGUIComponent, ActionListener, ItemListener,
		KeyListener, FocusListener, UnsharedComponent {
	private static transient Logger log = LoggingManager.getLoggerForClass();

	private JTextField portField;

	/**
	 * Used to indicate that HTTP request headers should be captured. The
	 * default is to capture the HTTP request headers, which are specific to
	 * particular browser settings.
	 */
	private JCheckBox httpHeaders;

	/**
	 * Whether to group requests together based on inactivity separation periods --
	 * and how to handle such grouping afterwards.
	 */
	private JComboBox groupingMode;

	/**
	 * Add an Assertion to the first sample of each set
	 */
	private JCheckBox addAssertions;

	/**
	 * Set/clear the Use Keep-Alive box on the samplers (default is true)
	 */
	private JCheckBox useKeepAlive;

	/*
	 * Use regexes to match the source data
	 */
	private JCheckBox regexMatch;

	/*
	 * Spoof the client into thinking that it is communicating with http
	 * even if it is really https.
	 */
	private JCheckBox httpsSpoof;
	
	/**
	 * List of available target controllers
	 */
	private JComboBox targetNodes;

	private DefaultComboBoxModel targetNodesModel;

	private ProxyControl model;

	private JTable excludeTable;

	private PowerTableModel excludeModel;

	private JTable includeTable;

	private PowerTableModel includeModel;

	private static final String CHANGE_TARGET = "change_target";

	private JButton stop, start, restart;

	private static final String STOP = "stop";

	private static final String START = "start";

	private static final String RESTART = "restart";

	private static final String ADD_INCLUDE = "add_include";

	private static final String ADD_EXCLUDE = "add_exclude";

	private static final String DELETE_INCLUDE = "delete_include";

	private static final String DELETE_EXCLUDE = "delete_exclude";

	private static final String INCLUDE_COL = JMeterUtils.getResString("patterns_to_include");

	private static final String EXCLUDE_COL = JMeterUtils.getResString("patterns_to_exclude");

	public ProxyControlGui() {
		super();
		log.debug("Creating ProxyControlGui");
		init();
	}

	public TestElement createTestElement() {
		model = makeProxyControl();
		log.debug("creating/configuring model = " + model);
		modifyTestElement(model);
		return model;
	}

	protected ProxyControl makeProxyControl() {
		ProxyControl local = new ProxyControl();
		return local;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement el) {
		configureTestElement(el);
		if (el instanceof ProxyControl) {
			model = (ProxyControl) el;
			model.setPort(portField.getText());
			setIncludeListInProxyControl(model);
			setExcludeListInProxyControl(model);
			model.setCaptureHttpHeaders(httpHeaders.isSelected());
			model.setGroupingMode(groupingMode.getSelectedIndex());
			model.setAssertions(addAssertions.isSelected());
			model.setUseKeepAlive(useKeepAlive.isSelected());
			model.setRegexMatch(regexMatch.isSelected());
			model.setHttpsSpoof(httpsSpoof.isSelected());			
			TreeNodeWrapper nw = (TreeNodeWrapper) targetNodes.getSelectedItem();
			if (nw == null) {
				model.setTarget(null);
			} else {
				model.setTarget(nw.getTreeNode());
			}
		}
	}

	protected void setIncludeListInProxyControl(ProxyControl element) {
		List includeList = getDataList(includeModel, INCLUDE_COL);
		element.setIncludeList(includeList);
	}

	protected void setExcludeListInProxyControl(ProxyControl element) {
		List excludeList = getDataList(excludeModel, EXCLUDE_COL);
		element.setExcludeList(excludeList);
	}

	private List getDataList(PowerTableModel p_model, String colName) {
		String[] dataArray = p_model.getData().getColumn(colName);
		List list = new LinkedList();
		for (int i = 0; i < dataArray.length; i++) {
			list.add(dataArray[i]);
		}
		return list;
	}

	public String getLabelResource() {
		return "proxy_title";
	}

	public Collection getMenuCategories() {
		return Arrays.asList(new String[] { MenuFactory.NON_TEST_ELEMENTS });
	}

	public void configure(TestElement element) {
		log.debug("Configuring gui with " + element);
		super.configure(element);
		model = (ProxyControl) element;
		portField.setText(model.getPortString());
		httpHeaders.setSelected(model.getCaptureHttpHeaders());
		groupingMode.setSelectedIndex(model.getGroupingMode());
		addAssertions.setSelected(model.getAssertions());
		useKeepAlive.setSelected(model.getUseKeepalive());
		regexMatch.setSelected(model.getRegexMatch());
		httpsSpoof.setSelected(model.getHttpsSpoof());

		reinitializeTargetCombo();// Set up list of potential targets and
									// enable listener

		populateTable(includeModel, model.getIncludePatterns().iterator());
		populateTable(excludeModel, model.getExcludePatterns().iterator());
		repaint();
	}

	private void populateTable(PowerTableModel p_model, PropertyIterator iter) {
		p_model.clearData();
		while (iter.hasNext()) {
			p_model.addRow(new Object[] { iter.next().getStringValue() });
		}
		p_model.fireTableDataChanged();
	}

	public void focusLost(FocusEvent e) {
		try {
			((JTable) e.getSource()).getCellEditor().stopCellEditing();
		} catch (Exception err) {
		}
	}

	public void focusGained(FocusEvent e) {
	}

	/*
	 * Handles groupingMode. actionPerfomed is not suitable, as that seems to be
	 * activated whenever the Proxy is selected in the Test Plan
	 */
	public void itemStateChanged(ItemEvent e) {
		// System.err.println(e.paramString());
		enableRestart();
	}

	/***************************************************************************
	 * !ToDo (Method description)
	 * 
	 * @param action
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public void actionPerformed(ActionEvent action) {
		String command = action.getActionCommand();

		// System.err.println(action.paramString()+" "+command+ "
		// "+action.getModifiers());

		if (command.equals(STOP)) {
			model.stopProxy();
			stop.setEnabled(false);
			start.setEnabled(true);
			restart.setEnabled(false);
		} else if (command.equals(START)) {
			startProxy();
		} else if (command.equals(RESTART)) {
			model.stopProxy();
			startProxy();
		} else if (command.equals(ProxyControl.CAPTURE_HTTP_HEADERS) || command.equals(ProxyControl.ADD_ASSERTIONS)
				|| command.equals(ProxyControl.USE_KEEPALIVE) || command.equals(ProxyControl.REGEX_MATCH)
				|| command.equals(ProxyControl.HTTPS_SPOOF)) {
			enableRestart();
		} else if (command.equals(ADD_EXCLUDE)) {
			excludeModel.addNewRow();
			excludeModel.fireTableDataChanged();
			enableRestart();
		} else if (command.equals(ADD_INCLUDE)) {
			includeModel.addNewRow();
			includeModel.fireTableDataChanged();
			enableRestart();
		} else if (command.equals(DELETE_EXCLUDE)) {
			excludeModel.removeRow(excludeTable.getSelectedRow());
			excludeModel.fireTableDataChanged();
			enableRestart();
		} else if (command.equals(DELETE_INCLUDE)) {
			includeModel.removeRow(includeTable.getSelectedRow());
			includeModel.fireTableDataChanged();
			enableRestart();
		} else if (command.equals(CHANGE_TARGET)) {
			log.debug("Change target " + targetNodes.getSelectedItem());
			log.debug("In model " + model);
			TreeNodeWrapper nw = (TreeNodeWrapper) targetNodes.getSelectedItem();
			model.setTarget(nw.getTreeNode());
			enableRestart();
		}
	}

	private void startProxy() {
		ValueReplacer replacer = GuiPackage.getInstance().getReplacer();
		modifyTestElement(model);
		try {
			replacer.replaceValues(model);
			model.startProxy();
			start.setEnabled(false);
			stop.setEnabled(true);
			restart.setEnabled(false);
		} catch (InvalidVariableException e) {
			JOptionPane.showMessageDialog(this, JMeterUtils.getResString("invalid_variables"), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void enableRestart() {
		if (stop.isEnabled()) {
			// System.err.println("Enable Restart");
			restart.setEnabled(true);
		}
	}

	/***************************************************************************
	 * !ToDo (Method description)
	 * 
	 * @param e
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public void keyPressed(KeyEvent e) {
	}

	/***************************************************************************
	 * !ToDo (Method description)
	 * 
	 * @param e
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public void keyTyped(KeyEvent e) {
	}

	/***************************************************************************
	 * !ToDo (Method description)
	 * 
	 * @param e
	 *            !ToDo (Parameter description)
	 **************************************************************************/
	public void keyReleased(KeyEvent e) {
		String fieldName = e.getComponent().getName();

		if (fieldName.equals(ProxyControl.PORT)) {
			try {
				Integer.parseInt(portField.getText());
			} catch (NumberFormatException nfe) {
				if (portField.getText().length() > 0) {
					JOptionPane.showMessageDialog(this, "You must enter a valid number", "Invalid data",
							JOptionPane.WARNING_MESSAGE);

					// Right now, the cleanest thing to do is simply clear the
					// entire text field. We do not want to set the text to
					// the default because that would be confusing to the user.
					// For example, the user typed "5t" instead of "56". After
					// the user closes the error dialog, the text would change
					// from "5t" to "1". A litle confusing. If anything, it
					// should display just "5". Future enhancement...
					portField.setText("");
				}
			}
			enableRestart();
		}
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

		JPanel mainPanel = new JPanel(new BorderLayout());

		Box myBox = Box.createVerticalBox();
		myBox.add(createPortPanel());
		myBox.add(Box.createVerticalStrut(5));
		myBox.add(createTargetPanel());
		myBox.add(Box.createVerticalStrut(5));
		myBox.add(createGroupingPanel());
		myBox.add(Box.createVerticalStrut(5));
		mainPanel.add(myBox, BorderLayout.NORTH);

		Box includeExcludePanel = Box.createVerticalBox();
		includeExcludePanel.add(createIncludePanel());
		includeExcludePanel.add(createExcludePanel());
		mainPanel.add(includeExcludePanel, BorderLayout.CENTER);

		mainPanel.add(createControls(), BorderLayout.SOUTH);

		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel createControls() {
		start = new JButton(JMeterUtils.getResString("start"));
		start.addActionListener(this);
		start.setActionCommand(START);
		start.setEnabled(true);

		stop = new JButton(JMeterUtils.getResString("stop"));
		stop.addActionListener(this);
		stop.setActionCommand(STOP);
		stop.setEnabled(false);

		restart = new JButton(JMeterUtils.getResString("restart"));
		restart.addActionListener(this);
		restart.setActionCommand(RESTART);
		restart.setEnabled(false);

		JPanel panel = new JPanel();
		panel.add(start);
		panel.add(stop);
		panel.add(restart);
		return panel;
	}

	private JPanel createPortPanel() {
		portField = new JTextField(ProxyControl.DEFAULT_PORT_S, 8);
		portField.setName(ProxyControl.PORT);
		portField.addKeyListener(this);

		JLabel label = new JLabel(JMeterUtils.getResString("port"));
		label.setLabelFor(portField);

		httpHeaders = new JCheckBox(JMeterUtils.getResString("proxy_headers"));
		httpHeaders.setName(ProxyControl.CAPTURE_HTTP_HEADERS);
		httpHeaders.setSelected(true); // maintain original default
		httpHeaders.addActionListener(this);
		httpHeaders.setActionCommand(ProxyControl.CAPTURE_HTTP_HEADERS);

		addAssertions = new JCheckBox(JMeterUtils.getResString("proxy_assertions"));
		addAssertions.setName(ProxyControl.ADD_ASSERTIONS);
		addAssertions.setSelected(false);
		addAssertions.addActionListener(this);
		addAssertions.setActionCommand(ProxyControl.ADD_ASSERTIONS);

		useKeepAlive = new JCheckBox(JMeterUtils.getResString("proxy_usekeepalive"));
		useKeepAlive.setName(ProxyControl.USE_KEEPALIVE);
		useKeepAlive.setSelected(true);
		useKeepAlive.addActionListener(this);
		useKeepAlive.setActionCommand(ProxyControl.USE_KEEPALIVE);

		regexMatch = new JCheckBox(JMeterUtils.getResString("proxy_regex"));
		regexMatch.setName(ProxyControl.REGEX_MATCH);
		regexMatch.setSelected(false);
		regexMatch.addActionListener(this);
		regexMatch.setActionCommand(ProxyControl.REGEX_MATCH);

		httpsSpoof = new JCheckBox(JMeterUtils.getResString("proxy_httpsspoofing"));
		httpsSpoof.setName(ProxyControl.HTTPS_SPOOF);
		httpsSpoof.setSelected(false);
		httpsSpoof.addActionListener(this);
		httpsSpoof.setActionCommand(ProxyControl.HTTPS_SPOOF);		
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(label);
		panel.add(portField);

		panel.add(Box.createHorizontalStrut(10));
		panel.add(httpHeaders);

		panel.add(useKeepAlive);
		panel.add(addAssertions);
		panel.add(regexMatch);
		panel.add(httpsSpoof);

		return panel;
	}

	private JPanel createTargetPanel() {
		targetNodesModel = new DefaultComboBoxModel();
		targetNodes = new JComboBox(targetNodesModel);
		targetNodes.setActionCommand(CHANGE_TARGET);
		// Action listener will be added later

		JLabel label = new JLabel(JMeterUtils.getResString("proxy_target"));
		label.setLabelFor(targetNodes);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(label);
		panel.add(targetNodes);

		/*
		 * This listener subscription prevents freeing up the GUI when it's no
		 * longer in use (e.g. on locale change)... plus causes some anoying
		 * NPEs in the GUI instance created by the menu manager just to find out
		 * our name and which menus we want to be in... ... plus I don't think
		 * it's really necessary: configure(TestElement) already takes care of
		 * reinitializing the target combo when we come back to it. And I can't
		 * see how the tree can change in a relevant way without we leaving this
		 * GUI (since it is very unlikely that we will want to record into one
		 * of the controllers created by the proxy). I'll comment it out for the
		 * time being: TODO: remove once we're convinced it's really
		 * unnecessary.
		 */
		/*
		 * try { Class addToTree =
		 * Class.forName("org.apache.jmeter.gui.action.AddToTree"); Class remove =
		 * Class.forName("org.apache.jmeter.gui.action.Remove"); ActionListener
		 * listener = new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { reinitializeTargetCombo(); } };
		 * ActionRouter ar = ActionRouter.getInstance();
		 * ar.addPostActionListener(addToTree, listener);
		 * ar.addPostActionListener(remove, listener); } catch
		 * (ClassNotFoundException e) { // This should never happen -- throw an
		 * Error: throw new Error(e.toString());//JDK1.4: remove .toString() }
		 */

		return panel;
	}

	private JPanel createGroupingPanel() {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		// Note: position of these elements in the menu *must* match the
		// corresponding ProxyControl.GROUPING_* values.
		m.addElement(JMeterUtils.getResString("grouping_no_groups"));
		m.addElement(JMeterUtils.getResString("grouping_add_separators"));
		m.addElement(JMeterUtils.getResString("grouping_in_controllers"));
		m.addElement(JMeterUtils.getResString("grouping_store_first_only"));
		groupingMode = new JComboBox(m);
		groupingMode.setName(ProxyControl.GROUPING_MODE);
		groupingMode.setSelectedIndex(0);
		groupingMode.addItemListener(this);

		JLabel label2 = new JLabel(JMeterUtils.getResString("grouping_mode"));
		label2.setLabelFor(groupingMode);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(label2);
		panel.add(groupingMode);

		return panel;
	}

	private JPanel createIncludePanel() {
		includeModel = new PowerTableModel(new String[] { INCLUDE_COL }, new Class[] { String.class });
		includeTable = new JTable(includeModel);
		includeTable.setPreferredScrollableViewportSize(new Dimension(100, 50));
		includeTable.addFocusListener(this);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("patterns_to_include")));

		panel.add(new JScrollPane(includeTable), BorderLayout.CENTER);
		panel.add(createTableButtonPanel(ADD_INCLUDE, DELETE_INCLUDE), BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createExcludePanel() {
		excludeModel = new PowerTableModel(new String[] { EXCLUDE_COL }, new Class[] { String.class });
		excludeTable = new JTable(excludeModel);
		excludeTable.setPreferredScrollableViewportSize(new Dimension(100, 50));
		excludeTable.addFocusListener(this);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("patterns_to_exclude")));

		panel.add(new JScrollPane(excludeTable), BorderLayout.CENTER);
		panel.add(createTableButtonPanel(ADD_EXCLUDE, DELETE_EXCLUDE), BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createTableButtonPanel(String addCommand, String deleteCommand) {
		JPanel buttonPanel = new JPanel();

		JButton addButton = new JButton(JMeterUtils.getResString("add"));
		addButton.setActionCommand(addCommand);
		addButton.addActionListener(this);
		buttonPanel.add(addButton);

		JButton deleteButton = new JButton(JMeterUtils.getResString("delete"));
		deleteButton.setActionCommand(deleteCommand);
		deleteButton.addActionListener(this);
		buttonPanel.add(deleteButton);

		return buttonPanel;
	}

	private void reinitializeTargetCombo() {
		log.debug("Reinitializing target combo");

		// Stop action notifications while we shuffle this around:
		targetNodes.removeActionListener(this);

		targetNodesModel.removeAllElements();
		GuiPackage gp = GuiPackage.getInstance();
		JMeterTreeNode root;
		if (gp != null) {
			root = (JMeterTreeNode) GuiPackage.getInstance().getTreeModel().getRoot();
			targetNodesModel
					.addElement(new TreeNodeWrapper(null, JMeterUtils.getResString("use_recording_controller")));
			buildNodesModel(root, "", 0);
		}
		TreeNodeWrapper choice = null;
		for (int i = 0; i < targetNodesModel.getSize(); i++) {
			choice = (TreeNodeWrapper) targetNodesModel.getElementAt(i);
			log.debug("Selecting item " + choice + " for model " + model + " in " + this);
			if (choice.getTreeNode() == model.getTarget()) // .equals caused
															// NPE
			{
				break;
			}
		}
		// Reinstate action notifications:
		targetNodes.addActionListener(this);
		// Set the current value:
		targetNodesModel.setSelectedItem(choice);

		log.debug("Reinitialization complete");
	}

	private void buildNodesModel(JMeterTreeNode node, String parent_name, int level) {
		String seperator = " > ";
		if (node != null) {
			for (int i = 0; i < node.getChildCount(); i++) {
				StringBuffer name = new StringBuffer();
				JMeterTreeNode cur = (JMeterTreeNode) node.getChildAt(i);
				TestElement te = cur.getTestElement();
				/*
				 * Will never be true. Probably intended to use
				 * org.apache.jmeter.threads.ThreadGroup rather than
				 * java.lang.ThreadGroup However, that does not work correctly;
				 * whereas treating it as a Controller does. if (te instanceof
				 * ThreadGroup) { name.append(parent_name);
				 * name.append(cur.getName()); name.append(seperator);
				 * buildNodesModel(cur, name.toString(), level); } else
				 */
				if (te instanceof Controller) {
					name.append(spaces(level));
					name.append(parent_name);
					name.append(cur.getName());
					TreeNodeWrapper tnw = new TreeNodeWrapper(cur, name.toString());
					targetNodesModel.addElement(tnw);
					name = new StringBuffer();
					name.append(cur.getName());
					name.append(seperator);
					buildNodesModel(cur, name.toString(), level + 1);
				} else if (te instanceof TestPlan || te instanceof WorkBench) {
					name.append(cur.getName());
					name.append(seperator);
					buildNodesModel(cur, name.toString(), 0);
				}
				// Ignore everything else
			}
		}
	}

	private String spaces(int level) {
		int multi = 4;
		StringBuffer spaces = new StringBuffer(level * multi);
		for (int i = 0; i < level * multi; i++) {
			spaces.append(" ");
		}
		return spaces.toString();
	}

	public void setNode(JMeterTreeNode node) {
		getNamePanel().setNode(node);
	}
}

class TreeNodeWrapper {
	private JMeterTreeNode tn;

	private String label;

	private TreeNodeWrapper() {
	};

	public TreeNodeWrapper(JMeterTreeNode tn, String label) {
		this.tn = tn;
		this.label = label;
	}

	public JMeterTreeNode getTreeNode() {
		return tn;
	}

	public String toString() {
		return label;
	}
}
