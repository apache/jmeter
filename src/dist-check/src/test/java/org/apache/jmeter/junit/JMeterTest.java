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

package org.apache.jmeter.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.gui.ObsoleteGui;
import org.apache.jmeter.control.IfControllerSchema;
import org.apache.jmeter.control.LoopControllerSchema;
import org.apache.jmeter.control.gui.TestFragmentControllerGui;
import org.apache.jmeter.dsl.DslPrinterTraverser;
import org.apache.jmeter.extractor.RegexExtractorSchema;
import org.apache.jmeter.gui.GuiComponentHolder;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.NamePanel;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.loadsave.IsEnabledNormalizer;
import org.apache.jmeter.protocol.http.control.gui.AjpSamplerGui;
import org.apache.jmeter.protocol.http.control.gui.GraphQLHTTPSamplerGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema;
import org.apache.jmeter.protocol.java.config.gui.JavaConfigGui;
import org.apache.jmeter.protocol.java.control.gui.JUnitTestSamplerGui;
import org.apache.jmeter.protocol.java.control.gui.JavaTestSamplerGui;
import org.apache.jmeter.protocol.jms.control.gui.JMSSamplerGui;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.AbstractScopedTestElementSchema;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestElementSchema;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.schema.CollectionPropertyDescriptor;
import org.apache.jmeter.testelement.schema.PropertyDescriptor;
import org.apache.jmeter.testelement.schema.TestElementPropertyDescriptor;
import org.apache.jmeter.threads.ThreadGroupSchema;
import org.apache.jmeter.threads.gui.PostThreadGroupGui;
import org.apache.jmeter.threads.gui.SetupThreadGroupGui;
import org.apache.jmeter.threads.openmodel.gui.OpenModelThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.backend.BackendListenerGui;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.jorphan.util.JOrphanUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Isolated("changes default locale")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // maps must persist across test method executions
public class JMeterTest extends JMeterTestCase {
    private static final Logger log = LoggerFactory.getLogger(JMeterTest.class);

    private static Map<String, Boolean> guiTitles;

    private static Map<String, Boolean> guiTags;

    private static Properties nameMap;

    private static final Locale TEST_LOCALE = Locale.ENGLISH;

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private static volatile boolean classPathShown = false;// Only show classpath once

    @BeforeAll
    public static void setLocale() {
        JMeterUtils.setLocale(TEST_LOCALE);
        Locale.setDefault(TEST_LOCALE);
    }

    // Restore the original Locale
    @AfterAll
    public static void resetLocale() {
        JMeterUtils.setLocale(DEFAULT_LOCALE);
        Locale.setDefault(DEFAULT_LOCALE);
    }

    /*
     * Extract titles from component_reference.xml
     */
    @BeforeAll
    public static void createTitleSet() throws Exception {
        guiTitles = new HashMap<>(90);

        String compref = "../xdocs/usermanual/component_reference.xml";
        try (InputStream stream = new FileInputStream(findTestFile(compref))) {
            org.w3c.dom.Element body = getBodyFromXMLDocument(stream);
            NodeList sections = body.getElementsByTagName("section");
            for (int i = 0; i < sections.getLength(); i++) {
                org.w3c.dom.Element section = (org.w3c.dom.Element) sections.item(i);
                NodeList components = section.getElementsByTagName("component");
                for (int j = 0; j < components.getLength(); j++) {
                    org.w3c.dom.Element comp = (org.w3c.dom.Element)
                            components.item(j);
                    String nm = comp.getAttribute("name");
                    if (!nm.equals("SSL Manager")) {// Not a true GUI component
                        guiTitles.put(nm.replace(' ', '_'), Boolean.FALSE);
                    }
                }
            }
        }
        // Add titles that don't need to be documented
        guiTitles.put("Example Sampler", Boolean.FALSE);
    }

    /**
     * @return first element named {@code body}
     * @throws ParserConfigurationException when stream contains invalid XML
     * @throws IOException when stream can not be read
     * @throws SAXException in case of XML parsing error
     */
    private static org.w3c.dom.Element getBodyFromXMLDocument(InputStream stream)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(stream));
        org.w3c.dom.Element root = doc.getDocumentElement();
        org.w3c.dom.Element body = (org.w3c.dom.Element) root.getElementsByTagName("body").item(0);
        return body;
    }

    /*
     * Extract titles from component_reference.xml
     */
    @BeforeAll
    public static void createTagSet() throws Exception {
        guiTags = new HashMap<>(90);

        String compref = "../xdocs/usermanual/component_reference.xml";
        try (InputStream stream = new FileInputStream(findTestFile(compref))) {
            org.w3c.dom.Element body = getBodyFromXMLDocument(stream);
            NodeList sections = body.getElementsByTagName("section");

            for (int i = 0; i < sections.getLength(); i++) {
                org.w3c.dom.Element section = (org.w3c.dom.Element) sections.item(i);
                NodeList components = section.getElementsByTagName("component");
                for (int j = 0; j < components.getLength(); j++) {
                    org.w3c.dom.Element comp = (org.w3c.dom.Element)
                            components.item(j);
                    String tag = comp.getAttribute("tag");
                    if (!StringUtils.isEmpty(tag)){
                        guiTags.put(tag, Boolean.FALSE);
                    }
                }
            }
        }
    }

    public static<T> List<T> keysWithFalseValues(Map<? extends T, Boolean> map) {
        return map.entrySet().stream()
                .filter(e -> !e.getValue().equals(Boolean.TRUE))
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    @AfterAll
    public static void checkGuiSet() {
        guiTitles.remove("Example Sampler");// We don't mind if this is left over
        guiTitles.remove("Sample_Result_Save_Configuration");// Ditto, not a sampler
        assertEquals(
                "[]",
                keysWithFalseValues(guiTitles).toString(),
                () -> "Should not have any names left over in guiTitles map, check name of components in EN (default) Locale, "
                        + "which must match name attribute of component, check java.awt.HeadlessException errors before,"
                        + " we are running with '-Djava.awt.headless="
                        + System.getProperty("java.awt.headless") + "'");
    }

    static Collection<GuiComponentHolder> customGuiComponents() throws Throwable {
        List<GuiComponentHolder> components = new ArrayList<>();
        for (Object o : getObjects(JMeterGUIComponent.class)) {
            JMeterGUIComponent item = (JMeterGUIComponent) o;
            if (item.getClass() == TestBeanGUI.class) {
                continue;
            }
            if (item instanceof JMeterTreeNode) {
                System.out.println("o.a.j.junit.JMeterTest INFO: JMeterGUIComponent: skipping all tests  " + item.getClass().getName());
                continue;
            }
            if (item instanceof ObsoleteGui) {
                continue;
            }
            components.add(new GuiComponentHolder(item));
        }
        return components;
    }

    /*
     * Test GUI elements - create the suite of tests
     */
    static Collection<GuiComponentHolder> guiComponents() throws Throwable {
        List<GuiComponentHolder> components = new ArrayList<>(customGuiComponents());
        for (Object o : getObjects(TestBean.class)) {
            Class<?> c = o.getClass();
            JMeterGUIComponent item = new TestBeanGUI(c);
            components.add(new GuiComponentHolder(item));
        }
        return components;
    }

    /*
     * Test GUI elements - run the test
     */
    @ParameterizedTest
    @MethodSource("guiComponents")
    public void runGUITitle(GuiComponentHolder componentHolder) throws Exception {
        JMeterGUIComponent guiItem = componentHolder.getComponent();
        if (!guiTitles.isEmpty()) {
            String title = guiItem.getDocAnchor();
            boolean ct = guiTitles.containsKey(title);
            if (ct) {
                guiTitles.put(title, Boolean.TRUE);// So we can detect extra entries
            }
            String name = guiItem.getClass().getName();
            if (// Is this a work in progress or an internal GUI component?
                title != null && !title.isEmpty() // Will be "" for internal components
                && !title.toUpperCase(Locale.ENGLISH).contains("(ALPHA")
                && !title.toUpperCase(Locale.ENGLISH).contains("(BETA")
                && !title.toUpperCase(Locale.ENGLISH).contains("(DEPRECATED")
                && !title.matches("Example\\d+") // Skip the example samplers ...
                && !name.startsWith("org.apache.jmeter.examples."))
            {// No, not a work in progress ...
                String s = "component_reference.xml needs '" + title + "' anchor for " + name;
                if (!ct) {
                    log.warn(s); // Record in log as well
                }
                assertTrue(ct, s);
            }
        }
    }

    /*
     * Test GUI elements - run for all components
     */
    @ParameterizedTest
    @MethodSource("guiComponents")
    public void GUIComponents1(GuiComponentHolder componentHolder) throws Exception {
        JMeterGUIComponent guiItem = componentHolder.getComponent();
        String name = componentHolder.toString();

        if (guiItem.getClass().getName().startsWith("org.apache.jmeter.examples.")){
            return;
        }
        if (guiItem.getClass() != TestBeanGUI.class) {
            try {
                String label = guiItem.getLabelResource();
                assertNotNull(label, () -> "Label should not be null for " + name);
                assertFalse(label.isEmpty(), () -> "Label should not be empty for " + name);
                assertFalse(JMeterUtils.getResString(
                        label).startsWith(JMeterUtils.RES_KEY_PFX), () -> "'" + label + "' should be in resource file for " + name);
            } catch (UnsupportedOperationException uoe) {
                log.warn("Class has not yet implemented getLabelResource {}", name);
            }
        }
        checkElementAlias(guiItem);
    }

    /*
     * Test GUI elements - not run for TestBeanGui items
     */
    @ParameterizedTest
    @MethodSource("guiComponents")
    public void GUIComponents2(GuiComponentHolder componentHolder) throws Exception {
        JMeterGUIComponent guiItem = componentHolder.getComponent();
        String name = guiItem.getClass().getName();

        // TODO these assertions should be separate tests

        TestElement el = guiItem.createTestElement();
        assertNotNull(el, name + ".createTestElement should be non-null ");
        assertEquals(name, el.getPropertyAsString(TestElement.GUI_CLASS), "GUI-CLASS: Failed on " + name);

        assertEquals(guiItem.getName(), el.getName(), () -> "NAME: Failed on " + name);
        if (StringUtils.isEmpty(el.getName())) {
            fail("Name of the element must not be blank. Gui class " + name + ", element class " + el.getClass().getName());
        }
        assertEquals(el.getClass().getName(), el
                .getPropertyAsString(TestElement.TEST_CLASS), "TEST-CLASS: Failed on " + name);
        if (guiItem.getClass() != TestFragmentControllerGui.class) {
            assertTrue(el.isEnabled(), "Should be enabled by default: " + name);
        }
        TestElement el2 = guiItem.createTestElement();
        el.setName("hey, new name!:");
        el.setProperty("NOT", "Shouldn't be here");
        if (!(guiItem instanceof UnsharedComponent)) {
            assertEquals("", el2.getPropertyAsString("NOT"), () -> "SHARED: Failed on " + name);
        }
        log.debug("Saving element: {}", el.getClass());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SaveService.saveElement(el, bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        bos.close();
        el = (TestElement) SaveService.loadElement(bis);
        bis.close();
        assertNotNull(el, "Load element failed on: "+name);
        guiItem.configure(el);
        assertEquals(el.getName(), guiItem.getName(), () -> "CONFIGURE-TEST: Failed on " + name);
        guiItem.modifyTestElement(el2);
        assertEquals("hey, new name!:", el2.getName(), () -> "Modify Test: Failed on " + name);
    }

    private static final Set<PropertyDescriptor<?, ?>> IGNORED_PROPERTIES = new HashSet<>();

    static {
        IGNORED_PROPERTIES.add(TestElementSchema.INSTANCE.getGuiClass());
        IGNORED_PROPERTIES.add(TestElementSchema.INSTANCE.getTestClass());
        // TODO: support variables in TestElement.enabled property
        IGNORED_PROPERTIES.add(TestElementSchema.INSTANCE.getEnabled());
        IGNORED_PROPERTIES.add(AbstractScopedTestElementSchema.INSTANCE.getScope());
        IGNORED_PROPERTIES.add(ThreadGroupSchema.INSTANCE.getOnSampleError());
        // TODO: migrate to editable checkboxes
        IGNORED_PROPERTIES.add(IfControllerSchema.INSTANCE.getEvaluateAll());
        IGNORED_PROPERTIES.add(IfControllerSchema.INSTANCE.getUseExpression());
        IGNORED_PROPERTIES.add(HTTPSamplerBaseSchema.INSTANCE.getPostBodyRaw());
        // TODO: LoopControlPanel does not set continueForever properly
        IGNORED_PROPERTIES.add(LoopControllerSchema.INSTANCE.getContinueForever());
        IGNORED_PROPERTIES.add(RegexExtractorSchema.INSTANCE.getMatchTarget());
        IGNORED_PROPERTIES.add(RegexExtractorSchema.INSTANCE.getDefaultIsEmpty());
        // TODO: support expressions?
        IGNORED_PROPERTIES.add(HTTPSamplerBaseSchema.INSTANCE.getIpSourceType());
        IGNORED_PROPERTIES.add(HTTPSamplerBaseSchema.INSTANCE.getImplementation());
        // TODO: support expressions in UrlConfigGui
        IGNORED_PROPERTIES.add(HTTPSamplerBaseSchema.INSTANCE.getFollowRedirects());
        IGNORED_PROPERTIES.add(HTTPSamplerBaseSchema.INSTANCE.getAutoRedirects());

    }

    /**
     * Assign simple expression value to every property of the element, and verify if the property is get back correctly
     * from the UI.
     */
    @ParameterizedTest
    @MethodSource("customGuiComponents")
    public void allPropertiesAreStoredInUI(GuiComponentHolder componentHolder) {
        JMeterGUIComponent guiItem = componentHolder.getComponent();
        assumeFalse(
                improperlyUsesUiPlaceholders(guiItem.getClass()),
                () -> "UI " + componentHolder + " does not use placeholders properly, so the test is skipped");
        assumeFalse(guiItem.getClass() == JMSSamplerGui.class,
                "JMSSamplerGui does not seem to use default values vs placeholders properly");
        assumeFalse(guiItem.getClass() == AjpSamplerGui.class,
                "AjpSamplerGui hides some fields from HTTP (e.g. proxy), so we skip testing AJP");
        TestElement el = guiItem.createTestElement();
        TestElementSchema schema = el.getSchema();
        // UI might set properties in a different order which makes it harder to compare
        Collection<PropertyDescriptor<?, ?>> properties = schema.getProperties().values();
        for (PropertyDescriptor<?, ?> property : properties) {
            if (IGNORED_PROPERTIES.contains(property)) {
                continue;
            }
            if (property instanceof CollectionPropertyDescriptor || property instanceof TestElementPropertyDescriptor) {
                continue;
            }
            if (guiItem.getClass() == NamePanel.class && property.equals(TestElementSchema.INSTANCE.getComments())) {
                // NamePanel does not configure description
                continue;
            }
            if ((guiItem.getClass() == SetupThreadGroupGui.class || guiItem.getClass() == PostThreadGroupGui.class) &&
                    property.equals(ThreadGroupSchema.INSTANCE.getDelayedStart())) {
                // Setup and Post thread groups do not show "delay thread creation" checkbox
                continue;
            }
            if (guiItem.getClass() == OpenModelThreadGroupGui.class && (
                    property.equals(ThreadGroupSchema.INSTANCE.getNumThreads()) ||
                            property.equals(ThreadGroupSchema.INSTANCE.getSameUserOnNextIteration()))) {
                continue;
            }
            el.set(property, "${test_" + property.getName() + "}");
        }
        // Configure UI with the modified properties
        guiItem.configure(el);
        // Assign the values from the UI to another element
        TestElement el2 = guiItem.createTestElement();
        guiItem.modifyTestElement(el2);

        // Remove all ignored properties
        for (PropertyDescriptor<?, ?> property : IGNORED_PROPERTIES) {
            if (property.equals(TestElementSchema.INSTANCE.getGuiClass())) {
                continue;
            }
            el.removeProperty(property);
            el2.removeProperty(property);
        }
        if (guiItem.getClass() == GraphQLHTTPSamplerGui.class) {
            el.removeProperty(HTTPSamplerBaseSchema.INSTANCE.getArguments());
            el2.removeProperty(HTTPSamplerBaseSchema.INSTANCE.getArguments());
            el.removeProperty(HTTPSamplerBaseSchema.INSTANCE.getUseBrowserCompatibleMultipart());
            el2.removeProperty(HTTPSamplerBaseSchema.INSTANCE.getUseBrowserCompatibleMultipart());
            el.removeProperty(HTTPSamplerBaseSchema.INSTANCE.getUseMultipartPost());
            el2.removeProperty(HTTPSamplerBaseSchema.INSTANCE.getUseMultipartPost());
        }

        compareAllProperties(el, el2,
                () -> "GUI element " + componentHolder + " be able to pass all the properties to a different TestElement");
    }

    @ParameterizedTest
    @MethodSource("guiComponents")
    public void propertiesShouldNotBeInitializedToNullValues(GuiComponentHolder componentHolder) {
        JMeterGUIComponent guiItem = componentHolder.getComponent();
        TestElement el = guiItem.createTestElement();

        assertFalse(
                StringUtils.isEmpty(el.getName()),
                () -> "Name should be non-blank for element " + componentHolder);
        PropertyIterator it = el.propertyIterator();
        while (it.hasNext()) {
            JMeterProperty property = it.next();
            if (property.getObjectValue() == null) {
                fail("Property " + property.getName() + " is initialized with NULL OBJECT value in " +
                        " test element " + el + " created with " + guiItem + ".createTestElement() " +
                        "Please refrain from that since null properties consume memory, and they will be " +
                        "removed when saving and loading the plan anyway");
            }
            if (property.getStringValue() == null) {
                fail("Property " + property.getName() + " is initialized with NULL STRING value in " +
                        " test element " + el + " created with " + guiItem + ".createTestElement() " +
                        "Please refrain from that since null properties consume memory, and they will be " +
                        "removed when saving and loading the plan anyway");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("guiComponents")
    public void elementShouldNotBeModifiedWithConfigureModify(GuiComponentHolder componentHolder) {
        JMeterGUIComponent guiItem = componentHolder.getComponent();
        TestElement expected = guiItem.createTestElement();
        TestElement actual = guiItem.createTestElement();
        guiItem.configure(actual);
        if (!Objects.equals(expected, actual)) {
            boolean breakpointForDebugging = Objects.equals(expected, actual);
            String expectedStr = new DslPrinterTraverser(DslPrinterTraverser.DetailLevel.ALL).append(expected).toString();
            String actualStr = new DslPrinterTraverser(DslPrinterTraverser.DetailLevel.ALL).append(actual).toString();
            assertEquals(
                    expectedStr,
                    actualStr,
                    () -> "TestElement should not be modified by " + guiItem.getClass().getName() + ".configure(element)"
            );
        }
        guiItem.modifyTestElement(actual);
        if (guiItem.getClass() == GraphQLHTTPSamplerGui.class) {
            // GraphQL sampler computes its arguments, so we don't compare them
            // See org.apache.jmeter.protocol.http.config.gui.GraphQLUrlConfigGui.modifyTestElement
            expected.removeProperty(HTTPSamplerBaseSchema.INSTANCE.getArguments());
            actual.removeProperty(HTTPSamplerBaseSchema.INSTANCE.getArguments());
        }
        if (!Objects.equals(expected, actual)) {
            if (improperlyUsesUiPlaceholders(guiItem.getClass())) {
                return;
            }
            boolean breakpointForDebugging = Objects.equals(expected, actual);
            String expectedStr = new DslPrinterTraverser(DslPrinterTraverser.DetailLevel.ALL).append(expected).toString();
            String actualStr = new DslPrinterTraverser(DslPrinterTraverser.DetailLevel.ALL).append(actual).toString();
            assertEquals(
                    expectedStr,
                    actualStr,
                    () -> "TestElement should not be modified by " + guiItem.getClass().getName() + ".configure(element); gui.modifyTestElement(element)"
            );
        }
    }

    private static boolean improperlyUsesUiPlaceholders(Class<? extends JMeterGUIComponent> klass) {
        if (klass == JavaConfigGui.class || klass == JavaTestSamplerGui.class) {
            // TODO: JavaConfigGui modifies UI when classname combobox changes, and it causes inconsistency between the
            //   element state and the UI state. We ignore the discrepancy for now
            return true;
        }
        if (klass == JUnitTestSamplerGui.class) {
            // TODO: fix org.apache.jmeter.protocol.java.control.gui.JUnitTestSamplerGui.configure to use placeholders
            return true;
        }
        if (klass == BackendListenerGui.class) {
            // TODO: fix handling of default arguments in org.apache.jmeter.visualizers.backend.BackendListenerGui.actionPerformed
            return true;
        }
        return false;
    }

    @ParameterizedTest
    @MethodSource("guiComponents")
    public void saveLoadShouldKeepElementIntact(GuiComponentHolder componentHolder) throws IOException {
        JMeterGUIComponent guiItem = componentHolder.getComponent();
        TestElement expected = guiItem.createTestElement();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        SaveService.saveElement(expected, bos);
        byte[] serializedBytes = bos.toByteArray();
        TestElement actual = (TestElement) SaveService.loadElement(new ByteArrayInputStream(serializedBytes));
        compareAllProperties(expected, actual,
                () -> "TestElement after 'save+load' should match the one created in GUI\n" +
                        "JMX is " + new String(serializedBytes, StandardCharsets.UTF_8));
    }

    private static void compareAllProperties(TestElement expected, TestElement actual,
            Supplier<String> message) {
        expected.traverse(IsEnabledNormalizer.INSTANCE);
        actual.traverse(IsEnabledNormalizer.INSTANCE);

        String expectedStr = new DslPrinterTraverser(DslPrinterTraverser.DetailLevel.ALL).append(expected).toString();
        if (!Objects.equals(expected, actual)) {
            boolean breakpointForDebugging = Objects.equals(expected, actual);
            assertEquals(
                    expectedStr,
                    new DslPrinterTraverser(DslPrinterTraverser.DetailLevel.ALL).append(actual).toString(),
                    message.get());
            fail("DSL representation is the same, however TestElement#equals says the elements are different. " + message.get());
        }
        assertEquals(expected.hashCode(), actual.hashCode(),
                "TestElement.hashCode after 'save+load' should match the one created in GUI. " +
                        "DSL representation is the same, however TestElement#hashCode says the elements are different. " +
                        message.get());
    }

    static Stream<Serializable> serializableObjects() throws Throwable {
        return getObjects(Serializable.class)
                .stream()
                .map(Serializable.class::cast)
                .filter(o -> !o.getClass().getName().endsWith("_Stub"));
    }

    /*
     * Test serializable elements - test the object
     */
    @ParameterizedTest
    @MethodSource("serializableObjects")
    public void runSerialTest(Serializable serObj) throws Exception {
        if (!(serObj instanceof Component)) {//
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bytes);
                out.writeObject(serObj);
                out.close();
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
                Object readObject = in.readObject();
                in.close();
                assertEquals(
                        serObj.getClass(),
                        readObject.getClass(),
                        () -> "deserializing class: " + serObj.getClass().getName());
            } catch (Exception e) {
                fail("serialization of " + serObj.getClass().getName() + " failed: " + e);
            }
        }
    }


    @BeforeAll
    public static void readAliases() throws Exception {
        nameMap = SaveService.loadProperties();
        assertNotNull(nameMap, "SaveService nameMap (saveservice.properties) should not be null");
    }

    private void checkElementAlias(Object item) {
        String name=item.getClass().getName();
        boolean contains = nameMap.values().contains(name);
        if (!contains){
            fail("SaveService nameMap (saveservice.properties) should contain "+name);
        }
    }

    public static Collection<Object> getObjects(Class<?> extendsClass) throws Throwable {
        String exName = extendsClass.getName();
        @SuppressWarnings("deprecation")
        Iterator<String> classes = ClassFinder
                .findClassesThatExtend(JMeterUtils.getSearchPaths(), new Class[] { extendsClass }).iterator();
        List<Object> objects = new ArrayList<>();
        while (classes.hasNext()) {
            String className = classes.next();
            // TODO - improve this check
            if (className.equals("org.apache.jmeter.gui.menu.StaticJMeterGUIComponent")) {
                continue;
            }
            if (className.endsWith("RemoteJMeterEngineImpl")) {
                continue; // Don't try to instantiate remote server
            }
            if (className.endsWith("RemoteSampleListenerImpl")) {
                // TODO: Cannot start. travis-job-e984b3d5-f93f-4b0f-b6c0-50988a5ece9d is a loopback address.
                continue;
            }
            if (className.startsWith("org.apache.jmeter.testelement.schema.") &&
                    className.endsWith("PropertyDescriptor")) {
                // PropertyDescriptors do not have no-arg constructor
                continue;
            }
            try {
                // Construct classes in the AWT thread, as we may have found classes, that
                // assume to be constructed in the AWT thread.
                SwingUtilities.invokeAndWait(() -> {
                    Object object = instantiateClass(className);
                    if (object != null) {
                        objects.add(object);
                    }
                });
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IllegalStateException && cause.getMessage().startsWith("Unable to instantiate ")) {
                    cause = cause.getCause();
                }
                if (extendsClass.equals(Serializable.class)) {
                    // TODO: ignore only well-known classes
                    System.err.println("Unable to instantiate " + className + " for service " + extendsClass + ", " + cause.toString());
                } else {
                    throw new IllegalStateException("Unable to instantiate " + className + " for service " + extendsClass, cause);
                }
            }
        }

        if (objects.isEmpty()) {
            System.out.println("No classes found that extend " + exName + ". Check the following:");
            System.out.println("Search paths are:");
            String[] ss = JMeterUtils.getSearchPaths();
            for (String s : ss) {
                System.out.println(s);
            }
            if (!classPathShown) {// Only dump it once
                System.out.println("Class path is:");
                String cp = System.getProperty("java.class.path");
                String[] classPathElements = JOrphanUtils.split(cp, java.io.File.pathSeparator);
                for (String classPathElement : classPathElements) {
                    System.out.println(classPathElement);
                }
                classPathShown = true;
            }
        }
        return objects;
    }

    private static Object instantiateClass(String className) {
        try {
            Class<?> aClass = Class.forName(className);
            if (aClass.isEnum() || Modifier.isAbstract(aClass.getModifiers()) || aClass.isInterface()) {
                return null;
            }
            return aClass
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new IllegalStateException("Unable to instantiate " + className, e instanceof InvocationTargetException ? e.getCause() : e);
        }
    }
}
