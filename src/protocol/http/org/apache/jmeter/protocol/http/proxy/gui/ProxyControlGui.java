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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.BindException;
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
import org.apache.jmeter.control.gui.TreeNodeWrapper;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.exec.KeyToolUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ProxyControlGui extends LogicControllerGui implements JMeterGUIComponent, ActionListener, ItemListener,
        KeyListener, UnsharedComponent {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 232L;

    private static final String NEW_LINE = "\n";  // $NON-NLS-1$

    private static final String SPACE = " ";  // $NON-NLS-1$

    /**
     * This choice means don't explicitly set Implementation and rely on default, see Bug 54154
     */
    private static final String USE_DEFAULT_HTTP_IMPL = ""; // $NON-NLS-1$

    private static final String SUGGESTED_EXCLUSIONS =
            JMeterUtils.getPropDefault("proxy.excludes.suggested", ".*\\.(bmp|css|js|gif|ico|jpe?g|png|swf|woff)"); // $NON-NLS-1$

    private JTextField portField;

    private JLabeledTextField sslDomains;

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

    //+ action names
    private static final String STOP = "stop"; // $NON-NLS-1$

    private static final String START = "start"; // $NON-NLS-1$

    private static final String RESTART = "restart"; // $NON-NLS-1$

    // This is applied to fields that should cause a restart when changed
    private static final String ENABLE_RESTART = "enable_restart"; // $NON-NLS-1$

    private static final String ADD_INCLUDE = "add_include"; // $NON-NLS-1$

    private static final String ADD_EXCLUDE = "add_exclude"; // $NON-NLS-1$

    private static final String DELETE_INCLUDE = "delete_include"; // $NON-NLS-1$

    private static final String DELETE_EXCLUDE = "delete_exclude"; // $NON-NLS-1$

    private static final String ADD_TO_INCLUDE_FROM_CLIPBOARD = "include_clipboard"; // $NON-NLS-1$

    private static final String ADD_TO_EXCLUDE_FROM_CLIPBOARD = "exclude_clipboard"; // $NON-NLS-1$

    private static final String ADD_SUGGESTED_EXCLUDES = "exclude_suggested";
    //- action names

    // Resource names for column headers
    private static final String INCLUDE_COL = "patterns_to_include"; // $NON-NLS-1$

    private static final String EXCLUDE_COL = "patterns_to_exclude"; // $NON-NLS-1$

    // Used by itemListener
    private static final String PORTFIELD = "portField"; // $NON-NLS-1$

    public ProxyControlGui() {
        super();
        log.debug("Creating ProxyControlGui");
        init();
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(excludeTable);
        GuiUtils.stopTableEditing(includeTable);
        configureTestElement(el);
        if (el instanceof ProxyControl) {
            model = (ProxyControl) el;
            model.setPort(portField.getText());
            model.setSslDomains(sslDomains.getText());
            setIncludeListInProxyControl(model);
            setExcludeListInProxyControl(model);
            model.setCaptureHttpHeaders(httpHeaders.isSelected());
            model.setGroupingMode(groupingMode.getSelectedIndex());
            model.setAssertions(addAssertions.isSelected());
            if(samplerTypeName.getSelectedIndex()< HTTPSamplerFactory.getImplementations().length) {
                model.setSamplerTypeName(HTTPSamplerFactory.getImplementations()[samplerTypeName.getSelectedIndex()]);
            } else {
                model.setSamplerTypeName(USE_DEFAULT_HTTP_IMPL);
            }
            model.setSamplerRedirectAutomatically(samplerRedirectAutomatically.isSelected());
            model.setSamplerFollowRedirects(samplerFollowRedirects.isSelected());
            model.setUseKeepAlive(useKeepAlive.isSelected());
            model.setSamplerDownloadImages(samplerDownloadImages.isSelected());
            model.setRegexMatch(regexMatch.isSelected());
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
        List<String> includeList = getDataList(includeModel, INCLUDE_COL);
        element.setIncludeList(includeList);
    }

    protected void setExcludeListInProxyControl(ProxyControl element) {
        List<String> excludeList = getDataList(excludeModel, EXCLUDE_COL);
        element.setExcludeList(excludeList);
    }

    private List<String> getDataList(PowerTableModel p_model, String colName) {
        String[] dataArray = p_model.getData().getColumn(colName);
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < dataArray.length; i++) {
            list.add(dataArray[i]);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public String getLabelResource() {
        return "proxy_title"; // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(new String[] { MenuFactory.NON_TEST_ELEMENTS });
    }

    /** {@inheritDoc} */
    @Override
    public void configure(TestElement element) {
        log.debug("Configuring gui with " + element);
        super.configure(element);
        model = (ProxyControl) element;
        portField.setText(model.getPortString());
        sslDomains.setText(model.getSslDomains());
        httpHeaders.setSelected(model.getCaptureHttpHeaders());
        groupingMode.setSelectedIndex(model.getGroupingMode());
        addAssertions.setSelected(model.getAssertions());
        samplerTypeName.setSelectedItem(model.getSamplerTypeName());
        samplerRedirectAutomatically.setSelected(model.getSamplerRedirectAutomatically());
        samplerFollowRedirects.setSelected(model.getSamplerFollowRedirects());
        useKeepAlive.setSelected(model.getUseKeepalive());
        samplerDownloadImages.setSelected(model.getSamplerDownloadImages());
        regexMatch.setSelected(model.getRegexMatch());
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
     * Also handles samplerTypeName
     */
    /** {@inheritDoc} */
    @Override
    public void itemStateChanged(ItemEvent e) {
        // System.err.println(e.paramString());
        enableRestart();
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent action) {
        String command = action.getActionCommand();

        // Prevent both redirect types from being selected
        final Object source = action.getSource();
        if (source.equals(samplerFollowRedirects) && samplerFollowRedirects.isSelected()) {
            samplerRedirectAutomatically.setSelected(false);
        } else if (source.equals(samplerRedirectAutomatically) && samplerRedirectAutomatically.isSelected()) {
            samplerFollowRedirects.setSelected(false);
        }

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
        } else if (command.equals(ENABLE_RESTART)){
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
        } else if (command.equals(ADD_TO_INCLUDE_FROM_CLIPBOARD)) {
            addFromClipboard(includeTable);
            includeModel.fireTableDataChanged();
            enableRestart();
        } else if (command.equals(ADD_TO_EXCLUDE_FROM_CLIPBOARD)) {
            addFromClipboard(excludeTable);
            excludeModel.fireTableDataChanged();
            enableRestart();
        } else if (command.equals(ADD_SUGGESTED_EXCLUDES)) {
            addSuggestedExcludes(excludeTable);
            excludeModel.fireTableDataChanged();
            enableRestart();
        }
    }

    /**
     * Add suggested excludes to exclude table
     * @param table {@link JTable}
     */
    protected void addSuggestedExcludes(JTable table) {
        GuiUtils.stopTableEditing(table);
        int rowCount = table.getRowCount();
        PowerTableModel model = null;
        String[] exclusions = SUGGESTED_EXCLUSIONS.split(";"); // $NON-NLS-1$
        if (exclusions.length>0) {
            model = (PowerTableModel) table.getModel();
            if(model != null) {
                for (String clipboardLine : exclusions) {
                    model.addRow(new Object[] {clipboardLine});
                }
                if (table.getRowCount() > rowCount) {
                    // Highlight (select) the appropriate rows.
                    int rowToSelect = model.getRowCount() - 1;
                    table.setRowSelectionInterval(rowCount, rowToSelect);
                }
            }
        }
    }

    /**
     * Add values from the clipboard to table
     * @param table {@link JTable}
     */
    protected void addFromClipboard(JTable table) {
        GuiUtils.stopTableEditing(table);
        int rowCount = table.getRowCount();
        PowerTableModel model = null;
        try {
            String clipboardContent = GuiUtils.getPastedText();
            if (clipboardContent != null) {
                String[] clipboardLines = clipboardContent.split(NEW_LINE);
                for (String clipboardLine : clipboardLines) {
                    model = (PowerTableModel) table.getModel();
                    model.addRow(new Object[] {clipboardLine});
                }
                if (table.getRowCount() > rowCount) {
                    if(model != null) {
                        // Highlight (select) the appropriate rows.
                        int rowToSelect = model.getRowCount() - 1;
                        table.setRowSelectionInterval(rowCount, rowToSelect);
                    }
                }
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                    JMeterUtils.getResString("proxy_daemon_error_read_args") // $NON-NLS-1$
                    + "\n" + ioe.getLocalizedMessage(), JMeterUtils.getResString("error_title"),  // $NON-NLS-1$  $NON-NLS-2$
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedFlavorException ufe) {
            JOptionPane.showMessageDialog(this,
                    JMeterUtils.getResString("proxy_daemon_error_not_retrieve") + SPACE // $NON-NLS-1$
                        + DataFlavor.stringFlavor.getHumanPresentableName() + SPACE
                        + JMeterUtils.getResString("proxy_daemon_error_from_clipboard") // $NON-NLS-1$
                        + ufe.getLocalizedMessage(), JMeterUtils.getResString("error_title"),  // $NON-NLS-1$
                        JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startProxy() {
        ValueReplacer replacer = GuiPackage.getInstance().getReplacer();
        modifyTestElement(model);
        // Proxy can take some while to start up; show a wating cursor
        Cursor cursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // TODO somehow show progress
        try {
            replacer.replaceValues(model);
            model.startProxy();
            start.setEnabled(false);
            stop.setEnabled(true);
            restart.setEnabled(false);
            if (ProxyControl.isDynamicMode()) {
                String details[] = model.getCertificateDetails();
                StringBuilder sb = new StringBuilder();
                sb.append(JMeterUtils.getResString("proxy_daemon_msg_rootca_cert"))  // $NON-NLS-1$
                        .append(SPACE).append(KeyToolUtils.ROOT_CACERT_CRT_PFX)
                        .append(SPACE).append(JMeterUtils.getResString("proxy_daemon_msg_created_in_bin"));
                sb.append(NEW_LINE).append(JMeterUtils.getResString("proxy_daemon_msg_install_as_in_doc")); // $NON-NLS-1$
                sb.append(NEW_LINE).append(JMeterUtils.getResString("proxy_daemon_msg_check_details")) // $NON-NLS-1$
                    .append(NEW_LINE).append(NEW_LINE);
                for(String detail : details) {
                    sb.append(detail).append(NEW_LINE);
                }
                JOptionPane.showMessageDialog(this,
                    sb.toString(),
                    JMeterUtils.getResString("proxy_daemon_msg_rootca_cert") + SPACE // $NON-NLS-1$
                    + KeyToolUtils.ROOT_CACERT_CRT_PFX + SPACE
                    + JMeterUtils.getResString("proxy_daemon_msg_created_in_bin"), // $NON-NLS-1$
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (InvalidVariableException e) {
            JOptionPane.showMessageDialog(this,
                    JMeterUtils.getResString("invalid_variables")+": "+e.getMessage(), // $NON-NLS-1$ $NON-NLS-2$
                    JMeterUtils.getResString("error_title"), // $NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
        } catch (BindException e) {
            JOptionPane.showMessageDialog(this,
                    JMeterUtils.getResString("proxy_daemon_bind_error")+": "+e.getMessage(), // $NON-NLS-1$ $NON-NLS-2$
                    JMeterUtils.getResString("error_title"), // $NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    JMeterUtils.getResString("proxy_daemon_error")+": "+e.getMessage(), // $NON-NLS-1$ $NON-NLS-2$
                    JMeterUtils.getResString("error_title"), // $NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(cursor);
        }
    }

    private void enableRestart() {
        if (stop.isEnabled()) {
            // System.err.println("Enable Restart");
            restart.setEnabled(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void keyPressed(KeyEvent e) {
    }

    /** {@inheritDoc} */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /** {@inheritDoc} */
    @Override
    public void keyReleased(KeyEvent e) {
        String fieldName = e.getComponent().getName();

        if (fieldName.equals(PORTFIELD)) {
            try {
                Integer.parseInt(portField.getText());
            } catch (NumberFormatException nfe) {
                int length = portField.getText().length();
                if (length > 0) {
                    JOptionPane.showMessageDialog(this, 
                            JMeterUtils.getResString("proxy_settings_port_error_digits"), // $NON-NLS-1$
                            JMeterUtils.getResString("proxy_settings_port_error_invalid_data"), // $NON-NLS-1$
                            JOptionPane.WARNING_MESSAGE);
                    // Drop the last character:
                    portField.setText(portField.getText().substring(0, length-1));
                }
            }
            enableRestart();
        } else if (fieldName.equals(ENABLE_RESTART)){
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
        portField = new JTextField(ProxyControl.DEFAULT_PORT_S, 5);
        portField.setName(PORTFIELD);
        portField.addKeyListener(this);

        JLabel label = new JLabel(JMeterUtils.getResString("port")); // $NON-NLS-1$
        label.setLabelFor(portField);

        JPanel gPane = new JPanel(new BorderLayout());
        gPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("proxy_general_settings"))); // $NON-NLS-1$

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(label);
        panel.add(portField);
        panel.add(Box.createHorizontalStrut(10));

        gPane.add(panel, BorderLayout.WEST);

        sslDomains = new JLabeledTextField(JMeterUtils.getResString("proxy_domains")); // $NON-NLS-1$
        sslDomains.setEnabled(ProxyControl.isDynamicMode());
        if (ProxyControl.isDynamicMode()) {
            sslDomains.setToolTipText(JMeterUtils.getResString("proxy_domains_dynamic_mode_tooltip"));
        } else {
            sslDomains.setToolTipText(JMeterUtils.getResString("proxy_domains_dynamic_mode_tooltip_java6"));
        }
        gPane.add(sslDomains, BorderLayout.CENTER);
        return gPane;
    }

    private JPanel createTestPlanContentPanel() {
        httpHeaders = new JCheckBox(JMeterUtils.getResString("proxy_headers")); // $NON-NLS-1$
        httpHeaders.setSelected(true); // maintain original default
        httpHeaders.addActionListener(this);
        httpHeaders.setActionCommand(ENABLE_RESTART);

        addAssertions = new JCheckBox(JMeterUtils.getResString("proxy_assertions")); // $NON-NLS-1$
        addAssertions.setSelected(false);
        addAssertions.addActionListener(this);
        addAssertions.setActionCommand(ENABLE_RESTART);

        regexMatch = new JCheckBox(JMeterUtils.getResString("proxy_regex")); // $NON-NLS-1$
        regexMatch.setSelected(false);
        regexMatch.addActionListener(this);
        regexMatch.setActionCommand(ENABLE_RESTART);

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("proxy_test_plan_content"))); // $NON-NLS-1$

        HorizontalPanel nodeCreationPanel = new HorizontalPanel();
        nodeCreationPanel.add(createGroupingPanel());
        nodeCreationPanel.add(httpHeaders);
        nodeCreationPanel.add(addAssertions);
        nodeCreationPanel.add(regexMatch);

        HorizontalPanel targetPanel = new HorizontalPanel();
        targetPanel.add(createTargetPanel());
        mainPanel.add(targetPanel);
        mainPanel.add(nodeCreationPanel);

        return mainPanel;
    }

    private JPanel createHTTPSamplerPanel() {
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        for (String s : HTTPSamplerFactory.getImplementations()){
            m.addElement(s);
        }
        m.addElement(USE_DEFAULT_HTTP_IMPL);
        samplerTypeName = new JComboBox(m);
        samplerTypeName.setPreferredSize(new Dimension(150, 20));
        samplerTypeName.setSelectedItem(USE_DEFAULT_HTTP_IMPL);
        samplerTypeName.addItemListener(this);
        JLabel label2 = new JLabel(JMeterUtils.getResString("proxy_sampler_type")); // $NON-NLS-1$
        label2.setLabelFor(samplerTypeName);

        samplerRedirectAutomatically = new JCheckBox(JMeterUtils.getResString("follow_redirects_auto")); // $NON-NLS-1$
        samplerRedirectAutomatically.setSelected(false);
        samplerRedirectAutomatically.addActionListener(this);
        samplerRedirectAutomatically.setActionCommand(ENABLE_RESTART);

        samplerFollowRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects")); // $NON-NLS-1$
        samplerFollowRedirects.setSelected(true);
        samplerFollowRedirects.addActionListener(this);
        samplerFollowRedirects.setActionCommand(ENABLE_RESTART);

        useKeepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive")); // $NON-NLS-1$
        useKeepAlive.setSelected(true);
        useKeepAlive.addActionListener(this);
        useKeepAlive.setActionCommand(ENABLE_RESTART);

        samplerDownloadImages = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
        samplerDownloadImages.setSelected(false);
        samplerDownloadImages.addActionListener(this);
        samplerDownloadImages.setActionCommand(ENABLE_RESTART);

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
        m.addElement(JMeterUtils.getResString("grouping_in_transaction_controllers")); // $NON-NLS-1$
        groupingMode = new JComboBox(m);
        groupingMode.setPreferredSize(new Dimension(150, 20));
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
        contentTypeInclude = new JTextField(35);
        contentTypeInclude.addKeyListener(this);
        contentTypeInclude.setName(ENABLE_RESTART);
        JLabel labelInclude = new JLabel(JMeterUtils.getResString("proxy_content_type_include")); // $NON-NLS-1$
        labelInclude.setLabelFor(contentTypeInclude);
        // Default value
        contentTypeInclude.setText(JMeterUtils.getProperty("proxy.content_type_include")); // $NON-NLS-1$

        contentTypeExclude = new JTextField(35);
        contentTypeExclude.addKeyListener(this);
        contentTypeExclude.setName(ENABLE_RESTART);
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
        includeTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        includeTable.setPreferredScrollableViewportSize(new Dimension(100, 30));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("patterns_to_include"))); // $NON-NLS-1$

        panel.add(new JScrollPane(includeTable), BorderLayout.CENTER);
        panel.add(createTableButtonPanel(ADD_INCLUDE, DELETE_INCLUDE, ADD_TO_INCLUDE_FROM_CLIPBOARD, null), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createExcludePanel() {
        excludeModel = new PowerTableModel(new String[] { EXCLUDE_COL }, new Class[] { String.class });
        excludeTable = new JTable(excludeModel);
        excludeTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        excludeTable.setPreferredScrollableViewportSize(new Dimension(100, 30));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("patterns_to_exclude"))); // $NON-NLS-1$

        panel.add(new JScrollPane(excludeTable), BorderLayout.CENTER);
        panel.add(createTableButtonPanel(ADD_EXCLUDE, DELETE_EXCLUDE, ADD_TO_EXCLUDE_FROM_CLIPBOARD, ADD_SUGGESTED_EXCLUDES), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTableButtonPanel(String addCommand, String deleteCommand, String copyFromClipboard, String addSuggestedExcludes) {
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addButton.setActionCommand(addCommand);
        addButton.addActionListener(this);
        buttonPanel.add(addButton);

        JButton deleteButton = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        deleteButton.setActionCommand(deleteCommand);
        deleteButton.addActionListener(this);
        buttonPanel.add(deleteButton);

        /** A button for adding new excludes/includes to the table from the clipboard. */
        JButton addFromClipboard = new JButton(JMeterUtils.getResString("add_from_clipboard")); // $NON-NLS-1$
        addFromClipboard.setActionCommand(copyFromClipboard);
        addFromClipboard.addActionListener(this);
        buttonPanel.add(addFromClipboard);

        if(addSuggestedExcludes != null) {
            /** A button for adding suggested excludes. */
            JButton addFromSuggestedExcludes = new JButton(JMeterUtils.getResString("add_from_suggested_excludes")); // $NON-NLS-1$
            addFromSuggestedExcludes.setActionCommand(addSuggestedExcludes);
            addFromSuggestedExcludes.addActionListener(this);
            buttonPanel.add(addFromSuggestedExcludes);
        }
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
        String separator = " > ";
        if (node != null) {
            for (int i = 0; i < node.getChildCount(); i++) {
                StringBuilder name = new StringBuilder();
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
                    name.append(parent_name);
                    name.append(cur.getName());
                    TreeNodeWrapper tnw = new TreeNodeWrapper(cur, name.toString());
                    targetNodesModel.addElement(tnw);
                    name.append(separator);
                    buildNodesModel(cur, name.toString(), level + 1);
                } else if (te instanceof TestPlan || te instanceof WorkBench) {
                    name.append(cur.getName());
                    name.append(separator);
                    buildNodesModel(cur, name.toString(), 0);
                }
                // Ignore everything else
            }
        }
    }
}