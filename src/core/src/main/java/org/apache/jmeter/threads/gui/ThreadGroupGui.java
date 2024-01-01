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
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.gui.JBooleanPropertyEditor;
import org.apache.jmeter.gui.JTextComponentBinding;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.AbstractThreadGroupSchema;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.ThreadGroupSchema;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JEditableCheckBox;

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

    private JBooleanPropertyEditor delayedStart;

    private final JBooleanPropertyEditor scheduler =
            new JBooleanPropertyEditor(
                    ThreadGroupSchema.INSTANCE.getUseScheduler(),
                    JMeterUtils.getResString("scheduler"));

    private final JTextField duration = new JTextField();
    private final JLabel durationLabel = labelFor(duration, "duration");

    private final JTextField delay = new JTextField(); // Relative start-up time
    private final JLabel delayLabel = labelFor(delay, "delay");

    private final JBooleanPropertyEditor sameUserBox =
            new JBooleanPropertyEditor(
                    AbstractThreadGroupSchema.INSTANCE.getSameUserOnNextIteration(),
                    JMeterUtils.getResString("threadgroup_same_user"));

    public ThreadGroupGui() {
        this(true);
    }

    public ThreadGroupGui(boolean showDelayedStart) {
        super();
        this.showDelayedStart = showDelayedStart;
        init();
        initGui();
        if (showDelayedStart) {
            bindingGroup.add(delayedStart);
        }
        bindingGroup.addAll(
                Arrays.asList(
                        new JTextComponentBinding(threadInput, AbstractThreadGroupSchema.INSTANCE.getNumThreads()),
                        new JTextComponentBinding(rampInput, ThreadGroupSchema.INSTANCE.getRampTime()),
                        new JTextComponentBinding(duration, ThreadGroupSchema.INSTANCE.getDuration()),
                        new JTextComponentBinding(delay, ThreadGroupSchema.INSTANCE.getDelay()),
                        sameUserBox,
                        scheduler
                )
        );
    }

    @Override
    public TestElement makeTestElement() {
        return new ThreadGroup();
    }

    @Override
    public void assignDefaultValues(TestElement element) {
        super.assignDefaultValues(element);
        element.set(ThreadGroupSchema.INSTANCE.getNumThreads(), 1);
        element.set(ThreadGroupSchema.INSTANCE.getRampTime(), 1);
        element.set(AbstractThreadGroupSchema.INSTANCE.getSameUserOnNextIteration(), true);
        ((AbstractThreadGroup) element).setSamplerController((LoopController) loopPanel.createTestElement());
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement tg) {
        super.modifyTestElement(tg);
        if (tg instanceof AbstractThreadGroup) {
            ((AbstractThreadGroup) tg).setSamplerController((LoopController) loopPanel.createTestElement());
        }
    }

    @Override
    public void configure(TestElement tg) {
        super.configure(tg);
        loopPanel.configure((TestElement) tg.getProperty(AbstractThreadGroup.MAIN_CONTROLLER).getObjectValue());
        toggleSchedulerFields();
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        // Method kept for backward compatibility
    }

    private void toggleSchedulerFields() {
        boolean enable = !scheduler.getValue().equals(JEditableCheckBox.Value.of(false));
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
        loopPanel.clearGui();
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
            delayedStart = new JBooleanPropertyEditor(
                    ThreadGroupSchema.INSTANCE.getDelayedStart(),
                    JMeterUtils.getResString("delayed_start")); // $NON-NLS-1$
            threadPropsPanel.add(delayedStart, "span 2");
        }
        scheduler.addPropertyChangeListener(
                JBooleanPropertyEditor.VALUE_PROPERTY, (ev) -> toggleSchedulerFields());

        threadPropsPanel.add(scheduler, "span 2");

        threadPropsPanel.add(durationLabel);
        threadPropsPanel.add(duration);
        threadPropsPanel.add(delayLabel);
        threadPropsPanel.add(delay);
        add(threadPropsPanel, BorderLayout.CENTER);
    }

}
