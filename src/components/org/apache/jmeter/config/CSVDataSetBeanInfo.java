/*
 * Created on Sep 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.jmeter.config;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

/**
 * @author mstover
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CSVDataSetBeanInfo extends BeanInfoSupport
{

    /**
     * @param beanClass
     */
    public CSVDataSetBeanInfo()
    {
        super(CSVDataSet.class);
        createPropertyGroup("csv_data",new String[]{"filename","variableNames"});
        PropertyDescriptor p = property("filename");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setValue(NOT_EXPRESSION,Boolean.TRUE);
        p = property("variableNames");
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, "");
        p.setValue(NOT_EXPRESSION,Boolean.TRUE);
    }
}
