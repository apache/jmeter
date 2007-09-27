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

package org.apache.jmeter.protocol.http.proxy.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ProxyControlGui extends LogicControllerGui implements JMeterGUIComponent, ActionListener, ItemListener,
		KeyListener, UnsharedComponent {
	private static transient Logger log = LoggingManager.getLoggerForClass();

	private static final long serialVersionUID = 1L;

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

	/**
	 * The list of sampler type names to choose from
	 */
	private JComboBox samplerTypeName;

	/**
	 * Set/clear the Redirect automatically box on the samplers (default is false)
	 */
	private JCheckBox samplerRedirectAutomatically;

	/**
	 * Set/clear the Follow-redirects box on the samplers (default is true)
	 */
	private JCheckBox samplerFollowRedirects;

	/**
	 * Set/clear the Download images box on the samplers (default is false)
	 */
	private JCheckBox samplerDownloadImages;

	/*
	 * Spoof the client into thinking that it is communicating with http
	 * even if it is really https.
	 */
	private JCheckBox httpsSpoof;

	/**
	 * Regular expression to include results based on content type
	 */
	private JTextField contentTypeInclude;

	/**
	 * Regular expression to exclude results based on content type
	 */
	private JTextField contentTypeExclude;
	
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

	private static final String CHANGE_TARGET = "change_target"; // $NON-NLS-1$

	private JButton stop, start, restart;

	private static final String STOP = "stop"; // $NON-NLS-1$

	private static final String START = "start"; // $NON-NLS-1$

	private static final String RESTART = "restart"; // $NON-NLS-1$

	private static final String ADD_INCLUDE = "add_include"; // $NON-NLS-1$

	private static final String ADD_EXCLUDE = "add_exclude"; // $NON-NLS-1$

	private static final String DELETE_INCLUDE = "delete_include"; // $NON-NLS-1$

	private static final String DELETE_EXCLUDE = "delete_exclude"; // $NON-NLS-1$

	private static final String INCLUDE_COL = JMeterUtils.getResString("patterns_to_include"); // $NON-NLS-1$

	private static final String EXCLUDE_COL = JMeterUtils.getResString("patterns_to_exclude"); // $NON-NLS-1$

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
		if (excludeTable.isEditing()) {// Bug 42948
			excludeTable.getCellEditor().stopCellEditing();
		}
		if (includeTable.isEditing()) {// Bug 42948
			includeTable.getCellEditor().stopCellEditing();
		}
		configureTestElement(el);
		if (el instanceof ProxyControl) {
			model = (ProxyControl) el;
			model.setPort(portField.getText());
			setIncludeListInProxyControl(model);
			setExcludeListInProxyControl(model);
			model.setCaptureHttpHeaders(httpHeaders.isSelected());
			model.setGroupingMode(groupingMode.getSelectedIndex());
			model.setAssertions(addAssertions.isSelected());
			model.setSamplerTypeName(samplerTypeName.getSelectedIndex());
			model.setSamplerRedirectAutomatically(samplerRedirectAutomatically.isSelected());
			model.setSamplerFollowRedirects(samplerFollowRedirects.isSelected());
			model.setUseKeepAlive(useKeepAlive.isSelected());
			model.setSamplerDownloadImages(samplerDownloadImages.isSelected());
			model.setRegexMatch(regexMatch.isSelected());
			model.setHttpsSpoof(httpsSpoof.isSelected());
			model.setContentTypeInclude(contentTypeInclude.getText());
			model.setContentTypeExclude(contentTypeExclude.getText());
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
		return "proxy_title"; // $NON-NLS-1$
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
		samplerTypeName.setSelectedIndex(model.getSamplerTypeName());
		samplerRedirectAutomatically.setSelected(model.getSamplerRedirectAutomatically());
		samplerFollowRedirects.setSelected(model.getSamplerFollowRedirects());
		useKeepAlive.setSelected(model.getUseKeepalive());
		samplerDownloadImages.setSelected(model.getSamplerDownloadImages());
		regexMatch.setSelected(model.getRegexMatch());
		httpsSpoof.setSelected(model.getHttpsSpoof());
		contentTypeInclude.setText(model.getContentTypeInclude());
		contentTypeExclude.setText(model.getContentTypeExclude());

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
		} else if (command.equals(ProxyControl.CAPTURE_HTTP_HEADERS)
				|| command.equals(ProxyControl.ADD_ASSERTIONS)
				|| command.equals(ProxyControl.SAMPLER_REDIRECT_AUTOMATICALLY)  
				|| command.equals(ProxyControl.SAMPLER_FOLLOW_REDIRECTS) 
				|| command.equals(ProxyControl.USE_KEEPALIVE)
				|| command.equals(ProxyControl.SAMPLER_DOWNLOAD_IMAGES) 
				|| command.equals(ProxyControl.REGEX_MATCH)
				|| command.equals(ProxyControl.HTTPS_SPOOF)
				|| command.equals(ProxyControl.CONTENT_TYPE_INCLUDE)
				|| command.equals(ProxyControl.CONTENT_TYPE_EXCLUDE)) {
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
			JOptionPane.showMessageDialog(this, 
					JMeterUtils.getResString("invalid_variables"), // $NON-NLS-1$
					"Error",
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
				int length = portField.getText().length();
				if (length > 0) {
					JOptionPane.showMessageDialog(this, "Only digits allowed", "Invalid data",
							JOptionPane.WARNING_MESSAGE);
					// Drop the last character:
					portField.setText(portField.getText().substring(0, length-1));
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
		myBox.add(createTestPlanContentPanel());
		myBox.add(Box.createVerticalStrut(5));
		myBox.add(createHTTPSamplerPanel());
		myBox.add(Box.createVerticalStrut(5));
		myBox.add(createContentTypePanel());
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
		start = new JButton(JMeterUtils.getResString("start")); // $NON-NLS-1$
		start.addActionListener(this);
		start.setActionCommand(START);
		start.setEnabled(true);

		stop = new JButton(JMeterUtils.getResString("stop")); // $NON-NLS-1$
		stop.addActionListener(this);
		stop.setActionCommand(STOP);
		stop.setEnabled(false);

		restart = new JButton(JMeterUtils.getResString("restart")); // $NON-NLS-1$
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

		JLabel label = new JLabel(JMeterUtils.getResString("port")); // $NON-NLS-1$
		label.setLabelFor(portField);

		httpsSpoof = new JCheckBox(JMeterUtils.getResString("proxy_httpsspoofing")); // $NON-NLS-1$
		httpsSpoof.setName(ProxyControl.HTTPS_SPOOF);
		httpsSpoof.setSelected(false);
		httpsSpoof.addActionListener(this);
		httpsSpoof.setActionCommand(ProxyControl.HTTPS_SPOOF);		
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(label);
		panel.add(portField);

		panel.add(Box.createHorizontalStrut(10));
		panel.add(httpsSpoof);

		return panel;
	}

	private JPanel createTestPlanContentPanel() {
		httpHeaders = new JCheckBox(JMeterUtils.getResString("proxy_headers")); // $NON-NLS-1$
		httpHeaders.setName(ProxyControl.CAPTURE_HTTP_HEADERS);
		httpHeaders.setSelected(true); // maintain original default
		httpHeaders.addActionListener(this);
		httpHeaders.setActionCommand(ProxyControl.CAPTURE_HTTP_HEADERS);

		addAssertions = new JCheckBox(JMeterUtils.getResString("proxy_assertions")); // $NON-NLS-1$
		addAssertions.setName(ProxyControl.ADD_ASSERTIONS);
		addAssertions.setSelected(false);
		addAssertions.addActionListener(this);
		addAssertions.setActionCommand(ProxyControl.ADD_ASSERTIONS);

		regexMatch = new JCheckBox(JMeterUtils.getResString("proxy_regex")); // $NON-NLS-1$
		regexMatch.setName(ProxyControl.REGEX_MATCH);
		regexMatch.setSelected(false);
		regexMatch.addActionListener(this);
		regexMatch.setActionCommand(ProxyControl.REGEX_MATCH);

		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				JMeterUtils.getResString("proxy_test_plan_content"))); // $NON-NLS-1$

		HorizontalPanel nodeCreationPanel = new HorizontalPanel();
		nodeCreationPanel.add(httpHeaders);
		nodeCreationPanel.add(addAssertions);
		nodeCreationPanel.add(regexMatch);
		
		mainPanel.add(createTargetPanel());
		mainPanel.add(createGroupingPanel());
		mainPanel.add(nodeCreationPanel);

		return mainPanel;
	}

	private JPanel createHTTPSamplerPanel() {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		// Note: position of these elements in the menu *must* match the
		// corresponding ProxyControl.SAMPLER_TYPE_* values.
		m.addElement(JMeterUtils.getResString("web_testing_title")); // $NON-NLS-1$
		m.addElement(JMeterUtils.getResString("web_testing2_title")); // $NON-NLS-1$
		samplerTypeName = new JComboBox(m);
		samplerTypeName.setName(ProxyControl.SAMPLER_TYPE_NAME);
		samplerTypeName.setSelectedIndex(0);
		samplerTypeName.addItemListener(this);
		JLabel label2 = new JLabel(JMeterUtils.getResString("proxy_sampler_type")); // $NON-NLS-1$
		label2.setLabelFor(samplerTypeName);

		samplerRedirectAutomatically = new JCheckBox(JMeterUtils.getResString("follow_redirects_auto")); // $NON-NLS-1$
		samplerRedirectAutomatically.setName(ProxyControl.SAMPLER_REDIRECT_AUTOMATICALLY);
		samplerRedirectAutomatically.setSelected(false);
		samplerRedirectAutomatically.addActionListener(this);
		samplerRedirectAutomatically.setActionCommand(ProxyControl.SAMPLER_REDIRECT_AUTOMATICALLY);
		
		samplerFollowRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects")); // $NON-NLS-1$
		samplerFollowRedirects.setName(ProxyControl.SAMPLER_FOLLOW_REDIRECTS);
		samplerFollowRedirects.setSelected(true);
		samplerFollowRedirects.addActionListener(this);
		samplerFollowRedirects.setActionCommand(ProxyControl.SAMPLER_FOLLOW_REDIRECTS);
		
		useKeepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive")); // $NON-NLS-1$
		useKeepAlive.setName(ProxyControl.USE_KEEPALIVE);
		useKeepAlive.setSelected(true);
		useKeepAlive.addActionListener(this);
		useKeepAlive.setActionCommand(ProxyControl.USE_KEEPALIVE);

		samplerDownloadImages = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
		samplerDownloadImages.setName(ProxyControl.SAMPLER_DOWNLOAD_IMAGES);
		samplerDownloadImages.setSelected(false);
		samplerDownloadImages.addActionListener(this);
		samplerDownloadImages.setActionCommand(ProxyControl.SAMPLER_DOWNLOAD_IMAGES);
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				JMeterUtils.getResString("proxy_sampler_settings"))); // $NON-NLS-1$
		panel.add(label2);
		panel.add(samplerTypeName);
		panel.add(samplerRedirectAutomatically);
		panel.add(samplerFollowRedirects);
		panel.add(useKeepAlive);
		panel.add(samplerDownloadImages);

		return panel;
	}
	
	private JPanel createTargetPanel() {
		targetNodesModel = new DefaultComboBoxModel();
		targetNodes = new JComboBox(targetNodesModel);
		targetNodes.setActionCommand(CHANGE_TARGET);
		// Action listener will be added later

		JLabel label = new JLabel(JMeterUtils.getResString("proxy_target")); // $NON-NLS-1$
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
		m.addElement(JMeterUtils.getResString("grouping_no_groups")); // $NON-NLS-1$
		m.addElement(JMeterUtils.getResString("grouping_add_separators")); // $NON-NLS-1$
		m.addElement(JMeterUtils.getResString("grouping_in_controllers")); // $NON-NLS-1$
		m.addElement(JMeterUtils.getResString("grouping_store_first_only")); // $NON-NLS-1$
		groupingMode = new JComboBox(m);
		groupingMode.setName(ProxyControl.GROUPING_MODE);
		groupingMode.setSelectedIndex(0);
		groupingMode.addItemListener(this);

		JLabel label2 = new JLabel(JMeterUtils.getResString("grouping_mode")); // $NON-NLS-1$
		label2.setLabelFor(groupingMode);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(label2);
		panel.add(groupingMode);

		return panel;
	}
	
	private JPanel createContentTypePanel() {
		contentTypeInclude = new JTextField(30);
		contentTypeInclude.setName(ProxyControl.CONTENT_TYPE_INCLUDE);
		contentTypeInclude.addActionListener(this);
		contentTypeInclude.setActionCommand(ProxyControl.CONTENT_TYPE_INCLUDE);
		JLabel labelInclude = new JLabel(JMeterUtils.getResString("proxy_content_type_include")); // $NON-NLS-1$
		labelInclude.setLabelFor(contentTypeInclude);
		// Default value
		contentTypeInclude.setText(JMeterUtils.getProperty("proxy.content_type_include")); // $NON-NLS-1$

		contentTypeExclude = new JTextField(30);
		contentTypeExclude.setName(ProxyControl.CONTENT_TYPE_EXCLUDE);
		contentTypeExclude.addActionListener(this);
		contentTypeExclude.setActionCommand(ProxyControl.CONTENT_TYPE_EXCLUDE);
		JLabel labelExclude = new JLabel(JMeterUtils.getResString("proxy_content_type_exclude")); // $NON-NLS-1$
		labelExclude.setLabelFor(contentTypeExclude);
		// Default value
		contentTypeExclude.setText(JMeterUtils.getProperty("proxy.content_type_exclude")); // $NON-NLS-1$
		
		HorizontalPanel panel = new HorizontalPanel();
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				JMeterUtils.getResString("proxy_content_type_filter"))); // $NON-NLS-1$
		panel.add(labelInclude);
		panel.add(contentTypeInclude);
		panel.add(labelExclude);
		panel.add(contentTypeExclude);
		
		return panel;
	}

	private JPanel createIncludePanel() {
		includeModel = new PowerTableModel(new String[] { INCLUDE_COL }, new Class[] { String.class });
		includeTable = new JTable(includeModel);
		includeTable.setPreferredScrollableViewportSize(new Dimension(100, 30));

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("patterns_to_include"))); // $NON-NLS-1$

		panel.add(new JScrollPane(includeTable), BorderLayout.CENTER);
		panel.add(createTableButtonPanel(ADD_INCLUDE, DELETE_INCLUDE), BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createExcludePanel() {
		excludeModel = new PowerTableModel(new String[] { EXCLUDE_COL }, new Class[] { String.class });
		excludeTable = new JTable(excludeModel);
		excludeTable.setPreferredScrollableViewportSize(new Dimension(100, 30));

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("patterns_to_exclude"))); // $NON-NLS-1$

		panel.add(new JScrollPane(excludeTable), BorderLayout.CENTER);
		panel.add(createTableButtonPanel(ADD_EXCLUDE, DELETE_EXCLUDE), BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createTableButtonPanel(String addCommand, String deleteCommand) {
		JPanel buttonPanel = new JPanel();

		JButton addButton = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
		addButton.setActionCommand(addCommand);
		addButton.addActionListener(this);
		buttonPanel.add(addButton);

		JButton deleteButton = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
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
					.addElement(new TreeNodeWrapper(null, JMeterUtils.getResString("use_recording_controller"))); // $NON-NLS-1$
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
			spaces.append(" "); // $NON-NLS-1$
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
