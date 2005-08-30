package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.oro.text.regex.StringSubstitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;

public class CompareAssertion extends AbstractTestElement implements Assertion, TestBean, Serializable,
		LoopIterationListener {
	static Logger log = LoggingManager.getLoggerForClass();

	transient List responses;

	private StringSubstitution emptySub = new StringSubstitution("");

	transient boolean iterationDone = false;

	private boolean compareContent = true;

	private long compareTime = -1;

	Collection<SubstitutionElement> stringsToSkip;

	public CompareAssertion() {
		super();
	}

	public AssertionResult getResult(SampleResult response) {
		log.info("get assertion result for sample");
		responses.add(response);
		if (responses.size() > 1) {
			CompareAssertionResult result = new CompareAssertionResult(false, false, null);
			compareContent(result);
			compareTime(result);
			return result;
		} else
			return new AssertionResult(false, false, null);
	}

	protected void compareTime(CompareAssertionResult result) {
		if (compareTime > -1) {
			Iterator iter = responses.iterator();
			long prevTime = -1;
			SampleResult prevResult = null;
			boolean success = true;
			while (iter.hasNext()) {
				SampleResult sResult = (SampleResult) iter.next();
				long currentTime = sResult.getTime();
				if (prevTime != -1) {
					success = Math.abs(prevTime - currentTime) < compareTime;
					prevResult = sResult;
				}
				if (!success) {
					result.setFailure(true);
					StringBuffer buf = new StringBuffer(prevResult.getSamplerData().trim()).append("\n").append(
							prevResult.getRequestHeaders()).append("\n\n").append("Response Time: ").append(prevTime);
					result.addToBaseResult(buf.toString());
					buf = new StringBuffer(sResult.getSamplerData().trim()).append("\n").append(
							sResult.getRequestHeaders()).append("\n\n").append("Response Time: ").append(currentTime);
					result.addToSecondaryResult(buf.toString());
					result.setFailureMessage("Responses differ in response time");
					break;
				}
				prevResult = sResult;
				prevTime = currentTime;
			}
		}
	}

	protected void compareContent(CompareAssertionResult result) {
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
					result.setFailureMessage("Unsupported Encoding Exception: " + sResult.getDataEncoding());
					return;
				}
				currentContent = filterString(currentContent);
				if (prevContent != null) {
					success = prevContent.equals(currentContent);
				}
				if (!success) {
					result.setFailure(true);
					StringBuffer buf = new StringBuffer(prevResult.getSamplerData().trim()).append("\n").append(
							prevResult.getRequestHeaders()).append("\n\n").append(prevContent);
					result.addToBaseResult(buf.toString());
					buf = new StringBuffer(sResult.getSamplerData().trim()).append("\n").append(
							sResult.getRequestHeaders()).append("\n\n").append(currentContent);
					result.addToSecondaryResult(buf.toString());
					result.setFailureMessage("Responses differ in content");
					break;
				}
				prevResult = sResult;
				prevContent = currentContent;
			}
		}
	}

	private String filterString(String content) {
		if (stringsToSkip == null || stringsToSkip.size() == 0) {
			return content;
		} else {
			for (SubstitutionElement regex : stringsToSkip) {
				log.info("replacing regex: " + regex);
				emptySub.setSubstitution(regex.getSubstitute());
				content = Util.substitute(JMeterUtils.getMatcher(), JMeterUtils.getPatternCache().getPattern(regex.getRegex()),
						emptySub, content, Util.SUBSTITUTE_ALL);
			}
		}
		return content;
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
	 * @param compareContent
	 *            The compareContent to set.
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
	 * @param compareTime
	 *            The compareTime to set.
	 */
	public void setCompareTime(long compareTime) {
		this.compareTime = compareTime;
	}

	/**
	 * @return Returns the stringsToSkip.
	 */
	public Collection getStringsToSkip() {
		return stringsToSkip;
	}

	/**
	 * @param stringsToSkip
	 *            The stringsToSkip to set.
	 */
	public void setStringsToSkip(Collection stringsToSkip) {
		this.stringsToSkip = (Collection<SubstitutionElement>) stringsToSkip;
	}

}
