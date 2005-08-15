package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class CompareAssertion extends AbstractTestElement implements Assertion, TestBean, Serializable,
		LoopIterationListener {
	static Logger log = LoggingManager.getLoggerForClass();
	transient List responses;

	transient boolean iterationDone = false;

	private boolean compareContent = true;

	private long compareTime = -1;

	public CompareAssertion() {
		super();
	}

	public AssertionResult getResult(SampleResult response) {
		log.info("get assertion result for sample");
		responses.add(response);
		if (responses.size() > 1) {
			AssertionResult result = new AssertionResult(false,false,null);
			compareContent(result);
			compareTime(result);
			return result;
		} else
			return new AssertionResult(false, false, null);
	}
	
	protected void compareTime(AssertionResult result)
	{
		if(compareTime > -1)
		{
			Iterator iter = responses.iterator();
			long prevTime = -1;
			SampleResult prevResult = null;
			boolean success = true;
			while (iter.hasNext()) {
				SampleResult sResult = (SampleResult) iter.next();
				long currentTime = sResult.getTime();
				if (prevTime != -1)
				{
					success = Math.abs(prevTime - currentTime) < compareTime;
					prevResult = sResult;
				}
				if (!success) {
					result.setFailure(true);
					StringBuffer message = new StringBuffer("##################\n");
					message.append("From Request: ");
					message.append(prevResult.toString());
					message.append("\n\n");
					message.append("Response Time = ");
					message.append(prevTime);
					message.append("\n\n");
					message.append("Not Close Enough To: \n\n");
					message.append("From Request: ");
					message.append(sResult.toString());
					message.append("\n\n");
					message.append("Response Time = ");
					message.append(currentTime);
					message.append("/n/n");
					message.append("==============================\n\n");
					result.setFailureMessage(message.toString());
					break;
				}
				prevResult = sResult;
				prevTime = currentTime;
			}
		}
	}

	protected void compareContent(AssertionResult result) {
		if (compareContent) {
			Iterator iter = responses.iterator();
			String prevContent = null;
			SampleResult prevResult = null;
			boolean success = true;
			while (iter.hasNext()) {
				SampleResult sResult = (SampleResult) iter.next();
				String currentContent;
				try {
					currentContent = new String(sResult.getResponseData(), sResult.getDataEncoding());
				} catch (UnsupportedEncodingException e) {
					result.setError(true);
					result.setFailureMessage("Unsupported Encoding Exception: " +sResult.getDataEncoding());
					return;
				}
				if (prevContent != null)
				{
					success = prevContent.equals(currentContent);
				}
				if (!success) {
					result.setFailure(true);
					StringBuffer message = new StringBuffer("##################\n");
					message.append("From Request: ");
					message.append(prevResult.toString());
					message.append("\n\n");
					message.append(prevContent);
					message.append("\n\n");
					message.append("Not Equal To: \n\n");
					message.append("From Request: ");
					message.append(sResult.toString());
					message.append("\n\n");
					message.append(currentContent);
					message.append("/n/n");
					message.append("==============================\n\n");
					result.setFailureMessage(message.toString());
					break;
				}
				prevResult = sResult;
				prevContent = currentContent;
			}
		}
	} 
	
	/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.jmeter.engine.event.LoopIterationListener#iterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
		 */
	public void iterationStart(LoopIterationEvent iterEvent) {
		log.info("iteration started for compare Assertion");
		responses = new LinkedList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.engine.event.LoopIterationListener#iterationEnd(org.apache.jmeter.engine.event.LoopIterationEvent)
	 */
	public void iterationEnd(LoopIterationEvent iterEvent) {
		log.info("iteration ended for compare Assertion");
		responses = null;
	}

	/**
	 * @return Returns the compareContent.
	 */
	public boolean isCompareContent() {
		return compareContent;
	}

	/**
	 * @param compareContent The compareContent to set.
	 */
	public void setCompareContent(boolean compareContent) {
		this.compareContent = compareContent;
	}

	/**
	 * @return Returns the compareTime.
	 */
	public long getCompareTime() {
		return compareTime;
	}

	/**
	 * @param compareTime The compareTime to set.
	 */
	public void setCompareTime(long compareTime) {
		this.compareTime = compareTime;
	}

}
