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

/*
 * Created on May 5, 2003
 */
package org.apache.jmeter.testelement.property;

public abstract class NumberProperty extends AbstractProperty {
    private static final long serialVersionUID = 240L;

    public NumberProperty() {
        super();
    }

    public NumberProperty(String name) {
        super(name);
    }

    /**
     * Set the value of the property with a Number object.
     *
     * @param n the value to set
     */
    protected abstract void setNumberValue(Number n);

    /**
     * Set the value of the property with a String object.
     *
     * @param n
     *            the number to set as a string representation
     * @throws NumberFormatException
     *             if the number <code>n</code> can not be converted to a
     *             {@link Number}
     */
    protected abstract void setNumberValue(String n) throws NumberFormatException;

    @Override
    public void setObjectValue(Object v) {
        if (v instanceof Number) {
            setNumberValue((Number) v);
        } else {
            try {
                setNumberValue(v.toString());
            } catch (RuntimeException ignored) {
            }
        }
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    @Override
    public int compareTo(JMeterProperty arg0) {
        return Double.compare(getDoubleValue(), arg0.getDoubleValue());
    }
}
