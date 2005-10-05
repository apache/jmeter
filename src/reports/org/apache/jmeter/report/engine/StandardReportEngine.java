/*
 * Created on Oct 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.jmeter.report.engine;

import java.io.Serializable;

import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jorphan.collections.HashTree;

/**
 * @author pete
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StandardReportEngine implements Runnable, Serializable,
		ReportEngine {

	/**
	 * 
	 */
	public StandardReportEngine() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.report.engine.ReportEngine#configure(org.apache.jorphan.collections.HashTree)
	 */
	public void configure(HashTree testPlan) {
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.report.engine.ReportEngine#runReport()
	 */
	public void runReport() throws JMeterEngineException {
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.report.engine.ReportEngine#stopReport()
	 */
	public void stopReport() {
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.report.engine.ReportEngine#reset()
	 */
	public void reset() {
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.report.engine.ReportEngine#exit()
	 */
	public void exit() {
	}

}
