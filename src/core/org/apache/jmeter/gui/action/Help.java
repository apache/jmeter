package org.apache.jmeter.gui.action;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.swing.HtmlPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class Help implements Command
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    public final static String HELP = "help";
    private static Set commands = new HashSet();

    public static final String HELP_DOCS =
        "file:///"
            + JMeterUtils.getJMeterHome()
            + "/printable_docs/usermanual/";

	public static final String HELP_PAGE =
		HELP_DOCS + "component_reference.html";

	public static final String HELP_FUNCTIONS =
		HELP_DOCS + "functions.html";

    private static JDialog helpWindow;
    private static HtmlPane helpDoc;
    private static JScrollPane scroller;
    private static String currentPage;

    static
    {
        commands.add(HELP);
        helpDoc = new HtmlPane();
        scroller = new JScrollPane(helpDoc);
        helpDoc.setEditable(false);
        try
        {
            helpDoc.setPage(HELP_PAGE);
            currentPage = HELP_PAGE;
        }
        catch (IOException err)
        {
        	String msg = "Couldn't load help file " + err.toString();
            log.error(msg);
            helpDoc.setText(msg);
            currentPage="";// Avoid NPE in resetPage()
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
			        new Frame(),// independent frame to allow it to be overlaid by the main frame
                    JMeterUtils.getResString("help"),//$NON-NLS-1$
                    false);
            helpWindow.getContentPane().setLayout(new GridLayout(1, 1));
            ComponentUtil.centerComponentInWindow(helpWindow, 60);
        }
        helpWindow.getContentPane().removeAll();
        helpWindow.getContentPane().add(scroller);
        helpWindow.show();
        if (e.getSource() instanceof String[])
        {
            String[] source = (String[]) e.getSource();
            resetPage(source[0]);
            helpDoc.scrollToReference(source[1]);
        }
        else
        {
            resetPage(HELP_PAGE);
            helpDoc.scrollToReference(
                GuiPackage
                    .getInstance()
                    .getTreeListener()
                    .getCurrentNode()
                    .getStaticLabel()
                    .replace(' ', '_'));
        }
    }

    private void resetPage(String source)
    {
        if (!currentPage.equals(source))
        {
        	if (currentPage.length()==0){
        		helpDoc = new HtmlPane();// setText seems to mangle the old one
        		scroller.setViewportView(helpDoc);
				helpDoc.setEditable(false);
				//TODO: still does not recover completely, but is usable
				// and unlikely to be needed now that HELP_PAGE is shared string
        	}
            try
            {
                helpDoc.setPage(source);
                currentPage = source;
            }
            catch (IOException err)
            {
				String msg = "Couldn't load page " + source + " " + err.toString();
                log.error(msg);
                helpDoc.setText(msg);
                currentPage="";
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
