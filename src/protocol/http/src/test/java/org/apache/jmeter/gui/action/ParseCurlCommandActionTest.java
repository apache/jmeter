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

package org.apache.jmeter.gui.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.JCheckBox;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser.Request;
import org.apache.jmeter.protocol.http.gui.action.ParseCurlCommandAction;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ParseCurlCommandActionTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testCreateCommentText() {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        String comment = p.createCommentText(request);
        assertEquals("Http request should can be set the right comment",
                "--max-redirs is in 'httpsampler.max_redirects(1062 line)' configure in jmeter.properties ", comment);
        cmdLine = "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        request = basicCurlParser.parse(cmdLine);
        comment = p.createCommentText(request);
        assertEquals("Http request should can be set the right comment", "--include --keepalive-time ignoring;",
                comment.trim());
        cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://aa:bb@example.com:8042' --proxy-ntlm";
        basicCurlParser = new BasicCurlParser();
        request = basicCurlParser.parse(cmdLine);
        comment = p.createCommentText(request);
        assertEquals("Http request should can be set the right comment", "--proxy-ntlm not supported; ", comment);
        cmdLine = "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        basicCurlParser = new BasicCurlParser();
        request = basicCurlParser.parse(cmdLine);
        comment = p.createCommentText(request);
        assertEquals("Http request should can be set the right comment", "--include --keepalive-time ignoring;",
                comment.trim());
        cmdLine = "curl 'http://jmeter.apache.org/'";
        basicCurlParser = new BasicCurlParser();
        request = basicCurlParser.parse(cmdLine);
        comment = p.createCommentText(request);
        assertTrue("Http request should can't be set the right comment", comment.isEmpty());
        cmdLine = "curl 'http://jmeter.apache.org/' --limit-rate '54M'";
        basicCurlParser = new BasicCurlParser();
        request = basicCurlParser.parse(cmdLine);
        comment = p.createCommentText(request);
        assertTrue(comment.trim().contains("Please configure the limit rate in 'httpclient.socket.http.cps'"));
        cmdLine = "curl 'http://jmeter.apache.org/' --noproxy ' localhost'";
        basicCurlParser = new BasicCurlParser();
        request = basicCurlParser.parse(cmdLine);
        comment = p.createCommentText(request);
        assertTrue(comment.trim().contains("Please configure noproxy list in terminal and restart JMeter."));
        cmdLine = "curl 'http://jmeter.apache.org/' --cacert '<CA certificate>'";
        basicCurlParser = new BasicCurlParser();
        request = basicCurlParser.parse(cmdLine);
        comment = p.createCommentText(request);
        assertTrue(comment.trim().contains("Please configure the SSL file with CA certificates"));
    }

    @Test
    public void testReadCurlCommandsFromTextPanel() {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b' "
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        List<String> commands = p.readFromTextPanel(cmdLine);
        assertTrue("Curl commands should be saved in list",
                commands.contains("curl 'http://jmeter.apache.org/' --max-redirs 'b'"));
        assertTrue("Curl commands should be saved in list",
                commands.contains("curl 'http://jmeter.apache.org/' --include --keepalive-time '20'"));
        assertTrue("The size of list should be 2", commands.size() == 2);
    }

    @Test
    public void testReadCurlCommandsFromFile() throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        File file = tempFolder.newFile("test.txt");
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b'" + System.lineSeparator()
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        FileUtils.writeStringToFile(file, cmdLine, encoding, true);
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        List<String> commands = p.readFromFile(file.getAbsolutePath());
        assertTrue("Curl commands should be saved in list",
                commands.contains("curl 'http://jmeter.apache.org/' --max-redirs 'b'"));
        assertTrue("Curl commands should be saved in list",
                commands.contains("curl 'http://jmeter.apache.org/' --include --keepalive-time '20'"));
        assertTrue("The size of list should be 2", commands.size() == 2);
    }

    @Test
    public void testParseCommands() {
        // commands from textpanel
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b' "
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        List<String> commands = p.readFromTextPanel(cmdLine);
        List<Request> requests = p.parseCommands(false, commands);
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request1 = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --max-redirs 'b'");
        assertEquals("The command line should be parsed in turn", request1.toString(), requests.get(0).toString());
        assertTrue("The size of list should be 2", requests.size() == 2);

        // commands from file
        cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b'" + System.lineSeparator()
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        commands = p.readFromTextPanel(cmdLine);
        requests = p.parseCommands(true, commands);
        request1 = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --max-redirs 'b'");
        assertTrue("The command line should be parsed in turn", request1.toString().equals(requests.get(0).toString()));
        assertTrue("The size of list should be 2", requests.size() == 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseCommandsException() {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redir 'b' "
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        List<String> commands = p.readFromTextPanel(cmdLine);
        p.parseCommands(false, commands);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseCommandsException2() {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redir 'b'" + System.lineSeparator()
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        List<String> commands = p.readFromTextPanel(cmdLine);
        p.parseCommands(true, commands);
    }

    @Test
    public void testCreateProxyServer()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory
                .newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -x 'https://aa:bb@example.com:8042'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, HTTPSamplerProxy.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("createProxyServer", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, httpSampler };
        method.invoke(p, objs);
        assertEquals("proxy host should be set in httpsampler", "example.com", httpSampler.getProxyHost());
        assertEquals("proxy user should be set in httpsampler", "aa", httpSampler.getProxyUser());
        assertEquals("proxy pass should be set in httpsampler", "bb", httpSampler.getProxyPass());
        assertEquals("proxy scheme should be set in httpsampler", "https", httpSampler.getProxyScheme());
        assertEquals("proxy host should be set in httpsampler", "example.com", httpSampler.getProxyHost());
        assertTrue("The command line should be parsed in turn", httpSampler.getProxyPortInt() == 8042);
    }

    @Test
    public void testCreateSampler()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        // test proxy in httpsampler
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org:8443/' -x 'https://aa:bb@example.com:8042'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, String.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("createSampler", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, "" };
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) method.invoke(p, objs);
        assertEquals("proxy scheme should be set in httpsampler", "https", httpSampler.getProxyScheme());
        assertEquals("proxy host should be set in httpsampler", "example.com", httpSampler.getProxyHost());
        assertTrue("The command line should be parsed in turn", httpSampler.getProxyPortInt() == 8042);
        assertEquals("path should be set in httpsampler", httpSampler.getPath(), "/");
        assertEquals("domain should be set in httpsampler", "jmeter.apache.org", httpSampler.getDomain());
        assertEquals("port should be set in httpsampler", 8443, httpSampler.getPort());
        assertEquals("method should be set in httpsampler", "GET", httpSampler.getMethod());

        // test post data in httpsampler
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --data 'name=test'");
        request.setInterfaceName("interface_name");
        objs = new Object[] { request, "" };
        httpSampler = (HTTPSamplerProxy) method.invoke(p, objs);
        assertEquals("method should be set in httpsampler", "POST", httpSampler.getMethod());
        assertEquals("data should be set in httpsampler", "name=test",
                httpSampler.getArguments().getArgument(0).toString());

        // test form data in httpsampler(upload data)
        request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/' -F 'test=name;type=text/foo' -F 'test1=name1'");
        objs = new Object[] { request, "" };
        httpSampler = (HTTPSamplerProxy) method.invoke(p, objs);
        assertEquals("method should be set in httpsampler", "POST", httpSampler.getMethod());
        assertEquals("form name should be set in httpsampler", "test",
                httpSampler.getArguments().getArgument(0).getName());
        assertEquals("form value should be set in httpsampler", "name",
                httpSampler.getArguments().getArgument(0).getValue());
        assertEquals("form name should be set in httpsampler", "test1",
                httpSampler.getArguments().getArgument(1).getName());
        assertEquals("form value should be set in httpsampler", "name1",
                httpSampler.getArguments().getArgument(1).getValue());

        // test form data in httpsampler(upload file)
        File file = tempFolder.newFile("test.txt");
        String filePath = file.getAbsolutePath();
        request = basicCurlParser.parse(
                "curl 'http://jmeter.apache.org/' -F 'c=@" + filePath + ";type=text/foo' -F 'c1=@" + filePath + "'");
        objs = new Object[] { request, "" };
        httpSampler = (HTTPSamplerProxy) method.invoke(p, objs);
        assertEquals("form name should be set in httpsampler", "c", httpSampler.getHTTPFiles()[0].getParamName());
        assertEquals("form name should be set in httpsampler", filePath, httpSampler.getHTTPFiles()[0].getPath());
        assertEquals("form name should be set in httpsampler", "c1", httpSampler.getHTTPFiles()[1].getParamName());

        // test form data in httpsampler
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --form-string 'c=@test.txt;type=text/foo'");
        objs = new Object[] { request, "" };
        httpSampler = (HTTPSamplerProxy) method.invoke(p, objs);
        assertEquals("form name should be set in httpsampler", "c",
                httpSampler.getArguments().getArgument(0).getName());
        assertEquals("form value should be set in httpsampler", "@test.txt;type=text/foo",
                httpSampler.getArguments().getArgument(0).getValue());
    }

    @Test
    public void testDataFormException() throws NoSuchMethodException, SecurityException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory
                .newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/' -F 'test=name' --data 'fname=a&lname=b'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, HTTPSamplerProxy.class };
        Object[] objs = new Object[] { request, httpSampler };
        try {
            Method method = parseCurlCommandAction.getDeclaredMethod("setFormData", classes);
            method.setAccessible(true);
            method.invoke(p, objs);
            fail();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                assertTrue(cause.getMessage().contains("--form and --data can't appear in the same command"));
            }
        }
    }

    @Test
    public void testCreateHttpRequest()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        ThreadGroup threadGroup = new ThreadGroup();
        TestPlan testPlan = new TestPlan();
        HashTree tree = new HashTree();
        HashTree testPlanHT = tree.add(testPlan);
        HashTree threadGroupHT = testPlanHT.add(threadGroup);
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  -E '<CA certificate>'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, HashTree.class, String.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("createHttpRequest", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, threadGroupHT, "comment" };
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) method.invoke(p, objs);
        assertEquals("path should be set in httpsampler", httpSampler.getPath(), "/");
        assertEquals("domain should be set in httpsampler", "jmeter.apache.org", httpSampler.getDomain());
        assertEquals("port should be 80 in httpsampler", 80, httpSampler.getPort());
        assertEquals("method should be set in httpsampler", "GET", httpSampler.getMethod());
    }

    @Test
    public void testConfigureTimeout() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  -m '20'  --connect-timeout '1'");
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory
                .newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, HTTPSamplerProxy.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("configureTimeout", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, httpSampler };
        method.invoke(p, objs);
        assertTrue("max connection time should be 1000", httpSampler.getConnectTimeout() == 1000);
        assertTrue("max response time should be 19000", httpSampler.getResponseTimeout() == 19000);
    }

    @Test
    public void testCreateHeaderManager()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse(
                "curl 'http://jmeter.apache.org/' -H 'Content-Type: application/x-www-form-urlencoded' --compressed");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("createHeaderManager", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request };
        HeaderManager headerManager = (HeaderManager) method.invoke(p, objs);
        assertEquals("header should be set in HeaderManager", "Content-Type", headerManager.get(0).getName());
        assertEquals("header should be set in HeaderManager", "application/x-www-form-urlencoded",
                headerManager.get(0).getValue());
        assertEquals("header should be set in HeaderManager", "Accept-Encoding", headerManager.get(1).getName());
        assertEquals("header should be set in HeaderManager", "gzip, deflate", headerManager.get(1).getValue());
    }

    @Test
    public void testCreateAuthManager()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        AuthManager authManager = new AuthManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -u 'user:passwd'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, AuthManager.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("createAuthManager", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, authManager };
        HeaderManager headerManager = (HeaderManager) method.invoke(p, objs);
        assertEquals("the username of Authorization should be set in AuthManager", "user",
                authManager.get(0).getUser());
        assertEquals("the password of Authorization should be set in AuthManager", "passwd",
                authManager.get(0).getPass());
    }

    @Test
    public void testCanAddAuthManagerInHttpRequest()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        AuthManager authManager = new AuthManager();
        Authorization authorization = new Authorization();
        authorization.setPass("passwd");
        authorization.setUser("user");
        authorization.setURL("http://jmeter.apache.org/");
        authorization.setMechanism(Mechanism.BASIC);
        authManager.addAuth(authorization);
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -u 'user:passwd'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, AuthManager.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("canAddAuthManagerInHttpRequest", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, authManager };
        assertFalse("When AuthManager contains this authorization, shouldn't add a AuthManager in Http Request",
                (boolean) method.invoke(p, objs));
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -u 'user1:passwd1'");
        objs = new Object[] { request, authManager };
        assertTrue("When AuthManager contains this url, but the username or password isn't the same,"
                + "should add a AuthManager in Http Request", (boolean) method.invoke(p, objs));
    }

    @Test
    public void testCanUpdateAuthManagerInThreadGroupt()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        AuthManager authManager = new AuthManager();
        Authorization authorization = new Authorization();
        authorization.setPass("passwd");
        authorization.setUser("user");
        authorization.setURL("http://jmeter.apache.org/");
        authorization.setMechanism(Mechanism.BASIC);
        authManager.addAuth(authorization);
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -u 'user:passwd'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, AuthManager.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("canUpdateAuthManagerInThreadGroup", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, authManager };
        assertFalse("When AuthManager contains this url, shouldn't add a AuthManager in ThreadGroup",
                (boolean) method.invoke(p, objs));
        request = basicCurlParser.parse("curl 'http://jmeter.apache.fr/' -u 'user:passwd'");
        objs = new Object[] { request, authManager };
        assertTrue("The AuthManager doesn't contain this url , should add a AuthManager in ThreadGroup",
                (boolean) method.invoke(p, objs));
    }

    @Test
    public void testCreateCookieManager()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        CookieManager cookieManager = new CookieManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -b 'name=Tom'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { CookieManager.class, Request.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("createCookieManager", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { cookieManager, request };
        method.invoke(p, objs);
        assertEquals("the domain of cookie should be set in cookieManager", "jmeter.apache.org",
                cookieManager.get(0).getDomain());
        assertEquals("the path of cookie should be set in cookieManager", "/", cookieManager.get(0).getPath());
        assertEquals("the name of cookie should be set in cookieManager", "name", cookieManager.get(0).getName());
        assertEquals("the password of cookie should be set in cookieManager", "Tom", cookieManager.get(0).getValue());
        cookieManager = new CookieManager();
        File file = tempFolder.newFile("test.txt");
        String filepath = file.getAbsolutePath();
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -b '" + filepath + "'");
        method = parseCurlCommandAction.getDeclaredMethod("createCookieManager", classes);
        method.setAccessible(true);
        objs = new Object[] { cookieManager, request };
        method.invoke(p, objs);
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -b 'test1.txt'");
        method = parseCurlCommandAction.getDeclaredMethod("createCookieManager", classes);
        method.setAccessible(true);
        objs = new Object[] { cookieManager, request };
        try {
            method.invoke(p, objs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                assertTrue(cause.getMessage().contains("File test1.txt doesn't exist"));
            }
        }
    }

    @Test
    public void testCreateCookieManagerHeader() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, NoSuchFieldException, SecurityException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        CookieManager cookieManager = new CookieManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/' -H 'cookie: PHPSESSID=testphpsessid;a=b' --compressed");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { CookieManager.class, Request.class };
        Field f = ParseCurlCommandAction.class.getDeclaredField("uploadCookiesCheckBox");
        f.setAccessible(true);
        JCheckBox uploadCookiesCheckBox = new JCheckBox(
                JMeterUtils.getResString("curl_add_cookie_header_to_cookiemanager"), true);
        f.set(p, uploadCookiesCheckBox);
        Method method = parseCurlCommandAction.getDeclaredMethod("createCookieManager", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { cookieManager, request };
        method.invoke(p, objs);
        assertEquals("the domain of cookie should be set in cookieManager", "jmeter.apache.org",
                cookieManager.get(0).getDomain());
        assertEquals("the path of cookie should be set in cookieManager", "/", cookieManager.get(0).getPath());
        assertEquals("the name of cookie should be set in cookieManager", "a", cookieManager.get(0).getName());
        assertEquals("the password of cookie should be set in cookieManager", "b", cookieManager.get(0).getValue());
        uploadCookiesCheckBox = new JCheckBox(JMeterUtils.getResString("curl_add_cookie_header_to_cookiemanager"),
                false);
        f.set(p, uploadCookiesCheckBox);
        method = parseCurlCommandAction.getDeclaredMethod("createCookieManager", classes);
        method.setAccessible(true);
        cookieManager = new CookieManager();
        objs = new Object[] { cookieManager, request };
        method.invoke(p, objs);
        assertTrue(
                "When doesn't choose to upload cookies from header," + "the cookie shouldn't be set in cookieManager",
                cookieManager.getCookies().size() == 0);
    }

    @Test
    public void testDnsServer()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --dns-servers '0.0.0.0'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, DNSCacheManager.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("createDnsServer", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, dnsCacheManager };
        method.invoke(p, objs);
        assertEquals("the dns server should be set in DNSCacheManager", "0.0.0.0",
                dnsCacheManager.getServers().get(0).getStringValue());
    }

    @Test
    public void testCanAddDnsServerInHttpRequest()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.addServer("0.0.0.0");
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --dns-servers '0.0.0.0'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, DNSCacheManager.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("canAddDnsServerInHttpRequest", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, dnsCacheManager };
        assertFalse("When the Dns servers are  the same, shouldn't add the DnsCacheManager in Http Request",
                (boolean) method.invoke(p, objs));
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --dns-servers '1.1.1.1'");
        objs = new Object[] { request, dnsCacheManager };
        assertTrue("When the Dns servers aren't the same, should add the DnsCacheManager in Http Request",
                (boolean) method.invoke(p, objs));
    }

    @Test
    public void testCreateDnsResolver()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:443:127.0.0.2'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, DNSCacheManager.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("createDnsResolver", classes);
        method.setAccessible(true);
        Object[] objs = new Object[] { request, dnsCacheManager };
        method.invoke(p, objs);
        assertEquals("the dns resolver should be set in DNSCacheManager", "StaticHost(moonagic.com, 127.0.0.2)",
                dnsCacheManager.getHosts().get(0).getStringValue());
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:9090:127.0.0.2'");
        method.setAccessible(true);
        objs = new Object[] { request, dnsCacheManager };
        method.invoke(p, objs);
        assertEquals("the dns resolver should be set in DNSCacheManager", "StaticHost(moonagic.com, 127.0.0.2)",
                dnsCacheManager.getHosts().get(0).getStringValue());
        assertTrue("the dns resolver should be set in DNSCacheManager",
                dnsCacheManager.getComment().contains("Custom DNS resolver doesn't support port"));
    }

    @Test
    public void testCanAddDnsResolverInHttpRequest()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.addHost("moonagic.com", "127.0.0.2");
        Request request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:443:127.0.0.2'");
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Class[] classes = new Class[] { Request.class, DNSCacheManager.class };
        Method method = parseCurlCommandAction.getDeclaredMethod("canAddDnsResolverInHttpRequest", classes);
        method.setAccessible(true);
        dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.addHost("moonagic.com", "127.0.0.2");
        Object[] objs = new Object[] { request, dnsCacheManager };
        method.invoke(p, objs);
        assertFalse("When the Dns servers are the same, shouldn't add the DnsCacheManager in Http Request",
                (boolean) method.invoke(p, objs));
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:9090:127.0.0.1'");
        method.setAccessible(true);
        objs = new Object[] { request, dnsCacheManager };
        method.invoke(p, objs);
        assertTrue("When the Dns servers aren't the same, should add the DnsCacheManager in Http Request",
                (boolean) method.invoke(p, objs));
        dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.addHost("moonagic.com", "127.0.0.1");
        dnsCacheManager.addHost("moonagic.com", "127.0.0.2");
        objs = new Object[] { request, dnsCacheManager };
        method.invoke(p, objs);
        assertTrue("When the Dns servers aren't the same, should add the DnsCacheManager in Http Request",
                (boolean) method.invoke(p, objs));
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:9090:127.0.0.1'");
        method.setAccessible(true);
        objs = new Object[] { request, dnsCacheManager };
        method.invoke(p, objs);
        assertTrue("When the Dns servers aren't the same, should add the DnsCacheManager in Http Request",
                (boolean) method.invoke(p, objs));
    }
}
