package org.apache.jmeter.util;

import java.io.Writer;

import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.io.WriterTarget;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class LoggingManager
{
	private static PatternFormatter format = new PatternFormatter("%{time:MM/dd/yyyy h:mm:ss a} %{priority} - %{category}: %{message}\n");
	private static WriterTarget target;
	
	public static final String JMETER = "jmeter";
	public static final String ENGINE = "jmeter.engine";
	public static final String ELEMENTS = "jmeter.elements";
	public static final String GUI = "jmeter.gui";
	public static final String UTIL = "jmeter.util";
	public static final String CLASSFINDER = "jmeter.util.classfinder";
	public static final String TEST = "jmeter.test";
	public static final String HTTP = "jmeter.protocol.http";
	public static final String JDBC = "jmeter.protocol.jdbc";
	public static final String FTP = "jmeter.protocol.ftp";
	public static final String JAVA = "jmeter.protocol.java";
	
	
	LoggingManager()
	{
	}
	
	public void setPriority(Priority p,String category)
	{
		Hierarchy.getDefaultHierarchy().getLoggerFor(category).setPriority(p);
	}
	
	public void setTarget(Writer targetFile)
	{
		if(target == null)
		{
			target = new WriterTarget(targetFile,format);
			Hierarchy.getDefaultHierarchy().setDefaultLogTarget(new WriterTarget(targetFile,format));
		}
		else
		{
			target.close();
			target = new WriterTarget(targetFile,format);
			Hierarchy.getDefaultHierarchy().setDefaultLogTarget(new WriterTarget(targetFile,format));
		}
	}
}
