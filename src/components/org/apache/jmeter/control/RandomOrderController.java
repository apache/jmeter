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
package org.apache.jmeter.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jmeter.testelement.TestElement;

/**
 * A controller that runs its children each at most once, but in a random order.
 *
 */
public class RandomOrderController extends GenericController implements Serializable {

    private static final long serialVersionUID = 240L;

    /**
     * Create a new RandomOrderController.
     */
    public RandomOrderController() {
    }

    /**
     * @see GenericController#initialize()
     */
    @Override
    public void initialize() {
        super.initialize();
        this.reorder();
    }

    /**
     * @see GenericController#reInitialize()
     */
    @Override
    protected void reInitialize() {
        super.reInitialize();
        this.reorder();
    }

    /**
     * Replace the subControllersAndSamplers list with a reordered ArrayList.
     */
    private void reorder() {
        int numElements = this.subControllersAndSamplers.size();

        // Create a new list containing numElements null elements.
        List<TestElement> reordered = new ArrayList<TestElement>(this.subControllersAndSamplers.size());
        for (int i = 0; i < numElements; i++) {
            reordered.add(null);
        }

        // Insert the subControllersAndSamplers into random list positions.
        for (Iterator<TestElement> i = this.subControllersAndSamplers.iterator(); i.hasNext();) {
            int idx = (int) Math.floor(Math.random() * reordered.size());
            while (true) {
                if (idx == numElements) {
                    idx = 0;
                }
                if (reordered.get(idx) == null) {
                    reordered.set(idx, i.next());
                    break;
                }
                idx++;
            }
        }

        // Replace subControllersAndSamplers with reordered copy.
        this.subControllersAndSamplers = reordered;
    }
}
