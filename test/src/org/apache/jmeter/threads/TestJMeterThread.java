/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.jmeter.threads;

import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.collections.HashTree;
import org.junit.Test;

/**
 * Tests for {@link JMeterThread}
 */
public class TestJMeterThread {
    private static class ThrowingThreadListener implements ThreadListener {

        private boolean throwError;

        public ThrowingThreadListener(boolean throwError) {
            this.throwError = throwError;
        }

        @Override
        public void threadStarted() {
            if(throwError) {
                throw new NoClassDefFoundError("Throw for Bug TestJMeterThread");
            } else {
                throw new RuntimeException("Throw for Bug TestJMeterThread");
            }
        }

        @Override
        public void threadFinished() {
            if(throwError) {
                throw new NoClassDefFoundError("Throw for Bug TestJMeterThread");
            } else {
                throw new RuntimeException("Throw for Bug TestJMeterThread");
            }
        }   
    }
    
    @Test(expected=NoClassDefFoundError.class)
    public void testBug61661OnError(){
        HashTree hashTree =new HashTree();
        hashTree.add("Test", new ThrowingThreadListener(true));
        JMeterThread.ThreadListenerTraverser traverser = 
                new JMeterThread.ThreadListenerTraverser(true);
        hashTree.traverse(traverser);
    }
    
    
    @Test
    public void testBug61661OnException(){
        HashTree hashTree =new HashTree();
        hashTree.add("Test", new ThrowingThreadListener(false));
        JMeterThread.ThreadListenerTraverser traverser = 
                new JMeterThread.ThreadListenerTraverser(true);
        hashTree.traverse(traverser);
    }
    
}
