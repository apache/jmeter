package org.apache.jmeter.assertions;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class CompareAssertionBeanInfo extends BeanInfoSupport {

	public CompareAssertionBeanInfo() {
		super(CompareAssertion.class);
		createPropertyGroup("compareChoices", new String[] { "compareContent", "compareTime" });
		PropertyDescriptor p = property("compareContent");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, Boolean.TRUE);
		p.setValue(NOT_EXPRESSION, Boolean.TRUE);
		p = property("compareTime");
		p.setValue(NOT_UNDEFINED, Boolean.TRUE);
		p.setValue(DEFAULT, new Long(-1));
		p.setValue(NOT_EXPRESSION, Boolean.FALSE);		
	}

}
