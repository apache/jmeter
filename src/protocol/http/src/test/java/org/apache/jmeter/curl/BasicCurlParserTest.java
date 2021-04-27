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

package org.apache.jmeter.curl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.curl.ArgumentHolder;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser;
import org.apache.jmeter.protocol.http.curl.FileArgumentHolder;
import org.apache.jmeter.protocol.http.curl.StringArgumentHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BasicCurlParserTest {

    private File tempFile;

    @BeforeEach
    public void setUpTempFolder(@TempDir Path tempDir) {
        tempFile = tempDir.resolve("test.txt").toFile();
    }

    @Test
    public void testBug65270SingleEqualsWithDataUrlEncodeOptions() {
        String cmdLine = String.join(" \\\n",
        Arrays.asList("curl --location --request POST 'https://example.invalid/access/token'",
        "--header 'HTTP_X_FORWARDED_FOR: 127.0.0.1'",
        "--header 'Accept-Language: it-IT'",
        "--header 'Content-Type: application/x-www-form-urlencoded'",
        "--data-urlencode '='"));
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("https://example.invalid/access/token", request.getUrl());
        assertEquals(3, request.getHeaders().size());
        assertEquals("", request.getPostData());
    }

    @Test
    public void testBug65270DuplicateDataUrlEncodeOptions() {
        String cmdLine = String.join(" \\\n",
                Arrays.asList("curl --location --request POST 'http://example.invalid/access/token'",
                        "--header 'HTTP_X_FORWARDED_FOR: 127.0.0'", "--header 'Accept-Language: it-IT'",
                        "--header 'Content-Type: application/x-www-form-urlencoded'",
                        "--data-urlencode 'client_id=someID'", "--data-urlencode 'client_secret=someSecret'",
                        "--data-urlencode 'grant_type=password'", "--data-urlencode 'username=test'",
                        "--data-urlencode 'password=Password1234'"));
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("http://example.invalid/access/token", request.getUrl());
        assertEquals(3, request.getHeaders().size());
        assertEquals(
                "client_id=someID&client_secret=someSecret&grant_type=password&username=test&password=Password1234",
                request.getPostData());
    }

    @Test
    public void testFFParsing() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'DNT: 1' "
                + "-H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("http://jmeter.apache.org/", request.getUrl());
        assertEquals(5, request.getHeaders().size());
        assertTrue(request.isCompressed());
        assertEquals("GET", request.getMethod());
        String resParser = "Request [compressed=true, url=http://jmeter.apache.org/, method=GET, headers=[(User-Agent,Mozilla/5.0 "
                +"(Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0), (Accept,text/html,application/xhtml+xml,"
                + "application/xml;q=0.9,*/*;q=0.8), (Accept-Language,en-US,en;q=0.5), (DNT,1), "
                + "(Upgrade-Insecure-Requests,1)]]";
        assertEquals(resParser, request.toString(),
                "The method 'toString' should get all parameters correctly");
    }

    @Test
    public void testChromeParsing() {
        String cmdLine = "curl 'https://jmeter.apache.org/' -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' -H 'Upgrade-Insecure-Requests: 1' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/70.0.3538.102 Mobile Safari/537.36' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                + "-H 'Accept-Encoding: gzip, deflate' -H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8' --compressed";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("https://jmeter.apache.org/", request.getUrl());
        assertEquals(7, request.getHeaders().size());
        assertTrue(request.isCompressed());
    }

    @Test
    public void testDoubleQuote() {
        String cmdLine = "curl \"http://jmeter.apache.org/\"";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("http://jmeter.apache.org/", request.getUrl());
    }

    @Test
    public void testBackslashAtLineEnding() {
        String cmdLine = "curl \\\n-d 'hey' http://jmeter.apache.org/";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("http://jmeter.apache.org/", request.getUrl());
        assertEquals("hey", request.getPostData());
    }

    @Test
    public void testSetRequestMethodOnData() {
        String cmdLine = "curl -X PUT -d 'hey' http://jmeter.apache.org/";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("http://jmeter.apache.org/", request.getUrl());
        assertEquals("hey", request.getPostData());
        assertEquals("PUT", request.getMethod());
    }

    @Test
    public void testChromeParsingNotCompressed() {
        String cmdLine = "curl 'https://jmeter.apache.org/' -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' -H 'Upgrade-Insecure-Requests: 1' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko)"
                + " Chrome/70.0.3538.102 Mobile Safari/537.36' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                + "-H 'Accept-Encoding: gzip, deflate' " + "-H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("https://jmeter.apache.org/", request.getUrl());
        assertEquals(7, request.getHeaders().size());
        assertFalse(request.isCompressed());
        assertEquals("GET", request.getMethod());
    }

    @Test
    public void testChromeParsingNoHeaders() {
        String cmdLine = "curl 'https://jmeter.apache.org/'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("https://jmeter.apache.org/", request.getUrl());
        assertTrue(request.getHeaders().isEmpty());
        assertFalse(request.isCompressed());
        assertEquals("GET", request.getMethod());
    }

    @Test
    public void testNullCommand() {
        String cmdLine = "";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("Request [compressed=false, url=null, method=GET, headers=[]]", request.toString(),
                "The method 'translateCommandline' should return 'null' when command is empty, ");
    }

    @Test
    public void testUnbalancedQuotes() {
        String cmdLine = "curl \"https://jmeter.apache.org/'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        assertThrows(
                IllegalArgumentException.class,
                () -> basicCurlParser.parse(cmdLine),
                "The method 'translateCommandline shouldn't run when the quotes are not balanced,");
    }

    @Test
    public void testPost() {
        String cmdLine = "curl 'https://jmeter.apache.org/test' -X 'POST' "
                + "-H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' -H 'Accept: */*' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'Referer: https://www.example.com/' "
                + "-H 'content-type: application/json;charset=UTF-8' -H 'Origin: https://www.example.com' "
                + "-H 'DNT: 1' -H 'Connection: keep-alive' -H 'TE: Trailers' "
                + "--data '{\"abc\":\"123\",\"no\":\"matter on sunshine\"}'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("https://jmeter.apache.org/test", request.getUrl());
        assertEquals(8, request.getHeaders().size());
        assertTrue(request.isCompressed());
        assertEquals("POST", request.getMethod());
        assertEquals("{\"abc\":\"123\",\"no\":\"matter on sunshine\"}", request.getPostData(),
                "The method 'getPostData' should return the data correctly");
    }

    @Test
    public void testMethodPut() {
        String cmdLine = "curl -X 'PUT' 'https://jmeter.apache.org/test' "
                + "-H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' -H 'Accept: */*' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'Referer: https://www.example.com/' "
                + "-H 'content-type: application/json;charset=UTF-8' -H 'Origin: https://www.example.com' "
                + "-H 'DNT: 1' -H 'Connection: keep-alive' -H 'TE: Trailers'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("PUT", request.getMethod());
    }

    @Test
    public void testError() {
        String cmdLine = "curl 'https://jmeter.apache.org/' --error -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko)"
                + " Chrome/70.0.3538.102 Mobile Safari/537.36' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                + "-H 'Accept-Encoding: gzip, deflate' " + "-H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        assertThrows(
                IllegalArgumentException.class,
                () -> basicCurlParser.parse(cmdLine));
    }

    @Test
    public void testUserAgent() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'User-Agent: Mozilla/5.0 (Macintosh;"
                + " Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'DNT: 1' "
                + "-H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1' -A 'Mozilla/5.0'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals(5, request.getHeaders().size(),
                "With method 'parser', the quantity of Headers should be 5'");
        assertTrue(request.getHeaders().contains(Pair.of("User-Agent", "Mozilla/5.0")),
                "With method 'parser', Headers need to add 'user-agent' with value 'Mozilla/5.0' ");
    }

    @Test
    public void testConnectMax() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'Connection: keep-alive' "
                + "-H 'Upgrade-Insecure-Requests: 1' --connect-timeout '2'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("2000.0", String.valueOf(request.getConnectTimeout()),
                "With method 'parser' request should contain 'connect-timeout=2'");
    }

    @Test
    public void testAuthorization() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -u 'arun:12345'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("arun", request.getAuthorization().getUser());
        assertEquals("12345", request.getAuthorization().getPass());
    }

    @Test
    public void testAuthorizationMechanismIsDigest() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -u 'arun:12345' --digest";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("DIGEST", request.getAuthorization().getMechanism().toString());
    }

    @Test
    public void testAuthMechanismIsBasic() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -u 'arun:12345' --basic";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("BASIC", request.getAuthorization().getMechanism().toString());
    }

    @Test
    public void testDefaultAuthMechanism() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -u 'arun:12345'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("BASIC", request.getAuthorization().getMechanism().toString());
    }

    @Test
    public void testCacert() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --cacert 'test.pem' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("cacert", request.getCaCert(),
                "With method 'parser',the cacert need to show a warning' ");
    }

    @Test
    public void testCapath() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --capath 'test.pem' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("capath", request.getCaCert(),
                "With method 'parser',the cacert need to show a warning'");
    }

    @Test
    public void testCert() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -E 'test.pem' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("cert", request.getCaCert(),
                "With method 'parser',the cacert need to show a warning' ");
    }

    @Test
    public void testCiphers() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --ciphers 'test.pem' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("ciphers", request.getCaCert(),
                "With method 'parser',the cacert need to show a warning' ");
    }

    @Test
    public void testCertStatus() {
        String cmdLine = "curl 'http://jmeter.apache.org/'  --cert-status ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("cert-status", request.getCaCert(),
                "With method 'parser',the cacert need to show a warning' ");
    }

    @Test
    public void testCertType() {
        String cmdLine = "curl 'http://jmeter.apache.org/'  --cert-type 'test'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("cert-type", request.getCaCert(),
                "With method 'parser',the cacert need to show a warning' ");
    }

    @Test
    public void testData() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data 'name=test' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("name=test", request.getPostData());
    }

    @Test
    public void testDuplicatedKeyInData() {
        String cmdLine = "curl 'https://example.invalid' "
                + "-H 'cache-control: no-cache' --data 'name=one' --data 'name=two' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("name=one&name=two", request.getPostData());
    }

    @Test
    public void testDataReadFromFile() throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        FileUtils.writeStringToFile(tempFile, "name=test" + System.lineSeparator(), encoding, true);
        String pathname = tempFile.getAbsolutePath();
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data '@" + pathname + "' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("name=test", request.getPostData(),
                "With method 'parser',the parameters need to reserve '\n' and '\r' ");
    }

    @Test
    public void testDataReadFromNonexistentFile() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data '@test.txt' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        assertThrows(
                IllegalArgumentException.class,
                () -> basicCurlParser.parse(cmdLine),
                "The method 'translateCommandline shouldn't run when the path of file is incorrect");
    }

    @Test
    public void testDataUrlEncodeOneParameterWithoutName() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-urlencode 'é' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("%C3%A9", request.getPostData(),
                "With method 'parser',the parameters need to be encoded' ");
    }

    @Test
    public void testDataUrlEncodeOneParameterWithName() {
        String cmdLine = "curl -s 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-urlencode 'value=é' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("value=%C3%A9", request.getPostData(),
                "With method 'parser',the parameters need to be encoded' ");
    }

    @Test
    public void testDataUrlEncodeMoreThanOneParameters() {
        String cmdLine = "curl -v 'https://postman-echo.com/post' -H 'Content-Type: application/x-www-form-urlencoded'"
                + " -H 'cache-control: no-cache' --data-urlencode 'foo1=!!!&foo2=???'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("foo1=%21%21%21%26foo2%3D%3F%3F%3F", request.getPostData(),
                "With method 'parser',the parameters need to be encoded' ");
    }

    @Test
    public void testDataUrlEncodeFromFile() throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        FileUtils.writeStringToFile(tempFile, "test", encoding, true);
        String pathname = tempFile.getAbsolutePath();
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-urlencode 'name@" + pathname + "' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("name=test", request.getPostData(),
                "With method 'parser',the parameters in the file need to be encoded' ");
    }

    @Test
    public void testDataUrlEncodeWith2AtSymbol() throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        FileUtils.writeStringToFile(tempFile, "test@", encoding, true);
        String pathname = tempFile.getAbsolutePath();
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-urlencode 'name@" + pathname + "' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("name=test%40", request.getPostData(),
                "With method 'parser',the parameters in the file need to be encoded'");
    }

    @Test
    public void testDataBinaryReadFromFile() throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        FileUtils.writeStringToFile(tempFile, "name=test" + System.lineSeparator(), encoding, true);
        String pathname = tempFile.getAbsolutePath();
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-binary '@" + pathname + "' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("name=test" + System.lineSeparator(), request.getPostData(),
                "With method 'parser',the parameters need to reserve '\n' and '\r'");
    }

    @Test
    public void testForm() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' -F 'test=name' -F 'test1=name1' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Pair<String,ArgumentHolder>> res = request.getFormData();
        assertTrue(res.contains(Pair.of("test1", StringArgumentHolder.of("name1"))),
                "With method 'parser', we should post form data");
    }

    @Test
    public void testFormWithEmptyValue() {
        String cmdLine = "curl 'https://example.invalid' -F 'test=\"\"' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Pair<String,ArgumentHolder>> res = request.getFormData();
        assertTrue(res.contains(Pair.of("test", StringArgumentHolder.of(""))),
                "With method 'parser', we should post form data: " + request.getFormData());
    }

    @Test
    public void testFormWithEmptyHeader() {
        String cmdLine = "curl 'https://example.invalid' -H 'X-Something;' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Pair<String, String>> res = request.getHeaders();
        assertTrue(res.contains(Pair.of("X-Something", "")),
                "With method 'parser', we should post form data: " + request.getFormData());
    }

    @Test
    public void testFormWithQuotedValue() {
        String cmdLine = "curl 'https://www.exaple.invalid/' "
                + "--form 'test=\"something quoted\"'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Pair<String,ArgumentHolder>> res = request.getFormData();
        assertTrue(res.contains(Pair.of("test", StringArgumentHolder.of("something quoted"))),
                "With method 'form', we should post form data");
    }

    @Test
    public void testFormWithQuotedValueWithQuotes() {
        String cmdLine = "curl 'https://www.exaple.invalid/' "
                + "--form 'test=\"something \\\"quoted\\\"\"'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Pair<String,ArgumentHolder>> res = request.getFormData();
        assertTrue(res.contains(Pair.of("test", StringArgumentHolder.of("something \"quoted\""))),
                "With method 'form', we should post form data");
    }

    @Test
    public void testFormWithQuotedFilename() {
        // The quotes will be removed later by the consumer, which is ParseCurlCommandAction
        String cmdLine = "curl 'https://www.exaple.invalid/' "
                + "--form 'image=@\"/some/file.jpg\"'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Pair<String,ArgumentHolder>> res = request.getFormData();
        assertTrue(res.contains(Pair.of("image", FileArgumentHolder.of("/some/file.jpg"))),
                "With method 'form', we should post form data: " + request.getFormData());
    }

    @Test
    public void testFormWithQuotedNotFilename() {
        // The quotes will be removed later by the consumer, which is ParseCurlCommandAction
        String cmdLine = "curl 'https://www.exaple.invalid/' "
                + "--form 'image=\"@/some/file.jpg\"'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Pair<String,ArgumentHolder>> res = request.getFormData();
        assertTrue(res.contains(Pair.of("image", StringArgumentHolder.of("@/some/file.jpg"))),
                "With method 'form', we should post form data");
    }

    @Test
    public void testFormString() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --form-string 'image=@C:\\Test\\test.jpg' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Pair<String,String>> res = request.getFormStringData();
        assertTrue(res.contains(Pair.of("image", "@C:\\Test\\test.jpg")),
                "With method 'parser', we should post form data: " + request.getFormStringData());
    }

    @Test
    public void testGet() {
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" "
                + " -H 'Authorization: Client-ID fb52f2bfa714a36' --data   " + "'name=aaa%&lname=bbb' -G";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("https://api.imgur.com/3/upload?name=aaa%&lname=bbb", request.getUrl(),
                "With method 'parser', it should put the post data in the url");
        assertEquals("GET", request.getMethod(),
                "With method 'parser',the method should be 'GET'");
    }

    @Test
    public void testDnsServer() {
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" "
                + " -H 'Authorization: Client-ID fb52f2bfa714a36' --dns-servers '0.0.0.0,1.1.1.1'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertTrue(request.getDnsServers().contains("0.0.0.0"),
                "With method 'parser', the Dns Server 0.0.0.0 should exist");
    }

    @Test
    public void testNotKeepAlive() {
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" "
                + " -H 'Authorization: Client-ID fb52f2bfa714a36' --dns-servers '0.0.0.0,1.1.1.1' --no-keepalive ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertFalse(request.isKeepAlive(), "With method 'parser', keepalive should be disabled");
    }

    @Test
    public void testProxy() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://aa:bb@example.com:8042'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("example.com", request.getProxyServer().get("servername"),
                "With method 'parser',the host of proxy should be 'examole.com'");
        assertEquals("8042", request.getProxyServer().get("port"),
                "With method 'parser',the port of proxy should be 8042");
        assertEquals("https", request.getProxyServer().get("scheme"),
                "With method 'parser',the scheme of proxy should be https");
        assertEquals("aa", request.getProxyServer().get("username"),
                "With method 'parser',the username of proxy should be aa");
        assertEquals("bb", request.getProxyServer().get("password"),
                "With method 'parser',the password of proxy should be aa");
    }

    @Test
    public void testProxyDefaultPort() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://example.com'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("1080", request.getProxyServer().get("port"),
                "With method 'parser',the default port of proxy should be 1080");
    }

    @Test
    public void testProxyUser() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --proxy '201.36.208.19:3128' -U 'aa:bb'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("aa", request.getProxyServer().get("username"));
        assertEquals("bb", request.getProxyServer().get("password"));
    }

    @Test
    public void testProxyUriIncorrectFormat() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://xxxx.xxx?xxx=xxx|xxxx|'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        assertThrows(
                IllegalArgumentException.class,
                () -> basicCurlParser.parse(cmdLine),
                "The method 'translateCommandline shouldn't run when the uri of proxy is not in the correct format");
    }

    @Test
    public void testMaxTime() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -m '2'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("2000.0", String.valueOf(request.getMaxTime()));
    }

    @Test
    public void testReferer() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --referer 'www.baidu.com'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertTrue(request.getHeaders().contains(Pair.of("Referer", "www.baidu.com")));
    }

    @Test
    public void testStringtoCookie() {
        String cookieStr = "name=Tom;password=123456";
        String url = "api.imgur.com/3/upload";
        assertThrows(
                IllegalArgumentException.class,
                () -> BasicCurlParser.stringToCookie(cookieStr, url));
    }

    @Test
    public void testCookie() {
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" -b 'name=Tom;password=123456'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Cookie c1 = new Cookie();
        c1.setDomain("api.imgur.com");
        c1.setName("name");
        c1.setValue("Tom");
        c1.setPath("/3/upload");
        Cookie c2 = new Cookie();
        c2.setDomain("api.imgur.com");
        c2.setName("password");
        c2.setValue("123456");
        c2.setPath("/3/upload");
        assertTrue(request.getCookies("https://api.imgur.com/3/upload").contains(c1),
                "With method 'parser', the cookie should be set in CookieManager");
        assertTrue(request.getCookies("https://api.imgur.com/3/upload").contains(c2),
                "With method 'parser', the cookie should be set in CookieManager");
        assertEquals(2, request.getCookies("https://api.imgur.com/3/upload").size(),
                "With method 'parser', the cookie should be set in CookieManager");
    }

    @Test
    public void testCookieFromFile() throws IOException {
        String pathname = tempFile.getAbsolutePath();
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" -b '" + pathname + "'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals(pathname, request.getFilepathCookie(),
                "With method 'parser', the file of cookie should be uploaded in CookieManager");
    }

    @Test
    public void testCookieInHeader() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'cookie: PHPSESSID=testphpsessid;a=b' --compressed";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<Cookie> cookies = request.getCookieInHeaders("http://jmeter.apache.org/");
        Cookie c1 = new Cookie();
        c1.setDomain("jmeter.apache.org");
        c1.setName("a");
        c1.setValue("b");
        c1.setPath("/");
        assertEquals(c1, cookies.get(0), "Just static cookie in header can be added in CookieManager");
        assertEquals(1, cookies.size(), "Just static cookie in header can be added in CookieManager");
    }

    @Test
    public void testIgnoreOptions() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<String> listOptions = request.getOptionsIgnored();
        assertTrue(listOptions.contains("include"), "The list of ignored options should contain 'include'");
        assertTrue(listOptions.contains("keepalive-time"), "The list of ignored options should contain 'keepalive-time'");
    }

    @Test
    public void testHead() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --head";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("HEAD", request.getMethod());
    }

    @Test
    public void testInterface() {
        String cmdLine = "curl 'http://jmeter.apache.org/'   --interface 'etho'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("etho", request.getInterfaceName());
    }

    @Test
    public void testResolver() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --resolve 'moonagic.com:443:127.0.0.2'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("moonagic.com:443:127.0.0.2", request.getDnsResolver());
    }

    @Test
    public void testLimitRate() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --limit-rate '1g'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals(1024000000, request.getLimitRate());
        cmdLine = "curl 'http://jmeter.apache.org/' --limit-rate '171k'";
        request = basicCurlParser.parse(cmdLine);
        assertEquals(175104, request.getLimitRate());
        cmdLine = "curl 'http://jmeter.apache.org/' --limit-rate '54M'";
        request = basicCurlParser.parse(cmdLine);
        assertEquals(55296000, request.getLimitRate());
    }

    @Test
    public void testNoproxy() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --noproxy 'localhost'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertEquals("localhost", request.getNoproxy());
    }

    @Test
    public void testConfigureInProperties() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertTrue(request.getOptionsInProperties().contains("--max-redirs is in 'httpsampler.max_redirects(1062 line)'"),
                "Option max-redirs should show warning");
    }

    @Test
    public void testNoSupport() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://aa:bb@example.com:8042' --proxy-ntlm";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        assertTrue(request.getOptionsNoSupport().contains("proxy-ntlm"),
                "Option proxy-ntlm should show warning");
    }

    @Test
    public void testIsValidCookie() {
        assertTrue(BasicCurlParser.isValidCookie("a=b;c=d"), "The string should be cookies");
        assertFalse(BasicCurlParser.isValidCookie("test.txt"), "A filename is not a valid cookie");
    }
}
