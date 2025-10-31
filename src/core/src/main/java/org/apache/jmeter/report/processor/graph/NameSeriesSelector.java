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

import org.apache.jmeter.report.core.Sample;

/**
 * The class NameSerieSelector provides a projection of a sample to its name.
 *
 * @since 3.0
 */
public class NameSeriesSelector extends AbstractSeriesSelector {

    /**
     * Instantiates a new name series selector.
     */
    public NameSeriesSelector() {
        super(true);
    }

    @Override
    public Iterable<String> select(Sample sample) {
        return withDefaultIfEmpty(sample.getName(), "EMPTY_SERIE_NAME"); //$NON-NLS-1$
    }

}
