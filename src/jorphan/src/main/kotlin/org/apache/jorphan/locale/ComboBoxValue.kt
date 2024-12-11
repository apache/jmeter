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

/**
 * Marker interface for values that can be used in a combo box.
 *
 * This interface is intended to standardize handling of values for GUI components
 * like combo boxes. Classes implementing this interface can define how their
 * values are represented and localized, providing flexibility for different types
 * of combo box content.
 *
 * Implementers may include:
 * - Simple values (e.g., strings or plain objects).
 * - Values with additional localization support for display in different locales.
 * - Wrapper types that encapsulate richer behaviors for combo box items.
 *
 * This interface is marked as experimental and may change in future releases.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public interface ComboBoxValue
