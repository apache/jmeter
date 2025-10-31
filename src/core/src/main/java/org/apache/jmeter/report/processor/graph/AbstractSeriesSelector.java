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

import java.util.Arrays;

import org.apache.jorphan.util.StringUtilities;


/**
 * The class AbstractSeriesSelector provide an abstract base class for
 * GraphSeriesSelector.
 *
 * @since 3.0
 */
public abstract class AbstractSeriesSelector implements GraphSeriesSelector {

    private final boolean allowsControllerDiscrimination;

    /**
     * Instantiates a new abstract series selector.
     */
    protected AbstractSeriesSelector() {
        this(false);
    }

    /**
     * Instantiates a new abstract series selector.
     *
     * @param allowsControllerDiscrimination
     *            indicates whether this selector allows to discriminate
     *            controllers
     */
    protected AbstractSeriesSelector(boolean allowsControllerDiscrimination) {
        this.allowsControllerDiscrimination = allowsControllerDiscrimination;
    }

    @Override
    public final boolean allowsControllersDiscrimination() {
        return allowsControllerDiscrimination;
    }

    /**
     * @param value String value
     * @param defaultValue Default value to use if value is empty or null
     * @return input or default value wrapped in a list
     */
    protected Iterable<String> withDefaultIfEmpty(String value, String defaultValue) {
        return Arrays.asList(StringUtilities.isBlank(value) ? defaultValue : value);
    }
}
