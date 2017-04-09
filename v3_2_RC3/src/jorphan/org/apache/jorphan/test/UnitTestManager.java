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

package org.apache.jorphan.test;

/**
 * Implement this interface to work with the AllTests class. This interface
 * allows AllTests to pass a configuration file to your application before
 * running the junit unit tests.
 * <p>
 * N.B. This interface must be in the main src/ tree (not test/) because it is
 * implemented by JMeterUtils
 * </p>
 * see JUnit class: org.apache.jorphan.test.AllTests
 */
public interface UnitTestManager {
    /**
     * Your implementation will be handed the filename that was provided to
     * AllTests as a configuration file. It can hold whatever properties you
     * need to configure your system prior to the unit tests running.
     *
     * @param filename
     *            path to the configuration file
     */
    void initializeProperties(String filename);
}
