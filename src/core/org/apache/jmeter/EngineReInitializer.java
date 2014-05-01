package org.apache.jmeter;

import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Created by dzmitrykashlach on 4/30/14.
 */
public class EngineReInitializer extends Thread {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final String engineStr;
    private final String RMI_RETRIES_NUMBER = "rmi.retries_number";
    private final String RMI_RETRIES_DELAY = "rmi.retries_delay";
    private JMeterEngine engine = null;
    private HashTree tree;


    public EngineReInitializer(String engineStr, HashTree tree) {
        this.engineStr = engineStr;
        this.tree = tree;
    }

    @Override
    public void run() {
        int i = 1;
        int attemptsNumber = JMeterUtils.getPropDefault(RMI_RETRIES_NUMBER, 0);
        int retries_delay = JMeterUtils.getPropDefault(RMI_RETRIES_DELAY, 0);
        while (i <= attemptsNumber & engine == null) {
            try {
                sleep(retries_delay);
                log.debug(String.valueOf(i) + " retry to connect to " + engineStr + "...");
                System.err.println(String.valueOf(i) + " retry to connect to " + engineStr + "...");
                i++;
                this.engine = new ClientJMeterEngine(this.engineStr);
            } catch (Exception e) {
                log.fatalError("Failed to re-connect to remote host: " + this.engineStr, e);
                System.err.println("Failed to re-connect to remote host: " + this.engineStr + " " + e);
            }
        }


        if (engine != null) {
            engine.configure(this.tree);
        } else {
            log.debug("Failed to re-initialize " + engineStr);
        }
        return;

    }

    public JMeterEngine getEngine() {
        return engine;
    }
}