package org.apache.jmeter.swing;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class HtmlPane extends JTextPane
{
    Logger log = LoggingManager.getLoggerForClass();
    
	public HtmlPane(){	
		this.addHyperlinkListener(
			new HyperlinkListener(){
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED){
						String ref=e.getURL().getRef();
						if (ref != null && ref.length() >0) {
                            log.warn("reference to scroll to = " + ref);
							scrollToReference(ref);
						}
					}
				}
			}
		);
	}

	
	public void scrollToReference(String reference)
	{
		super.scrollToReference(reference);
	}
}
