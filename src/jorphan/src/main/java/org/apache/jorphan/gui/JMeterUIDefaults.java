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

package org.apache.jorphan.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.text.StyleContext;

import org.apache.jorphan.gui.ui.TextAreaUIWithUndo;
import org.apache.jorphan.gui.ui.TextFieldUIWithUndo;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures JMeter-specific properties as {@link UIDefaults} properties for on-the-fly LaF updates.
 * <p>The workflow is as follows:</p>
 * <ul>
 *     <li>LaF is initialized, and it initializes its defaults (e.g. colors, keymaps, styles)</li>
 *     <li>UIManager fires lookAndFeel property change event</li>
 *     <li>JMeterUIDefaults handles the even and augments the properties (e.g. scales fonts, adds
 *     JMeter-specific colors)</li>
 * </ul>
 * <p>The sequence enables the components to use extra properties for styling the components</p>
 * <p>For instance, {@code label.setForeground(Color.black)} should not be used for styling the components
 * as the black colour might be hard to read in certain themes. That is why the styles should be
 * named (e.g. {@link #LABEL_ERROR_FOREGROUND}), so the actual value could be adapted to a given
 * theme without making changes to the component code</p>
 * @see JFactory
 */
public class JMeterUIDefaults {
    private static final Logger log = LoggerFactory.getLogger(JMeterUIDefaults.class);

    public static final String BUTTON_SMALL_FONT = "[jmeter]Button.smallFont"; // $NON-NLS-1$
    public static final String CHECKBOX_SMALL_FONT = "[jmeter]CheckBox.smallFont"; // $NON-NLS-1$
    public static final String LABEL_SMALL_FONT = "[jmeter]Label.smallFont"; // $NON-NLS-1$
    public static final String TEXTFIELD_SMALL_FONT = "[jmeter]TextField.smallFont"; // $NON-NLS-1$
    public static final String TOOLBAR_SMALL_FONT = "[jmeter]ToolBar.smallFont"; // $NON-NLS-1$
    public static final String LABEL_BIG_FONT = "[jmeter]Label.bigFont"; // $NON-NLS-1$

    public static final String TEXTAREA_BORDER = "[jmeter]TextArea.border"; // $NON-NLS-1$

    public static final String LABEL_WARNING_FONT = "[jmeter]Label.warningFont"; // $NON-NLS-1$
    public static final String LABEL_WARNING_FOREGROUND = "[jmeter]Label.warningForeground"; // $NON-NLS-1$

    public static final String LABEL_ERROR_FONT = "[jmeter]Label.errorFont"; // $NON-NLS-1$
    public static final String LABEL_ERROR_FOREGROUND = "[jmeter]Label.errorForeground"; // $NON-NLS-1$

    public static final String BUTTON_ERROR_FOREGROUND = "[jmeter]Button.errorForeground"; // $NON-NLS-1$

    public static final String TABLE_ROW_HEIGHT = "Table.rowHeight"; // $NON-NLS-1$
    public static final String TREE_ROW_HEIGHT = "Tree.rowHeight"; // $NON-NLS-1$

    private static final float SMALL_FONT_SCALE = 10f / 12;
    private static final float BIG_FONT_SCALE = 4f / 3;
    private static final float WARNING_FONT_SCALE = 11f / 10;
    private static final float ERROR_FONT_SCALE = 11f / 10;

    @API(since = "5.3", status = API.Status.INTERNAL)
    public static final JMeterUIDefaults INSTANCE = new JMeterUIDefaults();

    private float scale = 1.0f;

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public float getScale() {
        return scale;
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public void setScale(float scale) {
        this.scale = scale;
    }

    private JMeterUIDefaults() {
    }

    @API(since = "5.3", status = API.Status.INTERNAL)
    public void install() {
        DynamicStyle.onLaFChange(() -> {
            // We put JMeter-specific properties into getLookAndFeelDefaults,
            // so the properties are removed when LaF is changed
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();

            if (Math.abs(scale - 1.0f) > 0.01f) {
                scaleFonts(defaults);
                scaleIntProperties(defaults, scale);
                // We don't want to make controls extra big, so we damp the scaling factors
                scaleControlsProperties(defaults, (float) Math.sqrt(scale));
            }
            configureRowHeight(defaults, scale, TABLE_ROW_HEIGHT, "Table.font"); // $NON-NLS-1$
            configureRowHeight(defaults, scale, TREE_ROW_HEIGHT, "Tree.font"); // $NON-NLS-1$

            defaults.put("Button.defaultButtonFollowsFocus", false); // $NON-NLS-1$
            defaults.put(TEXTAREA_BORDER, (UIDefaults.LazyValue) d -> new JTextField().getBorder());

            addScaledFont(defaults, BUTTON_SMALL_FONT, "Button.font", SMALL_FONT_SCALE); // $NON-NLS-1$
            addScaledFont(defaults, CHECKBOX_SMALL_FONT, "CheckBox.font", SMALL_FONT_SCALE); // $NON-NLS-1$
            addScaledFont(defaults, LABEL_SMALL_FONT, "Label.font", SMALL_FONT_SCALE); // $NON-NLS-1$
            addScaledFont(defaults, TEXTFIELD_SMALL_FONT, "TextField.font", SMALL_FONT_SCALE); // $NON-NLS-1$
            addScaledFont(defaults, TOOLBAR_SMALL_FONT, "ToolBar.font", SMALL_FONT_SCALE); // $NON-NLS-1$

            addScaledFont(defaults, LABEL_BIG_FONT, "Label.font", BIG_FONT_SCALE); // $NON-NLS-1$

            addDerivedFont(defaults, LABEL_WARNING_FONT, "Label.font", // $NON-NLS-1$
                    f -> f.deriveFont(f.getStyle() | Font.BOLD, f.getSize2D() * WARNING_FONT_SCALE));
            defaults.put(LABEL_WARNING_FOREGROUND, defaults.get("Label.foreground")); // $NON-NLS-1$

            addDerivedFont(defaults, LABEL_ERROR_FONT, "Label.font", // $NON-NLS-1$
                    f -> f.deriveFont(f.getStyle() | Font.BOLD, f.getSize2D() * ERROR_FONT_SCALE));
            defaults.put(LABEL_ERROR_FOREGROUND, Color.red);

            defaults.put(BUTTON_ERROR_FOREGROUND, Color.red);

            TextFieldUIWithUndo.install(defaults);
            TextAreaUIWithUndo.install(defaults);
        });
    }

    public static Font createFont(String family, int style, int size) {
        return stripUiResource(StyleContext.getDefaultStyleContext().getFont(family, style, size));
    }

    private static void addScaledFont(UIDefaults defaults, String output, String input, float scale) {
        addDerivedFont(defaults, output, input, f -> f.deriveFont(f.getSize2D() * scale));
    }

    private static void addDerivedFont(UIDefaults defaults, String output, String input, Function<? super Font, ? extends Font> f) {
        defaults.put(output, (UIDefaults.LazyValue) d -> map(d.getFont(input), f));
    }

    private static Font map(Font input, Function<? super Font, ? extends Font> mapper) {
        Font output = mapper.apply(input);
        // Note: we drop UIResource here so LaF treats the font as user-provided rather than
        // LaF-provided.
        if (input instanceof UIResource) {
            output = stripUiResource(output);
        }
        return output;
    }

    private static void scaleIntProperties(UIDefaults defaults, float scale) {
        // Below are both standard and custom properties in a sorted order
        scaleIntProperty(defaults, "ArrowButton.size", scale); // $NON-NLS-1$
        scaleIntProperty(defaults, "ComboBox:\"ComboBox.arrowButton\".size", scale); // $NON-NLS-1$
        scaleIntProperty(defaults, "FileChooser.rowHeight", scale); // $NON-NLS-1$
        scaleIntProperty(defaults, "Spinner:\"Spinner.codeviousButton\".size", scale); // $NON-NLS-1$
        scaleIntProperty(defaults, "Spinner:\"Spinner.nextButton\".size", scale); // $NON-NLS-1$
    }

    private static void configureRowHeight(UIDefaults defaults, float scale, String rowHeight, String font) {
        if (defaults.getInt(rowHeight) == 0) {
            return;
        }
        defaults.put(rowHeight, (UIDefaults.LazyValue) d -> {
            Font f = d.getFont(font);
            float height;
            if (f == null) {
                height = 16 * scale;
            } else {
                Canvas c = new Canvas();
                height = c.getFontMetrics(f).getHeight();
            }
            // Set line height to be 1.3 of the font size. The number of completely made up,
            // 1.2 seems to be the minimal usable scale. 1.3 looks good.
            int round = (int) Math.floor(height * 1.3f);
            // Round to the next even, so the text does not move when editing the cell contents
            round += round & 1;
            return round;
        });
    }


    private static void scaleControlsProperties(UIDefaults defaults, float scale) {
        scaleIntProperty(defaults, "ScrollBar.thumbHeight", scale); // $NON-NLS-1$
        scaleIntProperty(defaults, "ScrollBar.width", scale); // $NON-NLS-1$
        scaleIntProperty(defaults, "ScrollBar:\"ScrollBar.button\".size", scale); // $NON-NLS-1$
        scaleIntProperty(defaults, "SplitPane.size", scale); // $NON-NLS-1$
    }

    private static void scaleIntProperty(UIDefaults defaults, String key, float scale) {
        int value = defaults.getInt(key);
        if (value != 0) {
            defaults.put(key, Math.round(value * scale));
        }
    }

    private void scaleFonts(UIDefaults defaults) {
        log.info("Applying font scale factor: {}", scale); // $NON-NLS-1$
        if ("Nimbus".equals(UIManager.getLookAndFeel().getID())) { // $NON-NLS-1$
            // Nimbus derives all the fonts from defaultFont, so it is enough to update it
            Font defaultFont = defaults.getFont("defaultFont"); // $NON-NLS-1$
            if (defaultFont != null) {
                Font newFont = defaultFont.deriveFont(defaultFont.getSize2D() * scale);
                defaults.put("defaultFont", sameUiResource(defaultFont, newFont)); // $NON-NLS-1$
                return;
            }
        }
        // For other LaFs just update all the font resources.
        // Note: there might be derived fonts (e.g. javax.swing.UIDefaults.ActiveValue)
        // that compute their value based on another UIDefaults
        // So we update fonts in two loops: first we remember the actual fonts, then we update them
        // javax.swing.MultiUIDefaults.keys and javax.swing.MultiUIDefaults.entrySet
        // are not always consistent, so we iterate over keys
        Map<Object, Font> fonts = new HashMap<>();
        for (Object key : Collections.list(defaults.keys())) {
            Font font = defaults.getFont(key);
            if (font != null) {
                fonts.put(key, font);
            }
        }

        for (Map.Entry<Object, Font> entry : fonts.entrySet()) {
            Font oldFont = entry.getValue();
            Font newFont = sameUiResource(oldFont, oldFont.deriveFont(oldFont.getSize2D() * scale));
            defaults.put(entry.getKey(), newFont);
        }
    }

    /**
     * Ensures the font isn't of type {@link UIResource}.
     *
     * @param font the font.
     * @return font which does not implement {@link UIResource}.
     */
    private static Font stripUiResource(Font font) {
        if (font instanceof UIResource) {
            return new NonUIResourceFont(font);
        }
        return font;
    }

    /**
     * Ensures {@code oldFont} and {@code newFont} either both implement {@link UIResource},
     * or none of them implement.
     *
     * @param oldFont old font
     * @param newFont new font
     * @return Font (when oldFont does not implement UIResource) or FontUIResource (when oldFont implements UIResource)
     */
    private static Font sameUiResource(Font oldFont, Font newFont) {
        boolean o = oldFont instanceof FontUIResource;
        boolean O = newFont instanceof FontUIResource;
        // This is a beautiful smile, isn't it?
        if (o ^ O) {
            return o ? new FontUIResource(newFont) : stripUiResource(newFont);
        }
        return newFont;
    }

    /**
     * Non UIResource wrapper for fonts which preserves the underlying {@code sun.font.Font2D}.
     * This way the font behaves the same way with respect to fallback fonts
     * (i.e. if the {@code sun.font.Font2D} base is of type {@code sun.font.CompositeFont}).
     */
    private static class NonUIResourceFont extends Font {
        private NonUIResourceFont(Font font) {
            super(font);
        }
    }
}
