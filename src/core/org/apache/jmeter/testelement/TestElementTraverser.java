package org.apache.jmeter.testelement;

import org.apache.jmeter.testelement.property.JMeterProperty;

/**
 * For traversing Test Elements, which contain property that can be other test
 * elements, strings, collections, maps, objects
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public interface TestElementTraverser
{

    /**
     * Notification that a new test element is about to be traversed.  
     * @param el
     */
    public void startTestElement(TestElement el);
    
    /**
     * Notification that the test element is now done.
     * @param el
     */
    public void endTestElement(TestElement el);
    
    /**
     * Notification that a property is starting.  This could be a test element
     * property or a Map property - depends on the context.
     * @param key
     */
    public void startProperty(JMeterProperty key);
    
    /**
     * Notification that a property is ending.  Again, this could be a test
     * element or a Map property, dependig on the context.
     * @param key
     */
    public void endProperty(JMeterProperty key);
    
}
