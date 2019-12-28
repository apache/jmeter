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

package org.apache.jmeter.util;

import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * The extension starts and initializes {@link JMeterContext} before each test.
 * The extension can inject {@link JMeterVariables} to a test method parameter.
 */
public class JMeterContextExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        return parameterType == JMeterContext.class
                || parameterType == JMeterVariables.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Class<?> parameterType = parameterContext.getParameter().getType();
        JMeterContext jMeterContext = JMeterContextService.getContext();
        if (parameterType == JMeterContext.class) {
            return jMeterContext;
        }
        if (parameterType == JMeterVariables.class) {
            return jMeterContext.getVariables();
        }
        throw new IllegalArgumentException(parameterContext.toString());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        JMeterContextService.getContext().clear();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        JMeterContext jMeterContext = JMeterContextService.getContext();
        jMeterContext.clear();
        jMeterContext.setVariables(new JMeterVariables());
    }
}
