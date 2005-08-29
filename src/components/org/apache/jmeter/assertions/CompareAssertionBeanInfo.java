package org.apache.jmeter.assertions;

import java.beans.PropertyDescriptor;
import java.util.Arrays;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TableEditor;

public class CompareAssertionBeanInfo extends BeanInfoSupport {

	public CompareAssertionBeanInfo() {
		super(CompareAssertion.class);
		createPropertyGroup("compareChoices", new String[] { "compareContent", "compareTime" });
		createPropertyGroup("comparison_filters", new String[]{"stringsToSkip"});
		PropertyDescriptor p = property("compareContent");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.TRUE);
		p.setValue(NOT_EXPRESSION, Boolean.TRUE);
		p = property("compareTime");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, new Long(-1));
		p.setValue(NOT_EXPRESSION, Boolean.FALSE);	
		p = property("stringsToSkip");
		p.setPropertyEditorClass(TableEditor.class);
		p.setValue(TableEditor.CLASSNAME,"java.lang.String");
		p.setValue(TableEditor.HEADERS,new String[]{"Regex String"});
		p.setValue(NOT_UNDEFINED,Boolean.TRUE);
		p.setValue(DEFAULT,Arrays.asList(new String[]{"One","Two"}));
		p.setValue(MULTILINE,Boolean.TRUE);
		
	}

}
