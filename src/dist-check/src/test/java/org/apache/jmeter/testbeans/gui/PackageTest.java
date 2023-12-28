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

package org.apache.jmeter.testbeans.gui;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassFinder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Find all beans out there and check their resource property files: - Check
 * that non-default property files don't have any extra keys. - Check all
 * necessary properties are defined at least in the default property file,
 * except for beans whose name contains "Experimental" or "Alpha".
 *
 * TODO: - Check property files don't have duplicate keys (is this important)
 *
 */
@Isolated("modifies jmeter locale")
public final class PackageTest extends JMeterTestCase {
    private static final Logger log = LoggerFactory.getLogger(PackageTest.class);

    private static final Locale defaultLocale = new Locale("en","");

    private BeanInfo beanInfo;

    private ResourceBundle bundle;

    public void setUp(Class<?> testBeanClass, Locale testLocale) {
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

    @AfterEach
    public void tearDown() {
        JMeterUtils.setLocale(Locale.getDefault());
    }

    @ParameterizedTest
    @MethodSource("testBeans")
    public void runTest(Class<?> testBeanClass, Locale testLocale, ResourceBundle defaultBundle) {
        setUp(testBeanClass, testLocale);
        if (bundle == defaultBundle) {
            checkAllNecessaryKeysPresent(bundle, defaultBundle);
        } else {
            checkNoInventedKeys(bundle, defaultBundle);
        }
    }

    public void checkNoInventedKeys(ResourceBundle bundle, ResourceBundle defaultBundle) {
        // Check that all keys in the bundle are also in the default bundle:
        for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            defaultBundle.getString(key);
            // Will throw MissingResourceException if key is not there.
        }
    }

    public void checkAllNecessaryKeysPresent(ResourceBundle bundle, ResourceBundle defaultBundle) {
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

    public static Collection<Arguments> testBeans() throws Exception {
        Collection<Arguments> suite = new ArrayList<>();

        @SuppressWarnings("deprecation")
        List<String> testBeanClassNames = ClassFinder.findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[] { TestBean.class });

        JMeterUtils.setLocale(defaultLocale);
        String defaultLocaleId = defaultLocale.toString();
        List<Class<?>> beansWithMissingBundle = new ArrayList<>();
        for (String className : testBeanClassNames) {
            Class<?> testBeanClass = Class.forName(className);
            ResourceBundle defaultBundle;
            try {
                defaultBundle = (ResourceBundle) Introspector.getBeanInfo(testBeanClass).getBeanDescriptor().getValue(
                        GenericTestBeanCustomizer.RESOURCE_BUNDLE);
            } catch (IntrospectionException e) {
                throw new Error("Can't get beanInfo for " + testBeanClass, e);
            }

            if (defaultBundle == null) {
                if (className.startsWith("org.apache.jmeter.examples.")) {
                    log.info("No default bundle found for {}", className);
                    continue;
                }
                beansWithMissingBundle.add(testBeanClass);
                continue;
            }

            suite.add(arguments(testBeanClass, defaultLocale, defaultBundle));

            String[] languages = JMeterMenuBar.getLanguages();
            for (String lang : languages) {
                final String[] language = lang.split("_");
                if (language.length == 1) {
                    Locale locale = new Locale(language[0]);
                    if (locale.toString().equals(defaultLocaleId)) {
                        continue;
                    }
                    suite.add(arguments(testBeanClass, locale, defaultBundle));
                } else if (language.length == 2) {
                    Locale locale = new Locale(language[0], language[1]);
                    if (locale.toString().equals(defaultLocaleId)) {
                        continue;
                    }
                    suite.add(arguments(testBeanClass, locale, defaultBundle));
                }
            }
        }
        if (!beansWithMissingBundle.isEmpty()) {
            fail("Default resource bundle not found for: " + beansWithMissingBundle);
        }
        return suite;
    }
}
