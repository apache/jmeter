@echo off

rem   $Id$
rem   Copyright 2001-2004 The Apache Software Foundation
rem 
rem   Licensed under the Apache License, Version 2.0 (the "License");
rem   you may not use this file except in compliance with the License.
rem   You may obtain a copy of the License at
rem 
rem       http://www.apache.org/licenses/LICENSE-2.0
rem 
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.

rem  ============================================
rem  Non-GUI version of JMETER.BAT (WinNT/2K only)
rem
rem  Drop a JMX file on this batch script, and it
rem  will run it in non-GUI mode, with a log file
rem  formed from the input file name but with the
rem  extension .jtl
rem
rem  Only the first parameter is used.
rem
rem  For other OSes, the script currently behaves
rem  like jmeter.bat - patches welcome!
rem
rem  ============================================
if not "%OS%"=="Windows_NT" goto win9xStart
:winNTStart
@setlocal

rem Need to check if we are using the 4NT shell...
if "%eval[2+2]" == "4" goto setup4NT

rem Change to directory containing this file, which must be in bin
if exist %~nx0 goto winNT1
echo Changing to JMeter home directory
cd %~dp0

:winNT1
rem On NT/2K grab all arguments at once
rem Check file is supplied
if a == a%1 goto winNT2
rem Check it has extension .jmx
if a%~x1 == a.jmx goto winNT3
:winNT2
echo Please supply a script name with the extension .jmx
goto :EOF
:winNT3
set JMETER_CMD_LINE_ARGS=-n -t %1 -l %~dpn1.jtl
echo %JMETER_CMD_LINE_ARGS%
goto doneStart

:setup4NT
set JMETER_CMD_LINE_ARGS=%$
goto doneStart

:win9xStart
rem Slurp the command line arguments.  This loop allows for an unlimited number of 
rem agruments (up to the command line limit, anyway).

set JMETER_CMD_LINE_ARGS=

:setupArgs
if %1a==a goto doneStart
set JMETER_CMD_LINE_ARGS=%JMETER_CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart
rem This label provides a place for the argument list loop to break out 
rem and for NT handling to skip to.

rem See the unix startup file for the rationale of the following parameters,
rem including some tuning recommendations
set HEAP=-Xms256m -Xmx256m
set NEW=-XX:NewSize=128m -XX:MaxNewSize=128m
set SURVIVOR=-XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=50%
set TENURING=-XX:MaxTenuringThreshold=2
set EVACUATION=-XX:MaxLiveObjectEvacuationRatio=20%
set RMIGC=-Dsun.rmi.dgc.client.gcInterval=600000 -Dsun.rmi.dgc.server.gcInterval=600000
set PERM=-XX:PermSize=64m -XX:MaxPermSize=64m
set DEBUG=-verbose:gc -XX:+PrintTenuringDistribution
set ARGS=%HEAP% %NEW% %SURVIVOR% %TENURING% %EVACUATION% %RMIGC% %PERM% %DEBUG%

java %JVM_ARGS% %ARGS% -jar ApacheJMeter.jar %JMETER_CMD_LINE_ARGS%
