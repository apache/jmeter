// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
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
 */
package org.apache.jmeter.testbeans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.Converter;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * This is an experimental class. An attempt to address the complexity of
 * writing new JMeter components.
 * <p>
 * TestBean currently extends AbstractTestElement to support
 * backward-compatibility, but the property-value-map may later on be
 * separated from the test beans themselves. To ensure this will be doable
 * with minimum damage, all inherited methods are deprecated.
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Revision$ updated on $Date$
 */
public abstract class TestBean extends AbstractTestElement
{
	protected static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * Property name to property descriptor method map.
     */
    private transient Map descriptors;

    /**
     * Parameter-less constructor.
     * <p>
     * This implementation will take care of obtaining bean-management
     * information if this was not already done.
     */
    protected TestBean()
    {
        super();
        
		try
		{
			// Obtain the property descriptors:
			BeanInfo beanInfo= Introspector.getBeanInfo(this.getClass());
			PropertyDescriptor[] desc= beanInfo.getPropertyDescriptors();
			descriptors= new HashMap();
			for (int i=0; i<desc.length; i++)
			{
				descriptors.put(desc[i].getName(), desc[i]);
			}
		}
		catch (IntrospectionException e)
		{
			log.error("Can't get beanInfo for "+this.getClass().getName(),
				e);
			throw new Error(e.toString()); // Programming error. Don't continue.
		}
    }

    /**
     * Prepare the bean for work by populating the bean's properties from the
     * property value map.
     * <p>
     * @deprecated to limit it's usage in expectation of moving it elsewhere.
     */
    public void prepare()
    {
        Object[] param= new Object[1];
        
        if (log.isDebugEnabled()) log.debug("Preparing "+this.getClass());
        
        for (PropertyIterator jprops= propertyIterator(); jprops.hasNext(); )
        {
            // Obtain a value of the appropriate type for this property. 
            JMeterProperty jprop= jprops.next();
            PropertyDescriptor descriptor= (PropertyDescriptor)descriptors.get(jprop.getName());

            if (descriptor == null)
            {
            	if (log.isDebugEnabled())
            	{
					log.debug("Ignoring auxiliary property "+jprop.getName());
            	}
				continue; 
            }

            Class type= descriptor.getPropertyType();
            Object value= unwrapProperty(jprop, type);
            
			if (log.isDebugEnabled()) log.debug("Setting "+jprop.getName()+"="+value);

            // Set the bean's property to the value we just obtained:
			if (value != null || !type.isPrimitive())
				// We can't assign null to primitive types.
			{
				param[0]= value;
				invokeOrBailOut(descriptor.getWriteMethod(), param);
			}
        }
    }
    
    /**
     * Utility method that invokes a method and does the error handling
     * around the invocation.
     * 
     * @param method
     * @param params
     * @return the result of the method invocation.
     */
	private Object invokeOrBailOut(Method method, Object[] params)
	{
		try
		{
				 return method.invoke(this, params);
		}
		catch (IllegalArgumentException e)
		{
			log.error("This should never happen.", e);
			throw new Error(e.toString()); // Programming error: bail out.
		}
		catch (IllegalAccessException e)
		{
			log.error("This should never happen.", e);
			throw new Error(e.toString()); // Programming error: bail out.
		}
		catch (InvocationTargetException e)
		{
			log.error("This should never happen.", e);
			throw new Error(e.toString()); // Programming error: bail out.
		}
	}

    /**
     * Utility method to obtain the value of a property in the given type.
     * <p>
     * I plan to get rid of this sooner than later, so please don't use it much.
     * 
     * @param property Property to get the value of.
     * @param type     Type of the result.
     * @return an object of the given type if it is one of the known supported
     *              types, or the value returned by property.getObjectValue
     * @deprecated
     */
    private static Object unwrapProperty(JMeterProperty property, Class type)
    {
        // TODO: Awful, but there will be time to improve... maybe using
        // property editors? Or just having each property know its
        // proper type? Or pre-building a type-to-valuegetter map?
        // Or maybe just getting rid of all this property mess and storing
        // the original objects instead?
        return Converter.convert(property.getObjectValue(),type);
    }

    /**
     * @see org.apache.jmeter.testelement.TestElement#addTestElement(org.apache.jmeter.testelement.TestElement)
     * @deprecated
     */
	public void addTestElement(TestElement el)
	{
		// Scan all properties for a writable property of the appropriate type:
		for (Iterator descs= descriptors.values().iterator();
			descs.hasNext(); )
		{
			PropertyDescriptor desc= (PropertyDescriptor)descs.next();
			if (desc.getPropertyType().isInstance(el)
				&& desc.getPropertyEditorClass() == null)
							// Note we ignore those for which we have an editor,
							// in assumption that they are already provided via
							// the GUI. Not very nice, but it's a solution.
							// TODO: find a nicer way to specify which TestElement
							// properties should be in the GUI and which should come
							// from the tree structure.
			{
				invokeOrBailOut(desc.getWriteMethod(), new Object[] { el });
				return; // We're done
			}
		}
		// If we found no property for this one...
		super.addTestElement(el);
	}
}
