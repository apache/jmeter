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

package org.apache.jorphan.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.util.JMeterError;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for classes that use Functors */
class TestFunctor extends JMeterTestCase {

    interface HasName {
        String getName();
    }

    interface HasString {
        String getString(String s);
    }

    static class Test1 implements HasName {
        private final String name;
        public Test1(){
            this("");
        }
        public Test1(String s){
            name=s;
        }
        @Override
        public String getName(){
            return name;
        }
        public String getString(String s){
            return s;
        }
    }
    static class Test1a extends Test1{
        Test1a(){
            super("1a");
        }
        Test1a(String s){
            super("1a:"+s);
        }
        @Override
        public String getName(){
            return super.getName()+".";
        }
    }
    static class Test2 implements HasName, HasString {
        private final String name;
        public Test2(){
            this("");
        }
        public Test2(String s){
            name=s;
        }
        @Override
        public String getName(){
            return name;
        }
        @Override
        public String getString(String s){
            return s;
        }
    }

    @BeforeEach
    void setUp(){
        Configurator.setAllLevels(Functor.class.getName(), Level.FATAL);
    }

    @Test
    void testName() throws Exception{
        Functor f1 = new Functor("getName");
        Functor f2 = new Functor("getName");
        Functor f1a = new Functor("getName");
        Test1 t1 = new Test1("t1");
        Test2 t2 = new Test2("t2");
        Test1a t1a = new Test1a("aa");
        assertEquals("t1",f1.invoke(t1));
        assertThrows(JMeterError.class, () -> f1.invoke(t2));
        assertEquals("t2",f2.invoke(t2));
        assertEquals("1a:aa.",f1a.invoke(t1a));
        assertThrows(JMeterError.class, () -> f1a.invoke(t1));
        // OK (currently) to invoke using sub-class
        assertEquals("1a:aa.",f1.invoke(t1a));
    }

    @Test
    void testNameTypes() throws Exception{
        Functor f = new Functor("getString",new Class[]{String.class});
        Functor f2 = new Functor("getString");// Args will be provided later
        Test1 t1 = new Test1("t1");
        assertEquals("x1",f.invoke(t1,new String[]{"x1"}));
        assertThrows(JMeterError.class, () -> f.invoke(t1));
        assertEquals("x2",f2.invoke(t1,new String[]{"x2"}));
        assertThrows(JMeterError.class, () -> f2.invoke(t1));
    }

    @Test
    void testObjectName() throws Exception{
        Test1 t1 = new Test1("t1");
        Test2 t2 = new Test2("t2");
        Functor f1 = new Functor(t1,"getName");
        assertEquals("t1",f1.invoke(t1));
        assertEquals("t1",f1.invoke(t2)); // should use original object
    }

    // Check how Class definition behaves
    @Test
    void testClass() throws Exception{
        Test1 t1 = new Test1("t1");
        Test1 t1a = new Test1a("t1a");
        Test2 t2 = new Test2("t2");
        Functor f1 = new Functor(HasName.class,"getName");
        assertEquals("t1",f1.invoke(t1));
        assertEquals("1a:t1a.",f1.invoke(t1a));
        assertEquals("t2",f1.invoke(t2));
        assertThrows(IllegalStateException.class, () -> f1.invoke());
        Functor f2 = new Functor(HasString.class,"getString");
        assertEquals("xyz",f2.invoke(t2,new String[]{"xyz"}));
        assertThrows(JMeterError.class, () -> f2.invoke(t1,new String[]{"xyz"}));
        Functor f3 = new Functor(t2,"getString");
        assertEquals("xyz",f3.invoke(t2,new Object[]{"xyz"}));

        Properties p = new Properties();
        p.put("Name","Value");
        Functor fk = new Functor(Map.Entry.class,"getKey");
        Functor fv = new Functor(Map.Entry.class,"getValue");
        Object o = p.entrySet().iterator().next();
        assertEquals("Name",fk.invoke(o));
        assertEquals("Value",fv.invoke(o));
    }

    @Test
    void testBadParameters() throws Exception{
        assertThrows(IllegalArgumentException.class, () -> new Functor(null));
        assertThrows(IllegalArgumentException.class, () -> new Functor(null, new Class[] {}));
        assertThrows(IllegalArgumentException.class, () -> new Functor(null, new Object[] {}));
        assertThrows(IllegalArgumentException.class, () -> new Functor(String.class, null));
        final Object someInvokee = new Object();
        assertThrows(IllegalArgumentException.class, () -> new Functor(someInvokee, null));
        assertThrows(IllegalArgumentException.class, () -> new Functor(someInvokee, null, new Class[] {}));
        assertThrows(IllegalArgumentException.class, () -> new Functor(someInvokee, null, new Object[] {}));
    }

    @Test
    void testIllegalState() throws Exception{
        Functor f = new Functor("method");
        assertThrows(IllegalStateException.class, ()-> f.invoke());
        assertThrows(IllegalStateException.class, () -> f.invoke(new Object[]{}));
    }
}
