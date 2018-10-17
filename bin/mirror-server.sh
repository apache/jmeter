#!/bin/sh

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

#   Run the JMeter mirror server in non-GUI mode
#   P1 = port to use (default 8080)

cd "$(dirname "$0")" || exit 1

CP=../lib/ext/ApacheJMeter_http.jar:../lib/ext/ApacheJMeter_core.jar:../lib/jorphan.jar:../lib/oro-2.0.8.jar
CP=${CP}:../lib/slf4j-api-1.7.25.jar:../lib/jcl-over-slf4j-1.7.25.jar:../lib/log4j-slf4j-impl-2.10.0.jar
CP=${CP}:../lib/log4j-api-2.10.0.jar:../lib/log4j-core-2.10.0.jar:../lib/log4j-1.2-api-2.10.0.jar

java -cp $CP org.apache.jmeter.protocol.http.control.HttpMirrorServer "$@"
