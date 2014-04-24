@echo off

rem   Licensed to the Apache Software Foundation (ASF) under one or more
rem   contributor license agreements.  See the NOTICE file distributed with
rem   this work for additional information regarding copyright ownership.
rem   The ASF licenses this file to You under the Apache License, Version 2.0
rem   (the "License"); you may not use this file except in compliance with
rem   the License.  You may obtain a copy of the License at
rem 
rem       http://www.apache.org/licenses/LICENSE-2.0
rem 
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.

rem set JAVA_HOME=c:/jdk1.5.0

setlocal

rem On NT/2K grab all arguments at once
set JMETER_CMD_LINE_ARGS=%*

rem See the unix startup file for the rationale of the following parameters,
rem including some tuning recommendations
set HEAP=-Xms256m -Xmx256m
set NEW=-XX:NewSize=128m -XX:MaxNewSize=128m
set SURVIVOR=-XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=50%
set TENURING=-XX:MaxTenuringThreshold=2
set PERM=-XX:PermSize=64m -XX:MaxPermSize=64m
set DEBUG=-verbose:gc -XX:+PrintTenuringDistribution
rem set ARGS=%HEAP% %NEW% %SURVIVOR% %TENURING% %PERM% %DEBUG%
set ARGS=-server -Xms128m -Xmx512m

%JAVA_HOME%/bin/java %JVM_ARGS% %ARGS% -jar ApacheJMeter.jar report %JMETER_CMD_LINE_ARGS%
