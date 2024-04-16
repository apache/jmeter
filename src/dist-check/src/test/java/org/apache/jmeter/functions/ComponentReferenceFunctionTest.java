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

package org.apache.jmeter.functions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTest;
import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // shares funcTitles between tests
public class ComponentReferenceFunctionTest extends JMeterTestCase {

    private static final Logger log = LoggerFactory.getLogger(ComponentReferenceFunctionTest.class);

    private Map<String, Boolean> funcTitles;

    static class Holder {
        static final Collection<Function> FUNCTIONS;

        static {
            try {
                FUNCTIONS = JMeterTest.getObjects(Function.class)
                        .stream()
                        .filter(f -> f.getClass() != CompoundVariable.class)
                        .map(Function.class::cast)
                        .collect(Collectors.toList());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * Test Functions - create the suite of tests
     */
    static Collection<Function> functions() throws Throwable {
        return Holder.FUNCTIONS;
    }

    private Element getBodyFromXMLDocument(InputStream stream)
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
     * Extract titles from functions.xml
     */
    @BeforeAll
    public void createFunctionSet() throws Exception {
        funcTitles = new HashMap<>(20);
        String compref = "../xdocs/usermanual/functions.xml";
        try (InputStream stream = new FileInputStream(findTestFile(compref))) {
            Element body = getBodyFromXMLDocument(stream);
            Element section = (Element) body.getElementsByTagName("section").item(0);
            NodeList subSections = section.getElementsByTagName("subsection");
            for (int i = 0; i < subSections.getLength(); i++) {
                NodeList components = ((Element)subSections.item(i)).getElementsByTagName("component");
                for (int j = 0; j < components.getLength(); j++) {
                    org.w3c.dom.Element comp = (org.w3c.dom.Element)
                            components.item(j);
                    funcTitles.put(comp.getAttribute("name"), Boolean.FALSE);
                    String tag = comp.getAttribute("tag");
                    if (!StringUtils.isEmpty(tag)){
                        funcTitles.put(tag, Boolean.FALSE);
                    }
                }
            }
        }
    }

    @AfterAll
    public void checkFunctionSet() throws Exception {
        Assertions.assertEquals("[]", JMeterTest.keysWithFalseValues(funcTitles).toString(), "Should not have any names left over in funcTitles");
    }

    /*
     * run the function test
     */
    @ParameterizedTest
    @MethodSource("functions")
    public void runFunction(Function funcItem) throws Exception {
        if (funcTitles.size() > 0) {
            String title = funcItem.getReferenceKey();
            boolean ct = funcTitles.containsKey(title);
            if (ct) {
                funcTitles.put(title, Boolean.TRUE);// For detecting extra entries
            }
            // Is this a work in progress ?
            if (!title.contains("(ALPHA") && !title.contains("(EXPERIMENTAL")) {
                // No, not a work in progress ...
                String s = "functions.xml needs '" + title + "' entry for " + funcItem.getClass().getName();
                if (!ct) {
                    log.warn(s); // Record in log as well
                }
                Assertions.assertTrue(ct, s);
            }
        }
    }

    /*
     * Check that function descriptions are OK
     */
    @ParameterizedTest
    @MethodSource("functions")
    public void runFunction2(Function funcItem) throws Exception {
        for (String o : funcItem.getArgumentDesc()) {
            assertInstanceOf(String.class, o, "Description must be a String");
            assertFalse(o.startsWith("[refkey"), "Description must not start with [refkey");
        }
    }
}
