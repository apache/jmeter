#! /bin/sh

##   Licensed to the Apache Software Foundation (ASF) under one or more
##   contributor license agreements.  See the NOTICE file distributed with
##   this work for additional information regarding copyright ownership.
##   The ASF licenses this file to You under the Apache License, Version 2.0
##   (the "License"); you may not use this file except in compliance with
##   the License.  You may obtain a copy of the License at
## 
##       http://www.apache.org/licenses/LICENSE-2.0
## 
##   Unless required by applicable law or agreed to in writing, software
##   distributed under the License is distributed on an "AS IS" BASIS,
##   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##   See the License for the specific language governing permissions and
##   limitations under the License.

## Basic JMeter startup script for Un*x systems
## See the "jmeter" script for details of options that can be used for Sun JVMs

##   ==============================================
##   Environment variables:
##   JVM_ARGS - optional java args, e.g. -Dprop=val
##
##   e.g.
##   JVM_ARGS="-Xms512m -Xmx512m" jmeter.sh etc.
##
##   ==============================================

# Add Mac-specific property - should be ignored elsewhere (Bug 47064)
java $JVM_ARGS -Dapple.laf.useScreenMenuBar=true -jar `dirname $0`/ApacheJMeter.jar "$@"
