package org.apache.jmeter.reporters;

import java.io.Serializable;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * Title:        Apache JMeter
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      Apache Foundation
 * @author Michael Stover
 * @version 1.0
 */

public class MailerResultCollector extends ResultCollector implements Serializable
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");
    public static final String MAILER_MODEL = "MailerResultCollector.mailer_model";


	public MailerResultCollector()
	{
		super();
        setProperty(new TestElementProperty(MAILER_MODEL,new MailerModel()));
	}
    
    public void clear()
    {
        super.clear();
        setProperty(new TestElementProperty(MAILER_MODEL,new MailerModel()));
    }
    

    /* (non-Javadoc)
     * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
     */
    public void sampleOccurred(SampleEvent e)
    {
        // TODO Auto-generated method stub
        super.sampleOccurred(e);
        getMailerModel().add(e.getResult());
    }
    
    public MailerModel getMailerModel()
    {
        return (MailerModel)getProperty(MAILER_MODEL).getObjectValue();
    }

}
