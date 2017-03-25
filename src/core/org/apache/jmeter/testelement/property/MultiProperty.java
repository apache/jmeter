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

package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * For JMeterProperties that hold multiple properties within, provides a simple
 * interface for retrieving a property iterator for the sub values.
 *
 */
public abstract class MultiProperty extends AbstractProperty implements Iterable<JMeterProperty> {
    private static final long serialVersionUID = 240L;

    public MultiProperty() {
        super();
    }

    public MultiProperty(String name) {
        super(name);
    }

    /**
     * Get the property iterator to iterate through the sub-values of this
     * JMeterProperty.
     *
     * @return an iterator for the sub-values of this property
     */
    @Override
    public abstract PropertyIterator iterator();

    /**
     * Add a property to the collection.
     *
     * @param prop the {@link JMeterProperty} to add
     */
    public abstract void addProperty(JMeterProperty prop);

    /**
     * Clear away all values in the property.
     */
    public abstract void clear();

    @Override
    public void setRunningVersion(boolean running) {
        super.setRunningVersion(running);
        for (JMeterProperty jMeterProperty : this) {
            jMeterProperty.setRunningVersion(running);
        }
    }

    protected void recoverRunningVersionOfSubElements(TestElement owner) {
        PropertyIterator iter = iterator();
        while (iter.hasNext()) {
            JMeterProperty prop = iter.next();
            if (owner.isTemporary(prop)) {
                iter.remove();
            } else {
                prop.recoverRunningVersion(owner);
            }
        }
    }

    @Override
    public void mergeIn(JMeterProperty prop) {
        if (prop.getObjectValue() == getObjectValue()) {
            return;
        }
        log.debug("merging in " + prop.getClass());
        if (prop instanceof MultiProperty) {
            for (JMeterProperty item : (MultiProperty) prop) {
                addProperty(item);
            }
        } else {
            addProperty(prop);
        }
    }
}
