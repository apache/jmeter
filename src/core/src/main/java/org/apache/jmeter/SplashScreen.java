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

package org.apache.jmeter;

import java.awt.BorderLayout;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.weisj.darklaf.icons.ThemedSVGIcon;

/**
 * Splash Screen
 * @since 3.2
 */
public class SplashScreen extends JDialog {
    private static final Logger log = LoggerFactory.getLogger(SplashScreen.class);

    private static final long serialVersionUID = 1L;
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    /**
     * Constructor
     */
    public SplashScreen() {
        setLayout(new BorderLayout());
        add(loadLogo(), BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setAutoRequestFocus(true);
        setUndecorated(true);
        pack();
        setLocationRelativeTo(null);
    }

    public static JComponent loadLogo() {
        JLabel logo = new JLabel();
        logo.setBorder(new EmptyBorder(10, 10, 10, 10));
        URI svgUri = null;
        String svgResourcePath = "/org/apache/jmeter/images/logo.svg";
        try {
            URL svgUrl = JMeterUtils.class.getResource(svgResourcePath);
            if (svgUrl != null) {
                svgUri = svgUrl.toURI();
            }
        } catch (URISyntaxException e) {
            log.warn("Unable to find logo {}", svgResourcePath, e);
        }

        if (svgUri != null) {
            Icon icon = new ThemedSVGIcon(svgUri, 521, 177);
            logo.setIcon(icon);
        } else {
            // Fallback logo
            logo.setText("<html>" +
                    "<span style=\"font-size:36px;font-family:'Lucida Grande',Corbel,Arial;color:#D11123\">&nbsp;&nbsp;APACHE<br></span>" +
                    "<span style=\"font-size:100px;font-family:'Lucida Grande',Corbel,Arial;font-weight:bold\">" +
                    "<span style=\"color:#D11123\">J</span>Meter" +
                    "<span style=\"color:#4d4d4d\">™</span></span></html>");
        }
        return logo;
    }

    /**
     * Show screen
     */
    public void showScreen() {
        SwingUtilities.invokeLater(() -> setVisible(true));
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
        if (SwingUtilities.isEventDispatchThread()) {
            progressBar.setValue(progress);
        } else {
            SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
        }
    }
}
