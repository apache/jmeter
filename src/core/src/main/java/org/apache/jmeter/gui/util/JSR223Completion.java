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

package org.apache.jmeter.gui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.DescWindowVisibility;
import org.fife.ui.autocomplete.FunctionCompletion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.fife.ui.autocomplete.VariableCompletion;

/**
 * Installs Ctrl+Space completion for the bindings exposed to JSR223 scripts.
 */
public final class JSR223Completion {

    public static final String SAMPLE_RESULT_BINDING = "jsr223.sampleResultBinding";

    private JSR223Completion() {
    }

    public static AutoCompletion install(JSyntaxTextArea area) {
        return install(area, false);
    }

    public static AutoCompletion install(JSyntaxTextArea area, boolean hasSampleResultBinding) {
        AutoCompletion completion = new AutoCompletion(createProvider(hasSampleResultBinding));
        completion.setAutoActivationEnabled(false);
        completion.setDescWindowVisibility(DescWindowVisibility.ALWAYS);
        completion.setParameterAssistanceEnabled(true);
        completion.install(area);
        return completion;
    }

    static DefaultCompletionProvider createProvider(boolean hasSampleResultBinding) {
        DefaultCompletionProvider provider = new DefaultCompletionProvider() {
            @Override
            protected boolean isValidChar(char ch) {
                return super.isValidChar(ch) || ch == '.';
            }
        };
        provider.setParameterizedCompletionParams('(', ", ", ')');

        variable(provider, "vars", "JMeterVariables", "Thread-local JMeter variables");
        variable(provider, "props", "java.util.Properties", "JMeter properties shared by all threads");
        variable(provider, "ctx", "JMeterContext", "Current thread context");
        variable(provider, "log", "org.slf4j.Logger", "Logger for jmeter.log");
        variable(provider, "sampler", "Sampler", "The current sampler");
        variable(provider, "prev", "SampleResult", "Previous sample result");
        variable(provider, "Label", "String", "Element name");
        variable(provider, "FileName", "String", "Script file name");
        variable(provider, "Parameters", "String", "Parameters field as one string");
        variable(provider, "args", "String[]", "Parameters split on whitespace");
        variable(provider, "OUT", "java.io.PrintStream", "System.out");

        method(provider, "vars.get", "String", "Get variable value", "String name");
        method(provider, "vars.put", "void", "Set variable value", "String name", "String value");
        method(provider, "vars.getObject", "Object", "Get object variable", "String name");
        method(provider, "vars.putObject", "void", "Set object variable", "String name", "Object value");
        method(provider, "vars.remove", "void", "Remove variable", "String name");
        method(provider, "vars.getIteration", "int", "Current iteration number");

        method(provider, "props.get", "Object", "Get property", "Object key");
        method(provider, "props.getProperty", "String", "Get property", "String name");
        method(provider, "props.put", "Object", "Set property", "Object key", "Object value");
        method(provider, "props.setProperty", "Object", "Set property", "String name", "String value");

        for (String level : new String[] {"debug", "info", "warn", "error"}) {
            method(provider, "log." + level, "void", "Log at " + level.toUpperCase(Locale.ROOT) + " level", "String msg");
        }

        method(provider, "ctx.getThreadNum", "int", "Thread number within its group");
        method(provider, "ctx.getPreviousResult", "SampleResult", "Previous sample result");
        method(provider, "ctx.getCurrentSampler", "Sampler", "Current sampler");
        method(provider, "ctx.getVariables", "JMeterVariables", "Thread variables");
        method(provider, "ctx.getThreadGroup", "AbstractThreadGroup", "Owning thread group");

        addSampleResultMethods(provider, "prev");
        if (hasSampleResultBinding) {
            variable(provider, "SampleResult", "SampleResult", "Result of this JSR223 element");
            addSampleResultMethods(provider, "SampleResult");
        }
        return provider;
    }

    private static void addSampleResultMethods(DefaultCompletionProvider provider, String result) {
        method(provider, result + ".getResponseDataAsString", "String", "Response body as text");
        method(provider, result + ".setResponseData", "void", "Set response body", "String data", "String encoding");
        method(provider, result + ".getResponseCode", "String", "Response code");
        method(provider, result + ".setResponseCode", "void", "Set response code", "String code");
        method(provider, result + ".setResponseMessage", "void", "Set response message", "String msg");
        method(provider, result + ".isSuccessful", "boolean", "Success flag");
        method(provider, result + ".setSuccessful", "void", "Set success flag", "boolean ok");
        method(provider, result + ".getTime", "long", "Elapsed time in ms");
        method(provider, result + ".getSampleLabel", "String", "Element name");
        method(provider, result + ".setSampleLabel", "void", "Set element name", "String label");
    }

    private static void variable(DefaultCompletionProvider provider, String name, String type, String summary) {
        VariableCompletion completion = new VariableCompletion(provider, name, type);
        completion.setShortDescription(summary);
        completion.setSummary(summary);
        provider.addCompletion(completion);
    }

    private static void method(DefaultCompletionProvider provider, String name, String returnType, String summary,
            String... params) {
        FunctionCompletion completion = new FunctionCompletion(provider, name, returnType);
        List<ParameterizedCompletion.Parameter> parameters = new ArrayList<>();
        for (String parameter : params) {
            int space = parameter.lastIndexOf(' ');
            parameters.add(new ParameterizedCompletion.Parameter(
                    parameter.substring(0, space), parameter.substring(space + 1)));
        }
        completion.setParams(parameters);
        completion.setShortDescription(summary);
        completion.setSummary(summary);
        provider.addCompletion(completion);
    }
}
