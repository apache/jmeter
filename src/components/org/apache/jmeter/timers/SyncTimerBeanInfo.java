package org.apache.jmeter.timers;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class SyncTimerBeanInfo extends BeanInfoSupport {

    /**
     * @param beanClass
     */
    public SyncTimerBeanInfo() {
        super(SyncTimer.class);
        
        createPropertyGroup("grouping", new String[] { "groupSize"});
        
        PropertyDescriptor p = property("groupSize");
        p.setValue(NOT_UNDEFINED,Boolean.TRUE);
        p.setValue(DEFAULT,new Integer(0));
    }


}
