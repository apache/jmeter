package org.apache.jmeter.exceptions;

/**
 * Title:        Apache JMeter
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      Apache Foundation
 * @author Michael Stover
 * @version 1.0
 */

public class IllegalUserActionException extends Exception
{

	public IllegalUserActionException()
	{
	}

	public IllegalUserActionException(String name)
	{
		super(name);
	}
}