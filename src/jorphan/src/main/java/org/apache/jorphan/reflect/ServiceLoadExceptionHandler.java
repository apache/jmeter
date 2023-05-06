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

package org.apache.jorphan.reflect;

import org.apiguardian.api.API;

/**
 * Service loading might fail (e.g. due to a missing dependency).
 * This handler enables client code factor the failure handing.
 *
 * @param <S> type of the service
 * @since 5.6
 * @see IgnoreServiceLoadExceptionHandler
 * @see LogAndIgnoreServiceLoadExceptionHandler
 * @see RethrowServiceLoadExceptionHandler
 */
@FunctionalInterface
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public interface ServiceLoadExceptionHandler<S> {
    void handle(Class<? extends S> service, String className, Throwable throwable);
}
