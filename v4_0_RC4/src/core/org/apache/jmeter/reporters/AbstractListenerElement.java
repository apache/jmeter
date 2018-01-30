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

package org.apache.jmeter.reporters;

import java.lang.ref.WeakReference;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.visualizers.Visualizer;

/**
 * Base class for Listeners
 */

public abstract class AbstractListenerElement extends AbstractTestElement {
    private static final long serialVersionUID = 240L;

    // TODO should class implement SampleListener?
    private transient WeakReference<Visualizer> listener;

    public AbstractListenerElement() {
    }

    protected final Visualizer getVisualizer() {
        if (listener == null){ // e.g. in non-GUI mode
            return null;
        }
        return listener.get();
    }

    public void setListener(Visualizer vis) {
        listener = new WeakReference<>(vis);
    }

    @Override
    public Object clone() {
        AbstractListenerElement clone = (AbstractListenerElement) super.clone();

        clone.listener=this.listener;
        return clone;
    }
}
