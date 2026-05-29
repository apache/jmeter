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

import org.apache.jorphan.locale.LocalizedString
import org.apiguardian.api.API

/**
 * Controls whether a property editor exposes a "Reset to default" action
 * in its component popup menu.
 *
 * The action is the keyboard / context-menu counterpart of the
 * [ModifiedGutter] indicator: when the gutter is lit (the value differs
 * from the default), users can right-click the control and pick this
 * item to revert to the default value. The action is automatically
 * enabled only while the gutter is lit.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public sealed class ResetMode {
    /**
     * Do not add a "Reset to default" action to the popup menu.
     */
    public object Forbid : ResetMode()

    /**
     * Add a "Reset to default" action to the popup menu. The action
     * delegates to the editor's `resetToDefault()` method.
     *
     * @property label Menu item title (typically the localized "Reset" string)
     */
    public data class Allow(val label: LocalizedString) : ResetMode()
}
