package org.apache.jmeter.gui.action;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.swing.HtmlPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Logger;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class Help implements Command
{
	transient private static Logger log =
		LoggingManager.getLoggerFor(JMeterUtils.GUI);
	private static Set commands = new HashSet();
	public final static String HELP = "help";
	private static String helpPage =
		"file:///"
			+ JMeterUtils.getJMeterHome()
			+ "/docs/usermanual/component_reference.html";
	private static JDialog helpWindow;
	private static HtmlPane helpDoc;
	private static JScrollPane scroller;
	private static String currentPage;
	static {
		commands.add(HELP);
		helpDoc = new HtmlPane();
		scroller = new JScrollPane(helpDoc);
		helpDoc.setEditable(false);
		try
		{
			helpDoc.setPage(helpPage);
			currentPage = helpPage;
		}
		catch (IOException err)
		{
			log.error("Couldn't load " + helpPage, err);
		}
	}
	/**
	 * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e)
	{
		if (helpWindow == null)
		{
			helpWindow =
				new JDialog(
					GuiPackage.getInstance().getMainFrame(),
					JMeterUtils.getResString("help"),
					false);
			helpWindow.getContentPane().setLayout(new GridLayout(1, 1));
			ComponentUtil.centerComponentInWindow(helpWindow, 60);
		}
		if(e.getSource() instanceof String[])
		{
			String[] source = (String[])e.getSource();
			resetPage(source[0]);
			helpDoc.scrollToReference(source[1]);
		}
		else
		{
			resetPage(helpPage);
			helpDoc.scrollToReference(GuiPackage
				.getInstance()
				.getTreeListener()
				.getCurrentNode()
				.getStaticLabel().replace(' ','_'));
		}		
		helpWindow.getContentPane().removeAll();
		helpWindow.getContentPane().add(scroller);
		helpWindow.show();
	}
	private void resetPage(String source)
	{
		if(!currentPage.equals(source))
		{
			try
			{
				helpDoc.setPage(source);
				currentPage = source;
			}
			catch (IOException err)
			{
				log.error("Couldn't load page: "+source,err);
			}				
		}
	}
	/**
	 * @see org.apache.jmeter.gui.action.Command#getActionNames()
	 */
	public Set getActionNames()
	{
		return commands;
	}
}
