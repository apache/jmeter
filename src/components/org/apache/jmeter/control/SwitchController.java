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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;

// For unit tests @see TestSwitchController

/**
 * <p>
 * Implements a controller which selects at most one of its children
 * based on the condition value, which may be a number or a string.
 * </p>
 * <p>
 * For numeric input, the controller processes the appropriate child,
 * where the numbering starts from 0.
 * If the number is out of range, then the first (0th) child is selected.
 * If the condition is the empty string, then it is assumed to be 0.
 * </p>
 * <p>
 * For non-empty non-numeric input, the child is selected by name.
 * This may be the name of the controller or a sampler.
 * If the string does not match any of the names, then the controller
 * with the name "default" (any case) is processed.
 * If there is no default entry, then unlike the numeric case,
 * no child is selected.
 * </p>
 */
public class SwitchController extends GenericController implements Serializable {
    private static final long serialVersionUID = 240L;

    // Package access for use by Test code
    static final String SWITCH_VALUE = "SwitchController.value"; //$NON-NLS-1$

    public SwitchController() {
        super();
    }

    @Override
    public Sampler next() {
        if (isFirst()) { // Set the selection once per iteration
            current = getSelectionAsInt();
        }
        return super.next();
    }

    /**
     * incrementCurrent is called when the current child (whether sampler or controller)
     * has been processed.
     * <p>
     * Setting it to int.max marks the controller as having processed all its
     * children. Thus the controller processes one child per iteration.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected void incrementCurrent() {
        current=Integer.MAX_VALUE;
    }

    public void setSelection(String inputValue) {
        setProperty(new StringProperty(SWITCH_VALUE, inputValue));
    }

    /**
     * @return the selection value as a int with the value set to zero if it is out of range.
     */
    private int getSelectionAsInt() {
        getProperty(SWITCH_VALUE).recoverRunningVersion(null);
        String sel = getSelection();
        if (StringUtils.isEmpty(sel)) {
            return 0;
        } else {
            try {
                if(StringUtils.isNumeric(sel)) {
                    int ret = Integer.parseInt(sel);
                    if (ret < 0 || ret >= getSubControllers().size()) {
                        // Out of range, we return first one
                        ret = 0;
                    }
                    return ret;
                }
            } catch (NumberFormatException e) {
                // it will be handled by code below
            }
            return scanControllerNames(sel);
        }
    }

    /**
     * @param sel controller name
     * @return index of controller named sel if present, otherwise index of default if found, otherwise {@link Integer#MAX_VALUE} 
     */
    private int scanControllerNames(String sel) {
        int i = 0;
        int defaultPos = Integer.MAX_VALUE;
        for (TestElement el : getSubControllers()) {
            String name = el.getName();
            if (name.equals(sel)) {
                return i;
            }
            if (name.equalsIgnoreCase("default")) { //$NON-NLS-1$
                defaultPos = i;
            }
            i++;
        }

        return defaultPos;
    }

    public String getSelection() {
        return getPropertyAsString(SWITCH_VALUE).trim();
    }
}
