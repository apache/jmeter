package org.apache.jmeter.save;


import java.io.IOException;
import java.util.Collection;

import org.apache.jmeter.samplers.SampleResult;


/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public interface DataSource
{
    public final static int BASE_INFO_MASK = 1;
    public final static int EXTRA_INFO_MASK = 1 << 1;
    public final static int SUB_RESULTS_MASK = 1 << 2;
    public final static int RESPONSE_MASK = 1 << 3;
    public final static int REQUEST_DATA_MASK = 1 << 4;
    public final static int ASSERTION_RESULTS_MASK = 1 << 5;

    public final static int APPEND = 1;
    public final static int OVERWRITE = 2;

    /**
     * Opens a file for recording sample results.
     * @param filename The name of the file to record to.  Any attempt to open a file that's
     * already been opened will result in an exception
     * @param mode Mode indicates whether the file is opened for appending data to the
     * end of the file or overwriting the file contents.
     * @param contentMask - A mask defining what data is recorded.  The options are:<br>
     * BASE_INFO_MASK = all the basic data points (label, time, success)<br>
     * EXTRA_INFO_MASK = Various miscellaneous data (thread_name, timestamp, response code,
     * response message, data type)<br>
     * SUB_RESULTS_MASK = Whether to include sub results in the recording.  The level of detail 
     * of the sub results will match that chosen for the main result<br>
     * RESPONSE_MASK = Whether to store the response data<br>
     * REQUEST_DATA_MASK = Records the request data<br>
     * ASSERTION_RESULTS_MASK = Record the messages from assertions
     * 	
     */
    public void openSource(int mode, int contentMask) throws IOException;

    /**
     * Closes a file that had been opened for recording.  
     * @param filename Name of file to close.
     */
    public void closeSource() throws IOException;

    /**
     * Load a file of previously recorded sample results and return them all in a collection.
     * @return Collection
     * @throws JMeterSaveException
     */
    public Collection loadLog() throws IOException;

    /**
     * Load a number of samples from the data source, starting from the next sample.
     * @param length
     * @return Collection
     * @throws IOException
     */
    public Collection loadLog(int length) throws IOException;

    /**
     * Save a SampleResult object to the specified file.  The file must have been initialized
     * with a {@link beginRecording(String,int,int,int)} call.
     * @param filename
     * @param result
     * @throws JMeterSaveException
     */
    public void recordSample(SampleResult result) throws IOException;

}
