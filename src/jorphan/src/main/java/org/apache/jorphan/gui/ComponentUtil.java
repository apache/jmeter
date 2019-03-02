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

package org.apache.jorphan.gui;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

/**
 * This class is a Util for awt Component and could be used to place them in
 * center of an other.
 *
 */
public final class ComponentUtil {
    /**
     * Use this static method if you want to center and set its position
     * compared to the size of the current users screen size. Valid percent is
     * between +-(0-100) minus is treated as plus, bigger than 100 is always set
     * to 100.
     *
     * @param component
     *            the component you want to center and set size on
     * @param percentOfScreen
     *            the percent of the current screensize you want the component
     *            to be
     */
    public static void centerComponentInWindow(Component component, int percentOfScreen) {
        if (percentOfScreen < 0) {
            centerComponentInWindow(component, -percentOfScreen);
            return;
        }
        if (percentOfScreen > 100) {
            centerComponentInWindow(component, 100);
            return;
        }
        double percent = percentOfScreen / 100.d;
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        component.setSize((int) (bounds.getWidth() * percent), (int) (bounds.getHeight() * percent));
        centerComponentInWindow(component);
    }

    /**
     * Use this static method if you want to center a component in Window.
     *
     * @param component
     *            the component you want to center in window
     */
    public static void centerComponentInWindow(Component component) {
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        component.setLocation((int) ((bounds.getWidth() - component.getWidth()) / 2),
                (int) ((bounds.getHeight() - component.getHeight()) / 2));
        component.validate();
        component.repaint();
    }

    /**
     * Use this static method if you want to center a component over another
     * component.
     *
     * @param parent
     *            the component you want to use to place it on
     * @param toBeCentered
     *            the component you want to center
     */
    public static void centerComponentInComponent(Component parent, Component toBeCentered) {
        toBeCentered.setLocation(parent.getX() + (parent.getWidth() - toBeCentered.getWidth()) / 2, parent.getY()
                + (parent.getHeight() - toBeCentered.getHeight()) / 2);

        toBeCentered.validate();
        toBeCentered.repaint();
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private ComponentUtil() {
    }
}
