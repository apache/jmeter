package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenLinkAction extends AbstractAction {
    
    private static final Logger log = LoggerFactory.getLogger(OpenLinkAction.class);

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.LINK_BUG_TRACKER);
        commands.add(ActionNames.LINK_NIGHTLY_BUILD);
    }
    
    
    /**
     * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
     */
    @Override
    public void doAction(ActionEvent e) {
        String url = null;
        if (e.getActionCommand().equals(ActionNames.LINK_BUG_TRACKER)) {
            url = "https://jmeter.apache.org/issues.html";
        } else if (e.getActionCommand().equals(ActionNames.LINK_NIGHTLY_BUILD)) {
            url = "https://jmeter.apache.org/nightly.html";
        }
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException err) {
            log.error("OpenLinkAction: User default browser is not found, or it fails to be launched, or the default handler application failed to be launched on {}", err);
        } catch (UnsupportedOperationException err) {
            log.error("OpenLinkAction: Current platform does not support the Desktop.Action.BROWSE actionon {}", err);
        } catch (SecurityException err) {
            log.error("OpenLinkAction: Security problem on {}", err);
        } catch (Exception err) {
            log.error("OpenLinkAction on {}", err);
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

}
