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

package org.apache.jmeter.testbeans.gui;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

/**
 * Allow direct specification of property editors.
 */
public enum TypeEditor {
    FileEditor { // NOSONAR Keep naming for compatibility
        @Override
        PropertyEditor getInstance(PropertyDescriptor descriptor) {
            return new FileEditor(descriptor);
        }
    },
    PasswordEditor { // NOSONAR Keep naming for compatibility
        @Override
        PropertyEditor getInstance(PropertyDescriptor descriptor) {
            return new PasswordEditor();
        }
    },
    TableEditor { // NOSONAR Keep naming for compatibility
        @Override
        PropertyEditor getInstance(PropertyDescriptor descriptor) {
            return new TableEditor();
        }
    },
    TextAreaEditor { // NOSONAR Keep naming for compatibility
        @Override
        PropertyEditor getInstance(PropertyDescriptor descriptor) {
            return new TextAreaEditor(descriptor);
        }
    },
    ComboStringEditor { // NOSONAR Keep naming for compatibility
        @Override
        PropertyEditor getInstance(PropertyDescriptor descriptor) {
            return new ComboStringEditor(descriptor);
        }
    };

    // Some editors may need the descriptor
    abstract PropertyEditor getInstance(PropertyDescriptor descriptor);
}
