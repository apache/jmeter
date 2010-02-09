/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jmeter.control.gui;

import org.apache.jmeter.control.RandomOrderController;
import org.apache.jmeter.testelement.TestElement;

/**
 * GUI for RandomOrderController.
 *
 */
public class RandomOrderControllerGui extends LogicControllerGui {

    private static final long serialVersionUID = 240L;

    @Override
    public String getLabelResource() {
        return "random_order_control_title"; // $NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        RandomOrderController ic = new RandomOrderController();
        modifyTestElement(ic);
        return ic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement ic) {
        configureTestElement(ic);
    }

}
