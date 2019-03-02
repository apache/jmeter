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

package org.apache.jmeter.protocol.java.control.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.java.sampler.JUnitSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

/**
 * The <code>JUnitTestSamplerGui</code> class provides the user interface
 * for the {@link JUnitSampler}.
 *
 */
public class JUnitTestSamplerGui extends AbstractSamplerGui
implements ChangeListener, ActionListener, ItemListener
{
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggerFactory.getLogger(JUnitTestSamplerGui.class);

    private static final String TESTMETHOD_PREFIX = "test"; //$NON-NLS-1$

    // Names of JUnit3 methods
    private static final String ONETIMESETUP = "oneTimeSetUp"; //$NON-NLS-1$
    private static final String ONETIMETEARDOWN = "oneTimeTearDown"; //$NON-NLS-1$
    private static final String SUITE = "suite"; //$NON-NLS-1$

    private static final AtomicBoolean IS_INITILIAZED = new AtomicBoolean(Boolean.FALSE);
    private static final String[] SPATHS;

    static {
        String[] paths;
        String ucp = JMeterUtils.getProperty("user.classpath");
        if (ucp!=null){
            String[] parts = ucp.split(File.pathSeparator);
            paths = new String[parts.length+1];
            paths[0] = JMeterUtils.getJMeterHome() + "/lib/junit/"; //$NON-NLS-1$
            System.arraycopy(parts, 0, paths, 1, parts.length);
        } else {
            paths = new String[]{
                JMeterUtils.getJMeterHome() + "/lib/junit/" //$NON-NLS-1$
            };
        }
        SPATHS = paths;
    }

    private JLabeledTextField constructorLabel =
        new JLabeledTextField(
            JMeterUtils.getResString("junit_constructor_string")); //$NON-NLS-1$

    private JLabel methodLabel =
        new JLabel(
            JMeterUtils.getResString("junit_test_method")); //$NON-NLS-1$

    private JLabeledTextField successMsg =
        new JLabeledTextField(
            JMeterUtils.getResString("junit_success_msg")); //$NON-NLS-1$

    private JLabeledTextField failureMsg =
        new JLabeledTextField(
            JMeterUtils.getResString("junit_failure_msg")); //$NON-NLS-1$

    private JLabeledTextField errorMsg =
        new JLabeledTextField(
            JMeterUtils.getResString("junit_error_msg")); //$NON-NLS-1$

    private JLabeledTextField successCode =
        new JLabeledTextField(
            JMeterUtils.getResString("junit_success_code")); //$NON-NLS-1$

    private JLabeledTextField failureCode =
        new JLabeledTextField(
            JMeterUtils.getResString("junit_failure_code")); //$NON-NLS-1$

    private JLabeledTextField errorCode =
        new JLabeledTextField(
            JMeterUtils.getResString("junit_error_code")); //$NON-NLS-1$

    private JLabeledTextField filterpkg =
        new JLabeledTextField(
            JMeterUtils.getResString("junit_pkg_filter")); //$NON-NLS-1$

    private JCheckBox doSetup = new JCheckBox(JMeterUtils.getResString("junit_do_setup_teardown")); //$NON-NLS-1$
    private JCheckBox appendError = new JCheckBox(JMeterUtils.getResString("junit_append_error")); //$NON-NLS-1$
    private JCheckBox appendExc = new JCheckBox(JMeterUtils.getResString("junit_append_exception")); //$NON-NLS-1$
    private JCheckBox junit4 = new JCheckBox(JMeterUtils.getResString("junit_junit4")); //$NON-NLS-1$
    private JCheckBox createInstancePerSample = new JCheckBox(JMeterUtils.getResString("junit_create_instance_per_sample")); //$NON-NLS-1$

    /** A combo box allowing the user to choose a test class. */
    private JComboBox<String> classnameCombo;
    private JComboBox<String> methodName;

    private final transient ClassLoader contextClassLoader =
        Thread.currentThread().getContextClassLoader(); // Potentially expensive; do it once

    private static List<String> annotatedTestClasses;
    private static List<String> junitTestClasses;

    /**
     * Constructor for JUnitTestSamplerGui
     */
    public JUnitTestSamplerGui()
    {
        super();
        init();
    }

    @Override
    public String getLabelResource()
    {
        return "junit_request"; //$NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);


        add(createClassPanel(), BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    private void setupClasslist(boolean initialize){
        classnameCombo.removeAllItems();
        methodName.removeAllItems();
        try
        {
            List<String> classList = new ArrayList<>();
            if(initialize) {
                synchronized (IS_INITILIAZED) {
                    if(IS_INITILIAZED.compareAndSet(false, true)) {
                        annotatedTestClasses = ClassFinder.findAnnotatedClasses(SPATHS,
                            new Class[] {Test.class}, false);
                        junitTestClasses = ClassFinder.findClassesThatExtend(SPATHS,
                             new Class[] { TestCase.class });
                    }
                    if (junit4.isSelected()){
                        classList = annotatedTestClasses;
                    } else {
                        classList = junitTestClasses;
                    }
                }
            }
            
            ClassFilter filter = new ClassFilter();
            filter.setPackges(JOrphanUtils.split(filterpkg.getText(),",")); //$NON-NLS-1$
            // change the classname drop down
            String[] clist = filter.filterArray(classList);
            for (String classStr : clist) {
                classnameCombo.addItem(classStr);
            }
        }
        catch (IOException e)
        {
            log.error("Exception getting interfaces.", e);
        }
    }

    private JPanel createClassPanel()
    {
        JLabel label =
            new JLabel(JMeterUtils.getResString("protocol_java_classname")); //$NON-NLS-1$

        classnameCombo = new JComboBox<>();
        classnameCombo.addActionListener(this);
        classnameCombo.setEditable(false);
        label.setLabelFor(classnameCombo);

        methodName = new JComboBox<>();
        methodName.addActionListener(this);
        methodLabel.setLabelFor(methodName);

        setupClasslist(false);

        VerticalPanel panel = new VerticalPanel();
        panel.add(junit4);
        junit4.addItemListener(this);
        panel.add(filterpkg);
        filterpkg.addChangeListener(this);

        panel.add(label);
        panel.add(classnameCombo);

        constructorLabel.setText("");
        panel.add(constructorLabel);
        panel.add(methodLabel);
        panel.add(methodName);

        panel.add(successMsg);
        panel.add(successCode);
        panel.add(failureMsg);
        panel.add(failureCode);
        panel.add(errorMsg);
        panel.add(errorCode);
        panel.add(doSetup);
        panel.add(appendError);
        panel.add(appendExc);
        panel.add(createInstancePerSample);
        return panel;
    }

    private void initGui(){
        appendError.setSelected(false);
        appendExc.setSelected(false);
        createInstancePerSample.setSelected(false);
        doSetup.setSelected(false);
        junit4.setSelected(false);
        filterpkg.setText(""); //$NON-NLS-1$
        constructorLabel.setText(""); //$NON-NLS-1$
        successCode.setText(JMeterUtils.getResString("junit_success_default_code")); //$NON-NLS-1$
        successMsg.setText(JMeterUtils.getResString("junit_success_default_msg")); //$NON-NLS-1$
        failureCode.setText(JMeterUtils.getResString("junit_failure_default_code")); //$NON-NLS-1$
        failureMsg.setText(JMeterUtils.getResString("junit_failure_default_msg")); //$NON-NLS-1$
        errorMsg.setText(JMeterUtils.getResString("junit_error_default_msg")); //$NON-NLS-1$
        errorCode.setText(JMeterUtils.getResString("junit_error_default_code")); //$NON-NLS-1$
        setupClasslist(true);
    }

    /** {@inheritDoc} */
    @Override
    public void clearGui() {
        super.clearGui();
        initGui();
    }

    /** {@inheritDoc} */
    @Override
    public TestElement createTestElement()
    {
        JUnitSampler sampler = new JUnitSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /** {@inheritDoc} */
    @Override
    public void modifyTestElement(TestElement el)
    {
        JUnitSampler sampler = (JUnitSampler)el;
        configureTestElement(sampler);
        if (classnameCombo.getSelectedItem() != null &&
                classnameCombo.getSelectedItem() instanceof String) {
            sampler.setClassname((String)classnameCombo.getSelectedItem());
        } else {
            sampler.setClassname(null);
        }
        sampler.setConstructorString(constructorLabel.getText());
        if (methodName.getSelectedItem() != null) {
            Object mobj = methodName.getSelectedItem();
            sampler.setMethod((String)mobj);
        } else {
            sampler.setMethod(null);
        }
        sampler.setFilterString(filterpkg.getText());
        sampler.setSuccess(successMsg.getText());
        sampler.setSuccessCode(successCode.getText());
        sampler.setFailure(failureMsg.getText());
        sampler.setFailureCode(failureCode.getText());
        sampler.setError(errorMsg.getText());
        sampler.setErrorCode(errorCode.getText());
        sampler.setDoNotSetUpTearDown(doSetup.isSelected());
        sampler.setAppendError(appendError.isSelected());
        sampler.setAppendException(appendExc.isSelected());
        sampler.setCreateOneInstancePerSample(createInstancePerSample.isSelected());
        sampler.setJunit4(junit4.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public void configure(TestElement el)
    {
        super.configure(el);
        JUnitSampler sampler = (JUnitSampler)el;
        junit4.setSelected(sampler.getJunit4());
        filterpkg.setText(sampler.getFilterString());
        classnameCombo.setSelectedItem(sampler.getClassname());
        setupMethods();
        methodName.setSelectedItem(sampler.getMethod());
        constructorLabel.setText(sampler.getConstructorString());
        if (sampler.getSuccessCode().length() > 0) {
            successCode.setText(sampler.getSuccessCode());
        } else {
            successCode.setText(JMeterUtils.getResString("junit_success_default_code")); //$NON-NLS-1$
        }
        if (sampler.getSuccess().length() > 0) {
            successMsg.setText(sampler.getSuccess());
        } else {
            successMsg.setText(JMeterUtils.getResString("junit_success_default_msg")); //$NON-NLS-1$
        }
        if (sampler.getFailureCode().length() > 0) {
            failureCode.setText(sampler.getFailureCode());
        } else {
            failureCode.setText(JMeterUtils.getResString("junit_failure_default_code")); //$NON-NLS-1$
        }
        if (sampler.getFailure().length() > 0) {
            failureMsg.setText(sampler.getFailure());
        } else {
            failureMsg.setText(JMeterUtils.getResString("junit_failure_default_msg")); //$NON-NLS-1$
        }
        if (sampler.getError().length() > 0) {
            errorMsg.setText(sampler.getError());
        } else {
            errorMsg.setText(JMeterUtils.getResString("junit_error_default_msg")); //$NON-NLS-1$
        }
        if (sampler.getErrorCode().length() > 0) {
            errorCode.setText(sampler.getErrorCode());
        } else {
            errorCode.setText(JMeterUtils.getResString("junit_error_default_code")); //$NON-NLS-1$
        }
        doSetup.setSelected(sampler.getDoNotSetUpTearDown());
        appendError.setSelected(sampler.getAppendError());
        appendExc.setSelected(sampler.getAppendException());
        createInstancePerSample.setSelected(sampler.getCreateOneInstancePerSample());
    }

    private void setupMethods(){
        String className = (String) classnameCombo.getSelectedItem();
        methodName.removeAllItems();
        if (className != null) {
            try {
                // Don't instantiate class
                Class<?> testClass = Class.forName(className, false, contextClassLoader);
                String [] names = getMethodNames(testClass);
                for (String name : names) {
                    methodName.addItem(name);
                }
                methodName.repaint();
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private String[] getMethodNames(Class<?> clazz)
    {
        Method[] meths = clazz.getMethods();
        List<String> list = new ArrayList<>();
        for (final Method method : meths) {
            final String name = method.getName();
            if (junit4.isSelected()) {
                if (method.isAnnotationPresent(Test.class) ||
                        method.isAnnotationPresent(BeforeClass.class) ||
                        method.isAnnotationPresent(AfterClass.class)) {
                    list.add(name);
                }
            } else {
                if (name.startsWith(TESTMETHOD_PREFIX) ||
                        name.equals(ONETIMESETUP) ||
                        name.equals(ONETIMETEARDOWN) ||
                        name.equals(SUITE)) {
                    list.add(name);
                }
            }
        }
        if (!list.isEmpty()){
            return list.toArray(new String[list.size()]);
        }
        return new String[0];
    }

    /**
     * Handle action events for this component.  This method currently handles
     * events for the classname combo box, and sets up the associated method names.
     *
     * @param evt  the ActionEvent to be handled
     */
    @Override
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == classnameCombo)
        {
            setupMethods();
        }
    }

    /**
     * Handle change events: currently handles events for the JUnit4
     * checkbox, and sets up the relevant class names.
     */
    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.getItem() == junit4){
            setupClasslist(true);
        }
    }

    /**
     * the current implementation checks to see if the source
     * of the event is the filterpkg field.
     */
    @Override
    public void stateChanged(ChangeEvent event) {
        if ( event.getSource() == filterpkg) {
            setupClasslist(true);
        }
    }
}

