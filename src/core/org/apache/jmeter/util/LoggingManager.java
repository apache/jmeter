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
