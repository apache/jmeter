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

package org.apache.jmeter.threads.openmodel

import java.util.concurrent.TimeUnit

/**
 * Returns the number of seconds in the current [TimeUnit].
 * Unfortunately Java's `TimeUnit.MILLISECONDS.toSecond(1)` returns `0`.
 */
internal val TimeUnit.asSeconds: Double get() = when (this) {
    TimeUnit.MILLISECONDS -> 0.001
    else -> toSeconds(1).toDouble()
}

/**
 * Returns the suitable axis scale given a [rate] in Hz.
 * For instance, 10000/sec looks better as 10/ms,
 * and 0.0025/sec looks better as 9/hour.
 */
internal fun rateUnitFor(rate: Double) = when {
    rate > 1000 -> TimeUnit.MILLISECONDS
    rate > 1000.0 / TimeUnit.MINUTES.asSeconds -> TimeUnit.SECONDS
    rate > 1000.0 / TimeUnit.HOURS.asSeconds -> TimeUnit.MINUTES
    else -> TimeUnit.HOURS
}
