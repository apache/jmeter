package org.apache.jmeter.reporters;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
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
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");
	String address;
	String from;
	String smtpHost;
	String failSubject;
	String successSubject;
	long failureCount = 0;
	long successCount = 0;
	long failureLimit = 2;
	long successLimit = 2;
	boolean failureMsgSent = false;
	boolean siteDown = false;
	boolean successMsgSent = false;

	public MailerResultCollector()
	{
		super();
		from = JMeterUtils.getPropDefault("mailer.from","");
		address = JMeterUtils.getPropDefault("mailer.addressies","");
		smtpHost = JMeterUtils.getPropDefault("mailer.smtphost","");
		failSubject = JMeterUtils.getPropDefault("mailer.failsubject","");
		successSubject = JMeterUtils.getPropDefault("mailer.successsubject","");
		try
		{
			failureLimit =
					Long.parseLong(JMeterUtils.getPropDefault("mailer.failurelimit","2"));
		}catch (NumberFormatException ex){}
		try
		{
			successLimit =
					Long.parseLong(JMeterUtils.getPropDefault("mailer.successlimit","2"));
		}catch (NumberFormatException ex){}
	}

		/**
	 *  Gets the GuiClass attribute of the ResultCollector object
	 *
	 *@return    The GuiClass value
	 */
	public Class getGuiClass()
	{
		return org.apache.jmeter.visualizers.MailerVisualizer.class;
	}

		/**
	 *  Gets the ClassLabel attribute of the ResultCollector object
	 *
	 *@return    The ClassLabel value
	 */
	public String getClassLabel()
	{
                return JMeterUtils.getResString("email_results_title");
	}

	public long getFailureLimit()
	{
		return failureLimit;
	}

	public long getFailureCount()
	{
		return failureCount;
	}

	public long getSuccessLimit()
	{
		return successLimit;
	}

	public void setFailureLimit(long limit)
	{
		failureLimit = limit;
	}

	public void setSuccessLimit(long limit)
	{
		successLimit = limit;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getAddress()
	{
		return address;
	}

	public void setFrom(String from)
	{
		this.from = from;
	}

	public String getFrom()
	{
		return from;
	}

	public void setSmtpHost(String host)
	{
		smtpHost = host;
	}

	public String getSmtpHost()
	{
		return smtpHost;
	}

	public void setFailSubject(String subject)
	{
		failSubject = subject;
	}

	public String getFailSubject()
	{
		return failSubject;
	}

	public void setSuccessSubject(String subject)
	{
		successSubject = subject;
	}

	public String getSuccessSubject()
	{
		return successSubject;
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public synchronized boolean isFailing()
	{
		return (failureCount > failureLimit);
	}

	public void sendTestMessage()
	{
		log.debug("### Test To:  " + this.address + ", " +
						"Via:  " + this.smtpHost + ", " +
						"Fail Subject:  " + this.failSubject + ", " +
						"Success Subject:  " + this.successSubject);
				String testMessage = ("### Test To:  " + this.address + ", " +
						"Via:  " + this.smtpHost + ", " +
						"Fail Subject:  " + this.failSubject + ", " +
						"Success Subject:  " + this.successSubject);

				Vector addressVector = newAddressVector(address);
				sendMail(from, addressVector, "Testing addressies", testMessage, smtpHost);
	}

		/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  sample  !ToDo (Parameter description)
	 ***********************************************************/
	public synchronized void sampleOccurred(SampleEvent event)
	{
		super.sampleOccurred(event);
		SampleResult sample = event.getResult();

		// -1 is the code for a failed sample.
		//
		if (!sample.isSuccessful())
		{
			failureCount++;
		}
		else
		{
			successCount++;
		}

		if (this.isFailing() && !siteDown && !failureMsgSent)
		{

			// Send the mail ...
			Vector addressVector = newAddressVector(address);
			sendMail(from, addressVector, failSubject, "URL Failed: " +
					sample.getSampleLabel(), smtpHost);
			siteDown = true;
			failureMsgSent = true;
			successCount = 0;
		}

		if (siteDown && (sample.getTime() != -1) & !successMsgSent)
		{
			// Send the mail ...
			if (successCount > successLimit)
			{
				Vector addressVector = newAddressVector(address);
				sendMail(from, addressVector, successSubject, "URL Restarted: " +
						sample.getSampleLabel(), smtpHost);
				siteDown = false;
				successMsgSent = true;
			}
		}

		if (successMsgSent && failureMsgSent)
		{
			clear();
		}
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  from      !ToDo (Parameter description)
	 *@param  vEmails   !ToDo (Parameter description)
	 *@param  subject   !ToDo (Parameter description)
	 *@param  attText   !ToDo (Parameter description)
	 *@param  SMTPHost  !ToDo (Parameter description)
	 ***********************************************************/
	public static synchronized void sendMail(String from,
			Vector vEmails,
			String subject,
			String attText,
			String SMTPHost)
	{
		try
		{
			String host = SMTPHost;
			boolean debug = Boolean.valueOf(host).booleanValue();
			InetAddress remote = InetAddress.getByName(host);

			InternetAddress[] address = new InternetAddress[vEmails.size()];
			for (int k = 0; k < vEmails.size(); k++)
			{
				address[k] = new InternetAddress(vEmails.elementAt(k).toString());
			}

			// create some properties and get the default Session
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			Session session = Session.getDefaultInstance(props, null);
			session.setDebug(debug);

			// create a message
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(subject);
			msg.setText(attText);
			Transport.send(msg);
			log.info("Mail sent successfully!!");
		}
		catch (UnknownHostException e1)
		{
			log.fatalError("NxError:Invalid Mail Server ", e1);
			System.exit(1);
		}
		catch (Exception e)
		{
			log.fatalError("",e);
			System.exit(1);
		}
	}

		/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  theAddressie  !ToDo (Parameter description)
	 *@return               !ToDo (Return description)
	 ***********************************************************/
	public synchronized Vector newAddressVector(String theAddressie)
	{
		Vector addressVector = new Vector();
		String addressSep = ", ";

		StringTokenizer next = new StringTokenizer(theAddressie, addressSep);

		while (next.hasMoreTokens())
		{
			String theToken = next.nextToken();

			if (theToken.indexOf("@") > 0)
			{
				addressVector.addElement(theToken);
			}
		}

		return addressVector;
	}
}
