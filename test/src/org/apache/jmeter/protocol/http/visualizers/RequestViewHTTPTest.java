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

package org.apache.jmeter.protocol.http.visualizers;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

public class RequestViewHTTPTest extends TestCase {
    @Test
    public void testGetQueryMapValueContainingAmpersand() {
        // see https://bz.apache.org/bugzilla/show_bug.cgi?id=58413
        String query = "login=toto1&pwd=Welcome%261";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);
        
        Assert.assertNotNull(params);
        Assert.assertEquals(2, params.size());
        
        String[] param1 = params.get("login");
        Assert.assertNotNull(param1);
        Assert.assertEquals(1, param1.length);
        Assert.assertEquals("toto1", param1[0]);
        
        String[] param2 = params.get("pwd");
        Assert.assertNotNull(param2);
        Assert.assertEquals(1, param2.length);
        Assert.assertEquals("Welcome&1", param2[0]);
    }
    
    //http://www.foo.com/test/json/getXXXX.jsp?postalCode=59115&qrcode=
    @Test
    public void testGetQueryMapWithEmptyValue() {
        String query = "postalCode=59115&qrcode=";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);
        
        Assert.assertNotNull(params);
        Assert.assertEquals(2, params.size());
        
        String[] param1 = params.get("postalCode");
        Assert.assertNotNull(param1);
        Assert.assertEquals(1, param1.length);
        Assert.assertEquals("59115", param1[0]);
        
        String[] param2 = params.get("qrcode");
        Assert.assertNotNull(param2);
        Assert.assertEquals(1, param2.length);
        Assert.assertEquals("", param2[0]);
    }
    
    @Test
    public void testGetQueryMapMultipleValues() {
        String query = "param2=15&param1=12&param2=baulpismuth";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);
        
        Assert.assertNotNull(params);
        Assert.assertEquals(2, params.size());
        
        String[] param1 = params.get("param1");
        Assert.assertNotNull(param1);
        Assert.assertEquals(1, param1.length);
        Assert.assertEquals("12", param1[0]);
        
        String[] param2 = params.get("param2");
        Assert.assertNotNull(param2);
        Assert.assertEquals(2, param2.length);
        Assert.assertEquals("15", param2[0]);
        Assert.assertEquals("baulpismuth", param2[1]);
    }
    
    @Test
    public void testGetQueryMapAmpInValue() {
        String query = "param2=15&param1=12&param3=baul%26Pismuth";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);
        
        Assert.assertNotNull(params);
        Assert.assertEquals(3, params.size());
        
        String[] param1 = params.get("param1");
        Assert.assertNotNull(param1);
        Assert.assertEquals(1, param1.length);
        Assert.assertEquals("12", param1[0]);
        
        String[] param2 = params.get("param2");
        Assert.assertNotNull(param2);
        Assert.assertEquals(1, param2.length);
        Assert.assertEquals("15", param2[0]);
        
        String[] param3 = params.get("param3");
        Assert.assertNotNull(param3);
        Assert.assertEquals(1, param3.length);
        Assert.assertEquals("baul&Pismuth", param3[0]);
    }
    
    @Test
    public void testGetQueryMapBug54055() {
        String query = "param2=15&param1=12&param3=bu4m1KzFvsozCnR4lra0%2Be69YzpnRcF09nDjc3VJvl8%3D";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);
        
        Assert.assertNotNull(params);
        Assert.assertEquals(3, params.size());
        
        String[] param1 = params.get("param1");
        Assert.assertNotNull(param1);
        Assert.assertEquals(1, param1.length);
        Assert.assertEquals("12", param1[0]);
        
        String[] param2 = params.get("param2");
        Assert.assertNotNull(param2);
        Assert.assertEquals(1, param2.length);
        Assert.assertEquals("15", param2[0]);
        
        String[] param3 = params.get("param3");
        Assert.assertNotNull(param3);
        Assert.assertEquals(1, param3.length);
        Assert.assertEquals("bu4m1KzFvsozCnR4lra0+e69YzpnRcF09nDjc3VJvl8=", param3[0]);
    }
    
    @Test
    public void testGetQueryMapBug52491() {
        String query = "<envelope><header><context><conversationId>119</conversationId></context></header>"
                + "<body><call component=\"OrderTransfer\" method=\"getSourceManifestID\" id=\"2\">\n"
                + "<params></params><refs></refs></call></body></envelope>";
        Map<String, String[]> params = RequestViewHTTP.getQueryMap(query);
        
        Assert.assertNotNull(params);
        Assert.assertEquals(1, params.size());
        
        Map.Entry<String, String[]> param1 = params.entrySet().iterator().next();
        Assert.assertNotNull(param1);
        Assert.assertEquals(1, param1.getValue().length);
        Assert.assertEquals(query, param1.getValue()[0]);
        Assert.assertTrue(StringUtils.isBlank(param1.getKey()));
    }
    
    @Test
    public void testGetQueryMapSoapHack() {
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
        
        Assert.assertNotNull(params);
        Assert.assertEquals(1, params.size());
        
        Map.Entry<String, String[]> param1 = params.entrySet().iterator().next();
        Assert.assertNotNull(param1);
        Assert.assertEquals(1, param1.getValue().length);
        Assert.assertEquals(query, param1.getValue()[0]);
        Assert.assertTrue(StringUtils.isBlank(param1.getKey()));
    }
}
