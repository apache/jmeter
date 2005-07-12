// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.save;

import java.io.IOException;
import java.util.Collection;

import org.apache.jmeter.samplers.SampleResult;

/**
 * TODO - does not appear to be used ...
 * 
 * @version $Revision$
 */
public interface DataSource {
	/** Content mask indicating the basic data points (label, time, success). */
	public final static int BASE_INFO_MASK = 1;

	/**
	 * Content mask indicating various miscellenous data (thread_name, timstamp,
	 * response code, response message, data type).
	 */
	public final static int EXTRA_INFO_MASK = 1 << 1;

	/**
	 * Content mask indicating that sub results should be included. The level of
	 * detail of the sub results will match that chosen for the main result.
	 */
	public final static int SUB_RESULTS_MASK = 1 << 2;

	/** Content mask indicating that response data should be recorded. */
	public final static int RESPONSE_MASK = 1 << 3;

	/** Content mask indicating that request data should be recorded. */
	public final static int REQUEST_DATA_MASK = 1 << 4;

	/** Content mask indicating that assertion messages should be recorded. */
	public final static int ASSERTION_RESULTS_MASK = 1 << 5;

	public final static int APPEND = 1;

	public final static int OVERWRITE = 2;

	/**
	 * Opens a file for recording sample results.
	 * 
	 * @param mode
	 *            indicates whether the file is opened for appending data to the
	 *            end of the file or overwriting the file contents.
	 * @param contentMask
	 *            mask defining what data is recorded. This is a combination of
	 *            one or more of the content mask constants defined in this
	 *            class (combined with bitwise 'or').
	 */
	public void openSource(int mode, int contentMask) throws IOException;

	/**
	 * Closes a file that had been opened for recording.
	 */
	public void closeSource() throws IOException;

	/**
	 * Load a file of previously recorded sample results and return them all in
	 * a collection.
	 */
	public Collection loadLog() throws IOException;

	/**
	 * Load a number of samples from the data source, starting from the next
	 * sample.
	 */
	public Collection loadLog(int length) throws IOException;

	/**
	 * Save a SampleResult object to the specified file. The file must have been
	 * initialized with a (link beginRecording(String,int,int,int)) call.
	 */
	public void recordSample(SampleResult result) throws IOException;
}
