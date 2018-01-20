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

/**
 * Collect all the action names together in one place.
 * This helps to ensure that there are no duplicates
 * 
 */
public final class ActionNames {

    public static final String ABOUT            = "about"; // $NON-NLS-1$
    public static final String ACTION_SHUTDOWN  = "shutdown"; // $NON-NLS-1$
    public static final String ACTION_START     = "start"; // $NON-NLS-1$
    public static final String ACTION_START_NO_TIMERS = "start_no_timers"; // $NON-NLS-1$
    public static final String ACTION_STOP      = "stop"; // $NON-NLS-1$
    public static final String ADD              = "Add"; // $NON-NLS-1$
    public static final String ADD_ALL          = "add_all"; // $NON-NLS-1$
    public static final String ADD_PARENT       = "Add Parent"; // $NON-NLS-1$
    public static final String ADD_THINK_TIME_BETWEEN_EACH_STEP    = "Add Think Time between each step"; // $NON-NLS-1$
    public static final String ANALYZE_FILE     = "Analyze File"; // $NON-NLS-1$
    public static final String APPLY_NAMING_CONVENTION    = "Apply Naming Convention"; // $NON-NLS-1$
    public static final String CHANGE_LANGUAGE  = "change_language"; // $NON-NLS-1$
    public static final String CHANGE_PARENT    = "Change Parent"; // $NON-NLS-1$
    public static final String CHECK_DIRTY      = "check_dirty"; // $NON-NLS-1$
    public static final String CHECK_REMOVE     = "check_remove"; // $NON-NLS-1$
    public static final String CHECK_CUT        = "check_cut"; // $NON-NLS-1$
    public static final String CLEAR            = "action.clear"; // $NON-NLS-1$
    public static final String CLEAR_ALL        = "action.clear_all"; // $NON-NLS-1$
    public static final String CLOSE            = "close"; // $NON-NLS-1$
    public static final String COLLAPSE_ALL     = "collapse all"; // $NON-NLS-1$
    public static final String COPY             = "Copy"; // $NON-NLS-1$
    public static final String CUT              = "Cut"; // $NON-NLS-1$
    public static final String DEBUG_ON         = "debug_on"; // $NON-NLS-1$
    public static final String DEBUG_OFF        = "debug_off"; // $NON-NLS-1$
    public static final String DISABLE          = "disable"; // $NON-NLS-1$
    /** Copy, then paste afterwards */
    public static final String DUPLICATE        = "duplicate"; // $NON-NLS-1$
    public static final String EDIT             = "edit"; // $NON-NLS-1$
    public static final String ENABLE           = "enable"; // $NON-NLS-1$
    public static final String EXIT             = "exit"; // $NON-NLS-1$
    public static final String EXPAND_ALL       = "expand all"; // $NON-NLS-1$
    public static final String FUNCTIONS        = "functions"; // $NON-NLS-1$
    public static final String HELP             = "help"; // $NON-NLS-1$
    public static final String HEAP_DUMP        = "heap_dump"; // $NON-NLS-1$
    public static final String LAF_PREFIX       = "laf:"; // Look and Feel prefix
    public static final String LINK_BUG_TRACKER = "link_bug_tracker:"; // URI of bug tracker
    public static final String LINK_COMP_REF    = "link_component_reference:"; // URI of component reference
    public static final String LINK_FUNC_REF    = "link_function_reference:"; // URI of functions reference
    public static final String LINK_NIGHTLY_BUILD = "link_nightly_build:"; // URI of nightly build
    public static final String LINK_RELEASE_NOTES = "link_release_notes"; // URI of the release note
    public static final String LOGGER_PANEL_ENABLE_DISABLE     = "logger_panel_enable_disable"; // $NON-NLS-1$
    public static final String LOG_LEVEL_PREFIX = "loglevel:"; // Logger level prefix
    public static final String MERGE            = "merge"; // $NON-NLS-1$
    public static final String OPEN             = "open"; // $NON-NLS-1$
    public static final String OPEN_RECENT      = "open_recent"; // $NON-NLS-1$
    public static final String TEMPLATES        = "templates"; // $NON-NLS-1$
    public static final String PASTE            = "Paste"; // $NON-NLS-1$
    public static final String REMOTE_EXIT      = "remote_exit"; // $NON-NLS-1$
    public static final String REMOTE_EXIT_ALL  = "remote_exit_all"; // $NON-NLS-1$
    public static final String REMOTE_SHUT      = "remote_shut"; // $NON-NLS-1$
    public static final String REMOTE_SHUT_ALL  = "remote_shut_all"; // $NON-NLS-1$
    public static final String REMOTE_START     = "remote_start"; // $NON-NLS-1$
    public static final String REMOTE_START_ALL = "remote_start_all"; // $NON-NLS-1$
    public static final String REMOTE_STOP      = "remote_stop"; // $NON-NLS-1$
    public static final String REMOTE_STOP_ALL  = "remote_stop_all"; // $NON-NLS-1$
    public static final String REMOVE           = "remove"; // $NON-NLS-1$
    public static final String REVERT_PROJECT   = "revert_project"; // $NON-NLS-1$
    public static final String SAVE             = "save"; // $NON-NLS-1$
    public static final String SAVE_ALL_AS      = "save_all_as";  // $NON-NLS-1$
    public static final String SAVE_AS          = "save_as"; // $NON-NLS-1$
    public static final String SAVE_AS_TEST_FRAGMENT          = "save_as_test_fragment"; // $NON-NLS-1$
    public static final String SAVE_GRAPHICS    = "save_graphics"; // $NON-NLS-1$
    public static final String SAVE_GRAPHICS_ALL= "save_graphics_all"; // $NON-NLS-1$
    public static final String SSL_MANAGER      = "sslManager"; // $NON-NLS-1$
    public static final String STOP_THREAD      = "stop_thread"; // $NON-NLS-1$
    public static final String SUB_TREE_LOADED  = "sub_tree_loaded"; // $NON-NLS-1$
    public static final String SUB_TREE_MERGED  = "sub_tree_merged"; // $NON-NLS-1$
    public static final String SUB_TREE_SAVED   = "sub_tree_saved"; // $NON-NLS-1$
    public static final String TOGGLE           = "toggle"; // $NON-NLS-1$ enable/disable
    public static final String TOOLBAR          = "toolbar"; // $NON-NLS-1$
    public static final String WHAT_CLASS       = "what_class"; // $NON-NLS-1$
    public static final String SEARCH_TREE      = "search_tree"; // $NON-NLS-1$
    public static final String SEARCH_RESET     = "search_reset"; // $NON-NLS-1$
    public static final String MOVE_UP          = "move_up"; // $NON-NLS-1$
    public static final String MOVE_DOWN        = "move_down"; // $NON-NLS-1$
    public static final String MOVE_LEFT        = "move_left"; // $NON-NLS-1$
    public static final String MOVE_RIGHT       = "move_right"; // $NON-NLS-1$
    public static final String UNDO             = "undo"; // $NON-NLS-1$
    public static final String REDO             = "redo"; // $NON-NLS-1$
    public static final String QUICK_COMPONENT  = "quick_component"; // $NON-NLS-1$
    public static final String COLLAPSE         = "collapse"; // $NON-NLS-1$
    public static final String EXPAND           = "expand"; // $NON-NLS-1$
    public static final String RUN_TG           = "run_tg"; // $NON-NLS-1$
    public static final String RUN_TG_NO_TIMERS = "run_tg_no_timers"; // $NON-NLS-1$
    public static final String SAVE_BEFORE_RUN  = "save_before_run"; //$NON-NLS-1$
    public static final String THREAD_DUMP      = "thread_dump"; // $NON-NLS-1$
    public static final String VALIDATE_TG      = "validate_tg"; //$NON-NLS-1$
    public static final String ZOOM_IN          = "zoom_in"; //$NON-NLS-1$
    public static final String ZOOM_OUT         = "zoom_out"; //$NON-NLS-1$

    // Prevent instantiation
    private ActionNames() {}
}
