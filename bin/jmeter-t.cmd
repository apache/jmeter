@echo off

rem   $Id$
rem   Copyright 2004,2006 The Apache Software Foundation
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
rem
rem  Drop a JMX file on this batch script, and it
rem  will load it in the GUI.
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

rem Start in directory with JMX file
cd /d %~dp1

rem Prepend the directory in which this script resides in case not on path

%~dp0jmeter -t %~nx1

:END