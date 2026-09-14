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

package org.apache.jmeter.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.FileEditor;
import org.apache.jmeter.testbeans.gui.TypeEditor;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.jorphan.util.StringUtilities;

public class CSVDataSetBeanInfo extends BeanInfoSupport {

    private static final CSVDataSetSchema SCHEMA = CSVDataSetSchema.INSTANCE;

    private static final String[] SHARE_TAGS = new String[3];
    static final int SHARE_ALL    = 0;

    // Store the resource keys
    static {
        for (CSVDataSet.ShareMode value : CSVDataSet.ShareMode.values()) {
            @SuppressWarnings("EnumOrdinal")
            int index = value.ordinal();
            SHARE_TAGS[index] = value.toString();
        }
    }

    public CSVDataSetBeanInfo() {
        super(CSVDataSet.class);

        createPropertyGroup("csv_data",             //$NON-NLS-1$
                new String[] { SCHEMA.getFilename().getName(), SCHEMA.getFileEncoding().getName(),
                        SCHEMA.getVariableNames().getName(), SCHEMA.getIgnoreFirstLine().getName(),
                        SCHEMA.getDelimiter().getName(), SCHEMA.getQuotedData().getName(),
                        SCHEMA.getRecycle().getName(), SCHEMA.getStopThread().getName(),
                        SCHEMA.getShareMode().getName() });

        PropertyDescriptor p = property(SCHEMA.getFilename().getName());
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getFilename().getDefaultValue());
        p.setValue(NOT_EXPRESSION, true);
        p.setPropertyEditorClass(FileEditor.class);

        p = property(SCHEMA.getFileEncoding().getName(), TypeEditor.ComboStringEditor);
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getFileEncoding().getDefaultValue());
        p.setValue(TAGS, getListFileEncoding());

        p = property(SCHEMA.getVariableNames().getName());
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getVariableNames().getDefaultValue());
        p.setValue(NOT_EXPRESSION, true);

        p = property(SCHEMA.getIgnoreFirstLine().getName());
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getIgnoreFirstLine().getDefaultValue());

        p = property(SCHEMA.getDelimiter().getName());
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getDelimiter().getDefaultValue());
        p.setValue(NOT_EXPRESSION, true);

        p = property(SCHEMA.getQuotedData().getName());
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getQuotedData().getDefaultValue());

        p = property(SCHEMA.getRecycle().getName());
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getRecycle().getDefaultValue());

        p = property(SCHEMA.getStopThread().getName());
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getStopThread().getDefaultValue());

        p = property(SCHEMA.getShareMode().getName(), TypeEditor.ComboStringEditor);
        p.setValue(RESOURCE_BUNDLE, getBeanDescriptor().getValue(RESOURCE_BUNDLE));
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, SCHEMA.getShareMode().getDefaultValue());
        p.setValue(NOT_OTHER, false);
        p.setValue(NOT_EXPRESSION, false);
        p.setValue(TAGS, SHARE_TAGS);
    }

    public static int getShareModeAsInt(String mode) {
        if (StringUtilities.isEmpty(mode)){
            return SHARE_ALL; // default (e.g. if test plan does not have definition)
        }
        for (int i = 0; i < SHARE_TAGS.length; i++) {
            if (SHARE_TAGS[i].equals(mode)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return array of String for possible sharing modes
     */
    public static String[] getShareTags() {
        String[] copy = new String[SHARE_TAGS.length];
        System.arraycopy(SHARE_TAGS, 0, copy, 0, SHARE_TAGS.length);
        return copy;
    }

    /**
     * Get the mains file encoding
     * list from https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html
     * @return a String[] with the list of file encoding
     */
    private static String[] getListFileEncoding() {
        return JOrphanUtils.split(JMeterUtils.getPropDefault("csvdataset.file.encoding_list", ""), "|"); //$NON-NLS-1$
    }
}
