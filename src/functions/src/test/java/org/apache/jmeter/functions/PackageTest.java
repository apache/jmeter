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

import static org.apache.jmeter.functions.FunctionTestHelper.makeParams;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.FileNotFoundException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test cases for Functions
 */
public class PackageTest extends JMeterTestCase {

    private static final Logger log = LoggerFactory.getLogger(PackageTest.class);

    // Create the BeanShell function and set its parameters.
    private static BeanShell BSHFParams(String p1, String p2, String p3) throws Exception {
        BeanShell bsh = new BeanShell();
        bsh.setParameters(makeParams(p1, p2, p3));
        return bsh;
    }

    private JMeterContext jmctx = null;

    private JMeterVariables vars = null;

    @BeforeEach
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        jmctx.setVariables(new JMeterVariables());
        vars = jmctx.getVariables();
    }

    @Test
    public void BSH1() throws Exception {
        assumeTrue(BeanShellInterpreter.isInterpreterPresent(), "BeanShell interpreter is needed for the test");
        String fn = "src/test/resources/org/apache/jmeter/functions/testfiles/BeanShellTest.bsh";

        assertThrows(InvalidVariableException.class, () -> BSHFParams(null, null, null));
        assertThrows(InvalidVariableException.class, () -> BSHFParams("", "", ""));

        BeanShell bsh;
        try {
            bsh = BSHFParams("", "", null);
            assertEquals("", bsh.execute());
        } catch (InvalidVariableException e) {
            throw new AssertionError("BeanShell not present", e);
        }

        bsh = BSHFParams("1", null, null);
        assertEquals("1", bsh.execute());

        bsh = BSHFParams("1+1", "VAR", null);
        assertEquals("2", bsh.execute());
        assertEquals("2", vars.get("VAR"));

        // Check some initial variables
        bsh = BSHFParams("return threadName", null, null);
        assertEquals(Thread.currentThread().getName(), bsh.execute());
        bsh = BSHFParams("return log.getClass().getName()", null, null);
        assertEquals(log.getClass().getName(), bsh.execute());

        // Check source works
        bsh = BSHFParams("source (\"" + fn + "\")", null, null);
        assertEquals("9876", bsh.execute());

        // Check persistence
        bsh = BSHFParams("${SCR1}", null, null);

        vars.put("SCR1", "var1=11");
        assertEquals("11", bsh.execute());

        vars.put("SCR1", "var2=22");
        assertEquals("22", bsh.execute());

        vars.put("SCR1", "x=var1");
        assertEquals("11", bsh.execute());

        vars.put("SCR1", "++x");
        assertEquals("12", bsh.execute());

        vars.put("VAR1", "test");
        vars.put("SCR1", "vars.get(\"VAR1\")");
        assertEquals("test", bsh.execute());

        // Check init file functioning
        JMeterUtils.getJMeterProperties().setProperty(BeanShell.INIT_FILE, fn);
        bsh = BSHFParams("${SCR2}", null, null);
        vars.put("SCR2", "getprop(\"" + BeanShell.INIT_FILE + "\")");
        assertEquals(fn, bsh.execute());// Check that bsh has read the file
        vars.put("SCR2", "getprop(\"avavaav\",\"default\")");
        assertEquals("default", bsh.execute());
        vars.put("SCR2", "++i");
        assertEquals("1", bsh.execute());
        vars.put("SCR2", "++i");
        assertEquals("2", bsh.execute());

    }

    // XPathFileContainer tests

    @Test
    public void XPathtestNull() throws Exception {
        assertThrows(FileNotFoundException.class, () -> new XPathFileContainer("nosuch.xml", "/"));
    }

    @Test
    public void XPathtestrowNum() throws Exception {
        XPathFileContainer f = new XPathFileContainer(getResourceFilePath("xpathfilecontainer.xml"), "/project/target/@name");
        assertNotNull(f);

        int myRow = f.nextRow();
        assertEquals(0, myRow);
        assertEquals(1, f.getNextRow());

        myRow = f.nextRow();
        assertEquals(1, myRow);
        assertEquals(2, f.getNextRow());

        myRow = f.nextRow();
        assertEquals(2, myRow);
        assertEquals(3, f.getNextRow());
    }

    @Test
    public void XPathtestColumns() throws Exception {
        XPathFileContainer f = new XPathFileContainer(getResourceFilePath("xpathfilecontainer.xml"), "/project/target/@name");
        assertNotNull(f);
        assertTrue(f.size() > 0, "Not empty");
        int last = 0;
        for (int i = 0; i < f.size(); i++) {
            last = f.nextRow();
            log.debug("found [{}]{}", i, f.getXPathString(last));
        }
        assertEquals(last + 1, f.size());

    }

    @Test
    public void XPathtestDefault() throws Exception {
        XPathFileContainer f = new XPathFileContainer(getResourceFilePath("xpathfilecontainer.xml"), "/project/@default");
        assertNotNull(f);
        assertTrue(f.size() > 0, "Not empty");
        assertEquals("install", f.getXPathString(0));

    }

    @Test
    public void XPathEmpty() throws Exception{
        XPath xp = setupXPath("","");
        String val=xp.execute();
        assertEquals("", val);
        val=xp.execute();
        assertEquals("", val);
        val=xp.execute();
        assertEquals("", val);
    }

    @Test
    public void XPathNoFile() throws Exception{
        XPath xp = setupXPath("no-such-file","");
        String val=xp.execute();
        assertEquals("", val); // TODO - should check that error has been logged...
    }

    @Test
    public void XPathFile() throws Exception{
        XPath xp = setupXPath("testfiles/XPathTest2.xml","note/body");
        assertEquals("Don't forget me this weekend!", xp.execute());

        xp = setupXPath("testfiles/XPathTest2.xml","//note2");
        assertEquals("", xp.execute());

        xp = setupXPath("testfiles/XPathTest2.xml","//note/to");
        assertEquals("Tove", xp.execute());
    }

    @Test
    public void XPathFile1() throws Exception{
        XPath xp = setupXPath("testfiles/XPathTest.xml","//user/@username");
        assertEquals("u1", xp.execute());
        assertEquals("u2", xp.execute());
        assertEquals("u3", xp.execute());
        assertEquals("u4", xp.execute());
        assertEquals("u5", xp.execute());
        assertEquals("u1", xp.execute());
    }

    @Test
    public void XPathFile2() throws Exception{
        XPath xp1  = setupXPath("testfiles/XPathTest.xml","//user/@username");
        XPath xp1a = setupXPath("testfiles/XPathTest.xml","//user/@username");
        XPath xp2  = setupXPath("testfiles/XPathTest.xml","//user/@password");
        XPath xp2a = setupXPath("testfiles/XPathTest.xml","//user/@password");
        assertEquals("u1", xp1.execute());
        assertEquals("p1", xp2.execute());
        assertEquals("p2", xp2.execute());
        assertEquals("u2", xp1a.execute());
        assertEquals("u3", xp1.execute());
        assertEquals("u4", xp1.execute());
        assertEquals("p3", xp2a.execute());
    }

    private XPath setupXPath(String file, String expr) throws Exception{
        XPath xp = new XPath();
        xp.setParameters(makeParams(getResourceFilePath(file), expr));
        return xp;
    }

}
