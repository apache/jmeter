package org.apache.jmeter.threads;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GUIMenuSortOrder(2)
public class VirtualThreadGroup extends AbstractThreadGroup {
    private static final long serialVersionUID = 284L;
    private static final Logger log = LoggerFactory.getLogger(VirtualThreadGroup.class);

    public static final String RAMP_TIME = "ThreadGroup.ramp_time";

    private final ConcurrentHashMap<JMeterThread, Thread> allVirtualThreads = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private int groupNumber;

    public VirtualThreadGroup() {
        super();
    }

    public void setRampUp(int rampUp) {
        setProperty(RAMP_TIME, rampUp);
    }

    public int getRampUp() {
        return getPropertyAsInt(RAMP_TIME, 1);
    }

    @Override
    public void start(int groupNum, ListenerNotifier notifier, ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
        this.running = true;
        this.groupNumber = groupNum;

        int numThreads = getNumThreads();
        log.info("Starting VIRTUAL thread group... number={} threads={}", groupNumber, numThreads);

        JMeterVariables variables = JMeterContextService.getContext().getVariables();

        for (int threadNum = 0; running && threadNum < numThreads; threadNum++) {
            createVirtualThread(notifier, threadGroupTree, engine, threadNum, variables);
        }

        log.info("Started virtual thread group number {}", groupNumber);
    }

    private void createVirtualThread(ListenerNotifier notifier, ListedHashTree threadGroupTree,
            StandardJMeterEngine engine, int threadNum, JMeterVariables variables) {

        JMeterThread jmThread = makeThread(engine, this, notifier, groupNumber, threadNum,
                cloneTree(threadGroupTree), variables);
        String threadName = getName() + " " + groupNumber + "-" + (threadNum + 1);
        jmThread.setThreadName(threadName);

        Thread virtualThread;
        try {
            Class<?> builderClass = Class.forName("java.lang.Thread$Builder$OfVirtual");
            Object builder = Thread.class.getMethod("ofVirtual").invoke(null);
            builder = builderClass.getMethod("name", String.class).invoke(builder, threadName);
            virtualThread = (Thread) builderClass.getMethod("start", Runnable.class).invoke(builder, jmThread);
            log.debug("Created virtual thread: {}", threadName);
        } catch (Exception e) {
            log.warn("Virtual Threads not available, using platform thread for: {}", threadName);
            virtualThread = new Thread(jmThread, threadName);
            virtualThread.start();
        }

        allVirtualThreads.put(jmThread, virtualThread);
    }

    @Override
    public void threadFinished(JMeterThread thread) {
        if (log.isDebugEnabled()) {
            log.debug("Ending virtual thread {}", thread.getThreadName());
        }
        allVirtualThreads.remove(thread);
    }

    @Override
    public void tellThreadsToStop() {
        running = false;
        allVirtualThreads.forEach((jmeterThread, thread) -> {
            jmeterThread.stop();
            jmeterThread.interrupt();
            if (thread != null) {
                thread.interrupt();
            }
        });
    }

    @Override
    public void stop() {
        running = false;
        allVirtualThreads.keySet().forEach(JMeterThread::stop);
    }

    @Override
    public int numberOfActiveThreads() {
        return allVirtualThreads.size();
    }

    @Override
    public boolean verifyThreadsStopped() {
        return allVirtualThreads.values().stream().allMatch(thread -> !thread.isAlive());
    }

    @Override
    public void waitThreadsStopped() {
        allVirtualThreads.values().forEach(thread -> {
            if (thread != null && thread.isAlive()) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    public boolean stopThread(String threadName, boolean now) {
        for (var entry : allVirtualThreads.entrySet()) {
            JMeterThread jmeterThread = entry.getKey();
            if (jmeterThread.getThreadName().equals(threadName)) {
                jmeterThread.stop();
                if (now && entry.getValue() != null) {
                    entry.getValue().interrupt();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public JMeterThread addNewThread(int delay, StandardJMeterEngine engine) {
        JMeterContext context = JMeterContextService.getContext();
        int numThreads;

        synchronized (this) {
            numThreads = getNumThreads();
            setNumThreads(numThreads + 1);
        }

        JMeterVariables variables = context.getVariables();
        createVirtualThread(null, null, engine, numThreads, variables);

        JMeterThread newJmThread = allVirtualThreads.keySet().stream()
                .filter(t -> t.getThreadName().contains(String.valueOf(numThreads)))
                .findFirst()
                .orElse(null);

        JMeterContextService.addTotalThreads(1);
        log.info("Started new virtual thread in group {}", groupNumber);
        return newJmThread;
    }
}
