/*
 * Copyright 2003-2004,2006 The Apache Software Foundation.
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

package org.apache.jmeter.functions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.log.Logger;

/**
 * StringFromFile Function to read a String from a text file.
 * 
 * Parameters: 
 * - file name 
 * - variable name (optional - defaults to StringFromFile_)
 * 
 * Returns: 
 * - the next line from the file
 * - or **ERR** if an error occurs
 * - value is also saved in the variable for later re-use.
 * 
 * Ensure that different variable names are used for each call to the function
 * 
 * 
 * Notes: 
 * - JMeter instantiates a copy of each function for every reference in a
 * Sampler or elsewhere; each instance will open its own copy of the the file
 * - the file name is resolved at file (re-)open time
 * - the output variable name is resolved every time the function is invoked
 * 
 */
public class StringFromFile extends AbstractFunction implements Serializable, TestListener {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private static final List desc = new LinkedList();

	private static final String KEY = "__StringFromFile";//$NON-NLS-1$

	// Function name (only 1 _)

	static final String ERR_IND = "**ERR**";//$NON-NLS-1$

	static {
		desc.add(JMeterUtils.getResString("string_from_file_file_name"));//$NON-NLS-1$
		desc.add(JMeterUtils.getResString("function_name_param"));//$NON-NLS-1$
		desc.add(JMeterUtils.getResString("string_from_file_seq_start"));//$NON-NLS-1$
		desc.add(JMeterUtils.getResString("string_from_file_seq_final"));//$NON-NLS-1$
	}

	private static final int MIN_PARAM_COUNT = 1;

	private static final int PARAM_NAME = 2;

	private static final int PARAM_START = 3;

	private static final int PARAM_END = 4;

	private static final int MAX_PARAM_COUNT = 4;

	transient private String myValue = ERR_IND;

	transient private String myName = "StringFromFile_";//$NON-NLS-1$ - Name to store the value in

	transient private Object[] values;

	transient private BufferedReader myBread = null; // Buffered reader

	transient private FileReader fis; // keep this round to close it

	transient private boolean firstTime = false; // should we try to open the
													// file?

	transient private String fileName; // needed for error messages

	public StringFromFile() {
		if (log.isDebugEnabled()) {
			log.debug("++++++++ Construct " + this);
		}
	}

	public Object clone() {
		StringFromFile newReader = new StringFromFile();
		if (log.isDebugEnabled()) { // Skip expensive parameter creation ..
			log.debug(this + "::StringFromFile.clone()", new Throwable("debug"));//$NON-NLS-1$
		}

		return newReader;
	}

	/*
	 * Warning: the file will generally be left open at the end of a test run.
	 * This is because functions don't (yet) have any way to find out when a
	 * test has ended ...
	 */
	private void closeFile() {
		if (myBread == null)
			return;
		String tn = Thread.currentThread().getName();
		log.info(tn + " closing file " + fileName);//$NON-NLS-1$
		try {
			myBread.close();
			fis.close();
		} catch (IOException e) {
			log.error("closeFile() error: " + e.toString());//$NON-NLS-1$
		}
	}

	private static final int COUNT_UNUSED = -2;

	private int myStart = COUNT_UNUSED;

	private int myCurrent = COUNT_UNUSED;

	private int myEnd = COUNT_UNUSED;

	private void openFile() {
		String tn = Thread.currentThread().getName();
		fileName = ((CompoundVariable) values[0]).execute();

		String start = "";
		if (values.length >= PARAM_START) {
			start = ((CompoundVariable) values[PARAM_START - 1]).execute();
			try {
				myStart = Integer.valueOf(start).intValue();
			} catch (NumberFormatException e) {
				myStart = COUNT_UNUSED;// Don't process invalid numbers
			}
		}
		// Have we used myCurrent yet?
		// Set to 1 if start number is missing (to allow for end without start)
		if (myCurrent == COUNT_UNUSED)
			myCurrent = myStart == COUNT_UNUSED ? 1 : myStart;

		if (values.length >= PARAM_END) {
			String tmp = ((CompoundVariable) values[PARAM_END - 1]).execute();
			try {
				myEnd = Integer.valueOf(tmp).intValue();
			} catch (NumberFormatException e) {
				myEnd = COUNT_UNUSED;// Don't process invalid numbers
										// (including "")
			}

		}

		if (values.length >= PARAM_START) {
			log.info(tn + " Start = " + myStart + " Current = " + myCurrent + " End = " + myEnd);//$NON-NLS-1$
			if (myEnd != COUNT_UNUSED) {
				if (myCurrent > myEnd) {
					log.info(tn + " No more files to process, " + myCurrent + " > " + myEnd);//$NON-NLS-1$
					myBread = null;
					return;
				}
			}
			/*
			 * DecimalFormat adds the number to the end of the format if there
			 * are no formatting characters, so we need a way to prevent this
			 * from messing up the file name.
			 * 
			 */
			if (myStart != COUNT_UNUSED) // Only try to format if there is a
											// number
			{
				log.info(tn + " using format " + fileName);
				try {
					DecimalFormat myFormatter = new DecimalFormat(fileName);
					fileName = myFormatter.format(myCurrent);
				} catch (NumberFormatException e) {
					log.warn("Bad file name format ", e);
				}
			}
			myCurrent++;// for next time
		}

		log.info(tn + " opening file " + fileName);//$NON-NLS-1$
		try {
			fis = new FileReader(fileName);
			myBread = new BufferedReader(fis);
		} catch (Exception e) {
			log.error("openFile() error: " + e.toString());//$NON-NLS-1$
			myBread = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
	 */
	public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
			throws InvalidVariableException {

		JMeterVariables vars = getVariables();

		if (values.length >= PARAM_NAME) {
			myName = ((CompoundVariable) values[PARAM_NAME - 1]).execute();
		}

		myValue = ERR_IND;

		/*
		 * To avoid re-opening the file repeatedly after an error, only try to
		 * open it in the first execute() call (It may be re=opened at EOF, but
		 * that will cause at most one failure.)
		 */
		if (firstTime) {
			openFile();
			firstTime = false;
		}

		if (null != myBread) { // Did we open the file?
			try {
				String line = myBread.readLine();
				if (line == null) { // EOF, re-open file
					String tn = Thread.currentThread().getName();
					log.info(tn + " EOF on  file " + fileName);//$NON-NLS-1$
					closeFile();
					openFile();
					if (myBread != null) {
						line = myBread.readLine();
					} else {
						line = ERR_IND;
						if (myEnd != COUNT_UNUSED) {// Are we processing a file
													// sequence?
							log.info(tn + " Detected end of sequence.");
							throw new JMeterStopThreadException("End of sequence");
						}
					}
				}
				myValue = line;
			} catch (IOException e) {
				String tn = Thread.currentThread().getName();
				log.error(tn + " error reading file " + e.toString());//$NON-NLS-1$
			}
		} else { // File was not opened successfully
			if (myEnd != COUNT_UNUSED) {// Are we processing a file sequence?
				String tn = Thread.currentThread().getName();
				log.info(tn + " Detected end of sequence.");
				throw new JMeterStopThreadException("End of sequence");
			}
		}

		if (myName.length() > 0) {
			vars.put(myName, myValue);
		}

		if (log.isDebugEnabled()) {
			String tn = Thread.currentThread().getName();
			log.debug(tn + " name:" //$NON-NLS-1$ 
					+ myName + " value:" + myValue);//$NON-NLS-1$
		}

		return myValue;

	}

	/*
	 * (non-Javadoc) Parameters: - file name - variable name (optional) - start
	 * index (optional) - end index or count (optional)
	 * 
	 * @see org.apache.jmeter.functions.Function#setParameters(Collection)
	 */
	public synchronized void setParameters(Collection parameters) throws InvalidVariableException {

		log.debug(this + "::StringFromFile.setParameters()");//$NON-NLS-1$

		values = parameters.toArray();

		if ((values.length > MAX_PARAM_COUNT) || (values.length < MIN_PARAM_COUNT)) {
			throw new InvalidVariableException("Wrong number of parameters");//$NON-NLS-1$
		}

		StringBuffer sb = new StringBuffer(40);
		sb.append("setParameters(");//$NON-NLS-1$
		for (int i = 0; i < values.length; i++) {
			if (i > 0)
				sb.append(",");
			sb.append(((CompoundVariable) values[i]).getRawParameters());
		}
		sb.append(")");//$NON-NLS-1$
		log.info(sb.toString());

		// N.B. setParameters is called before the test proper is started,
		// and thus variables are not interpreted at this point
		// So defer the file open until later to allow variable file names to be
		// used.
		firstTime = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#getReferenceKey()
	 */
	public String getReferenceKey() {
		return KEY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.functions.Function#getArgumentDesc()
	 */
	public List getArgumentDesc() {
		return desc;
	}

	public void testStarted() {
		//
	}

	public void testStarted(String host) {
		//
	}

	public void testEnded() {
		this.testEnded(""); //$NON-NLS-1$
	}

	public void testEnded(String host) {
		closeFile();
	}

	public void testIterationStart(LoopIterationEvent event) {
		//
	}
}