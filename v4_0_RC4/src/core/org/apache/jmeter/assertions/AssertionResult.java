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

package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.gui.Searchable;

/**
 * Implements Response Assertion checking.
 */
public class AssertionResult implements Serializable, Searchable {
    public static final String RESPONSE_WAS_NULL = "Response was null"; // $NON-NLS-1$

    private static final long serialVersionUID = 240L;

    /** Name of the assertion. */
    private final String name;

    /** True if the assertion failed. */
    private boolean failure;

    /** True if there was an error checking the assertion. */
    private boolean error;

    /** A message describing the failure. */
    private String failureMessage;

    /**
     * Create a new Assertion Result. The result will indicate no failure or
     * error.
     * @deprecated - use the named constructor
     */
    @Deprecated
    public AssertionResult() { // Needs to be public for tests
        this.name = null;
    }

    /**
     * Create a new Assertion Result. The result will indicate no failure or
     * error.
     *
     * @param name the name of the assertion
     */
    public AssertionResult(String name) {
        this.name = name;
    }

    /**
     * Get the name of the assertion
     *
     * @return the name of the assertion
     */
    public String getName() {
        return name;
    }

    /**
     * Check if the assertion failed. If it failed, the failure message may give
     * more details about the failure.
     *
     * @return true if the assertion failed, false if the sample met the
     *         assertion criteria
     */
    public boolean isFailure() {
        return failure;
    }

    /**
     * Check if an error occurred while checking the assertion. If an error
     * occurred, the failure message may give more details about the error.
     *
     * @return true if an error occurred while checking the assertion, false
     *         otherwise.
     */
    public boolean isError() {
        return error;
    }

    /**
     * Get the message associated with any failure or error. This method may
     * return null if no message was set.
     *
     * @return a failure or error message, or null if no message has been set
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Set the flag indicating whether or not an error occurred.
     *
     * @param e
     *            true if an error occurred, false otherwise
     */
    public void setError(boolean e) {
        error = e;
    }

    /**
     * Set the flag indicating whether or not a failure occurred.
     *
     * @param f
     *            true if a failure occurred, false otherwise
     */
    public void setFailure(boolean f) {
        failure = f;
    }

    /**
     * Set the failure message giving more details about a failure or error.
     *
     * @param message
     *            the message to set
     */
    public void setFailureMessage(String message) {
        failureMessage = message;
    }

    /**
     * Convenience method for setting up failed results
     *
     * @param message
     *            the message to set
     * @return this
     *
     */
    public AssertionResult setResultForFailure(String message) {
        error = false;
        failure = true;
        failureMessage = message;
        return this;
    }

    /**
     * Convenience method for setting up results where the response was null
     *
     * @return assertion result with appropriate fields set up
     */
    public AssertionResult setResultForNull() {
        error = false;
        failure = true;
        failureMessage = RESPONSE_WAS_NULL;
        return this;
    }

    @Override
    public String toString() {
        return getName() != null ? getName() : super.toString();
    }
    
    @Override
    public List<String> getSearchableTokens() throws Exception {
        List<String> datasToSearch = new ArrayList<>(2);
        datasToSearch.add(getName());
        datasToSearch.add(getFailureMessage());
        return datasToSearch;
    }
}
