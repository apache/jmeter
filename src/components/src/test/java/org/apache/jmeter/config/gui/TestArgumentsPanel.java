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

package org.apache.jmeter.config.gui;

import static org.junit.Assert.assertEquals;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.junit.Test;

/**
 * A GUI panel allowing the user to enter name-value argument pairs. These
 * arguments (or parameters) are usually used to provide configuration values
 * for some other component.
 * 
 */
public class TestArgumentsPanel {
        

        /**
         * Test that adding an argument to the table results in an appropriate
         * TestElement being created.
         * 
         * @throws Exception
         *             if an exception occurred during the test
         */
        @Test
        public void testArgumentCreation() throws Exception {
            ArgumentsPanel gui = new ArgumentsPanel();
            gui.tableModel.addRow(new Argument());
            gui.tableModel.setValueAt("howdy", 0, 0);
            gui.tableModel.addRow(new Argument());
            gui.tableModel.setValueAt("doody", 0, 1);

            assertEquals("=", ((Argument) ((Arguments) gui.createTestElement()).getArguments().get(0).getObjectValue())
                    .getMetaData());
        }
}
