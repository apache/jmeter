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

if not "%ANT_HOME%" == "" goto java:
echo ANT_HOME is not set, please set it
goto eof:

:java
if not "%JAVA_HOME%" == "" goto start:
echo JAVA_HOME not set, please set
goto eof:

:start
set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%ANT_HOME%\lib\ant.jar
set OLDPATH=%PATH%
set PATH=%JAVA_HOME%\bin;%PATH%

rem All the jars are now resolved in build.xml
rem for %%i in (".\lib\*.jar") do if not "%%i" == ".\lib\jorphan.jar" CALL lcp %%i
rem for %%i in (".\ext\*.jar") do CALL lcp %%i

SET BUILDFILE=build.xml
echo %LOCALCLASSPATH%
java -classpath %LOCALCLASSPATH% org.apache.tools.ant.Main -buildfile %BUILDFILE% %1 %2 %3 %4 %5 %6
set PATH=%OLDPATH%

:eof
