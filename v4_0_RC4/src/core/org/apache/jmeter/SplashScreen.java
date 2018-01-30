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

package org.apache.jmeter;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Splash Screen
 * @since 3.2
 */
public class SplashScreen extends JWindow {

    private static final long serialVersionUID = 1L;
    private BorderLayout borderLayout = new BorderLayout();
    private JLabel imageLabel = new JLabel();
    private JProgressBar progressBar = new JProgressBar(0, 100);

    /**
     * Constructor
     */
    public SplashScreen() {
        imageLabel.setIcon(JMeterUtils.getImage("jmeter.png"));
        imageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(borderLayout);
        add(imageLabel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Show screen
     */
    public void showScreen() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            setAlwaysOnTop(true);
        });
    }

    /**
     * Close splash
     */
    public void close() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        });
    }

    /**
     * @param progress Loading progress
     */
    public void setProgress(final int progress) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }
}
