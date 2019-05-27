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

package org.apache.jmeter.curl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @since 5.1
 */
public class BasicCurlParserTest {
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    /**
     * 
     */
    public BasicCurlParserTest() {
        super();
    }
    
    @Test
    public void testFFParsing() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'DNT: 1' "
                + "-H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("http://jmeter.apache.org/", request.getUrl());
        Assert.assertEquals(6, request.getHeaders().size());
        Assert.assertTrue(request.isCompressed());
        Assert.assertEquals("GET", request.getMethod());
        String resParser = "Request [compressed=true, url=http://jmeter.apache.org/, method=GET, "
                + "headers={User-Agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 "
                + "Firefox/63.0, Accept=text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8, "
                + "Accept-Language=en-US,en;q=0.5, DNT=1, Connection=keep-alive, Upgrade-Insecure-Requests=1}]";
        Assert.assertEquals("The method 'toString' should get all parameters correctly", resParser, request.toString());
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
        Assert.assertEquals("https://jmeter.apache.org/", request.getUrl());
        Assert.assertEquals(7, request.getHeaders().size());
        Assert.assertTrue(request.isCompressed());
    }

    @Test
    public void testDoubleQuote() {
        String cmdLine = "curl \"http://jmeter.apache.org/\"";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("The method 'translateCommandline' should get the url correctly When using double quotes, ",
                "http://jmeter.apache.org/", request.getUrl());
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
        Assert.assertEquals("https://jmeter.apache.org/", request.getUrl());
        Assert.assertEquals(7, request.getHeaders().size());
        Assert.assertFalse(request.isCompressed());
        Assert.assertEquals("GET", request.getMethod());
    }

    @Test
    public void testChromeParsingNoHeaders() {
        String cmdLine = "curl 'https://jmeter.apache.org/'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("https://jmeter.apache.org/", request.getUrl());
        Assert.assertTrue(request.getHeaders().isEmpty());
        Assert.assertFalse(request.isCompressed());
        Assert.assertEquals("GET", request.getMethod());
    }

    @Test
    public void testNullCommand() {
        String cmdLine = "";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("The method 'translateCommandline' should return 'null' when command is empty, ",
                "Request [compressed=false, url=null, method=GET, headers={}]", request.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnbalancedQuotes() {
        String cmdLine = "curl \"https://jmeter.apache.org/'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        basicCurlParser.parse(cmdLine);
        Assert.fail("The method 'translateCommandline shouldn't run when the quotes are not balanced,");
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
        Assert.assertEquals("https://jmeter.apache.org/test", request.getUrl());
        Assert.assertEquals(9, request.getHeaders().size());
        Assert.assertTrue(request.isCompressed());
        Assert.assertEquals("POST", request.getMethod());
        Assert.assertEquals("The method 'getPostData' should return the data correctly",
                "{\"abc\":\"123\",\"no\":\"matter on sunshine\"}", request.getPostData());
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
        Assert.assertEquals("PUT", request.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError() {
        String cmdLine = "curl 'https://jmeter.apache.org/' --error -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko)"
                + " Chrome/70.0.3538.102 Mobile Safari/537.36' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                + "-H 'Accept-Encoding: gzip, deflate' " + "-H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        basicCurlParser.parse(cmdLine);
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
        Assert.assertEquals("With method 'parser', the quantity of Headers should be 6' ", 6,
                request.getHeaders().size());
        Assert.assertEquals("With method 'parser', Headers need to add 'user-agent' with value 'Mozilla/5.0' ",
                "Mozilla/5.0", request.getHeaders().get("User-Agent"));
    }

    @Test
    public void testConnectMax() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'Connection: keep-alive' "
                + "-H 'Upgrade-Insecure-Requests: 1' --connect-timeout '2'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser' request should contain 'connect-timeout=2'", "2000.0",
               String.valueOf( request.getConnectTimeout()));
    }

    @Test
    public void testAuthorization() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -u 'arun:12345'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals(
                "With method 'parser',request should contain the parameters of the userneame of authorization", "arun",
                request.getAuthorization().getUser());
        Assert.assertEquals(
                "With method 'parser',request should contain the " + "parameters of the password of authorization",
                "12345", request.getAuthorization().getPass());
    }

    @Test
    public void testAuthorizationMechanismIsDigest() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -u 'arun:12345' --digest";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the mechanism should be 'DIGEST' ", "DIGEST",
                request.getAuthorization().getMechanism().toString());
    }

    @Test
    public void testAuthMechanismIsBasic() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -u 'arun:12345' --basic";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the mechanism should be 'BASIC' ", "BASIC",
                request.getAuthorization().getMechanism().toString());
    }

    @Test
    public void testDefaultAuthMechanism() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -u 'arun:12345'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the mechanism should be 'BASIC' ", "BASIC",
                request.getAuthorization().getMechanism().toString());
    }

    @Test
    public void testCacert() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --cacert 'test.pem' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the cacert need to show a warning' ", "cacert", request.getCacert());
    }

    @Test
    public void testCapath() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --capath 'test.pem' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the cacert need to show a warning' ", "capath", request.getCacert());
    }

    @Test
    public void testCert() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -E 'test.pem' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the cacert need to show a warning' ", "cert", request.getCacert());
    }

    @Test
    public void testCiphers() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --ciphers 'test.pem' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the cacert need to show a warning' ", "ciphers", request.getCacert());
    }

    @Test
    public void testCertStatus() {
        String cmdLine = "curl 'http://jmeter.apache.org/'  --cert-status ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the cacert need to show a warning' ", "cert-status",
                request.getCacert());
    }

    @Test
    public void testCertType() {
        String cmdLine = "curl 'http://jmeter.apache.org/'  --cert-type 'test'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the cacert need to show a warning' ", "cert-type",
                request.getCacert());
    }

    @Test
    public void testData() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data 'name=test' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the parameters should be name=test' ", "name=test",
                request.getPostData());
    }

    @Test
    public void testDataReadFromFile() throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        File file = tempFolder.newFile("test.txt");
        FileUtils.writeStringToFile(file, "name=test" + System.lineSeparator(), encoding, true);
        String pathname = file.getAbsolutePath();
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data '@" + pathname + "' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the parameters need to reserve '\n' and '\r' ", "name=test",
                request.getPostData());
    }
    @Test(expected = IllegalArgumentException.class)
    public void testDataReadFromNonexistentFile() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data '@test.txt' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        basicCurlParser.parse(cmdLine);
        Assert.fail("The method 'translateCommandline shouldn't run when the path of file is incorrect");
    }
    @Test
    public void testDataUrlEncodeOneParameterWithoutName() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-urlencode 'é' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the parameters need to be encoded' ", "%C3%A9",
                request.getPostData());
    }
    @Test
    public void testDataUrlEncodeOneParameterWithName() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-urlencode 'value=é' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the parameters need to be encoded' ", "value=%C3%A9",
                request.getPostData());
    }

    @Test
    public void testDataUrlEncodeMoreThanOneParameters() {
        String cmdLine = "curl 'https://postman-echo.com/post' -H 'Content-Type: application/x-www-form-urlencoded'"
                + " -H 'cache-control: no-cache' --data-urlencode 'foo1=!!!&foo2=???'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the parameters need to be encoded' ",
                "foo1=%21%21%21%26foo2%3D%3F%3F%3F", request.getPostData());
    }

    @Test
    public void testDataUrlEncodeFromFile() throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        File file = tempFolder.newFile("test.txt");
        FileUtils.writeStringToFile(file, "test", encoding, true);
        String pathname = file.getAbsolutePath();
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-urlencode 'name@" + pathname + "' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the parameters in the file need to be encoded' ", "name=test",
                request.getPostData());
    }

    @Test
    public void testDataBinaryReadFromFile() throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        File file = tempFolder.newFile("test.txt");
        FileUtils.writeStringToFile(file, "name=test" + System.lineSeparator(), encoding, true);
        String pathname = file.getAbsolutePath();
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --data-binary '@" + pathname + "' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the parameters need to reserve '\n' and '\r' ",
                "name=test" + System.lineSeparator(), request.getPostData());
    }

    @Test
    public void testForm() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' -F 'test=name' -F 'test1=name1' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Map<String, String> res = request.getFormData();
        Assert.assertEquals("With method 'parser', we should post form data", "name1", res.get("test1"));
    }

    @Test
    public void testFormString() {
        String cmdLine = "curl 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_form_submit/action_page.php' "
                + "-H 'cache-control: no-cache' --form-string 'image=@C:\\Test\\test.jpg' ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Map<String, String> res = request.getFormStringData();
        Assert.assertEquals("With method 'parser', we should post form data", "@C:\\Test\\test.jpg", res.get("image"));
    }

    @Test
    public void testGet() {
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" "
                + " -H 'Authorization: Client-ID fb52f2bfa714a36' --data   " + "'name=aaa%&lname=bbb' -G";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser', it should put the post data in the url",
                "https://api.imgur.com/3/upload?name=aaa%&lname=bbb", request.getUrl());
        Assert.assertEquals("With method 'parser',the method should be 'GET'", "GET", request.getMethod());
    }

    @Test
    public void testDnsServer() {
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" "
                + " -H 'Authorization: Client-ID fb52f2bfa714a36' --dns-servers '0.0.0.0,1.1.1.1'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertTrue("With method 'parser', the Dns Server 0.0.0.0 should exist",
                request.getDnsServers().contains("0.0.0.0"));
    }

    @Test
    public void testNotKeepAlive() {
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" "
                + " -H 'Authorization: Client-ID fb52f2bfa714a36' --dns-servers '0.0.0.0,1.1.1.1' --no-keepalive ";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertFalse("With method 'parser', keepalive should be disabled", request.isKeepAlive());
    }

    @Test
    public void testProxy() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://aa:bb@example.com:8042'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the host of proxy should be 'examole.com'", "example.com",
                request.getProxyServer().get("servername"));
        Assert.assertEquals("With method 'parser',the port of proxy should be 8042", "8042",
                request.getProxyServer().get("port"));
        Assert.assertEquals("With method 'parser',the scheme of proxy should be https", "https",
                request.getProxyServer().get("scheme"));
        Assert.assertEquals("With method 'parser',the username of proxy should be aa", "aa",
                request.getProxyServer().get("username"));
        Assert.assertEquals("With method 'parser',the password of proxy should be aa", "bb",
                request.getProxyServer().get("password"));
    }

    @Test
    public void testProxyDefaultPort() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://example.com'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the default port of proxy should be 1080", "1080",
                request.getProxyServer().get("port"));
    }

    @Test
    public void testProxyUser() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --proxy '201.36.208.19:3128' -U 'aa:bb'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser',the username of proxy should be aa", "aa",
                request.getProxyServer().get("username"));
        Assert.assertEquals("With method 'parser',the password of proxy should be aa", "bb",
                request.getProxyServer().get("password"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProxyUriIncorrectFormat() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://xxxx.xxx?xxx=xxx|xxxx|'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        basicCurlParser.parse(cmdLine);
        Assert.fail(
                "The method 'translateCommandline shouldn't run when the uri of proxy is not in the correct format");
    }

    @Test
    public void testMaxTime() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -m '2'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser', max time of all the operation should be 200", "2000.0",
                String.valueOf( request.getMaxTime()));
    }

    @Test
    public void testReferer() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --referer 'www.baidu.com'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser', the refer page information should be set in http Header",
                "www.baidu.com", request.getHeaders().get("Referer"));
    }

    @Test
    public void testCookie() {
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" -b 'name=Tom;password=123456'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser', the cookie should be set in CookieManager",
                "name=Tom;password=123456", request.getCookie());
    }
    @Test
    public void testCookieFromFile() throws IOException {
        File file = tempFolder.newFile("test.txt");
        String pathname = file.getAbsolutePath();
        String cmdLine = "curl -X POST  \"https://api.imgur.com/3/upload\" -b '" + pathname + "'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser', the file of cookie should be uploaded in CookieManager",
                file.getAbsolutePath(), request.getCookie());}

    @Test
    public void testIgnoreOptions() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --include --keepalive-time '20'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        List<String> listOptions=request.getOptionsIgnored();
        Assert.assertTrue("The list of ignored options should contain 'include'", listOptions.contains("include"));
        Assert.assertTrue("The list of ignored options should contain 'keepalive-time'", listOptions.contains("keepalive-time"));
    }

    @Test
    public void testHead() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --head";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("The method should be HEAD","HEAD", request.getMethod());
    }

    @Test
    public void testInterface() {
        String cmdLine = "curl 'http://jmeter.apache.org/'   --interface 'etho'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("The interface should to be etho","etho", request.getInterfaceName());
    }

    @Test
    public void testResolver() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --resolve 'moonagic.com:443:127.0.0.2'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("The resolve DNS should be 'moonagic.com:443:127.0.0.2'", "moonagic.com:443:127.0.0.2",
                request.getResolverDNS());
    }

    @Test
    public void testLimitRate() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --limit-rate '1g'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertTrue("The limit rate should be 128000000",request.getLimitRate()==128000000);
        cmdLine = "curl 'http://jmeter.apache.org/' --limit-rate '171k'";
        request = basicCurlParser.parse(cmdLine);
        Assert.assertTrue("The limit rate should be 21888",request.getLimitRate()==21888);
        cmdLine = "curl 'http://jmeter.apache.org/' --limit-rate '54M'";
        request = basicCurlParser.parse(cmdLine);
        Assert.assertTrue("The limit rate should be 6912000",request.getLimitRate()==6912000);
    }

    @Test
    public void testNoproxy() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --noproxy 'localhost'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("No Proxy server should be localhost", "localhost",
                request.getNoproxy());
    }

    @Test
    public void testConfigureInProperties() {
        String cmdLine = "curl 'http://jmeter.apache.org/' --max-redirs 'b'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertTrue("Option max-redirs should show warning",
                request.getOptionsInProperties().contains("max-redirs"));
    }

    @Test
    public void testNoSupport() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -x 'https://aa:bb@example.com:8042' --proxy-ntlm";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertTrue("Option proxy-ntlm should show warning",
                request.getOptionsNoSupport().contains("proxy-ntlm"));
    }
}
