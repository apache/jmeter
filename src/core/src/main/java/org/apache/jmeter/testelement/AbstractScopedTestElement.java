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

package org.apache.jmeter.testelement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.schema.PropertiesAccessor;

/**
 * <p>
 * Super-class for TestElements that can be applied to main sample, sub-samples or both.
 * [Assertions use a different class because they use a different value for the {@link #getScopeName()} constant]
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
 */
public abstract class AbstractScopedTestElement extends AbstractTestElement {

    private static final long serialVersionUID = 240L;

    //+ JMX attributes - do not change
    private static final String SCOPE = "Sample.scope"; // $NON-NLS-1$
    private static final String SCOPE_PARENT = "parent"; // $NON-NLS-1$
    private static final String SCOPE_CHILDREN = "children"; // $NON-NLS-1$
    private static final String SCOPE_ALL = "all"; // $NON-NLS-1$
    private static final String SCOPE_VARIABLE = "variable"; // $NON-NLS-1$
    private static final String SCOPE_VARIABLE_NAME = "Scope.variable"; // $NON-NLS-1$
    //- JMX

    @Override
    public AbstractScopedTestElementSchema getSchema() {
        return AbstractScopedTestElementSchema.INSTANCE;
    }

    @Override
    public PropertiesAccessor<? extends AbstractScopedTestElement, ? extends AbstractScopedTestElementSchema> getProps() {
        return new PropertiesAccessor<>(this, getSchema());
    }

    protected String getScopeName() {
        return SCOPE;
    }

    /**
     * Get the scope setting
     * @return the scope, default parent
     */
    public String fetchScope() {
        return getPropertyAsString(getScopeName(), SCOPE_PARENT);
    }

    /**
     * Is the assertion to be applied to the main (parent) sample?
     *
     * @param scope
     *            name of the scope to be checked
     * @return <code>true</code> if the assertion is to be applied to the parent
     *         sample.
     */
    public boolean isScopeParent(String scope) {
        return scope.equals(SCOPE_PARENT);
    }

    /**
     * Is the assertion to be applied to the sub-samples (children)?
     *
     * @param scope
     *            name of the scope to be checked
     * @return <code>true</code> if the assertion is to be applied to the
     *         children.
     */
    public boolean isScopeChildren(String scope) {
        return scope.equals(SCOPE_CHILDREN);
    }

    /**
     * Is the assertion to be applied to the all samples?
     *
     * @param scope
     *            name of the scope to be checked
     * @return <code>true</code> if the assertion is to be applied to the all
     *         samples.
     */
    public boolean isScopeAll(String scope) {
        return scope.equals(SCOPE_ALL);
    }

    /**
     * Is the assertion to be applied to the all samples?
     *
     * @param scope
     *            name of the scope to be checked
     * @return <code>true</code> if the assertion is to be applied to the all
     *         samples.
     */
    public boolean isScopeVariable(String scope) {
        return scope.equals(SCOPE_VARIABLE);
    }

    /**
     * Is the assertion to be applied to the all samples?
     *
     * @return <code>true</code> if the assertion is to be applied to the all samples.
     */
    protected boolean isScopeVariable() {
        return isScopeVariable(fetchScope());
    }

    public String getVariableName(){
        return getPropertyAsString(SCOPE_VARIABLE_NAME, "");
    }

    public void setScopeParent() {
        removeProperty(getScopeName());
    }

    public void setScopeChildren() {
        setProperty(getScopeName(), SCOPE_CHILDREN);
    }

    public void setScopeAll() {
        setProperty(getScopeName(), SCOPE_ALL);
    }

    public void setScopeVariable(String variableName) {
        setProperty(getScopeName(), SCOPE_VARIABLE);
        setProperty(SCOPE_VARIABLE_NAME, variableName);
    }

    /**
     * Generate a list of qualifying sample results,
     * depending on the scope.
     *
     * @param result current sample
     * @return list containing the current sample and/or its child samples
     */
    protected List<SampleResult> getSampleList(SampleResult result) {
        List<SampleResult> sampleList = new ArrayList<>();

        String scope = fetchScope();
        if (isScopeParent(scope) || isScopeAll(scope)) {
            sampleList.add(result);
        }
        if (isScopeChildren(scope) || isScopeAll(scope)) {
            recurseResults(sampleList, result);
        }
        return sampleList;
    }

    private static void recurseResults(List<? super SampleResult> resultList, SampleResult sampleResult) {
        Collections.addAll(resultList, sampleResult.getSubResults());
        recurseResults(resultList, sampleResult.getSubResults(), 3);
    }

    private static void recurseResults(List<? super SampleResult> resultList, SampleResult[] sampleResult, int level) {
        if (level < 0) {
            return;
        }
        for (SampleResult child: sampleResult) {
            Collections.addAll(resultList, child.getSubResults());
            recurseResults(resultList, child.getSubResults(), level - 1);
        }
    }

}
