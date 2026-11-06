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

package org.apache.jmeter.gui.util;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.junit.JMeterTestCase;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that verify {@link MenuFactory#addFileMenu(JPopupMenu)} does NOT add
 * the file-level actions (Open, Merge, Save Selection As) to the node popup menu.
 *
 * <p>Those actions already live in the application's File menu bar and must not be
 * duplicated in the right-click / Edit menu (GitHub issue #6633).
 *
 * <p>This test lives in the {@code dist-check} module because {@link MenuFactory}'s
 * static initializer scans the JMeter classpath for GUI components, which requires
 * the full JMeter distribution to be present on the classpath.
 *
 * <p>Because {@code addFileMenu} calls {@link org.apache.jmeter.gui.GuiPackage#getInstance()}
 * later in its body (for image-save and enable/disable items), and {@code GuiPackage} is
 * {@code null} in headless tests, we use a {@link RecordingPopupMenu} that captures every
 * item added to it.  The items we care about (Open, Merge, Save Selection As) are added –
 * or intentionally omitted – before the {@code GuiPackage} call, so the assertion is valid.
 */
public class MenuFactoryFileMenuTest extends JMeterTestCase {

    /**
     * A {@link JPopupMenu} subclass that records the action command of every
     * {@link JMenuItem} added to it.
     */
    private static final class RecordingPopupMenu extends JPopupMenu {
        private static final long serialVersionUID = 1L;
        final List<String> actionCommands = new ArrayList<>();

        @Override
        public Component add(Component comp) {
            if (comp instanceof JMenuItem item) {
                String cmd = item.getActionCommand();
                if (cmd != null) {
                    actionCommands.add(cmd);
                }
            }
            return super.add(comp);
        }
    }

    /**
     * Collect all action commands that {@code addFileMenu} adds to the popup.
     * The method will throw a {@link NullPointerException} when it tries to
     * dereference {@code GuiPackage.getInstance()} (which is {@code null} in
     * headless tests); we catch that and return whatever was recorded up to
     * that point.  The three actions we are testing are added before the NPE.
     */
    private static List<String> collectActionCommands(boolean addSaveTestFragment) {
        RecordingPopupMenu popup = new RecordingPopupMenu();
        try {
            MenuFactory.addFileMenu(popup, addSaveTestFragment);
        } catch (NullPointerException ignored) {
            // Expected: GuiPackage.getInstance() returns null in headless tests.
            // Items added before that call are already recorded.
        }
        return popup.actionCommands;
    }

    // -----------------------------------------------------------------------
    // tests
    // -----------------------------------------------------------------------

    /**
     * {@code ActionNames.OPEN} must NOT appear in the node popup menu.
     * It is a file-level action that belongs only in the File menu bar.
     */
    @Test
    public void addFileMenu_doesNotContainOpenAction() {
        List<String> commands = collectActionCommands(true);
        assertFalse(commands.contains(ActionNames.OPEN),
                "ActionNames.OPEN must not be added to the node popup / Edit menu "
                        + "(it duplicates the File menu entry – issue #6633). "
                        + "Found action commands: " + commands);
    }

    /**
     * {@code ActionNames.MERGE} must NOT appear in the node popup menu.
     * It is a file-level action that belongs only in the File menu bar.
     */
    @Test
    public void addFileMenu_doesNotContainMergeAction() {
        List<String> commands = collectActionCommands(true);
        assertFalse(commands.contains(ActionNames.MERGE),
                "ActionNames.MERGE must not be added to the node popup / Edit menu "
                        + "(it duplicates the File menu entry – issue #6633). "
                        + "Found action commands: " + commands);
    }

    /**
     * {@code ActionNames.SAVE_AS} (Save Selection As) must NOT appear in the
     * node popup menu.  It is a file-level action that belongs only in the
     * File menu bar.
     */
    @Test
    public void addFileMenu_doesNotContainSaveAsAction() {
        List<String> commands = collectActionCommands(true);
        assertFalse(commands.contains(ActionNames.SAVE_AS),
                "ActionNames.SAVE_AS must not be added to the node popup / Edit menu "
                        + "(it duplicates the File menu entry – issue #6633). "
                        + "Found action commands: " + commands);
    }

    /**
     * The same three actions must also be absent when the
     * {@code addSaveTestFragmentMenu=false} variant is used.
     */
    @Test
    public void addFileMenu_noFragment_doesNotContainDuplicateFileActions() {
        List<String> commands = collectActionCommands(false);
        assertFalse(commands.contains(ActionNames.OPEN),
                "OPEN must not appear even when addSaveTestFragmentMenu=false");
        assertFalse(commands.contains(ActionNames.MERGE),
                "MERGE must not appear even when addSaveTestFragmentMenu=false");
        assertFalse(commands.contains(ActionNames.SAVE_AS),
                "SAVE_AS must not appear even when addSaveTestFragmentMenu=false");
    }
}
