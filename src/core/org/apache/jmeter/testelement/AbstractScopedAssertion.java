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

/**
 * <p>
 * Super-class for all Assertions that can be applied to main sample, sub-samples or both.
 * Test elements merely need to extend this class to support scoping.
 * </p>
 *
 * <p>
 * Their corresponding GUI classes need to add the AssertionScopePanel to the GUI
 * using the AbstractAssertionGui methods:
 * <ul>
 * <li>createScopePanel()</li>
 * <li>saveScopeSettings()</li>
 * <li>showScopeSettings()</li>
 * </ul>
 */
public abstract class AbstractScopedAssertion extends AbstractScopedTestElement {

    private static final long serialVersionUID = 240L;

    //+ JMX attributes - do not change
    private static final String SCOPE = "Assertion.scope";
    //- JMX

    @Override
    protected String getScopeName() {
        return SCOPE;
    }

}
