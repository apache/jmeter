package org.apache.jmeter.testbeans.gui;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

public interface TestBeanPropertyEditor extends PropertyEditor {
	
	public void setDescriptor(PropertyDescriptor descriptor);

}
