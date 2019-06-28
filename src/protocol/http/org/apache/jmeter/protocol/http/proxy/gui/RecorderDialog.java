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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Dialog for Recorder
 * @since 5.0
 */
public class RecorderDialog extends JDialog implements ItemListener, KeyListener, ActionListener { // NOSONAR


    /**
     *
     */
    private static final long serialVersionUID = 931790497924069705L;

    /**
     * Add a prefix/transaction name to HTTP sample name recorded
     */
    private JTextField prefixHTTPSampleName;

    private JTextField proxyPauseHTTPSample;

    /**
     * To choose between a prefix or a transaction name
     */
    private JComboBox<String> httpSampleNamingMode;

    private ProxyControlGui recorderGui;

    private JButton stop;

    /**
     * For tests Only
     */
    public RecorderDialog() {
        super();
        //
    }
    public RecorderDialog(ProxyControlGui controlGui) {
        super((JFrame) null, JMeterUtils.getResString("proxy_recorder_dialog"), false); //$NON-NLS-1$
        this.recorderGui = controlGui;
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        init();
    }

    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        // Hide Window on ESC
        Action escapeAction = new AbstractAction("ESCAPE") {

            private static final long serialVersionUID = -6543764044868772971L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };

        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put(escapeAction.getValue(Action.NAME), escapeAction);
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStrokes.ESC, escapeAction.getValue(Action.NAME));

        return rootPane;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.getContentPane().setLayout(new BorderLayout(10,10));

        DefaultComboBoxModel<String> choice = new DefaultComboBoxModel<>();
        choice.addElement(JMeterUtils.getResString("sample_name_prefix")); // $NON-NLS-1$
        choice.addElement(JMeterUtils.getResString("sample_name_transaction")); // $NON-NLS-1$
        httpSampleNamingMode = new JComboBox<>(choice);
        httpSampleNamingMode.setName(ProxyControlGui.HTTP_SAMPLER_NAMING_MODE);
        httpSampleNamingMode.addItemListener(this);

        prefixHTTPSampleName = new JTextField(20);
        prefixHTTPSampleName.addKeyListener(this);
        prefixHTTPSampleName.setName(ProxyControlGui.PREFIX_HTTP_SAMPLER_NAME);

        proxyPauseHTTPSample = new JTextField(10);
        proxyPauseHTTPSample.addKeyListener(this);
        proxyPauseHTTPSample.setName(ProxyControlGui.PROXY_PAUSE_HTTP_SAMPLER);

        proxyPauseHTTPSample.setActionCommand(ProxyControlGui.ENABLE_RESTART);
        JLabel labelProxyPause = new JLabel(JMeterUtils.getResString("proxy_pause_http_sampler")); // $NON-NLS-1$
        labelProxyPause.setLabelFor(proxyPauseHTTPSample);

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

        this.getContentPane().add(panel, BorderLayout.CENTER);

        String iconSize = JMeterUtils.getPropDefault(JMeterToolBar.TOOLBAR_ICON_SIZE, JMeterToolBar.DEFAULT_TOOLBAR_ICON_SIZE);
        stop = recorderGui.createStopButton(iconSize);
        stop.addActionListener(this);

        GridLayout gridLayout = new GridLayout(1, 1);
        JPanel panelStop = new JPanel(gridLayout);
        panelStop.add(stop);
        this.getContentPane().add(panelStop, BorderLayout.WEST);
        this.pack();
        this.setLocation(5, 10);
        prefixHTTPSampleName.requestFocusInWindow();
    }

    /* (non-Javadoc)
     * @see java.awt.Dialog#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        stop.setEnabled(true);
        prefixHTTPSampleName.requestFocusInWindow();
        prefixHTTPSampleName.setText(recorderGui.getPrefixHTTPSampleName());
        httpSampleNamingMode.setSelectedIndex(recorderGui.getHTTPSampleNamingMode());
        proxyPauseHTTPSample.setText(recorderGui.getProxyPauseHTTPSample());
        setAlwaysOnTop(b);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof JComboBox) {
            JComboBox combo = (JComboBox) e.getSource();
            if(ProxyControlGui.HTTP_SAMPLER_NAMING_MODE.equals(combo.getName())){
                recorderGui.setHTTPSampleNamingMode(httpSampleNamingMode.getSelectedIndex());
            }
        }
        else {
            recorderGui.enableRestart();
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
        if(fieldName.equals(ProxyControlGui.PREFIX_HTTP_SAMPLER_NAME)) {
            recorderGui.setPrefixHTTPSampleName(prefixHTTPSampleName.getText());
        } else if(fieldName.equals(ProxyControlGui.PROXY_PAUSE_HTTP_SAMPLER)) {
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
            recorderGui.setProxyPauseHTTPSample(proxyPauseHTTPSample.getText());
            recorderGui.enableRestart();
        }
    }
    @Override
    public void actionPerformed(ActionEvent event) {
        recorderGui.stopRecorder();
    }
}
