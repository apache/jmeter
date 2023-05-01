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

package org.apache.jmeter.protocol.http.visualizers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RequestViewHTTPTest {

    @Test
    void testGetQueryMapValueContainingAmpersand() {
        // see https://bz.apache.org/bugzilla/show_bug.cgi?id=58413
        String query = "login=toto1&pwd=Welcome%261";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);

        Assertions.assertNotNull(params);
        Assertions.assertEquals(2, params.size());

        String[] param1 = params.get("login");
        Assertions.assertNotNull(param1);
        Assertions.assertEquals(1, param1.length);
        Assertions.assertEquals("toto1", param1[0]);

        String[] param2 = params.get("pwd");
        Assertions.assertNotNull(param2);
        Assertions.assertEquals(1, param2.length);
        Assertions.assertEquals("Welcome&1", param2[0]);
    }

    //http://www.foo.com/test/json/getXXXX.jsp?postalCode=59115&qrcode=
    @Test
    void testGetQueryMapWithEmptyValue() {
        String query = "postalCode=59115&qrcode=";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);

        Assertions.assertNotNull(params);
        Assertions.assertEquals(2, params.size());

        String[] param1 = params.get("postalCode");
        Assertions.assertNotNull(param1);
        Assertions.assertEquals(1, param1.length);
        Assertions.assertEquals("59115", param1[0]);

        String[] param2 = params.get("qrcode");
        Assertions.assertNotNull(param2);
        Assertions.assertEquals(1, param2.length);
        Assertions.assertEquals("", param2[0]);
    }

    @Test
    void testGetQueryMapMultipleValues() {
        String query = "param2=15&param1=12&param2=baulpismuth";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);

        Assertions.assertNotNull(params);
        Assertions.assertEquals(2, params.size());

        String[] param1 = params.get("param1");
        Assertions.assertNotNull(param1);
        Assertions.assertEquals(1, param1.length);
        Assertions.assertEquals("12", param1[0]);

        String[] param2 = params.get("param2");
        Assertions.assertNotNull(param2);
        Assertions.assertEquals(2, param2.length);
        Assertions.assertEquals("15", param2[0]);
        Assertions.assertEquals("baulpismuth", param2[1]);
    }

    @Test
    void testGetQueryMapAmpInValue() {
        String query = "param2=15&param1=12&param3=baul%26Pismuth";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);

        Assertions.assertNotNull(params);
        Assertions.assertEquals(3, params.size());

        String[] param1 = params.get("param1");
        Assertions.assertNotNull(param1);
        Assertions.assertEquals(1, param1.length);
        Assertions.assertEquals("12", param1[0]);

        String[] param2 = params.get("param2");
        Assertions.assertNotNull(param2);
        Assertions.assertEquals(1, param2.length);
        Assertions.assertEquals("15", param2[0]);

        String[] param3 = params.get("param3");
        Assertions.assertNotNull(param3);
        Assertions.assertEquals(1, param3.length);
        Assertions.assertEquals("baul&Pismuth", param3[0]);
    }

    @Test
    void testGetQueryMapBug54055() {
        String query = "param2=15&param1=12&param3=bu4m1KzFvsozCnR4lra0%2Be69YzpnRcF09nDjc3VJvl8%3D";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);

        Assertions.assertNotNull(params);
        Assertions.assertEquals(3, params.size());

        String[] param1 = params.get("param1");
        Assertions.assertNotNull(param1);
        Assertions.assertEquals(1, param1.length);
        Assertions.assertEquals("12", param1[0]);

        String[] param2 = params.get("param2");
        Assertions.assertNotNull(param2);
        Assertions.assertEquals(1, param2.length);
        Assertions.assertEquals("15", param2[0]);

        String[] param3 = params.get("param3");
        Assertions.assertNotNull(param3);
        Assertions.assertEquals(1, param3.length);
        Assertions.assertEquals("bu4m1KzFvsozCnR4lra0+e69YzpnRcF09nDjc3VJvl8=", param3[0]);
    }

    @Test
    void testGetQueryMapBug52491() {
        String query = "<envelope><header><context><conversationId>119</conversationId></context></header>"
                + "<body><call component=\"OrderTransfer\" method=\"getSourceManifestID\" id=\"2\">\n"
                + "<params></params><refs></refs></call></body></envelope>";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);

        Assertions.assertNotNull(params);
        Assertions.assertEquals(1, params.size());

        Map.Entry<String, String[]> param1 = params.entrySet().iterator().next();
        Assertions.assertNotNull(param1);
        Assertions.assertEquals(1, param1.getValue().length);
        Assertions.assertEquals(query, param1.getValue()[0]);
        Assertions.assertTrue(StringUtils.isBlank(param1.getKey()));
    }

    @Test
    void testGetQueryMapSoapHack() {
        String query = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <SOAP-ENV:Header>\n" +
                "        <m:Security\n" +
                "xmlns:m=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n" +
                "            <UsernameToken>\n" +
                "                <Username>hello</Username>\n" +
                "                <Password>world</Password>\n" +
                "            </UsernameToken>\n" +
                "        </m:Security>\n" +
                "    </SOAP-ENV:Header>\n" +
                "    <SOAP-ENV:Body>     \n" +
                "        <m:GeefPersoon xmlns:m=\"http://webservice.namespace\">\n" +
                "            <Vraag>\n" +
                "                <Context>\n" +
                "                    <Naam>GeefPersoon</Naam>\n" +
                "                    <Versie>01.00.0000</Versie>\n" +
                "                </Context>\n" +
                "                <Inhoud>\n" +
                "                    <INSZ>650602505589</INSZ>\n" +
                "                </Inhoud>\n" +
                "            </Vraag>\n" +
                "        </m:GeefPersoon>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);

        Assertions.assertNotNull(params);
        Assertions.assertEquals(1, params.size());

        Map.Entry<String, String[]> param1 = params.entrySet().iterator().next();
        Assertions.assertNotNull(param1);
        Assertions.assertEquals(1, param1.getValue().length);
        Assertions.assertEquals(query, param1.getValue()[0]);
        Assertions.assertTrue(StringUtils.isBlank(param1.getKey()));
    }

    @SafeVarargs
    private static Map<String, List<String>> mapOf(Pair<String, String>... args) {
        Map<String, List<String>> results = new HashMap<>();
        Arrays.stream(args)
                .forEach(arg -> results.put(arg.getKey(), Arrays.asList(arg.getValue().split(","))));
        return results;
    }

    private static Stream<Arguments> data() {
        return Stream.of(Arguments.of("k1=v1&=&k2=v2",
                mapOf(
                        Pair.of("k1", "v1"),
                        Pair.of("k2", "v2"))),
                Arguments.of("=", mapOf()),
                Arguments.of("k1=v1&=value&k2=v2",
                        mapOf(
                                Pair.of("k1", "v1"),
                                Pair.of("", "value"),
                                Pair.of("k2", "v2"))),
                Arguments.of("a=1&a=2&=abc&=def",
                        mapOf(
                                Pair.of("a", "1,2"),
                                Pair.of("", "abc,def"))));
    }

    @ParameterizedTest
    @MethodSource("data")
    void testGetQueryMapWithEmptyKeyAndValue(String query, Map<String, List<String>> expected) {
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);
        Assertions.assertNotNull(params);
        Assertions.assertEquals(expected.size(), params.size());
        expected.forEach((key, values) -> {
            MatcherAssert.assertThat(params, Matchers.hasKey(key));
            Assertions.assertArrayEquals(values.toArray(), params.get(key));
        });
    }
}
