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

package org.apache.jmeter.report.processor.graph;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.report.core.Sample;

/**
 * The class IndexedNameSelector provides a projection from a sample to its name
 * and maintains an index of the projected names.
 *
 * @since 3.0
 */
public class IndexedNameSelector implements GraphKeysSelector {

    /** The names. */
    private final List<String> names = new ArrayList<>();

    /**
     * Gets the names.
     *
     * @return the names
     */
    public final List<String> getNames() {
        return names;
    }

    @Override
    public Double select(Sample sample) {
        String name = sample.getName();
        int index = names.indexOf(name);
        if (index < 0) {
            names.add(name);
            index = names.size() - 1;
        }
        return (double) index;
    }

}
