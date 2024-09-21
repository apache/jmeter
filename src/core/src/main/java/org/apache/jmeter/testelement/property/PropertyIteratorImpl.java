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

package org.apache.jmeter.testelement.property;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.jmeter.testelement.TestElement;

public class PropertyIteratorImpl implements PropertyIterator {
    public static final PropertyIterator EMPTY_ITERATOR = new PropertyIteratorImpl(Collections.emptyList());

    private final TestElement owner;
    private final Iterator<? extends JMeterProperty> iter;
    private String lastPropertyName;

    public PropertyIteratorImpl(Collection<JMeterProperty> value) {
        this(null, value);
    }

    public PropertyIteratorImpl(TestElement owner, Iterable<? extends JMeterProperty> properties) {
        this.owner = owner;
        this.iter = properties.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    /** {@inheritDoc} */
    @Override
    public JMeterProperty next() {
        JMeterProperty last = iter.next();
        if (owner != null) {
            lastPropertyName = last.getName();
        }
        return last;
    }

    /** {@inheritDoc} */
    @Override
    public void remove() {
        iter.remove();
        if (lastPropertyName != null) {
            owner.removeProperty(lastPropertyName);
            lastPropertyName = null;
        }
    }

}
