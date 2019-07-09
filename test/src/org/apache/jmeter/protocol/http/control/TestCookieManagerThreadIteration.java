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
package org.apache.jmeter.protocol.http.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

public class TestCookieManagerThreadIteration {
    private JMeterContext jmctx;
    private JMeterVariables jmvars;
    private static final String SAME_USER = "__jmv_SAME_USER";
    private static final String DYNAMIC_COOKIE = "dynamic_cookie_added_by_user";
    private static final String STATIC_COOKIE = "static_cookie";

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        jmvars = new JMeterVariables();
    }

    @Test
    public void testJmeterVariableCookieWhenThreadIterationIsANewUser() {
        jmvars.putObject(SAME_USER, true);
        jmctx.setVariables(jmvars);
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setControlledByThread(true);
        sampler.setCookieManager(cookieManager);
        sampler.setThreadContext(jmctx);
        boolean res = (boolean) cookieManager.getThreadContext().getVariables().getObject(SAME_USER);
        assertTrue("When test different users on the different iternations, the cookie should be cleared", res);
    }

    @Test
    public void testJmeterVariableCookieWhenThreadIterationIsSameUser() {
        jmvars.putObject(SAME_USER, false);
        jmctx.setVariables(jmvars);
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setControlledByThread(true);
        sampler.setCookieManager(cookieManager);
        sampler.setThreadContext(jmctx);
        boolean res = (boolean) cookieManager.getThreadContext().getVariables().getObject(SAME_USER);
        assertFalse("When test same user on the different iternations, the cookie shouldn't be cleared", res);
    }

    @Test
    public void testCookieManagerWhenThreadIterationIsNewUser() throws NoSuchFieldException, IllegalAccessException {
        // Controlled by Thread Group
        jmvars.putObject(SAME_USER, false);
        jmctx.setVariables(jmvars);
        CookieManager cookieManagerDynamic = new CookieManager();
        cookieManagerDynamic.setThreadContext(jmctx);
        cookieManagerDynamic.getCookies().clear();
        cookieManagerDynamic.testStarted();
        Cookie cookieDynamic = new Cookie();
        cookieDynamic.setName(DYNAMIC_COOKIE);
        cookieManagerDynamic.getCookies().addItem(cookieDynamic);
        cookieManagerDynamic.setControlledByThread(true);
        Field privateStringField = CookieManager.class.getDeclaredField("initialCookies");
        privateStringField.setAccessible(true);
        CookieManager cookieManagerStatic = new CookieManager();
        Cookie cookieStatic = new Cookie();
        cookieStatic.setName(STATIC_COOKIE);
        cookieManagerStatic.getCookies().addItem(cookieStatic);
        CollectionProperty initialCookies = cookieManagerStatic.getCookies();
        privateStringField.set(cookieManagerDynamic, initialCookies);
        assertTrue("Before the iteration,the quantity of cookies should be 1",
                cookieManagerDynamic.getCookies().size() == 1);
        assertEquals("Before the iteration, the value of cookie should be what user have set", DYNAMIC_COOKIE,
                cookieManagerDynamic.getCookies().get(0).getName());
        cookieManagerDynamic.testIterationStart(null);
        assertEquals("After the iteration, the value of cookie should be the initial cookies", STATIC_COOKIE,
                cookieManagerDynamic.getCookies().get(0).getName());
        assertTrue("After the iteration, the quantity of cookies should be 1",
                cookieManagerDynamic.getCookies().size() == 1);
        // Controlled by CookieManager
        jmvars.putObject(SAME_USER, true);
        jmctx.setVariables(jmvars);
        cookieManagerDynamic.setThreadContext(jmctx);
        cookieManagerDynamic.getCookies().clear();
        cookieManagerDynamic.testStarted();
        cookieDynamic.setName(DYNAMIC_COOKIE);
        cookieManagerDynamic.getCookies().addItem(cookieDynamic);
        cookieManagerDynamic.setClearEachIteration(true);
        cookieManagerDynamic.setControlledByThread(false);
        cookieManagerStatic.getCookies().clear();
        cookieManagerStatic.getCookies().addItem(cookieStatic);
        initialCookies = cookieManagerStatic.getCookies();
        privateStringField.set(cookieManagerDynamic, initialCookies);
        assertEquals("Before the iteration, the value of cookie should be what user have set", DYNAMIC_COOKIE,
                cookieManagerDynamic.getCookies().get(0).getName());
        assertTrue("Before the iteration,the quantity of cookies should be 1",
                cookieManagerDynamic.getCookies().size() == 1);
        cookieManagerDynamic.testIterationStart(null);
        assertEquals("After the iteration, the value of cookie should be the initial cookies", STATIC_COOKIE,
                cookieManagerDynamic.getCookies().get(0).getName());
        assertTrue("After the iteration, the quantity of cookies should be 1",
                cookieManagerDynamic.getCookies().size() == 1);
    }

    @Test
    public void testCookieManagerWhenThreadIterationIsSameUser() throws NoSuchFieldException, IllegalAccessException {
        // Controlled by Thread Group
        jmvars.putObject(SAME_USER, true);
        jmctx.setVariables(jmvars);
        CookieManager cookieManagerDynamic = new CookieManager();
        cookieManagerDynamic.setThreadContext(jmctx);
        cookieManagerDynamic.getCookies().clear();
        cookieManagerDynamic.testStarted();
        Cookie cookieDynamic = new Cookie();
        cookieDynamic.setName(DYNAMIC_COOKIE);
        cookieManagerDynamic.getCookies().addItem(cookieDynamic);
        cookieManagerDynamic.setControlledByThread(true);
        Field privateStringField = CookieManager.class.getDeclaredField("initialCookies");
        privateStringField.setAccessible(true);
        CookieManager cookieManagerStatic = new CookieManager();
        Cookie cookieStatic = new Cookie();
        cookieStatic.setName(STATIC_COOKIE);
        cookieManagerStatic.getCookies().addItem(cookieStatic);
        CollectionProperty initialCookies = cookieManagerStatic.getCookies();
        privateStringField.set(cookieManagerDynamic, initialCookies);
        assertTrue("Before the iteration,the quantity of cookies should be 1",
                cookieManagerDynamic.getCookies().size() == 1);
        assertEquals("Before the iteration, the value of cookie should be what user have set", DYNAMIC_COOKIE,
                cookieManagerDynamic.getCookies().get(0).getName());
        cookieManagerDynamic.testIterationStart(null);
        assertEquals("After the iteration, the value of cookie should be what user have set", DYNAMIC_COOKIE,
                cookieManagerDynamic.getCookies().get(0).getName());
        assertTrue("After the iteration, the quantity of cookies should be 1",
                cookieManagerDynamic.getCookies().size() == 1);

        // Controlled by CookieManager
        jmvars.putObject(SAME_USER, false);
        jmctx.setVariables(jmvars);
        cookieManagerDynamic.setControlledByThread(false);
        cookieManagerDynamic.getCookies().clear();
        cookieManagerDynamic.testStarted();
        cookieDynamic.setName(DYNAMIC_COOKIE);
        cookieManagerDynamic.getCookies().addItem(cookieDynamic);
        cookieManagerDynamic.setClearEachIteration(false);
        cookieManagerStatic.getCookies().clear();
        cookieStatic.setName(STATIC_COOKIE);
        privateStringField.set(cookieManagerDynamic, initialCookies);
        assertEquals("Before the iteration, the value of cookie should be what user have set", DYNAMIC_COOKIE,
                cookieManagerDynamic.getCookies().get(0).getName());
        assertTrue("Before the iteration,the quantity of cookies should be 1",
                cookieManagerDynamic.getCookies().size() == 1);
        cookieManagerDynamic.testIterationStart(null);
        assertEquals("After the iteration, the value of cookie should be what user have set", DYNAMIC_COOKIE,
                cookieManagerDynamic.getCookies().get(0).getName());
        assertTrue("After the iteration, the quantity of cookies should be 1",
                cookieManagerDynamic.getCookies().size() == 1);
    }
}
