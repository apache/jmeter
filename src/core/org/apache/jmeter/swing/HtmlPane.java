package org.apache.jmeter.swing;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$  Updated on: $Date$
 */
public class HtmlPane extends JTextPane
{
    private static Logger log = LoggingManager.getLoggerForClass();
    
    public HtmlPane()
    {
        this.addHyperlinkListener(new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                {
                    String ref = e.getURL().getRef();
                    if (ref != null && ref.length() > 0)
                    {
                        log.debug("reference to scroll to = " + ref);
                        scrollToReference(ref);
                    }
                }
            }
        });
    }

    public void scrollToReference(String reference)
    {
        super.scrollToReference(reference);
    }
}
