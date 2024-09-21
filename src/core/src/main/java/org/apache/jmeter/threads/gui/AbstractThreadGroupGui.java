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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.AbstractThreadGroupSchema;
import org.apache.jmeter.util.JMeterUtils;
import org.apiguardian.api.API;

import net.miginfocom.swing.MigLayout;

public abstract class AbstractThreadGroupGui extends AbstractJMeterGuiComponent {
    private static final long serialVersionUID = 240L;

    // Sampler error action buttons
    private JRadioButton continueBox;
    private JRadioButton startNextLoop;
    private JRadioButton stopThreadBox;
    private JRadioButton stopTestBox;
    private JRadioButton stopTestNowBox;

    protected AbstractThreadGroupGui(){
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
        pop.add(createAddMenu());

        if (this.isEnabled() && !JMeterUtils.isTestRunning()) {
            pop.addSeparator();

            pop.add(createMenuItem("add_think_times", ActionNames.ADD_THINK_TIME_BETWEEN_EACH_STEP));
            pop.add(createMenuItem("run_threadgroup", ActionNames.RUN_TG));
            pop.add(createMenuItem("run_threadgroup_no_timers", ActionNames.RUN_TG_NO_TIMERS));
            pop.add(createMenuItem("validate_threadgroup", ActionNames.VALIDATE_TG));
        }

        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop, false);
        return pop;
    }

    private static JMenuItem createMenuItem(String name, String actionCommand) {
        JMenuItem addThinkTimesToChildren = new JMenuItem(JMeterUtils.getResString(name));
        addThinkTimesToChildren.setName(name);
        addThinkTimesToChildren.addActionListener(ActionRouter.getInstance());
        addThinkTimesToChildren.setActionCommand(actionCommand);
        return addThinkTimesToChildren;
    }

    private static JMenu createAddMenu() {
        String addAction = ActionNames.ADD;
        JMenu addMenu = new JMenu(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addMenu.add(MenuFactory.makeMenu(MenuFactory.SAMPLERS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.CONTROLLERS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.PRE_PROCESSORS, addAction));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.POST_PROCESSORS, addAction));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.ASSERTIONS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.TIMERS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.FRAGMENTS, addAction));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.CONFIG_ELEMENTS, addAction));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.LISTENERS, addAction));
        return addMenu;
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

    // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        VerticalPanel box = new VerticalPanel();
        box.add(makeTitlePanel());
        box.add(createOnErrorPanel());
        add(box, BorderLayout.NORTH);
    }

    private void initGui() {
        continueBox.setSelected(true);
    }

    private JPanel createOnErrorPanel() {
        JPanel panel = new JPanel(new MigLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("sampler_on_error_action"))); // $NON-NLS-1$

        ButtonGroup group = new ButtonGroup();

        continueBox = new JRadioButton(
                JMeterUtils.getResString("sampler_on_error_continue")); // $NON-NLS-1$
        group.add(continueBox);
        panel.add(continueBox);

        startNextLoop = new JRadioButton(
                JMeterUtils.getResString("sampler_on_error_start_next_loop")); // $NON-NLS-1$
        group.add(startNextLoop);
        panel.add(startNextLoop);

        stopThreadBox = new JRadioButton(
                JMeterUtils.getResString("sampler_on_error_stop_thread")); // $NON-NLS-1$
        group.add(stopThreadBox);
        panel.add(stopThreadBox);

        stopTestBox = new JRadioButton(
                JMeterUtils.getResString("sampler_on_error_stop_test")); // $NON-NLS-1$
        group.add(stopTestBox);
        panel.add(stopTestBox);

        stopTestNowBox = new JRadioButton(
                JMeterUtils.getResString("sampler_on_error_stop_test_now")); // $NON-NLS-1$
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
            stopThreadBox.setSelected(true);
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
        if (stopThreadBox.isSelected()) {
            return AbstractThreadGroup.ON_SAMPLE_ERROR_STOPTHREAD;
        }
        if (startNextLoop.isSelected()) {
            return AbstractThreadGroup.ON_SAMPLE_ERROR_START_NEXT_LOOP;
        }

        // Defaults to continue
        return AbstractThreadGroup.ON_SAMPLE_ERROR_CONTINUE;
    }

    @Override
    public void assignDefaultValues(TestElement element) {
        super.assignDefaultValues(element);
        element.set(AbstractThreadGroupSchema.INSTANCE.getOnSampleError(), AbstractThreadGroup.ON_SAMPLE_ERROR_CONTINUE);
    }

    @Override
    public void configure(TestElement tg) {
        super.configure(tg);
        setSampleErrorBoxes((AbstractThreadGroup) tg);
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);
        element.set(AbstractThreadGroupSchema.INSTANCE.getOnSampleError(), onSampleError());
    }

    /**
     * {@inheritDoc}
     * @deprecated Override {@link #modifyTestElement(TestElement)} instead
     * @param tg the TestElement being configured.
     */
   @Override
   @Deprecated
   @API(status = API.Status.DEPRECATED, since = "5.6.3")
    protected void configureTestElement(TestElement tg) {
        super.configureTestElement(tg);
        tg.set(AbstractThreadGroupSchema.INSTANCE.getOnSampleError(), onSampleError());
    }
}
