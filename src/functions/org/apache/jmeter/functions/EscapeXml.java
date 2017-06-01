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

package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;

/**
 * <p>Function which escapes the characters in a <code>String</code> using XML 1.0 entities.</p>
 *
 * <p>
 * For example:
 * </p>
 * <p><code>"bread" &amp; 'butter'</code></p>
 * becomes:
 * <p>
 * <code>&amp;quot;bread&amp;quot; &amp;amp; &amp;apos;butter&amp;apos;</code>.
 * </p>
 *
 *
 * @see StringEscapeUtils#escapeXml10(String) (Commons Lang)
 * @since 3.2
 */
public class EscapeXml extends AbstractFunction {

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__escapeXml"; //$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("escape_xml_string")); //$NON-NLS-1$
    }

    private Object[] values;

    public EscapeXml() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        String rawString = ((CompoundVariable) values[0]).execute();
        return StringEscapeUtils.escapeXml10(rawString);

    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }
}
