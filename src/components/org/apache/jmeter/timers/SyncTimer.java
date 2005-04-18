package org.apache.jmeter.timers;

import java.io.Serializable;

import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The purpose of the SyncTimer is to block threads until X number of threads have been blocked, and
 * then they are all released at once.  A SyncTimer can thus create large instant loads at various
 * points of the test plan.
 * @author mike
 *
 */
public class SyncTimer extends AbstractTestElement implements Timer,Serializable,TestBean {
    private static final long serialVersionUID = 1;
    static Logger log = LoggingManager.getLoggerForClass();
    int[] timerCounter = new int[] {0};
    Object sync = new Object();
    
    int groupSize;
    
    /**
     * @return Returns the numThreads.
     */
    public int getGroupSize() {
        return groupSize;
    }

    /**
     * @param numThreads The numThreads to set.
     */
    public void setGroupSize(int numThreads) {
        this.groupSize = numThreads;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.timers.Timer#delay()
     */
    public long delay() {
        synchronized(sync)
        {
            timerCounter[0]++;
            if((getGroupSize() == 0 && timerCounter[0] >= JMeterContextService.getNumberOfThreads())
                    || (getGroupSize() > 0 && timerCounter[0] >= getGroupSize()))
            {
                timerCounter[0] = 0;
                sync.notifyAll();
            }
            else
            {
                try {
                    sync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    /**
     * We have to controll the cloning process because we need some cross-thread communication if
     * our synctimers are to be able to determine when to block and when to release.
     */
    public Object clone() {
        SyncTimer newTimer = (SyncTimer)super.clone();
        newTimer.timerCounter = timerCounter;
        newTimer.sync = sync;
        return newTimer;
    }


}
