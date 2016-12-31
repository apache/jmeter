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

package org.apache.jmeter.gui.util;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;

/**
 * derived from: http://www.javaspecialists.eu/archive/Issue145.html
 */
public final class TristateCheckBox extends JCheckBox {
    private static final long serialVersionUID = 1L;
    // Listener on model changes to maintain correct focusability
    private final class TSCBChangeListener implements ChangeListener, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -3718373200229708535L;

        @Override
        public void stateChanged(ChangeEvent e) {
            TristateCheckBox.this.setFocusable(
                    getModel().isEnabled());
        }
    }
    private final ChangeListener enableListener = new TSCBChangeListener();

    public TristateCheckBox() {
        this(null, null, TristateState.DESELECTED);
    }

    public TristateCheckBox(String text) {
        this(text, null, TristateState.DESELECTED);
    }

    public TristateCheckBox(String text, boolean selected) {
        this(text, null, selected ? TristateState.SELECTED : TristateState.DESELECTED);
    }

    public TristateCheckBox(String text, Icon icon, TristateState initial) {
        this(text, icon, initial, false);
    }

    // For testing only at present
    TristateCheckBox(String text, Icon icon, TristateState initial, boolean original) {
        super(text, icon);

        //Set default single model
        setModel(new TristateButtonModel(initial, this, original));

        // override action behaviour
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TristateCheckBox.this.iterateState();
            }
        });
        ActionMap actions = new ActionMapUIResource();
        actions.put("pressed", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) {
                TristateCheckBox.this.iterateState();
            }
        });
        actions.put("released", null);
        SwingUtilities.replaceUIActionMap(this, actions);
    }

    /**
     * Set state depending on property
     * @param element TestElement
     * @param propName String property name
     */
    public void setTristateFromProperty(TestElement element,String propName) {
        JMeterProperty jmp = element.getProperty(propName);
        if (jmp instanceof NullProperty) {
            this.setIndeterminate();
        } else {
            this.setSelected(jmp.getBooleanValue());
        }
    }
    
    /**
     * Sets a boolean property from a tristate checkbox.
     * 
     * @param element the test element
     * @param propName the property name
     */
    public void setPropertyFromTristate(TestElement element, String propName) {
        if (isIndeterminate()) {
            element.removeProperty(propName);
        } else {
            element.setProperty(propName, isSelected());
        }
    }
    
    // Next two methods implement new API by delegation to model
    public void setIndeterminate() {
        getTristateModel().setIndeterminate();
    }

    public boolean isIndeterminate() {
        return getTristateModel().isIndeterminate();
    }

    public TristateState getState() {
        return getTristateModel().getState();
    }

    //Overrides superclass method
    @Override
    public void setModel(ButtonModel newModel) {
        super.setModel(newModel);
        //Listen for enable changes
        if (model instanceof TristateButtonModel) {
            model.addChangeListener(enableListener);
        }
    }

    //Empty override of superclass method
    @Override
    public synchronized void addMouseListener(MouseListener l) {
    }

    // Mostly delegates to model
    private void iterateState() {
        //Maybe do nothing at all?
        if (!getModel().isEnabled()) {
            return;
        }

        grabFocus();

        // Iterate state
        getTristateModel().iterateState();

        // Fire ActionEvent
        int modifiers = 0;
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        if (currentEvent instanceof InputEvent) {
            modifiers = ((InputEvent) currentEvent).getModifiers();
        } else if (currentEvent instanceof ActionEvent) {
            modifiers = ((ActionEvent) currentEvent).getModifiers();
        }
        fireActionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, getText(),
                System.currentTimeMillis(), modifiers));
    }

    //Convenience cast
    public TristateButtonModel getTristateModel() {
        return (TristateButtonModel) super.getModel();
    }

    private static class TristateButtonModel extends ToggleButtonModel {

        private static final long serialVersionUID = 1L;
        private TristateState state = TristateState.DESELECTED;
        private final TristateCheckBox tristateCheckBox;
        private final Icon icon;
        private final boolean original;

        public TristateButtonModel(TristateState initial,
                TristateCheckBox tristateCheckBox, boolean original) {
            setState(TristateState.DESELECTED);
            this.tristateCheckBox = tristateCheckBox;
            icon = new TristateCheckBoxIcon();
            this.original = original;
        }

        public void setIndeterminate() {
            setState(TristateState.INDETERMINATE);
        }

        public boolean isIndeterminate() {
            return state == TristateState.INDETERMINATE;
        }

        // Overrides of superclass methods
        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            // Restore state display
            displayState();
        }

        @Override
        public void setSelected(boolean selected) {
            setState(selected ?
                    TristateState.SELECTED : TristateState.DESELECTED);
        }

        // Empty overrides of superclass methods
        @Override
        public void setArmed(boolean b) {
        }

        @Override
        public void setPressed(boolean b) {
        }

        void iterateState() {
            setState(state.next());
        }

        private void setState(TristateState state) {
            //Set internal state
            this.state = state;
            displayState();
            if (state == TristateState.INDETERMINATE && isEnabled()) {
                // force the events to fire

                // Send ChangeEvent
                fireStateChanged();

                // Send ItemEvent
                int indeterminate = 3;
                fireItemStateChanged(new ItemEvent(
                        this, ItemEvent.ITEM_STATE_CHANGED, this,
                        indeterminate));
            }
        }

        private void displayState() {
            super.setSelected(state != TristateState.DESELECTED);
            if (original) {
                super.setArmed(state == TristateState.INDETERMINATE);
            } else {
                if (state == TristateState.INDETERMINATE) {
                    tristateCheckBox.setIcon(icon); // Needed for all but Nimbus
                    tristateCheckBox.setSelectedIcon(icon); // Nimbus works - after a fashion - with this
                    tristateCheckBox.setDisabledSelectedIcon(icon); // Nimbus works - after a fashion - with this
                } else { // reset
                    if (tristateCheckBox!= null){
                        tristateCheckBox.setIcon(null);
                        tristateCheckBox.setSelectedIcon(null);
                        tristateCheckBox.setDisabledSelectedIcon(null); // Nimbus works - after a fashion - with this
                    }
                }
            }
            super.setPressed(state == TristateState.INDETERMINATE);

        }

        public TristateState getState() {
            return state;
        }
    }

    /**
     * derived from: http://www.coderanch.com/t/342563/GUI/java/TriState-CheckBox
     */
    private static class TristateCheckBoxIcon implements Icon, UIResource, Serializable {

        private static final long serialVersionUID = 290L;

        private final int iconHeight;
        private final int iconWidth;

        public TristateCheckBoxIcon() {
            // Assume that the UI has not changed since the checkbos was created
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            final Icon icon = (Icon) defaults.get("CheckBox.icon");
            iconHeight = icon.getIconHeight();
            iconWidth = icon.getIconWidth();
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JCheckBox cb = (JCheckBox) c;
            ButtonModel model = cb.getModel();

            // TODO fix up for Nimbus LAF
            if (model.isEnabled()) {
                if (model.isPressed() && model.isArmed()) {
                    g.setColor(MetalLookAndFeel.getControlShadow());
                    g.fillRect(x, y, iconWidth - 1, iconHeight - 1);
                    drawPressed3DBorder(g, x, y, iconWidth, iconHeight);
                } else {
                    drawFlush3DBorder(g, x, y, iconWidth, iconHeight);
                }
                g.setColor(MetalLookAndFeel.getControlInfo());
            } else {
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.drawRect(x, y, iconWidth - 1, iconHeight - 1);
            }

            drawLine(g, x, y);
        }// paintIcon

        private void drawLine(Graphics g, int x, int y) {
            final int left = x + 2;
            final int right =  x + (iconWidth - 4);
            int height = y + iconHeight/2;
            g.drawLine(left, height, right, height);
            g.drawLine(left, height - 1, right, height - 1);
        }

        private void drawFlush3DBorder(Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            g.setColor(MetalLookAndFeel.getControlDarkShadow());
            g.drawRect(0, 0, w - 2, h - 2);
            g.setColor(MetalLookAndFeel.getControlHighlight());
            g.drawRect(1, 1, w - 2, h - 2);
            g.setColor(MetalLookAndFeel.getControl());
            g.drawLine(0, h - 1, 1, h - 2);
            g.drawLine(w - 1, 0, w - 2, 1);
            g.translate(-x, -y);
        }

        private void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            drawFlush3DBorder(g, 0, 0, w, h);
            g.setColor(MetalLookAndFeel.getControlShadow());
            g.drawLine(1, 1, 1, h - 2);
            g.drawLine(1, 1, w - 2, 1);
            g.translate(-x, -y);
        }

        @Override
        public int getIconWidth() {
            return iconWidth;
        }

        @Override
        public int getIconHeight() {
            return iconHeight;
        }
    }
}
