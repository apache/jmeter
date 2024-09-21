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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.logging.log4j.LoggingException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestClassFinder {

    private String[] libDirs;

    private String getJMeterHome() throws Exception {
        JMeterTestUtils.setupJMeterHome();
        String path = JMeterUtils.getJMeterHome() + "/lib";
        return Paths.get(path).toRealPath().toString();
    }

    @BeforeEach
    public void setUp() throws Exception {
        libDirs = new String[] { getJMeterHome() };
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArray() throws IOException {
        @SuppressWarnings("deprecation")
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { Exception.class });
        MatcherAssert.assertThat(findClassesThatExtend, CoreMatchers.hasItem(LoggingException.class.getName()));
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayTrue() throws Exception {
        @SuppressWarnings("deprecation")
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { Object.class },
                true);
        assertFalse(findClassesThatExtend.stream().noneMatch(s -> s.contains("$")));
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayFalse() throws Exception {
        @SuppressWarnings("deprecation")
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { Exception.class },
                false);
        assertTrue(findClassesThatExtend.stream().noneMatch(s -> s.contains("$")));
        MatcherAssert.assertThat(findClassesThatExtend, CoreMatchers.hasItem(LoggingException.class.getName()));
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayBooleanStringString() throws Exception {
        @SuppressWarnings("deprecation")
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { Exception.class },
                false,
                "org.apache.log",
                "core");
        assertTrue(findClassesThatExtend.stream().noneMatch(s -> s.contains("core")));
        assertFalse(findClassesThatExtend.isEmpty());
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayBooleanStringStringTrue() throws Exception {
        @SuppressWarnings("deprecation")
        List<String> annotatedClasses = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { java.beans.Transient.class },
                false,
                null,
                null,
                true);
        assertFalse(annotatedClasses.isEmpty());
    }

    @Test
    public void testFindAnnotatedClasses() throws Exception {
        @SuppressWarnings({"deprecation", "unchecked"})
        List<String> annotatedClasses = ClassFinder.findAnnotatedClasses(
                libDirs,
                new Class[] { java.beans.Transient.class});
        assertFalse(annotatedClasses.isEmpty());
    }

    @Test
    public void testFindAnnotatedInnerClasses() throws Exception {
        @SuppressWarnings({"deprecation", "unchecked"})
        List<String> annotatedClasses = ClassFinder.findAnnotatedClasses(libDirs,
                new Class[] { java.lang.Deprecated.class}, true);
        assertTrue(annotatedClasses.stream().anyMatch(s->s.contains("$")));
    }

    @Test
    public void testFindClasses() throws IOException {
        @SuppressWarnings("deprecation")
        List<String> classes = ClassFinder.findClasses(libDirs, className -> true);
        assertFalse(classes.isEmpty());
    }

    @Test
    public void testFindClassesNone() throws IOException {
        @SuppressWarnings("deprecation")
        List<String> classes = ClassFinder.findClasses(libDirs, className -> false);
        assertTrue(classes.isEmpty());
    }

}
