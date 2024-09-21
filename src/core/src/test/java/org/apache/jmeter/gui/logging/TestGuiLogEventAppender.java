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

package org.apache.jmeter.gui.logging;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGuiLogEventAppender {

    private static List<String> log4j2LevelErrorMessages = Collections.synchronizedList(new ArrayList<>());

    /*
     * Configure logging with GuiLogEventAppender for root logger, and override the handler of GuiLogEventAppender
     * to see if there's any log4j2 AppenderControl level error (e.g, "Recursive call to appender gui-log-event").
     */
    @BeforeAll
    public static void beforeClass() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setPackages("org.apache.jmeter.gui.logging");

        AppenderComponentBuilder appenderBuilder = builder
                .newAppender("Stdout", "CONSOLE")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
                .add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d %p %c{1.}: %m%n"));
        builder.add(appenderBuilder);

        appenderBuilder = builder
                .newAppender("gui-log-event", "GuiLogEvent")
                .add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d %p %c{1.}: %m%n"));
        builder.add(appenderBuilder);

        RootLoggerComponentBuilder rootLoggerBuilder = builder.newRootLogger(Level.INFO);
        rootLoggerBuilder.add(builder.newAppenderRef("Stdout")).add(builder.newAppenderRef("gui-log-event"));
        builder.add(rootLoggerBuilder);

        LoggerContext loggerContext = Configurator.initialize(builder.build());
        Appender guiLogEventAppender = loggerContext.getRootLogger().getAppenders().get("gui-log-event");

        guiLogEventAppender.stop();
        guiLogEventAppender.setHandler(new ErrorHandler() {
            @Override
            public void error(String msg) {
                log4j2LevelErrorMessages.add(msg);
            }

            @Override
            public void error(String msg, Throwable t) {
                log4j2LevelErrorMessages.add(msg + " " + t);
            }

            @Override
            public void error(String msg, LogEvent event, Throwable t) {
                log4j2LevelErrorMessages.add(msg + " " + t);
            }
        });
        guiLogEventAppender.start();
    }

    @BeforeEach
    public void setUp() {
        log4j2LevelErrorMessages.clear();
    }

    /*
     * Make simple logs and see whether there's any log4j2 AppenderControl level error (e.g, "Recursive call to
     * appender gui-log-event").
     * For example, if GuiLogEventAppender meets an exception while accessing GuiPackage class due to static member
     * initialization failure, the error is passed to the root logger, causing "Recursive call ...".
     */
    @Test
    public void testSimpleLogging() throws Exception {
        final Logger log = LoggerFactory.getLogger(TestGuiLogEventAppender.class);
        log.info("logger created.");
        assertTrue(log4j2LevelErrorMessages.isEmpty(), "Logging appender error: " + log4j2LevelErrorMessages);
    }
}
