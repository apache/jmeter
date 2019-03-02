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

/**
 * The interface ResultDataVisitor represents a visitor for result data from
 * samples processing.
 *
 * @param <T>
 *            the type returned by visit methods
 * @since 3.0
 */
public interface ResultDataVisitor<T> {

    /**
     * Visits the specified list result.
     *
     * @param listResult
     *            the list result
     * @return the result of the visit
     */
    T visitListResult(ListResultData listResult);

    /**
     * Visits the specified map result.
     *
     * @param mapResult
     *            the map result
     * @return the result of the visit
     */
    T visitMapResult(MapResultData mapResult);

    /**
     * Visits the specified value result.
     *
     * @param valueResult
     *            the value result
     * @return the result of the visit
     */
    T visitValueResult(ValueResultData valueResult);
}
