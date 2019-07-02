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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Window;
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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.control.gui.TreeNodeWrapper;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.exec.KeyToolUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI of HTTP(s) Test Script Recorder
 *
 */
public class ProxyControlGui extends LogicControllerGui implements JMeterGUIComponent, ActionListener, ItemListener,
        KeyListener, UnsharedComponent {
    private static final Logger log = LoggerFactory.getLogger(ProxyControlGui.class);

    private static final long serialVersionUID = 232L;

    private static final String NEW_LINE = "\n";  // $NON-NLS-1$

    private static final String SPACE = " ";  // $NON-NLS-1$

    /**
     * This choice means don't explicitly set Implementation and rely on default, see Bug 54154
     */
    private static final String USE_DEFAULT_HTTP_IMPL = ""; // $NON-NLS-1$

    private static final String SUGGESTED_EXCLUSIONS =
            JMeterUtils.getPropDefault("proxy.excludes.suggested", "(?i).*\\.(bmp|css|js|gif|ico|jpe?g|png|swf|woff|woff2)"); // $NON-NLS-1$

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
    private JComboBox<String> groupingMode;

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
    private JComboBox<String> samplerTypeName;

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
     * Default enccoding for parsing
     */
    private JTextField defaultEncoding;

    /**
     * To choose between a prefix or a transaction name
     */
    private JComboBox<String> httpSampleNamingMode;

    /**
     * Add a prefix/transaction name to HTTP sample name recorded
     */
    private JTextField prefixHTTPSampleName;

    /**
     * Delay between HTTP requests
     */
    private JTextField proxyPauseHTTPSample;

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
    private JComboBox<Object> targetNodes;

    /**
     * Notify child Listener of Filtered Samplers
     */
    private JCheckBox notifyChildSamplerListenerOfFilteredSamplersCB;

    private DefaultComboBoxModel<Object> targetNodesModel;

    private ProxyControl model;

    private JTable excludeTable;

    private PowerTableModel excludeModel;

    private JTable includeTable;

    private PowerTableModel includeModel;

    private static final String CHANGE_TARGET = "change_target"; // $NON-NLS-1$

    private JButton stop;
    private JButton start;
    private JButton restart;

    private transient RecorderDialog recorderDialog;

    private Component labelDefaultEncoding;

    //+ action names
    private static final String ACTION_STOP = "stop"; // $NON-NLS-1$

    private static final String ACTION_START = "start"; // $NON-NLS-1$

    private static final String ACTION_RESTART = "restart"; // $NON-NLS-1$

    // This is applied to fields that should cause a restart when changed
    static final String ENABLE_RESTART = "enable_restart"; // $NON-NLS-1$

    private static final String ADD_INCLUDE = "add_include"; // $NON-NLS-1$

    private static final String ADD_EXCLUDE = "add_exclude"; // $NON-NLS-1$

    private static final String DELETE_INCLUDE = "delete_include"; // $NON-NLS-1$

    private static final String DELETE_EXCLUDE = "delete_exclude"; // $NON-NLS-1$

    private static final String ADD_TO_INCLUDE_FROM_CLIPBOARD = "include_clipboard"; // $NON-NLS-1$

    private static final String ADD_TO_EXCLUDE_FROM_CLIPBOARD = "exclude_clipboard"; // $NON-NLS-1$

    private static final String ADD_SUGGESTED_EXCLUDES = "exclude_suggested";

    static final String HTTP_SAMPLER_NAMING_MODE = "proxy_http_sampler_naming_mode"; // $NON-NLS-1$

    static final String PREFIX_HTTP_SAMPLER_NAME = "proxy_prefix_http_sampler_name"; // $NON-NLS-1$

    static final String PROXY_PAUSE_HTTP_SAMPLER = "proxy_pause_http_sampler"; // $NON-NLS-1$
    //- action names

    // Resource names for column headers
    private static final String INCLUDE_COL = "patterns_to_include"; // $NON-NLS-1$

    private static final String EXCLUDE_COL = "patterns_to_exclude"; // $NON-NLS-1$

    // Used by itemListener
    private static final String PORT_FIELD_NAME = "portField"; // $NON-NLS-1$

    public ProxyControlGui() {
        super();
        log.debug("Creating ProxyControlGui");
        init();
        try {
            this.recorderDialog = new RecorderDialog(this);
        } catch (HeadlessException ex) { // NOSONAR Needed for Headless tests
            // Ignore as due to Headless tests
        }
    }

    /** {@inheritDoc} */
    @Override
    public TestElement createTestElement() {
        model = makeProxyControl();
        log.debug("creating/configuring model = {}", model);
        modifyTestElement(model);
        return model;
    }

    protected ProxyControl makeProxyControl() {
        return new ProxyControl();
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
            model.setHTTPSampleNamingMode(httpSampleNamingMode.getSelectedIndex());
            model.setDefaultEncoding(defaultEncoding.getText());
            model.setPrefixHTTPSampleName(prefixHTTPSampleName.getText());
            model.setProxyPauseHTTPSample(proxyPauseHTTPSample.getText());
            model.setNotifyChildSamplerListenerOfFilteredSamplers(notifyChildSamplerListenerOfFilteredSamplersCB.isSelected());
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

    private List<String> getDataList(PowerTableModel pModel, String colName) {
        String[] dataArray = pModel.getData().getColumn(colName);
        List<String> list = new LinkedList<>();
        for (String data : dataArray) {
            list.add(data);
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
        return Arrays.asList(MenuFactory.NON_TEST_ELEMENTS);
    }

    /** {@inheritDoc} */
    @Override
    public void configure(TestElement element) {
        log.debug("Configuring gui with {}", element);
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
        httpSampleNamingMode.setSelectedIndex(model.getHTTPSampleNamingMode());
        prefixHTTPSampleName.setText(model.getPrefixHTTPSampleName());
        defaultEncoding.setText(model.getDefaultEncoding());
        proxyPauseHTTPSample.setText(model.getProxyPauseHTTPSample());
        notifyChildSamplerListenerOfFilteredSamplersCB.setSelected(model.getNotifyChildSamplerListenerOfFilteredSamplers());
        regexMatch.setSelected(model.getRegexMatch());
        contentTypeInclude.setText(model.getContentTypeInclude());
        contentTypeExclude.setText(model.getContentTypeExclude());

        reinitializeTargetCombo();// Set up list of potential targets and
                                    // enable listener

        populateTable(includeModel, model.getIncludePatterns().iterator());
        populateTable(excludeModel, model.getExcludePatterns().iterator());
        repaint();
    }

    private void populateTable(PowerTableModel pModel, PropertyIterator iter) {
        pModel.clearData();
        while (iter.hasNext()) {
            pModel.addRow(new Object[] { iter.next().getStringValue() });
        }
        pModel.fireTableDataChanged();
    }

    /*
     * Handles groupingMode. actionPerfomed is not suitable, as that seems to be
     * activated whenever the Proxy is selected in the Test Plan
     * Also handles samplerTypeName
     */
    /** {@inheritDoc} */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof JComboBox) {
            JComboBox combo = (JComboBox) e.getSource();
            if(HTTP_SAMPLER_NAMING_MODE.equals(combo.getName())){
                model.setHTTPSampleNamingMode(httpSampleNamingMode.getSelectedIndex());
                }
            }
        else {
            enableRestart();
        }
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

        if (command.equals(ACTION_STOP)) {
            stopRecorder();
        } else if (command.equals(ACTION_START)) {
            if(startProxy()) {
                recorderDialog.setVisible(true);
            }
        } else if (command.equals(ACTION_RESTART)) {
            model.stopProxy();
            if(startProxy()) {
                recorderDialog.setVisible(true);
            }
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
            deleteRowFromTable(excludeModel, excludeTable);
        } else if (command.equals(DELETE_INCLUDE)) {
            deleteRowFromTable(includeModel, includeTable);
        } else if (command.equals(CHANGE_TARGET)) {
            log.debug("Change target {} in model {}", targetNodes.getSelectedItem(), model);
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
     *
     */
    void stopRecorder() {
        model.stopProxy();
        stop.setEnabled(false);
        start.setEnabled(true);
        restart.setEnabled(false);
        recorderDialog.setVisible(false);
    }

    /**
     * Delete row from table, select one if possible and enable restart button
     * @param tableModel {@link PowerTableModel}
     * @param table {@link JTable}
     *
     */
    private void deleteRowFromTable(PowerTableModel tableModel, JTable table) {
        int selectedRow = table.getSelectedRow();
        if(selectedRow >= 0) {
            tableModel.removeRow(table.getSelectedRow());
        } else {
            if(table.getRowCount()>0) {
                tableModel.removeRow(0);
            }
        }

        tableModel.fireTableDataChanged();
        if(table.getRowCount()>0) {
            if(selectedRow == -1) {
                table.setRowSelectionInterval(0, 0);
            } else {
                int rowToSelect = selectedRow>0 ? selectedRow-1:0;
                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
        enableRestart();
    }

    /**
     * Add suggested excludes to exclude table
     * @param table {@link JTable}
     */
    protected void addSuggestedExcludes(JTable table) {
        GuiUtils.stopTableEditing(table);
        int rowCount = table.getRowCount();
        String[] exclusions = SUGGESTED_EXCLUSIONS.split(";"); // $NON-NLS-1$
        if (exclusions.length>0) {
            PowerTableModel model = (PowerTableModel) table.getModel();
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
        try {
            String clipboardContent = GuiUtils.getPastedText();
            if (clipboardContent != null) {
                PowerTableModel model = null;
                String[] clipboardLines = clipboardContent.split(NEW_LINE);
                for (String clipboardLine : clipboardLines) {
                    model = (PowerTableModel) table.getModel();
                    model.addRow(new Object[] {clipboardLine});
                }
                if (model != null && table.getRowCount() > rowCount) {
                    // Highlight (select) the appropriate rows.
                    int rowToSelect = model.getRowCount() - 1;
                    table.setRowSelectionInterval(rowCount, rowToSelect);
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

    private boolean startProxy() {
        ValueReplacer replacer = GuiPackage.getInstance().getReplacer();
        modifyTestElement(model);
        TreeNodeWrapper treeNodeWrapper = (TreeNodeWrapper)targetNodesModel.getSelectedItem();
        if (JMeterUtils.getResString("use_recording_controller").equals(treeNodeWrapper.getLabel())) {
            JMeterTreeNode targetNode = model.findTargetControllerNode();
            if(targetNode == null || !(targetNode.getTestElement() instanceof RecordingController)) {
                JOptionPane.showMessageDialog(this,
                        JMeterUtils.getResString("proxy_cl_wrong_target_cl"), // $NON-NLS-1$
                        JMeterUtils.getResString("error_title"), // $NON-NLS-1$
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        // Proxy can take some while to start up; show a waiting cursor
        Cursor cursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            replacer.replaceValues(model);
            model.startProxy();
            start.setEnabled(false);
            stop.setEnabled(true);
            restart.setEnabled(false);
            if (ProxyControl.isDynamicMode()) {
                String[] details = model.getCertificateDetails();
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append(JMeterUtils.getResString("proxy_daemon_msg_rootca_cert"))  // $NON-NLS-1$
                        .append("&nbsp;<b>").append(KeyToolUtils.ROOT_CACERT_CRT_PFX)
                        .append("</b>&nbsp;").append(JMeterUtils.getResString("proxy_daemon_msg_created_in_bin"));
                sb.append("<br>").append(JMeterUtils.getResString("proxy_daemon_msg_install_as_in_doc")); // $NON-NLS-1$
                sb.append("<br><b>").append(MessageFormat.format(
                        JMeterUtils.getResString("proxy_daemon_msg_check_expiration"),
                        ProxyControl.CERT_VALIDITY)) // $NON-NLS-1$
                    .append("</b><br>");
                sb.append("<br>").append(JMeterUtils.getResString("proxy_daemon_msg_check_details"))
                    .append("<ul>"); // $NON-NLS-1$
                for(String detail : details) {
                    sb.append("<li>").append(detail).append("</li>");
                }
                sb.append("</ul>").append("</html>");

                // Make dialog disappear after 7 seconds
                JLabel messageLabel = new JLabel(sb.toString());
                Timer timer = new Timer(7000, evt -> {
                    Window window = SwingUtilities.getWindowAncestor(messageLabel);
                    // Window may be closed by user
                    if(window != null) {
                        window.dispose();
                    }
                });
                timer.setRepeats(false);
                timer.start();
                JOptionPane.showMessageDialog(this,
                        messageLabel,
                    JMeterUtils.getResString("proxy_daemon_msg_rootca_cert") + SPACE // $NON-NLS-1$
                    + KeyToolUtils.ROOT_CACERT_CRT_PFX + SPACE
                    + JMeterUtils.getResString("proxy_daemon_msg_created_in_bin"), // $NON-NLS-1$
                    JOptionPane.INFORMATION_MESSAGE);
            }
            return true;
        } catch (InvalidVariableException e) {
            JOptionPane.showMessageDialog(this,
                    JMeterUtils.getResString("invalid_variables")+": "+e.getMessage(), // $NON-NLS-1$ $NON-NLS-2$
                    JMeterUtils.getResString("error_title"), // $NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (BindException e) {
            JOptionPane.showMessageDialog(this,
                    JMeterUtils.getResString("proxy_daemon_bind_error")+": "+e.getMessage(), // $NON-NLS-1$ $NON-NLS-2$
                    JMeterUtils.getResString("error_title"), // $NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    JMeterUtils.getResString("proxy_daemon_error")+": "+e.getMessage(), // $NON-NLS-1$ $NON-NLS-2$
                    JMeterUtils.getResString("error_title"), // $NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            setCursor(cursor);
        }
    }

    void enableRestart() {
        if (stop.isEnabled()) {
            restart.setEnabled(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void keyPressed(KeyEvent e) {
        // NOOP
    }

    /** {@inheritDoc} */
    @Override
    public void keyTyped(KeyEvent e) {
        // NOOP
    }

    /** {@inheritDoc} */
    @Override
    public void keyReleased(KeyEvent e) {
        String fieldName = e.getComponent().getName();

        if (fieldName.equals(PORT_FIELD_NAME)) {
            try {
                int port = Integer.parseInt(portField.getText());
                log.debug("Using port {} for recording", port);
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
        } else if(fieldName.equals(PREFIX_HTTP_SAMPLER_NAME)) {
            model.setPrefixHTTPSampleName(prefixHTTPSampleName.getText());
        } else if(fieldName.equals(PROXY_PAUSE_HTTP_SAMPLER)) {
            try {
                Long.parseLong(proxyPauseHTTPSample.getText());
            } catch (NumberFormatException nfe) {
                int length = proxyPauseHTTPSample.getText().length();
                if (length > 0) {
                    JOptionPane.showMessageDialog(this, JMeterUtils.getResString("proxy_settings_pause_error_digits"), // $NON-NLS-1$
                            JMeterUtils.getResString("proxy_settings_pause_error_invalid_data"), // $NON-NLS-1$
                            JOptionPane.WARNING_MESSAGE);
                    // Drop the last character:
                    proxyPauseHTTPSample.setText(proxyPauseHTTPSample.getText().substring(0, length - 1));
                }
            }
            model.setProxyPauseHTTPSample(proxyPauseHTTPSample.getText());
            enableRestart();
        }
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createControls(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel testPlanPanel = new VerticalPanel();
        testPlanPanel.add(createTestPlanContentPanel());
        testPlanPanel.add(Box.createVerticalStrut(5));
        testPlanPanel.add(createHTTPSamplerPanel());
        tabbedPane.add(JMeterUtils
                .getResString("proxy_test_plan_creation"), testPlanPanel);

        JPanel filteringPanel = new VerticalPanel();
        tabbedPane.add(JMeterUtils
                .getResString("proxy_test_plan_filtering"), filteringPanel);

        filteringPanel.add(createContentTypePanel());
        filteringPanel.add(createIncludePanel());
        filteringPanel.add(createExcludePanel());
        filteringPanel.add(createNotifyListenersPanel());

        JPanel vPanel = new VerticalPanel();
        vPanel.add(createPortPanel());
        vPanel.add(Box.createVerticalStrut(5));
        vPanel.add(tabbedPane);
        mainPanel.add(vPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createControls() {

        String iconSize = JMeterUtils.getPropDefault(JMeterToolBar.TOOLBAR_ICON_SIZE, JMeterToolBar.DEFAULT_TOOLBAR_ICON_SIZE);

        start = new JButton(JMeterUtils.getResString("start")); // $NON-NLS-1$
        ImageIcon startImage = JMeterUtils.getImage("toolbar/" + iconSize + "/arrow-right-3.png");
        start.setIcon(startImage);
        start.addActionListener(this);
        start.setActionCommand(ACTION_START);
        start.setEnabled(true);

        stop = createStopButton(iconSize);
        stop.addActionListener(this);

        ImageIcon restartImage = JMeterUtils.getImage("toolbar/" + iconSize + "/edit-redo-7.png");
        restart = new JButton(JMeterUtils.getResString("restart")); // $NON-NLS-1$
        restart.setIcon(restartImage);
        restart.addActionListener(this);
        restart.setActionCommand(ACTION_RESTART);
        restart.setEnabled(false);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("proxy_general_lifecycle"))); // $NON-NLS-1$
        panel.add(start);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(stop);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(restart);
        return panel;
    }

    /**
     * @param iconSize
     */
    JButton createStopButton(String iconSize) {
        JButton stop = new JButton(JMeterUtils.getResString("stop")); // $NON-NLS-1$
        ImageIcon stopImage = JMeterUtils.getImage("toolbar/" + iconSize + "/process-stop-4.png");
        stop.setIcon(stopImage);
        stop.setActionCommand(ACTION_STOP);
        stop.setEnabled(false);
        return stop;
    }

    private JPanel createPortPanel() {
        portField = new JTextField(ProxyControl.DEFAULT_PORT_S, 20);
        portField.setName(PORT_FIELD_NAME);
        portField.addKeyListener(this);
        Dimension portPreferredSize = portField.getPreferredSize();
        portField.setMinimumSize(new Dimension((int) Math.round(portPreferredSize.width*0.75), portPreferredSize.height));

        JLabel label = new JLabel(JMeterUtils.getResString("port")); // $NON-NLS-1$
        label.setLabelFor(portField);

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(label);
        panel.add(portField);

        sslDomains = new JLabeledTextField(JMeterUtils.getResString("proxy_domains")); // $NON-NLS-1$
        sslDomains.setEnabled(ProxyControl.isDynamicMode());
        if (ProxyControl.isDynamicMode()) {
            sslDomains.setToolTipText(JMeterUtils.getResString("proxy_domains_dynamic_mode_tooltip"));
        } else {
            sslDomains.setToolTipText(JMeterUtils.getResString("proxy_domains_dynamic_mode_tooltip_java6"));
        }

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;

        JPanel gPane = new JPanel(gridBagLayout);
        gPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("proxy_general_settings"))); // $NON-NLS-1$
        gPane.add(panel, gbc.clone());
        gbc.gridx++;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.weightx = 6;
        gPane.add(sslDomains, gbc);
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
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        for (String s : HTTPSamplerFactory.getImplementations()){
            m.addElement(s);
        }
        m.addElement(USE_DEFAULT_HTTP_IMPL);
        samplerTypeName = new JComboBox<>(m);
        samplerTypeName.setSelectedItem(USE_DEFAULT_HTTP_IMPL);
        samplerTypeName.addItemListener(this);

        JLabel labelSamplerType = new JLabel(JMeterUtils.getResString("proxy_sampler_type")); // $NON-NLS-1$
        labelSamplerType.setLabelFor(samplerTypeName);

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

        DefaultComboBoxModel<String> choice = new DefaultComboBoxModel<>();
        choice.addElement(JMeterUtils.getResString("sample_name_prefix")); // $NON-NLS-1$
        choice.addElement(JMeterUtils.getResString("sample_name_transaction")); // $NON-NLS-1$
        httpSampleNamingMode = new JComboBox<>(choice);
        httpSampleNamingMode.setName(HTTP_SAMPLER_NAMING_MODE);
        httpSampleNamingMode.addItemListener(this);

        defaultEncoding = new JTextField(15);

        prefixHTTPSampleName = new JTextField(20);
        prefixHTTPSampleName.addKeyListener(this);
        prefixHTTPSampleName.setName(PREFIX_HTTP_SAMPLER_NAME);

        proxyPauseHTTPSample = new JTextField(10);
        proxyPauseHTTPSample.addKeyListener(this);
        proxyPauseHTTPSample.setName(PROXY_PAUSE_HTTP_SAMPLER);
        proxyPauseHTTPSample.setActionCommand(ENABLE_RESTART);
        JLabel labelProxyPause = new JLabel(JMeterUtils.getResString("proxy_pause_http_sampler")); // $NON-NLS-1$
        labelProxyPause.setLabelFor(proxyPauseHTTPSample);

        JLabel labelDefaultEncoding = new JLabel(JMeterUtils.getResString("proxy_default_encoding")); // $NON-NLS-1$
        labelDefaultEncoding.setLabelFor(defaultEncoding);

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        JPanel panel = new JPanel(gridBagLayout);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("proxy_sampler_settings"))); // $NON-NLS-1$
        panel.add(httpSampleNamingMode, gbc.clone());
        gbc.gridx++;
        gbc.weightx = 3;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(prefixHTTPSampleName, gbc.clone());
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(labelProxyPause, gbc.clone());
        gbc.gridx++;
        gbc.weightx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(proxyPauseHTTPSample, gbc.clone());
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(labelDefaultEncoding, gbc.clone());
        gbc.gridx++;
        gbc.weightx = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(defaultEncoding, gbc.clone());
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill=GridBagConstraints.VERTICAL;
        panel.add(samplerDownloadImages, gbc.clone());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill=GridBagConstraints.VERTICAL;
        panel.add(samplerRedirectAutomatically, gbc.clone());
        gbc.gridx++;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(samplerFollowRedirects, gbc.clone());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill=GridBagConstraints.VERTICAL;
        panel.add(useKeepAlive, gbc.clone());
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill=GridBagConstraints.VERTICAL;
        panel.add(labelSamplerType, gbc.clone());
        gbc.gridx++;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(samplerTypeName, gbc.clone());
        return panel;
    }

    private JPanel createTargetPanel() {
        targetNodesModel = new DefaultComboBoxModel<>();
        targetNodes = new JComboBox<>(targetNodesModel);
        targetNodes.setPrototypeDisplayValue(""); // $NON-NLS-1$ // Bug 56303 fixed the width of combo list
        JPopupMenu popup = (JPopupMenu) targetNodes.getUI().getAccessibleChild(targetNodes, 0); // get popup element
        JScrollPane scrollPane = findScrollPane(popup);
        if(scrollPane != null) {
            scrollPane.setHorizontalScrollBar(new JScrollBar(JScrollBar.HORIZONTAL)); // add scroll pane if label element is too long
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        targetNodes.setActionCommand(CHANGE_TARGET);
        // Action listener will be added later

        JLabel label = new JLabel(JMeterUtils.getResString("proxy_target")); // $NON-NLS-1$
        label.setLabelFor(targetNodes);

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(label);
        panel.add(targetNodes);

        return panel;
    }

    private JScrollPane findScrollPane(JPopupMenu popup) {
        Component[] components = popup.getComponents();
        for (Component component : components) {
            if(component instanceof JScrollPane) {
                return (JScrollPane) component;
            }
        }
        return null;
    }

    private JPanel createGroupingPanel() {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        // Note: position of these elements in the menu *must* match the
        // corresponding ProxyControl.GROUPING_* values.
        m.addElement(JMeterUtils.getResString("grouping_no_groups")); // $NON-NLS-1$
        m.addElement(JMeterUtils.getResString("grouping_add_separators")); // $NON-NLS-1$
        m.addElement(JMeterUtils.getResString("grouping_in_controllers")); // $NON-NLS-1$
        m.addElement(JMeterUtils.getResString("grouping_store_first_only")); // $NON-NLS-1$
        m.addElement(JMeterUtils.getResString("grouping_in_transaction_controllers")); // $NON-NLS-1$
        groupingMode = new JComboBox<>(m);
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
        JMeterUtils.applyHiDPI(includeTable);
        includeTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        includeTable.setPreferredScrollableViewportSize(new Dimension(80, 80));

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
        JMeterUtils.applyHiDPI(excludeTable);
        excludeTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        excludeTable.setPreferredScrollableViewportSize(new Dimension(80, 80));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("patterns_to_exclude"))); // $NON-NLS-1$

        panel.add(new JScrollPane(excludeTable), BorderLayout.CENTER);
        panel.add(createTableButtonPanel(ADD_EXCLUDE, DELETE_EXCLUDE, ADD_TO_EXCLUDE_FROM_CLIPBOARD, ADD_SUGGESTED_EXCLUDES), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createNotifyListenersPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("notify_child_listeners_fr"))); // $NON-NLS-1$

        notifyChildSamplerListenerOfFilteredSamplersCB = new JCheckBox(JMeterUtils.getResString("notify_child_listeners_fr")); // $NON-NLS-1$
        notifyChildSamplerListenerOfFilteredSamplersCB.setSelected(false);
        notifyChildSamplerListenerOfFilteredSamplersCB.addActionListener(this);
        notifyChildSamplerListenerOfFilteredSamplersCB.setActionCommand(ENABLE_RESTART);

        panel.add(notifyChildSamplerListenerOfFilteredSamplersCB);
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
            log.debug("Selecting item {} for model {} in {}", choice, model, this);
            if (choice.getTreeNode() == model.getTarget()) // .equals caused NPE
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

    private void buildNodesModel(JMeterTreeNode node, String parentName, int level) {
        String separator = " > ";
        if (node != null) {
            for (int i = 0; i < node.getChildCount(); i++) {
                StringBuilder name = new StringBuilder();
                JMeterTreeNode cur = (JMeterTreeNode) node.getChildAt(i);
                TestElement te = cur.getTestElement();
                if (te instanceof Controller) {
                    name.append(parentName);
                    name.append(cur.getName());
                    TreeNodeWrapper tnw = new TreeNodeWrapper(cur, name.toString());
                    targetNodesModel.addElement(tnw);
                    name.append(separator);
                    buildNodesModel(cur, name.toString(), level + 1);
                } else if (te instanceof TestPlan) {
                    name.append(cur.getName());
                    name.append(separator);
                    buildNodesModel(cur, name.toString(), 0);
                }
                // Ignore everything else
            }
        }
    }

    /**
     * Redefined to remove change parent and inserrt parent menu
     * @see org.apache.jmeter.control.gui.AbstractControllerGui#createPopupMenu()
     */
    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        JMenu addMenu = new JMenu(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addMenu.add(MenuFactory.makeMenu(MenuFactory.TIMERS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.PRE_PROCESSORS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.LISTENERS, ActionNames.ADD));

        pop.add(addMenu);

        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }

    int getHTTPSampleNamingMode() {
        return httpSampleNamingMode.getSelectedIndex();
    }

    String getProxyPauseHTTPSample() {
        return proxyPauseHTTPSample.getText();
    }

    public String getPrefixHTTPSampleName() {
        return prefixHTTPSampleName.getText();
    }

    void setHTTPSampleNamingMode(int selectedIndex) {
        httpSampleNamingMode.setSelectedIndex(selectedIndex);
        model.setHTTPSampleNamingMode(httpSampleNamingMode.getSelectedIndex());
    }

    void setProxyPauseHTTPSample(String text) {
        proxyPauseHTTPSample.setText(text);
        model.setProxyPauseHTTPSample(text);
    }

    void setPrefixHTTPSampleName(String text) {
        prefixHTTPSampleName.setText(text);
        model.setPrefixHTTPSampleName(text);
    }
}
