package org.apache.jmeter.testelement.property;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public interface PropertyIterator
{
    public boolean hasNext();
    
    public JMeterProperty next();
    
    public void remove();
}
