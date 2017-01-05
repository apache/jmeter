package org.apache.jmeter.timers;

import java.util.concurrent.TimeUnit;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.junit.Before;
import org.junit.Test;

public class TimerServiceTest {

    public static final long NO_DURATION = 0;
    public static final long SHORT_DURATION = TimeUnit.MINUTES.toMillis(5);
    public static final long LONG_DURATION = TimeUnit.HOURS.toMillis(1);

    JMeterThread thread;

    @Before
    public void initialize() {
        HashTree tree = new HashTree();
        Controller controller = new GenericController();
        tree.add(controller);
        thread = new JMeterThread(tree, null, null);
        JMeterContextService.getContext().setThread(thread);

    }

    @Test
    public void checkDelayBeforeEndTime() {
        thread.setEndTime(System.currentTimeMillis() + LONG_DURATION);
        TimerService.checkDelay(SHORT_DURATION);
    }

    @Test
    public void checkDelayWithoutEndTime() {
        thread.setEndTime(NO_DURATION);
        TimerService.checkDelay(LONG_DURATION);
    }

    @Test(expected = JMeterStopThreadException.class)
    public void chekDelayAfterEndTime() {
        thread.setEndTime(System.currentTimeMillis() + SHORT_DURATION);
        TimerService.checkDelay(LONG_DURATION);
    }

}
