/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

/*
 * Created on May 16, 2004
 *
 */
package org.apache.jmeter.protocol.jdbc.sampler;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TextAreaEditor;

/**
 * @author mstover
 * 
 */
public class JDBCSamplerBeanInfo extends BeanInfoSupport {

	/**
	 * 
	 */
	public JDBCSamplerBeanInfo() {
		super(JDBCSampler.class);

		createPropertyGroup("varName", new String[] { "dataSource" });

		createPropertyGroup("sql", new String[] { "queryType", "query" });

		PropertyDescriptor p = property("dataSource");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");

		p = property("queryType");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, JDBCSampler.SELECT);
		p.setValue(NOT_OTHER,Boolean.TRUE);
		p.setValue(TAGS,new String[]{JDBCSampler.SELECT,JDBCSampler.UPDATE,JDBCSampler.CALLABLE});

		p = property("query");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, "");
		p.setPropertyEditorClass(TextAreaEditor.class);

	}
}
