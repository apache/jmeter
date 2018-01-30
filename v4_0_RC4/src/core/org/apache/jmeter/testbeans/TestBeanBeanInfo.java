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
 */
package org.apache.jmeter.testbeans;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

/**
 * This is the BeanInfo object for the TestBean class. It acts as a "stopper"
 * for the introspector: we don't want it to look at properties defined at this
 * or higher classes.
 * <p>
 * Note this is really needed since using Introspector.getBeanInfo with a stop
 * class is not an option because:
 * <ol>
 * <li>The API does not define a 3-parameter getBeanInfo in which you can use a
 * stop class AND flags. [Why? I guess this is a bug in the spec.]
 * <li>java.beans.Introspector is buggy and, opposite to what's stated in the
 * Javadocs, only results of getBeanInfo(Class) are actually cached.
 * </ol>
 *
 */
public class TestBeanBeanInfo implements BeanInfo {

    @Override
    public BeanInfo[] getAdditionalBeanInfo() {
        return new BeanInfo[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultEventIndex() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDefaultPropertyIndex() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        return new EventSetDescriptor[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getIcon(int iconKind) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }
}
