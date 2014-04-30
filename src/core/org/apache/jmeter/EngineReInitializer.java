package org.apache.jmeter;

/**
 * Created by dzmitrykashlach on 4/30/14.
 */
public class EngineReInitializer implements Runnable {
    public final String engine;
    public int numberOfRetries;

    public EngineReInitializer(int numberOfRetries, String engine) {
        this.numberOfRetries = numberOfRetries;
        this.engine = engine;
    }

    @Override
    public void run() {
//        JMeterEngine eng = doRemoteInit(engine, tree);

    }
}
