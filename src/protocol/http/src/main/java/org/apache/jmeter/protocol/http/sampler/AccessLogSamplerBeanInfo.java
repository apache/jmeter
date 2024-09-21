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

package org.apache.jmeter.protocol.http.sampler;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.apache.jmeter.protocol.http.util.accesslog.Filter;
import org.apache.jmeter.protocol.http.util.accesslog.LogParser;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.FileEditor;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.LogAndIgnoreServiceLoadExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessLogSamplerBeanInfo extends BeanInfoSupport {
    private static final Logger log = LoggerFactory.getLogger(AccessLogSamplerBeanInfo.class);
    private static final List<String> LOG_PARSER_CLASSES =
            JMeterUtils.loadServicesAndScanJars(
                    LogParser.class,
                    ServiceLoader.load(LogParser.class),
                    Thread.currentThread().getContextClassLoader(),
                    new LogAndIgnoreServiceLoadExceptionHandler(log)
            ).stream()
                    .map(s -> s.getClass().getName())
                    .sorted()
                    .collect(Collectors.toList());

    public AccessLogSamplerBeanInfo() {
        super(AccessLogSampler.class);
        log.debug("Entered access log sampler bean info");
        createPropertyGroup("defaults",  // $NON-NLS-1$
                new String[] { "protocol", "domain", "portString", "imageParsing" });// $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$

        createPropertyGroup("plugins",  // $NON-NLS-1$
                new String[] { "parserClassName", "filterClassName" }); // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$

        createPropertyGroup("accesslogfile",  // $NON-NLS-1$
                new String[] { "logFile" }); // $NON-NLS-1$

        PropertyDescriptor p;

        p = property("parserClassName");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, AccessLogSampler.DEFAULT_CLASS);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);

        log.debug("found parsers: {}", LOG_PARSER_CLASSES);
        p.setValue(TAGS, LOG_PARSER_CLASSES.toArray(new String[0]));

        p = property("filterClassName"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.FALSE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        String[] classes = JMeterUtils.loadServicesAndScanJars(
                        Filter.class,
                        ServiceLoader.load(Filter.class),
                        Thread.currentThread().getContextClassLoader(),
                        new LogAndIgnoreServiceLoadExceptionHandler(log)
                ).stream()
                .map(s -> s.getClass().getName())
                .sorted()
                .toArray(String[]::new);
        p.setValue(TAGS, classes);

        p = property("logFile"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setPropertyEditorClass(FileEditor.class);

        p = property("domain"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");

        p = property("protocol"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "http"); // $NON-NLS-1$
        p.setValue(DEFAULT_NOT_SAVED, Boolean.TRUE);

        p = property("portString"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, ""); // $NON-NLS-1$

        p = property("imageParsing"); // $NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
        p.setValue(NOT_OTHER, Boolean.TRUE);
        log.debug("Got to end of access log sampler bean info init");
    }

}
