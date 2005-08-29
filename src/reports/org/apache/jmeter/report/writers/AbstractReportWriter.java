//$Header:
/*
 * Copyright 2005 The Apache Software Foundation.
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
 * 
 */
package org.apache.jmeter.report.writers;

import org.apache.jmeter.testelement.TestElement;

/**
 * @author pete
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractReportWriter implements ReportWriter {

	/**
	 * 
	 */
	public AbstractReportWriter() {
		super();
		// TODO Auto-generated constructor stub
	}

    /* (non-Javadoc)
	 * @see org.apache.jmeter.report.writers.ReportWriter#writeReport(org.apache.jmeter.testelement.TestElement)
	 */
	public ReportSummary writeReport(TestElement element) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.report.writers.ReportWriter#getTargetDirectory()
	 */
	public String getTargetDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.report.writers.ReportWriter#setTargetDirectory(java.lang.String)
	 */
	public void setTargetDirectory(String directory) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
	}
}
