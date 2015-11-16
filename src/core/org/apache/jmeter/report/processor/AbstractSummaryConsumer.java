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
package org.apache.jmeter.report.processor;

import java.util.Set;

/**
 * The class AbstractSummaryConsumer provides a base class for data of the
 * dashboard page.
 *
 * @since 2.14
 */
public abstract class AbstractSummaryConsumer extends AbstractSampleConsumer {

    public static final String RESULT_KEY = "Result";
    public static final String OVERALL_KEY = "overall";
    public static final String ITEMS_KEY = "items";
    public static final String TITLES_KEY = "titles";

    private boolean hasOverallResult;

    /**
     * Defines whether the result contains an overall item.
     *
     * @return true, if the result contains an overall item
     */
    public final boolean hasOverallResult() {
	return hasOverallResult;
    }

    /**
     * Defines whether the result contains an overall item.
     *
     * @param hasOverallResult
     *            true, if the result contains an overall item; false otherwise
     */
    public final void setHasOverallResult(boolean hasOverallResult) {
	this.hasOverallResult = hasOverallResult;
    }

    /**
     * Instantiates a new abstract summary consumer.
     */
    protected AbstractSummaryConsumer() {
    }

    /**
     * Creates a result item for the sample with the specified name.<br/>
     * If name is empty, an overall result is expected.
     * 
     * @param name
     *            the name of the sample
     * @return the map result data
     */
    protected abstract ListResultData createResultItem(String name);

    /**
     * Creates the result containing titles of columns.
     *
     * @return the list of titles
     */
    protected abstract ListResultData createResultTitles();

    /**
     * Store the result of samples consumption building from the specified samples.
     *
     * @param samples the samples
     */
    protected final void storeResult(Set<String> samples) {
	MapResultData result = new MapResultData();

	// Add headers
	result.setResult(TITLES_KEY, createResultTitles());

	// Add overall row if needed
	if (hasOverallResult) {
	    result.setResult(OVERALL_KEY, createResultItem(""));
	}

	// Build rows from samples
	ListResultData itemsResult = new ListResultData();
	result.setResult(ITEMS_KEY, itemsResult);
	if (samples != null) {
	    for (String name : samples) {
		itemsResult.addResult(createResultItem(name));
	    }
	}
	
	// Store to the context
	setLocalData(RESULT_KEY, result);
    }
}
