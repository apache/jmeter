package org.apache.jmeter.testelement;

import static org.junit.Assert.*;

import org.apache.jmeter.testelement.property.DoubleProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.NumberProperty;
import org.junit.Test;

public class TestNumberProperty {

    @Test
    public void testDZeroCompareToDZero() {
        NumberProperty n1 = new DoubleProperty("n1", 0.0);
        NumberProperty n2 = new DoubleProperty("n2", 0.0);
        assertTrue(n1.compareTo(n2) == 0);
    }

    @Test
    public void testIZeroCompareToDZero() {
        NumberProperty n1 = new IntegerProperty("n1", 0);
        NumberProperty n2 = new DoubleProperty("n2", 0.0);
        assertTrue(n1.compareTo(n2) == 0);
    }

    @Test
    public void testCompareToPositive() {
        NumberProperty n1 = new DoubleProperty("n1", 1.0);
        NumberProperty n2 = new DoubleProperty("n2", 0.0);
        assertTrue(n1.compareTo(n2) > 0);
    }

    @Test
    public void testCompareToNegative() {
        NumberProperty n1 = new DoubleProperty("n1", -1.0);
        NumberProperty n2 = new DoubleProperty("n2", 0.0);
        assertTrue(n1.compareTo(n2) < 0);
    }

    @Test
    public void testCompareToMinMax() {
        NumberProperty n1 = new DoubleProperty("n1", Double.MIN_VALUE);
        NumberProperty n2 = new DoubleProperty("n2", Double.MAX_VALUE);
        assertTrue(n1.compareTo(n2) < 0);
    }

}
