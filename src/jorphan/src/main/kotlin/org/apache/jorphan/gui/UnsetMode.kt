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

package org.apache.jorphan.gui

import org.apache.jorphan.locale.ComboBoxValue
import org.apiguardian.api.API

/**
 * Controls whether a property editor allows clearing/unsetting the selected value.
 *
 * This mode is applicable to property editors that display a selection of values
 * (such as combo boxes or enum selectors) where it may be useful to distinguish
 * between "no value selected" and "a specific value is selected".
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public sealed class UnsetMode {
    /**
     * Do not allow clearing the value. The user must always have a value selected,
     * and there is no way to return to an unset/null state.
     */
    public object Forbid : UnsetMode()

    /**
     * Allow clearing the value. For combo boxes, this adds an empty option
     * to the dropdown that represents "no selection".
     *
     * @property unsetValue The value to display when nothing is selected (typically localized "Undefined")
     */
    public data class Allow(val unsetValue: ComboBoxValue) : UnsetMode()
}
