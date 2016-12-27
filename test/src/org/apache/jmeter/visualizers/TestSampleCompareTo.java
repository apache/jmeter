package org.apache.jmeter.visualizers;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestSampleCompareTo {

    private final long thisCount;
    private final long otherCount;
    private final int compareResult;

    public TestSampleCompareTo(long thisCount, long otherCount,
            int compareResult) {
        this.thisCount = thisCount;
        this.otherCount = otherCount;
        this.compareResult = compareResult;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { 0L, 0L, 0 }, { 1L, 0L, 1 },
                { 0L, 1L, -1 }, { Long.MAX_VALUE, Long.MIN_VALUE, 1 },
                { Long.MIN_VALUE, Long.MAX_VALUE, -1 }, { 1000L, -1000L, 1 },
                { -1000L, 1000L, -1 }, { Long.MIN_VALUE, Long.MIN_VALUE, 0 },
                { Long.MAX_VALUE, Long.MAX_VALUE, 0 } });
    }

    @Test
    public void testCompareTo() {
        assertThat(sample(thisCount).compareTo(sample(otherCount)),
                CoreMatchers.is(compareResult));
    }

    private Sample sample(long count) {
        return new Sample("dummy", 0l, 0L, 0L, 0L, 0L, 0.0, 0L, true, count, 0L);
    }

}
