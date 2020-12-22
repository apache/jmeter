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

package org.apache.jmeter.gui.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.swing.JCheckBox;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser.Request;
import org.apache.jmeter.protocol.http.gui.action.ParseCurlCommandAction;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

public class ParseCurlCommandActionTest {

    @Test
    public void testCreateCommentText() {
        testCommentText("curl 'http://jmeter.apache.org/' --max-redirs 'b'",
                "--max-redirs is in 'httpsampler.max_redirects(1062 line)' configure in jmeter.properties");

        testCommentText("curl 'http://jmeter.apache.org/' --include --keepalive-time '20'",
                "--include --keepalive-time ignoring;");

        testCommentText("curl 'http://jmeter.apache.org/' -x 'https://aa:bb@example.com:8042' --proxy-ntlm",
                "--proxy-ntlm not supported;");
        testCommentText("curl 'http://jmeter.apache.org/' --include --keepalive-time '20'",
                "--include --keepalive-time ignoring;");
        testCommentText("curl 'http://jmeter.apache.org/'", "");

        testCommentTextStartsWith("curl 'http://jmeter.apache.org/' --limit-rate '54M'", "Please configure the limit rate in 'httpclient.socket.http.cps'");
        testCommentTextStartsWith("curl 'http://jmeter.apache.org/' --noproxy ' localhost'", "Please configure noproxy list in terminal and restart JMeter.");
        testCommentTextStartsWith("curl 'http://jmeter.apache.org/' --cacert '<CA certificate>'", "Please configure the SSL file with CA certificates");
    }

    private void testCommentTextStartsWith(String cmdLine, String expectedComment) {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse(cmdLine);
        String comment = p.createCommentText(request);
        assertTrue(comment.trim().startsWith(expectedComment));
    }

    private void testCommentText(String cmdLine, String expectedComment) {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse(cmdLine);
        String comment = p.createCommentText(request);
        assertEquals(expectedComment, comment.trim());
    }

    @Test
    public void testReadCurlCommandsFromTextPanel() {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b' "
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        List<String> commands = p.readFromTextPanel(cmdLine);
        assertTrue(commands.contains("curl 'http://jmeter.apache.org/' --max-redirs 'b'"),
                "Curl commands should be saved in list");
        assertTrue(commands.contains("curl 'http://jmeter.apache.org/' --include --keepalive-time '20'"),
                "Curl commands should be saved in list");
        assertEquals(2, commands.size());
    }

    @Test
    public void testReadCurlCommandsFromFile(@TempDir Path tempDir) throws Exception {
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b'" + System.lineSeparator()
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        String tempPath = writeToTempFile(tempDir, cmdLine);
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        List<String> commands = p.readFromFile(tempPath);
        assertTrue(commands.contains("curl 'http://jmeter.apache.org/' --max-redirs 'b'"),
                "Curl commands should be saved in list");
        assertTrue(commands.contains("curl 'http://jmeter.apache.org/' --include --keepalive-time '20'"),
                "Curl commands should be saved in list");
        assertEquals(2, commands.size());
    }

    private String writeToTempFile(Path dir, String s) throws IOException {
        return Files.write(dir.resolve("test.txt"), s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW)
                .toAbsolutePath()
                .toString();
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
        assertEquals(request1.toString(), requests.get(0).toString());
        assertEquals(2, requests.size());

        // commands from file
        cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b'" + System.lineSeparator()
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        commands = p.readFromTextPanel(cmdLine);
        requests = p.parseCommands(true, commands);
        request1 = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --max-redirs 'b'");
        assertEquals(request1.toString(), requests.get(0).toString(), "The command line should be parsed in turn");
        assertEquals(2, requests.size());
    }

    @Test
    public void testParseCommandsException() {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redir 'b' "
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        List<String> commands = p.readFromTextPanel(cmdLine);
        assertThrowsIllegalArgument(() -> p.parseCommands(false, commands));
    }

    private void assertThrowsIllegalArgument(Executable e) {
        assertThrows(IllegalArgumentException.class, e);
    }

    @Test
    public void testParseCommandsException2() {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redir 'b'" + System.lineSeparator()
                + "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        List<String> commands = p.readFromTextPanel(cmdLine);
        assertThrowsIllegalArgument(() -> p.parseCommands(true, commands));
    }

    @Test
    public void testCreateProxyServer() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory
                .newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -x 'https://aa:bb@example.com:8042'");
        Method method = getMethodFor("createProxyServer", Request.class, HTTPSamplerProxy.class);
        method.invoke(p, request, httpSampler);
        assertEquals("example.com", httpSampler.getProxyHost(), "proxy host should be set in httpsampler");
        assertEquals("aa", httpSampler.getProxyUser(), "proxy user should be set in httpsampler");
        assertEquals("bb", httpSampler.getProxyPass(), "proxy pass should be set in httpsampler");
        assertEquals("https", httpSampler.getProxyScheme(), "proxy scheme should be set in httpsampler");
        assertEquals("example.com", httpSampler.getProxyHost(), "proxy host should be set in httpsampler");
        assertEquals(8042, httpSampler.getProxyPortInt(), "The command line should be parsed in turn");
    }

    @Test
    public void testCreateSampler(@TempDir Path tempDir) throws Exception {
        // test proxy in httpsampler
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org:8443/' -x 'https://aa:bb@example.com:8042'");
        Method method = getMethodFor("createSampler", Request.class, String.class);
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) method.invoke(p, request, "");
        assertEquals("https", httpSampler.getProxyScheme(), "proxy scheme should be set in httpsampler");
        assertEquals("example.com", httpSampler.getProxyHost(), "proxy host should be set in httpsampler");
        assertEquals(8042, httpSampler.getProxyPortInt(), "The command line should be parsed in turn");
        assertEquals("/", httpSampler.getPath(), "path should be set in httpsampler");
        assertEquals("jmeter.apache.org", httpSampler.getDomain(), "domain should be set in httpsampler");
        assertEquals(8443, httpSampler.getPort(), "port should be set in httpsampler");
        assertEquals("GET", httpSampler.getMethod(), "method should be set in httpsampler");

        // test post data in httpsampler
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --data 'name=test'");
        request.setInterfaceName("interface_name");
        httpSampler = (HTTPSamplerProxy) method.invoke(p, request, "");
        assertEquals("POST", httpSampler.getMethod());
        assertEquals("name=test", httpSampler.getArguments().getArgument(0).toString());

        // test form data in httpsampler(upload data)
        request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/' -F 'test=name;type=text/foo' -F 'test1=name1'");
        httpSampler = (HTTPSamplerProxy) method.invoke(p, request, "");
        Arguments samplerArguments = httpSampler.getArguments();
        assertEquals("POST", httpSampler.getMethod(), "method should be set in httpsampler");
        assertEquals("test", samplerArguments.getArgument(0).getName(), "form name should be set in httpsampler");
        assertEquals("name", samplerArguments.getArgument(0).getValue(), "form value should be set in httpsampler");
        assertEquals("test1", samplerArguments.getArgument(1).getName(), "form name should be set in httpsampler");
        assertEquals("name1", samplerArguments.getArgument(1).getValue(), "form value should be set in httpsampler");

        // test form data in httpsampler(upload file)
        String filePath = tempDir.resolve("test.txt").toAbsolutePath().toString();
        request = basicCurlParser.parse(
                "curl 'http://jmeter.apache.org/' -F 'c=@" + filePath + ";type=text/foo' -F 'c1=@" + filePath + "'");
        httpSampler = (HTTPSamplerProxy) method.invoke(p, request, "");
        assertEquals("c", httpSampler.getHTTPFiles()[0].getParamName(), "form name should be set in httpsampler");
        assertEquals(filePath, httpSampler.getHTTPFiles()[0].getPath(), "form name should be set in httpsampler");
        assertEquals("c1", httpSampler.getHTTPFiles()[1].getParamName(), "form name should be set in httpsampler");

        // test form data in httpsampler
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --form-string 'c=@test.txt;type=text/foo'");
        httpSampler = (HTTPSamplerProxy) method.invoke(p, request, "");
        assertEquals("c", httpSampler.getArguments().getArgument(0).getName());
        assertEquals("@test.txt;type=text/foo", httpSampler.getArguments().getArgument(0).getValue());

        // test form data in httpsampler
        request = basicCurlParser.parse(
                "curl -X PUT \"https://www.example.com:123/12345?param1=value1&param2=value2\" -H  \"accept: */*\" -H  \"X-XSRF-TOKEN: 1234\"");
        httpSampler = (HTTPSamplerProxy) method.invoke(p, request, "");
        assertEquals(new URL("https://www.example.com:123/12345?param1=value1&param2=value2"), httpSampler.getUrl());
        assertEquals(123, httpSampler.getPort());
        assertEquals("www.example.com", httpSampler.getDomain());
        assertEquals("/12345?param1=value1&param2=value2", httpSampler.getPath());
        assertEquals("PUT", httpSampler.getMethod());
        assertEquals(new Header("accept", "*/*"), httpSampler.getHeaderManager().getHeader(0));
        assertEquals(new Header("X-XSRF-TOKEN", "1234"), httpSampler.getHeaderManager().getHeader(1));
    }

    @Test
    public void testDataFormException() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory
                .newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/' -F 'test=name' --data 'fname=a&lname=b'");
        Method method = getMethodFor("setFormData", Request.class, HTTPSamplerProxy.class);
        try {
            method.invoke(p, request, httpSampler);
            throw new IllegalStateException("Should have thrown InvocationTargetException");
        } catch (InvocationTargetException | IllegalAccessException e) {
            assertEquals("--form and --data can't appear in the same command", e.getCause().getMessage());
        }
    }

    @Test
    public void testCreateHttpRequest() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        ThreadGroup threadGroup = new ThreadGroup();
        TestPlan testPlan = new TestPlan();
        HashTree tree = new HashTree();
        HashTree testPlanHT = tree.add(testPlan);
        HashTree threadGroupHT = testPlanHT.add(threadGroup);
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  -E '<CA certificate>'");
        Method method = getMethodFor("createHttpRequest", Request.class, HashTree.class, String.class);
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) method.invoke(p, request, threadGroupHT, "comment");
        assertEquals("/", httpSampler.getPath(), "path should be set in httpsampler");
        assertEquals("jmeter.apache.org", httpSampler.getDomain(), "domain should be set in httpsampler");
        assertEquals(80, httpSampler.getPort(), "port should be 80 in httpsampler");
        assertEquals("GET", httpSampler.getMethod(), "method should be set in httpsampler");
    }

    @Test
    public void testCreateHttpRequestWithQuotedFilenameAsData() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        ThreadGroup threadGroup = new ThreadGroup();
        TestPlan testPlan = new TestPlan();
        HashTree tree = new HashTree();
        HashTree testPlanHT = tree.add(testPlan);
        HashTree threadGroupHT = testPlanHT.add(threadGroup);
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -X POST --form 'pic=@\"/some/file.jpg\"'");
        Method method = getMethodFor("createHttpRequest", Request.class, HashTree.class, String.class);
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) method.invoke(p, request, threadGroupHT, "comment");
        assertEquals("/", httpSampler.getPath(), "path should be set in httpsampler");
        assertEquals("jmeter.apache.org", httpSampler.getDomain(), "domain should be set in httpsampler");
        assertEquals(80, httpSampler.getPort(), "port should be 80 in httpsampler");
        assertEquals("POST", httpSampler.getMethod(), "method should be set in httpsampler");
        HTTPFileArg fileArg = httpSampler.getHTTPFiles()[0];
        assertEquals("pic", fileArg.getParamName());
        assertEquals("/some/file.jpg", fileArg.getPath());
        assertEquals("image/jpeg", fileArg.getMimeType());
    }

    @Test
    public void testConfigureTimeout() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  -m '20'  --connect-timeout '1'");
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory
                .newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        Method method = getMethodFor("configureTimeout", Request.class, HTTPSamplerProxy.class);
        method.invoke(p, request, httpSampler);
        assertEquals(1000, httpSampler.getConnectTimeout());
        assertEquals(19000, httpSampler.getResponseTimeout());
    }

    @Test
    public void testCreateHeaderManager() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse(
                "curl 'http://jmeter.apache.org/' -H 'Content-Type: application/x-www-form-urlencoded' --compressed");
        Method method = getMethodFor("createHeaderManager", Request.class);
        HeaderManager headerManager = (HeaderManager) method.invoke(p, request);
        // The following headers should be set in the HeaderManager
        assertEquals("Content-Type", headerManager.get(0).getName());
        assertEquals("application/x-www-form-urlencoded", headerManager.get(0).getValue());
        assertEquals("Accept-Encoding", headerManager.get(1).getName());
        assertEquals("gzip, deflate", headerManager.get(1).getValue());
    }

    @Test
    public void testCreateAuthManager() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        AuthManager authManager = new AuthManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -u 'user:passwd'");
        Method method = getMethodFor("createAuthManager", Request.class, AuthManager.class);
        method.invoke(p, request, authManager);
        assertEquals("user", authManager.get(0).getUser());
        assertEquals("passwd", authManager.get(0).getPass());
    }

    @Test
    public void testCanAddAuthManagerInHttpRequest() throws Exception {
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
        Method method = getMethodFor("canAddAuthManagerInHttpRequest", Request.class, AuthManager.class);
        assertFalse((boolean) method.invoke(p, request, authManager),
                "When AuthManager contains this authorization, shouldn't add a AuthManager in Http Request");
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -u 'user1:passwd1'");
        assertTrue((boolean) method.invoke(p, request, authManager),
                "When AuthManager contains this url, but the username or password isn't the same,"
                        + "should add a AuthManager in Http Request");
    }

    @Test
    public void testCanUpdateAuthManagerInThreadGroupt() throws Exception {
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
        Method method = getMethodFor("canUpdateAuthManagerInThreadGroup", Request.class, AuthManager.class);
        assertFalse((boolean) method.invoke(p, request, authManager),
                "When AuthManager contains this url, shouldn't add a AuthManager in ThreadGroup");
        request = basicCurlParser.parse("curl 'http://jmeter.apache.fr/' -u 'user:passwd'");
        assertTrue((boolean) method.invoke(p, request, authManager),
                "The AuthManager doesn't contain this url , should add a AuthManager in ThreadGroup");
    }

    @Test
    public void testCreateCookieManager(@TempDir Path tempDir) throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        CookieManager cookieManager = new CookieManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -b 'name=Tom'");
        Method method = getMethodFor("createCookieManager", CookieManager.class, Request.class);
        method.invoke(p, cookieManager, request);
        assertEquals("jmeter.apache.org", cookieManager.get(0).getDomain(),
                "the domain of cookie should be set in cookieManager");
        assertEquals("/", cookieManager.get(0).getPath(), "the path of cookie should be set in cookieManager");
        assertEquals("name", cookieManager.get(0).getName(), "the name of cookie should be set in cookieManager");
        assertEquals("Tom", cookieManager.get(0).getValue(), "the password of cookie should be set in cookieManager");

        cookieManager = new CookieManager();
        String filepath = tempDir.resolve("test.txt").toAbsolutePath().toString();
        assertTrue(tempDir.resolve("test.txt").toFile().createNewFile());
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -b '" + filepath + "'");
        method.invoke(p, cookieManager, request);
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' -b 'test1.txt'");
        try {
            method.invoke(p, cookieManager, request);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                assertTrue(cause.getMessage().contains("File test1.txt doesn't exist"));
            }
        }
    }

    @Test
    public void testCreateCookieManagerHeader() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        CookieManager cookieManager = new CookieManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/' -H 'cookie: PHPSESSID=testphpsessid;a=b' --compressed");
        Field f = ParseCurlCommandAction.class.getDeclaredField("uploadCookiesCheckBox");
        f.setAccessible(true);
        JCheckBox uploadCookiesCheckBox = new JCheckBox(
                JMeterUtils.getResString("curl_add_cookie_header_to_cookiemanager"), true);
        f.set(p, uploadCookiesCheckBox);
        Method method = getMethodFor("createCookieManager", CookieManager.class, Request.class);
        method.invoke(p, cookieManager, request);
        assertEquals("jmeter.apache.org", cookieManager.get(0).getDomain(), "the domain of cookie should be set in cookieManager");
        assertEquals("/", cookieManager.get(0).getPath(), "the path of cookie should be set in cookieManager");
        assertEquals("a", cookieManager.get(0).getName(), "the name of cookie should be set in cookieManager");
        assertEquals("b", cookieManager.get(0).getValue(), "the password of cookie should be set in cookieManager");
        uploadCookiesCheckBox = new JCheckBox(JMeterUtils.getResString("curl_add_cookie_header_to_cookiemanager"), false);

        f.set(p, uploadCookiesCheckBox);
        cookieManager = new CookieManager();
        method.invoke(p, cookieManager, request);
        assertEquals(0, cookieManager.getCookies().size(),
                "When doesn't choose to upload cookies from header," +
                        "the cookie shouldn't be set in cookieManager");
    }

    @Test
    public void testDnsServer() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --dns-servers '0.0.0.0'");
        Method method = getMethodFor("createDnsServer", Request.class, DNSCacheManager.class);
        method.invoke(p, request, dnsCacheManager);
        assertEquals("0.0.0.0", dnsCacheManager.getServers().get(0).getStringValue(),
                "the dns server should be set in DNSCacheManager");
    }

    @Test
    public void testCanAddDnsServerInHttpRequest() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.addServer("0.0.0.0");
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --dns-servers '0.0.0.0'");
        Method method = getMethodFor("canAddDnsServerInHttpRequest", Request.class, DNSCacheManager.class);
        assertFalse((boolean) method.invoke(p, request, dnsCacheManager),
                "When the Dns servers are  the same, shouldn't add the DnsCacheManager in Http Request");
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/' --dns-servers '1.1.1.1'");
        assertTrue((boolean) method.invoke(p, request, dnsCacheManager),
                "When the Dns servers aren't the same, should add the DnsCacheManager in Http Request");
    }

    @Test
    public void testCreateDnsResolver() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        Request request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:443:127.0.0.2'");
        Method method = getMethodFor("createDnsResolver", Request.class, DNSCacheManager.class);
        method.invoke(p, request, dnsCacheManager);
        assertEquals("StaticHost(moonagic.com, 127.0.0.2)", dnsCacheManager.getHosts().get(0).getStringValue());
        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:9090:127.0.0.2'");
        method.invoke(p, request, dnsCacheManager);
        assertEquals("StaticHost(moonagic.com, 127.0.0.2)", dnsCacheManager.getHosts().get(0).getStringValue(),
                "the dns resolver should be set in DNSCacheManager");
        assertTrue(dnsCacheManager.getComment().contains("Custom DNS resolver doesn't support port"),
                "the dns resolver should be set in DNSCacheManager");
    }

    @Test
    public void testCanAddDnsResolverInHttpRequest() throws Exception {
        ParseCurlCommandAction p = new ParseCurlCommandAction();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.addHost("moonagic.com", "127.0.0.2");

        Request request = basicCurlParser
                .parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:443:127.0.0.2'");
        Method method = getMethodFor("canAddDnsResolverInHttpRequest", Request.class, DNSCacheManager.class);
        dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.addHost("moonagic.com", "127.0.0.2");
        method.invoke(p, request, dnsCacheManager);
        assertFalse((boolean) method.invoke(p, request, dnsCacheManager),
                "When the Dns servers are the same, shouldn't add the DnsCacheManager in Http Request");

        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:9090:127.0.0.1'");
        method.invoke(p, request, dnsCacheManager);
        assertTrue((boolean) method.invoke(p, request, dnsCacheManager),
                "When the Dns servers aren't the same, should add the DnsCacheManager in Http Request");
        dnsCacheManager = new DNSCacheManager();
        dnsCacheManager.addHost("moonagic.com", "127.0.0.1");
        dnsCacheManager.addHost("moonagic.com", "127.0.0.2");
        method.invoke(p, request, dnsCacheManager);
        assertTrue((boolean) method.invoke(p, request, dnsCacheManager),
                "When the Dns servers aren't the same, should add the DnsCacheManager in Http Request");

        request = basicCurlParser.parse("curl 'http://jmeter.apache.org/'  --resolve 'moonagic.com:9090:127.0.0.1'");
        method.invoke(p, request, dnsCacheManager);
        assertTrue((boolean) method.invoke(p, request, dnsCacheManager),
                "When the Dns servers aren't the same, should add the DnsCacheManager in Http Request");
    }

    private Method getMethodFor(String name, Class<?>...paramsClasses) throws NoSuchMethodException, SecurityException {
        Class<ParseCurlCommandAction> parseCurlCommandAction = ParseCurlCommandAction.class;
        Method method = parseCurlCommandAction.getDeclaredMethod(name, paramsClasses);
        method.setAccessible(true);
        return method;
    }
}
