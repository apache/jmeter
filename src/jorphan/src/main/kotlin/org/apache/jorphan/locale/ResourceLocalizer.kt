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

package org.apache.jorphan.locale

import org.apiguardian.api.API
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

/**
 * Functional interface for localizing resource strings.
 *
 * This interface provides a single method, [localize], which takes a non-localized input string
 * and returns its localized representation. Implementations of this interface define how the
 * localization is performed, such as looking up translations in a resource bundle or applying
 * other localization mechanisms.
 *
 * The input string is expected to be a resource identifier or key, and the output is a string
 * appropriate for display in a user interface or other localized context.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public fun interface ResourceLocalizer {
    public fun localize(input: @NonNls String): @Nls String
}
