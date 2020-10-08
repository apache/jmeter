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

package org.apache.jmeter.protocol.http.config.gui;

import javax.swing.JTabbedPane;

/**
 * Abstract {@link JTabbedPane} to allow validating the requested tab index, updating states and changing the tab index
 * after the validation if necessary.
 */
abstract class AbstractValidationTabbedPane extends JTabbedPane {

    private static final long serialVersionUID = 7014311238367882880L;

    /**
     * Flag whether the validation feature should be enabled or not, {@code true} by default.
     */
    private boolean validationEnabled = true;

    /**
     * {@inheritDoc}
     * <P>
     * Overridden to delegate to {@link #setSelectedIndex(int, boolean)} in order to validate the requested tab index by default.
     */
    @Override
    public void setSelectedIndex(int index) {
        setSelectedIndex(index, true);
    }

    /**
     * Apply some check rules by invoking {@link #getValidatedTabIndex(int, int)}
     * if {@link #isValidationEnabled()} returns true and the {@code check} input is true.
     *
     * @param index index to select
     * @param check flag whether to perform checks before setting the selected index
     */
    public void setSelectedIndex(int index, boolean check) {
        final int curIndex = super.getSelectedIndex();

        if (!isValidationEnabled() || !check || curIndex == -1) {
            super.setSelectedIndex(index);
            return;
        }

        super.setSelectedIndex(getValidatedTabIndex(curIndex, index));
    }

    /**
     * Validate the requested tab index ({@code newTabIndex}) and return a validated tab index after applying some check rules.
     * @param currentTabIndex current tab index
     * @param newTabIndex new requested tab index to validate
     * @return the validated tab index
     */
    protected abstract int getValidatedTabIndex(final int currentTabIndex, final int newTabIndex);

    /**
     * Return true if the validation feature should be enabled, {@code true} by default.
     * @return true if the validation feature should be enabled, {@code true} by default
     */
    protected boolean isValidationEnabled() {
        return validationEnabled;
    }

    /**
     * Set the flag whether the validation feature should be enabled or not.
     * @param validationEnabled flag whether the validation feature should be enabled or not
     */
    protected void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }
}
