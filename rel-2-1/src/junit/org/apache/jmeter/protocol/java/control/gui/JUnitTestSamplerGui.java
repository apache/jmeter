/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.java.control.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import junit.framework.TestCase;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.java.sampler.JUnitSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * The <code>JUnitTestSamplerGui</code> class provides the user interface
 * for the {@link JUnitTestSampler}.
 * 
 * @version $Revision$ on $Date$
 */
public class JUnitTestSamplerGui extends AbstractSamplerGui 
implements ChangeListener, ActionListener
{
    private static final Logger log = LoggingManager.getLoggerForClass();

    /** The name of the classnameCombo JComboBox */
    private static final String CLASSNAMECOMBO = "classnamecombo"; //$NON-NLS-1$
    private static final String METHODCOMBO = "methodcombo"; //$NON-NLS-1$
    private static final String PREFIX = "test"; //$NON-NLS-1$
    
    private static final String ONETIMESETUP = "oneTimeSetUp"; //$NON-NLS-1$
    private static final String ONETIMETEARDOWN = "oneTimeTearDown"; //$NON-NLS-1$
    private static final String SUITE = "suite"; //$NON-NLS-1$
    
    private static final String[] SPATHS = new String[] {
            JMeterUtils.getJMeterHome() + "/lib/junit/", //$NON-NLS-1$
    };

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
    
    /** A combo box allowing the user to choose a test class. */
    private JComboBox classnameCombo;
    private JComboBox methodName;
    private TestCase TESTCLASS = null;
    private List METHODLIST = null;
    
    // TODO: make private?
    protected ClassFilter FILTER = new ClassFilter();
    protected List CLASSLIST = null;
    
    /**
     * Constructor for JUnitTestSamplerGui
     */
    public JUnitTestSamplerGui()
    {
        super();
        init();
    }

    public String getLabelResource()
    {
        return "junit_request"; //$NON-NLS-1$
    }

    public String getDocAnchor() {
        return "JUnit_Sampler";
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        
        add(makeTitlePanel(), BorderLayout.NORTH);


        add(createClassPanel(), BorderLayout.CENTER);
    }
    
    private JPanel createClassPanel()
    {
        METHODLIST = new java.util.ArrayList();

        try
        {
            // Find all the classes which extend junit.framework.TestCase
            CLASSLIST =
                ClassFinder.findClassesThatExtend(
                    SPATHS,
                    new Class[] { TestCase.class });
        }
        catch (IOException e)
        {
            log.error("Exception getting interfaces.", e);
        } 
        catch (ClassNotFoundException e) {
            log.error("Exception getting interfaces.", e);
        }

        JLabel label =
            new JLabel(JMeterUtils.getResString("protocol_java_classname"));

        classnameCombo = new JComboBox(CLASSLIST.toArray());
        classnameCombo.addActionListener(this);
        classnameCombo.setName(CLASSNAMECOMBO);
        classnameCombo.setEditable(false);
        label.setLabelFor(classnameCombo);
        
        if (FILTER != null && FILTER.size() > 0) {
            methodName = new JComboBox(FILTER.filterArray(METHODLIST));
        } else {
            methodName = new JComboBox(METHODLIST.toArray());
        }
        methodName.addActionListener(this);
        methodName.setName(METHODCOMBO);
        methodLabel.setLabelFor(methodName);
        
        VerticalPanel panel = new VerticalPanel();
        panel.add(filterpkg);
        panel.add(label);
        filterpkg.addChangeListener(this);

        if (classnameCombo != null){
            panel.add(classnameCombo);
        }
        constructorLabel.setText("");
        panel.add(constructorLabel);
        panel.add(methodLabel);
        if (methodName != null){
            panel.add(methodName);
        }
        panel.add(successMsg);
        panel.add(successCode);
        panel.add(failureMsg);
        panel.add(failureCode);
        panel.add(errorMsg);
        panel.add(errorCode);
        panel.add(doSetup);
        panel.add(appendError);
        panel.add(appendExc);
        return panel;
    }

    
    /* Implements JMeterGuiComponent.createTestElement() */ 
    public TestElement createTestElement()
    {
        JUnitSampler sampler = new JUnitSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /* Implements JMeterGuiComponent.modifyTestElement(TestElement) */
    public void modifyTestElement(TestElement el)
    {
        JUnitSampler sampler = (JUnitSampler)el;
        configureTestElement(sampler);
        if (classnameCombo.getSelectedItem() != null && 
                classnameCombo.getSelectedItem() instanceof String) {
            sampler.setClassname((String)classnameCombo.getSelectedItem());
        }
        sampler.setConstructorString(constructorLabel.getText());
        if (methodName.getSelectedItem() != null) {
            Object mobj = methodName.getSelectedItem();
            sampler.setMethod((String)mobj);
        }
        sampler.setFilterString(filterpkg.getText());
        sampler.setSuccess(successMsg.getText());
        sampler.setSuccessCode(successCode.getText());
        sampler.setFailure(failureMsg.getText());
        sampler.setFailureCode(failureCode.getText());
        sampler.setDoNotSetUpTearDown(doSetup.isSelected());
        sampler.setAppendError(appendError.isSelected());
        sampler.setAppendException(appendExc.isSelected());
    }

    /* Overrides AbstractJMeterGuiComponent.configure(TestElement) */
    public void configure(TestElement el)
    {
        super.configure(el);
        JUnitSampler sampler = (JUnitSampler)el;
        classnameCombo.setSelectedItem(sampler.getClassname());
        instantiateClass();
        methodName.setSelectedItem(sampler.getMethod());
        filterpkg.setText(sampler.getFilterString());
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
    }
    
    public void instantiateClass(){
        String className =
            ((String) classnameCombo.getSelectedItem());
        if (className != null) {
            TESTCLASS = (TestCase)JUnitSampler.getClassInstance(className,
                    constructorLabel.getText());
            if (TESTCLASS == null) {
                clearMethodCombo();
            }
            configureMethodCombo();
        }
    }

    public void showErrorDialog() {
        JOptionPane.showConfirmDialog(this, 
                JMeterUtils.getResString("junit_constructor_error"),  //$NON-NLS-1$
                "Warning",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    }
    
    public void configureMethodCombo(){
        if (TESTCLASS != null) {
            clearMethodCombo();
            String [] names = getMethodNames(getMethods(TESTCLASS,METHODLIST));
            for (int idx=0; idx < names.length; idx++){
                methodName.addItem(names[idx]);
                METHODLIST.add(names[idx]);
            }
            methodName.repaint();
        }
    }
    
    public void clearMethodCombo(){
        methodName.removeAllItems();
        METHODLIST.clear();
    }
    
    public Method[] getMethods(Object obj, List list)
    {
        Method[] meths = obj.getClass().getMethods();
        for (int idx=0; idx < meths.length; idx++){
            if (meths[idx].getName().startsWith(PREFIX) ||
                    meths[idx].getName().equals(ONETIMESETUP) ||
                    meths[idx].getName().equals(ONETIMETEARDOWN) ||
                    meths[idx].getName().equals(SUITE)) {
                list.add(meths[idx]);
            }
        }
        if (list.size() > 0){
            Method[] rmeth = new Method[list.size()];
            return (Method[])list.toArray(rmeth);
        }
		return new Method[0];
    }
    
    public String[] getMethodNames(Method[] meths)
    {
        String[] names = new String[meths.length];
        for (int idx=0; idx < meths.length; idx++){
            names[idx] = meths[idx].getName();
        }
        return names;
    }
    
    public Class[] filterClasses(Class[] clz) {
        if (clz != null && clz.length > 0){
            Class[] nclz = null;
            return nclz;
        }
		return clz;
    }
  
    /**
     * Handle action events for this component.  This method currently handles
     * events for the classname combo box.
     * 
     * @param evt  the ActionEvent to be handled
     */
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == classnameCombo)
        {
            instantiateClass();
        }
    }
    
    /**
     * the current implementation checks to see if the source
     * of the event is the filterpkg field.
     */
    public void stateChanged(ChangeEvent event) {
        if ( event.getSource() == filterpkg) {
            FILTER.setPackges(JOrphanUtils.split(filterpkg.getText(),",")); //$NON-NLS-1$
            classnameCombo.removeAllItems();
            // change the classname drop down
            Object[] clist = FILTER.filterArray(CLASSLIST);
            for (int idx=0; idx < clist.length; idx++) {
                classnameCombo.addItem(clist[idx]);
            }
        }
    }
}

