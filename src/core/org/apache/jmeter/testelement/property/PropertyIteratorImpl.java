package org.apache.jmeter.testelement.property;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class PropertyIteratorImpl implements PropertyIterator
{

    Iterator iter;
        
        public PropertyIteratorImpl(Collection value)
        {
            iter = value.iterator();
        }
        
        public PropertyIteratorImpl()
        {
        }
        
        public void setCollection(Collection value)
        {
            iter = value.iterator();
        }
        
        public boolean hasNext()
        {
            return iter.hasNext();
        }
        
        public JMeterProperty next()
        {
            return (JMeterProperty)iter.next();
        }

    /**
     * @see org.apache.jmeter.testelement.property.PropertyIterator#remove()
     */
    public void remove()
    {
        iter.remove();
    }

}
