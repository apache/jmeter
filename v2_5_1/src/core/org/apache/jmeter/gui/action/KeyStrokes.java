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

package org.apache.jmeter.gui.action;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

/*
 * Collect all the keystrokes together in one place.
 * This helps to ensure that there are no duplicates.
 */
public final class KeyStrokes {
    // Prevent instantiation
    private KeyStrokes(){
    }

    // Bug 47064 - fixes for Mac LAF
    private static final int CONTROL_MASK =Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    public static final KeyStroke COPY              = KeyStroke.getKeyStroke(KeyEvent.VK_C, CONTROL_MASK);
    public static final KeyStroke DEBUG_OFF         = KeyStroke.getKeyStroke(KeyEvent.VK_D, CONTROL_MASK);
    public static final KeyStroke DEBUG_ON          = KeyStroke.getKeyStroke(KeyEvent.VK_D, CONTROL_MASK | KeyEvent.SHIFT_DOWN_MASK);
    public static final KeyStroke CLEAR_ALL         = KeyStroke.getKeyStroke(KeyEvent.VK_E, CONTROL_MASK);
    public static final KeyStroke CLEAR             = KeyStroke.getKeyStroke(KeyEvent.VK_E, CONTROL_MASK | KeyEvent.SHIFT_DOWN_MASK);
    public static final KeyStroke FUNCTIONS         = KeyStroke.getKeyStroke(KeyEvent.VK_F, CONTROL_MASK);
    public static final KeyStroke SAVE_GRAPHICS     = KeyStroke.getKeyStroke(KeyEvent.VK_G, CONTROL_MASK);
    public static final KeyStroke SAVE_GRAPHICS_ALL = KeyStroke.getKeyStroke(KeyEvent.VK_G, CONTROL_MASK | KeyEvent.SHIFT_DOWN_MASK);
    public static final KeyStroke HELP              = KeyStroke.getKeyStroke(KeyEvent.VK_H, CONTROL_MASK);
    public static final KeyStroke CLOSE             = KeyStroke.getKeyStroke(KeyEvent.VK_L, CONTROL_MASK);
    public static final KeyStroke SSL_MANAGER       = KeyStroke.getKeyStroke(KeyEvent.VK_M, CONTROL_MASK);
    public static final KeyStroke OPEN              = KeyStroke.getKeyStroke(KeyEvent.VK_O, CONTROL_MASK);
    public static final KeyStroke EXIT              = KeyStroke.getKeyStroke(KeyEvent.VK_Q, CONTROL_MASK);
    public static final KeyStroke ACTION_START      = KeyStroke.getKeyStroke(KeyEvent.VK_R, CONTROL_MASK);
    public static final KeyStroke REMOTE_START_ALL  = KeyStroke.getKeyStroke(KeyEvent.VK_R, CONTROL_MASK | KeyEvent.SHIFT_DOWN_MASK);
    public static final KeyStroke SAVE              = KeyStroke.getKeyStroke(KeyEvent.VK_S, CONTROL_MASK);
    public static final KeyStroke SAVE_ALL_AS       = KeyStroke.getKeyStroke(KeyEvent.VK_S, CONTROL_MASK | KeyEvent.SHIFT_DOWN_MASK);
    public static final KeyStroke TOGGLE            = KeyStroke.getKeyStroke(KeyEvent.VK_T, CONTROL_MASK);
    public static final KeyStroke PASTE             = KeyStroke.getKeyStroke(KeyEvent.VK_V, CONTROL_MASK);
    public static final KeyStroke WHAT_CLASS        = KeyStroke.getKeyStroke(KeyEvent.VK_W, CONTROL_MASK);
    public static final KeyStroke CUT               = KeyStroke.getKeyStroke(KeyEvent.VK_X, CONTROL_MASK);
    public static final KeyStroke REMOTE_STOP_ALL   = KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_DOWN_MASK);
    public static final KeyStroke REMOTE_SHUT_ALL   = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.ALT_DOWN_MASK);

    public static final KeyStroke REMOVE            = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
    public static final KeyStroke ACTION_STOP       = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, CONTROL_MASK);
    public static final KeyStroke ACTION_SHUTDOWN   = KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, CONTROL_MASK);
    public static final KeyStroke COLLAPSE_ALL      = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, CONTROL_MASK);
    // VK_PLUS + CTRL_DOWN_MASK did not work...
    public static final KeyStroke EXPAND_ALL        = KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, CONTROL_MASK | KeyEvent.SHIFT_DOWN_MASK);

    /**
     * Check if an event matches the KeyStroke definition.
     *
     * @param e event
     * @param k keystroke
     * @return true if event matches the keystroke definition
     */
    public static boolean matches(KeyEvent e, KeyStroke k){
        final int modifiersEx = e.getModifiersEx()  | e.getModifiers();// Hack to get full modifier value
        return e.getKeyCode() == k.getKeyCode() && modifiersEx == k.getModifiers();
    }
}
