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

rem   ===============================================================
rem   Enviroment variables
rem   SERVER_PORT (optional) - define the rmiregistry and server port
rem
rem   JVM_ARGS - Java flags - these are handled by jmeter.bat
rem
rem   ===============================================================


REM Protect environment against changes
setlocal

if exist jmeter-server.bat goto winNT1
echo Changing to JMeter home directory
cd /D %~dp0
:winNT1

if exist %JMETER_HOME%\lib\ext\ApacheJMeter_core.jar goto setCP
echo Could not find ApacheJmeter_core.jar ...
REM Try to work out JMETER_HOME
echo ... Trying JMETER_HOME=..
set JMETER_HOME=..
if exist %JMETER_HOME%\lib\ext\ApacheJMeter_core.jar goto setCP
echo ... trying JMETER_HOME=.
set JMETER_HOME=.
if exist %JMETER_HOME%\lib\ext\ApacheJMeter_core.jar goto setCP
echo Cannot determine JMETER_HOME !
goto exit

:setCP
echo Found ApacheJMeter_core.jar

REM No longer need to create the rmiregistry as it is done by the server
REM set CLASSPATH=%JMETER_HOME%\lib\ext\ApacheJMeter_core.jar;%JMETER_HOME%\lib\jorphan.jar;%JMETER_HOME%\lib\logkit-1.2.jar

REM START rmiregistry %SERVER_PORT%
REM

rem On NT/2K grab all arguments at once
set JMETER_CMD_LINE_ARGS=%*

if not "%SERVER_PORT%" == "" goto port

call jmeter -s -j jmeter-server.log %JMETER_CMD_LINE_ARGS%
goto end


:port
call jmeter -Dserver_port=%SERVER_PORT% -s -j jmeter-server.log %JMETER_CMD_LINE_ARGS%

:end

rem No longer needed, as server is started in-process
rem taskkill /F /IM rmiregistry.exe

:exit