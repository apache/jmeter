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
    private static final String RMI_RETRIES_NUMBER = "rmi.retries_number";
    private static final String RMI_RETRIES_DELAY = "rmi.retries_delay";
    private static final String RMI_CONTINUE_ON_FAIL = "rmi.continue_on_fail"; // $NON-NLS-1$
    private static final boolean rmiContinueOnFail;
    private static int rmiRetriesDelay;
    private static int rmiRetriesNumber;
    private final String engineStr;
    private JMeterEngine engine = null;
    private HashTree tree;

    static {
        rmiContinueOnFail = JMeterUtils.getPropDefault(RMI_CONTINUE_ON_FAIL, true);
        rmiRetriesDelay = JMeterUtils.getPropDefault(RMI_RETRIES_DELAY, 0);
        rmiRetriesNumber = JMeterUtils.getPropDefault(RMI_RETRIES_NUMBER, 0);
    }

    public EngineReInitializer(String engineStr, HashTree tree) {
        this.engineStr = engineStr;
        this.tree = tree;
    }

    public static String getRmiRetriesNumberName() {
        return RMI_RETRIES_NUMBER;
    }

    public static String getRmiRetriesDelayName() {
        return RMI_RETRIES_DELAY;
    }

    public static String getRmiContinueOnFailName() {
        return RMI_CONTINUE_ON_FAIL;
    }

    public static boolean isRmiContinueOnFailValue() {
        return rmiContinueOnFail;
    }

    public static int getRmiRetriesDelayValue() {
        return rmiRetriesDelay;
    }

    public static int getRmiRetriesNumberValue() {
        return rmiRetriesNumber;
    }

    @Override
    public void run() {
        int i = 1;
        while (i <= rmiRetriesNumber & engine == null) {
            try {
                sleep(rmiRetriesDelay);
                log.debug(String.valueOf(i) + "/"
                        + String.valueOf(rmiRetriesNumber) + " retry to connect to " + engineStr + "...");
                System.err.println(String.valueOf(i) + "/"
                        + String.valueOf(rmiRetriesNumber) + " retry to connect to " + engineStr + "...");
                i++;
                this.engine = new ClientJMeterEngine(this.engineStr);
            } catch (Exception e) {
                log.fatalError("Failed to re-connect to remote host " + this.engineStr + ": " + String.valueOf(i - 1)
                        + " retry", e);
                System.err.println("Failed to re-connect to remote host " + this.engineStr + ": " + String.valueOf(i - 1)
                        + " retry");
            }
        }


        if (engine != null) {
            log.debug("Successfull re-connection with " + String.valueOf(i - 1) + "/"
                    + String.valueOf(rmiRetriesNumber) + " retry");
            System.err.println("Successfull re-connection with " + String.valueOf(i - 1) + "/"
                    + String.valueOf(rmiRetriesNumber) + " retry");
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

