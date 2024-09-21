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

package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.functions.gui.FunctionHelper;

import com.google.auto.service.AutoService;

@AutoService(Command.class)
public class CreateFunctionDialog extends AbstractAction {

    private static final Set<String> commands;
    static {
        commands = new HashSet<>();
        commands.add(ActionNames.FUNCTIONS);
    }

    public CreateFunctionDialog() {
        super();
    }

    /**
     * Provide the list of Action names that are available in this command.
     */
    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public void doAction(ActionEvent event) {
        FunctionHelper helper = new FunctionHelper(getParentFrame(event));
        helper.setVisible(true);
    }
}
