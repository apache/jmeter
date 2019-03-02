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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.protocol.http.control.HttpMirrorControl;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI of Mirror Server Test element
 *
 */
public class HttpMirrorControlGui extends LogicControllerGui
    implements JMeterGUIComponent, ActionListener, UnsharedComponent {

    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(HttpMirrorControlGui.class);

    private JTextField portField;

    private JTextField maxPoolSizeField;

    private JTextField maxQueueSizeField;

    private JButton stop;
    private JButton start;

    private static final String ACTION_STOP = "stop"; // $NON-NLS-1$

    private static final String ACTION_START = "start"; // $NON-NLS-1$

    private HttpMirrorControl mirrorController;


    public HttpMirrorControlGui() {
        super();
        log.debug("Creating HttpMirrorControlGui");
        init();
    }

    @Override
    public TestElement createTestElement() {
        mirrorController = new HttpMirrorControl();
        log.debug("creating/configuring model = {}", mirrorController);
        modifyTestElement(mirrorController);
        return mirrorController;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement el) {
        configureTestElement(el);
        if (el instanceof HttpMirrorControl) {
            mirrorController = (HttpMirrorControl) el;
            mirrorController.setPort(portField.getText());
            mirrorController.setMaxPoolSize(maxPoolSizeField.getText());
            mirrorController.setMaxQueueSize(maxQueueSizeField.getText());
        }
    }

    @Override
    public String getLabelResource() {
        return "httpmirror_title"; // $NON-NLS-1$
    }

    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(MenuFactory.NON_TEST_ELEMENTS);
    }

    @Override
    public void configure(TestElement element) {
        log.debug("Configuring gui with {}", element);
        super.configure(element);
        mirrorController = (HttpMirrorControl) element;
        portField.setText(mirrorController.getPortString());
        maxPoolSizeField.setText(mirrorController.getMaxPoolSizeAsString());
        maxQueueSizeField.setText(mirrorController.getMaxQueueSizeAsString());
        repaint();
    }


    @Override
    public void actionPerformed(ActionEvent action) {
        String command = action.getActionCommand();

        if (command.equals(ACTION_STOP)) {
            mirrorController.stopHttpMirror();
            stop.setEnabled(false);
            start.setEnabled(true);
        } else if (command.equals(ACTION_START)) {
            modifyTestElement(mirrorController);
            mirrorController.startHttpMirror();
            start.setEnabled(false);
            stop.setEnabled(true);
        }
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        Box myBox = Box.createVerticalBox();
        myBox.add(createPortPanel());
        mainPanel.add(myBox, BorderLayout.NORTH);

        mainPanel.add(createControls(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createControls() {
        start = new JButton(JMeterUtils.getResString("start")); // $NON-NLS-1$
        start.addActionListener(this);
        start.setActionCommand(ACTION_START);
        start.setEnabled(true);

        stop = new JButton(JMeterUtils.getResString("stop")); // $NON-NLS-1$
        stop.addActionListener(this);
        stop.setActionCommand(ACTION_STOP);
        stop.setEnabled(false);

        JPanel panel = new JPanel();
        panel.add(start);
        panel.add(stop);
        return panel;
    }

    private JPanel createPortPanel() {
        portField = new JTextField(HttpMirrorControl.DEFAULT_PORT_S, 8);
        portField.setName(HttpMirrorControl.PORT);

        JLabel label = new JLabel(JMeterUtils.getResString("port")); // $NON-NLS-1$
        label.setLabelFor(portField);

        maxPoolSizeField = new JTextField(Integer.toString(HttpMirrorControl.DEFAULT_MAX_POOL_SIZE), 8);
        maxPoolSizeField.setName(HttpMirrorControl.MAX_POOL_SIZE);

        JLabel mpsLabel = new JLabel(JMeterUtils.getResString("httpmirror_max_pool_size")); // $NON-NLS-1$
        mpsLabel.setLabelFor(maxPoolSizeField);

        maxQueueSizeField = new JTextField(Integer.toString(HttpMirrorControl.DEFAULT_MAX_QUEUE_SIZE), 8);
        maxQueueSizeField.setName(HttpMirrorControl.MAX_QUEUE_SIZE);

        JLabel mqsLabel = new JLabel(JMeterUtils.getResString("httpmirror_max_queue_size")); // $NON-NLS-1$
        mqsLabel.setLabelFor(maxQueueSizeField);

        HorizontalPanel panel = new HorizontalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("httpmirror_settings"))); // $NON-NLS-1$

        panel.add(label);
        panel.add(portField);

        panel.add(mpsLabel);
        panel.add(maxPoolSizeField);

        panel.add(mqsLabel);
        panel.add(maxQueueSizeField);

        panel.add(Box.createHorizontalStrut(10));

        return panel;
    }

    @Override
    public void clearGui(){
        super.clearGui();
        portField.setText(HttpMirrorControl.DEFAULT_PORT_S);
        maxPoolSizeField.setText(Integer.toString(HttpMirrorControl.DEFAULT_MAX_POOL_SIZE));
    }

    /**
     * Redefined to remove change parent and inserrt parent menu
     * @see org.apache.jmeter.control.gui.AbstractControllerGui#createPopupMenu()
     */
    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }
}
