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

/**
 *
 */
package org.apache.jmeter.visualizers;

import java.awt.Color;

import javax.swing.JTabbedPane;

import org.apache.jmeter.samplers.SampleResult;


/**
 * Interface to results render
 */
public interface ResultRenderer {

    void clearData();

    void init();

    void setupTabPane();

    void setLastSelectedTab(int index);

    void setRightSide(JTabbedPane rightSide);

    void setSamplerResult(Object userObject);

    void renderResult(SampleResult sampleResult);

    void renderImage(SampleResult sampleResult);

    /**
     *
     * @return the string to be displayed by the ComboBox
     */
    @Override
    String toString();

    void setBackgroundColor(Color backGround);

}
