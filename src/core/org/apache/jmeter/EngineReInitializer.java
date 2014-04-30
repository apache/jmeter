package org.apache.jmeter;

import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Created by dzmitrykashlach on 4/30/14.
 */
public class EngineReInitializer extends Thread {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final String engineStr;
    private JMeterEngine engine = null;
    private HashTree tree;


    public EngineReInitializer(String engineStr, HashTree tree) {
        this.engineStr = engineStr;
        this.tree = tree;
    }

    @Override
    public void run() {
            try {
                this.engine = new ClientJMeterEngine(this.engineStr);
            } catch (Exception e) {
                log.fatalError("Failure connecting to remote host: " + this.engineStr, e);
                System.err.println("Failure connecting to remote host: " + this.engineStr + " " + e);
                return;
            }
            engine.configure(this.tree);
        }

    public JMeterEngine getEngine() {
        return engine;
    }
}