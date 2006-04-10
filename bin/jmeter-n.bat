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
rem  Only works for Win2k.
rem
rem  ============================================

if "%OS%"=="Windows_NT" goto WinNT
echo "Sorry, this command file requires Windows NT/ 2000 / XP"
pause
goto END
:WinNT

rem Check file is supplied
if a == a%1 goto winNT2
rem Check it has extension .jmx
if a%~x1 == a.jmx goto winNT3
:winNT2
echo Please supply a script name with the extension .jmx
pause
goto :EOF
:winNT3

rem Change to script directory
cd /D %~dp1

rem use same directory to find jmeter script
%~dp0jmeter -n -t %~nx1 -l %~n1.jtl

:END