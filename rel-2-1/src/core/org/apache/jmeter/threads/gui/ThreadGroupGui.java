// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.threads.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.FocusRequester;
import org.apache.jmeter.gui.util.JDateField;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @version $Revision$ on $Date$
 */
public class ThreadGroupGui extends AbstractJMeterGuiComponent implements ItemListener {
	LoopControlPanel loopPanel;

	private VerticalPanel mainPanel;

	private final static String THREAD_NAME = "Thread Field";

	private final static String RAMP_NAME = "Ramp Up Field";

	private JTextField threadInput;

	private JTextField rampInput;

	// private final static String SCHEDULER = "scheduler";
	// private final static String START_TIME= "start_time";
	// private final static String END_TIME= "end_time";

	private JDateField start;

	private JDateField end;

	private JCheckBox scheduler;

	private JTextField duration;

	private JTextField delay; // Relative start-up time

	// Sampler error action buttons
	private JRadioButton continueBox;

	private JRadioButton stopThrdBox;

	private JRadioButton stopTestBox;

	public ThreadGroupGui() {
		super();
		init();
	}

	public Collection getMenuCategories() {
		return null;
	}

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
	public void modifyTestElement(TestElement tg) {
		super.configureTestElement(tg);
		if (tg instanceof ThreadGroup) {
			((ThreadGroup) tg).setSamplerController((LoopController) loopPanel.createTestElement());
		}

		tg.setProperty(ThreadGroup.NUM_THREADS, threadInput.getText());
		tg.setProperty(ThreadGroup.RAMP_TIME, rampInput.getText());
		tg.setProperty(new LongProperty(ThreadGroup.START_TIME, start.getDate().getTime()));
		tg.setProperty(new LongProperty(ThreadGroup.END_TIME, end.getDate().getTime()));
		tg.setProperty(new BooleanProperty(ThreadGroup.SCHEDULER, scheduler.isSelected()));
		tg.setProperty(new StringProperty(ThreadGroup.ON_SAMPLE_ERROR, onSampleError()));
		tg.setProperty(ThreadGroup.DURATION, duration.getText());
		tg.setProperty(ThreadGroup.DELAY, delay.getText());
	}

	private void setSampleErrorBoxes(ThreadGroup te) {
		stopTestBox.setSelected(te.getOnErrorStopTest());
		stopThrdBox.setSelected(te.getOnErrorStopThread());
		continueBox.setSelected(!te.getOnErrorStopThread() && !te.getOnErrorStopTest());
	}

	private String onSampleError() {
		if (stopTestBox.isSelected())
			return ThreadGroup.ON_SAMPLE_ERROR_STOPTEST;
		if (stopThrdBox.isSelected())
			return ThreadGroup.ON_SAMPLE_ERROR_STOPTHREAD;

		// Defaults to continue
		return ThreadGroup.ON_SAMPLE_ERROR_CONTINUE;
	}

	public void configure(TestElement tg) {
		super.configure(tg);
		threadInput.setText(tg.getPropertyAsString(ThreadGroup.NUM_THREADS));
		rampInput.setText(tg.getPropertyAsString(ThreadGroup.RAMP_TIME));
		loopPanel.configure((TestElement) tg.getProperty(ThreadGroup.MAIN_CONTROLLER).getObjectValue());
		scheduler.setSelected(tg.getPropertyAsBoolean(ThreadGroup.SCHEDULER));

		if (scheduler.isSelected()) {
			mainPanel.setVisible(true);
		} else {
			mainPanel.setVisible(false);
		}

		// Check if the property exists
		String s = tg.getPropertyAsString(ThreadGroup.START_TIME);
		if (s.length() == 0) {// Must be an old test plan
			start.setDate(new Date());
			end.setDate(new Date());
		} else {
			start.setDate(new Date(tg.getPropertyAsLong(ThreadGroup.START_TIME)));
			end.setDate(new Date(tg.getPropertyAsLong(ThreadGroup.END_TIME)));
		}
		duration.setText(tg.getPropertyAsString(ThreadGroup.DURATION));
		delay.setText(tg.getPropertyAsString(ThreadGroup.DELAY));

		setSampleErrorBoxes((ThreadGroup) tg);
	}

	public void itemStateChanged(ItemEvent ie) {
		if (ie.getItem().equals(scheduler)) {
			if (scheduler.isSelected()) {
				mainPanel.setVisible(true);
			} else {
				mainPanel.setVisible(false);
			}
		}
	}

	public JPopupMenu createPopupMenu() {
		JPopupMenu pop = new JPopupMenu();
		pop.add(MenuFactory.makeMenus(new String[] { MenuFactory.CONTROLLERS, MenuFactory.LISTENERS,
				MenuFactory.SAMPLERS, MenuFactory.ASSERTIONS,MenuFactory.TIMERS, MenuFactory.CONFIG_ELEMENTS, MenuFactory.PRE_PROCESSORS,
				MenuFactory.POST_PROCESSORS }, JMeterUtils.getResString("Add"), "Add"));
		MenuFactory.addEditMenu(pop, true);
		MenuFactory.addFileMenu(pop);
		return pop;
	}

	public JPanel createControllerPanel() {
		loopPanel = new LoopControlPanel(false);
		LoopController looper = (LoopController) loopPanel.createTestElement();
		looper.setLoops(1);
		loopPanel.configure(looper);
		return loopPanel;
	}

	/**
	 * Create a panel containing the StartTime field and corresponding label.
	 * 
	 * @return a GUI panel containing the StartTime field
	 */
	private JPanel createStartTimePanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("starttime"));
		panel.add(label, BorderLayout.WEST);
		Date today = new Date();
		start = new JDateField(today);
		panel.add(start, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Create a panel containing the EndTime field and corresponding label.
	 * 
	 * @return a GUI panel containing the EndTime field
	 */
	private JPanel createEndTimePanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("endtime"));
		panel.add(label, BorderLayout.WEST);
		Date today = new Date();
		end = new JDateField(today);
		panel.add(end, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Create a panel containing the Duration field and corresponding label.
	 * 
	 * @return a GUI panel containing the Duration field
	 */
	private JPanel createDurationPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("duration"));
		panel.add(label, BorderLayout.WEST);
		duration = new JTextField();
		panel.add(duration, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Create a panel containing the Duration field and corresponding label.
	 * 
	 * @return a GUI panel containing the Duration field
	 */
	private JPanel createDelayPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel(JMeterUtils.getResString("delay"));
		panel.add(label, BorderLayout.WEST);
		delay = new JTextField();
		panel.add(delay, BorderLayout.CENTER);
		return panel;
	}

	public String getLabelResource() {
		return "threadgroup";
	}

	private JPanel createOnErrorPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("sampler_on_error_action")));

		ButtonGroup group = new ButtonGroup();

		continueBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_continue"));
		group.add(continueBox);
		continueBox.setSelected(true);
		panel.add(continueBox);

		stopThrdBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_thread"));
		group.add(stopThrdBox);
		panel.add(stopThrdBox);

		stopTestBox = new JRadioButton(JMeterUtils.getResString("sampler_on_error_stop_test"));
		group.add(stopTestBox);
		panel.add(stopTestBox);

		return panel;
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		Box box = Box.createVerticalBox();
		box.add(makeTitlePanel());
		box.add(createOnErrorPanel());
		add(box, BorderLayout.NORTH);

		// JPanel mainPanel = new JPanel(new BorderLayout());

		// THREAD PROPERTIES
		VerticalPanel threadPropsPanel = new VerticalPanel();
		threadPropsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("thread_properties")));

		// NUMBER OF THREADS
		JPanel threadPanel = new JPanel(new BorderLayout(5, 0));

		JLabel threadLabel = new JLabel(JMeterUtils.getResString("number_of_threads"));
		threadPanel.add(threadLabel, BorderLayout.WEST);

		threadInput = new JTextField("1", 5);
		threadInput.setName(THREAD_NAME);
		threadLabel.setLabelFor(threadInput);
		threadPanel.add(threadInput, BorderLayout.CENTER);

		threadPropsPanel.add(threadPanel);
		new FocusRequester(threadInput);

		// RAMP-UP
		JPanel rampPanel = new JPanel(new BorderLayout(5, 0));
		JLabel rampLabel = new JLabel(JMeterUtils.getResString("ramp_up"));
		rampPanel.add(rampLabel, BorderLayout.WEST);

		rampInput = new JTextField("1", 5);
		rampInput.setName(RAMP_NAME);
		rampLabel.setLabelFor(rampInput);
		rampPanel.add(rampInput, BorderLayout.CENTER);

		threadPropsPanel.add(rampPanel);

		// LOOP COUNT
		threadPropsPanel.add(createControllerPanel());

		// mainPanel.add(threadPropsPanel, BorderLayout.NORTH);
		// add(mainPanel, BorderLayout.CENTER);

		scheduler = new JCheckBox(JMeterUtils.getResString("scheduler"));
		scheduler.addItemListener(this);
		threadPropsPanel.add(scheduler);
		mainPanel = new VerticalPanel();
		mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("scheduler_configuration")));
		mainPanel.add(createStartTimePanel());
		mainPanel.add(createEndTimePanel());
		mainPanel.add(createDurationPanel());
		mainPanel.add(createDelayPanel());
		mainPanel.setVisible(false);
		VerticalPanel intgrationPanel = new VerticalPanel();
		intgrationPanel.add(threadPropsPanel);
		intgrationPanel.add(mainPanel);
		add(intgrationPanel, BorderLayout.CENTER);
	}

	public void setNode(JMeterTreeNode node) {
		getNamePanel().setNode(node);
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
