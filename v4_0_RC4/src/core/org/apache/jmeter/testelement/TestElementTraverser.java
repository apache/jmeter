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

package org.apache.jmeter.testelement;

import org.apache.jmeter.testelement.property.JMeterProperty;

/**
 * For traversing Test Elements, which contain property that can be other test
 * elements, strings, collections, maps, objects
 *
 */
public interface TestElementTraverser {

    /**
     * Notification that a new test element is about to be traversed.
     *
     * @param el element to be traversed
     */
    void startTestElement(TestElement el);

    /**
     * Notification that the test element is now done.
     *
     * @param el element that was traversed
     */
    void endTestElement(TestElement el);

    /**
     * Notification that a property is starting. This could be a test element
     * property or a Map property - depends on the context.
     *
     * @param key property to be traversed
     */
    void startProperty(JMeterProperty key);

    /**
     * Notification that a property is ending. Again, this could be a test
     * element or a Map property, depending on the context.
     *
     * @param key property that was traversed
     */
    void endProperty(JMeterProperty key);

}
