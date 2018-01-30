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
 */
package org.apache.jmeter.testbeans.gui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.junit.JMeterTestCaseJUnit;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Test;
import junit.framework.TestSuite;

/*
 * Find all beans out there and check their resource property files: - Check
 * that non-default property files don't have any extra keys. - Check all
 * necessary properties are defined at least in the default property file,
 * except for beans whose name contains "Experimental" or "Alpha". 
 * 
 * TODO: - Check property files don't have duplicate keys (is this important)
 * 
 */
public final class PackageTest extends JMeterTestCaseJUnit {
    private static final Logger log = LoggerFactory.getLogger(PackageTest.class);

    private static final Locale defaultLocale = new Locale("en","");

    private final ResourceBundle defaultBundle;

    private final Class<?> testBeanClass;

    private final Locale testLocale;

    private PackageTest(Class<?> testBeanClass, Locale locale, ResourceBundle defaultBundle) {
        super(testBeanClass.getName() + " - " + locale.getLanguage() + " - " + locale.getCountry());
        this.testBeanClass = testBeanClass;
        this.testLocale = locale;
        this.defaultBundle = defaultBundle;
    }

    private PackageTest(String name){
        super(name);
        this.testBeanClass = null;
        this.testLocale = null;
        this.defaultBundle = null;
    }
    
    private BeanInfo beanInfo;

    private ResourceBundle bundle;

    @Override
    public void setUp() {
        if (testLocale == null) {
            return;
        }
        JMeterUtils.setLocale(testLocale);
        Introspector.flushFromCaches(testBeanClass);
        try {
            beanInfo = Introspector.getBeanInfo(testBeanClass);
            bundle = (ResourceBundle) beanInfo.getBeanDescriptor().getValue(GenericTestBeanCustomizer.RESOURCE_BUNDLE);
        } catch (IntrospectionException e) {
            log.error("Can't get beanInfo for {}", testBeanClass, e);
            throw new Error(e.toString(), e); // Programming error. Don't continue.
        }
        if (bundle == null) {
            throw new Error("This can't happen!");
        }
    }

    @Override
    public void tearDown() {
        JMeterUtils.setLocale(Locale.getDefault());
    }

    @Override
    public void runTest() throws Throwable {
        if (testLocale == null) {
            super.runTest();
            return;
        }
        if (bundle == defaultBundle) {
            checkAllNecessaryKeysPresent();
        } else {
            checkNoInventedKeys();
        }
    }

    public void checkNoInventedKeys() {
        // Check that all keys in the bundle are also in the default bundle:
        for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            defaultBundle.getString(key);
            // Will throw MissingResourceException if key is not there.
        }
    }

    public void checkAllNecessaryKeysPresent() {
        // Check that all necessary keys are there:

        // displayName is always mandatory:
        String dn = defaultBundle.getString("displayName").toUpperCase(Locale.ENGLISH);

        // Skip the rest of this test for alpha/experimental beans:
        if (dn.contains("(ALPHA") || dn.contains("(EXPERIMENTAL")) {
            return;
        }

        // Check for property- and group-related texts:
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            // Skip non-editable properties, that is:
            // Ignore hidden, read-only, and write-only properties
            if (descriptor.isHidden() || descriptor.getReadMethod() == null
                    || descriptor.getWriteMethod() == null) {
                continue;
            }
            // Ignore TestElement properties which don't have an explicit
            // editor:
            if (TestElement.class.isAssignableFrom(descriptor.getPropertyType())
                    && descriptor.getPropertyEditorClass() == null) {
                continue;
            }
            // Done -- we're working with an editable property.

            String name = descriptor.getName();

            bundle.getString(name + ".displayName");

            String group = (String) descriptor.getValue(GenericTestBeanCustomizer.GROUP);
            if (group != null) {
                bundle.getString(group + ".displayName");
            }
        }
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite("Bean Resource Test Suite");

        List<String> testBeanClassNames = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[] { TestBean.class });

        boolean errorDetected = false;
        JMeterUtils.setLocale(defaultLocale);
        for (String className : testBeanClassNames) {
            Class<?> testBeanClass = Class.forName(className);
            ResourceBundle defaultBundle;
            try {
                defaultBundle = (ResourceBundle) Introspector.getBeanInfo(testBeanClass).getBeanDescriptor().getValue(
                        GenericTestBeanCustomizer.RESOURCE_BUNDLE);
            } catch (IntrospectionException e) {
                log.error("Can't get beanInfo for {}", testBeanClass, e);
                throw new Error(e.toString(), e); // Programming error. Don't continue.
            }

            if (defaultBundle == null) {
                if (className.startsWith("org.apache.jmeter.examples.")) {
                    log.info("No default bundle found for {}", className);
                    continue;
                }
                errorDetected=true;
                log.error("No default bundle found for {} using {}", className, defaultLocale);
                continue;
            }

            suite.addTest(new PackageTest(testBeanClass, defaultLocale, defaultBundle));

            String[] languages = JMeterMenuBar.getLanguages();
            for (String lang : languages) {
                final String[] language = lang.split("_");
                if (language.length == 1){
                    suite.addTest(new PackageTest(testBeanClass, new Locale(language[0]), defaultBundle));                                    
                } else if (language.length == 2){
                    suite.addTest(new PackageTest(testBeanClass, new Locale(language[0], language[1]), defaultBundle));                                                        
                }
            }
        }

        if (errorDetected)
        {
            suite.addTest(new PackageTest("errorDetected"));
        }
        return suite;
    }

    public void errorDetected(){
        fail("One or more errors detected - see log file");
    }
}
