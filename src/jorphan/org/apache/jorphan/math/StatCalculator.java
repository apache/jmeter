// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jorphan.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 * This class serves as a way to calculate the median of a list of values.  It
 * is not threadsafe.
 */
public class StatCalculator implements Serializable
{
    List values = new ArrayList();
    double sum = 0;
    double sumOfSquares = 0;
    double mean = 0;
    double deviation = 0;
    int count = 0;
    
    public void clear()
    {
        values.clear();
        sum = 0;
        sumOfSquares = 0;
        mean = 0;
        deviation = 0;
        count = 0;
    }

    public void addValue(long newValue)
    {
        Number val = new Long(newValue);
        addValue(val);
    }

    public void addValue(int newValue)
    {
        Number val = new Integer(newValue);
        addValue(val);
    }

    public void addValue(float newValue)
    {
        Number val = new Float(newValue);
        addValue(val);
    }

    public void addValue(double newValue)
    {
        Number val = new Double(newValue);
        addValue(val);
    }

    public Number getMedian()
    {
        return (Number) values.get((int)(values.size() * .5));
    }
    
    /**
     * Get the value which %percent% of the values are less than.  This works just like 
     * median (where median represents the 50% point).  A typical desire is to see the 90%
     * point - the value that 90% of the data points are below, the remaining 10% are above.
     * @param percent
     * @return
     */
    public Number getPercentPoint(float percent)
    {
        return (Number) values.get((int)(values.size() * percent));
    }
    
    /**
     * Get the value which %percent% of the values are less than.  This works just like 
     * median (where median represents the 50% point).  A typical desire is to see the 90%
     * point - the value that 90% of the data points are below, the remaining 10% are above.
     * @param percent
     * @return
     */
    public Number getPercentPoint(double percent)
    {
        return (Number) values.get((int)(values.size() * percent));
    }
    
    public double getMean()
    {
        return mean;
    }
    
    public double getStandardDeviation()
    {
        return deviation;
    }
    
    public Number getMin()
    {
        return (Number)values.get(0);
    }
    
    public Number getMax()
    {
        return (Number)values.get(count-1);
    }
    
    public int getCount()
    {
        return count;
    }

    public void addValue(Number val)
    {
        int index = Collections.binarySearch(values, val);
        if (index >= 0 && index < values.size())
        {
            values.add(index, val);
        }
        else if (index == values.size() || values.size() == 0)
        {
            values.add(val);
        }
        else
        {
            values.add((index * (-1)) - 1, val);
        }
        count++;
        double currentVal = val.doubleValue();
        sum += currentVal;
        sumOfSquares += currentVal * currentVal;
        mean = sum / count;
        deviation = Math.sqrt( (sumOfSquares / count) - (mean * mean) );
    }
    
    public static class Test extends TestCase
    {
        StatCalculator calc;
        
        public Test(String name)
        {
            super(name);
        }
        
        public void setUp()
        {
            calc = new StatCalculator();
        }
        
        public void testCalculation()
        {
            calc.addValue(18);
            calc.addValue(10);
            calc.addValue(9);
            calc.addValue(11);
            calc.addValue(28);
            calc.addValue(3);
            calc.addValue(30);
            calc.addValue(15);
            calc.addValue(15);
            calc.addValue(21);
            assertEquals(16,(int)calc.getMean());
            assertEquals(8.0622577F,(float)calc.getStandardDeviation(),0F);
            assertEquals(30,calc.getMax().intValue());
            assertEquals(3,calc.getMin().intValue());
            assertEquals(15,calc.getMedian().intValue());
        }
    }
}
