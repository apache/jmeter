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

package org.apache.jmeter.testelement.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.apache.jmeter.config.LoginConfig;
import org.junit.Test;

/**
 * Class for testing the property package.
 */
public class PackageTest {


    @Test
    public void testStringProperty() throws Exception {
        StringProperty prop = new StringProperty("name", "value");
        prop.setRunningVersion(true);
        prop.setObjectValue("new Value");
        assertEquals("new Value", prop.getStringValue());
        prop.recoverRunningVersion(null);
        assertEquals("value", prop.getStringValue());
        prop.setObjectValue("new Value");
        prop.setObjectValue("2nd Value");
        assertEquals("2nd Value", prop.getStringValue());
        prop.recoverRunningVersion(null);
        assertEquals("value", prop.getStringValue());
    }

    @Test
    public void testElementProperty() throws Exception {
        LoginConfig config = new LoginConfig();
        config.setUsername("username");
        config.setPassword("password");
        TestElementProperty prop = new TestElementProperty("name", config);
        prop.setRunningVersion(true);
        config = new LoginConfig();
        config.setUsername("user2");
        config.setPassword("pass2");
        prop.setObjectValue(config);
        assertEquals("user2=pass2", prop.getStringValue());
        prop.recoverRunningVersion(null);
        assertEquals("username=password", prop.getStringValue());
        config = new LoginConfig();
        config.setUsername("user2");
        config.setPassword("pass2");
        prop.setObjectValue(config);
        config = new LoginConfig();
        config.setUsername("user3");
        config.setPassword("pass3");
        prop.setObjectValue(config);
        assertEquals("user3=pass3", prop.getStringValue());
        prop.recoverRunningVersion(null);
        assertEquals("username=password", prop.getStringValue());
    }

    private void checkEquals(JMeterProperty jp1, JMeterProperty jp2) {
        assertEquals(jp1, jp2);
        assertEquals(jp2, jp1);
        assertEquals(jp1, jp1);
        assertEquals(jp2, jp2);
        assertEquals(jp1.hashCode(), jp2.hashCode());

    }

    private void checkNotEquals(JMeterProperty jp1, JMeterProperty jp2) {
        assertEquals(jp1, jp1);
        assertEquals(jp2, jp2);
        assertFalse(jp1.equals(jp2));
        assertFalse(jp2.equals(jp1));
        // do not check hashcodes; unequal objects may have equal hashcodes
    }

    @Test
    public void testBooleanEquality() throws Exception {
        BooleanProperty jpn1 = new BooleanProperty();
        BooleanProperty jpn2 = new BooleanProperty();
        BooleanProperty jp1 = new BooleanProperty("name1", true);
        BooleanProperty jp2 = new BooleanProperty("name1", true);
        BooleanProperty jp3 = new BooleanProperty("name2", true);
        BooleanProperty jp4 = new BooleanProperty("name2", false);
        checkEquals(jpn1, jpn2);
        checkNotEquals(jpn1, jp1);
        checkNotEquals(jpn1, jp2);
        checkEquals(jp1, jp2);
        checkNotEquals(jp1, jp3);
        checkNotEquals(jp2, jp3);
        checkNotEquals(jp3, jp4);
    }

    @Test
    public void testDoubleEquality() throws Exception {
        DoubleProperty jpn1 = new DoubleProperty();
        DoubleProperty jpn2 = new DoubleProperty();
        DoubleProperty jp1 = new DoubleProperty("name1", 123.4);
        DoubleProperty jp2 = new DoubleProperty("name1", 123.4);
        DoubleProperty jp3 = new DoubleProperty("name2", -123.4);
        DoubleProperty jp4 = new DoubleProperty("name2", 123.4);
        DoubleProperty jp5 = new DoubleProperty("name2", Double.NEGATIVE_INFINITY);
        DoubleProperty jp6 = new DoubleProperty("name2", Double.NEGATIVE_INFINITY);
        DoubleProperty jp7 = new DoubleProperty("name2", Double.POSITIVE_INFINITY);
        DoubleProperty jp8 = new DoubleProperty("name2", Double.POSITIVE_INFINITY);
        DoubleProperty jp9 = new DoubleProperty("name2", Double.NaN);
        DoubleProperty jp10 = new DoubleProperty("name2", Double.NaN);
        DoubleProperty jp11 = new DoubleProperty("name1", Double.NaN);
        DoubleProperty jp12 = new DoubleProperty("name1", Double.MIN_VALUE);
        DoubleProperty jp13 = new DoubleProperty("name2", Double.MIN_VALUE);
        DoubleProperty jp14 = new DoubleProperty("name2", Double.MIN_VALUE);
        DoubleProperty jp15 = new DoubleProperty("name1", Double.MAX_VALUE);
        DoubleProperty jp16 = new DoubleProperty("name2", Double.MAX_VALUE);
        DoubleProperty jp17 = new DoubleProperty("name2", Double.MAX_VALUE);
        checkEquals(jpn1, jpn2);
        checkNotEquals(jpn1, jp1);
        checkNotEquals(jpn1, jp2);
        checkEquals(jp1, jp2);
        checkNotEquals(jp1, jp3);
        checkNotEquals(jp2, jp3);
        checkNotEquals(jp3, jp4);
        checkEquals(jp5, jp6);
        checkNotEquals(jp3, jp6);
        checkEquals(jp7, jp8);
        checkNotEquals(jp4, jp7);
        checkNotEquals(jp8, jp9);
        checkEquals(jp9, jp10);
        checkNotEquals(jp10, jp11);
        checkNotEquals(jp5, jp10);
        checkNotEquals(jp12, jp14);
        checkEquals(jp13, jp14);
        checkNotEquals(jp15, jp16);
        checkEquals(jp16, jp17);
    }

    @Test
    public void testFloatEquality() throws Exception {
        FloatProperty jp1 = new FloatProperty("name1", 123.4f);
        FloatProperty jp2 = new FloatProperty("name1", 123.4f);
        FloatProperty jp3 = new FloatProperty("name2", -123.4f);
        FloatProperty jp4 = new FloatProperty("name2", 123.4f);
        FloatProperty jp5 = new FloatProperty("name2", Float.NEGATIVE_INFINITY);
        FloatProperty jp6 = new FloatProperty("name2", Float.NEGATIVE_INFINITY);
        FloatProperty jp7 = new FloatProperty("name2", Float.POSITIVE_INFINITY);
        FloatProperty jp8 = new FloatProperty("name2", Float.POSITIVE_INFINITY);
        FloatProperty jp9 = new FloatProperty("name2", Float.NaN);
        FloatProperty jp10 = new FloatProperty("name2", Float.NaN);
        FloatProperty jp11 = new FloatProperty("name1", Float.NaN);
        FloatProperty jp12 = new FloatProperty("name1", Float.MIN_VALUE);
        FloatProperty jp13 = new FloatProperty("name2", Float.MIN_VALUE);
        FloatProperty jp14 = new FloatProperty("name2", Float.MIN_VALUE);
        FloatProperty jp15 = new FloatProperty("name1", Float.MAX_VALUE);
        FloatProperty jp16 = new FloatProperty("name2", Float.MAX_VALUE);
        FloatProperty jp17 = new FloatProperty("name2", Float.MAX_VALUE);
        checkEquals(jp1, jp2);
        checkNotEquals(jp1, jp3);
        checkNotEquals(jp2, jp3);
        checkNotEquals(jp3, jp4);
        checkEquals(jp5, jp6);
        checkNotEquals(jp3, jp6);
        checkEquals(jp7, jp8);
        checkNotEquals(jp4, jp7);
        checkNotEquals(jp8, jp9);
        checkEquals(jp9, jp10);
        checkNotEquals(jp10, jp11);
        checkNotEquals(jp5, jp10);
        checkNotEquals(jp12, jp14);
        checkEquals(jp13, jp14);
        checkNotEquals(jp15, jp16);
        checkEquals(jp16, jp17);
    }

    @Test
    public void testIntegerEquality() throws Exception {
        IntegerProperty jp1 = new IntegerProperty("name1", 123);
        IntegerProperty jp2 = new IntegerProperty("name1", 123);
        IntegerProperty jp3 = new IntegerProperty("name2", -123);
        IntegerProperty jp4 = new IntegerProperty("name2", 123);
        IntegerProperty jp5 = new IntegerProperty("name2", Integer.MIN_VALUE);
        IntegerProperty jp6 = new IntegerProperty("name2", Integer.MIN_VALUE);
        IntegerProperty jp7 = new IntegerProperty("name2", Integer.MAX_VALUE);
        IntegerProperty jp8 = new IntegerProperty("name2", Integer.MAX_VALUE);
        IntegerProperty jp9 = new IntegerProperty("name1", Integer.MIN_VALUE);
        IntegerProperty jp10 = new IntegerProperty("name1", Integer.MAX_VALUE);
        checkEquals(jp1, jp2);
        checkNotEquals(jp1, jp3);
        checkNotEquals(jp2, jp3);
        checkNotEquals(jp3, jp4);
        checkEquals(jp5, jp6);
        checkNotEquals(jp3, jp6);
        checkEquals(jp7, jp8);
        checkNotEquals(jp4, jp7);
        checkNotEquals(jp9, jp5);
        checkNotEquals(jp10, jp7);
        checkNotEquals(jp9, jp10);
        try {
            new IntegerProperty(null);
            fail("Should have generated an Illegal Argument Exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            new IntegerProperty(null, 0);
            fail("Should have generated an Illegal Argument Exception");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLongEquality() throws Exception {
        LongProperty jp1 = new LongProperty("name1", 123);
        LongProperty jp2 = new LongProperty("name1", 123);
        LongProperty jp3 = new LongProperty("name2", -123);
        LongProperty jp4 = new LongProperty("name2", 123);
        LongProperty jp5 = new LongProperty("name2", Long.MIN_VALUE);
        LongProperty jp6 = new LongProperty("name2", Long.MIN_VALUE);
        LongProperty jp7 = new LongProperty("name2", Long.MAX_VALUE);
        LongProperty jp8 = new LongProperty("name2", Long.MAX_VALUE);
        LongProperty jp9 = new LongProperty("name1", Long.MIN_VALUE);
        LongProperty jp10 = new LongProperty("name1", Long.MAX_VALUE);
        checkEquals(jp1, jp2);
        checkNotEquals(jp1, jp3);
        checkNotEquals(jp2, jp3);
        checkNotEquals(jp3, jp4);
        checkEquals(jp5, jp6);
        checkNotEquals(jp3, jp6);
        checkEquals(jp7, jp8);
        checkNotEquals(jp4, jp7);
        checkNotEquals(jp9, jp5);
        checkNotEquals(jp10, jp7);
        checkNotEquals(jp9, jp10);
        try {
            new LongProperty(null, 0L);
            fail("Should have generated an Illegal Argument Exception");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testMapEquality() throws Exception {
        try {
            new MapProperty(null, null);
            fail("Should have generated an Illegal Argument Exception");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testNullEquality() throws Exception {
        NullProperty jpn1 = new NullProperty();
        NullProperty jpn2 = new NullProperty();
        try {
            new NullProperty(null);
            fail("Should have generated an Illegal Argument Exception");
        } catch (IllegalArgumentException e) {
        }
        NullProperty jp1 = new NullProperty("name1");
        NullProperty jp2 = new NullProperty("name1");
        NullProperty jp3 = new NullProperty("name2");
        NullProperty jp4 = new NullProperty("name2");
        checkEquals(jpn1, jpn2);
        checkNotEquals(jpn1, jp1);
        checkEquals(jp1, jp2);
        checkNotEquals(jp1, jp3);
        checkNotEquals(jp2, jp3);
        checkEquals(jp3, jp4);
    }

    @Test
    public void testStringEquality() throws Exception {
        StringProperty jpn1 = new StringProperty();
        StringProperty jpn2 = new StringProperty();
        StringProperty jp1 = new StringProperty("name1", "value1");
        StringProperty jp2 = new StringProperty("name1", "value1");
        StringProperty jp3 = new StringProperty("name2", "value1");
        StringProperty jp4 = new StringProperty("name2", "value2");
        StringProperty jp5 = new StringProperty("name1", null);
        StringProperty jp6 = new StringProperty("name1", null);
        StringProperty jp7 = new StringProperty("name2", null);
        checkEquals(jpn1, jpn2);
        checkNotEquals(jpn1, jp1);
        checkEquals(jp1, jp2);
        checkNotEquals(jp1, jp3);
        checkNotEquals(jp2, jp3);
        checkNotEquals(jp3, jp4);
        checkEquals(jp5, jp6);
        checkNotEquals(jp3, jp5);
        checkNotEquals(jp6, jp7);
        try {
            new StringProperty(null, "");
            fail("Should have generated an Illegal Argument Exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            new StringProperty(null, null);
            fail("Should have generated an Illegal Argument Exception");
        } catch (IllegalArgumentException e) {
        }

    }
    @Test
    public void testAddingProperties() throws Exception {
        CollectionProperty coll = new CollectionProperty();
        coll.addItem("joe");
        coll.addProperty(new FunctionProperty());
        assertEquals("joe", coll.get(0).getStringValue());
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", coll.get(1).getClass().getName());
    }
}
