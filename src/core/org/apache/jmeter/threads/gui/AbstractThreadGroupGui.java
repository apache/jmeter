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

package org.apache.jmeter.threads.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;

public abstract class AbstractThreadGroupGui extends AbstractJMeterGuiComponent {
    private static final long serialVersionUID = 240L;

    // Sampler error action buttons
    private JRadioButton continueBox;

    private JRadioButton startNextLoop;

    private JRadioButton stopThrdBox;

    private JRadioButton stopTestBox;

    private JRadioButton stopTestNowBox;

    public AbstractThreadGroupGui(){
        super();
        init();
        initGui();
    }

    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(MenuFactory.THREADS);
    }

    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        pop.add(MenuFactory.makeMenus(new String[] {
                MenuFactory.CONTROLLERS,
                MenuFactory.CONFIG_ELEMENTS,
                MenuFactory.TIMERS,
                MenuFactory.PRE_PROCESSORS,
                MenuFactory.SAMPLERS,
                MenuFactory.POST_PROCESSORS,
                MenuFactory.ASSERTIONS,
                MenuFactory.LISTENERS,
                },
                JMeterUtils.getResString("add"), // $NON-NLS-1$
                ActionNames.ADD));
        
        if(this.isEnabled() && 
                // Check test is not started already
                !JMeterUtils.isTestRunning()) {
            pop.addSeparator();

            JMenuItem addThinkTimesToChildren = new JMenuItem(JMeterUtils.getResString("add_think_times"));
            addThinkTimesToChildren.setName("add_think_times");
            addThinkTimesToChildren.addActionListener(ActionRouter.getInstance());
            addThinkTimesToChildren.setActionCommand(ActionNames.ADD_THINK_TIME_BETWEEN_EACH_STEP);
            pop.add(addThinkTimesToChildren);

            JMenuItem runTg = new JMenuItem(JMeterUtils.getResString("run_threadgroup"));
            runTg.setName("run_threadgroup");
            runTg.addActionListener(ActionRouter.getInstance());
            runTg.setActionCommand(ActionNames.RUN_TG);
            pop.add(runTg);
    
            JMenuItem runTgNotimers = new JMenuItem(JMeterUtils.getResString("run_threadgroup_no_timers"));
            runTgNotimers.setName("run_threadgroup_no_timers");
            runTgNotimers.addActionListener(ActionRouter.getInstance());
            runTgNotimers.setActionCommand(ActionNames.RUN_TG_NO_TIMERS);
            pop.add(runTgNotimers);

            JMenuItem validateTg = new JMenuItem(JMeterUtils.getResString("validate_threadgroup"));
            validateTg.setName("validate_threadgroup");
            validateTg.addActionListener(ActionRouter.getInstance());
            validateTg.setActionCommand(ActionNames.VALIDATE_TG);
            pop.add(validateTg);

        }
        
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop, false);
        return pop;
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public void clearGui(){
        super.clearGui();
        initGui();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createOnErrorPanel());
        add(box, BorderLayout.NORTH);
    }
    
    private void initGui() {
        continueBox.setSelected(true);
    }

    private JPanel createOnErrorPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("sampler_on_error_action"))); // $NON-NLS-1$

        ButtonGroup group = new ButtonGroup();

        continueBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_continue")); // $NON-NLS-1$
        group.add(continueBox);
        panel.add(continueBox);

        startNextLoop = new JRadioButton(JMeterUtils.getResString("sampler_on_error_start_next_loop")); // $NON-NLS-1$
        group.add(startNextLoop);
        panel.add(startNextLoop);

        stopThrdBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_thread")); // $NON-NLS-1$
        group.add(stopThrdBox);
        panel.add(stopThrdBox);

        stopTestBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_test")); // $NON-NLS-1$
        group.add(stopTestBox);
        panel.add(stopTestBox);

        stopTestNowBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_test_now")); // $NON-NLS-1$
        group.add(stopTestNowBox);
        panel.add(stopTestNowBox);

        return panel;
    }

    private void setSampleErrorBoxes(AbstractThreadGroup te) {
        if (te.getOnErrorStopTest()) {
            stopTestBox.setSelected(true);
        } else if (te.getOnErrorStopTestNow()) {
            stopTestNowBox.setSelected(true);
        } else if (te.getOnErrorStopThread()) {
            stopThrdBox.setSelected(true);
        } else if (te.getOnErrorStartNextLoop()) {
            startNextLoop.setSelected(true);
        } else {
            continueBox.setSelected(true);
        }
    }

    private String onSampleError() {
        if (stopTestBox.isSelected()) {
            return AbstractThreadGroup.ON_SAMPLE_ERROR_STOPTEST;
        }
        if (stopTestNowBox.isSelected()) {
            return AbstractThreadGroup.ON_SAMPLE_ERROR_STOPTEST_NOW;
        }
        if (stopThrdBox.isSelected()) {
            return AbstractThreadGroup.ON_SAMPLE_ERROR_STOPTHREAD;
        }
        if (startNextLoop.isSelected()) {
            return AbstractThreadGroup.ON_SAMPLE_ERROR_START_NEXT_LOOP;
        }

        // Defaults to continue
        return AbstractThreadGroup.ON_SAMPLE_ERROR_CONTINUE;
    }

   @Override
    public void configure(TestElement tg) {
        super.configure(tg);
        setSampleErrorBoxes((AbstractThreadGroup) tg);
    }
    
   @Override
    protected void configureTestElement(TestElement tg) {
        super.configureTestElement(tg);
        tg.setProperty(new StringProperty(AbstractThreadGroup.ON_SAMPLE_ERROR, onSampleError()));
    }

}
