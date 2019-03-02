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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

import javax.swing.JPanel;

/**
 * Interface for request panel in View Results Tree
 * All classes which implements this interface is display
 * on bottom tab in request panel
 *
 */
public interface RequestView {

    /**
     * Init the panel
     */
    void init();

    /**
     * Clear all data in panel
     */
    void clearData();

    /**
     * Put the result bean to display in panel
     * @param userObject result to display
     */
    void setSamplerResult(Object userObject);

    /**
     * Get the panel
     * @return the panel viewer
     */
    JPanel getPanel();

    /**
     * Get the label. Use as name for bottom tab
     * @return the label's panel
     */
    String getLabel(); // return label

}
