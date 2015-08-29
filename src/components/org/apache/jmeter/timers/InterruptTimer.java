package org.apache.jmeter.timers;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class InterruptTimer extends AbstractTestElement implements Timer, Serializable, LoopIterationListener {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggingManager.getLoggerForClass();

    private static final String TIMEOUT = "InterruptTimer.timeout"; //$NON-NLS-1$

    private long timeout = 0;

    private JMeterContext context;

    private ScheduledFuture<?> future;
    
    private ScheduledExecutorService tpool;
    
    private final boolean debug;

    /**
     * No-arg constructor.
     */
    public InterruptTimer() {
//        LOG.setPriority(org.apache.log.Priority.DEBUG); // for local debugging
        debug = LOG.isDebugEnabled(); // TODO is this the best place for this? 
    }

    /**
     * Set the timeout for this timer.
     * @param timeout The timeout for this timer
     */
    public void setTimeout(String timeout) {
        setProperty(TIMEOUT, timeout);
    }

    /**
     * Get the timeout value for display.
     *
     * @return the timeout value for display.
     */
    public String getTimeout() {
        return getPropertyAsString(TIMEOUT);
    }

    /**
     * Retrieve the delay to use during test execution.
     * This is called just before starting a sampler.
     * It is used to schedule future task to interrupt the sampler.
     * 
     * @return Always returns zero, because this timer does not wait
     */
    @Override
    public long delay() {
        if (debug) {
            LOG.debug(whoAmI("delay()", this));
        }
        if (future != null) {
            if (!future.isDone()) {
                boolean cancelled = future.cancel(false);
                if (debug) {
                    LOG.debug("Cancelled the task:" + future + " with result " + cancelled);
                }
            }
            future = null;
        }
        if (timeout <= 0) {
            return 0;
        }
        final Sampler samp = context.getCurrentSampler();
        if (!(samp instanceof Interruptible)) {
            // Log this?
            return 0;
        }
        final Interruptible sampler = (Interruptible) samp;
        Runnable run=new Runnable() {
            public void run() {
                  boolean interrupted = sampler.interrupt();
                  if (interrupted) {
                      LOG.warn("Interruped the sampler " + getInfo(samp));
                  } else {
                      if (debug) {
                          LOG.debug("Did not interrupt the sampler " + getInfo(samp));                          
                      }
                  }
            }
        };
        // schedule the interrupt to occur 
        if (debug) {
            LOG.debug("Scheduling timer for " + getInfo(samp));
        }
        future = tpool.schedule(run, timeout, TimeUnit.MILLISECONDS);
        return 0;
    }

    /**
     * Provide a description of this timer class.
     *
     * @return the description of this timer class.
     */
    @Override
    public String toString() {
        return JMeterUtils.getResString("interrupt_timer_memo"); //$NON-NLS-1$
    }

    /**
     * Gain access to any variables that have been defined.
     *
     * @see LoopIterationListener#iterationStart(LoopIterationEvent)
     */
    @Override
    public void iterationStart(LoopIterationEvent event) {
        if (debug) {
            LOG.debug(whoAmI("iterationStart()", this));
        }
        timeout = getPropertyAsLong(TIMEOUT);
        context = JMeterContextService.getContext(); // TODO is this called from the correct thread?
        tpool = Executors.newScheduledThreadPool(1, // ditto
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }
            });
    }

    private String whoAmI(String id, TestElement o) {
        return id + " @" + System.identityHashCode(o)+ " '"+ o.getName() + "'";         
    }

    private String getInfo(TestElement o) {
        return whoAmI(o.getClass().getSimpleName(), o); 
    }
}
