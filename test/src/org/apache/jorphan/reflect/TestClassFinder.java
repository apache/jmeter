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

package org.apache.jorphan.reflect;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.logging.log4j.LoggingException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class TestClassFinder {

    private static final String[] LIB_DIRS = new String[] { getJMeterHome() };

    private static String getJMeterHome() {
        if (JMeterUtils.getJMeterHome() == null) {
            return "./lib";
        }
        return JMeterUtils.getJMeterHome() + "/lib";
    }
    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArray() throws IOException {
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(LIB_DIRS,
                new Class<?>[] { Exception.class });
        Assert.assertThat(findClassesThatExtend, CoreMatchers.hasItem(LoggingException.class.getName()));
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayTrue() throws Exception {
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(LIB_DIRS,
                new Class<?>[] { Object.class }, true);
        Assert.assertFalse(
                findClassesThatExtend.stream().filter(s -> s.contains("$")).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayFalse() throws Exception {
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(LIB_DIRS,
                new Class<?>[] { Exception.class }, false);
        Assert.assertTrue(
                findClassesThatExtend.stream().filter(s -> s.contains("$")).collect(Collectors.toList()).isEmpty());
        Assert.assertThat(findClassesThatExtend, CoreMatchers.hasItem(LoggingException.class.getName()));
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayBooleanStringString() throws Exception {
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(LIB_DIRS,
                new Class<?>[] { Exception.class }, false, "org.apache.log", "core");
        Assert.assertTrue(
                findClassesThatExtend.stream().filter(s -> s.contains("core")).collect(Collectors.toList()).isEmpty());
        Assert.assertFalse(findClassesThatExtend.isEmpty());
    }

    @Test
    public void testFindClasses() throws IOException {
        Assert.assertFalse(ClassFinder.findClasses(LIB_DIRS, className -> true).isEmpty());
    }

    @Test
    public void testFindClassesNone() throws IOException {
        Assert.assertTrue(ClassFinder.findClasses(LIB_DIRS, className -> false).isEmpty());
    }

}
