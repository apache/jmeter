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
 * Controls whether a property editor allows entering custom expressions.
 *
 * JMeter supports runtime expressions using syntax like `${variableName}` or
 * `${__functionName(args)}`. When expression mode is allowed, the property editor
 * provides a mechanism to switch from selecting predefined values to entering
 * arbitrary text that may contain these expressions.
 *
 * This mode is applicable to property editors where it makes sense to provide
 * both structured value selection and free-form text entry.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public sealed class ExpressionMode {
    /**
     * Only allow selecting from predefined values. The property editor does not
     * provide any way to enter custom text or expressions, forcing the user to
     * choose from the available options only.
     */
    public object Forbid : ExpressionMode()

    /**
     * Allow entering custom expressions. The property editor offers a
     * "Use Expression" popup-menu item that switches to a text field where the
     * user can type expressions like `${__P(property)}` or `${variableName}`.
     *
     * @property useExpression Text for the menu item to switch to expression mode
     * @property useExpressionTooltip Tooltip shown on the "Use Expression" menu item
     */
    public data class Allow(
        val useExpression: LocalizedString,
        val useExpressionTooltip: LocalizedString,
    ) : ExpressionMode()
}
