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

package woolfel;

public class SubDummyTest2 extends DummyTestCase {

    @SuppressWarnings("unused")
    private SubDummyTest2() {
        super();
        System.out.println("private SubDummyTest2()");
    }

    public SubDummyTest2(String arg0) {
        super(arg0);
        System.out.println("public SubDummyTest2("+arg0+")");
    }

    public void oneTimeSetUp() {
        System.out.println("SubDummyTest2#oneTimeSetUp(): "+getName());
    }

    public void oneTimeTearDown() {
        System.out.println("SubDummyTest2#oneTimeTearDown(): "+getName());
    }
}
