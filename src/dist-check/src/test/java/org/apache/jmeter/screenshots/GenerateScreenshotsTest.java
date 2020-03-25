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

package org.apache.jmeter.screenshots;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.junit.JMeterTestCaseJUnit;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JMeterUIDefaults;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.xmlgraphics.image.writer.ImageWriterUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.IntelliJTheme;
import com.github.weisj.darklaf.ui.tabbedpane.DarkTabbedPaneUI;

public class GenerateScreenshotsTest {
    public static Stream<Arguments> classes() throws IOException, InvocationTargetException, InterruptedException {
        File screenshots = new File("build/screenshots");
        screenshots.mkdirs();
        System.setProperty("darklaf.decorations", "false");
        JMeterUIDefaults.INSTANCE.install();
        SwingUtilities.invokeAndWait(() -> LafManager.installTheme(new IntelliJTheme()));
        new JMeterTestCaseJUnit() {
            // ^^ initialize JMeter properties, paths, etc
        };
        return ClassFinder.findClassesThatExtend(
                JMeterUtils.getSearchPaths(),
                new Class[]{JMeterGUIComponent.class, TestBean.class}
        ).stream()
                .filter(className -> !className.equals("org.apache.jmeter.testbeans.gui.TestBeanGUI"))
                .filter(className -> !className.equals("org.apache.jmeter.config.gui.ObsoleteGui"))
                .filter(className -> !className.equals("org.apache.jmeter.gui.menu.StaticJMeterGUIComponent"))
                // .filter(className -> className.equals("org.apache.jmeter.visualizers.ViewResultsFullVisualizer"))
                // E.g. execute a subset only
                // .filter(className -> className.equals("org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui"))
                .map(className -> Arguments.of(className, screenshots));
    }

    private static JFrame frame;

    @BeforeAll
    public static void initFrame() {
        try {
            JFrame f = new JFrame();
            f.setSize(new Dimension(10, 10));
            f.pack();
            f.setVisible(true);
            frame = f;
        } catch (Throwable t) {
            System.out.println("Unable to create frame: " + t.getMessage());
        }
    }

    @AfterAll
    public static void dispose() {
        JFrame frame = GenerateScreenshotsTest.frame;
        if (frame != null) {
            frame.dispose();
        }
    }

    @ParameterizedTest
    @MethodSource("classes")
    public void takeScreenshot(String className, File screenshots) throws Exception {
        File out = new File(screenshots, className + ".png");
        if (out.exists()) {
            out.delete();
        }

        FutureTask<BufferedImage> task = new FutureTask<BufferedImage>(() -> {
            JMeterGUIComponent gui = createjMeterGUIComponent(className);
            if (gui == null) {
                return null;
            }

            JComponent guiComp = (JComponent) gui;
            Dimension size = getPreferredSize(guiComp);
            return paintComponent(className, guiComp, size);
        });

        SwingUtilities.invokeAndWait(task);

        BufferedImage image = task.get();
        if (image != null) {
            ImageWriterUtil.saveAsPNG(image, 144, out);
        }
    }

    private BufferedImage paintComponent(String className, JComponent guiComp, Dimension size) {
        int scale = 2; // Generate images that would look nice in HiDPI screens

        BufferedImage image = new BufferedImage(size.width * scale, size.height * scale, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();

        try {
            AffineTransform transform = new AffineTransform();
            transform.setToScale(scale, scale);

            g2.setTransform(transform);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            JFrame f = frame;
            // Try painting the element in a frame
            if (f != null) {
                try {
                    f.setSize(size);
                    f.add(guiComp);
                    f.pack();
                    guiComp.paint(g2);
                    return image;
                } catch (HeadlessException e) {
                    // Ok, try with headless to get at least some paint done
                }
            }

            // It might be sub-optimal as some of the elements might be invisible (e.g. splitpane)
            JPanel panel = new JPanel();
            panel.setSize(size);
            panel.add(guiComp);
            try {
                guiComp.paint(g2);
            } catch (HeadlessException | NullPointerException ex) {
                // e.g. https://github.com/bobbylight/RSyntaxTextArea/issues/335
                // Some of the components do not support painting
                System.out.println("Unable to render " + className + " " + ex);
                return null;
            }
        } finally {
            g2.dispose();
        }
        return image;
    }

    private Dimension getPreferredSize(JComponent guiComp) {
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(1600, 900));
        panel.add(guiComp);
        layoutComponent(panel);
        return guiComp.getSize();
    }

    private JMeterGUIComponent createjMeterGUIComponent(String className) throws ClassNotFoundException {
        Class<?> klass = Class.forName(className);

        JMeterGUIComponent gui;

        try {
            if (TestBean.class.isAssignableFrom(klass)) {
                gui = new TestBeanGUI(klass);
            } else {
                try {
                    gui = (JMeterGUIComponent) klass.getDeclaredConstructor().newInstance();
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof NullPointerException &&
                            DarkTabbedPaneUI.class.getName()
                                    .equals(cause.getStackTrace()[0].getClassName())) {
                        // ignore, see https://github.com/weisJ/darklaf/issues/119
                        return null;
                    } else {
                        throw cause;
                    }
                }
            }
            // Init UI
            TestElement testElement = gui.createTestElement();
            gui.configure(testElement);
        } catch (HeadlessException e) {
            System.out.println("Unable to instantiate " + className + " " + e);
            return null;
        } catch (Error | RuntimeException e) {
            e.addSuppressed(new IllegalArgumentException("Unable to instantiate UI: " + className, e));
            throw e;
        } catch (Throwable e) {
            throw new IllegalArgumentException("Unable to instantiate UI: " + className, e);
        }
        return gui;
    }

    private static void layoutComponent(Component component) {
        synchronized (component.getTreeLock()) {
            component.invalidate();
            component.doLayout();
            if (component instanceof Container) {
                for (Component child : ((Container) component).getComponents()) {
                    layoutComponent(child);
                }
            }
        }
    }
}
