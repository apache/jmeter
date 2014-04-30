package org.apache.jmeter;

import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Created by dzmitrykashlach on 4/30/14.
 */
public class EngineReInitializer implements Runnable {
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final String engineStr;
    private JMeterEngine engine;
    private int numberOfRetries;
    private int delayBetweenRetries;
    private HashTree tree;


    public EngineReInitializer(int numberOfRetries, String engineStr, int delayBetweenRetries, HashTree tree) {
        this.numberOfRetries = numberOfRetries;
        this.engineStr = engineStr;
        this.delayBetweenRetries = delayBetweenRetries;
        this.tree = tree;

    }

    @Override
    public void run() {
        for (int i = 0; i < this.numberOfRetries; i++) {
            try {
                this.engine = new ClientJMeterEngine(this.engineStr);
                Thread.sleep(this.delayBetweenRetries);
            } catch (Exception e) {
                log.fatalError("Failure connecting to remote host: " + this.engineStr, e);
                System.err.println("Failure connecting to remote host: " + this.engineStr + " " + e);
                return;
            }
            engine.configure(this.tree);
        }
    }

    public JMeterEngine getEngine() {
        return engine;
    }
}