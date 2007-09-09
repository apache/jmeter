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

package org.apache.jmeter.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

/**
 * @author mstover
 * 
 */
public class CSVDataSetBeanInfo extends BeanInfoSupport {

    // These names must agree case-wise with the variable and property names
    private static final String FILENAME = "filename";               //$NON-NLS-1$
    private static final String FILE_ENCODING = "fileEncoding";      //$NON-NLS-1$
    private static final String VARIABLE_NAMES = "variableNames";    //$NON-NLS-1$
    private static final String DELIMITER = "delimiter";             //$NON-NLS-1$
    private static final String RECYCLE = "recycle";                 //$NON-NLS-1$
    private static final String STOPTHREAD = "stopThread";           //$NON-NLS-1$

	public CSVDataSetBeanInfo() {
		super(CSVDataSet.class);
		createPropertyGroup("csv_data",             //$NON-NLS-1$
                new String[] { FILENAME, FILE_ENCODING, VARIABLE_NAMES, DELIMITER, RECYCLE, STOPTHREAD });
        
		PropertyDescriptor p = property(FILENAME);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");        //$NON-NLS-1$
		p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        
		p = property(FILE_ENCODING);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");        //$NON-NLS-1$
		p.setValue(NOT_EXPRESSION, Boolean.TRUE);
		
		p = property(VARIABLE_NAMES);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");        //$NON-NLS-1$
		p.setValue(NOT_EXPRESSION, Boolean.TRUE);
		
        p = property(DELIMITER);
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, ",");        //$NON-NLS-1$
		p.setValue(NOT_EXPRESSION, Boolean.TRUE);
        
        p = property(RECYCLE);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.TRUE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);

        p = property(STOPTHREAD);
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Boolean.FALSE);
        p.setValue(NOT_EXPRESSION, Boolean.TRUE);
	}
}
