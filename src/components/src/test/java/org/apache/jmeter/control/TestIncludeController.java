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

package org.apache.jmeter.control;

import static org.junit.Assert.assertEquals;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Test;

public class TestIncludeController extends JMeterTestCase {


    @Test
    public void testGetIncludePathWithVariable() {
        String varName = "testGetIncludePathWithVariable_file_name";
        String varValue = "C:\\testPath\\testFile.jmx";
        String varExpression = "${" + varName + "}";

        JMeterVariables vars = new JMeterVariables();
        JMeterContext jmctx = JMeterContextService.getContext();

        jmctx.setVariables(vars);
        vars.put(varName, varValue);

        IncludeController includeController = new IncludeController();
        includeController.setIncludePath(varExpression);

        assertEquals(varValue, includeController.getIncludePathAsFunction());
    }

    @Test
    public void testGetIncludePathWithVariables() {
        String dirName = "var1";
        String dirValue = "/tmp/path/test";

        String fileName = "var2";
        String fileValue = "testFile.jmx";

        String varExpression = "${" + dirName + "}/${" + fileName + "}";

        JMeterVariables vars = new JMeterVariables();
        JMeterContext jmctx = JMeterContextService.getContext();

        jmctx.setVariables(vars);
        vars.put(dirName, dirValue);
        vars.put(fileName, fileValue);

        IncludeController includeController = new IncludeController();
        includeController.setIncludePath(varExpression);

        assertEquals(dirValue + "/" + fileValue, includeController.getIncludePathAsFunction());
    }


    @Test
    public void testGetIncludePathWithSimpleString() {
        String varValue = "C:\\testPath\\testFile.jmx";

        JMeterVariables vars = new JMeterVariables();
        JMeterContext jmctx = JMeterContextService.getContext();

        IncludeController includeController = new IncludeController();
        includeController.setIncludePath(varValue);

        assertEquals(varValue, includeController.getIncludePathAsFunction());
    }

    @Test
    public void testGetIncludePathWithEmptyString() {
        String varValue = "";

        IncludeController includeController = new IncludeController();
        includeController.setIncludePath(varValue);

        assertEquals(varValue, includeController.getIncludePathAsFunction());
    }

}
