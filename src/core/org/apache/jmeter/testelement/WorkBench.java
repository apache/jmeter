package org.apache.jmeter.testelement;
import java.io.Serializable;
import java.util.List;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   March 13, 2001
 *@version   1.0
 ***************************************/

public class WorkBench extends TestPlan implements Serializable
{
	private static List itemsCanAdd = null;
	private boolean isRootNode;

	/****************************************
	 * Constructor for the WorkBench object
	 *
	 *@param name        Description of Parameter
	 *@param isRootNode  !ToDo (Parameter description)
	 ***************************************/
	public WorkBench(String name, boolean isRootNode)
	{
		setName(name);
	}

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public WorkBench()
	{
	}

}
