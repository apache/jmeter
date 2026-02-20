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
import org.jetbrains.annotations.NonNls

/**
 * Interface for enum types that provide their own localization resource key for GUI display.
 *
 * When an enum implements this interface, the [resourceKey] will be used for:
 * - Storing the value in JMeter properties (instead of the enum name)
 * - Looking up localized display text via `JMeterUtils.getResString(resourceKey)`
 * - Binding to GUI components
 *
 * This allows enums to have stable, localization-friendly identifiers that are independent
 * of the enum constant names.
 *
 * Example:
 * ```java
 * public enum ResponseMode implements ResourceKeyed {
 *     STORE_COMPRESSED("response_mode_store"),
 *     FETCH_DISCARD("response_mode_discard");
 *
 *     private final String key;
 *     ResponseMode(String key) { this.key = key; }
 *
 *     @Override
 *     public String getResourceKey() { return key; }
 * }
 * ```
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public interface ResourceKeyed {
    /**
     * Returns the resource key used for localization and property storage.
     *
     * This key should:
     * - Be stable across JMeter versions (don't change it)
     * - Have a corresponding entry in messages.properties
     * - Use lowercase with underscores by convention
     *
     * @return the resource key for this enum value
     */
    public val resourceKey: @NonNls String
}
