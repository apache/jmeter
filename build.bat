@echo off

if not "%JAVA_HOME%" == "" goto start:
echo JAVA_HOME not set, please set
goto eof:

:start
set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar
set OLDPATH=%PATH%
set PATH=%JAVA_HOME%\bin;%PATH%

for %%i in (".\lib\*.jar") do CALL lcp %%i
for %%i in (".\ext\*.jar") do CALL lcp %%i

for %%i in (..\jakarta-site2\lib\*.jar) do CALL lcp %%i

SET BUILDFILE=build.xml
echo %LOCALCLASSPATH%
java -classpath %LOCALCLASSPATH% org.apache.tools.ant.Main -buildfile %BUILDFILE% %1 %2 %3 %4 %5 %6
set PATH=%OLDPATH%

:eof
