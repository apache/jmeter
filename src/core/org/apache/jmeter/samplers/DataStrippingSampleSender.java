package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The standard remote sample reporting should be more friendly to the main purpose of
 * remote testing - which is scalability.  To increase scalability, this class strips out the 
 * response data before sending.
 * 
 *
 */
public class DataStrippingSampleSender implements SampleSender, Serializable {
	private static final long serialVersionUID = 1;
	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private final RemoteSampleListener listener;
	private final SampleSender decoratedSender;

	/**
	 * @deprecated only for use by test code
	 */
    @Deprecated
    public DataStrippingSampleSender(){
        log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
        listener = null;
        decoratedSender = null;
    }

	DataStrippingSampleSender(RemoteSampleListener listener) {
		this.listener = listener;
		decoratedSender = null;
	}
	
	DataStrippingSampleSender(SampleSender decorate)
	{
		this.decoratedSender = decorate;
        this.listener = null;
	}

	public void testEnded() {
		if(decoratedSender != null) decoratedSender.testEnded();
	}

	public void testEnded(String host) {
		if(decoratedSender != null) decoratedSender.testEnded(host);
	}

	public void sampleOccurred(SampleEvent event) {
		//Strip the response data before writing, but only for a successful request.
	    SampleResult result = event.getResult();
		if(result.isSuccessful()) {
		    result.setResponseData(new byte[0]);
		}
		if(decoratedSender == null)
		{
			try {
				listener.sampleOccurred(event);
			} catch (RemoteException e) {
				log.error("Error sending sample result over network ",e);
			}
		}
		else
		{
			decoratedSender.sampleOccurred(event);
		}
	}

}
