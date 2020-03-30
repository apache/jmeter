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

package org.apache.jmeter.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * Enables to get component information (e.g. title, group in menu) at the startup.
 * <p>Historically JMeter was creating component classes which initializes GUI elements.
 * The idea here is to enable access to the metadata without instantiating GUI classes.</p>
 * <p>This annotation is not meant to be used by the third-party plugins,
 * and it might be removed as better alternatives are implemented.</p>
 * @since 5.3
 * @see org.apache.jmeter.gui.util.MenuFactory
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(since = "5.3", status = API.Status.INTERNAL)
public @interface TestElementMetadata {
    String labelResource();
    String resourceBundle() default "";
    String[] actionGroups() default {};
}
