// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.java.config.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.protocol.java.config.JavaConfig;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The <code>JavaConfigGui</code> class provides the user interface for the
 * {@link JavaConfig} object.
 * 
 * @version $Revision$ on $Date$
 */
public class JavaConfigGui extends AbstractConfigGui implements ActionListener {
	/** Logging */
	private static transient Logger log = LoggingManager.getLoggerForClass();

	/** The name of the classnameCombo JComboBox */
	private static final String CLASSNAMECOMBO = "classnamecombo";

	/** A combo box allowing the user to choose a test class. */
	private JComboBox classnameCombo;

	/**
	 * Indicates whether or not the name of this component should be displayed
	 * as part of the GUI. If true, this is a standalone component. If false, it
	 * is embedded in some other component.
	 */
	private boolean displayName = true;

	/** A panel allowing the user to set arguments for this test. */
	private ArgumentsPanel argsPanel;

	/**
	 * Create a new JavaConfigGui as a standalone component.
	 */
	public JavaConfigGui() {
		this(true);
	}

	/**
	 * Create a new JavaConfigGui as either a standalone or an embedded
	 * component.
	 * 
	 * @param displayNameField
	 *            tells whether the component name should be displayed with the
	 *            GUI. If true, this is a standalone component. If false, this
	 *            component is embedded in some other component.
	 */
	public JavaConfigGui(boolean displayNameField) {
		this.displayName = displayNameField;
		init();
	}

	public String getLabelResource() {
		return "java_request_defaults";
	}

	/**
	 * Initialize the GUI components and layout.
	 */
	protected void init() {
		setLayout(new BorderLayout(0, 5));

		if (displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
		}

		JPanel classnameRequestPanel = new JPanel(new BorderLayout(0, 5));
		classnameRequestPanel.add(createClassnamePanel(), BorderLayout.NORTH);
		classnameRequestPanel.add(createParameterPanel(), BorderLayout.CENTER);

		add(classnameRequestPanel, BorderLayout.CENTER);
	}

	/**
	 * Create a panel with GUI components allowing the user to select a test
	 * class.
	 * 
	 * @return a panel containing the relevant components
	 */
	private JPanel createClassnamePanel() {
		List possibleClasses = null;

		try {
			// Find all the classes which implement the JavaSamplerClient
			// interface.
			possibleClasses = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(),
					new Class[] { JavaSamplerClient.class });

			// Remove the JavaConfig class from the list since it only
			// implements the interface for error conditions.

			possibleClasses.remove(JavaSampler.class.getName() + "$ErrorSamplerClient");
		} catch (Exception e) {
			log.debug("Exception getting interfaces.", e);
		}

		JLabel label = new JLabel(JMeterUtils.getResString("protocol_java_classname"));

		classnameCombo = new JComboBox(possibleClasses.toArray());
		classnameCombo.addActionListener(this);
		classnameCombo.setName(CLASSNAMECOMBO);
		classnameCombo.setEditable(false);
		label.setLabelFor(classnameCombo);

		HorizontalPanel panel = new HorizontalPanel();
		panel.add(label);
		panel.add(classnameCombo);

		return panel;
	}

	/**
	 * Handle action events for this component. This method currently handles
	 * events for the classname combo box.
	 * 
	 * @param evt
	 *            the ActionEvent to be handled
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == classnameCombo) {
			String className = ((String) classnameCombo.getSelectedItem()).trim();
			try {
				JavaSamplerClient client = (JavaSamplerClient) Class.forName(className, true,
						Thread.currentThread().getContextClassLoader()).newInstance();

				Arguments currArgs = new Arguments();
				argsPanel.modifyTestElement(currArgs);
				Map currArgsMap = currArgs.getArgumentsAsMap();

				Arguments newArgs = new Arguments();
				Arguments testParams = null;
				try {
					testParams = client.getDefaultParameters();
				} catch (AbstractMethodError e) {
					log.warn("JavaSamplerClient doesn't implement "
							+ "getDefaultParameters.  Default parameters won't "
							+ "be shown.  Please update your client class: " + className);
				}

				if (testParams != null) {
					PropertyIterator i = testParams.getArguments().iterator();
					while (i.hasNext()) {
						Argument arg = (Argument) i.next().getObjectValue();
						String name = arg.getName();
						String value = arg.getValue();

						// If a user has set parameters in one test, and then
						// selects a different test which supports the same
						// parameters, those parameters should have the same
						// values that they did in the original test.
						if (currArgsMap.containsKey(name)) {
							String newVal = (String) currArgsMap.get(name);
							if (newVal != null && newVal.length() > 0) {
								value = newVal;
							}
						}
						newArgs.addArgument(name, value);
					}
				}

				argsPanel.configure(newArgs);
			} catch (Exception e) {
				log.error("Error getting argument list for " + className, e);
			}
		}
	}

	/**
	 * Create a panel containing components allowing the user to provide
	 * arguments to be passed to the test class instance.
	 * 
	 * @return a panel containing the relevant components
	 */
	private JPanel createParameterPanel() {
		argsPanel = new ArgumentsPanel(JMeterUtils.getResString("paramtable"));
		return argsPanel;
	}

	/* Overrides AbstractJMeterGuiComponent.configure(TestElement) */
	public void configure(TestElement config) {
		super.configure(config);

		argsPanel.configure((Arguments) config.getProperty(JavaSampler.ARGUMENTS).getObjectValue());

		classnameCombo.setSelectedItem(config.getPropertyAsString(JavaSampler.CLASSNAME));
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		JavaConfig config = new JavaConfig();
		modifyTestElement(config);
		return config;
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	public void modifyTestElement(TestElement config) {
		configureTestElement(config);
		((JavaConfig) config).setArguments((Arguments) argsPanel.createTestElement());
		((JavaConfig) config).setClassname(classnameCombo.getSelectedItem().toString());
	}
}
