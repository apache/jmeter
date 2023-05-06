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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * This is a marker annotation that describes JMeter will try using {@link java.util.ServiceLoader} to find the implementations.
 * If the plugin exposes the service via service loader (META-INF/services) then it will improve JMeter startup.
 * <p>Note: JMeter will still try scanning the classes in the jars for backward compatibility reasons,
 * so if you expose services, then consider adding {@code JMeter-Skip-Class-Scanning: true} manifest attribute
 * to your jar file. JMeter will skip scanning class files in such jars</p>
 * @since 5.6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public @interface JMeterService {
}
