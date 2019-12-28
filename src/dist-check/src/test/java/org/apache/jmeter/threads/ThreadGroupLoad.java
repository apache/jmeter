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

package org.apache.jmeter.threads;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ThreadGroupLoad {
    static {
        // Initialize properties
        JMeterTestCase.assertPrimitiveEquals(true, true);
    }

    @TempDir
    public Path tmpDir;

    @Test
    public void readJmxSavedWithJMeter26() throws IOException {
        String jmx26 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<jmeterTestPlan version=\"1.2\" properties=\"2.2\">\n" +
                "  <hashTree>\n" +
                "    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"Test Plan\" enabled=\"true\">\n" +
                "      <stringProp name=\"TestPlan.comments\"></stringProp>\n" +
                "      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n" +
                "      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n" +
                "      <elementProp name=\"TestPlan.user_defined_variables\" elementType=\"Arguments\"" +
                " guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" testname=\"User Defined Variables\n" +
                "\" enabled=\"true\">\n" +
                "        <collectionProp name=\"Arguments.arguments\"/>\n" +
                "      </elementProp>\n" +
                "      <stringProp name=\"TestPlan.user_define_classpath\"></stringProp>\n" +
                "    </TestPlan>\n" +
                "    <hashTree>\n" +
                "      <ThreadGroup guiclass=\"ThreadGroupGui\" testclass=\"ThreadGroup\" testname=\"Thread Group\" enabled=\"true\">\n" +
                "        <stringProp name=\"ThreadGroup.on_sample_error\">continue</stringProp>\n" +
                "        <elementProp name=\"ThreadGroup.main_controller\" elementType=\"LoopController\"" +
                " guiclass=\"LoopControlPanel\" testclass=\"LoopController\" testname=\"Loop Control\n" +
                "ler\" enabled=\"true\">\n" +
                "          <boolProp name=\"LoopController.continue_forever\">false</boolProp>\n" +
                "          <stringProp name=\"LoopController.loops\">1</stringProp>\n" +
                "        </elementProp>\n" +
                "        <stringProp name=\"ThreadGroup.num_threads\">1</stringProp>\n" +
                "        <stringProp name=\"ThreadGroup.ramp_time\">1</stringProp>\n" +
                "        <longProp name=\"ThreadGroup.start_time\">1570221190000</longProp>\n" +
                "        <longProp name=\"ThreadGroup.end_time\">1570221190000</longProp>\n" +
                "        <boolProp name=\"ThreadGroup.scheduler\">false</boolProp>\n" +
                "        <stringProp name=\"ThreadGroup.duration\"></stringProp>\n" +
                "        <stringProp name=\"ThreadGroup.delay\"></stringProp>\n" +
                "      </ThreadGroup>\n" +
                "      <hashTree/>\n" +
                "    </hashTree>\n" +
                "  </hashTree>\n" +
                "</jmeterTestPlan>";

        Path jmx = tmpDir.resolve("default_thread_group_2_6.xml");

        Files.write(jmx, Collections.singleton(jmx26));

        HashTree hashTree = SaveService.loadTree(jmx.toFile());
        HashTree testPlanTree = hashTree.values().iterator().next();
        ThreadGroup tg = (ThreadGroup) testPlanTree.keySet().iterator().next();

        tg.setRunningVersion(true);
        String actual = "getName: " + tg.getName() + "\n" +
                "getNumThreads: " + tg.getNumThreads() + "\n" +
                "getRampUp: " + tg.getRampUp() + "\n" +
                "getScheduler: " + tg.getScheduler() + "\n" +
                "isSameUserOnNextIteration: " + tg.isSameUserOnNextIteration() + "\n";

        Assertions.assertEquals("getName: Thread Group\n" +
                "getNumThreads: 1\n" +
                "getRampUp: 1\n" +
                "getScheduler: false\n" +
                "isSameUserOnNextIteration: true\n", actual);
    }
}
