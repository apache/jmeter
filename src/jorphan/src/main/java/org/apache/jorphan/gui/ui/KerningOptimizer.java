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

package org.apache.jorphan.gui.ui;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Text rendering might be slow for long lines when kerning is enabled, so it is worth disabling kerning for long
 * texts.
 */
@API(since = "5.5", status = API.Status.INTERNAL)
public class KerningOptimizer {
    private final static Logger log = LoggerFactory.getLogger(KerningOptimizer.class);

    public static final KerningOptimizer INSTANCE = new KerningOptimizer();

    private volatile int maxLengthWithKerning = 10000;

    /**
     * Cache to avoid repeated calls to {@link Font#getAttributes()} since it copies the map every time.
     */
    static class FontKerningCache {
        private static final String CLIENT_PROPERTY_KEY = "[jmeter]FontKerningCache";
        WeakReference<Font> font;
        boolean kerning;

        static Boolean kerningOf(JComponent component) {
            Font font = component.getFont();
            if (font == null) {
                return null;
            }
            if (!font.hasLayoutAttributes()) {
                return false;
            }
            FontKerningCache cache = (FontKerningCache) component.getClientProperty(CLIENT_PROPERTY_KEY);
            if (cache == null) {
                cache = new FontKerningCache();
                component.putClientProperty(CLIENT_PROPERTY_KEY, cache);
            }
            if (cache.font == null || !font.equals(cache.font.get())) {
                cache.font = new WeakReference<>(font);
                cache.kerning = TextAttribute.KERNING_ON.equals(font.getAttributes().get(TextAttribute.KERNING));
            }
            return cache.kerning;
        }
    }

    /**
     * Configures the maximum document length for rendering with kerning enabled.
     *
     * @param length maximum document length for rendering with kerning enabled
     */
    public void setMaxTextLengthWithKerning(int length) {
        maxLengthWithKerning = length;
    }

    public int getMaxTextLengthWithKerning() {
        return maxLengthWithKerning;
    }

    /**
     * Configures text kerning according to the expected document length. This might be useful before setting the
     * document so the kerning is disabled before updating the document.
     *
     * @param component      text component for kerning configuration
     * @param documentLength expected document length
     */
    public void configureKerning(JComponent component, int documentLength) {
        Boolean kerning = FontKerningCache.kerningOf(component);
        if (kerning == null) {
            return;
        }
        boolean desiredKerning = documentLength <= maxLengthWithKerning;
        if (kerning != desiredKerning) {
            if (log.isDebugEnabled()) {
                log.info("Updating kerning (old: {}, new: {}), documentLength={}, component {}, ", kerning, desiredKerning, documentLength, component);
            }
            Font font = component.getFont();
            Font newFont = font.deriveFont(Collections.singletonMap(TextAttribute.KERNING, desiredKerning ? TextAttribute.KERNING_ON : 0));
            SwingUtilities.invokeLater(() -> component.setFont(newFont));
        }
    }

    /**
     * Adds a listener that disables kerning if text length reaches a certain threshold.
     *
     * @param textComponent text component for kerning configuration
     */
    public void installKerningListener(JTextComponent textComponent) {
        log.debug("Installing KerningOptimizer {} to {}", this, textComponent);
        textComponent.addPropertyChangeListener("document", new DisableKerningForLargeTexts(textComponent));
    }

    /**
     * Removes the listener that disables kerning if text length reaches a certain threshold.
     *
     * @param textComponent text component for kerning configuration
     */
    public void uninstallKerningListener(JTextComponent textComponent) {
        DisableKerningForLargeTexts kerningListener = null;
        for (PropertyChangeListener listener : textComponent.getPropertyChangeListeners("document")) {
            if (listener instanceof DisableKerningForLargeTexts) {
                kerningListener = (DisableKerningForLargeTexts) listener;
            }
        }
        if (kerningListener == null) {
            return;
        }
        log.debug("Uninstalling KerningOptimizer {} from {}", this, textComponent);
        Document document = textComponent.getDocument();
        if (document != null) {
            document.removeDocumentListener(kerningListener);
        }
        textComponent.removePropertyChangeListener("document", kerningListener);
    }

    static class DisableKerningForLargeTexts implements PropertyChangeListener, DocumentListener {
        final JTextComponent component;

        DisableKerningForLargeTexts(JTextComponent component) {
            this.component = component;
        }

        private void configureKerning(Document e) {
            // RSyntaxTextArea and other implementations do not expect setFont called from document change listeners
            // It looks like invokeLater fixes that
            Boolean kerning = FontKerningCache.kerningOf(component);
            if (kerning == null) {
                return;
            }
            boolean desiredKerning = e.getLength() <= INSTANCE.getMaxTextLengthWithKerning();
            if (kerning != desiredKerning) {
                SwingUtilities.invokeLater(() -> INSTANCE.configureKerning(component, e.getLength()));
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!"document".equals(evt.getPropertyName())) {
                return;
            }
            Document oldDocument = (Document) evt.getOldValue();
            if (oldDocument != null) {
                oldDocument.removeDocumentListener(this);
            }
            Document newDocument = (Document) evt.getNewValue();
            if (newDocument != null) {
                newDocument.addDocumentListener(this);
                configureKerning(newDocument);
            }
        }


        @Override
        public void insertUpdate(DocumentEvent e) {
            configureKerning(e.getDocument());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            configureKerning(e.getDocument());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            configureKerning(e.getDocument());
        }
    }
}
