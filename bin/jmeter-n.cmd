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

rem  ============================================
rem  Non-GUI version of JMETER.BAT
rem
rem  Drop a JMX file on this batch script, and it
rem  will run it in non-GUI mode, with a log file
rem  formed from the input file name but with the
rem  extension .jtl
rem
rem  Only the first parameter is used.
rem
rem  ============================================

rem Check file is supplied
if a == a%1 goto winNT2

rem Allow special name LAST
if LAST == %1 goto winNT3

rem Check it has extension .jmx
if "%~x1" == ".jmx" goto winNT3
:winNT2
echo Please supply a script name with the extension .jmx
pause
goto END
:winNT3

rem Change to script directory
pushd %~dp1

rem use same directory to find jmeter script
call "%~dp0"jmeter -n -t "%~nx1" -j "%~n1.log" -l "%~n1.jtl" %2 %3 %4 %5 %6 %7 %8 %9

popd

:END