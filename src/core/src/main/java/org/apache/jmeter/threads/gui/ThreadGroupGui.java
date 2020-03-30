/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.threads.gui;

import static org.apache.jmeter.util.JMeterUtils.labelFor;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;

import net.miginfocom.swing.MigLayout;

@TestElementMetadata(labelResource = "threadgroup")
public class ThreadGroupGui extends AbstractThreadGroupGui implements ItemListener {
    private static final long serialVersionUID = 240L;

    private LoopControlPanel loopPanel;

    private static final String THREAD_NAME = "Thread Field";

    private static final String RAMP_NAME = "Ramp Up Field";

    private final JTextField threadInput = new JTextField();

    private final JTextField rampInput = new JTextField();

    private final boolean showDelayedStart;

    private JCheckBox delayedStart;

    private final JCheckBox scheduler = new JCheckBox(JMeterUtils.getResString("scheduler"));

    private final JTextField duration = new JTextField();
    private final JLabel durationLabel = labelFor(duration, "duration");

    private final JTextField delay = new JTextField(); // Relative start-up time
    private final JLabel delayLabel = labelFor(delay, "delay");

    private final JCheckBox sameUserBox =
            new JCheckBox(JMeterUtils.getResString("threadgroup_same_user"));

    public ThreadGroupGui() {
        this(true);
    }

    public ThreadGroupGui(boolean showDelayedStart) {
        super();
        this.showDelayedStart = showDelayedStart;
        init();
        initGui();
    }

    @Override
    public TestElement createTestElement() {
        ThreadGroup tg = new ThreadGroup();
        modifyTestElement(tg);
        return tg;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement tg) {
        super.configureTestElement(tg);
        if (tg instanceof AbstractThreadGroup) {
            ((AbstractThreadGroup) tg).setSamplerController((LoopController) loopPanel.createTestElement());
        }

        tg.setProperty(AbstractThreadGroup.NUM_THREADS, threadInput.getText());
        tg.setProperty(ThreadGroup.RAMP_TIME, rampInput.getText());
        if (showDelayedStart) {
            tg.setProperty(ThreadGroup.DELAYED_START, delayedStart.isSelected(), false);
        }
        tg.setProperty(new BooleanProperty(ThreadGroup.SCHEDULER, scheduler.isSelected()));
        tg.setProperty(ThreadGroup.DURATION, duration.getText());
        tg.setProperty(ThreadGroup.DELAY, delay.getText());
        tg.setProperty(AbstractThreadGroup.IS_SAME_USER_ON_NEXT_ITERATION,sameUserBox.isSelected());
    }

    @Override
    public void configure(TestElement tg) {
        super.configure(tg);
        threadInput.setText(tg.getPropertyAsString(AbstractThreadGroup.NUM_THREADS));
        rampInput.setText(tg.getPropertyAsString(ThreadGroup.RAMP_TIME));
        loopPanel.configure((TestElement) tg.getProperty(AbstractThreadGroup.MAIN_CONTROLLER).getObjectValue());
        if (showDelayedStart) {
            delayedStart.setSelected(tg.getPropertyAsBoolean(ThreadGroup.DELAYED_START));
        }
        scheduler.setSelected(tg.getPropertyAsBoolean(ThreadGroup.SCHEDULER));

        toggleSchedulerFields(scheduler.isSelected());

        duration.setText(tg.getPropertyAsString(ThreadGroup.DURATION));
        delay.setText(tg.getPropertyAsString(ThreadGroup.DELAY));
        final boolean isSameUser = tg.getPropertyAsBoolean(AbstractThreadGroup.IS_SAME_USER_ON_NEXT_ITERATION, true);
        sameUserBox.setSelected(isSameUser);
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getItem().equals(scheduler)) {
            toggleSchedulerFields(scheduler.isSelected());
        }
    }

    /**
     * @param enable boolean used to enable/disable fields related to scheduler
     */
    private void toggleSchedulerFields(boolean enable) {
        duration.setEnabled(enable);
        durationLabel.setEnabled(enable);
        delay.setEnabled(enable);
        delayLabel.setEnabled(enable);
    }

    private JPanel createControllerPanel() {
        loopPanel = new LoopControlPanel(false);
        LoopController looper = (LoopController) loopPanel.createTestElement();
        looper.setLoops(1);
        loopPanel.configure(looper);
        return loopPanel;
    }


    @Override
    public String getLabelResource() {
        return "threadgroup"; // $NON-NLS-1$
    }

    @Override
    public void clearGui(){
        super.clearGui();
        initGui();
    }

    // Initialise the gui field values
    private void initGui(){
        threadInput.setText("1"); // $NON-NLS-1$
        rampInput.setText("1"); // $NON-NLS-1$
        loopPanel.clearGui();
        if (showDelayedStart) {
            delayedStart.setSelected(false);
        }
        scheduler.setSelected(false);
        delay.setText(""); // $NON-NLS-1$
        duration.setText(""); // $NON-NLS-1$
        sameUserBox.setSelected(true);
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        // THREAD PROPERTIES
        JPanel threadPropsPanel = new JPanel(new MigLayout("fillx, wrap 2", "[][fill,grow]"));
        threadPropsPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("thread_properties"))); // $NON-NLS-1$

        // NUMBER OF THREADS
        threadPropsPanel.add(labelFor(threadInput, "number_of_threads")); // $NON-NLS-1$
        threadInput.setName(THREAD_NAME);
        threadPropsPanel.add(threadInput);

        // RAMP-UP
        threadPropsPanel.add(labelFor(rampInput, "ramp_up"));
        rampInput.setName(RAMP_NAME);
        threadPropsPanel.add(rampInput);

        // LOOP COUNT
        LoopControlPanel loopController = (LoopControlPanel) createControllerPanel();
        threadPropsPanel.add(loopController.getLoopsLabel(), "split 2");
        threadPropsPanel.add(loopController.getInfinite(), "gapleft push");
        threadPropsPanel.add(loopController.getLoops());
        threadPropsPanel.add(sameUserBox, "span 2");
        if (showDelayedStart) {
            delayedStart = new JCheckBox(JMeterUtils.getResString("delayed_start")); // $NON-NLS-1$
            threadPropsPanel.add(delayedStart, "span 2");
        }
        scheduler.addItemListener(this);

        threadPropsPanel.add(scheduler, "span 2");

        threadPropsPanel.add(durationLabel);
        threadPropsPanel.add(duration);
        threadPropsPanel.add(delayLabel);
        threadPropsPanel.add(delay);
        add(threadPropsPanel, BorderLayout.CENTER);
    }

}
