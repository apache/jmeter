/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jmeter.timers;

import java.util.*;
import java.io.*;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.testelement.AbstractTestElement;

/************************************************************
 *  This class implements a constant throughput timer with its own panel and
 *  fields for value update and user interaction.
 *
 *@author     <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *@created    $Date$
 *@version    $Revision$ $Date$
 ***********************************************************/

public class ConstantThroughputTimer extends AbstractTestElement implements Timer, Serializable
{
	public final static String THROUGHPUT = "ConstantThroughputTimer.throughput";
	private static List addableList = new LinkedList();

	// The target time for the start of the next request:
	private long targetTime= 0;

	// Ideal interval between two requests to get the desired throughput:
	private long delay;

	/************************************************************
	 *  !ToDo (Constructor description)
	 ***********************************************************/
	public ConstantThroughputTimer()
	{
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  throughput  !ToDo (Parameter description)
	 ***********************************************************/
	public void setThroughput(long throughput)
	{
		setProperty(THROUGHPUT,new Long(throughput));
		delay= 60000/throughput;
	}

	public void setRange(double range) { }
	public double getRange() { return (double)0; }
	public void setDelay(long delay) { }
	public long getDelay() { return 0; }


	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public long getThroughput()
	{
		Object throughput = getProperty(THROUGHPUT);
		if(throughput instanceof Long)
		{
			return ((Long)throughput).longValue();
		}
		else
		{
			return Long.parseLong((String)throughput);
		}
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public synchronized long delay()
	{
		long currentTime= System.currentTimeMillis();
		long currentTarget= targetTime==0 ? currentTime : targetTime;
		targetTime=currentTarget+delay;
		if (currentTime > currentTarget) return 0;
		return currentTarget-currentTime;
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public String toString()
	{
		return JMeterUtils.getResString("constant_throughput_timer_memo");
	}

	public Object clone() {
	  ConstantThroughputTimer result= (ConstantThroughputTimer)super.clone();
	  result.targetTime= 0;
	  return result;
	}
}
