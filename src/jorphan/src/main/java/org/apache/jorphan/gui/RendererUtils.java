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

package org.apache.jorphan.gui;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Utility class for Renderers
 */
public final class RendererUtils {
    private RendererUtils(){
        // uninstantiable
    }
    public static void applyRenderers(final JTable table, final TableCellRenderer [] renderers){
        final TableColumnModel columnModel = table.getColumnModel();
        for(int i = 0; i < renderers.length; i++){
            final TableCellRenderer rend = renderers[i];
            if (rend != null) {
                columnModel.getColumn(i).setCellRenderer(rend);
            }
        }
}
}
