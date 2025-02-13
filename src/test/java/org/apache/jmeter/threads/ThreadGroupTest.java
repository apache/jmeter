package org.apache.jmeter.threads;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ThreadGroupTest {

    @Test
    public void testThreadGroupCreation() {
        ThreadGroup tg = new ThreadGroup();
        assertNotNull(tg, "ThreadGroup should be created successfully");
    }

    @Test
    public void testSchedulerDefaults() {
        ThreadGroup tg = new ThreadGroup();
        assertFalse(tg.getScheduler(), "Scheduler should be false by default");
    }
}

