package org.apache.jmeter.testelement;

import java.util.Collection;
import java.util.Map;

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
     * @param value
     */
    public void startProperty(Object key);
    
    /**
     * Notification that a property is ending.  Again, this could be a test
     * element or a Map property, dependig on the context.
     * @param key
     */
    public void endProperty(Object key);
    
    /**
     * Notification of the occurence of a property value that is a simple
     * object, such as a String, or Integer, etc.  Which property it relates to
     * is dependent on the context.
     * @param value
     */
    public void simplePropertyValue(Object value);
    
    /**
     * Notification that a Map object is starting.
     * @param map
     */
    public void startMap(Map map);
    
    /**
     * Notification that a Map object is ending.
     * @param map
     */
    public void endMap(Map map);
    
    /**
     * Notification that a collection object is starting.
     */
    public void startCollection(Collection col);
    
    /**
     * Notification that a collection object is ending.
     * @param col
     */
    public void endCollection(Collection col);
}
