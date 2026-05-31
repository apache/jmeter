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

package org.apache.jmeter.samplers;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.engine.util.LightweightClone;
import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * Base class for samplers that can share properties across threads for memory efficiency.
 * <p>
 * Samplers implementing {@link LightweightClone} will only use lightweight cloning if
 * they have no properties containing JMeter variables (${...}) or functions (__()).
 * Elements with variables are fully cloned to ensure proper per-thread variable evaluation.
 * </p>
 */
public abstract class AbstractSampler extends AbstractTestElement implements Sampler, ConfigMergabilityIndicator, LightweightClone {
    private static final long serialVersionUID = 240L;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        return true;
    }
}
