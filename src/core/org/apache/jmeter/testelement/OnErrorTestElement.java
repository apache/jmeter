/*
 * Created on Dec 9, 2003
 *
 */
package org.apache.jmeter.testelement;

import org.apache.jmeter.testelement.property.IntegerProperty;

/**
 * @author sebb at apache dot org
 * @version $revision$ $date$
 */
public abstract class OnErrorTestElement extends AbstractTestElement
{
	/* Action to be taken when a Sampler error occurs*/
	public final static int ON_ERROR_CONTINUE = 0;
	public final static int ON_ERROR_STOPTHREAD = 1;
	public final static int ON_ERROR_STOPTEST = 2;

	/* Property name */
	public final static String ON_ERROR_ACTION = "OnError.action";

    protected OnErrorTestElement()
    {
        super();
    }
    
	public void setErrorAction(int value)
	{
		setProperty(new IntegerProperty(ON_ERROR_ACTION, value));
	}

	public int getErrorAction()
	{
		int value = getPropertyAsInt(ON_ERROR_ACTION);
		return value;
	}
   
    public boolean isContinue()
    {
		int value = getErrorAction();
		return value == ON_ERROR_CONTINUE;
    }

	public boolean isStopThread()
	{	
		int value = getErrorAction();
		return value == ON_ERROR_STOPTHREAD;
	}
	
	public boolean isStopTest()
	{
		int value = getErrorAction();
		return value == ON_ERROR_STOPTEST;
	}
}
