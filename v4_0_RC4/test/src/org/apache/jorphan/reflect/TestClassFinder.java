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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jmeter.junit.JMeterTestUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.logging.log4j.LoggingException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestClassFinder {

    private String[] libDirs;

    private String getJMeterHome() throws Exception {
        JMeterTestUtils.setupJMeterHome();
        String path = JMeterUtils.getJMeterHome() + "/lib";
        return Paths.get(path).toRealPath().toString();
    }

    @Before
    public void setUp() throws Exception {
        libDirs = new String[] { getJMeterHome() };
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArray() throws IOException {
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { Exception.class });
        Assert.assertThat(findClassesThatExtend, CoreMatchers.hasItem(LoggingException.class.getName()));
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayTrue() throws Exception {
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { Object.class },
                true);
        Assert.assertFalse(
                findClassesThatExtend.stream().filter(s -> s.contains("$")).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayFalse() throws Exception {
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { Exception.class },
                false);
        Assert.assertTrue(
                findClassesThatExtend.stream().filter(s -> s.contains("$")).collect(Collectors.toList()).isEmpty());
        Assert.assertThat(findClassesThatExtend, CoreMatchers.hasItem(LoggingException.class.getName()));
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayBooleanStringString() throws Exception {
        List<String> findClassesThatExtend = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { Exception.class },
                false,
                "org.apache.log",
                "core");
        Assert.assertTrue(
                findClassesThatExtend.stream().filter(s -> s.contains("core")).collect(Collectors.toList()).isEmpty());
        Assert.assertFalse(findClassesThatExtend.isEmpty());
    }

    @Test
    public void testFindClassesThatExtendStringArrayClassOfQArrayBooleanStringStringTrue() throws Exception {
        List<String> annotatedClasses = ClassFinder.findClassesThatExtend(
                libDirs,
                new Class<?>[] { java.beans.Transient.class },
                false,
                null,
                null,
                true);
        Assert.assertFalse(annotatedClasses.isEmpty());
    }

    @Test
    public void testFindAnnotatedClasses() throws Exception {
        @SuppressWarnings("unchecked")
        List<String> annotatedClasses = ClassFinder.findAnnotatedClasses(
                libDirs,
                new Class[] { java.beans.Transient.class});
        Assert.assertFalse(annotatedClasses.isEmpty());
    }

    @Test
    public void testFindAllClassesInJar() throws Exception {
        List<Path> jarsPaths = Files.find(Paths.get(libDirs[0]), 1, (p, a) -> String.valueOf(p).endsWith(".jar"))
                .collect(Collectors.toList());
        for (Path jarPath : jarsPaths) {
            if (!ClassFinder.findClasses(new String[] { jarPath.toRealPath().toString() }, c -> true).isEmpty()) {
                // ok, we found an annotated class
                return;
            }
        }
        Assert.fail("No classes found in: " + jarsPaths);
    }

    @Test
    public void testFindAnnotatedInnerClasses() throws Exception {
        @SuppressWarnings("unchecked")
        List<String> annotatedClasses = ClassFinder.findAnnotatedClasses(libDirs,
                new Class[] { java.lang.Deprecated.class}, true);
        Assert.assertTrue(annotatedClasses.stream().anyMatch(s->s.contains("$")));
    }

    @Test
    public void testFindClasses() throws IOException {
        Assert.assertFalse(ClassFinder.findClasses(libDirs, className -> true).isEmpty());
    }

    @Test
    public void testFindClassesNone() throws IOException {
        Assert.assertTrue(ClassFinder.findClasses(libDirs, className -> false).isEmpty());
    }

}
