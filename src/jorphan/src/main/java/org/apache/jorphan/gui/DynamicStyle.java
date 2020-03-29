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

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apiguardian.api.API;

/**
 * By default, Swing does not provide a way to augment components when
 * look and feel changes. There's {@link JComponent#updateUI()}, however,
 * it requires to sub-class component class.
 * <p>{@code DynamicStyle} enables to augment the components (e.g. border, font, color)
 * as LaF changes</p>
 */
@API(since = "5.3", status = API.Status.EXPERIMENTAL)
public class DynamicStyle {
    private final Map<JComponent, List<Consumer<JComponent>>> listeners =
            new WeakHashMap<>();

    /**
     * Attaches a configuration action that is executed when Look and Feel changes.
     * <p>Note: the action is executed when {@code withDynamic} is called, and the action is
     * executed even if the new and the old LaFs are the same.</p>
     * @param component component to update
     * @param onUpdateUi action to run (immediately and when look and feel changes)
     * @param <T> type of the component
     * @return input component (e.g. for fluent APIs)
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public <T extends JComponent> T withDynamic(T component, Consumer<T> onUpdateUi) {
        // Explicit component update is required since the component already exists
        // and we can't want to wait for the next LaF change
        onUpdateUi.accept(component);
        synchronized (listeners) {
            listeners.compute(component, (k, v) -> {
                if (v == null) {
                    //noinspection unchecked
                    return Collections.singletonList((Consumer<JComponent>) onUpdateUi);
                }
                List<Consumer<JComponent>> res = v.size() == 1 ? new ArrayList<>(v) : v;
                //noinspection unchecked
                res.add((Consumer<JComponent>) onUpdateUi);
                return res;
            });
        }
        return component;
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public <T extends JComponent> T withBorder(T component, String resource) {
        return withDynamic(component, c -> {
            c.setBorder(UIManager.getBorder(resource));
        });
    }

    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public <T extends JComponent> T withFont(T component, String resource) {
        return withDynamic(component, c -> {
            c.setFont(UIManager.getFont(resource));
        });
    }

    /**
     * Schedules an action to be executed after each Look and Feel change.
     * @param action action to execute
     * @return a handle that can be used to un-register the listener
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public static Closeable onLaFChange(Runnable action) {
        // Explicit component update is required since the component already exists
        // and we can't want to wait for the next LaF change
        action.run();

        PropertyChangeListener listener = evt -> {
            if ("lookAndFeel".equals(evt.getPropertyName())) { // $NON-NLS-1$
                action.run();
            }
        };
        UIManager.addPropertyChangeListener(listener);
        return () -> UIManager.removePropertyChangeListener(listener);
    }

    /**
     * Re-initializes the current LaF and updates the UI for all the open windows.
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public void updateLaf() {
        updateLaf(UIManager.getLookAndFeel().getClass().getName());
    }

    /**
     * Set new look and feel for all the open windows.
     * @param className look and feel class name
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public void updateLaf(String className) {
        try {
            // setLookAndFeel has two purposes here:
            // 1) Nimbus LaF caches styles (e.g. Label.font), so if we want apply new zoom scale,
            //    then we need to invalidate the cache
            // 2) We need to generate "property changed lookAndFeel" UI event. Unfortunately,
            //    firePropertyChanged skips the event when old and new LaFs are equal
            UIManager.setLookAndFeel(className);
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException |
                UnsupportedLookAndFeelException e) {
            throw new IllegalStateException("Unable to update look and feel to " + className, e); // $NON-NLS-1$
        }

        List<Component> components = new ArrayList<>();
        for (final Window w : Window.getWindows()) {
            updateComponentTreeUI(w, components);
            components.clear();
        }
    }

    /**
     * Updates UI for the components under a given component.
     * @param root the root of the hierarchy to update
     */
    @API(since = "5.3", status = API.Status.EXPERIMENTAL)
    public void updateComponentTreeUI(Component root) {
        updateComponentTreeUI(root, new ArrayList<>());
    }

    private void updateComponentTreeUI(Component root, List<Component> components) {
        collectComponents(root, components);
        for (Component component : components) {
            if (component instanceof JComponent) {
                updateComponent((JComponent) component);
            }
            component.invalidate();
        }
        for (Component component : components) {
            component.validate();
        }
        for (Component component : components) {
            component.repaint();
        }
    }

    private void updateComponent(JComponent component) {
        synchronized (listeners) {
            List<Consumer<JComponent>> list = listeners.get(component);
            if (list == null) {
                return;
            }
            for (Consumer<JComponent> action : list) {
                action.accept(component);
            }
        }
    }

    private static void collectComponents(Component root, List<Component> components) {
        if (root == null) {
            // E.g. getTabComponentAt might return null
            // https://stackoverflow.com/questions/988734/jtabbedpane-gettabcomponentatint-returning-null
            return;
        }
        components.add(root);
        if (root instanceof JComponent) {
            JComponent jc = (JComponent) root;
            // Note: updateUI might alter the component tree, so we should call it before
            // we try iterating over the children
            jc.updateUI();
            if (!jc.getInheritsPopupMenu()) {
                JPopupMenu jpm = jc.getComponentPopupMenu();
                if (jpm != null) {
                    collectComponents(jpm, components);
                }
            }
        }
        Component[] children = null;
        if (root instanceof JMenu) {
            synchronized (root.getTreeLock()) {
                children = ((JMenu) root).getMenuComponents();
            }
        } else if (root instanceof Container) {
            synchronized (root.getTreeLock()) {
                children = ((Container) root).getComponents();
            }
        }
        if (children != null) {
            for (Component child : children) {
                collectComponents(child, components);
            }
        }
        if (root instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) root;
            int size = tabbedPane.getTabCount();
            for (int i = 0; i < size; i++) {
                // This is the contents of the tab
                collectComponents(tabbedPane.getComponentAt(i), components);
                // This is the tab itself (might be null)
                collectComponents(tabbedPane.getTabComponentAt(i), components);
            }
        }
    }
}
