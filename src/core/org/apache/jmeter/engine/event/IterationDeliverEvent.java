/*
 * Created on Apr 30, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.engine.event;

import org.apache.jmeter.testelement.TestElement;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IterationDeliverEvent  
{  
	TestElement current;  
	TestElement source;  
      
	public IterationDeliverEvent(TestElement source,TestElement c)  
	{  
		current = c;  
		this.source = source;  
	}  
	/**  
	 * @return  
	 */  
	public TestElement getCurrent()  
	{  
		return current;  
	}  
  
	/**  
	 * @param element  
	 */  
	public void setCurrent(TestElement element)  
	{  
		current = element;  
	}  
  
	/**  
	 * @return  
	 */  
	public TestElement getSource()  
	{  
		return source;  
	}  
  
	/**  
	 * @param element  
	 */  
	public void setSource(TestElement element)  
	{  
		source = element;  
	}  
  
}
