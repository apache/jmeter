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
 * Super-class for all TestElements that can be applied to main sample, sub-samples or both.
 * Test elements can use the 
 * </p>
 * 
 * <p>
 * Their corresponding GUI classes need to add the ScopePanel to the GUI
 * using the AbstractXXXGui methods:
 * <ul>
 * <li>createScopePanel()</li>
 * <li>saveScopeSettings()</li>
 * <li>showScopeSettings()</li>
 * </ul>
 * </p>
 */
public abstract class AbstractScopedTestElement extends AbstractTestElement {

    private static final String SCOPE = "Sample.scope";
    private static final String SCOPE_PARENT = "parent";
    private static final String SCOPE_CHILDREN = "children";
    private static final String SCOPE_ALL = "all";

    /**
     * Get the scope setting
     * @return the scope, default parent
     */
    public String fetchScope() {
        return getPropertyAsString(SCOPE, SCOPE_PARENT);
    }

    /**
     * Is the assertion to be applied to the main (parent) sample?
     * 
     * @param scope
     * @return if the assertion is to be applied to the parent sample.
     */
    public boolean isScopeParent(String scope) {
        return scope.equals(SCOPE_PARENT);
    }

    /**
     * Is the assertion to be applied to the sub-samples (children)?
     * 
     * @param scope
     * @return if the assertion is to be applied to the children.
     */
    public boolean isScopeChildren(String scope) {
        return scope.equals(SCOPE_CHILDREN);
    }

    /**
     * Is the assertion to be applied to the all samples?
     * 
     * @param scope
     * @return if the assertion is to be applied to the all samples.
     */
    protected boolean isScopeAll(String scope) {
        return scope.equals(SCOPE_ALL);
    }

    public void setScopeParent() {
        removeProperty(SCOPE);
    }

    public void setScopeChildren() {
        setProperty(SCOPE, SCOPE_CHILDREN);
    }

    public void setScopeAll() {
        setProperty(SCOPE, SCOPE_ALL);
    }
}
